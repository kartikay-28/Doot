$ErrorActionPreference = "Stop"

$AssetsDir = "app\src\main\assets\models\en"
if (!(Test-Path -Path $AssetsDir)) {
    New-Item -ItemType Directory -Force -Path $AssetsDir | Out-Null
}

$TempDir = "app\build\temp_models"
if (!(Test-Path -Path $TempDir)) {
    New-Item -ItemType Directory -Force -Path $TempDir | Out-Null
}

Write-Host "Downloading ASR English..."
Invoke-WebRequest -Uri "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-en-2023-06-26.tar.bz2" -OutFile "$TempDir\asr_en.tar.bz2"

Write-Host "Downloading TTS English..."
Invoke-WebRequest -Uri "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-en_US-amy-low.tar.bz2" -OutFile "$TempDir\tts_en.tar.bz2"

Write-Host "Extracting..."
cd $TempDir
tar -xf asr_en.tar.bz2
tar -xf tts_en.tar.bz2
cd ..\..\..

Write-Host "Copying ASR files..."
$AsrDir = "$TempDir\sherpa-onnx-zipformer-en-2023-06-26"
Copy-Item "$AsrDir\tokens.txt" "$AssetsDir\tokens.txt" -Force
Copy-Item "$AsrDir\encoder-epoch-99-avg-1.onnx" "$AssetsDir\encoder.onnx" -Force
Copy-Item "$AsrDir\decoder-epoch-99-avg-1.onnx" "$AssetsDir\decoder.onnx" -Force
Copy-Item "$AsrDir\joiner-epoch-99-avg-1.onnx" "$AssetsDir\joiner.onnx" -Force

Write-Host "Copying TTS files..."
$TtsDir = "$TempDir\vits-piper-en_US-amy-low"
Copy-Item "$TtsDir\en_US-amy-low.onnx" "$AssetsDir\tts.onnx" -Force
Copy-Item "$TtsDir\tokens.txt" "$AssetsDir\tts_tokens.txt" -Force

Copy-Item -Recurse "$TtsDir\espeak-ng-data" "$AssetsDir\espeak-ng-data" -Force

Write-Host "Cleaning up temp files..."
Remove-Item -Recurse -Force $TempDir

Write-Host "Model setup complete!"
