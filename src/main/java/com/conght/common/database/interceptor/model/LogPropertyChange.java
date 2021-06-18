package com.conght.common.database.interceptor.model;

import lombok.Data;

@Data
public class LogPropertyChange {
    private String property;
    private Object from;
    private Object to;

    public LogPropertyChange(String propertyName, Object from, Object to) {
        this.property = propertyName;
        this.from = from;
        this.to = to;
    }
}
