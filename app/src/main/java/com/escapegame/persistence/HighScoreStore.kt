package com.escapegame.persistence

import android.content.Context

/** Persists the best escape (high score) across app launches. */
class HighScoreStore(context: Context) {

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

    private companion object {
        const val PREFS_NAME = "escape_menahel"
        const val KEY_HIGH_SCORE = "high_score"
    }
}
