package com.jd.eptid.scheduler.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.eptid.scheduler.core.generic.SimpleParameterizedType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by classdan on 16-10-18.
 */
public class JsonUtils {

    public static Object parse(Object obj, Class clazz) {
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            List<Object> objects = new ArrayList<Object>();
            for (int i = 0; i < array.size(); ++i) {
                Object value = array.get(i);
                objects.add(parse(value, clazz));
            }
            return objects;
        } else if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            String jsonString = jsonObject.toJSONString();
            return JSON.parseObject(jsonString, clazz);
        } else {
            return obj;
        }
    }

    public static Object parse(String json, Type rawType, Type... actualTypeArguments) {
        SimpleParameterizedType parameterizedType = new SimpleParameterizedType(rawType, actualTypeArguments);
        return JSON.parseObject(json, parameterizedType);
    }

}
