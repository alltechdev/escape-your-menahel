package com.escapegame.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.sin

/**
 * Funky klezmer chiptune, synthesized at runtime — no audio assets, no
 * downloads, fully kosher for the tiniest APK.
 *
 * A four-bar loop in D freygish (Ahava Raba mode): square-wave lead with a
 * little vibrato, triangle oom-pah bass, and offbeat chord stabs for the
 * funk. Streams to an [AudioTrack] from its own low-priority thread; mute
 * simply zeroes the output so the loop position is preserved.
 */
class MusicEngine {

    @Volatile
    private var muted = false

    @Volatile
    private var running = false
    private var thread: Thread? = null

    fun isMuted(): Boolean = muted

    fun setMuted(value: Boolean) {
        muted = value
    }

    fun start() {
        if (thread?.isAlive == true) return
        running = true
        thread = Thread(this::runLoop, "MusicEngine").also {
            it.isDaemon = true
            it.priority = Thread.MIN_PRIORITY + 1
            it.start()
        }
    }

    fun stop() {
        running = false
        try {
            thread?.join(500)
        } catch (e: InterruptedException) {
            // Shutting down anyway
        }
        thread = null
    }

    private fun runLoop() {
        val minBuffer = AudioTrack.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) return
        @Suppress("DEPRECATION")
        val track = AudioTrack(
            AudioManager.STREAM_MUSIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBuffer, 8192),
            AudioTrack.MODE_STREAM
        )
        if (track.state != AudioTrack.STATE_INITIALIZED) {
            track.release()
            return
        }
        track.play()

        val buffer = ShortArray(2048)
        var sample = 0L
        while (running) {
            for (i in buffer.indices) {
                buffer[i] = synthesize(sample + i)
            }
            sample += buffer.size
            // Blocking write paces the loop at real time
            track.write(buffer, 0, buffer.size)
        }
        track.stop()
        track.release()
    }

    // Oscillator phase accumulators (avoid float-precision drift over time)
    private var leadPhase = 0f
    private var bassPhase = 0f
    private var stabPhase = 0f
    private var vibratoPhase = 0f

    private fun synthesize(sample: Long): Short {
        val slot = ((sample / SAMPLES_PER_EIGHTH) % SLOTS).toInt()
        val posInSlot = (sample % SAMPLES_PER_EIGHTH).toFloat() / SAMPLES_PER_EIGHTH

        var mix = 0f

        // Lead: square wave with vibrato and a per-note decay envelope
        val leadFreq = LEAD[slot]
        vibratoPhase = wrap(vibratoPhase + 5.5f / SAMPLE_RATE)
        if (leadFreq > 0f) {
            val vibrato = 1f + 0.006f * sin(TWO_PI * vibratoPhase)
            leadPhase = wrap(leadPhase + leadFreq * vibrato / SAMPLE_RATE)
            val square = if (leadPhase < 0.5f) 1f else -1f
            mix += square * 0.16f * (1f - 0.65f * posInSlot)
        }

        // Bass: triangle "oom" on the beat, sustained across two slots
        val bassFreq = BASS[slot and 0x1E]
        if (bassFreq > 0f) {
            bassPhase = wrap(bassPhase + bassFreq / SAMPLE_RATE)
            val triangle = 4f * kotlin.math.abs(bassPhase - 0.5f) - 1f
            val bassPos = ((slot and 1) + posInSlot) / 2f
            mix += triangle * 0.24f * (1f - 0.45f * bassPos)
        }

        // Offbeat "pah": a short chord stab for the funk
        if (slot and 1 == 1 && posInSlot < 0.5f) {
            val stabFreq = STAB_TONES[slot / 8]
            stabPhase = wrap(stabPhase + stabFreq / SAMPLE_RATE)
            val square = if (stabPhase < 0.5f) 1f else -1f
            mix += square * 0.07f * (1f - 2f * posInSlot)
        }

        if (muted) return 0
        val clamped = mix.coerceIn(-1f, 1f)
        return (clamped * 30000f).toInt().toShort()
    }

    private fun wrap(phase: Float): Float = if (phase >= 1f) phase - 1f else phase

    private companion object {
        const val SAMPLE_RATE = 22050
        const val TWO_PI = (2.0 * Math.PI).toFloat()

        // ~140 BPM: one eighth note per slot
        const val SAMPLES_PER_EIGHTH = 4725L
        const val SLOTS = 32

        // D freygish (Ahava Raba): D Eb F# G A Bb C
        const val D3 = 146.83f
        const val G2 = 98.00f
        const val A2 = 110.00f
        const val BB2 = 116.54f
        const val C3 = 130.81f
        const val D4 = 293.66f
        const val EB4 = 311.13f
        const val FS4 = 369.99f
        const val G4 = 392.00f
        const val A4 = 440.00f
        const val BB4 = 466.16f
        const val C5 = 523.25f
        const val D5 = 587.33f

        /** Four bars of melody, one note per eighth (0 = rest). */
        val LEAD = floatArrayOf(
            D4, FS4, A4, D5, C5, BB4, A4, G4,
            FS4, G4, A4, BB4, A4, G4, FS4, EB4,
            D4, D4, FS4, FS4, A4, A4, D5, D5,
            C5, BB4, A4, G4, FS4, EB4, D4, 0f
        )

        /** Oom-pah bass: notes land on even slots, sustained through the odd. */
        val BASS = floatArrayOf(
            D3, 0f, A2, 0f, D3, 0f, A2, 0f,
            C3, 0f, G2, 0f, C3, 0f, G2, 0f,
            D3, 0f, A2, 0f, D3, 0f, A2, 0f,
            BB2, 0f, G2, 0f, A2, 0f, A2, 0f
        )

        /** One stab chord tone per bar. */
        val STAB_TONES = floatArrayOf(220f, 196f, 220f, 220f)
    }
}
