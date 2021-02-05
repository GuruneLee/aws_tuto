# 01. 인텔리제이로 스프링부트 시작하기
## 01.0 프로젝트 환경 점검
- Java 8 (jdk 1.8.x)
- Gradle 4.x
- Spring Boot 2.1.x

## 01.x 그레이들 버전 변경
1. `gradle>wrapper>gradle-wrapper-properties`에서 그레이들 버전 체크
   - 나는 첨에 6이었음
2. 4.x가 아니라면 변경해야 함
   - `alt + F12` (터미널 열기)
   - `gradlew wrapper --gradle-version 4.10.2` 명령어 입력
   - Build Success가 뜨면 성공, 다시 버전 확인해보기
- 여담  
Spring Boot 버전은 `build.gradle`에 명시되어 있으면 됨
  
## 01.4 그레이들 프로젝트를 스프링 부트 프로젝트로 변경하기
1. build.gradle 파일 변경
    - 처음엔 자바 개발에 가장 기초적인 설정만 존재
   ~~~
   plugins {
       id 'java'
   }

   group 'com.oops.www'
   version '1.0-SNAPSHOT'

   repositories {
       mavenCentral()
   }

   dependencies {
       testImplementation 'org.junit.jupiter:junit-jupiter-api:5.6.0'
       testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'
   }

   test {
       useJUnitPlatform()
   }
   ~~~
    1. 프로젝트의 플러그인 의존성 관리 설정
   ~~~
   //코드 최상단
   buildscript {
       ext { //build.gradle의 전역 변수 설정 키워드 `ext`
       springBootVersion = '2.1.7.RELEASE'
       }
       repositories {
       mavenCentral()
       jcenter()
       }
       dependencies {
       classpath("org.springframework.boot:sprint-boot-gradle-plugin:${springBootVersion}")
       }
   }
   ~~~
    2. 앞서 선언한 플로그인 의존성들을 적용할 것인지 설정
   ~~~
   apply plugin: 'java'
   apply plugin: 'eclipse'
   apply plugin: 'org.springframework.boot'
   apply plugin: 'io.spring.dependency-management' //스프링 부트의 의존성들을 관리해주는 '필수'플러그인
   ~~~
    3. 의존성(라이브러리)을 어떤 원격저장소에서 받을지 결정
   ~~~
   repositories {
       mavenCentral() //본인이 만든 라이브러리 업로드하기 너무 어려움
       jcenter() //mavenCentral의 문제점을 해결한 저장소
   }
   ~~~
    4. 프로젝트 개발에 필요한 의존성 선언
   ~~~
   dependencies {
       //특정 버전을 명시하지 않아야, 맨 위에 작성한 버전을 따라가게 됨
       compile('org.springframework.boot:spring-boot-starter-web')
       testCompile('org.springframework.boot:spring-boot-starter-test')
   }
   ~~~
    - **스프링 이니셜라이저**를 사용할 수 있지만, 배움의 기회를 잃어버릴 수 있음
2. 변경된 .gradle적용
    - load-gradle어쩌고를 클릭하면 됨
    - `BUILD SUCCESSFUL in 1m 20s`이라는 메시지뜨면 성공
    - 오른쪽 위의 `Gradle`을 클릭해서 의존성이 모두 받아졌는지 확인



## 01.5 인텔리제이에서 깃과 깃허브 사용하기
1. Action검색창을 열어(`ctrl+shift+A`), `share project on github`검색
2. github에 로그인 (or 토큰 생성)
3. repo만들기 - share클릭
4. .idea를 제외한 파일 커밋 (.gitignore에 추가하기) / 푸쉬
    - .gitignore작성
    1. .ignore플러그인 설치
    2. 다시시작 후 플러그인 적용
    3. 잘 안된거 같으면 그냥 만들자
    4. .idea, .gradle (자동생성파일)을 추가하기