package com.escapegame.persistence

import android.content.Context

/** Persists the best escape (high score) and settings across app launches. */
class GamePrefs(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getHighScore(): Int = prefs.getInt(KEY_HIGH_SCORE, 0)

    /** Records [score] if it beats the current best. Returns true on a new record. */
    fun submitScore(score: Int): Boolean {
        if (score > getHighScore()) {
            prefs.edit().putInt(KEY_HIGH_SCORE, score).apply()
            return true
        }
        return false
    }

    fun isMusicMuted(): Boolean = prefs.getBoolean(KEY_MUTED, false)

    fun setMusicMuted(muted: Boolean) {
        prefs.edit().putBoolean(KEY_MUTED, muted).apply()
    }

    fun getDifficultyName(default: String): String =
        prefs.getString(KEY_DIFFICULTY, default) ?: default

    fun setDifficultyName(name: String) {
        prefs.edit().putString(KEY_DIFFICULTY, name).apply()
    }

    fun isAchieved(name: String): Boolean = prefs.getBoolean("ach_" + name, false)

    fun setAchieved(name: String) {
        prefs.edit().putBoolean("ach_" + name, true).apply()
    }

    fun getCounter(name: String): Int = prefs.getInt("cnt_" + name, 0)

    /** Increments a lifetime counter and returns the new value. */
    fun incrementCounter(name: String): Int {
        val value = getCounter(name) + 1
        prefs.edit().putInt("cnt_" + name, value).apply()
        return value
    }

    private companion object {
        const val PREFS_NAME = "escape_menahel"
        const val KEY_HIGH_SCORE = "high_score"
        const val KEY_MUTED = "music_muted"
        const val KEY_DIFFICULTY = "difficulty"
    }
}
