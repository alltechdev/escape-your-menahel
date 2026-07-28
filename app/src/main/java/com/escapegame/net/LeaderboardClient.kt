package com.escapegame.net

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicReference

/** One row of the global leaderboard. */
data class LeaderboardEntry(
    val name: String,
    val score: Int,
    val mode: String,
    val difficulty: String
)

/**
 * Serverless global leaderboard, powered entirely by GitHub: reads
 * leaderboard.json anonymously from raw.githubusercontent.com (submissions
 * happen via GitHub issues processed by a repository Action — see
 * .github/workflows/leaderboard.yml).
 *
 * Fetches on a daemon thread and publishes into an [AtomicReference]; the
 * game thread just polls [latest], so no locking is needed anywhere.
 */
class LeaderboardClient {

    /** Outcome of the automatic run-end submission. */
    enum class SubmitState { IDLE, SENDING, OK, FAILED, UNAVAILABLE }

    private val result = AtomicReference<List<LeaderboardEntry>?>(null)

    @Volatile
    var submitState = SubmitState.IDLE
        private set

    @Volatile
    private var failed = false

    @Volatile
    private var fetching = false

    fun resetSubmitState() {
        submitState = SubmitState.IDLE
    }

    /**
     * Fires the score to the global board by opening a GitHub issue that the
     * leaderboard workflow validates and records. Requires the build-time
     * token; without it (or without internet) this quietly reports
     * UNAVAILABLE/FAILED and the game carries on — the leaderboard is
     * strictly optional.
     */
    fun submitScore(name: String, score: Int, mode: String, difficulty: String) {
        val token = com.escapegame.BuildConfig.LEADERBOARD_TOKEN
        if (token.isEmpty()) {
            submitState = SubmitState.UNAVAILABLE
            return
        }
        if (submitState == SubmitState.SENDING) return
        submitState = SubmitState.SENDING
        Thread({
            try {
                val checksum = (score.toLong() * 7919L + 5747L) % 99991L
                val title = "SCORE: $score-$checksum $mode $difficulty $name"
                val body = JSONObject()
                    .put("title", title)
                    .put("body", "Automated submission from the game.")
                    .toString()
                val connection = URL(SUBMIT_URL).openConnection() as HttpURLConnection
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.outputStream.use { it.write(body.toByteArray()) }
                val code = connection.responseCode
                connection.disconnect()
                submitState = if (code in 200..299) SubmitState.OK else SubmitState.FAILED
            } catch (e: Exception) {
                submitState = SubmitState.FAILED
            }
        }, "LeaderboardSubmit").apply { isDaemon = true }.start()
    }

    /** Most recently fetched entries, best first, or null if none yet. */
    fun latest(): List<LeaderboardEntry>? = result.get()

    fun hasFailed(): Boolean = failed

    /** Kicks off a background fetch; no-op if one is already running. */
    fun refresh() {
        if (fetching) return
        fetching = true
        failed = false
        Thread({
            try {
                val connection = URL(LEADERBOARD_URL).openConnection() as HttpURLConnection
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                val array = JSONObject(text).getJSONArray("entries")
                val entries = ArrayList<LeaderboardEntry>(array.length())
                for (i in 0 until array.length()) {
                    val row = array.getJSONObject(i)
                    entries.add(
                        LeaderboardEntry(
                            row.optString("name", "?"),
                            row.optInt("score", 0),
                            row.optString("mode", ""),
                            row.optString("difficulty", "")
                        )
                    )
                }
                result.set(entries.sortedByDescending { it.score })
            } catch (e: Exception) {
                failed = true
            } finally {
                fetching = false
            }
        }, "LeaderboardFetch").apply { isDaemon = true }.start()
    }

    private companion object {
        const val LEADERBOARD_URL =
            "https://raw.githubusercontent.com/alltechdev/escape-your-menahel/main/leaderboard.json"
        const val SUBMIT_URL =
            "https://api.github.com/repos/alltechdev/escape-your-menahel/issues"
    }
}
