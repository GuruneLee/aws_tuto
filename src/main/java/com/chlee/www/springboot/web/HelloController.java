//간단한 API용 클래스
//단위 테스트 실습용임 ㅇㅇ
package com.chlee.www.springboot.web;

import com.chlee.www.springboot.web.dto.HelloResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//@RestController
//- 컨트롤러를 JSON을 반환하는 컨트롤러로 만들어준다
//- 예전에는 @ResponseBody를 각 메소드마다 선언했던 것을 한번에 사용할 수 있게 해준것
@RestController
public class HelloController {
    //@GetMapping
    //- HTTP Method인 Get의 요청을 받을 수 있는 API를 만들어줌
    //- 예전 @RequestMapping(method = RequestMethod.GET)으로 사용되었음
    //- /hello요청이 오면 문자열 hello를 반환해줌
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/hello/dto")
    //- /hello/dto에 get요청이 오면 아래 함수 출력함
    //name과 amount를 받아서 HelloResponseDto객체를 생성하는 함수
    public HelloResponseDto helloDto(@RequestParam("name") String name, //1
                                     @RequestParam("amount") int amount ) {
        return new HelloResponseDto(name, amount);
    }
}
