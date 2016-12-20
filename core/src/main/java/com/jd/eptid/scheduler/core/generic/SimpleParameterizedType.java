package com.jd.eptid.scheduler.core.generic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by classdan on 16-10-18.
 */
public class SimpleParameterizedType implements ParameterizedType {
    private Type[] actualTypeArguments;
    private Type ownerType;
    private Type rawType;

    public SimpleParameterizedType(Type rawType, Type[] actualTypeArguments) {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    public void setActualTypeArguments(Type[] actualTypeArguments) {
        this.actualTypeArguments = actualTypeArguments;
    }

    public void setOwnerType(Type ownerType) {
        this.ownerType = ownerType;
    }

    public void setRawType(Type rawType) {
        this.rawType = rawType;
    }
}
