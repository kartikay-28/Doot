package com.doot.app

sealed class AppState {
    object Disconnected : AppState()
    object Connected : AppState()
    object Recording : AppState()
    object Transcribing : AppState()
    object Sending : AppState()
    object Receiving : AppState()
    object Speaking : AppState()
}
