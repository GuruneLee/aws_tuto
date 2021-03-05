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


> 이제, 발급받은 구글 로그인 인증 정보로 프로젝트 구현을 진행해보자  
### User엔티티 관련 코드 작성
1. `src/main/java/com/chlee/www/springboot/domain/user`에 사용자 정보를 담당할 도메인인 `User`클래스를 생성
~~~java
@Getter
@NoArgsConstructor
@Entity
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public User(String name, String email, String picture, Role role) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
    }

    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;

        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
~~~
  - @Enumerated(EnumType.STRING)
    - JPA로 데이터베이스로 저장할 때 Enum값을 어떤 형태로 저장할지를 결정
    - 기본적으로는 int로 된 숫자가 저장됨
    - 숫자로 저장되면 데이터베이스로 확인할때, 확인하기가 힘드므로 문자열(EnumType.String)로 저장될 수 있도록 선언
  

2. `src/main/java/com/chlee/www/springboot/domain/user`에 사용자의 권한을 관리할 Enum클래스 `Role`을 생성
~~~
@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST("ROLE_GUEST", "손님"),
    USER("ROLE_USER", "일반 사용자");

    private final String key;
    private final String title;
}
~~~
  - 스프링 시큐리티에서는 권한코드에 항상 ROLE_이 앞에 있어야함

3. `src/main/java/com/chlee/www/springboot/domain/user`에 `User`의 CRUD를 책임질 `UserRepository`인터페이스를 생성
~~~
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
~~~
  - findByEmail
    - 소셜 로그인으로 반환되는 값 중 email을 통해 이미 생성된 사용자인지, 처음가입하는 사용자인지 판단하기 위한 메소드
  
### 스프링 시큐리티 설정
1. `build.gradle`에 스프링 시큐리티 관련 의존성 추가하기
~~~
compile('org.springframework.boot:spring-boot-starter-oauth2-client')
~~~  
- spring-boot-starter-oauth2-client
    - 소셜 로그인 등 클라이언트 입장에서 소셜 기능 구현 시 필요한 의존성
    - spring-security-oauth2-client와 spring-security-oauth2-jose를 기본으로 관리함
    
2. OAuth라이브러리를 이용한 소셜 로그인 설정 코드 작성하기
- `config.auth`패키지 생성 (시큐리티 관련 클래스를 모두 관리)
- `SecurityConfig`클래스 생성
~~~java
@RequiredArgsConstructor
@EnableWebSecurity //1
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    private final CustomOAuth2UserService customOAuth2UserService;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .headers().frameOptions().disable() //2
                .and()
                    .authorizeRequests() //3
                    .antMatchers("/", "/css/**", "/images/**", "/js/**", "/h2-console/**").permitAll()
                    .antMatchers("/api/v1/**").hasRole(Role.USER.name()) //4
                    .anyRequest().authenticated() //5
                .and()
                    .logout()
                        .logoutSuccessUrl("/") //6
                .and()
                    .oauth2Login() //7
                        .userInfoEndpoint() //8
                            .userService(customOAuth2UserService); //9
    }
}
~~~
- 코드설명
    1. @EnableWebSecurity
        - Spring Security 설정들을 활성화 시켜줌
    2. .csrf().disable().headers().frameOptions().disable()
        - h2-console화면을 사용하기위해서 해당 옵션을 모두 disable함
    3. authorizeRequests()
        - URL별 권한 관리를 설정하는 옵션의 시작점
        - authorizeRequests가 선언되어야만 antMatchers옵션을 사용할 수 있음
    4. antMatchers()
        - 권한 관리 대상을 지정
        - URL, HTTP 메소드별로 관리 가능
        - "/"등 지정된 url은 permitAll()을 통해 전체 열람 권한을 줌
        - "api/v1/**"주소를 사진 API는 USER권한을 가진 사람만 가능
    5. anyRequest
        - 설정된 값들 이외 나머지 URL들을 나타냄
        - 여기선 .authenticated()를 추가하여, 나머지 URL들은 모두 인증된 사용자들에게만 허용함
        - 인증된 사용자 = 로그인된 사용자
    6. logout().logoutSuccessUrl("/")
        - 로그아웃 기능에 대한 설정의 진입점
        - 로그아웃 성공시 "/"로 이동함   
    7. oauth2Login()
        - OAuth2로그인 기능에 대한 설정의 진입점
    8. userInfoEndpoint()
        - OAuth2 로그인 성공 이후 사용자 정보를 가져올 때의 설정들을 담당함       

    9. userService()
        - 소셜 로그인 성공 시 후속 조치를 진행할 UserService 인터페이스의 구현체를 등록함
        - 리소스 서버(즉, 소셜 서비스들)에서 사용자 정보를 가져온 상태에서 추가로 진행하고자 하는 기능을 명시할 수 있음

