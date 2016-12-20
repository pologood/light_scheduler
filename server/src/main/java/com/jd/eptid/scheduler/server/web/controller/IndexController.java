package com.jd.eptid.scheduler.server.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by classdan on 16-12-6.
 */
@Controller
public class IndexController extends BaseController {

    @RequestMapping(value = {"", "/", "/index"})
    public String index() {
        return "index";
    }

}
