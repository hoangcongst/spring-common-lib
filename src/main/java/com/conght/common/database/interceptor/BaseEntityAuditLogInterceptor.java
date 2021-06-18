package com.conght.common.database.interceptor;

import com.conght.common.CommonUtil;
import com.conght.common.database.interceptor.model.BaseLogEntityChange;
import com.conght.common.database.interceptor.model.LogPropertyChange;
import com.conght.common.database.interceptor.model.UserInfoLog;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class BaseEntityAuditLogInterceptor extends EmptyInterceptor {
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (this.isLog(entity)) {
            UserInfoLog mUserInfoLog = getUser();
            BaseLogEntityChange baseLogEntityChange = new BaseLogEntityChange(entity.getClass().getSimpleName(),
                    (Long) id, mUserInfoLog.getUserName(), mUserInfoLog.getId());
            baseLogEntityChange.setChanges(new ArrayList<>());
            for (int i = 0; i < propertyNames.length; i++) {
                Object previousVal = getValueFromObject(previousState[i]);
                Object currentVal = getValueFromObject(currentState[i]);
                if (!Objects.equals(previousVal, currentVal))
                    baseLogEntityChange.addChange(new LogPropertyChange(propertyNames[i],
                            previousVal, currentVal));
            }
            try {
                entity.getClass().getField("isCreate").set(entity, false);
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                exception.printStackTrace();
            }
            writeLog(baseLogEntityChange);
        }
        return true;
    }

    @Override
    public void postFlush(Iterator entities) {
        Object entity = entities.next();
        if (this.isLog(entity) && this.isCreate(entity)) {
            try {
                UserInfoLog mUserInfoLog = getUser();
                Method getIdMethod = entity.getClass().getMethod("getId");
                BaseLogEntityChange baseLogEntityChange = new BaseLogEntityChange(entity.getClass().getSimpleName(),
                        (Long) getIdMethod.invoke(entity), mUserInfoLog.getUserName(), mUserInfoLog.getId());

                HashMap<String, Object> changes = new HashMap<>();
                for (PropertyDescriptor propertyDescriptor :
                        Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors()) {
                    changes.put(propertyDescriptor.getName(), this.getValueFromObject(propertyDescriptor.getReadMethod().invoke(entity)));
                }

                baseLogEntityChange.addChange(new LogPropertyChange(entity.getClass().getSimpleName(), null, changes));
                writeLog(baseLogEntityChange);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | IntrospectionException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isCreate(Object entity) {
        try {
            Field field = entity.getClass().getField("isCreate");
            Object isLog = field.get(entity);
            return (Boolean) isLog;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return false;
    }

    private boolean isLog(Object entity) {
        try {
            Field field = entity.getClass().getField("isLog");
            Object isLog = field.get(entity);
            return (Boolean) isLog;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return false;
    }

    private Object getValueFromObject(Object objectState) {
        if (objectState == null) return null;
        else if (CommonUtil.isJavaDeclaredType(objectState.getClass())) {
            return objectState;
        } else if (objectState instanceof Collection) {
            List<Long> ids = new ArrayList<>();
            ((Collection<?>) objectState).forEach(item -> {
                Long itemId = getIdFromObjectIfExist(item);
                if(itemId != null)
                    ids.add(itemId);
            });
            return ids;
        } else {
            return getIdFromObjectIfExist(objectState);
        }
    }

    private Long getIdFromObjectIfExist(Object objectState) {
        try {
            Method getIdMethod = objectState.getClass().getMethod("getId");
            return (Long) getIdMethod.invoke(objectState);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

    public abstract UserInfoLog getUser();

    public abstract void writeLog(BaseLogEntityChange baseLogEntityChange);
}