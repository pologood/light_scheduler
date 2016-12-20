package com.jd.eptid.scheduler.server.web.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by classdan on 16-12-7.
 */
public class BaseController {
    protected static Logger logger = LoggerFactory.getLogger(BaseController.class);

    public void cleanCache(HttpServletResponse response) {
        response.setDateHeader("Expires", 0);
        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");
    }

    protected HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    protected HttpServletResponse getResponse() {
        return ((ServletWebRequest) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    protected void setRequestAttribute(String key, Object value) {
        this.getRequest().setAttribute(key, value);
    }

    protected Map<String, String> getParameterMap() {
        return getParameterMap(this.getRequest());
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getParameterMap(HttpServletRequest request) {
        Map<String, String> parameterMap = new HashMap();
        Set<String> keys = request.getParameterMap().keySet();
        for (String key : keys) {
            if (StringUtils.isEmpty(request.getParameter(key))) {
                continue;
            }
            parameterMap.put(key, request.getParameter(key));
        }
        return parameterMap;
    }

    protected boolean isAjaxRequest(HttpServletRequest request) {
        String requestType = request.getHeader("X-Requested-With");
        if (StringUtils.isNotBlank(requestType) && StringUtils.equals(requestType, "XMLHttpRequest")) {
            return true;
        }
        return false;
    }

    protected void flush(HttpServletResponse response, Object data) {
        try {
            BufferedOutputStream os = new BufferedOutputStream(response.getOutputStream());
            os.write(JSONObject.toJSONString(data).getBytes("UTF-8"));
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        logger.error(ex.getMessage(), ex);
        if (isAjaxRequest(request)) {
            JSONObject result = new JSONObject();
            result.put("success", false);
            result.put("message", ex.getMessage());
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-type", "application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            flush(response, result);
            return null;
        } else {
            ModelAndView exceptionView = new ModelAndView("error");
            exceptionView.addObject("message", ex.getMessage());
            return exceptionView;
        }
    }
}
