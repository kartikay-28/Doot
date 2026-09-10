package com.doot.app.speech

import android.content.Context
import java.io.File

object ModelAssets {
    fun getModelPath(context: Context, path: String): String {
        return File(context.filesDir, "models/$path").absolutePath
    }

    fun isLanguageInstalled(context: Context, lang: String): Boolean {
        val baseDir = File(context.filesDir, "models/$lang")
        if (!baseDir.exists() || !baseDir.isDirectory) return false

        // Check for essential ASR files
        val hasAsr = File(baseDir, "encoder.onnx").exists() &&
                     File(baseDir, "decoder.onnx").exists() &&
                     File(baseDir, "tokens.txt").exists()

        if (!hasAsr) return false

        // Check for either Kokoro or VITS TTS
        val hasKokoro = File(baseDir, "model.onnx").exists() && File(baseDir, "voices.bin").exists()
        val hasVits = File(baseDir, "tts.onnx").exists() && File(baseDir, "tts_tokens.txt").exists()

        return hasKokoro || hasVits
    }

    fun isAnyLanguageInstalled(context: Context): Boolean {
        val supportedLangs = listOf("en", "hi", "pa")
        for (lang in supportedLangs) {
            if (isLanguageInstalled(context, lang)) return true
        }
        return false
    }
}
