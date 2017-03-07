package com.jd.eptid.scheduler.server.dao;

import com.jd.eptid.scheduler.core.domain.job.JobStatus;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ClassDan on 2016/10/8.
 */
public class JobStatusTypeHandler implements TypeHandler<JobStatus> {
    @Override
    public void setParameter(PreparedStatement ps, int i, JobStatus status, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, status.getCode());
    }

    @Override
    public JobStatus getResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return JobStatus.getStatus(code);
    }

    @Override
    public JobStatus getResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return JobStatus.getStatus(code);
    }

    @Override
    public JobStatus getResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return JobStatus.getStatus(code);
    }
}
