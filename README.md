<section> <h1>🌍 moyora - 백엔드 레포지토리</h1> <p><em>지역 기반 소모임 플랫폼<br>관심사로 연결되는 사람들의 모임, <strong>모여라</strong></em></p> </section> <br>

## ✅ 1차 구현 내역 (팀 프로젝트)

- `카카오 소셜 로그인` (Spring Security + OAuth2)
- `신분증 사진을 통한 신원 인증 시스템`
- `모임글 CRUD` (모임 모집 게시판 기능)
- `모임 신청, 수락, 거절`
- `공지글 등록` (각 모임당 1개)
- `댓글 기능`
- `JWT 기반 인증/인가`
- `Redis 캐싱` (인기 태그 조회 성능 개선)
- `Docker + GCP Cloud Run 배포`

---

## 🔧 2차 개인 리팩토링 내역

- `검색엔진 개선` → Elasticsearch 적용 (SQL vs ElasticSearch: 검색 속도 및 정확도 비교 [[참고링크]](https://xyz987164.tistory.com/28))
- `기존 SQL 검색` → 형태소 분석기, ngram, fuzziness 적용
- `추천/검색 고도화`
- `태그 자동 추출 로직` → AI 명사 추출 후 Gemini API 활용
- `공지글 시스템` → 단체 채팅으로 대체/확장
- `실시간 알림` (SSE 기반) 구현
- `모임 신청/승인, 댓글, 채팅 등 이벤트 알림`
- `별점 기능 추가` (모임 후기/만족도 평가)


## 🌐 서비스 아키텍처
```sql
+--------------------+       +--------------------+
|    Frontend (앱)   |  <--> |   API Gateway      |
+--------------------+       +--------------------+
           |                          |
           v                          v
+--------------------+       +--------------------+
|  Moyora Backend    |       |  Authentication    |
|  (Spring Boot)     |       |  OAuth2 / JWT      |
+--------------------+       +--------------------+
| - BoardController  |
| - NoticeController |
| - ChatController   |
| - UserController   |
| - SearchService    |
| - SSEService       |
+--------------------+
           |
           v
+--------------------+
|     Database       |
|  Cloud SQL (MySQL) |
+--------------------+
           |
           v
+--------------------+
|     Cache Layer    |
|       Redis        |
+--------------------+
           |
           v
+--------------------+
|   Search Engine    |
|   Elasticsearch    |
+--------------------+
           |
           v
+--------------------+
|    AI / Gemini     |
|  Tag Extraction,   |
+--------------------+
```

## 🛠️ 기술 스택
<!-- 🛠️ 기술 스택 -->
<section>
  <h3><small>🧑‍💻 Backend</small></h3>
  <small>
    <img src="https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=java&logoColor=white" alt="Java">
    <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
    <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Security">
    <img src="https://img.shields.io/badge/JPA-Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white" alt="JPA">
  </small>
</section>

<section>
  <h3><small>🗄️ Database</small></h3>
  <small>
    <img src="https://img.shields.io/badge/Cloud%20SQL-MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="Cloud SQL MySQL">
  </small>
</section>

<section>
  <h3><small>🟢 Cache</small></h3>
  <small>
    <img src="https://img.shields.io/badge/Redis-20232A?style=for-the-badge&logo=redis&logoColor=DC382D" alt="Redis">
  </small>
</section>

<section>
  <h3><small>🔍 Search</small></h3>
  <small>
    <img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white" alt="Elasticsearch">
  </small>
</section>

<section>
  <h3><small>☁️ Infra</small></h3>
  <small>
    <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
    <img src="https://img.shields.io/badge/GCP-4285F4?style=for-the-badge&logo=googlecloud&logoColor=white" alt="GCP">
  </small>
</section>

<section>
  <h3><small>🔐 Auth</small></h3>
  <small>
    <img src="https://img.shields.io/badge/OAuth2-Kakao-FFCD00?style=for-the-badge&logo=kakaotalk&logoColor=black" alt="OAuth2 Kakao">
    <img src="https://img.shields.io/badge/JWT-JSON%20Web%20Token-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT">
    <img src="https://img.shields.io/badge/AES-Encryption-4B0082?style=for-the-badge&logoColor=white" alt="AES">
  </small>
</section>

<section>
  <h3><small>🤖 AI / LLM</small></h3>
  <small>
    <img src="https://img.shields.io/badge/Gemini-AI-FF8800?style=for-the-badge&logoColor=white" alt="Gemini API">
  </small>
</section>

<section>
  <h3><small>⚡ Realtime</small></h3>
  <small>
    <img src="https://img.shields.io/badge/SSE-Server%20Sent%20Events-1E90FF?style=for-the-badge&logoColor=white" alt="SSE">
    <img src="https://img.shields.io/badge/WebSocket-8080-FF4500?style=for-the-badge&logoColor=white" alt="WebSocket">
  </small>
</section>
