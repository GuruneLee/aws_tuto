# 05. 스프링 시큐리티와 OAuth 2.0으로 로그인 기능 구현하기
## 05.0 Intro
- 스프링 시큐리티
    - 인증(Authentication)와 인가(Authorization) 기능을 제공하는 프레임 워크
    - 스프링 기반 애플리케이션 보안의 표준
    
## 05.1 스프링 시큐리티와 스프링 시큐리티 Oauth2 클라이언트
- 로그인을 구현하기 위해 필요한 것
    - 로그인 시 보안
    - 비밀번호 찾기 / 변경
    - 회원가입 시 이메일 혹은 전화번호 인증
    - 회원정보 변겅 
    - etc.
    
- Oauth로그인 구현시, 구글/페북/네이버 등에 위에거 전부 맡겨버리면 됨 ㅋㅋㄹㅃㅃ

### 스프링 부트 1.5 vs 스프링 부트 2.0
- OAuth연동방법이 크게 변동됨
- 설정 방법에 크게 차이가 없음
    - **spring-security-oauth2-autoconfigure**라이브러리 덕분!!
    - 1.5에서 쓰던 설정을 2.0에서도 사용할 수 있음
    
- 그러나 현 프로젝트는 스프링부트2 방식인 **Spring Security Oauth2 Client**라이브러리를 사용할 것임
    1. 기존 1.5의 라이브러리는 업데이트없이 유지 상태로 결정됨
    2. 스프링 부트용 라이브러리(starter) 출시
    3. 신규 기능은 2.0에서만 지원하겠다 선언
    4. 기존 방식은 확장 포인트가 적절하게 오픈되어있지 않았음. 즉, 직접 상속 하거나 오버라이딩 했어야 했음
    
- 스프링부트2 방식에 대한 서치(search) 팁!
    1. spring-security-oauth2-autoconfigure라이브러리를 사용하였는지 확인
    2. application.properties 혹은 application.yml정보를 확인
        - 1.5는 url주소를 모두 명시함
        - 2.0은 client인증 정보만 입력하면 됨
        - 1.5에서 직접 입력했던 것들이 enum으로 대체됨 (CommonOAuth2Provider)
    
## 05.2 구글 서비스 등록
### 구글 서비스에 신규 서비스 생성
- clientId, clientSecret을 발급받아야 로그인 기능과 소셜 서비스 기능을 사용할 수 있음
1. [구글 클라우드 플랫폼 주소](https://console.cloud.google.com)로 이동하기
2. '프로젝트 선택'탭 클릭
3. '새 프로젝트' 클릭
4. 등록될 서비스 이름 입력 (여기선 'freelec-springboot2-webservice')
5. API및 서비스 카테고리로 이동
6. '사용자 인증 정보' > '사용자 인증 정보 만들기' 클릭 -> 'OAuth클라이언트 ID'항목 클릭!! > '동의 화면 구성'
7. 'OAuth동의 화면'탭에서 '앱 이름(freelec-springboot2-webservice)' '이메일/프로파일/오픈id'설정
8. OAuth클라이언트 ID만들기 화면으로 이동하기 -> 웹애플리케이션 유형
9. URL주소 등록 '승인된 리디렉션 URL' (https://localhost:8080/login/oauth2/code/google)

### application-oauth 등록
1. `src/main/resources`디렉토리에 `application-oauth.properties`생성
~~~
spring.security.oauth2.client.registration.google.client-id=클라이언트ID
spring.security.oauth2.client.registration.google.client-secret=클라이언트 보안 비밀번호
spring.security.oauth2.client.registration.google.scope=profile,email
~~~
- 스프링 부트에서는 'properties'의 이름을 'application-XXX.properties'라고 하면, XXX라는 이름의 profile이 생성되어 이를 통해 관리할 수 있음
- 즉, profile=XXX라는 식으로 호출해서 설정을 가져올 수 있음
  - 여기서는 application.properties에서 oauth설정을 포함하도록 해보자   
~~~
spring.profiles.include=oauth
~~~

2. 개인정보가 들어간 `application-oauth.properties`는 `.gitignore`에 추가하자
~~~
application-oauth.properties
~~~
