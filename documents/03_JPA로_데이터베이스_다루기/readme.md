# 03. 스프링부트에서 JPA로 데이터베이스 다루기
## 시작하기 앞서
## 03.0 Intro
- DB의 문제점
   - 옛날에는 iBatis(현 MyBatis)와 같은 'SQL 매퍼'를 이용해 쿼리를 작성함  
   - SQL을 다루는 시간이 너무 길어짐  
   - 테이블 모델링에 집중하여, 객체지향 프로그래밍에 힘을 쓸 수 없었음
  
- 해결책
   - ORM의 등장 - **Object Relational Mapping**기술  
     (SQL매퍼는 쿼리를, ORM은 객체를 매핑함)
   - 자바 표준 ORM인 **JPA**등장
   - 자세한건 03.1에서 보자
   
## 03.1 JPA 소개
### RDB와 OOP의 패러다임 불일치
- 현대 웹 애플리케이션에선 RDB(Relational DataBase)가 필수적임  
   -> **객체를 관계형 데이터베이스에서 관리하는 능력**이 필수
- 문제점
   - 각 테이블마다 기본적인 CRUD SQL을 생성해야 하니, 애플리케이션코드보다 SQL이 더 많은 프로젝트가 넘쳐남...
   - 패러다임 불일치
      - RDB: 데이터를 저장하는 방법에 집중
      - OOP: 메시지 기반으로 기능과 속성을 한 곳에서 관리하는 방법에 집중
      - ex. 부모가 되는 객체를 가져오는 방법
         1. OOP
         ~~~
         User user = findUser();
         Group group = user.getGroup();
         ~~~
         - User와 Group이 부모-자식 관계임이 명확함
         2. OOP + RDB
         ~~~
         User user = userDao.findUser();
         Group group = groupDao.findGroup(user.getGroupId());
         ~~~
         - User따로, Group따로 조회하게 되어서 관계가 불명확해짐
   
- JPA의 등장
   - RDB와 OOP의 패러다임을 일치시켜줄 기술
   - 개발자는 oop에 집중, JPA가 이를 RDB에 맞는 SQL을 생성해서 실행해주는 것
   - **SQL에 종속적인 개발을 하지 않아도 됨!!!**
   
### Spring Data JPA란?
- JPA는 **인터페이스**로서 **자바 표준명세서**이다
   - 사용하기 위한 '구현체'가 필요하다는 의미
   - 구현체로는 'Hibernate', 'Eclipse Link'등이 있으나, Spring에서 JPA를 사용할 때는 구현체를 직접 다루지 않음
   - **구현체를 추상화** 시킨 **Spring Data JPA**라는 모듈을 이용해 JPA를 다룸
      - 'JAP < Hibernate < SpringData JPA' 형태로 추상화
      1. 구현체 교체의 용이성 (Hibernate말고 다른거 쓰고싶어!!)
      2. 저장소 교체의 용이성 (JPA같은 RDB말고 No-SQL, InMemoryDB가 쓰고싶어!!)
         - Spring Data의 하위 프로젝트들은 CRUD의 인터페이스가 같음
         - Spring Data JPA, Spring Data Redis, Spring Data MongoDB등을 쓰면 됨

### 실무에서의 JPA
- 러닝 커브가 너무 높음
   - JPA를 잘 쓰기위해선, OOP와 RDB 모두 이해하고 있어야 함
- 그러나 장점이 많아요~


## 03.2 프로젝트에 Spring Data JPA 적용하기
### 시작하기 앞서
이제부턴 실제 실습을 위한 **요구사항**을 보고, 이를 구현하는 시간을 가질것이다
- 3장~6장: 게시판 웹 애플리케이션 만들기
- 7장~10장: AWS에 무중단 배포하기

#### 요구사항
1. 게시판 기능 (CRUD)
   - 게시글 조회 (Read)
   - 게시글 등록 (Create)
   - 게시글 수정 (Update)
   - 게시글 삭제 (Delete)
2. 회원 기능
   - 구글/네이버 로그인
   - 로그인한 사용자 글 작성 권한
   - 본인 작성 글에 대한 권한 관리

