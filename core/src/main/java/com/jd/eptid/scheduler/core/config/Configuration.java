package com.jd.eptid.scheduler.core.config;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Created by classdan on 16-9-12.
 */
public class Configuration {
    private static Properties properties = new Properties();

    static {
        load();
    }

    private static void load() {
        URL url = Configuration.class.getResource("/");
        File classPathDir = new File(url.getPath());
        for (File file : classPathDir.listFiles()) {
            if (file.isFile()) {
                loadFile(file);
            }
        }
    }

    private static void loadFile(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read configuration file.", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key, null);
    }

    public static String get(String key, String defaultIfEmpty) {
        String result = null;
        try {
            result = get(key);
        } catch (Exception e) {
            // the invoker should be aware about non-existed config. not need to be logged
            // logger.error(e.getMessage(),e);
            result = defaultIfEmpty;
        }
        return org.apache.commons.lang3.StringUtils.isBlank(result) ? defaultIfEmpty : result;
    }

    public static Integer getInteger(String key) {
        return Integer.valueOf(get(key));
    }

    public static Integer getInteger(String key, Integer defaultIfNull) {
        String result = get(key, null);
        return NumberUtils.toInt(result, defaultIfNull);
    }

    public static Map<String, String> listAll() {
        return Maps.asMap(properties.stringPropertyNames(), new Function<String, String>() {
            @Override
            public String apply(String input) {
                return properties.getProperty(input);
            }
        });
    }

}
