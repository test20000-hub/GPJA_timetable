# 군포중앙고등학교 시간표

NEIS 고등학교시간표를 이용하는 군포중앙고 전용 모바일 시간표/PWA입니다.

## 현재 버전

- 사이트: **v2.0.2**
- Android 앱: **v2.0.2**
- Galaxy Watch Wear OS 앱: **v1.0.3**

## 구성

- GitHub Pages 정적 웹앱
- GitHub Actions 기반 NEIS 시간표 수집
- 학년/반 선택 및 주간·오늘 시간표
- Smart Timetable: 현재/다음 교시, 실시간 카운트다운, 수업 진행률
- Smart School 대시보드: 오늘 급식 + 학사일정 + 개인 설정 + 빠른 이동
- 수업 시작 전·쉬는 시간·시간표 변경 알림 설정
- 시간표 변경 감지 알림
- PWA 설치 및 오프라인 캐시
- 급식 조회 및 메뉴 검색
- 학사일정 조회
- 라이트/다크 모드 및 Liquid Glass UI
- 모바일 Android 앱 및 홈 화면 위젯
- Android 위젯 실시간 현재/다음 수업 카운트다운
- Galaxy Watch Wear OS companion 앱 및 APK/AAB CI 빌드
- 사이트 비밀번호 인증
- Android 관리자 기능 + QR 기기 승인
- QR 승인 기기 이름, 승인 시각, 토큰 만료 및 1회용 요청 관리

학교 정보: 시도교육청 코드 `J10`, 학교 코드 `7531272`.

## 버전 기록

### v1.0.3 — Galaxy Watch Wear OS
- AndroidX 설정을 CI 실행 직전에 강제로 재생성하여 구버전 커밋/환경에서 설정이 누락되지 않도록 수정
- 빌드 전 `android.useAndroidX=true`, Gradle 설정 파일 및 핵심 프로젝트 파일 존재 여부를 사전 검증
- Debug APK, Release APK, Release AAB 모두 실제 파일 크기까지 검증 후 artifact 업로드
- Wear OS 앱 `versionCode 4`, `versionName 1.0.3` 반영

### v1.0.2 — Galaxy Watch Wear OS
- Wear OS 앱 실제 `versionName 1.0.2` 반영
- CI에서 `android.useAndroidX=true`를 Gradle 프로퍼티로 강제 적용
- Debug/Release APK 및 Release AAB 빌드 검증 유지
- CI artifact 버전을 v1.0.2로 동기화

### v1.0.1 — Galaxy Watch Wear OS
- Wear OS CI 빌드 실패 수정
- `android.useAndroidX=true` 추가
- Gradle/JVM/Kotlin CI 설정 추가
- Galaxy Watch용 Debug/Release APK 및 Release AAB 빌드 구조 유지

### v1.0.0 — Galaxy Watch Wear OS
- Wear OS companion 앱 모듈 추가
- Galaxy Watch launcher activity 추가
- Compose for Wear OS UI 기반 추가
- APK/AAB Gradle 빌드 구조 추가

### v2.0.2
- Smart School 통합 대시보드 추가: 오늘 급식, 학사일정, 학교생활 바로가기
- 자주 보는 과목 및 수업/급식/시간표 변경 알림 설정 추가
- 시간표 변경 감지 알림을 개인 설정과 연동
- QR 승인 시스템 2.0: 기기 이름, 관리자 키/승인 요청 만료, 승인 시각 표시
- Android 위젯에 현재 수업 종료/다음 수업 시작까지 초 단위 카운트다운 표시
- Android `versionCode 22`, `versionName 2.0.2` 반영
- 사이트/QR 센터 버전 표기 동기화

### v2.0.1
- 스마트 대시보드 실시간 카운트다운과 상태 표시 개선
- 선택한 반의 시간표 변경 감지 및 중복 알림 제한
- 스마트 대시보드/서비스 워커 캐시 갱신
- Android `versionCode 21`, `versionName 2.0.1` 반영

### v2.0.0
- Smart Timetable 메인 화면 도입
- 현재/다음 교시, 수업 종료·시작 카운트다운, 오늘 수업 진행률
- 다음 수업 과목·담당 교사·교실 정보 표시
- 기존 시간표·급식·학사일정·알림·위젯 기능 유지

### v1.6.6
- 시간표 가로 스크롤 성능 최적화
- 스크롤 영역의 고비용 blur/backdrop-filter 제거
- layout/paint containment 및 overscroll/touch 최적화
- 시계 갱신 시 전체 시간표 재렌더링 최소화

### v1.6.5
- 교시 종료 알림을 상세 시간표 알림으로 개선
- 다음 교시 과목/교사/시간 및 알림 탭 이동 지원

### v1.6.3
- Android 위젯 1분 자동 새로고침 안정화
- 현재/다음 교시 판정 및 한국 표준시 처리 개선
- Android `versionCode 17`, `versionName 1.6.3`

### v1.6.0
- 관리자 기능과 일반 앱 기능의 병행 사용 지원
- 관리자 기능 활성화 상태에서도 시간표·급식·학사일정 사용 가능
- 관리자 승인 QR 생성/스캔 개선

### v1.5.9
- 관리자 앱 등록코드(`ADMIN_CODE`) 검증 추가
- 관리자 코드의 소스 직접 노출 방지

### v1.5.8
- GitHub Pages 기반 QR 기기 승인 센터
- 관리자/일반 앱 역할 및 Android 승인 브리지
- QR 스캐너와 카메라 권한 처리

### v1.5.7
- 시스템 라이트/다크 모드 자동 감지 및 테마 동기화

### v1.5.5
- 사이트 전체 비밀번호 인증 및 인증 상태 유지

### v1.5.2 ~ v1.5.4
- 급식 UI, 날짜 탐색, 메뉴 검색 및 주말 처리 개선

### v1.5.0
- Android 앱/위젯 및 GitHub Actions APK 빌드 구성 개선
- 앱 아이콘 및 학교 로고 적용

### v1.3.x
- Android 앱 빌드 안정화, 위젯 개선, 시스템 바 인셋 처리

### v1.2.x
- 학사일정 표시 및 데이터 연동 개선

### v1.0.0 ~ v1.2.1
- 군포중앙고 시간표 웹앱 기본 기능 구축
- NEIS 연동, 학년·반 선택, 주간/오늘 시간표, PWA, 오프라인 캐시 구축

## 보안 참고

GitHub Pages 정적 구조에서는 서버 측 비밀 저장소를 제공할 수 없으므로 QR 승인 시스템은 클라이언트 저장소와 서명 없는 토큰 교환을 기반으로 합니다. 높은 보안 수준의 중앙 승인/폐기·감사를 요구하는 경우에는 별도 서버가 필요합니다.

`ADMIN_CODE` 같은 빌드 비밀은 GitHub Actions Secret으로만 주입합니다.