<!--나중에 사진찍기-->
![요구사항분석1](./needs1.jpg)
![요구사항분석2](./needs2.jpg)
![요구사항분석3](./needs3.jpg)

### JPA의존성 등록
- build.gradle에 JPA의존성 등록
   ~~~
   compile('org.springframework.boot:spring-boot-starter-data-jpa') //1
   compile('com.h2database:h2') //2
   ~~~
    1. spring-boot-starter-data-jpa
        - 스프링부트용 Spring Data JPA추상화 라이브러리
        - 스프링부트 버전에 맞춰 자동으로 JPA관련 라이브러리들의 버전을 관리해줌
    2. h2
        - 인메모리 관계형 데이터베이스
        - 별도의 설치가 필요 없이 프로젝트 의존성만으로 관리 가능
        - 메모리에서 실행되기 때문에 애플리케이션이 재시작될 때마다 초기화됨
            - 테스트 용도로 많이 사용됨
        - 여기선 JPA의 테스트, 로컬환경에서의 구동에서 사용할 예정!
    
### 도메인영역 관리
1. `com.chlee.www.springboot`아래 `domain`패키지 만들기
    - **도메인**이란?
        - 소프트웨어에 대한 요구사항 및 문제영역
        - ex. 게시글, 댓글, 회원, 정산, 결제
    - 기존 MyBatis와같은 SQL매퍼의 dao패키지와는 다름
        - 기존엔 xml에 쿼리를 담고, 클래스는 오로지 쿼리의 결과만 담았다면, 지금은 이게 '도메인 클래스'라 불리는 곳에서 해결됨
        - 참고서적 *DDD Start, 지앤선, 2016*