- `CustomOAuth2UserService`클래스 생성
- 구글 로그인 이후 가져온 사용자의 정보를 기반으로 가입/정보수정/세션저장 등의 기능
~~~java
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); //1
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(); //2

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes()); //3
        
        User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(user)); //4

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey()))
                , attributes.getAttributes()
                , attributes.getNameAttributeKey()
        );

        private User saveOrUpdate(OAuthAttributes attributes) {
            User user = userRepository.findByEmail(attributes.getEmail())
                    .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                    .orElse(attributes.toEntity());

            return userRepository.save(user);
        }
    }
}
~~~
- 코드설명
    1. registrationId
        - 현재 로그인 진행중인 서비스를 구분하는 코드
        - 지금은 구글만 사용하는 값이라 불필요하지만, 이후 네이버 로그인 연동 시 네이버로그인/구글로그인을 구분하기 위해 쓰임
    2. userNameAttributeName
        - OAuth2 로그인 진행 시 키가 되는 필드값. Primary Key와 같음
        - 구글의 경우 기본적으로 코드를 지원함 (기본코드 "sub", 네이버 카카오는 지원안함)
        - 이후 네이버 로그인과 구글 로그인을 동시 지원할 때 사용됨
    3. OAuthAttributes
        - OAuth2UserService를 통해 가져온 OAuth2User의 attribute를 담을 클래스
        - 이후 네이버 등 다른 소셜 로그인도 이 클래스를 사용함
        - 아래에서 코드 작성할 거임
    4. SessionUser
        - 세션에 사용자 정보를 저장하기 위한 Dto클래스
        - *근데 왜 User클래스 안쓰고 또 따로 만들어서 사용함??* - 아래에 또 나온다 기다려라
    - 구글 사용자 정보 업데이트 되었을 때를 대비하여 **update**기능도 같이 구현 (User entity에 자동으로 반영됨)
    

- `OAuthAttributes`클래스 생성
- Dto이기 때문에 `config/auth/dto`패키지 생성해서 클래스 코드 작성
~~~java
@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
    }

    //1
    public static OAuthAttributes of(String registraionId, String userNameAttributeName, Map<String, Object> attributes) {
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    //2
    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.GUEST)
                .build();
    }
}
~~~
- 코드 설명
    1. of()
        - OAuth2User에서 반환하는 사용자 정보는 Map이기 때문에, 값 하나하나를 변환해줘야 함
    2. toEntity()
        - User 엔터티를 생성함
        - OAuthAttributes에서 엔터티를 생성하는 시점은 처음 가입할 때!!
        - 가입할 때의 기본 권한을 GUEST로 주기 위해서, role빌더값에는 Role.GUEST를 사용함
        - OAuthAttributes 클래스 생성이 끝났으면, 같은 패키지에 SessionUser클래스를 생성함
    
- `config/auth/dto`에 `SessionUser`클래스도 생성하자
~~~java
@Getter
public class SessionUser implements Serializable {
    private String name;
    private String email;
    private String picture;

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getName();
        this.picture = user.getPicture();
    }
}
~~~
- SessionUser에는 **인증된 사용자 정보**만 필요함. 따라서 name, email, picture만 필드로 선언함
- User클래스말고 SessionUser를 따로 만들어서 쓰는 이유
    - error *Failed to convert from type \[java.lang.Object\] to type \[byte\[\]\] for value ...*
    - **직렬화를 구현하지 않음**이라는 에러임 (Serializable)
    - 이를 해결하기 위해서 User클래스에 직렬화코드를 넣을 수 없음...
        - 엔티티 클래스는 막쓰는거 아니라고 했냐 안했냐!!!!


