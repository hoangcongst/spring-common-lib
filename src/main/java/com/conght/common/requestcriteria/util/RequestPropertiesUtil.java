package com.conght.common.requestcriteria.util;

import com.conght.common.CommonUtil;
import com.conght.common.database.interceptor.EntityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
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
            Method getFieldInEntity = mEntity.getClass().getMethod("get"
                    + fieldInEntity.getName().substring(0, 1).toUpperCase()
                    + fieldInEntity.getName().substring(1));
            Object fieldObjectInEntity = getFieldInEntity.invoke(mEntity);
            boolean isListId =  fieldObjectInEntity instanceof Collection;

            String suffixId = isListId ? "Ids" : "Id";
            String fieldName = fieldInEntity.getName().substring(0, fieldInEntity.getName().length() - 1) + suffixId;
            Constructor<?> constructor = null;
            if(!isListId)
                constructor = fieldInEntity.getType().getConstructor();
            else {
                ParameterizedType pt = (ParameterizedType) fieldInEntity.getGenericType();
                var classField = pt.getActualTypeArguments()[0];
                Class<?> cls = (Class<?>) classField;
                constructor = cls.getConstructor();
            }

            Object listObjectFromIds = getObjectFromFieldAndRequest(fieldName, constructor,
                    mRequest, isListId);
            setValueIntoEntity(fieldInEntity, listObjectFromIds, mEntity);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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
     * @param fieldName - The field of current entity
     * @param mRequest      - Request object
     * @return the object which is the value of entity, which contain the Id get from request
     */
    public static Object getObjectFromFieldAndRequest(String fieldName, Constructor<?> constructorField
            , Object mRequest, boolean isListId) {
        try {
            Method getterIdMethodFromRequest = mRequest.getClass().getMethod(
                    "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            Object idFromRequest = getterIdMethodFromRequest.invoke(mRequest);
            if (isListId) {
                List<Object> listObjectFromIds = new ArrayList<>();
                assert idFromRequest != null;
                ((Collection<?>) idFromRequest).forEach(id -> {
                    if (id != null) {
                        try {
                            listObjectFromIds.add(newInstanceFromReflect(constructorField, id, id.getClass()));
                        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException | NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return listObjectFromIds;
            }
            return newInstanceFromReflect(constructorField, idFromRequest,
                    mRequest.getClass().getDeclaredField(fieldName).getType());
        } catch (NoSuchMethodException | NoSuchFieldException ignored) {
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object newInstanceFromReflect(Constructor<?> constructor, Object idFromRequest, Class<?> idType) throws InvocationTargetException,
            IllegalAccessException, NoSuchMethodException, InstantiationException, NoSuchFieldException {
        // Create a new instance
        Object relationshipProperty = constructor.newInstance();
        Method setterNewObj;
        try {
            setterNewObj = relationshipProperty.getClass().getMethod("setId", CommonUtil.WRAPPER_TYPE_MAP.get(idType));
        } catch (NoSuchMethodException noSuchMethodException) {
            setterNewObj = relationshipProperty.getClass().getMethod("setId", idType);
        }
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
