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

    private val result = AtomicReference<List<LeaderboardEntry>?>(null)

    @Volatile
    private var failed = false

    @Volatile
    private var fetching = false

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
    }
}
