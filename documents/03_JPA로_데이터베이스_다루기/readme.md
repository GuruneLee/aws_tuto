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
        package com.chlee.www.springboot.domain.posts;

        import lombok.Builder;
        import lombok.Getter;
        import lombok.NoArgsConstructor;
        
        import javax.persistence.Column;
        import javax.persistence.Entity;
        import javax.persistence.GeneratedValue;
        import javax.persistence.GenerationType;
        import javax.persistence.Id;
        
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
