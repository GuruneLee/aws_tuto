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
  

## 04.3 게시글 등록 화면 만들기 (Create)
- API는 3장에서 구현했자나!
- 그냥 HTML을 사용하면 밋밋하니까, 오픈소스인 **부트스트랩**을 이용하자
  - 부트스트랩: 트위터에서 개발한 프론트엔드 UI 라이브러리
  
### 프론트엔드 라이브러리 사용하는 방법
방법1. 외부 CDN사용 (Content Delivery Network)  
방법2. 직접 라이브러리 설치 (npm/bower/yarn + grunt/gulp/webpack도 여기에 속함)  

### CDN을 이용해서 게시글 등록화면 구현하기
- 부트스트랩/제이쿼리를 ,`index.mustache`에 추가하자
- '레이아웃'방식으로 추가 -> 공통영역을 별도의 파일로 분리하여 필요한 곳에서 가져다 쓰는 방식
- 부트스트랩/제이쿼리는 모든 머스테치화면에서 필요하므로, 레이아웃 파일들을 만들어 추가하자
  
- `src/main/resources/templates`디렉토리에 `layout`디렉토리를 추가로 생성. 여기에 `footer.mustache`와 `header.mustache`파일 생성
~~~html
<!--header-->
<!DOCTYPE HTML>
<html>
<head>
  <title>스프링 부트 웹서비스</title>
  <meta http-equiv="Content-Type" content="text/html"; charset="UTF-8" />

  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
</head>

<body>
~~~
~~~html
<!--footer-->
<script src="https://code.jquery.com/jquery-3.3.1min.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>

</body>
</html>
~~~

1. `index.mustache`파일이 레이아웃을 써먹을 수 있게 바꾸기 + '글 등록'버튼 추가
~~~html
{{>layout/header}} <!--현재 머스테치 파일의 위치를 기준으로 다른파일을 가져옴-->
    <h1>스프링 부트로 시작하는 웹 서비스</h1>
    <div class="col-md-12">
        <div class="row">
            <div class="col-md-6">
                <a href="/posts/save" role="button" class="btn btn-primary">글 등록</a>
            </div>
        </div>
    </div>
{{>layout/footer}}
~~~
- 버튼을 누르면 "/posts/save"로 이동하게끔 링크를 걸어줌
- 이 주소에 해당하는 컨트롤러 만들어야댐

2. `indexController`에 버튼 컨트롤 추가
~~~
...
@GetMapping("/posts/save")
public String postsSave() {
  return "posts-save";
}
...
~~~
- /posts/save를 호출하면 posts-save.mustache를 호출하는 메소드

3. /posts/save화면을 책임질 `posts-save.mustache`를 `templates`디렉토리에 생성
~~~html
{{>layout/header}}
<h1>게시글 등록</h1>

<div class="col-md-12">
    <div class="col-md-4">
        <form>
            <div class="form-group">
                <label for="title">제목</label>
                <input type="text" class="form-control" id="title" placeholder="제목을 입력하세요">
            </div>
            <div class="form-group">
                <label for="author"> 작성자</label>
                <input type="text" class="form-control" id="author" placeholder="작성자를 입력하세요">
            </div>
            <div class="form-group">
                <label for="content"> 내용</label>
                <textarea class="form-control" id="content" placeholder="내용을 입력하세요"></textarea>
            </div>
        </form>
        <a href="/" role="button" class="btn btn-secondary">취소</a>
        <button type="button" class="btn btn-primary" id="btn-save">등록</button>
    </div>
</div>

{{>layout/footer}}
~~~
- 모양은 나왔지만, 등록 버튼은 아직 제 기능을 하지 않음 (api를 호출하는 JS가 없음)

4. `src/main/resources`에 `static/js/app`디렉토리 생성. 여기에 `index.js`생성
~~~
var main = {
    init : function() {
        var _this = this;
        $('#btn-save').on('click', function() {
            _this.save();
        });
    },
    save : function() {
        var data = {
            title: $('#title').val(),
            author: $('#author').val(),
            content: $('#content').val()
        };

        $.ajax({
            type: 'POST',
            url: '/api/v1/posts',
            dataType: 'json',
            contentType: 'application/json; charest=utf-8',
            data: JSON.stringify(data)
        }).done(function() {
            alert('글이 등록되었습니다.');
            window.location.href = '/'; //1
        }).fail(function (error) {
            alert(JSON.stringfy(error));
        });
    }
};

