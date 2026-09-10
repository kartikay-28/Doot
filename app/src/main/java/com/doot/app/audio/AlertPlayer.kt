package com.doot.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlertPlayer(private val context: Context) {

    suspend fun playAlert(samples: FloatArray, sampleRate: Int) = withContext(Dispatchers.IO) {
        playAudio(samples, sampleRate, isAlert = true)
    }

    suspend fun playNormal(samples: FloatArray, sampleRate: Int) = withContext(Dispatchers.IO) {
        playAudio(samples, sampleRate, isAlert = false)
    }

    private fun playAudio(samples: FloatArray, sampleRate: Int, isAlert: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        val usage = if (isAlert) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA
        val contentType = if (isAlert) AudioAttributes.CONTENT_TYPE_SONIFICATION else AudioAttributes.CONTENT_TYPE_SPEECH

        if (isAlert) {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(contentType)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 4)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
        audioTrack.play()

        // Wait until playback completes
        while (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING && audioTrack.playbackHeadPosition < samples.size) {
            Thread.sleep(50)
        }

        audioTrack.release()

        if (isAlert) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
        }
    }
}
