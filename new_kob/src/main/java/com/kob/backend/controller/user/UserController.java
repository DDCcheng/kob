package com.kob.backend.controller.user;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserMapper userMapper;

    @RequestMapping("/user/all/")
    public List<User> getAll(){
     return userMapper.selectList(null);
    }

    @GetMapping("/user/{userId}/")
    public User getuser(@PathVariable int userId){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", userId);
        return userMapper.selectOne(queryWrapper);
    }


    @RequestMapping("/user/add/{userId}/{username}/{password}/")
    public String adduser(@PathVariable int userId, @PathVariable String username, @PathVariable String password){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePassword = passwordEncoder.encode(password);
        User user = new User(userId, username, encodePassword);
        userMapper.insert(user);
        return "ADD USER SUCCESS";
    }

    @RequestMapping("user/delete/{userId}")
    public String deleteuser(@PathVariable int userId){
        userMapper.deleteById(userId);
        return "DELETE USER SUCCESS";
    }
}
