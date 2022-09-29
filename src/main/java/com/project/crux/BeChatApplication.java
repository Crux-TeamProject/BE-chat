package com.project.crux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BeChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeChatApplication.class, args);
    }

}
