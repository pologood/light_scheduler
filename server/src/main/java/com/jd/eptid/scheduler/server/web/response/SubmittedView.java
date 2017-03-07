package com.jd.eptid.scheduler.server.web.response;

import java.util.Set;

/**
 * Created by classdan on 17-2-7.
 */
public class SubmittedView {
    private Long id;
    private String name;
    private int interval;
    private Set<String> scheduling;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public Set<String> getScheduling() {
        return scheduling;
    }

    public void setScheduling(Set<String> scheduling) {
        this.scheduling = scheduling;
    }
}
