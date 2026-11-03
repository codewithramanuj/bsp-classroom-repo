package com.bsp.springsecurityexample.config;

import com.bsp.springsecurityexample.service.TicketService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Scanner;

@Configuration
public class AppConfig {
//    @Bean
//    TicketService getTicketService(){
//        return new TicketService();
//    }

    @Bean
    Scanner getScanner(){
        return new Scanner(System.in);
    }

}