2. `domain`패키지 하위에 `posts`패키지와 `Posts`클래스 생성
    - `Posts`클래스 코드  
        ~~~java
        //Posts클래스의 코드
        @Getter //6
        @NoArgsConstructor //5
        @Entity //1
        public class Posts {
        
            @Id //2
            @GeneratedValue(strategy = GenerationType.IDENTITY) //3
            private Long id;
        
            @Column(length = 500, nullable = false) //4
            private String title;
        
            @Column(columnDefinition = "TEXT", nullable = false)
            private String content;
        
            private String author;
        
            @Builder //7
            public Posts(String title, String content, String author) {
                this.title = title;
                this.content = content;
                this.author = author;
            }
        }
        ~~~
        - 어노테이션 순서: 주요 어노테이션을 클래스에 가깝게 두는게 좋음
            - @Entity는 JPA의 어노테이션, @Getter와 @NoArgsConstructor는 럼복의 어노테이션. 롬복은 필수 어노테이션이 아니므로 클래스에서 멀게 두었다
        - Posts클래스는 실제 DB의 테이블과 매칭될 클래스 - 보통 **Entity클래스**라고 함
        - 코드 설명
            1. @Entity
                - 테이블과 링크될 클래스임을 명시
                - 기본값으로 클래스의 카멜클래스(aB) 이름을 언더스코어(a_b) 네이밍으로 테이블 이름을 매칭함
                - ex. SalesManager.java -> sales_manager table
            2. @Id
                - 해당 테이블의 PK필드(Primary Key)를 나타냄
                - *PK필드가 뭘까???*
            3. @GeneratedValue
                - PK의 생성 규칙을 나타냄
                - 스프링부트 2.0에선 GenerationType.IDENTITY옵션을 추가해야 auto_increment가 된다
            4. @Column
                - 테이블의 칼럼을 나타냄
                - 선언 안해도 @Entity클래스의 필드는 모두 칼럼이 되지만, 추가 옵션이 필요할때 선언함
                - ex. 문자열 VARCHAR(255)기본 사이즈를 500으로 늘리고 싶다!
            - 아래는 앞서 설명한 롬복 라이브러리의 어노테이션 (테이블 변경시 코드 변경량을 현저히 줄여줌)
            5. @NoArgsConstructor
                - cf) @RequiredArgsConstructor
                - 기본 생성자 추가 ( public Posts(){} )
            6. @Getter
                - 클래스 내 모든 필드의 Getter메소드를 자동 생성
            7. @Builder
                - 해당 클래스의 **빌더 패턴 클래스**([링크](https://johngrib.github.io/wiki/builder-pattern/)를 참조하자)를 생성
                - 생성자 상단에 선언시, 생성자에 포함된 필드만 빌더에 포함
3. `domain.posts`패키지 하위에 `PostsRepository`인터페이스 생성 
    - `Posts`클래스로 DB에 접근하게 해줄 **JpaRepository**를 생성하는 것
    - ibatis나 MyBatis등에선 Dao라고 불리는 **DB Layer접근자** 이다
    - Entity클래스와 Entity Repository는 같은 패키지에 있어야 함!!
    ~~~
    //PostsRepository 생성
    package com.chlee.www.springboot.domain.posts;
    import org.springframework.data.jpa.repository.JpaRepository;
    
    public interface PostsRepository extends JpaRepository<Posts, Long>{
    }
    ~~~
    - 인터페이스 생성 후 'JpaRepository<*Entity클래스*, *PK타입*>'을 상속하면, 기본 CRUD메소드가 자동으로 생성됨...대박
    - '@Repository'선언도 필요없음 ㅋ

#### Entity클래스 주의사항 및 Builder패턴
- **Setter메소드는 Entity클래스에 저어어어어얼대 만들지 않는다!!!!!!!!!!!**
    - 해당 인스턴스 값들이 언제 어디서 변해야 하는지 코드상으로 구별할 수 없음
    - 값 변경이 불가피 하다면 목적과 의도가 분명한 메소드를 따로 추가해야 함
    - 주문 취소 메소드를 만들때의 예시
        1. 잘못된 사용 예시
        ~~~
        //잘못된 예
        public class Order{
            public void setStatus(boolean status) {
                this.status = status;
            }
        }
        
        public void 주문서비스의_취소이벤트() {
            order.setStatus(false);
        }
        ~~~
        2. 올바른 사용 예시
        ~~~
        //올바른 예
        public class Order{
            public void cancelOrder() {
                this.status = false;
            }
        }
        
        public void 주문서비스의_취소이벤트() {
            order.cancelOrder();
        }
        ~~~  
- 그럼 ㅅㅂ Setter가 없는데 어떻게 DB를 채워요????????
    - 기본구조: 최종값을 채운 후 **생성자를 통해** DB에 삽입
    - 값 변경이 필요하면 **해당 이벤트에 맞는 public메소드 호출**
    - 생성자를 통한 값채우기는 실수를 야기할 수 있음
        ~~~
        //생성자를 통한 값채우기 절망편
        public Example(String a, String b) {
            this.a = a;
            this.b = b;
        }
      
        new Example(b,a); //반대로 넣어 벌임
        ~~~
    - @Builder를 통한 빌더 클래스를 활용하면, 필드-값의 매칭이 명확해짐
        ~~~
        //빌더 패턴 이용
        Example.builder()
            .a(a)
            .b(b)
            .build();
        ~~~

## 03.3 Spring Data JPA 테스트 코드 작성하기
### 테스트 코드 작성
- `test`디렉토리에 `domain.posts`패키지를 생성, `PostsRepositoryTest`클래스 생성
    - `PostsRepositoryTest`코드
        ~~~
        //PostsRepositoryTest 코드
        //save, findAll기능을 테스트함
        @RunWith(SpringRunner.class)
        @SpringBootTest //4
        public class PostsRepositoryTest {
        
            @Autowired
            PostsRepository postsRepository;
        
            @After //1
            public void cleanup() {
                postsRepository.deleteAll();
            }
        
            @Test
            public void 게시글저장불러오기() {
                //given
                String title = "테스트 게시글";
                String content = "테스트 본문";
        
                postsRepository.save(Posts.builder() //2
                                        .title(title)
                                        .content(content)
                                        .author("whoRU@example.com")
                                        .build());
        
                //when
                List<Posts> postsList = postsRepository.findAll(); //3
        
                //then
                Posts posts = postsList.get(0);
                assertThat(posts.getTitle()).isEqualTo(title);
                assertThat(posts.getContent()).isEqualTo(content);
            }
        }
        ~~~
    - 코드 설명
        1. @After
            - Junit에서 단위 테스트가 끝날 때마다 수행되는 메소드를 지정
            - 보통은 배포 전 전체 테스트를 수행할 때, 테스트간 데이터 침범을 막기위해 사용
            - 여러 테스트가 동시에 수행되면 H2에 데이터가 그대로 남아서 다음 테스트실행시 테스트 실패 가능성 있음
        2. postsRepository.save
            - 테이블 posts에 insert/update 쿼리를 실행함
            - id값이 있다면 update, 없다면 insert쿼리가 실행됨
        3. postsRepository.findAll
            - 테이블 posts에 있는 모든 데이터를 조회해오는 메소드
        4. @SpringBootTest
            - 별다른 설정이 없이 이 어노테이션을 사용하면 H2데이터베이스를 자동으로 실행해줌
    
### 실제로 실행된 쿼리 보기
- SpringBoot에선 application.properties혹은 application.yml등의 파일을 수정해서 쿼리 로그를 확인 할 수 있음
- `src>main>resouces`에 `application.properties`파일을 생성
    ~~~
    spring.jpa.show_sql=true
  
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
    ~~~
    입력


## 03.4 등록/수정/조회 API 만들기
### API를 만들기 위해 필요한 클래스
1. Dto클래스
    - Request데이터를 받음
2. Controller클래스
    - API요청을 받음 
3. Service클래스
    - 트랜잭션,도메인 기능간의 순서를 보장함
        - 트랜잭션이 뭘까?  
          : 트랜잭션(Transaction 이하 트랜잭션)이란, 데이터베이스의 상태를 변화시키기 해서 수행하는 작업의 단위를 뜻한다. 간단하게 말해서 아래의 질의어(SQL)를 이용하여 데이터베이스를 접근 하는 것을 의미
    - **비지니스 로직은 여기에서 처리하지 않음!!**
        - 비지니스 로직이란?  
        : 유저 눈엔 보이진 않지만, 유저가 바라는 결과물을 올바르게 도출하기 위해 코드 (Presentation/ View영역과 상반됨)
    - 그럼 **비지니스 로직은 어디서 처리함?**  
    : **Domain Model**에서 비지니스 처리를 담당해야함!!
      
#### 이해를 위한 막간 Spring 웹 계층 살펴보기!
![spring웹계층](./springWebArch.jpg)
- Web Layer
    - 흔히 사용하는 컨트롤러(@Controller)와 JSP/Freemarker등의 뷰 템플릿 영역
    - 이 외에 필터(@Filter), 인터셉터, 컨트롤러 어드바이스(@ControllerAdvice)등 외부 요청과 응답에 대한 전반적인 영역을 의미
    
- Service Layer
    - @Service에 사용되는 서비스 영역
    - 일반적으로 Controller와 Dao중간 영역에서 사용
    - @Transactional이 사용되어야 하는 영역 
    
- Repository Layer
    - Database와 같이 데이터 저장소에 접근하는 영역
    - Dao(Data Access Object)가 이 영역에 해당됨
    
- Dtos
    - Dto(Data Transfer Object)는 계층 간에 데이터 교환을 위한 객체. Dtos는 이들의 영역
    - ex. 뷰 템플릿 엔진에서 사용될 객체. Repository Layer에서 결과로 넘겨준 객체.
    
- Domain Model
    - '도메인'이라는 개발 대상을 모든 사람이 동일한 관점에서 이해할 수 있고 공유할 수 있도록 단순화시킨 것
    - @Entity가 사용된 영역도 도메인 모델에 속함
    - 무조건 DB의 테이블과 관계가있는것은 아님 (VO(value object)처럼 값 객체들도 이 영역에 속함)
    - **여기서 비지니스 로직을 처리해야 함**
    
### 등록 기능 만들기
- `web>PostsApiController`, `service.posts>PostsService`, `web.dto>PostsSaveRequestDto` 클래스 생성
    1. `PostsApiController`코드
        ~~~
        //PostsApiController 코드
        @RequiredArgsConstructor
        @RestController
        public class PostsApiController {
        
            private final PostsService postsService;
        
            @PostMapping("/api/v1/posts")
            public Long save(@RequestBody PostsSaveRequestDto requestDto) {
                return postsService.save(requestDto);
            }
        }
        ~~~
    2. `PostsService` 코드
        ~~~
        //PostsService 코드
        @RequiredArgsConstructor
        @Service
        public class PostsService {
        
            private final PostsRepository postsRepository;
        
            @Transactional
            public Long save(PostsSaveRequestDto requestDto) {
                return postsRepository.save(requestDto.toEntity()).getId();
            }
        }
        ~~~ 
    3. `PostsSaveRequestDto` 코드
        ~~~
        //PostsSaveRequestDto 코드
        @Getter
        @NoArgsConstructor
        public class PostsSaveRequestDto {
            private String title;
            private String content;
            private String author;
            @Builder
            public PostsSaveRequestDto(String title, String content, String author) {
                this.title = title;
                this.content = content;
                this.author = author;
            }
            
            public Posts toEntity() {
                return Posts.builder()
                        .title(title)
                        .content(content)
                        .author(author)
                        .build();
            }
        }
        ~~~ 
       
    - 코드를 다시 한 번 자세히 읽어보는 것을 추천한다
    - 어차피 다시 코드를 봐야겠지만, 현재 내가 이해한 것을 적어놓겠다. 
        - Controller는 API요청을 받으면, ~함수를 실행시킨다! 만 명시
        - Service는 Controller가 명시해놓은 것 중에서 DB에 접근하는 일만 맡아서 함
        - Dto는 Service가 DB에 넣어야 하는 데이터를 인스턴스로 정의해서 만들어주는 놈 (Entity클래스의 인스턴스를 만드는거임 결국)
    
#### Dto클래스와 Entity클래스
- Entity클래스와 Dto클래스는 모양새가 많이 비슷하다
    - Dto클래스의 toEntity()함수도 Entity클래스의 인스턴스를 만들어서 반환함
    - **모양도 비슷한데 왜 Dto클래스를 따로 만들어서 쓸까?**
    
##### Entity클래스를 Request/Response클래스로 사용하면 안된다!
- Entity클래스는 DB와 맞닿은 핵심 클래스 이다!!!
    - Entity클래스를 기준으로 테이블이 생성되고, 스키마가 변경된다
- View Layer와 DB Layer의 역할 분리를 철저히 하는것이 좋다
    - Request와 Response용 Dto는 View를 위한 클래스라 정말 정말 자아아아아주 변경이 필요함
    - 화면변경은 너무 사소한 기능변경인데, 이 때 마다 Entity클래스를 변경할 수 없다!!
    
- Entity 클래스와 Controller에서 쓸 Dto는 꼭 분리해서 사용하자


#### Controller 테스트 코드 작성  
- `src>test>java>com.chlee.www.springboot>web`에 `PostsApiControllerTest`클래스 작성
    - `PostsApiControllerTest` 코드
        ~~~
        @RunWith(SpringRunner.class)
        @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
        public class PostsApiControllerTest {
        
            @LocalServerPort
            private int port;
        
            @Autowired
            private TestRestTemplate restTemplate;
        
            @Autowired
            private PostsRepository postsRepository;
        
            @After
            public void tearDown() throws Exception {
                postsRepository.deleteAll();
            }
        
            @Test
            public void Posts_등록된다() throws Exception {
                //given
                String title = "title";
                String content = "content";
                PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
                        .title(title)
                        .content(content)
                        .author("author")
                        .build();
        
                String url = "http://localhost:" + port + "/api/v1/posts";
        
                //when
                ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url, requestDto, Long.class);
        
                //then
                assertThat(responseEntity.getStatusCode())
                        .isEqualTo(HttpStatus.OK);
                assertThat(responseEntity.getBody())
                        .isGreaterThan(0L);
        
                List<Posts> all = postsRepository.findAll();
                assertThat(all.get(0).getTitle()).isEqualTo(title);
                assertThat(all.get(0).getContent())
                        .isEqualTo(content);
        
            }
        }
        ~~~
    - 코드 설명
        - HelloController와 달리 @WebMvcTest를 사용하지 않았음  
        -> @WebMvcTest는 JPA기능이 작동하지 않음!!!!!!
        - JPA기능까지 한 번에 테스트 할 때는, @SpringBootTest와 TestRestTemplate를 사용하면 된다!
    
### 수정/조회 기능 만들기
- `web>PostsApiController`, `domain.posts>Posts`, `service.posts>PostsService` 클래스 수정
- `web.dto>PostsResponseDto`, `web.dto>PostsUpdateRequestDto` 클래스 생성
    1. `PostsApiController`코드 수정
        ~~~
        //PostsApiController 코드
        @PutMapping("/api/v1/posts/{id}")
        public Long update(@PathVariable Long id, @RequestBody PostsUpdateRequestDto requestDto) {
            return postsService.update(id, requestDto);
        }
        
        @GetMapping("/api/v1/posts/{id}")
        public PostsResponseDto findById (@PathVariable Long id) {
            return postsService.findById(id);
        }  
        ~~~
    2. `PostsResponseDto` 코드
        ~~~
        //PostsResponseDto 코드
        @Getter
        public class PostsResponseDto {
        
            private Long id;
            private String title;
            private String content;
            private String author;
            
            //PostsResponseDto는 Entity의 필드 중 일부만 사용하므로, 
            //생성자로 Entity를 받아 필드에 값을 넣는다
            public PostsResponseDto(Posts entity) {
                this.id = entity.getId();
                this.title = entity.getTitle();
                this.content = entity.getContent();
                this.author = entity.getAuthor();
            } 
        }
        ~~~ 
    3. `PostsUpdateRequestDto` 코드
        ~~~
        //PostsUpdateRequestDto 코드
        @Getter
        @NoArgsConstructor
        public class PostsUpdateRequestDto {
        
            private String title;
            private String content;
            
            @Builder
            public PostsUpdateRequestDto(String title, String content) {
                this.title = title;
                this.content = content;
            }
        }
        ~~~ 
       
    4. `Posts` 코드 추가
        ~~~
        //PostsUpdateRequestDto 코드
        public void update(String title, String content) {
            this.title = title;
            this.content = content;
        }
        ~~~ 
       
    5. `PostsService` 코드 추가
        ~~~
        @Transactional
        public Long update(Long id, PostsUpdateRequestDto requestDto) {
            Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + id));
            posts.update(requestDto.getTitle(), requestDto.getContent());
            //근데.. 데이터베이스에 쿼리를 날리는 부분이 없다?
            //PostsRepository를 이용해서 DB도 업데이트 해야하는거 아닌가?
            return id;
        }
        
        public PostsResponseDto findById (Long id) {
            Posts entity = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다 id=" + id));
            
            return new PostsResponseDto(entity);
        }
        ~~~
       
    - 코드설명
        1. `PostsService`의 update에서 데이터베이스에 쿼리를 날리는 부분이 없음 
            - **JPA의 영속성 컨텍스트** 때문임
            - 영속성 컨텍스트: 엔티티를 영구 저장하는 환경(논리적 개념)
            - JPA의 핵심 내용은 엔티티가 영속성 컨텍스트에 포함되어있냐 아니냐로 갈리게 됨
                - JPA의 **엔티티 매니저(Entity Manager)가 활성화**된 상태(Spring Data Jpa에선 기본 옵션 on)로 
                  **트랜잭션 안에서 데이터베이스에서 데이터를 가져오면** 이 데이터는 영속성 컨텍스트가 유지되어있는 상태
                - 이 상태에서 해당 데이터 값을 변경하면, **트랜잭션이 끝나는 시점에 해당 테이블에 변경분을 반영**함
                - 이를 **더티체킹 dirty checking**이라고 부름 [(링크)](https://jojoldu.tistory.com/415)
                    
#### Controller 테스트코드 추가
- PUT테스트
~~~
@Test
public void Posts_수정된다() throws Exception {
    //given
    Posts savedPosts = postsRepository.save(Posts.builder()
            .title("title")
            .content("content")
            .author("author")
            .build());

    Long updateId = savedPosts.getId();
    String expectedTitle = "title2";
    String expectedContent = "content2";

    PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder()
            .title(expectedTitle)
            .content(expectedContent)
            .build();

    String url = "http://localhost:" + port + "api/v1/posts/" + updateId;

    HttpEntity<PostsUpdateRequestDto> requestEnity = new HttpEntity<>(requestDto);

    //when
    ResponseEntity<Long> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEnity, Long.class);

    //then
    assertThat(responseEntity.getStatusCode())
            .isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody())
            .isGreaterThan(0L);

    List<Posts> all = postsRepository.findAll();
    assertThat(all.get(0).getTitle())
            .isEqualTo(expectedTitle);
    assertThat(all.get(0).getContent())
            .isEqualTo(expectedContent);
}
~~~

