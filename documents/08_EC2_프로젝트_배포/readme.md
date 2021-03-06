# 08. EC2 서버에 프로젝트를 배포해보자
## 08.1 EC2에 프로젝트 Clone받기
1. ec2에 깃 설치
    - `sudo yum install git`
    
2. 프로젝트 저장할 디렉토리 생성하기
    - `mkdir ~/app && mkdir ~/app/step1`
    - `cd ~/app/step1`
    
3. 프로젝트 클론받기
    - `git clone _gitURL_`
    
4. 테스트 돌려서 제대로 돌아가는지 확인하기
    - `./gradlew test`
    - 아마 8개 실패할 거임
    - permission denied뜨면 `chmod +x ./gradlew`하셈
    
## 08.2 배포 스크립트 만들기
- 배포 : 작성한 코드를 실제 서버에 반영하는 것
   - 다음 과정을 포괄한다
   1. git clone, git pull을 통해 새 버전의 프로젝트 받음
   2. Gradle이나 Maven을 통한 프로젝트 테스트와 빌드
   3. EC2 서버에서 해당 프로젝트 실행 및 재실행
   
- 위 과정을 쉘 스크립트로 작성해보자
1. `deploy.sh`파일 생성하기
   - `vim ~/app/step1/deploy.sh`
   ~~~bash
   #!/bin/bash
   
   REPOSITORY=/home/ec2-user/app/step1
   PROJECT_NAME=aws_tuto
   
   cd $REPOSITORY/$PROJECT_NAME/
   
   echo "> Git Pull"
   
   git pull
   
   echo "> 프로젝트 Build 시작"
   
   ./gradlew build --exclude-task test
   
   echo "> step1 디렉토리로 이동"
   
   cd $REPOSITORY
   
   echo "> Build 파일 복사"
   
   cp $REPOSITORY/$PROJECT_NAME/build/libs/*.jar $REPOSITORY/
   
   echo "> 현재 구동중인 애플리케이션 pid 확인"
   
   CURRENT_PID=$(pgrep -f ${PROJECT_NAME}.*.jar)
   
   echo "현재 구동 중인 애플리케이션 pid: $CURRENT_PID"
   
   if [ -z "$CURRENT_PID" ]; then
      echo "> 현재 구동 중인 애플리케이션이 없으므로 종료하지 않습니다."
   else
      echo "> kill -15 $CURRENT_PID"
      kill -15 $CURRENT_PID
      sleep 5
   fi
   
   echo "> 새 애플리케이션 배포"
   
   JAR_NAME=$(ls -tr $REPOSITORY/ | grep jar | tail -n 1)
   
   echo "> JAR NAME: $JAR_NAME"
   
   nohup java -jar $REPOSITORY/$JAR_NAME 2>&1 &
   ~~~
   - nohup : 터미널 종료해도 실행됨
     
   
2. `nohup.out`파일로그 확인해보기
- **Consider revisiting the entries above or defining a bean of type 'org.springframework.security.oauth2.client.registration.ClientRegistrationRepository' in your configuration.*이라는 에러로그가 뜬다!!!
- 에라이 `application-oauth.properties` .gitignore에 추가한거 기억 안남??!??!?!???

## 08.3 외부 Security 파일 등록하기
- .gitignore에 추가했던 application-oauth.properties를 서버에서 직접 작성해주자!!
1. `~/app`에 `application-oauth.properties`추가하기
~~~
...원래 로컬에 있던 내용 그대로...
~~~
2. application_oauth.properties를 쓰도록 deploy.sh파일을 수정
~~~bash
...
nohup java -jar \
  -Dspring.config.location=classpath:/application.properties, /home/ec2-user/app/application-oath.properties \
  $REPOSITORY/$JAR_NAME 2>&1 &
~~~
- 코드설명
   1. -Dspring.config.location
      - 스프링 설정 파일 위치를 지정함
      - application.properties와 application-oauth.properties의 위치를 지정함
      - classpath가 붙으면 안에 있는 resources디렉토리를 기준으로 경로가 생성됨
      - oauth파일은 외부에 위치해있으므로 절대경로를 사용함
    
## 08.4 스프링 부트 프로젝트로 RDS 접근하기
- RDS는 MariaDB를 사용하므로 스프링부트 프로젝트를 MariaDB로 실행하기 위해서 할 작업이 있음
1. 테이블 생성
    - H2에서 자동 생성해주던 테이블들을 MariaDB에선 직접 퀴리를 이용해 생성해야 함
2. 프로젝트 설정
    - 자바 프로젝트가 MariaDB에 접근 하려면 DB드라이버가 필수. 이를 추가해주자
3. EC2 설정
    - DB의 접속정보는 중요하게 보호해야 할 정보. 따라서 이를 EC2서버 내부에서 관리하도록 하자
    
### RDS 테이블 생성
- JPA가 사용될 엔티티 테이블과 스프링 세션이 사용될 테이블 2가지 종류의 테이블 생성
- JPA가 사용할 테이블은 **테스트 코드 수행 시 로그로 생성되는 쿼리를 사용하면 됨**
~~~
create table posts (
    id bigint not null auto_increment, 
    created_date datetime, 
    modified_date datetime,
    author varchar(255),
    content TEXT not null,
    title varchar(500) not null,
    primary key (id)
) engine=InnoDB;
~~~

~~~
create table user (
    id bigint not null auto_increment, 
    created_date datetime, 
    modified_date datetime,
    email varchar(255) not null,
    name varchar(255) not null,
    picture varchar(255),
    role varchar(255) not null,
    primary key (id)
) engine=InnoDB;
~~~

- 스프링 세션 테이블은 다음 쿼리를 이용하자
~~~
CREATE TABLE SPRING_SESSION (
	PRIMARY_ID CHAR(36) NOT NULL,
	SESSION_ID CHAR(36) NOT NULL,
	CREATION_TIME BIGINT NOT NULL,
	LAST_ACCESS_TIME BIGINT NOT NULL,
	MAX_INACTIVE_INTERVAL INT NOT NULL,
	EXPIRY_TIME BIGINT NOT NULL,
	PRINCIPAL_NAME VARCHAR(100),
	CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
	SESSION_PRIMARY_ID CHAR(36) NOT NULL,
	ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
	ATTRIBUTE_BYTES BLOB NOT NULL,
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;
~~~

### 프로젝트 설정
1. MariaDB 드라이버를 build.gradle에 추가하기
~~~
compile("org.mariadb.jdbc:mariadb-java-client")
~~~
2. 서버에서 구동될 환경 구성
- `src/main/resources`에 `application-real.properties`추가하기
~~~
spring.profiles.include=oauth,real-db
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
spring.session.store-type=jdbc
~~~

### EC2 설정
- RDS 접속 정보는 중요하니, EC2서버에 직접 설정파일을 둔다
1. `~/app`에 `application-real-db.properties`생성
~~~
spring.jpa.hibernate.ddl-auto=none
spring.datasource.url=jdbc:mariadb://freelec-springboot2-webservice.cmys0ityt4qt.ap-northeast-2.rds.amazonaws.com:3306/freelec-springboot2-webservice
spring.datasource.username=db계정
spring.datasource.password=db계정 비밀번호
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
~~~
2. `deploy.sh`가 real profile을 쓸 수 있도록 하자
~~~
...
nohup java -jar \
-Dspring.config.location=classpathL/application.properties,/home/ec2-user/app/application-oauth.properties,/home/ec2-user/app/application-real-db.properties \
-Dspring.profiles.active=real \
$REPOSITORY/$JAR_NAME 2>&1 &
~~~
- -Dspring.profiles.active=real
    - application-real.properties를 활성화시킴
    - 요 안에 spring.profiles.include=oauth,real-db옵션 때문에 real-db역시 함께 활성화대상에 포함됨