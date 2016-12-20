package com.jd.eptid.scheduler.server.dao;


import com.jd.eptid.scheduler.core.domain.task.ScheduledTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by ClassDan on 2016/9/18.
 */
@Repository
public interface ScheduledTaskDao {

    int count(long jobScheduleId);

    List<ScheduledTask> findByJobId(@Param("jobScheduleId") long jobScheduleId, @Param("rowNo") int rowNo, @Param("pageSize") int pageSize);

    int add(ScheduledTask scheduledTask);

    int updateStatus(@Param("id") long id, @Param("oldStatus") int oldStatus, @Param("newStatus") int newStatus);

    int start(@Param("id") long id, @Param("clientIp") String clientIp);

    int success(long id);

    int fail(@Param("id") long id, @Param("errorMessage") String errorMessage);

}
