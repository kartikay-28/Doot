package com.doot.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.doot.app.speech.DownloadState
import com.doot.app.speech.ModelAssets
import com.doot.app.speech.ModelDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class LangState(
    val code: String,
    val name: String,
    val isInstalled: Boolean,
    val downloadState: DownloadState = DownloadState.Idle
)

class SetupViewModel(application: Application) : AndroidViewModel(application) {
    private val downloader = ModelDownloader(application)

    private val _languages = MutableStateFlow<List<LangState>>(emptyList())
    val languages: StateFlow<List<LangState>> = _languages.asStateFlow()

    init {
        refreshLanguageState()
    }

    fun refreshLanguageState() {
        val app = getApplication<Application>()
        _languages.value = listOf(
            LangState("en", "English", ModelAssets.isLanguageInstalled(app, "en")),
            LangState("hi", "Hindi", ModelAssets.isLanguageInstalled(app, "hi")),
            LangState("pa", "Punjabi", ModelAssets.isLanguageInstalled(app, "pa"))
        )
    }

    fun downloadLanguage(code: String) {
        viewModelScope.launch {
            downloader.downloadLanguage(code).collect { state ->
                _languages.value = _languages.value.map { lang ->
                    if (lang.code == code) {
                        lang.copy(
                            downloadState = state,
                            isInstalled = if (state is DownloadState.Done) true else lang.isInstalled
                        )
                    } else {
                        lang
                    }
                }
            }
        }
    }

    fun deleteLanguage(code: String) {
        val app = getApplication<Application>()
        val baseDir = File(app.filesDir, "models/$code")
        if (baseDir.exists()) {
            baseDir.deleteRecursively()
        }
        refreshLanguageState()
    }
}
