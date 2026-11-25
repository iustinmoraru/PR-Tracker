# PR Tracker

Aplicație Android pentru monitorizarea progresului la sală: planifici antrenamente, notezi exercițiile (seturi, repetări, greutate), iar aplicația îți urmărește automat **recordurile personale (PR-urile)** și consistența.

Proiect realizat pentru laboratorul de Calcul Mobil.

## Tehnologii

| Zonă | Tehnologie |
|---|---|
| Limbaj | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 (design din Figma, font Manrope) |
| Arhitectură | MVVM: Activity → ViewModel (StateFlow) → Repository, cu dependency injection manual (`AppContainer`) |
| Autentificare | Firebase Authentication: Google (Credential Manager) și email/parolă |
| Stocare cloud | Cloud Firestore |
| Stocare locală | Jetpack DataStore (tema aleasă, notificări) |
| Build | Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`) |

## Structura proiectului

```
app/src/main/java/com/iustinmoraru/prtracker/
├── PrTrackerApp.kt          # Application: creează AppContainer
├── di/                      # AppContainer (dependency injection manual)
├── data/                    # surse de date: Firebase, DataStore
│   ├── auth/                # AuthRepository, erori de autentificare
│   └── settings/            # SettingsRepository, ThemeMode
├── domain/                  # logică pură Kotlin (validări, calcule), testată unitar
└── ui/                      # activități, ecrane Compose, ViewModel-uri
    ├── auth/                # LoginActivity (Google + email/parolă)
    ├── main/                # MainActivity
    ├── settings/            # SettingsActivity (temă, notificări)
    ├── common/              # ThemedActivity (aplică tema aleasă)
    ├── components/          # componente UI reutilizabile
    └── theme/               # culori, tipografie, tema luminoasă/întunecată
```

## Configurare Firebase (o singură dată)

Proiectul compilează și fără Firebase, dar autentificarea și sincronizarea au nevoie de el.

1. Intră pe [console.firebase.google.com](https://console.firebase.google.com) și creează un proiect.
2. **Add app → Android**, cu package name `com.iustinmoraru.prtracker`.
3. Adaugă amprenta **SHA-1** a cheii de debug (necesară pentru Google Sign-In). O afli din Android Studio cu
   *Gradle → app → Tasks → android → signingReport*, sau din terminal:
   ```
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```
4. Descarcă `google-services.json` și pune-l în folderul `app/`.
5. **Authentication → Sign-in method**: activează **Google** și **Email/Password**.
6. **Firestore Database → Create database**, apoi la *Rules* copiază conținutul fișierului `firestore.rules` din proiect.
7. Dă *Sync Project with Gradle Files* în Android Studio și rulează aplicația.

> `google-services.json` nu conține secrete (cheile Firebase pentru Android sunt publice prin design), deci poate fi pus în repository, ca să poată compila oricine proiectul.

## Rulare

Deschide folderul proiectului în Android Studio și apasă **Run**. minSdk 26 (Android 8.0), targetSdk 35.

Testele unitare: `gradlew test`.

## Licențe

Fontul Manrope este distribuit sub SIL Open Font License 1.1.
