package com.techtalk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.techtalk.mapper")
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableScheduling
public class TechTalkApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechTalkApplication.class, args);
        System.out.println("========================================");
        System.out.println("  TechTalk 技术社区启动成功！");
        System.out.println("  API 文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
