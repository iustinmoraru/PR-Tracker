# PR Tracker

Aplicație Android pentru monitorizarea progresului la sală: planifici antrenamente, notezi exercițiile (seturi, repetări, greutate), iar aplicația îți urmărește automat **recordurile personale (PR-urile)** și consistența.

Proiect realizat pentru laboratorul de Calcul Mobil.

## Funcționalități

- **Autentificare** cu Google (Credential Manager) sau cu email și parolă, cu validarea datelor și mesaje de eroare clare.
- **Acasă**: planul săptămânii, inel de progres, serie de săptămâni active, volum lunar, istoric complet.
- **Antrenamente**: creare și editare (nume, dată, durată, notițe, exerciții cu seturi × repetări × greutate), detalii, marcare ca finalizat, ștergere.
- **Recorduri personale (PR)**: calculate automat din antrenamentele finalizate, cu notificare la fiecare record nou și istoric pe exercițiu cu grafic.
- **Progres**: volum total, minute, antrenamente finalizate, consistența din ultimele 7 zile.
- **Deadhang timer**: cronometru pentru priză, pornit/oprit și prin **scuturarea telefonului** (accelerometru), cu vibrație și notificare la final.
- **Temă**: implicit urmează tema dispozitivului; din *Profil → Setări* se poate alege Luminoasă sau Întunecată.
- Funcționează și **offline** (cache Firestore), cu sincronizare automată.

## Cum acoperă baremul

| Criteriu | Implementare |
|---|---|
| Autentificare și validare | `LoginActivity`, `AuthRepository`, `AuthValidator` (+ teste unitare) |
| Evenimente UI | click, long click (meniu contextual pe antrenamente), scuturare (senzor), switch-uri, date picker, notificări |
| Minim două activități | `LoginActivity`, `MainActivity`, `WorkoutEditorActivity`, `WorkoutDetailActivity`, `ExerciseHistoryActivity`, `SettingsActivity` |
| Meniu funcțional | meniul de opțiuni din header, meniul contextual (long press), meniul ⋮ din detalii, bara de navigare |
| Stocare date | Cloud Firestore (cloud) + DataStore (local) |
| Design și UX | design Figma, temă luminoasă/întunecată, stări de încărcare/gol/eroare, confirmări, snackbar-uri |
| Cod curat și modularitate | MVVM, pachete `data` / `domain` / `ui`, repository-uri, KDoc |
| Bonus | autentificare Google + senzor (accelerometru) |

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
│   ├── settings/            # SettingsRepository, ThemeMode
│   └── workout/             # modele + WorkoutRepository (Firestore)
├── domain/                  # logică pură Kotlin, testată unitar
│   ├── stats/               # PR-uri, serii, volum, istoric
│   └── validation/          # validări pentru autentificare și antrenamente
├── notifications/           # AppNotifier (canale + notificări)
├── sensors/                 # ShakeDetector (accelerometru)
└── ui/                      # activități, ecrane Compose, ViewModel-uri
    ├── auth/                # LoginActivity (Google + email/parolă)
    ├── main/                # MainActivity: Acasă, Progres, Profil
    ├── timer/               # Deadhang timer
    ├── editor/              # WorkoutEditorActivity + foaia de exerciții
    ├── detail/              # WorkoutDetailActivity
    ├── history/             # ExerciseHistoryActivity
    ├── settings/            # SettingsActivity (temă, notificări)
    ├── workout/             # componente comune pentru antrenamente
    ├── common/              # ThemedActivity, formatare
    ├── components/          # componente UI reutilizabile
    └── theme/               # culori, tipografie, tema luminoasă/întunecată
```

## Configurare Firebase (o singură dată)

Proiectul compilează și fără Firebase, dar autentificarea și sincronizarea au nevoie de el.

1. Intră pe [console.firebase.google.com](https://console.firebase.google.com) și creează un proiect.
2. **Add app → Android**, cu package name `com.iustinmoraru.prtracker`.
3. Adaugă amprenta **SHA-1** a cheii de debug (necesară pentru Google Sign-In). Cea mai simplă variantă: în Android Studio,
   panoul *Gradle* (dreapta) → iconița de terminal (*Execute Gradle Task*) → scrie `signingReport` → Enter, și copiezi linia `SHA1`.
   Din PowerShell, folosind `keytool` din JDK-ul inclus în Android Studio:
   ```powershell
   & "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
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
