package com.jd.eptid.scheduler.core.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by classdan on 16-10-10.
 */
public class GenericUtils {

    public static Class getGenericParameterClass(Object obj) {
        Type[] genericInterfaces = obj.getClass().getGenericInterfaces();
        Type gInterface = genericInterfaces[0];
        if (gInterface instanceof ParameterizedType) {
            Type actualType = ((ParameterizedType) gInterface).getActualTypeArguments()[0];
            return (Class) actualType;
        }
        return null;
    }

}
