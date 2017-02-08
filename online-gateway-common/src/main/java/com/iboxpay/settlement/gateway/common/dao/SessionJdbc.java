package com.iboxpay.settlement.gateway.common.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionJdbc {

    private Session session;
    private Connection conn;
    private Statement stm;
    private ResultSet rs;
    private static final Logger logger = LoggerFactory.getLogger(SessionJdbc.class);

    /**
     * @param session
     */
    public SessionJdbc(Session session) {
        try {
            this.session = session;
            this.conn = this.session.connection();
        } catch (Exception e) {
            logger.info("SessionJdbc connection exception ", e);
        }
    }

    /*
     * 浣滆�:happyMan 鏃ユ湡:2014-1-20 鍔熻兘:
     * 
     * @param st
     * 
     * @param params
     * 
     * @throws SQLException
     */
    private void setParam(PreparedStatement st, Object... params) throws SQLException {
        if (params != null) {
            ParameterMetaData pmd = st.getParameterMetaData();
            for (int i = 0; i < params.length; i++) {
                if (params[i] != null) {
                    st.setObject(i + 1, params[i]);
                } else {
                    int sqlType = Types.VARCHAR;
                    sqlType = pmd.getParameterType(i + 1);
                    st.setNull(i + 1, sqlType);
                }
            }
        }
    }

    // 璋冪敤瀛樺偍杩囩▼
    /*
     * 浣滆�:happyMan 鏃ユ湡:2014-1-20 鍔熻兘:
     * 
     * @param domainId
     * 
     * @param typeId
     * 
     * @return
     */
    public synchronized ResultSet callStoreProc(String proName, int domainId, int typeId) {
        try {
            stm = conn.prepareCall("{call " + proName + "(?,?)}");
            ((CallableStatement) stm).setInt(1, typeId);
            ((CallableStatement) stm).setInt(2, domainId);
            rs = ((CallableStatement) stm).executeQuery();
        } catch (Exception e) {
            logger.info(" callStoreProc exception", e);
        }
        return rs;
    }

    public synchronized int[] batchUpdate(String sql, List<Object[]> params) {
        int[] flage = null;
        if (params == null || params.isEmpty()) {
            return flage;
        }
        StackTraceElement[] ste = new Throwable().getStackTrace();
        StringBuffer CallStack = new StringBuffer();
        for (int i = 0; i < ste.length; i++) {
            CallStack.append(ste[i].toString() + " | ");
            if (i > 1) break;
        }
        ste = null;
        try {
            this.conn.setAutoCommit(false);
            stm = this.conn.prepareStatement(sql);
            for (Object[] param : params) {
                batchSetParam((PreparedStatement) stm, param);
            }
            flage = ((PreparedStatement) stm).executeBatch();
            this.conn.commit();
        } catch (Exception e) {
            flage = null;

            throw new RuntimeException();
        }
        return flage;
    }

    /*
     * 浣滆�:happyMan
     * 鏃ユ湡:2014-1-20
     * 鍔熻兘:
     * @param st
     * @param param
     * @throws SQLException
     */
    private void batchSetParam(PreparedStatement st, Object... param) throws SQLException {
        setParam(st, param);
        st.addBatch();
    }

    /*
     * 浣滆�:happyMan 鏃ユ湡:2014-1-20 鍔熻兘:
     */
    public synchronized void closeCon() {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            logger.error(e.toString());
        }
        try {
            if (stm != null) stm.close();
        } catch (SQLException e) {
            logger.error(e.toString());
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.error(e.toString());
        }
    }

    public synchronized int updateDateBase(String sql) {
        int flage = 1;
        StackTraceElement[] ste = new Throwable().getStackTrace();
        StringBuffer CallStack = new StringBuffer();
        for (int i = 0; i < ste.length; i++) {
            CallStack.append(ste[i].toString() + " | ");
            if (i > 1) break;
        }
        ste = null;
        try {
            stm = this.conn.prepareStatement(sql);
            flage = ((PreparedStatement) stm).executeUpdate();
        } catch (Exception e) {
            flage = 0;
            logger.error(e.toString());
            throw new RuntimeException();
        }
        return flage;
    }

}