package com.jd.eptid.scheduler.server.dao;

import com.jd.eptid.scheduler.core.domain.job.Status;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ClassDan on 2016/10/8.
 */
public class StatusTypeHandler implements TypeHandler<Status> {
    @Override
    public void setParameter(PreparedStatement ps, int i, Status status, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, status.getCode());
    }

    @Override
    public Status getResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return Status.getStatus(code);
    }

    @Override
    public Status getResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return Status.getStatus(code);
    }

    @Override
    public Status getResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return Status.getStatus(code);
    }
}
