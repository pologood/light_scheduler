package com.jd.eptid.scheduler.server.web.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jd.eptid.scheduler.server.web.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-7-7.
 */
@Controller
@RequestMapping("/sql")
public class SqlController {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private static String prefix = "eptid_lts_sql:";
    @Resource(name = "masterJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    private Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(30, TimeUnit.SECONDS).build();

    @RequestMapping(value = "/token/get", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> getToken(@RequestParam("u") String track) {
        if (StringUtils.isBlank(track)) {
            return Response.failureResponse("error.");
        }

        String token = UUID.randomUUID().toString();
        cache.put(prefix + track, token);
        return Response.successResponse(token);
    }

    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    @ResponseBody
    @Transactional
    public Response execute(@RequestParam("sql") String sql, @RequestParam("u") String track, @RequestParam("t") String token) {
        if (StringUtils.isBlank(track) || StringUtils.isBlank(token)) {
            return Response.failureResponse("error.");
        }

        String tokenExpect = cache.getIfPresent(prefix + track);
        if (!token.equals(tokenExpect)) {
            return Response.failureResponse("error.");
        }

        if (StringUtils.isBlank(sql)) {
            return Response.failureResponse("SQL is empty.");
        }

        sql = sql.trim();
        logger.info("Execute sql: [{}]...", sql);
        try {
            int count = jdbcTemplate.update(sql);
            logger.info("Execute sql: [{}] successful.", sql);
            return Response.successResponse(count);
        } catch (Exception e) {
            logger.error("Failed to execute sql: {}.", sql, e);
            return Response.failureResponse(e.getMessage());
        }
    }

}
