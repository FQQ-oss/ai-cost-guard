package com.aicostguard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.aicostguard.module.*.mapper")
@EnableScheduling
public class AiCostGuardApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiCostGuardApplication.class, args);
    }
}
