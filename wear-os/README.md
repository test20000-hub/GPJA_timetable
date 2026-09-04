# GPJA Timetable — Galaxy Watch

Wear OS companion app for Galaxy Watch.

## v1.0.9
- Wear OS 앱 `versionCode 10`, `versionName 1.0.9`로 업데이트했습니다.
- 휴대폰 앱과 Wear OS 앱 사이에 Google Play Services Wearable Data Layer 기반 양방향 통신을 추가했습니다.
- 워치에서 `/gpja/request/sync` 메시지를 보내면 휴대폰이 시간표와 급식 데이터를 조회해 `/gpja/sync` DataItem으로 전송합니다.
- 시간표는 현재 선택된 기본 학년/반 데이터, 급식은 `kschoolinfo.com`의 군포중앙고 API 응답을 워치로 전달합니다.
- 워치는 수신 데이터를 로컬에 저장하고 화면에 시간표/급식을 표시한 뒤 `/gpja/ack` 메시지로 휴대폰에 수신 완료를 회신합니다.
- CI에서 양쪽 앱의 동기화 경로와 payload 키(`scheduleJson`, `mealJson`)가 일치하는지 계약 검증을 수행합니다.
- Release APK는 CI에서 직접 설치 가능한 서명 APK인지 `apksigner verify`로 검증합니다.
- Artifact 이름을 `gpja-wear-os-v1.0.9`로 갱신했습니다.

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
- 해당 앱에 적용되지 않는 `NullSafeMutableLiveData` lint detector를 비활성화했습니다.

## v1.0.6
- Wear OS 앱 `versionCode 7`, `versionName 1.0.6`으로 업데이트했습니다.
- AndroidX 설정과 Android SDK 패키지 설치를 CI에서 명시적으로 유지합니다.

## v1.0.5
- Wear OS 앱 `versionCode 6`, `versionName 1.0.5`로 업데이트했습니다.
- Gradle configuration 단계에서 필수 프로젝트/Manifest/Activity 파일을 사전 검증합니다.

## v1.0.4
- Wear OS 앱 `versionCode 5`, `versionName 1.0.4`로 업데이트했습니다.
- Android SDK/Gradle CI 안정화를 진행했습니다.

## v1.0.3
- AndroidX 설정을 CI 실행 직전에 강제로 재생성합니다.
- Debug APK, Release APK, Release AAB를 빌드하고 산출물 존재/크기를 검증합니다.
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
