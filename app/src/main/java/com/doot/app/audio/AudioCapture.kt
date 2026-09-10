package com.doot.app.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AudioCapture {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val bufferSize = AudioRecord.getMinBufferSize(
        16000,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(1024)

    private val samples = mutableListOf<Short>()
    var isRecording = false
        private set

    @SuppressLint("MissingPermission")
    fun start() {
        if (isRecording) return
        samples.clear()

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize * 2
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord?.release()
            audioRecord = null
            return
        }

        audioRecord?.startRecording()
        isRecording = true

        recordingJob = scope.launch {
            val audioData = ShortArray(bufferSize)
            // Max 5 seconds: 16000 samples/sec * 5 sec = 80000 samples
            val maxSamples = 80000
            
            while (isRecording && samples.size < maxSamples) {
                val readSize = audioRecord?.read(audioData, 0, bufferSize) ?: 0
                if (readSize > 0) {
                    for (i in 0 until readSize) {
                        samples.add(audioData[i])
                    }
                }
            }
            if (isRecording) {
                stop() // Auto stop if we hit max duration
            }
        }
    }

    fun stop(): FloatArray {
        if (!isRecording) return FloatArray(0)
        
        isRecording = false
        recordingJob?.cancel()
        
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        val floatSamples = FloatArray(samples.size)
        for (i in samples.indices) {
            floatSamples[i] = samples[i] / 32768f
        }
        return floatSamples
    }
}
