package com.bsp.springsecurityexample.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController {



    @GetMapping
    public String sayHello(){
        return "Hello...";
    }

    @GetMapping("/admin")
    public String sayHelloAdmin(){
        return "Hello Admin";
    }

    @GetMapping("/user")
    public String sayHelloUser(){
        return "Hello User";
    }

}