- GET테스트는 어.. p.118을 참고해주세요


## 03.5 JPA Auditin으로 생성시간/수정시간 자동화하기
- JPA Auditing을 이용하면 DB에 들어가는 생성시간/수정시간 입력을 자동화할 수 있다
### LocalDate사용  
Java8 이상에서의 날짜타입 **LocalDate**와 **LocalDateTime** 라이브러리를 무조건 사용해야함
1. `domain>BaseTimeEntity`클래스 생성
    ~~~
    //BaseTimeEntity 클래스 생성
    @Getter
    @MappedSuperclass //1
    @EntityListeners(AuditingEntityListener.class) //2
    public class BaseTimeEntity {
    
        @CreatedDate //3
        private LocalDateTime createdDate;
        
        @LastModifiedDate //4
        private LocalDateTime modifiedDate;
    }
    ~~~
    - 코드 설명
        1. @MappedSuperclass
            - JPA Entity클래스들이 BaseTimeEntity를 상속할 경우, 필드도 칼럼으로 인식하게됨 (createdDate, modifiedDate)
        2. @EntityListeners(AuditingEntityListener.class)
            - BaseTimeEntity 클래스에 Auditing기능을 포함시킴
        3. @CreatedDate
            - Entity가 생성되어 저장될 때 시간이 자동 저장됨
        4. @LastModifiedDate
            - 조회한 Entity의 값을 변경할 때 시간이 자동 저장됨
    
