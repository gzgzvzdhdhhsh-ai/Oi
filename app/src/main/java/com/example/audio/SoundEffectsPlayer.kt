package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

object SoundEffectsPlayer {
    private const val SAMPLE_RATE = 16000

    fun playEffect(effectId: String, scope: CoroutineScope, onPcmGenerated: ((ByteArray) -> Unit)? = null) {
        scope.launch(Dispatchers.Default) {
            val pcmData = when (effectId) {
                "airhorn" -> generateAirHornPcm()
                "applause" -> generateApplausePcm()
                "cheer" -> generateCheerPcm()
                "laugh" -> generateLaughPcm()
                "bell" -> generateBellPcm()
                "whistle" -> generateWhistlePcm()
                else -> generateBellPcm()
            }

            onPcmGenerated?.invoke(pcmData)
            playPcm(pcmData)
        }
    }

    private fun playPcm(pcm: ByteArray) {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, pcm.size)
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(pcm, 0, pcm.size)
            audioTrack.play()

            // Release after playing
            val durationMs = (pcm.size.toLong() * 1000L) / (SAMPLE_RATE * 2)
            Thread.sleep(durationMs + 100)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateAirHornPcm(): ByteArray {
        val durationSec = 1.2
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val pcm = ByteArray(numSamples * 2)
        val f1 = 280.0
        val f2 = 370.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = when {
                t < 0.05 -> t / 0.05
                t > 1.0 -> (1.2 - t) / 0.2
                else -> 1.0
            }
            val sampleVal = (sin(2.0 * Math.PI * f1 * t) * 0.5 + sin(2.0 * Math.PI * f2 * t) * 0.5) * envelope
            val shortVal = (sampleVal * 28000).toInt().coerceIn(-32767, 32767).toShort()
            pcm[i * 2] = (shortVal.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((shortVal.toInt() shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    private fun generateApplausePcm(): ByteArray {
        val durationSec = 1.6
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val pcm = ByteArray(numSamples * 2)
        val random = Random(42)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = (1.6 - t) / 1.6
            val clapBurst = if (i % 800 < 200) 1.5 else 0.4
            val noise = (random.nextDouble() * 2.0 - 1.0) * decay * clapBurst
            val shortVal = (noise * 22000).toInt().coerceIn(-32767, 32767).toShort()
            pcm[i * 2] = (shortVal.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((shortVal.toInt() shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    private fun generateCheerPcm(): ByteArray {
        val durationSec = 1.4
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val pcm = ByteArray(numSamples * 2)
        val chords = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C major

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val chordIdx = ((t / durationSec) * 4).toInt().coerceIn(0, 3)
            val freq = chords[chordIdx]
            val envelope = sin(Math.PI * (t % (durationSec / 4)) / (durationSec / 4))
            val sampleVal = sin(2.0 * Math.PI * freq * t) * envelope
            val shortVal = (sampleVal * 26000).toInt().coerceIn(-32767, 32767).toShort()
            pcm[i * 2] = (shortVal.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((shortVal.toInt() shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    private fun generateLaughPcm(): ByteArray {
        val durationSec = 1.5
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val pcm = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val burstIdx = (t * 5).toInt()
            val burstT = (t * 5) - burstIdx
            val envelope = sin(Math.PI * burstT.coerceIn(0.0, 1.0)) * 0.8
            val freq = 440.0 + (burstIdx % 2) * 80.0
            val sampleVal = sin(2.0 * Math.PI * freq * t) * envelope
            val shortVal = (sampleVal * 25000).toInt().coerceIn(-32767, 32767).toShort()
            pcm[i * 2] = (shortVal.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((shortVal.toInt() shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    private fun generateBellPcm(): ByteArray {
        val durationSec = 1.0
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val pcm = ByteArray(numSamples * 2)
        val freq = 1318.51 // E6 note

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = kotlin.math.exp(-3.5 * t)
            val sampleVal = sin(2.0 * Math.PI * freq * t) * decay
            val shortVal = (sampleVal * 26000).toInt().coerceIn(-32767, 32767).toShort()
            pcm[i * 2] = (shortVal.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((shortVal.toInt() shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    private fun generateWhistlePcm(): ByteArray {
        val durationSec = 0.9
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val pcm = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 1800.0 + (t / durationSec) * 900.0
            val envelope = sin(Math.PI * (t / durationSec))
            val sampleVal = sin(2.0 * Math.PI * freq * t) * envelope
            val shortVal = (sampleVal * 24000).toInt().coerceIn(-32767, 32767).toShort()
            pcm[i * 2] = (shortVal.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((shortVal.toInt() shr 8) and 0xFF).toByte()
        }
        return pcm
    }
}
