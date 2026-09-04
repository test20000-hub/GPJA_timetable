# GPJA Timetable — Galaxy Watch

Wear OS companion app for Galaxy Watch.

## v1.0.0
- Wear OS application module added
- Galaxy Watch launcher activity added
- Compose for Wear OS UI foundation added
- APK/AAB Gradle build structure added
- Application ID: `kr.co.gpja.timetable.wear`

## Build

```bash
gradle --no-daemon :app:assembleDebug
gradle --no-daemon :app:assembleRelease
gradle --no-daemon :app:bundleRelease
```

Outputs:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`