2. `Posts`클래스가 `BaseTimeEntity`를 상속받도록 변경
    ~~~
    public class Posts extends BaseTimeEntity {
        ...
    }
    ~~~
   
3. JPA Auditing어노테이션들을 모두 활성화
    - Application 클래스에 활성화 어노테이션 추가
        ~~~
        @EnableJpaAuditing
        ~~~
    
#### JPA Auditing 테스트코드 추가하기 (on PostsRepositoryTest)
- `PostsRepositoryTest`코드 수정
    ~~~
    @Test
    public void BaseTimeEntity_등록() {
        //given
        LocalDateTime now = LocalDateTime.of(2021,2,15,0,0,0);
        postsRepository.save(Posts.builder().title("t").content("t").author("a").build());

        //when
        List<Posts> postsLists = postsRepository.findAll();

        //then
        Posts posts = postsLists.get(0);

        System.out.println(">>>>>> createDate=" + posts.getCreatedDate() + ", modifiedDate=" + posts.getModifiedDate());

        assertThat(posts.getCreatedDate()).isAfter(now);
        assertThat(posts.getModifiedDate()).isAfter(now);
    }
    ~~~
  
- 앞으로 추가될 Entity들은 `BaseTimeEntity`만 상속받으면 이짓거리 안해도 생성시간/수정시간 알아서 다 저장됨 개꿀