package com.example.infra.mybatis.typehandler;

import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.orm.ibatis.support.AbstractLobTypeHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BlobInputStreamTypeHandler extends AbstractLobTypeHandler {

    @Override
    protected void setParameterInternal(PreparedStatement ps, int index, Object value, String jdbcType, LobCreator lobCreator) throws SQLException, IOException {
        lobCreator.setBlobAsBinaryStream(ps, index, (InputStream) value, Integer.MAX_VALUE);
    }

    @Override
    protected Object getResultInternal(ResultSet rs, int index, LobHandler lobHandler) throws SQLException, IOException {
        return lobHandler.getBlobAsBinaryStream(rs, index);
    }

    @Override
    public Object valueOf(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
