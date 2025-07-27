# 🏠 BE:OUR Backend
비어 있는 순간을 수익으로 채울 수 있게 하는 B2C플랫폼, BE:OUR 백엔드 레포지토리

<br>

## 🛠️ 기술 스택

| 구분 | 사용 기술 |
|------------------|---------------------------------------------------|
| Language         | Java 17                                           |
| Framework        | Spring Boot 3.x, Spring Security, Spring Data JPA |
| DB               | MySQL                                             |
| Cache            | Redis                                             |
| Build Tool       | Gradle                                            |
| Query            | JPA                                               |
| Version Control  | Git / GitHub                                      |
| Test             | JUnit 5, Mockito                                  |
| CI/CD            | GitHub Actions                                    |
| Infra            | AWS EC2, S3, Route5S                              |

<br>

## 🧩 주요 기능

#### 🔸 인증/인가
- JWT 기반 로그인 및 회원가입
- Access/Refresh Token 관리
- Refresh Token 쿠키 저장 (`SameSite=None; Secure; HttpOnly`)
- Role 기반 권한 분리 (GUEST / HOST)

#### 🔸 유저
- 내 정보 조회/수정/비밀번호 변경
- 회원 탈퇴

#### 🔸 공간
- 키워드/필터 기반 검색
- 거리 기반 정렬 (ST_Distance_Sphere)
- 공간 등록, 수정, 삭제
- 공간 상세 조회

#### 🔸 예약
- 날짜별 예약 가능 시간 조회
- 공간 예약 생성, 승인/거절, 취소
- 현재/과거 예약 내역 확인

#### 🔸 리뷰
- 리뷰 작성, 조회, 수정, 삭제
- 리뷰 가능한 예약 목록 제공

#### 🔸 댓글
- 댓글 작성, 조회, 수정, 삭제
- 댓글 가능한 리뷰 목록 제공

#### 🔸 좋아요
- 공간 찜하기, 찜 해제
- 찜한 공간 조회

<br>

## 📁 프로젝트 구조
```text
```

<br>

## 📄 API 명세
👉 [Wiki에서 확인하기](https://github.com/99hyeon/beour-be/wiki#-api-%EB%AA%85%EC%84%B8%EC%B6%94%EA%B0%80-%EC%98%88%EC%A0%95)

<br>

## 📝 커밋 컨벤션
👉 Wiki에서 확인하기(url 추가 예정)

<br>

## 🌐 배포 주소 (수정 필요)
서버: 

Swagger: 

프론트엔드: 

<!---멤버 추가 예정 --->
