package com.conght.common.requestcriteria.util;

import com.conght.common.database.interceptor.EntityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Role: used for null assignment in entity objects
 */
public class RequestPropertiesUtil {
    /**
     * Copy the attributes of the null value from the target entity class to the source entity class
     *
     * @param mRequest: The object to be overwritten by the null value in the attribute (source entity class)
     * @param mEntity:  The target object queried from the database based on id
     */
    public static void copyNonNullProperties(Object mRequest, Object mEntity) {
        BeanUtils.copyProperties(mRequest, mEntity, getNullProperties(mRequest));

        Class<?> classEntity = mEntity.getClass();
        for (Field fieldInEntity : classEntity.getDeclaredFields()) {
            if (EntityUtil.isRelationShip(fieldInEntity)) {
                handleRelationshipProperty(fieldInEntity, mRequest, mEntity);
            }
        }
    }

    private static void handleRelationshipProperty(Field fieldInEntity, Object mRequest, Object mEntity) {
        try {
            if (fieldInEntity.get(mEntity) instanceof Collection) {
                Object listIds = getObjectFromFieldAndRequest(fieldInEntity, mRequest);
                List<Object> listObjectFromIds = new ArrayList<>();
                assert listIds != null;
                ((Collection<?>) listIds).forEach(item -> {
                    Long itemId = getIdFromObjectIfExist();
                    if (itemId != null)
                        listObjectFromIds.add();
                });
                setValueIntoEntity(fieldInEntity, , mEntity);
            } else {
                Object objFromRequest = getObjectFromFieldAndRequest(fieldInEntity, mRequest);
                setValueIntoEntity(fieldInEntity, objFromRequest, mEntity);
            }
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void setValueIntoEntity(Field field, Object objFromRequest, Object mEntity) {
        if (objFromRequest != null) {
            try {
                Method setterMethod = mEntity.getClass().getDeclaredMethod("set"
                        + field.getName().substring(0, 1).toUpperCase()
                        + field.getName().substring(1), field.getType());
                setterMethod.invoke(mEntity, objFromRequest);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param fieldInEntity        - The field of current entity
     * @param mRequest - Request object
     * @return the object which is the value of entity, which contain the Id get from request
     */
    private static Object getObjectFromFieldAndRequest(Field fieldInEntity, Object mRequest) {
        try {
            Method getterIdMethodFromRequest = mRequest.getClass().getMethod("get"
                    + fieldInEntity.getName().substring(0, 1).toUpperCase()
                    + fieldInEntity.getName().substring(1) + "Id");
            Object idFromRequest = getterIdMethodFromRequest.invoke(mRequest);
            return newInstanceFromReflect(fieldInEntity, idFromRequest,
                    mRequest.getClass().getDeclaredField(fieldInEntity.getName() + "Id").getType());
        } catch (NoSuchMethodException | NoSuchFieldException ignored) {
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object newInstanceFromReflect(Field fieldInEntity, Object idFromRequest, Class<?> idType) throws InvocationTargetException,
            IllegalAccessException, NoSuchMethodException, InstantiationException, NoSuchFieldException {
        // Create a new instance
        Object relationshipProperty = fieldInEntity.getType().getConstructor(new Class[]{}).newInstance();
        Method setterNewObj = relationshipProperty.getClass().getMethod("setId", idType);
        // Invoke the setter for the name field with a value
        setterNewObj.invoke(relationshipProperty, idFromRequest);
        return relationshipProperty;
    }

    /**
     * Find out the empty properties and return them
     *
     * @param src
     * @return
     */
    private static String[] getNullProperties(Object src) {
        BeanWrapper srcBean = new BeanWrapperImpl(src);
        PropertyDescriptor[] pds = srcBean.getPropertyDescriptors();
        Set<String> emptyName = new HashSet<>();
        for (PropertyDescriptor p : pds) {
            Object srcValue = srcBean.getPropertyValue(p.getName());
            if (srcValue == null) emptyName.add(p.getName());
        }
        String[] result = new String[emptyName.size()];
        return emptyName.toArray(result);
    }
}