main.init();
~~~
- window.location.href='/'
  - '/'로 위치를 변경하는거임~
  
- var main={...}처럼 index라는 변수의 속성으로 function을 추가한 이유
  - 'init' 'save'함수의 유효 스코프를 var index라는 객체로 지정해서 이름이 겹쳐서 생기는 에러를 차단함


5. `index.js`를 머스테치 파일이 사용할 수 있게, `footer.mustache`에 추가하기
~~~html
...
<!--index.js 추가-->
<script src="/js/app/index.js"></script>
...
~~~
- 스프링부트에선 기본적으로 `src/main/resources/static`에 위치한 자바스크립트, CSS, 이미지 등 정적 파일들은 URL에서 /로 설정됨
  - ex. src/main/resources/static/js/... -> http://도메인/js/...
  - 이런식으로 호출할 수 있음
  
## 04.4 전체 조회 화면 만들기 (Read)
1. `index.mustache`를 수정해서 게시글 Read가능하게 하기
~~~html
...
    <br>
        <!--목록 출력 영역-->
        <table class="table table-horizontal table-bordered">
            <thead class="thead-strong">
            <tr>
                <th>게시글번호</th>
                <th>제목</th>
                <th>작성자</th>
                <th>최종수정일</th>
            </tr>
            </thead>
            <tbody id="tbody">
            {{#posts}} <!--1-->
                <tr>
                    <td>{{id}}</td> <!--2-->
                    <td>{{title}}</td>
                    <td>{{author}}</td>
                    <td>{{modifiedDate}}</td>
                </tr>
            {{/posts}}
            </tbody>
        </table>
...
~~~
- {{#posts}} 
  - posts라는 List를 순회함 (DB테이블)
  - Java의 for문과 동일한 기능임 ㅋㅋ
  
- {{id}}등의 {{변수명}}
  - List레서 뽑아낸 객체의 필드를 사용합니다
  
2. Repository코드 작성 (Controller/Service/**Repository**)
- `PostsRepository`인터페이스에 쿼리를 추가하자
~~~
public interface PostsRepository extends JpaRepository<Posts, Long>{
    
    @Query("SELECT p FROM Posts p ORDER BY p.id DESC")
    List<Posts> findAllDesc();
}
~~~
- SpringDataJpa에서 제공하지 않는 메소드는 직접 쿼리를 작성해도 됨 (@Query)

3. Service코드 작성 (Controller/**Service**/Repository)
- `PostsService`에 코드추가 (findAllDesc()함수 정의)
~~~
    ...
    private final PostsRepository postsRepository;
    
    ...
    
    @Transactional(readOnly = true)
    public List<PostsListResponseDto> findAllDesc() {
        return postsRepository.findAllDesc().stream()
                .map(PostsListResponseDto::new)
                .collect(Collectors.toList());
    }
~~~
- @Transctional(readOnly = true)
  - 트랜잭션 범위는 유지하되, 조회 기능만 남겨두어 조회속도 개선에 도움이 됨 (등록, 수정, 삭제가 없으면 사용)
- .map(PostsListResponseDto::new)의 람다식
  - 원형: .map(posts->new PostsListResponseDto(posts))
  - postsRepository결과로 넘어온 Posts의 Stream을 map을 통해 PostsListResponseDto변환 -> List로 반환하는 메소드
  
- `PostsListResponseDto`클래스 생성하자
~~~
@Getter
public class PostsListResponseDto {
    private Long id;
    private String title;
    private String author;
    private LocalDateTime modifiedDate;

    public PostsListResponseDto(Posts entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.author = entity.getAuthor();
        this.modifiedDate = entity.getModifiedDate();
    }
}
~~~

4. Controller코드 작성 (**Controller**/Service/Repository)
- `IndexController`코드 수정하기
~~~
@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;
    
    @GetMapping("/")
    public String index(Model model) { //1
        model.addAttribute("posts", postsService.findAllDesc());
        return "index";
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    }
}
~~~
- Model
  - 서버 템플릿 엔진에서 사용할 수 이쓴 객체를 저장할 수 있음
  - 여기선, postsService.findAllDesc()로 가져온 결과를 posts로 index.mustache에 전달함
  

## 4.5 게시글 수정, 삭제 화면 만들기 (Update, Delete)
- 수정 api는 3.4에서 만들었었지? (`PostsApiController`에서 "/api/v1/posts/{id}")
### 게시글 수정 (Update)
1. 수정을 하는 UI를 만들자. `posts-update.mustache`생성
~~~html
{{>layout/header}}
<h1>게시글 등록</h1>

<div class="col-md-12">
    <div class="col-md-4">
        <form>
            <div class="form-group">
                <label for="id">글 번호</label>
                <input type="text" class="form-control" id="id" value="{{post.id}}" readonly>
            </div>
            <div class="form-group">
                <label for="title">제목</label>
                <input type="text" class="form-control" id="title" value="{{post.title}}">
            </div>
            <div class="form-group">
                <label for="author"> 작성자</label>
                <input type="text" class="form-control" id="author" value="{{post.author}}" readonly>
            </div>
            <div class="form-group">
                <label for="content"> 내용</label>
                <textarea class="form-control" id="content">{{post.content}}</textarea>
            </div>
        </form>
        <a href="/" role="button" class="btn btn-secondary">취소</a>
        <button type="button" class="btn btn-primary" id="btn-update">수정 완료</button>
    </div>
</div>

{{>layout/footer}}
~~~
- {{post.id}}
  - mustache는 객체의 필드 접근 시 dot으로 구분함
  
- readonly
  - input태그에 읽기 기능만 허용하는 속성
  - id와 author는 수정할 수 없음
  
2. btn-update버튼이 update기능을 호출할 수 있도록 `index.js`파일에 update function추가하기
~~~html
    update : function () {
        var data = {
            title: $('#title').val(),
            content: $('#content').val()
        };
        
        var id = $('#id').val();
        
        $.ajax({
            type: 'PUT',
            url: '/api/v1/posts/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function() {
            alert('글이 수정되었습니다.');
            window.location.href = '/';
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    }
~~~

3. 전체목록->수정페이지로 넘어갈 수 있도록 `index.mustache`의 UI변경하기
~~~html
{{#posts}}
    <tr>
        <td>{{id}}</td>
        <td><a href="/posts/update/{{id}}">{{title}}</a></td>
        <td>{{author}}</td>
        <td>{{modifiedDate}}</td>
    </tr>
{{/posts}}
~~~
- 타이틀에 a테그를 추가하여, 수정화면으로 이동할 수 있도록 함

4. 수정화면을 연결할 Controller코드를 작업하기
- `IndexController`수정하기


#### 게시글 삭제 (Delete)
1. 삭제는 본문을 확인한 후 해야하므로, 수정 UI에 버튼 추가한다 (`posts-update.mustache`)
~~~html
        ...
        <a href="/" role="button" class="btn btn-secondary">취소</a>
        <button type="button" class="btn btn-primary" id="btn-update">수정 완료</button>
        <button type="button" class="btn btn-danger" id="btn=delete">삭제</button>
    ...
~~~

2. 삭제 이벤트를 진행 할 JS코드 추가하기 (`index.js`)
~~~
    delete : function() {
        var id = $('#id').val();

        $.ajax({
            type: 'DELETE',
            url: '/api/v1/posts/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8'
        }).done(function() {
            alert('글이 삭제제되었습다.');
            window.location.href = '/';
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    }
~~~

3. 삭제 API만들기 (Service/Controller)
- `PostsService`코드 추가
~~~
  //PostsService
  ...
  @Transactional
  public void delete (Long id) {
      Posts posts = postsRepository.findById(id)
              .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없슴다. id=" + id));
      //JPA에서 제공하는 delete메소드를 그대로 이용하자
      postsRepository.delete(posts);
  }
~~~
- `PostsApiController`코드 추가
~~~
  //PostsApiController
  ...
  @DeleteMapping("/api/v1/posts/{id}")
  public Long delete(@PathVariable Long id) {
      postsService.delete(id);
      return id;
  }
~~~

