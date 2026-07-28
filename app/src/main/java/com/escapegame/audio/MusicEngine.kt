package com.escapegame.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.sin

/** One-shot sound effects, synthesized like everything else. */
enum class Sfx {
    JUMP,
    THROW,
    PICKUP,
    POWERUP,
    STUN,
    CAUGHT,
    LEVEL_CLEAR
}

/**
 * Funky klezmer chiptune plus sound effects, synthesized at runtime — no
 * audio assets, no downloads, fully kosher for the tiniest APK.
 *
 * Music: a four-bar loop in D freygish (Ahava Raba mode): square-wave lead
 * with a little vibrato, triangle oom-pah bass, and offbeat chord stabs for
 * the funk. SFX are short synthesized voices mixed into the same stream.
 * Streams to an [AudioTrack] from its own low-priority thread; mute simply
 * zeroes the output so the loop position is preserved.
 */
class MusicEngine {

    @Volatile
    private var muted = false

    @Volatile
    private var running = false
    private var thread: Thread? = null

    private val voices = ConcurrentLinkedQueue<Voice>()

    fun isMuted(): Boolean = muted

    fun setMuted(value: Boolean) {
        muted = value
    }

    /** Queues a sound effect; cheap and thread-safe. */
    fun playSfx(type: Sfx) {
        if (voices.size < MAX_VOICES) voices.add(Voice(type))
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

        val mix = FloatArray(2048)
        val out = ShortArray(mix.size)
        var sample = 0L
        while (running) {
            for (i in mix.indices) {
                mix[i] = musicSample(sample + i)
            }
            sample += mix.size
            mixVoices(mix)
            for (i in mix.indices) {
                val value = if (muted) 0f else mix[i].coerceIn(-1f, 1f)
                out[i] = (value * 30000f).toInt().toShort()
            }
            // Blocking write paces the loop at real time
            track.write(out, 0, out.size)
        }
        track.stop()
        track.release()
    }

    // ------------------------------------------------------------- the tune

    // Oscillator phase accumulators (avoid float-precision drift over time)
    private var leadPhase = 0f
    private var bassPhase = 0f
    private var stabPhase = 0f
    private var vibratoPhase = 0f

    private fun musicSample(sample: Long): Float {
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

        return mix
    }

    private fun wrap(phase: Float): Float = if (phase >= 1f) phase - 1f else phase

    // ------------------------------------------------------------------ sfx

    private class Voice(val type: Sfx) {
        var pos = 0
    }

    private fun mixVoices(out: FloatArray) {
        val iterator = voices.iterator()
        while (iterator.hasNext()) {
            val voice = iterator.next()
            val duration = voiceDuration(voice.type)
            var i = 0
            while (i < out.size && voice.pos < duration) {
                out[i] += voiceSample(voice.type, voice.pos)
                voice.pos++
                i++
            }
            if (voice.pos >= duration) iterator.remove()
        }
    }

    private fun voiceDuration(type: Sfx): Int = when (type) {
        Sfx.JUMP -> (0.10f * SAMPLE_RATE).toInt()
        Sfx.THROW -> (0.08f * SAMPLE_RATE).toInt()
        Sfx.PICKUP -> (0.12f * SAMPLE_RATE).toInt()
        Sfx.POWERUP -> (0.24f * SAMPLE_RATE).toInt()
        Sfx.STUN -> (0.25f * SAMPLE_RATE).toInt()
        Sfx.CAUGHT -> (0.40f * SAMPLE_RATE).toInt()
        Sfx.LEVEL_CLEAR -> (0.48f * SAMPLE_RATE).toInt()
    }

    private fun voiceSample(type: Sfx, pos: Int): Float {
        val t = pos.toFloat() / SAMPLE_RATE
        return when (type) {
            Sfx.JUMP -> {
                // Rising square blip, 180 -> 520 Hz
                val phase = 180.0 * t + 1700.0 * t * t
                square(phase) * 0.20f * envelope(t, 0.10f)
            }
            Sfx.THROW -> {
                // A short "pfft" of noise
                noise(pos) * 0.16f * envelope(t, 0.08f)
            }
            Sfx.PICKUP -> {
                // Two-note ding: E5-ish then B5-ish
                val freq = if (t < 0.05f) 660.0 else 990.0
                sin(TWO_PI_D * freq * t).toFloat() * 0.22f * envelope(t, 0.12f)
            }
            Sfx.POWERUP -> {
                // Freygish arpeggio, one note per 60ms
                val notes = ARPEGGIO
                val idx = (t / 0.06f).toInt().coerceAtMost(notes.size - 1)
                val noteT = t - idx * 0.06f
                square(notes[idx] * t.toDouble()) * 0.17f * envelope(noteT, 0.06f)
            }
            Sfx.STUN -> {
                // Dizzy wobble: FM warble
                val arg = TWO_PI_D * 300.0 * t + 4.0 * sin(TWO_PI_D * 9.0 * t)
                sin(arg).toFloat() * 0.20f * envelope(t, 0.25f)
            }
            Sfx.CAUGHT -> {
                // Descending womp, 220 -> 70 Hz
                val phase = 220.0 * t - 187.5 * t * t
                square(phase) * 0.26f * envelope(t, 0.40f)
            }
            Sfx.LEVEL_CLEAR -> {
                // Little fanfare up the scale
                val notes = FANFARE
                val idx = (t / 0.12f).toInt().coerceAtMost(notes.size - 1)
                val noteT = t - idx * 0.12f
                square(notes[idx] * t.toDouble()) * 0.20f * envelope(noteT, 0.12f)
            }
        }
    }

    /** Square wave from a phase given in cycles. */
    private fun square(phaseCycles: Double): Float =
        if (sin(TWO_PI_D * phaseCycles) >= 0.0) 1f else -1f

    /** Linear decay envelope over [duration] seconds. */
    private fun envelope(t: Float, duration: Float): Float =
        (1f - t / duration).coerceIn(0f, 1f)

    /** Deterministic cheap noise from the sample index. */
    private fun noise(pos: Int): Float {
        val hashed = pos * 1103515245 + 12345
        return ((hashed ushr 16) and 0x7fff) / 16384f - 1f
    }

    private companion object {
        const val SAMPLE_RATE = 22050
        const val TWO_PI = (2.0 * Math.PI).toFloat()
        const val TWO_PI_D = 2.0 * Math.PI
        const val MAX_VOICES = 8

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

        val ARPEGGIO = doubleArrayOf(293.66, 369.99, 440.0, 587.33)
        val FANFARE = doubleArrayOf(293.66, 440.0, 587.33, 739.99)
    }
}
