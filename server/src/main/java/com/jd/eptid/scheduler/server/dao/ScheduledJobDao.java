package com.jd.eptid.scheduler.server.dao;


import com.jd.eptid.scheduler.core.domain.job.ScheduledJob;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by ClassDan on 2016/9/18.
 */
@Repository
public interface ScheduledJobDao {

    int count();

    List<ScheduledJob> getByPage(@Param("rowNo") int rowNo, @Param("pageSize") int pageSize);

    ScheduledJob findByJobId(long jobId);

    ScheduledJob findRunningJobByJobId(long jobId);

    int add(ScheduledJob scheduledJob);

    int start(long id);

    int end(@Param("id") long id, @Param("endStatus") int endStatus, @Param("totalTasks") int totalTasks, @Param("successTasks") int successTasks, @Param("failedTasks") int failedTasks);

}
