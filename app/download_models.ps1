$ErrorActionPreference = "Stop"

$AssetsDir = "src\main\assets\models"
if (!(Test-Path -Path $AssetsDir)) {
    New-Item -ItemType Directory -Force -Path $AssetsDir | Out-Null
}

Write-Host "Please download the following models and extract them into the assets folder:"
Write-Host "1. ASR English: https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-en-2023-06-26.tar.bz2"
Write-Host "2. TTS English: https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-en_US-amy-low.tar.bz2"
Write-Host "3. ASR Hindi: (Check sherpa-onnx docs for the exact zipformer model)"
Write-Host "4. ASR & TTS Punjabi: (Check sherpa-onnx docs for Punjabi/Multilingual models)"
Write-Host "For this prototype, ensure the extracted models are placed in src/main/assets/models/en, src/main/assets/models/hi, and src/main/assets/models/pa."
