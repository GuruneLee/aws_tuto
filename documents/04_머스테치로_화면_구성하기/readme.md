# 04. 머스테치로 화면 구성하기
## 04.0 Intro
- 머스테치(Mustache)를 통해 화면영역을 개발하는 방법 배우기
  - 머스테치를 통한 CRUD화면 개발 방법
- 서버 템플릿 엔진 vs 클라이언트 템플릿 엔진
- JSP가 아닌 머스테치를 사용하는 이유

## 04.1 서버 템플릿 엔진과 머스테치 소개
### 템플릿 엔진이란?
: 지정된 템플릿 양식+데이터를 이용해 HTML문서를 출력하는 소프트웨어  
( 웹사이트의 화면을 어떤 형태로 만들지 도와주는 양식 )  
- JSP, Freemarker (스프링, 서블릿)
- View파일 (React, Vue)  

#### 서버 템플릿 엔진 vs 클라이언트 템플릿 엔진
이 코드는 if문과 관계없이 무조건 test를 콘솔에 출력한다  
~~~
<script type="text/javascript">

$(document).ready(function(){
    if(a=="1") {
    <%
        System.out.println("test");
    %>
    }
});
~~~
- 프론트엔드의 자바스크립트가 작동하는 영역과 JSP가 작동하는 영역이 다르기 때문
  
- JSP를 비롯한 서버 템플렛 엔진은 **서버에서 구동**된다
    - 서버 템플릿 엔진을 이용한 화면 생성은 **1)서버에서 Java코드로 문자열 생성**한 후 **2)이 문자열을 HTML로 변환**하여 **3)브라우저로 전달**한다
    - 위의 코드는 HTML을 만드는 과정 중 'System.out.println("test")'를 실행할 뿐임  
    ![서버템플릿엔진](./ste.jpg)  
- Vue.js, React.js를 이용한 SPA(Single Page Application)는 클라이언트 템플릿 방식으로, 브라우저에서 화면을 생성함
    - 서버에서는 Json/Xml형식의 데이터만 전달하고, 클라이언트에서 조립함  
    ![클라이언트템플릿엔진](./cte.jpg) 
      

### 머스테치(Mustache)란?
- 가장 심플한 템플릿엔진
- 로직코드를 사용할 수 없으므로 View역할과 서버의 역할이 분명하게 분리됨
- 다양한 언어를 지원 (루비, JS, Phython, PHP, Java, purl, Go, Asp등 대부분의 언어 지원)  
- 다른 템플릿 엔진으로는 JAP, Velocity, Freemarker, Thymeleaf등등..

### 머스테치 플러그인 설치하기
- 인텔리제이에서 플러그인 설치 가능!!!



## 04.2 기본 페이지 만들기
1. 머스테치 스타터 의존성을 build.gradle에 등록
~~~
compile('org.springframework.boot:spring-boot-starter-mustache')
~~~
2. 첫 페이지를 담당할 `index.mustache`를 `src>main>resources>templates`에 생성
- 머스테치의 기본 파일위치는 `src>main>resources>templates`임
- 이 위치에 파일을 두면 스프링 부트에서 자동으로 로딩함
~~~html
<!DOCTYPE HTML>
<html>
<head>
    <title>스프링 부트 웹서비스</title>
    <meta http-equiv="Content-Type" content="text/html"; charset="UTF-8" /
</head>

<body>
    <h1>스프링 부트로 시작하는 웹 서비스</h1>
</body>
</html>
~~~
3. `index.mustache`에 URL을 매핑
- '당연히' Controller에서 진행
- `web>IndexController`생성
~~~
@Controller
public class IndexController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
~~~
- 머스테치 스타더 덕에, 컨트롤러에서 반환하는 문자열 앞의 경로(src/main/resources/templates)와 뒤의 파일확장자(.mustache)는 자동으로 지정
- "src/main/resources/templates/index.mustache"로 전환된 문자열을 **View Resolver**가 처리하게 됨
  - View Resolver: URL요청의 결과를 전달할 타입과 값을 지정하는 관리자
  
#### IndexControllerTest
- `src>test>java>com.chlee.www.springboot>web`에 `IndexControllerTest`생성
~~~
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IndexControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void 메인페이지_로딩() {
        //when
        String body = this.restTemplate.getForObject("/", String.class);

        //then
        assertThat(body).contains("스프링 부트로 시작하는 웹 서비스");
    }
}
~~~
- 코드 설명
  - TestRestTemplate를 통해 "/"로 호출했을 때, index.mustache가 "스프링 부트로 시작하는 웹 서비스"라는 문자열을 포함하는지 확인하기
  

## 04.3 게시글 등록 화면 만들기
- API는 3장에서 구현했자나!
- 그냥 HTML을 사용하면 밋밋하니까, 오픈소스인 **부트스트랩**을 이용하자
  - 부트스트랩: 트위터에서 개발한 프론트엔드 UI 라이브러리
  
### 프론트엔드 라이브러리 사용하기
방법1. 외부 CDN사용 (Content Delivery Network)  
방법2. 직접 라이브러리 설치 (npm/bower/yarn + grunt/gulp/webpack도 여기에 속함)  
