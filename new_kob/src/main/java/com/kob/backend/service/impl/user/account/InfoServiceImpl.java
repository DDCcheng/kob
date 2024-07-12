package com.kob.backend.service.impl.user.account;

import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.account.InfoService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InfoServiceImpl implements InfoService {
    @Override
    public Map<String, String> getInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, String> map = new HashMap<>();

        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken authenticationToken =
                    (UsernamePasswordAuthenticationToken) authentication;

            UserDetailsImpl userInfo = (UserDetailsImpl) authenticationToken.getPrincipal();
            User user = userInfo.getUser();
            map.put("error_message", "SUCCESS");
            map.put("userId", user.getId().toString());
            map.put("username", user.getUsername());
            map.put("photo", user.getPhoto());
        } else if (authentication instanceof AnonymousAuthenticationToken) {
            map.put("error_message", "USER_NOT_LOGGED_IN");
        } else {
            map.put("error_message", "UNKNOWN_AUTHENTICATION_TYPE");
        }

        return map;
    }
}
