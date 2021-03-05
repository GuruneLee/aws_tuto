# 07. AWS에 데이터베이스 환경을 만들어보자 - AWS RDS
## 07.0 Intro
- AWS에서 제공하는 관리형 서비스, RDS(Relational Database Service)를 사용할 거다!
    - 하드웨어 프로비저닝, 각종 설정, 패치 및 백업 등 운영작업이 자동화 되어있음
    - 유동적인 용량 변경도 가능!!
    
## 07.1 RDS 인스턴스 생성하기
1. RDS검색해서 대시보드 들어가 > 데이터베이스 생성 클릭
2. DB엔진 선택
    - 'MariaDB'로 고르자. 왜?
    - RDS에는 오라클, MSSQL, PostgreSQL등이 있음 그 중 MariaDB가 1)가격이 싸고 2)Amazon Aurora 교체 용이성 (성능이 좋다고) 3)MySQL을 기반으로 만들어짐
3. 설정 마저 하기

## 07.2 RDS 운영환경에 맞는 파라미터 설정하기
- RDS생성 후 필수 설정
    1. 타임존
    2. Character Set
    3. Max Connection
    
1. RDS대시보드 카테고리에서 '파라미터 그룹' > '파라미터 그룹 생성'
    - 세부정보 위쪽에 DB엔진을 선택하는 항복에서, 위에 생성한 MariaDB와 같은 버전으로 맞춰야 함
2. 생성된 파라미터 그룹 클릭 > 파라미터 편집
    - time_zone = Asia/Seoul
    - character_set_ = utf8mb4 (utf8은 이모지를 저장하지 못함)
    - collation_ = utf8mb4_general_ci
    - max_connections = 
    
3. 파라미터 그룹을 데이터베이스에 연결하기
    - 데이터베이스 카테고리 > 위에서 만든거 선택 > 수정
    - DB 파리미터 그룹 항목을 방금 만든 파라미터 그룹으로 교체
    - 반영시점 -> 즉시적용
    
## 07.3 내 PC에서 RDS에 접속해 보기
- RDS보안그룹에 본인 IP추가하기
    - 본인 IP랑, EC2 보안 그룹 ID를 넣어주자 
    
### DataBase 플러그인 설치하기
- MySQL의 GUI 클라이언트로는 Workbench, SQLyog, Sequel Pro, DataGrip등이 있다
- 근데 여기서는 인텔리제이 DB플러그인으로 가보즈아
1. RDS 정보 페이지에서 '엔드 포인트'를 확인하자
    - 엔드 포인트 = 접근 가능한 URL
2. 인텔리제이 database플러그인 설치
    - 'Database Navigator'설치 > 재시작
    - action검색으로 Database Browser검색 > 왼쪽 db browser사이드바에서 MySQL접속정보 열어보기
    - RDS정보 등록하기
    .  
    .
    .  
    - gist에서 rds접속이 안돼...

## 07.4 EC2에서 RDS로 접근 확인
1. ec2접속
2. MySQL CLI 설치. `sudo yum install mysql`
3. 터미널에 명령어 넣어보자
    - `mysql -u _계정_ -p -h _HOST주소_`
    - 비밀번호 ㄱㄱ
    
