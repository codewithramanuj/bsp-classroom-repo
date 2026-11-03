package com.bsp.springsecurityexample.api;

import com.bsp.springsecurityexample.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController {

    @Autowired
    TicketService service;


    @GetMapping
    public String sayHello(){
        return service.assignTicket();
    }

    @GetMapping("/admin")
    public String sayHelloAdmin(){
        return service.assignTicket();
    }

    @GetMapping("/user")
    public String sayHelloUser(){
        return "Hello User";
    }

}
