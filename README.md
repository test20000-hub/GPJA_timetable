# 군포중앙고등학교 시간표

NEIS 고등학교시간표를 이용하는 군포중앙고 전용 모바일 시간표/PWA입니다.

## 현재 버전

- 사이트: **v2.0.7**
- Android 앱: **v2.0.8**
- Galaxy Watch Wear OS: **EOS 처리**

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
- 사이트 비밀번호 인증
- Android 관리자 기능 + QR 기기 승인
- QR 승인 기기 이름, 승인 시각, 토큰 만료 및 1회용 요청 관리

학교 정보: 시도교육청 코드 `J10`, 학교 코드 `7531272`.

## 버전 기록

### v2.0.8 — 위젯 추가 실패 경로 차단 및 2.0.9 롤백 유지
- Android 앱 버전을 `versionCode 28`, `versionName 2.0.8`로 유지했습니다.
- v2.0.9의 고정 release keystore 의존성을 제거하고 기존 v2.0.8 debug 빌드 방식으로 복구했습니다.
- 위젯 provider의 `initialLayout`을 실제 위젯 레이아웃으로 사용하고 설정 Activity 없이 즉시 생성되도록 구성했습니다.
- 런처의 위젯 추가 콜백에서 네트워크/파일 I/O가 실행되지 않도록 유지했습니다.
- 위젯 데이터 갱신은 백그라운드에서 수행하며 실패해도 기본 위젯 UI를 유지합니다.
- 위젯 receiver와 스케줄러 예외를 격리해 위젯 추가 과정에서 앱 프로세스가 죽지 않도록 보강했습니다.

### v2.0.7 — 초 단위 실시간 카운트다운 및 Android 버전 통합
- Smart Timetable 카운트다운에서 현재 초를 정확히 반영하도록 수정했습니다.
- 웹사이트와 Android 앱의 카운트다운이 매초 실제 남은 시간을 표시합니다.
- Android 홈 화면 위젯은 `Chronometer` 기반으로 초 단위 카운트다운이 계속 진행되도록 변경했습니다.
- Android 위젯의 예약 갱신 브로드캐스트가 실제 위젯 업데이트를 수행하도록 수정했습니다.
- 사이트와 Android 앱 버전을 `v2.0.7`로 통합했습니다.
- Android 앱 `versionCode`를 `27`, `versionName`을 `2.0.7`로 갱신했습니다.
- 사이트 캐시 버스터와 푸터 버전을 `v2.0.7`로 유지했습니다.

### v2.0.6 — 급식 주말 제외
- 급식 날짜 탐색에서 토요일·일요일을 완전히 건너뛰도록 수정했습니다.
- 이전/다음 날짜 버튼과 좌우 스와이프 모두 평일 기준으로 이동합니다.
- 급식 페이지 최초 진입일이 주말이면 다음 평일을 자동 표시합니다.

### v2.0.5 — 급식 주말 날짜 탐색 복구
- 급식 탭에서 토요일·일요일을 포함한 날짜 이동을 다시 명시적으로 지원합니다.
- 이전/다음 날짜 버튼의 표시와 터치 영역을 고정해 모바일에서도 날짜 탐색이 사라지지 않도록 수정했습니다.
- 급식 날짜 카드에서 좌우 스와이프로 전날/다음 날 이동할 수 있도록 추가했습니다.
- 날짜 이동 시 새로운 Date 객체를 사용해 주말 및 월말 경계에서도 안정적으로 이동하도록 수정했습니다.
- 급식 JavaScript 캐시 버전을 `v10`으로 갱신했습니다.

### v2.0.4 — Wear OS EOS
- Galaxy Watch Wear OS 프로젝트를 공식 종료(EOS)했습니다.
- `wear-os/` 전체 프로젝트 및 Wear OS 전용 GitHub Actions 빌드 워크플로를 제거했습니다.
- Android 앱에서 Wearable Data Layer 동기화 코드와 Wear OS listener service를 제거했습니다.
- Android 앱 버전을 `versionCode 24`, `versionName 2.0.4`로 갱신했습니다.
- Android APK 빌드 워크플로에서 Wear OS 동기화 계약 검사를 제거했습니다.
- 사이트의 기존 v2.0.4 버전 표기는 유지했습니다.

### v2.0.4 — 사이트
- 급식 데이터 렌더링을 방어적으로 정규화하여 메뉴가 문자열/객체 형태여도 표시되도록 개선
- 급식 API 요청에 10초 타임아웃과 요청 순번 검사를 추가
- 급식 페이지의 주말 자동 이동 제거
- 재시도 버튼을 이벤트 리스너 방식으로 변경
- 급식 페이지 캐시 버스터 및 서비스 워커 캐시 갱신
- 사이트 및 급식 페이지 푸터/메타 버전 동기화

### v2.0.3
- 급식 페이지 렌더링 오류 수정
- Android 앱 버전 `versionCode 23`, `versionName 2.0.3`
- Wearable Data Layer 기반 휴대폰 ↔ Galaxy Watch 양방향 동기화 추가

### v1.0.9 — Galaxy Watch Wear OS
- Wear OS 앱 `versionCode 10`, `versionName 1.0.9`
- 휴대폰 ↔ 워치 시간표/급식 양방향 통신 구현
- `/gpja/request/sync` → `/gpja/sync` → `/gpja/ack` 동기화 흐름 구현
- APK 서명 및 CI artifact 검증

### v1.0.8 ~ v1.0.0 — Galaxy Watch Wear OS
- Wear OS APK/AAB CI 안정화 및 산출물 검증
- AndroidX/Gradle/Kotlin CI 구성
- Galaxy Watch companion 앱 및 Compose for Wear OS UI 추가

### v2.0.2
- Smart School 통합 대시보드
- 알림 설정 및 시간표 변경 감지
- QR 승인 시스템 2.0
- Android 위젯 초 단위 카운트다운
- Android `versionCode 22`, `versionName 2.0.2`

### v2.0.1
- 스마트 대시보드 실시간 상태 개선
- 시간표 변경 감지 및 중복 알림 제한
- 캐시 갱신

### v2.0.0
- Smart Timetable 메인 화면 도입
- 현재/다음 교시 및 수업 카운트다운
- 오늘 수업 진행률 및 다음 수업 정보

### v1.6.x
- 시간표 스크롤 성능 최적화
- 수업 종료 알림 및 다음 수업 상세 정보 개선
- Android 위젯 자동 새로고침 안정화

### v1.5.x
- 관리자 기능, QR 기기 승인, 사이트 비밀번호 인증
- 급식 UI 및 날짜 탐색 개선
- Android 앱/위젯 및 GitHub Actions APK 빌드 구성 개선
