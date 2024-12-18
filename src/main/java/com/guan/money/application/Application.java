package com.guan.money.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.guan.money")
@SpringBootApplication
public class Application implements ApplicationRunner {
        
    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
    
}
