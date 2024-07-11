package com.kob.backend.service.user.account;

import java.util.Map;

public interface LoginService {
    Map<String, String> gettoken(String username, String password);
}
