package com.kob.matchingsystem.service.impl;


import com.kob.matchingsystem.service.MatchingService;
import com.kob.matchingsystem.service.impl.utils.MatchingPool;
import org.springframework.stereotype.Service;

@Service
public class MatchingServiceImpl implements MatchingService {

    public final static MatchingPool matchingpool = new MatchingPool();

    @Override
    public String addPlayer(Integer userId, Integer rating) {
        System.out.println("Adding player"+userId);
        matchingpool.addPlayer(userId, rating);
        return "added player successfully";
    }

    @Override
    public String removePlayer(Integer userId ) {
        System.out.println("Removing player"+userId);
        matchingpool.removePlayer(userId);
        return "removed player successfully";
    }
}
