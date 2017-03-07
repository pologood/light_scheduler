package com.jd.eptid.scheduler.server.web.controller;

import com.google.common.collect.ImmutableMap;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.server.core.ClientManager;
import com.jd.eptid.scheduler.server.web.page.PageData;
import com.jd.eptid.scheduler.server.web.response.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

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

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public String detailPage(String ip, int port) {
        setRequestAttribute("ip", ip);
        setRequestAttribute("port", port);
        return "client/detail";
    }

    @RequestMapping(value = "/monitor", method = RequestMethod.GET)
    public String monitorPage() {
        return "client/monitor";
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

    @ResponseBody
    @RequestMapping(value = "/listByJob", method = RequestMethod.GET)
    public Response<Map<String, Set<Client>>> list() {
        Map<String, Set<Client>> allSchedulableClients = clientManager.getAllSchedulableClients();
        return Response.successResponse(allSchedulableClients);
    }

    @ResponseBody
    @RequestMapping(value = "/supportJobs", method = RequestMethod.GET)
    public Response<Map<String, List<String>>> getSupportJobs(String ip, int port) {
        Client client = clientManager.findClient(ip, port);
        if (client == null) {
            return Response.successResponse(Collections.<String, List<String>>emptyMap());
        }

        List<String> supportedJobs = new ArrayList<String>(client.getSupportJobs());
        List<String> schedulableJobs = clientManager.getSchedulableJobs(ip, port);
        Map<String, List<String>> response = ImmutableMap.of("supportedJobs", supportedJobs, "schedulableJobs", schedulableJobs);
        return Response.successResponse(response);
    }

    @ResponseBody
    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    public Response<Boolean> disable(String jobName, String ip, int port) {
        try {
            clientManager.removeSchedulableClient(jobName, ip, port);
            return Response.successResponse(true);
        } catch (Exception e) {
            logger.error("Failed to disable client[{}:{}] for job: {}.", ip, port, jobName);
            return Response.successResponse(false);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/enable", method = RequestMethod.POST)
    public Response<Boolean> enable(String jobName, String ip, int port) {
        try {
            clientManager.addSchedulableClient(jobName, ip, port);
            return Response.successResponse(true);
        } catch (Exception e) {
            logger.error("Failed to enable client[{}:{}] for job: {}.", ip, port, jobName);
            return Response.successResponse(false);
        }
    }

}
