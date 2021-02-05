// 앞으로 만들 프로젝트의 메인 클래스!!
package com.chlee.www.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
//스프링 부트의 자동 설정, 스프링 Bean읽기와 생성을 모두 자동으로 설정해줌
//이 위치부터 설정을 읽기 때문에, 이 클래스는 프로젝트의 최상단에 위치해야 함
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        //SpringApplication.run
        //내장 WAS를 실행함 (애플리케이션 실행시 내부에서 WAS실행)
        //항상 서버에 톰캣을 설치할 필요가 없고, 스프링부트로 만들어진 Jar파일로 실행하면 됨
        SpringApplication.run(Application.class, args);
    }
}
