package com.bsp.supportassistantplsql.config;

import com.bsp.supportassistantplsql.service.TicketService;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

//    @Bean("dev")
//    DataSourceProperties setDataSourceProperties(){
//        DataSourceProperties dp = new DataSourceProperties();
//        dp.setPassword("");
//        dp.setUrl("");
//        dp.setUsername("");
//        return dp;
//    }
//
//    @Bean("qa")
//    DataSourceProperties setDataSourcePropertiesForQa(){
//        DataSourceProperties dp = new DataSourceProperties();
//        dp.setPassword("");
//        dp.setUrl("");
//        dp.setUsername("");
//        return dp;
//    }
//
//    @Bean("prod")
//    DataSourceProperties setDataSourcePropertiesForProd(){
//        DataSourceProperties dp = new DataSourceProperties();
//        dp.setPassword("");
//        dp.setUrl("");
//        dp.setUsername("");
//        return dp;
//    }

    @Bean("ticketService")

    public TicketService ticketService(){
        System.out.println("Manual bean");
        return new TicketService();
    }


}
