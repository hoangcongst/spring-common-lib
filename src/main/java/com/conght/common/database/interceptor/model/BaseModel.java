package com.conght.common.database.interceptor.model;

import javax.persistence.Transient;

public class BaseModel {
    @Transient
    public boolean isLog = true;
    @Transient
    public boolean isCreate = true;
}
