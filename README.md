# Random Cat Playground 🐱✨

A playful, tactile, and native Android application designed to discover random cat photos and GIFs, create funny memes and captions, apply visual effects, and effortlessly share or save them to your device.

Built with **Jetpack Compose**, **Material 3**, and modern Android architecture following **Claymorphism** visual styling and **C.R.A.P.** design principles.

---

## 📱 Features

- **🐾 Discover Random Cats & GIFs**: Fetch high-resolution cat photos or animated GIFs from the [Cataas (Cat as a service)](https://cataas.com) API with smooth crossfades and zero screen flicker.
- **🏷️ Category & Tag Filtering**: Browse by categories such as *Cute*, *Sleepy*, *Orange*, *Kitten*, *Funny*, *Black*, *Playful*, and *GIF*.
- **🎨 Visual Filters**: Instantly preview and apply visual effects (*Original*, *Monochrome / B&W*, *Warm*, *Negative*, *Sepia*, *Cool*).
- **📝 Caption & Meme Editor**:
  - Add custom captions or generate one-tap fun presets (*"If I fits, I sits"*, *"Feed me right meow!"*, etc.).
  - Choose text placement (*Top*, *Center*, *Bottom*).
  - Customize text colors (*White*, *Butter*, *Mint*, *Lavender*, *Yellow*, *Red*, *Black*) with classic high-contrast meme outlines.
- **💾 Offline Favorites & History**:
  - Save favorite cats directly to app storage with Room database support for guaranteed offline access.
  - Recent history buffer capped at 50 cats with one-tap clear functionality.
- **🚀 Native Sharing & Gallery Export**:
  - Export rendered memes and cat images directly to device Photos / MediaStore.
  - Share securely via Android's native Sharesheet using `FileProvider`.
- **🧸 Claymorphic Design & Accessibility**:
  - Soft pastel palette (`#FFF7EF`, `#E7DEFF`, `#D7F2E5`, `#6050C5`, `#FFE6B6`).
  - Tactile 3D clay cards and pill chips with spring-like pressed state animations.
  - Supports System, Light Clay, and Dark themes, as well as a "Reduce Animations" accessibility toggle.
- **🛡️ Family & Child Safe**:
  - No user accounts, zero analytics trackers, and zero personal data collection.

---

## 🛠️ Architecture & Tech Stack

| Layer | Technology |
|---|---|
| **UI** | Jetpack Compose + Material 3 |
| **Design System** | Claymorphism + C.R.A.P. (Contrast, Repetition, Alignment, Proximity) |
| **State Management** | Android ViewModel + StateFlow |
| **Asynchronous** | Kotlin Coroutines & Flow |
| **Networking** | Retrofit 2 + OkHttp 4 + Kotlinx Serialization |
| **Image Loading** | Coil Compose (with GIF support via `coil-gif`) |
| **Local Database** | Room SQLite Database (Favorites & History) |
| **Preferences** | AndroidX DataStore Preferences |
| **Image Processing** | Android Canvas + Paint (Stroke & Fill) + ColorMatrix |
| **Export & Share** | Android MediaStore API & AndroidX FileProvider |

---

## 📂 Project Structure

```
com.example.catplayground/
├── CatPlaygroundApp.kt          # Application class, Dependency wiring, Coil configuration
├── MainActivity.kt              # Entry point activity & Compose theme provider
├── data/
│   ├── remote/                  # Retrofit API & Kotlinx Serialization DTOs
│   │   ├── CataasApi.kt
│   │   └── CatDto.kt
│   ├── local/                   # Room entities, DAOs, Database & DataStore
│   │   ├── CatDatabase.kt
│   │   ├── CatDao.kt
│   │   ├── CatEntity.kt
│   │   └── SettingsStore.kt
│   └── repository/              # Repository layer with offline caching & history
│       └── CatRepository.kt
├── domain/
│   └── model/                   # Domain models & filter/caption definitions
│       ├── CatImage.kt
│       ├── VisualFilter.kt
│       └── CaptionStyle.kt
├── image/                       # Bitmap canvas processor, MediaStore exporter, FileProvider sharer
│   ├── CatImageProcessor.kt
│   ├── ImageExporter.kt
│   └── ImageSharer.kt
└── ui/
    ├── components/              # Reusable Claymorphic UI tokens (Card, Button, Chip, TopBar)
    ├── explore/                 # Explore screen & ViewModel
    ├── saved/                   # Favorites & Recent history screen & ViewModel
    ├── editor/                  # Caption & Meme editor screen & ViewModel
    ├── settings/                # Settings screen & ViewModel
    ├── navigation/              # Compose Navigation host & Clay bottom bar
    └── theme/                   # Colors, Typography, Shapes, Themes
```

---

## 🤖 CI/CD & Automated Verification

Continuous Integration is powered by **GitHub Actions** (`.github/workflows/build.yml`):
- Automates JDK 17 & Gradle 8.7 setup.
- Runs JUnit unit tests (`testDebugUnitTest`).
- Assembles production/debug APKs (`assembleDebug`).
- Uploads the debug APK as a downloadable artifact.

---

## 🐾 Attribution & Credits

- Images and catalog provided by **[Cataas](https://cataas.com/)** (Cat as a service).