### 로그인 테스트
- 화면에 로그인 버튼 추가해서, 잘 됐는지 확인해보장
1. `index.mustache`에 로그인버튼과 로그인 성공 시 사용자 이름을 보여주는 코드 추가
~~~html
...
<h1>스프링 부트로 시작하는 웹 서비스</h1>
    <div class="col-md-12">
        <!--로그인 기능 영역-->
        <div class="row">
            <div class="col-md-6">
                <a href="/posts/save" role="button" class="btn btn-primary">글 등록</a>
                {{#userName}}
                    Logged in as: <span id="user">{{userName}}</span>
                    <a href="/logout" class="btn btn-info active" role="button">Logout</a>
                {{/userName}}
                {{^userName}}
                    <a href="/oauth2/authorization/google" class="btn btn-success active" role="button">Google Login</a>
                {{/userName}}
            </div>
        </div>
        <br>
        <!--목록 출력 영역-->
        
...
~~~
- 코드 설명
    1. {{#userName}}
        - 머스테치는 다른 언어와 같은 if문 (if userName != null 등)을 제공하지 않음
        - true/false만 판별
        - 따라서, 머스테치에서는 항상 최종값을 넘겨줘야 함
        - 여기서도 userName이 있으면 userName을 노출시키도록 구성함
    2. a href="/logout"
        - 스프링 시큐리티에서 기본적으로 제공하는 로그아웃 URL
        - 즉, 개발자가 별도로 저 URL에 해당하는 컨트롤러를 만들 필요가 없음
    3. {{^userName}}
        - 머스테치에서 해당 값이 존재하지 않는 경우에는 ^를 사용함
        - 여기선 userName이 없다면 로그인 버튼을 노출시키도록 구성함
    4. a href="oauth2/authorization/google"
        - 스프링 시큐리티에서 기본적으로 제공하는 로그인 URL
        - 즉, 개발자가 별도로 저 URL에 해당하는 컨트롤러를 만들 필요가 없음
    

2. `IndexController`코드 추가 
- `index.mustache`에서 'userName'을 사용할 수 있게끔 userName을 modle에 저장하는 코드 추가
~~~
...
private final HttpSession httpSession;

@GetMapping("/")
public String index(Model model) {
    model.addAttribute("posts", postsService.findAllDesc());

    SessionUser user = (SessionUser) httpSession.getAttribute("user"); //1
    if (user != null) { //2
        model.addAttribute("userName", user.getName());
    }

    return "index";
}
...
~~~
- 코드 설명
    1. (SessionUser) httpSession.getAttribute("user")
        - 앞서 작성된 CustomOAuth2UserService에서 로그인 성공 시 세션에 SessionUser를 저장하도록 구성
        - 즉, 로그인 성공 시 httpSession.getAttribute("user")에서 값을 가져올 수 있음
    2. if (user != null)
        - 세션에 저장된 값이 있을 때만, model에 userName으로 등록
        - 세션에 저장된 값이 없으면 로그인 버튼이 보이게쬬?
    

## 05.4 어노테이션 기반으로 개선하기
- 같은 코드가 반복되는 부분을 개선해보자
~~~
SessionUser user = (SessionUser) httpSession.getAttribute("user");
~~~
- 세션값을 가져오는 부분을 보면, index메소드 외에 다른 컨트롤러와 메소드에서 세션값이 필요할 때마다 직접 세션에서 값을 가져와야함
- -> 메소드 인자로 세션값을 바로 받을 수 있도록 변경하자
1. `config.auth`패키지에 `@LoginUser`어노테이션 생성
~~~java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}
~~~
- 코드 설명
    1. @Target(ElementType.PARAMETER)
        - 이 어노테이션이 생성될 수 있는 위치 지정
        - PARAMETER -> 메소드의 파라미터로 선언된 객체에서만 사용할 수 있음
        - 이 외에도 TYPE등, 클래스 선언문에 쓸 수 있는 값도 있다용
    2. @interface
        - 이 파일을 어노테이션 클래스로 지정함
        - LoginUser라는 이름을 가진 어노테이션이 생성된거임!!
    
2. 같은 위치에 `LoginUserArgumentResolver`생성하기
- Login-UserArgumentResolver 라는 HandlerMethodArgumentResolver 인터페이스를 구현한 클래스
- HandlerMethodArgumentResolver -> 조건에 맞는 경우 메소드가 있다면 HandlerMethodArgumentResolver의 구현체가 지정한 값으로 해당 메소드의 파라미터로 넘길 수 있음
~~~java
@RequiredArgsConstructor
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final HttpSession httpSession;

    @Override
    public boolean supportsParameter(MethodParameter parameter) { //1
        boolean isLoginUserAnnotation = parameter.getParameterAnnotation(LoginUser.class) != null;
        boolean isUserClass = SessionUser.class.equals(parameter.getParameterType());
        return isLoginUserAnnotation&&isUserClass;
    }

    @Override //2
    public Object resolveArgument (MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return httpSession.getAttribute("user");
    }
}
~~~
- 코드 설명
    1. supportsParameter()
        - 컨트롤러 메서드의 특정 파라미터를 지원하는지 판단함
        - 여기서는 파라미터에 @LoginUser어노테이션이 붙어있고, 파라미터 클래스 타입이 SessionUser.class인 경우 true를 반환함
    2. resolveArgument()
        - 파라미터에 전달할 객체를 생성함
        - 여기서는 세션에서 객체를 가져옴
    

3. `config`패키지에 `WebConfig`생성 (WebMvcConfigurer)
- LoginUserArgumentResolver가 스프링에서 인식될 수 있도록 해보자
~~~java
@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginUserArgumentResolver loginUserArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginUserArgumentResolver);
    }
}
~~~
- HandleraMethodArgumentResolver는 항상 WebMvcConfigurer의 'addArgumentResolvers()'를 통해 추가해야 함
- 다른 HandleraMethodArgumentResolver가 필요하면 같은 방법으로 추가해주면 댐댐대대대댐


