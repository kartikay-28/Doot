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

        // Check for essential files to verify successful extraction
        val requiredFiles = listOf(
            "encoder.onnx",
            "decoder.onnx",
            "tokens.txt",
            "tts.onnx",
            "tts_tokens.txt"
        )
        for (f in requiredFiles) {
            if (!File(baseDir, f).exists()) return false
        }
        return true
    }

    fun isAnyLanguageInstalled(context: Context): Boolean {
        val supportedLangs = listOf("en", "hi", "pa")
        for (lang in supportedLangs) {
            if (isLanguageInstalled(context, lang)) return true
        }
        return false
    }
}
