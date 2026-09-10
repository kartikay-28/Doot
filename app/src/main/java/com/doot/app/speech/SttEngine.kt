package com.doot.app.speech

import android.content.Context
import android.util.Log
import java.io.File
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SttEngine(private val context: Context) {
    private var recognizer: OfflineRecognizer? = null
    private var currentLang: String? = null

    fun loadModel(lang: String) {
        if (currentLang == lang && recognizer != null) return

        release()

        val baseLang = when {
            File(ModelAssets.getModelPath(context, "$lang/encoder.onnx")).exists() -> lang
            File(ModelAssets.getModelPath(context, "en/encoder.onnx")).exists() -> "en"
            else -> lang
        }

        val tokens = ModelAssets.getModelPath(context, "$baseLang/tokens.txt")
        val encoder = ModelAssets.getModelPath(context, "$baseLang/encoder.onnx")
        val decoder = ModelAssets.getModelPath(context, "$baseLang/decoder.onnx")
        val joinerPath = ModelAssets.getModelPath(context, "$baseLang/joiner.onnx")
        val joinerFile = java.io.File(joinerPath)

        val modelConfig = if (joinerFile.exists()) {
            OfflineModelConfig(
                transducer = OfflineTransducerModelConfig(
                    encoder = encoder,
                    decoder = decoder,
                    joiner = joinerPath
                ),
                tokens = tokens,
                numThreads = 4,
                debug = true
            )
        } else {
            OfflineModelConfig(
                whisper = com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig(
                    encoder = encoder,
                    decoder = decoder,
                    language = lang,
                    task = "transcribe"
                ),
                tokens = tokens,
                numThreads = 4,
                debug = true
            )
        }

        val config = OfflineRecognizerConfig(
            modelConfig = modelConfig
        )
        
        try {
            recognizer = OfflineRecognizer(
                assetManager = null,
                config = config
            )
            currentLang = lang
            Log.i("SttEngine", "Loaded model for lang: $lang")
        } catch (e: Exception) {
            Log.e("SttEngine", "Failed to load STT model", e)
        }
    }

    suspend fun recognize(samples: FloatArray): String = withContext(Dispatchers.Default) {
        val rec = recognizer ?: return@withContext ""
        val stream = rec.createStream()
        stream.acceptWaveform(samples, sampleRate = 16000)
        rec.decode(stream)
        val result = rec.getResult(stream)
        stream.release()
        result.text
    }

    fun release() {
        recognizer?.release()
        recognizer = null
        currentLang = null
    }
}
