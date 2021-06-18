package com.conght.common.database.interceptor.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;

@Data
public class BaseLogEntityChange {
    private String entityName;
    private long entityId;
    private Date time;
    private String userName;
    private long userId;
    private Object changes;

    public BaseLogEntityChange(String entityName, long entityId, String userName, long userId) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.time = new Date();
        this.userName = userName;
        this.userId = userId;
    }

    public void addChange(LogPropertyChange change) {
        if (changes != null && changes.getClass() == ArrayList.class) {
            ArrayList<LogPropertyChange> mChanges = (ArrayList) changes;
            mChanges.add(change);
        } else
            changes = change;
    }
}
