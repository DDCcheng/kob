package com.kob.matchingsystem;

import com.kob.matchingsystem.service.impl.utils.MatchingPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MatchingSystemApplication {
    public static void main(String[] args) {
        MatchingPool matchingPool = new MatchingPool();
        matchingPool.start();
        SpringApplication.run(MatchingSystemApplication.class, args);
        }
    }
