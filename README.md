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
>자세한 폴더별 설명은 [Wiki - 폴더 구조](https://github.com/beour-team/beour-be/wiki#-%ED%8F%B4%EB%8D%94-%EA%B5%AC%EC%A1%B0) 를 참고해주세요.
```text
📦 src
 ┣ 📂main
 ┃ ┣ 📂java/com.beour
 ┃ ┃ ┣ 📂global        # 전역 설정 및 공통 모듈 (CORS, JWT, 예외처리 등)
 ┃ ┃ ┣ 📂user          # 사용자 도메인 (회원가입, 로그인, 내 정보 등)
 ┃ ┃ ┣ 📂space         # 공간 도메인 (공간 등록, 검색, 상세조회 등)
 ┃ ┃ ┣ 📂reservation   # 예약 도메인 (예약 생성, 승인/거절, 조회 등)
 ┃ ┃ ┣ 📂review        # 리뷰 도메인 (작성, 수정, 삭제 등)
 ┃ ┃ ┣ 📂wishlist      # 찜하기 기능
 ┃ ┃ ┣ 📂banner        # 배너 관리
 ┃ ┃ ┗ 📂token         # 토큰 재발급 도메인
 ┃ ┃  
 ┃ ┣ 📂resources
 ┃ ┃ ┗ 📜application.yml  # 전역 환경 설정 파일
 ┣ 📂test
 ┃ ┗ 📂...             # 각 도메인별 테스트 코드

```

<br>

<!--해당 url 수정시 변경-->
## 📄 API 명세
👉 [Wiki에서 확인하기]([https://google.github.io/styleguide/javaguide.html](https://github.com/beour-team/beour-be/wiki#-api-%EB%AA%85%EC%84%B8%EC%B6%94%EA%B0%80-%EC%98%88%EC%A0%95))


<br>


## 📝 커밋 컨벤션
👉 [Wiki에서 확인하기](https://github.com/beour-team/beour-be/wiki/Commit-Convention)

<br>

## 🌐 배포 주소 (수정 필요)
서버: 

Swagger: 

프론트엔드: 

<br>


## 백엔드 멤버 소개
<table  width="100%">
  <tr>
    <td  align="center">
      <img  src="https://avatars.githubusercontent.com/u/176730442?v=4"  width="100px;"  alt=""/>
    </td>
    <td  align="center">
      <img  src="https://avatars.githubusercontent.com/u/114418850?v=4"  width="100px;"  alt=""/>
    </td>
  </tr>
  <tr>
    <td align="center">
        <strong>기획/BE(PM)</strong>
        <a href="https://github.com/seminjjang">
          <div>프제<br>(박세민)</div>
        </a>
    </td>
        <td align="center">
          <strong>BE</strong>
        <a href="https://github.com/99hyeon">
          <div>바울<br>(박서현)</div>
        </a>
    </td>
  </tr>
</table>
