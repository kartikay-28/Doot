# DOOT Speech Models Registry & Architecture Guide

This document maintains the exact record of both **V1 Baseline Models** and **V2 Upgraded Models**, their release URLs, file structures, and step-by-step instructions to switch or rollback at any time.

---

## 1. Current Upgraded Configuration (V2)

The V2 configuration targets **high-accuracy Indian accents**, **multilingual Whisper base**, and **natural offline TTS voice models**.

### A. Speech-to-Text (STT / ASR)
* **Model:** `sherpa-onnx-whisper-base` (Multilingual 74M Parameters)
* **Handles:** English (`en`), Hindi (`hi`), Punjabi (`pa`)
* **Download URL:**
  ```text
  https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-whisper-base.tar.bz2
  ```
* **Extracted Files in Language Folder (`models/<lang>/` or shared in `models/en/`):**
  * `encoder.onnx`
  * `decoder.onnx`
  * `tokens.txt`
* **Architecture Benefit:** Whisper Base understands Indian-English accents, native conversational Hindi, and native Punjabi with significantly lower hallucination rates than Whisper Tiny. `SttEngine.kt` automatically shares the model across languages, avoiding duplicate RAM consumption.

### B. English Text-to-Speech (TTS)
* **Model:** `kokoro-int8-en-v0_19` (Kokoro Studio-grade TTS)
* **Download URL:**
  ```text
  https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/kokoro-int8-en-v0_19.tar.bz2
  ```
  *(Full precision fallback: `https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/kokoro-en-v0_19.tar.bz2`)*
* **Extracted Files in `models/en/`:**
  * `model.onnx` (or `model.int8.onnx`)
  * `voices.bin`
  * `tokens.txt`
  * `espeak-ng-data/`
* **Sample Rate:** `24000 Hz` (handled dynamically by `TtsEngine.sampleRate`).

### C. Hindi Text-to-Speech (TTS)
* **Model:** `vits-piper-hi_IN-priyamvada-medium` (Natural Indian female voice)
* **Download URL:**
  ```text
  https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-hi_IN-priyamvada-medium.tar.bz2
  ```
* **Extracted Files in `models/hi/`:**
  * `tts.onnx`
  * `tts_tokens.txt`
  * `espeak-ng-data/`

### D. Punjabi Text-to-Speech (TTS)
* **Model:** `vits-piper-hi_IN-priyamvada-medium` (Shared natural Indian voice)
* **Download URL:**
  ```text
  https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-hi_IN-priyamvada-medium.tar.bz2
  ```
* **Extracted Files in `models/pa/`:**
  * `tts.onnx`
  * `tts_tokens.txt`
  * `espeak-ng-data/`

---

## 2. Baseline Configuration (V1) - Rollback Reference

If the upgraded models cause higher RAM usage or performance drops on low-end test hardware, use these exact links to rollback:

### A. Speech-to-Text (STT / ASR)
* **English:** `sherpa-onnx-zipformer-en-2023-06-26.tar.bz2`
  ```text
  https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-en-2023-06-26.tar.bz2
  ```
  * Files: `encoder.onnx`, `decoder.onnx`, `joiner.onnx`, `tokens.txt`
* **Hindi & Punjabi:** `sherpa-onnx-whisper-tiny.tar.bz2`
  ```text
  https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-whisper-tiny.tar.bz2
  ```
  * Files: `encoder.onnx`, `decoder.onnx`, `tokens.txt`

### B. Text-to-Speech (TTS)
* **English:** `vits-piper-en_US-amy-low.tar.bz2`
  ```text
  https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-en_US-amy-low.tar.bz2
  ```
* **Hindi & Punjabi:** `vits-piper-hi_IN-rohan-medium.tar.bz2`
  ```text
  https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-hi_IN-rohan-medium.tar.bz2
  ```

---

## 3. Dual-Engine Architecture Compatibility

The engine code (`TtsEngine.kt`, `SttEngine.kt`, `ModelAssets.kt`, `ModelDownloader.kt`) has been designed with **zero-friction backward and forward compatibility**:

1. **Auto-Detection for TTS:**
   - Checks for `model.onnx` + `voices.bin` $\rightarrow$ automatically loads **Kokoro**.
   - Checks for `tts.onnx` + `tts_tokens.txt` $\rightarrow$ automatically loads **VITS Piper**.
2. **Auto-Detection for STT:**
   - Checks for `joiner.onnx` $\rightarrow$ automatically loads **Zipformer Transducer**.
   - If no joiner exists $\rightarrow$ automatically loads **Whisper (Base or Tiny)** with the target language.
   - If Hindi/Punjabi lacks dedicated ASR files $\rightarrow$ reuses the installed multilingual Whisper model from `en/`.
