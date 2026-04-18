package com.smartnote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.smartnote.mapper")
@EnableScheduling // 启用定时任务
public class SmartNoteApplication {
    public static void main(String[] args) {
        System.setProperty("spring.beaninfo.ignore", "true");
        System.setProperty("spring.sql.init.mode", "never");
        SpringApplication.run(SmartNoteApplication.class, args);
    }
}
