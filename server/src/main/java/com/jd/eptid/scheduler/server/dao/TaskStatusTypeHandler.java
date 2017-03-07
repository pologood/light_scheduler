package com.jd.eptid.scheduler.server.dao;

import com.jd.eptid.scheduler.core.domain.task.TaskStatus;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ClassDan on 2016/10/8.
 */
public class TaskStatusTypeHandler implements TypeHandler<TaskStatus> {
    @Override
    public void setParameter(PreparedStatement ps, int i, TaskStatus status, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, status.getCode());
    }

    @Override
    public TaskStatus getResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return TaskStatus.getStatus(code);
    }

    @Override
    public TaskStatus getResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return TaskStatus.getStatus(code);
    }

    @Override
    public TaskStatus getResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return TaskStatus.getStatus(code);
    }
}
