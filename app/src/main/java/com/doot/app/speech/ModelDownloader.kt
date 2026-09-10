package com.doot.app.speech

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float, val label: String) : DownloadState()
    data class Extracting(val label: String) : DownloadState()
    object Done : DownloadState()
    data class Error(val error: String) : DownloadState()
}

class ModelDownloader(private val context: Context) {
    companion object {
        // Upgraded High-Accuracy Models
        val UPGRADED_ASR_WHISPER_BASE = "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-whisper-base.tar.bz2"
        val UPGRADED_TTS_KOKORO_EN = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/kokoro-int8-en-v0_19.tar.bz2"
        val UPGRADED_TTS_HINDI_FEMALE = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-hi_IN-priyamvada-medium.tar.bz2"

        // Baseline (V1) Models for instant rollback if needed
        val BASELINE_ASR_EN_ZIPFORMER = "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-en-2023-06-26.tar.bz2"
        val BASELINE_ASR_WHISPER_TINY = "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-whisper-tiny.tar.bz2"
        val BASELINE_TTS_EN_AMY = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-en_US-amy-low.tar.bz2"
        val BASELINE_TTS_HI_ROHAN = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-hi_IN-rohan-medium.tar.bz2"
    }

    private val asrLinks = mapOf(
        "en" to UPGRADED_ASR_WHISPER_BASE,
        "hi" to UPGRADED_ASR_WHISPER_BASE,
        "pa" to UPGRADED_ASR_WHISPER_BASE
    )

    private val ttsLinks = mapOf(
        "en" to UPGRADED_TTS_KOKORO_EN,
        "hi" to UPGRADED_TTS_HINDI_FEMALE,
        "pa" to UPGRADED_TTS_HINDI_FEMALE
    )

    fun downloadLanguage(lang: String): Flow<DownloadState> = flow {
        try {
            val asrUrl = asrLinks[lang] ?: throw Exception("ASR link not found for $lang")
            val ttsUrl = ttsLinks[lang] ?: throw Exception("TTS link not found for $lang")

            val baseDir = File(context.filesDir, "models/$lang")
            baseDir.mkdirs()

            val asrArchive = File(context.cacheDir, "asr_$lang.tar.bz2")
            val ttsArchive = File(context.cacheDir, "tts_$lang.tar.bz2")

            // 1. Download ASR
            downloadFile(asrUrl, asrArchive) { progress ->
                emit(DownloadState.Downloading(progress * 0.5f, "Downloading Speech Model..."))
            }

            // 2. Download TTS
            downloadFile(ttsUrl, ttsArchive) { progress ->
                emit(DownloadState.Downloading(0.5f + (progress * 0.5f), "Downloading Voice Model..."))
            }

            // 3. Extract ASR
            emit(DownloadState.Extracting("Extracting Speech Model..."))
            extractTarBz2(asrArchive, baseDir, isAsr = true)

            // 4. Extract TTS
            emit(DownloadState.Extracting("Extracting Voice Model..."))
            extractTarBz2(ttsArchive, baseDir, isAsr = false)

            // 5. Cleanup
            asrArchive.delete()
            ttsArchive.delete()

            emit(DownloadState.Done)
        } catch (e: Exception) {
            Log.e("ModelDownloader", "Download failed", e)
            emit(DownloadState.Error(e.localizedMessage ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun downloadFile(urlString: String, dest: File, onProgress: suspend (Float) -> Unit) {
        val client = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        val request = Request.Builder()
            .url(urlString)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP Error ${response.code}")

            val body = response.body ?: throw Exception("Empty body")
            val fileLength = body.contentLength()
            val input = BufferedInputStream(body.byteStream())
            val output = FileOutputStream(dest)

            val data = ByteArray(8192)
            var total: Long = 0
            var count: Int
            var lastEmitTime = 0L

            while (input.read(data).also { count = it } != -1) {
                total += count
                output.write(data, 0, count)
                if (fileLength > 0) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastEmitTime > 100) {
                        onProgress(total.toFloat() / fileLength.toFloat())
                        lastEmitTime = currentTime
                    }
                }
            }
            output.flush()
            output.close()
        }
    }

    private fun extractTarBz2(archive: File, destDir: File, isAsr: Boolean) {
        archive.inputStream().use { fileIn ->
            BufferedInputStream(fileIn).use { bufIn ->
                BZip2CompressorInputStream(bufIn).use { bzIn ->
                    TarArchiveInputStream(bzIn).use { tarIn ->
                        var entry = tarIn.nextTarEntry
                        while (entry != null) {
                            if (!entry.isDirectory) {
                                val targetName = if (isAsr) {
                                    val name = entry.name.substringAfterLast("/")
                                    when {
                                        name.endsWith("tokens.txt") -> "tokens.txt"
                                        name.contains("encoder") && name.endsWith(".onnx") -> "encoder.onnx"
                                        name.contains("decoder") && name.endsWith(".onnx") -> "decoder.onnx"
                                        name.contains("joiner") && name.endsWith(".onnx") -> "joiner.onnx"
                                        else -> null
                                    }
                                } else {
                                    if (entry.name.contains("/espeak-ng-data/")) {
                                        "espeak-ng-data/" + entry.name.substringAfter("/espeak-ng-data/")
                                    } else {
                                        val name = entry.name.substringAfterLast("/")
                                        when {
                                            // Kokoro files
                                            name == "voices.bin" -> "voices.bin"
                                            name.contains("model") && name.endsWith(".onnx") -> "model.onnx"
                                            entry.name.contains("kokoro") && name.endsWith("tokens.txt") -> "tokens.txt"
                                            // VITS files
                                            name.endsWith(".onnx") -> "tts.onnx"
                                            name.endsWith("tokens.txt") -> "tts_tokens.txt"
                                            else -> null
                                        }
                                    }
                                }

                                if (targetName != null && !targetName.endsWith("/")) {
                                    val outFile = File(destDir, targetName)
                                    outFile.parentFile?.mkdirs()
                                    FileOutputStream(outFile).use { out ->
                                        val buffer = ByteArray(8192)
                                        var len: Int
                                        while (tarIn.read(buffer).also { len = it } != -1) {
                                            out.write(buffer, 0, len)
                                        }
                                    }
                                }
                            }
                            entry = tarIn.nextTarEntry
                        }
                    }
                }
            }
        }
    }
}
