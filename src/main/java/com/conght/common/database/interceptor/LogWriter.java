package com.conght.common.database.interceptor;

import com.conght.common.database.interceptor.model.BaseLogEntityChange;

public interface LogWriter {
    public void write(BaseLogEntityChange baseLogEntityChange);
}