4. `IndexController`의 코드에서 반복되는 부분들을 모두 @LoginUser로 개선하자
~~~
...
    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user) { //1
        model.addAttribute("posts", postsService.findAllDesc());

        if (user != null) {
            model.addAttribute("userName", user.getName());
        }

        return "index";
    }
...
~~~
- 코드 설명
    1. @LoginUser SessionUser user
        - 기존에 (User) httpSession.getAttribute("user")로 가져오던 세션 정보 값이 개선됨
        - 이제는 어느 컨트롤러에서든지 @LoginUser만 사용하면 세션 정보를 가져올 수 있음
    

## 05.5 세션 저장소로 데이터베이스 사용하기
- 문제점1: **애플리케이션을 재실행하면 로그인이 풀려벌임!!?!?!??!????!?!!!!!!!!!!**
    - 세션이 내장 톰캣의 메모리에 저장되게 때문임
    - 배포할 때마다 톰캣이 재시작 됨
    
- 문제점2: 두 대 이상의 서버에서 서비스하고 있다면 **톰캣마다 세션 동기화**설정을 해줘야함

- 현업에서 사용해는 해결책
    1. 톰캣 세션 사용
        - 별다른 설정 x
        - 세션 공유를 위한 추가 설정이 필요함
    2. MySQL과 같은 데이터베이스를 세션 저장소로 사용
        - 여러 WAS간 공용 세션을 사용할 수 있는 가장 쉬운 방법
        - 로그인 요청마다 DB IO가 발생함 -> 성능상 이슈 발생 가능
        - 로그인 요청이 많이 없는 '백오피스', '사내 시스템'등에서 사용
    3. Redis, Memcached와 같은 메모리DB를 세션 저장소로 사용
        - B2C서비스에서 가장 많이 사용하는 방식
        - 실제 서비스로 사용하려면 외부 메모리 서버가 필요함 (not Embedded-Redis)
    
- 여기선 `2`번 방법인 **데이터베이스를 세션 저장소로 사용**하는 방식을 선택
    - 비용적고, 간단, 사용자별로 없잖슴 ㅋㅋㄹㅃㅃ
    
### spring-session-jdbc 등록
1. `build.gradle`에 'spring-session-jdbc'등록하기
~~~
compile('org.springframework.session:spring-session-jdbc')
~~~
2. `application.properties`에 세션 저장소를 jdpc로 선택하도록 코드 추가
~~~
spring.session.store-type=jdbc
~~~
3. 앱 실행 후 로그인 테스트하고 h2-console확인하기
- SPRING_SESSION, SPRING_SESSION_ATTRIBUTES 테이블이 생겨야 함 (JPA로 인해 자동 생성)
- 한 개의 세션이 등록되어 있어야 함
- 물론 지금은 h2를 사용하고 있으니, 재시작 시 세션이 풀리지만 / 이후 AWS로 배포하게 되면 RDS를 사용하게 되므로 세션이 풀리지 않게됨
    - 기반코드 작성한거임~


## 05.6 네이버 로그인 추가
- 안할거임

## 05.7 기존 테스트에 시큐리티 적용하기
