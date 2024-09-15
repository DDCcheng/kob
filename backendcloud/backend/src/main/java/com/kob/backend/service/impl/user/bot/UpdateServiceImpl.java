package com.kob.backend.service.impl.user.bot;

import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.bot.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UpdateServiceImpl implements UpdateService {

    @Autowired
    private BotMapper botMapper;


    @Override
    public Map<String, String> update(Map<String, String> data) {
        Map<String, String> map = new HashMap<>();
        UsernamePasswordAuthenticationToken token =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userinfo = (UserDetailsImpl) token.getPrincipal();
        User user=userinfo.getUser();

        int bot_id=Integer.parseInt(data.get("bot_id"));
        String title=data.get("title");
        String description=data.get("description");
        String content=data.get("content");
        if(title==null||title.isEmpty()) {
            map.put("error_message","标题不能为空");
            return map;
        }
        if(title.length()>100){
            map.put("error_message","标题长度不能大于100");
            return map;
        }
        if(content==null||content.isEmpty()) {
            map.put("error_message","内容不能为空");
            return map;
        }
        if(content.length()>10000){
            map.put("error_message","内容长度超出限制");
            return map;
        }
        if(description==null||description.isEmpty()) {
            map.put("error_message","这个用户很懒");
        }
        if(description.length()>300){
            map.put("error_message","简介长度超出限制");
            return map;
        }
        Bot previousBot=botMapper.selectById(bot_id);
        if(previousBot==null){
            map.put("error_message","该bot不存在");
            return map;
        }
        if (!previousBot.getUserId().equals(user.getId())) {
            map.put("error_message","没有权限修改当前bot");
            return map;
        }
        Bot newBot=new Bot(previousBot.getId(),
                user.getId(),
                title,
                description,
                content,
                previousBot.getCreatetime(),
                new Date());
        botMapper.updateById(newBot);
        map.put("error_message","SUCCESS");
        return map;
    }
}
