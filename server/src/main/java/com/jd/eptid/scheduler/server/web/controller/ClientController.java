package com.jd.eptid.scheduler.server.web.controller;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.server.core.ClientManager;
import com.jd.eptid.scheduler.server.web.page.PageData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * Created by classdan on 16-12-7.
 */
@Controller
@RequestMapping("/client")
public class ClientController extends BaseController {
    @Resource
    private ClientManager clientManager;

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public String managementPage() {
        return "client/manage";
    }

    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public PageData<List<Client>> list(@RequestParam("draw") int pageNo, @RequestParam("length") int pageSize) {
        int total = clientManager.count();
        List<Client> clients = null;
        if (total > 0) {
            clients = clientManager.getAllClients();
        } else {
            clients = Collections.emptyList();
        }
        return PageData.build(pageNo, total, clients);
    }

}
