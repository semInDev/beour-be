# beour-be
비어 있는 순간을 수익으로 채울 수 있게 하는 B2C플랫폼, BE:OUR 백엔드 레포지토리

<br>

# ERD
```
[User] (사용자)
- id (PK)
- name
- nickname: unique
- email
- loginId: unique
- password
- phone
- role(GUEST / HOST / ADMIN)
- created_at
- updated_at
- deleted_at

[Space]
- id (PK)
- host_id (FK → User)
- name
- space_category (CAFE / RESTAURANT / COOKING / LEATHER / COSTUME / ART)
- use_category (MEETING / COOKING / BARISTA / FLEA_MARKET / FILMING / ETC)
- max_capacity
- address
- detail_address
- price_per_hour
- thumbnail_url
- latitude
- longitude
- avg_rating
- created_at
- updated_at
- deleted_at

[Like] → 찜 기능
- id (PK)
- user_id (FK → User) 
- space_id (FK → Space) → user_id, space_id 조합 unique 제약

[Description]
- id (PK)
- space_id (FK)
- description
- price_guide
- facility_notice
- notice
- location_description
- refund_policy
- website_url

[Tag]
- id (PK)
- space_id (FK)
- contents

[Available_times]
- id (PK)
- space_id (FK)
- date
- start_time
- end_time
- deleted_at

[SpaceImage]
- id (PK)
- space_id (FK)
- image_url
- deleted_at

[Reservation]
- id (PK)
- space_id (FK → Space)
- user_id (FK → User)
- host_id (FK → User)
- status (PENDING / ACCEPTED / REJECTED / COMPLETED)
- date
- start_time
- end_time
- price
- guest_count
- created_at
- updated_at
- deleted_at

[Review]
- id (PK)
- reservation_id (FK → Reservation)
- space_id (FK → Space)
- user_id (FK → User)
- rating (1~5)
- content
- created_at
- updated_at
- deleted_at

[reviewImage]
- id (PK)
- review_id (FK)
- image_url
- deleted_at

[ReviewComment]
- id (PK)
- review_id (FK → Review)
- host_id (FK → User)
- content
- created_at
- updated_at
- deleted_at

[Banner]
- id (PK)
- image_url
- link_url
- title
- is_active
- display_order
- start_date
- end_date
- created_at
- updated_at
- deleted_at

```

# 커밋 컨벤션

### **Commit Message Format**

```
[#이슈번호] <type> : <subject>   - subject line

<body>                          - message body

<footer>                        - message footer
```

**Subject line**

- **필수**
- 변경 사항에 대한 간단한 설명
- 현재 시제로 작성(ex. “추가”, “수정”, “구현”)
- 마침표 `.`  사용 안 함
- 최대 70자

**Message body**

- 선택 사항
- 수정 이유와 전후 비교 설명
- 현재 시제로 작성
- 70자 이상일 경우 줄바꿈

**Message footer**

- 해당 커밋에 관련된 이슈 번호 명시 - 관련 이슈 자동 연결
- 하위 호환이 깨지는 변경이 있을 경우 명시
- 공동 작업자 있을 경우 작성자 추가
- 문서 링크, 배포 주의사항이 있을 경우 명시

💡**type 종류**

| 타입 | 설명 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `!HOTFIX` | 긴급 수정 사항 |
| `refactor` | 코드 리팩토링 (기능 변경 X) |
| `style` | 코드 스타일, 포맷 변경 (기능 영향 X) |
| `docs` | 문서 수정 (README 등) |
| `comment` | 코드 주석 추가/수정 |
| `test` | 테스트 코드 추가/수정 |
| `rename` | 파일 또는 폴더명 변경 |
| `move` | 파일 또는 폴더 이동 |
| `remove` | 파일 삭제 |
| `chore` | 빌드 설정, 의존성 변경 등 기타 작업 |
| `!BREAKING CHANGE` | 하위 호환이 깨지는 주요 변경사항 |

**📌footer 주요 키워드**

**`Closes`, `Fixes` , `Resolves`** : 해당 이슈 자동으로 닫음

**`Related to`, `Refs`** : 관련은 있지만 이슈를 닫지는 않음

`BREAKING CHANGE` : 중요한 변경사항 강조

`Co-authored-by` : 공동 작업자 명시

💡**Commit Message 예시**

```
[#23] feat: 공간 찜하기 기능 구현

- 찜 버튼 클릭 시 서버로 공간 ID 전달
- 사용자가 찜한 공간 리스트 조회 가능

Closes #23
```

```
[#10] fix: 비밀번호 검증 오류 수정

- 비밀번호 길이 조건 로직 버그 수정
- 유효성 메시지 개선

BREAKING CHANGE: 프론트에 전달되는 메시지 포맷이 변경됨
```

<br>

# 브랜치 전략

**📁 브랜치 구조**

```
main
│
├── develop
│   ├── feat/login
│   ├── fix/token-error
│   ├── chore/init-env
│   └── ...
```

**브랜치 설명**

| 브랜치 | 설명 |
| --- | --- |
| `main`  | 실제 서비스 운영용 (항상 안정된 코드 유지) |
| `develop` | 개발 통합 브랜치 (기능 통합, 리뷰 기준) |
| `feat/*` | 새로운 기능 개발 (예: feat/login) |
| `fix/*` | 버그 수정 (예: fix/password-check) |
| `refactor/*` | 코드 리팩토링 |
| `style/*` | 코드 포맷/스타일 변경 |
| `test/*` | 테스트 코드 작성/수정 |
| `chore/*` | 설정/빌드 관련 잡일 |
| `hotfix/*` | 운영 중 긴급 수정 시 사용 |

**브랜치 네이밍 규칙**

| Prefix | 사용 예 | 의미 |
| --- | --- | --- |
| `feat/` | feat/reservation | 기능 추가 |
| `fix/` | fix/token-refresh | 버그 수정 |
| `refactor/` | refactor/user-service | 리팩토링 |
| `style/` | style/header-format | 코드 스타일 변경 |
| `test/` | test/login-api | 테스트 코드 작성 |
| `chore/` | chore/prettier-config | 설정 관련 변경 |
| `hotfix/` | hotfix/auth-bug | 긴급 수정 |

**🔁 브랜치 작업 흐름**

1. `develop` 브랜치에서 작업 브랜치 생성
2. 기능 구현 후 커밋 및 푸시
3. GitHub에 Pull Request 생성 (base: develop ← compare: feat/login)
4. 팀원 리뷰 → merge
5. 전체 기능 완료 후 develop → main으로 PR 작성 및 배포

```
feat/login ─┐
            ├──> develop ───┐
fix/bugfix ─┘               │
                            └──> main (배포)
```

