package com.jd.eptid.scheduler.server.dao;

import com.jd.eptid.scheduler.server.po.JobEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by classdan on 17-1-3.
 */
@Repository
public interface JobDao {

    void add(JobEntity job);

    int count();

    List<JobEntity> getAllJobs();

    List<JobEntity> getAvailableJobs();

    JobEntity getJobById(long id);

    int update(JobEntity job);

    int updateStatus(@Param("id") long id, @Param("yn") int yn);

    int delete(long id);

}
