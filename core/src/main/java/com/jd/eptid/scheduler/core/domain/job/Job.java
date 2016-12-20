package com.jd.eptid.scheduler.core.domain.job;

/**
 * Created by classdan on 16-9-7.
 */
public abstract class Job {
    private Long id;
    private String name;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        return name.equals(job.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
