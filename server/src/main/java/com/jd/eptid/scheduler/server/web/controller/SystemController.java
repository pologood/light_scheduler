package com.jd.eptid.scheduler.server.web.controller;

import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.server.web.response.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by classdan on 17-2-10.
 */
@Controller
@RequestMapping("/system")
public class SystemController {

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public String configPage() {
        return "system/config";
    }

    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Response<Map<String, String>> list() {
        Map<String, String> configItems = Configuration.listAll();
        return Response.successResponse(configItems);
    }

}
