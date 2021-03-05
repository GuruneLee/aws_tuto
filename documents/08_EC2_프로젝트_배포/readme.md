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