package com.conght.common.database.interceptor.model;

import lombok.Data;

@Data
public class UserInfoLog {
    private long id;
    private String userName;

    public UserInfoLog(long id, String userName) {
        this.id = id;
        this.userName = userName;
    }
}
