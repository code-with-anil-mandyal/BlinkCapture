# BlinkCapture

Realtime eye blink detection and automatic photo capture using CameraX and Google ML Kit in Android Kotlin.

This project detects eye blink events in realtime using ML Kit face detection and automatically captures an image when a valid blink is detected.

> Note:
> This is a blink-triggered auto capture demo project and NOT a complete liveness detection or anti-spoofing solution.

---

# Features

- Realtime front camera preview using CameraX
- ML Kit face detection
- Eye open probability tracking
- Blink detection using state-based logic
- Automatic image capture on blink
- Captured image preview
- Android Native (Kotlin)

---

# Tech Stack

- Kotlin
- CameraX
- Google ML Kit Face Detection
- Android XML UI
- Glide

---

# How Blink Detection Works

The app continuously analyzes camera frames using ML Kit.

Blink detection flow:

Eyes Open  
→ Eyes Closed  
→ Eyes Open Again  
→ Capture Image

The implementation uses:
- eye probability thresholds
- blink duration validation
- cooldown logic
- reopen confirmation

to reduce false triggers.

---

# Dependencies

```gradle
implementation("androidx.camera:camera-core:1.4.2")
implementation("androidx.camera:camera-camera2:1.4.2")
implementation("androidx.camera:camera-lifecycle:1.4.2")
implementation("androidx.camera:camera-view:1.4.2")

implementation("com.google.mlkit:face-detection:16.1.7")

implementation("com.github.bumptech.glide:glide:4.16.0")
```

---

# Permissions

```xml
<uses-permission android:name="android.permission.CAMERA"/>
```

---

# Project Structure

```text
MainActivity
│
├── CameraX Setup
├── Frame Analyzer
├── ML Kit Face Detection
├── Blink Detection Logic
└── Image Capture
```

---

# Future Improvements

Planned improvements for future versions:

- Face alignment overlay
- Multiple face validation
- Better blink stability
- Head movement detection
- Smile detection
- Liveness detection pipeline
- Compose UI version
- SDK/library architecture

---

# Demo Flow

```text
Open App
→ Camera Opens
→ Face Detected
→ Blink
→ Image Captured
→ Preview Displayed
```

---

# Important Notes

- Works best in good lighting conditions
- Face should be clearly visible
- ML Kit eye probabilities may vary across devices
- This project is intended for learning and demo purposes

---

# Author

Anil Kumar

Android Developer | Kotlin | CameraX | ML Kit | Realtime Vision Processing
