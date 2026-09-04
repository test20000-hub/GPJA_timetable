# GPJA Timetable — Galaxy Watch

Wear OS companion app for Galaxy Watch.

## v1.0.8
- Wear OS 앱 `versionCode 9`, `versionName 1.0.8`로 업데이트했습니다.
- v1.0.7에서 Debug/Release APK와 Release AAB 자체 빌드는 모두 성공했지만, CI의 산출물 검증 단계가 고정 파일 경로에 의존해 실패하는 문제를 수정했습니다.
- 빌드 후 실제 `.apk`/`.aab` 파일을 탐색하고 비어 있지 않은 파일만 선별합니다.
- CI 내부의 안정적인 파일명으로 APK 2개와 AAB 1개를 정규화한 뒤 `test -s`와 `file` 명령으로 다시 검증합니다.
- Artifact 경로는 저장소 루트 기준의 정규화된 산출물 경로를 사용합니다.
- Artifact 이름을 `gpja-wear-os-v1.0.8`로 갱신했습니다.

## v1.0.7
- Wear OS 앱 `versionCode 8`, `versionName 1.0.7`로 업데이트했습니다.
- Kotlin 2.1.0 + lifecycle 2.8.7 조합에서 Release lint가 `NullSafeMutableLiveData` detector 내부의 `IncompatibleClassChangeError`로 충돌하는 문제를 수정했습니다.
- 해당 앱에 적용되지 않는 `NullSafeMutableLiveData` lint detector를 비활성화하여 Release APK/AAB 빌드가 lint 구현 버그에 막히지 않도록 했습니다.
- Debug APK → Release APK → Release AAB 순서로 빌드합니다.
- 세 산출물을 `test -s`로 실제 생성 여부와 비어 있지 않은 상태까지 검증한 뒤 업로드합니다.
- Artifact 이름을 `gpja-wear-os-v1.0.7`으로 갱신했습니다.

## v1.0.6
- Wear OS 앱 `versionCode 7`, `versionName 1.0.6`으로 업데이트했습니다.
- AndroidX 설정을 CI와 프로젝트 양쪽에서 명시적으로 유지합니다.
- Android SDK 패키지 목록을 `setup-android`가 안정적으로 해석할 수 있는 단일 공백 구분 형식으로 수정했습니다.
- Debug APK → Release APK → Release AAB 순서로 빌드합니다.
- 세 산출물을 `test -s`로 실제 생성 여부와 비어 있지 않은 상태까지 검증한 뒤 업로드합니다.
- Artifact 이름을 `gpja-wear-os-v1.0.6`으로 갱신했습니다.

## v1.0.5
- Wear OS 앱 `versionCode 6`, `versionName 1.0.5`로 업데이트했습니다.
- AndroidX 설정을 CI와 프로젝트 양쪽에서 명시적으로 유지합니다.
- Gradle configuration 단계에서 필수 프로젝트/Manifest/Activity 파일을 사전 검증합니다.
- Debug APK → Release APK → Release AAB 순서로 빌드합니다.
- 세 산출물을 `test -s`로 실제 생성 여부와 비어 있지 않은 상태까지 검증한 뒤 업로드합니다.
- Artifact 이름을 `gpja-wear-os-v1.0.5`로 갱신했습니다.

## v1.0.4
- Wear OS 앱 `versionCode 5`, `versionName 1.0.4`로 업데이트했습니다.
- Android SDK 설치 단계를 multiline 패키지 목록으로 고정해 CI 환경별 파싱 문제를 줄였습니다.
- Gradle configuration 단계에서 필수 프로젝트/Manifest/Activity 파일을 사전 검증합니다.
- Debug APK → Release APK → Release AAB 순서로 빌드합니다.
- 세 산출물을 `test -s`로 실제 생성 여부와 비어 있지 않은 상태까지 검증한 뒤 업로드합니다.
- Artifact 이름을 `gpja-wear-os-v1.0.4`로 갱신했습니다.

## v1.0.3
- CI가 빌드 직전에 `gradle.properties`를 재생성하여 `android.useAndroidX=true`를 확정 적용합니다.
- 빌드 전 Gradle/프로젝트 파일 및 AndroidX 설정을 사전 검증합니다.
- Debug APK, Release APK, Release AAB를 모두 빌드하고 실제 산출물 존재/크기를 검증합니다.
- Wear OS 앱 `versionCode 4`, `versionName 1.0.3`을 사용합니다.

## v1.0.2
- Wear OS 앱 실제 `versionName 1.0.2` 반영
- AndroidX Gradle 프로퍼티 강제 적용
- Debug/Release APK 및 Release AAB CI 빌드 구조 유지

## v1.0.1
- Wear OS CI 빌드 실패 수정
- `android.useAndroidX=true` 추가
- 표준 Gradle JVM/Kotlin 설정 추가

## v1.0.0
- Wear OS application module added
- Galaxy Watch launcher activity added
- Compose for Wear OS UI foundation added
- APK/AAB Gradle build structure added
- Application ID: `kr.co.gpja.timetable.wear`

## Build

```bash
gradle --no-daemon -Pandroid.useAndroidX=true :app:assembleDebug
gradle --no-daemon -Pandroid.useAndroidX=true :app:assembleRelease
gradle --no-daemon -Pandroid.useAndroidX=true :app:bundleRelease
```

Outputs:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`
