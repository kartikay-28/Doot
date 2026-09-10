package com.doot.app.speech

import android.content.Context
import android.util.Log
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TtsEngine(private val context: Context) {
    private var tts: OfflineTts? = null
    private var currentLang: String? = null
    var sampleRate: Int = 22050
        private set

    fun loadModel(lang: String) {
        if (currentLang == lang && tts != null) return

        release()

        val model = ModelAssets.getModelPath(context, "$lang/tts.onnx")
        val lexicon = ""
        val tokens = ModelAssets.getModelPath(context, "$lang/tts_tokens.txt")
        val dataDir = ModelAssets.getModelPath(context, "$lang/espeak-ng-data")

        val config = OfflineTtsConfig(
            model = OfflineTtsModelConfig(
                vits = OfflineTtsVitsModelConfig(
                    model = model,
                    lexicon = lexicon,
                    tokens = tokens,
                    dataDir = dataDir
                ),
                numThreads = 4,
                debug = true
            )
        )

        try {
            tts = OfflineTts(
                assetManager = null,
                config = config
            )
            currentLang = lang
            sampleRate = tts?.sampleRate() ?: 22050
            Log.i("TtsEngine", "Loaded TTS model for lang: $lang, SampleRate: $sampleRate")
        } catch (e: Exception) {
            Log.e("TtsEngine", "Failed to load TTS model", e)
        }
    }

    suspend fun synthesize(text: String): FloatArray = withContext(Dispatchers.Default) {
        val ttsEngine = tts ?: return@withContext FloatArray(0)
        try {
            val audio = ttsEngine.generate(text, sid = 0, speed = 1.0f)
            audio.samples
        } catch (e: Exception) {
            Log.e("TtsEngine", "Failed to synthesize text: $text", e)
            FloatArray(0)
        }
    }

    fun release() {
        tts?.release()
        tts = null
        currentLang = null
    }
}
