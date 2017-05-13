package com.genepoint.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tomcat.jdbc.pool.DataSource;

import com.genepoint.tool.Log;

/**
 * JDBC连接池
 * @author jd
 *
 */
public class DBUtil {
	private static DataSource pool;
	private Connection conn = null;
	private Statement stmt = null;
	private int count = 0;
    static {
         Context env = null;
          try {
              env = (Context) new InitialContext().lookup("java:comp/env");
              pool = (DataSource)env.lookup("jdbc/DBPool");
              if(pool==null)
                  System.err.println("'DBPool' is an unknown DataSource");
               } catch(NamingException ne) {
                  ne.printStackTrace();
          }
      }
    public static DataSource getPool() {
        return pool;
    }
    
	public static void destoryPool() {
		pool.close();
	}

    public static Connection getConnection() throws SQLException{
    	Connection con = null;
    	int retry = 3;
    	int count = 0;
    	con = pool.getConnection();
//    	con.setAutoCommit(true);// 我把这个改为true，好像没有那个问题了，你再看下，点击注册大厦，总是提示“系统出错”
    	return con;
    	/*
    	while(true){
    		con = pool.getConnection();
    		if(con==null){
    			try {
    				Thread.sleep(500);
    			} catch (InterruptedException e) {
    			}
        		count++;
        		if(count>=retry){
        			throw new SQLException("can not get connection");
        		}
    		}else{
    			break;
    		}
    	}
    	con.setAutoCommit(false);
    	return con;
    	*/
    }
    
    public static boolean release(ResultSet rs,PreparedStatement pstmt,Connection con){
    	boolean rsClosed = true;
    	boolean pstmtClosed = true;
    	boolean conClosed = true;
    	if(rs!=null){
            try{
                //关闭存储查询结果的ResultSet对象
            	if(!rs.isClosed()){
            		rs.close();
            	}
            	rs=null;
            }catch (Exception e) {
                Log.trace(DBUtil.class, "关闭ResultSet对象时出错", e);
                rsClosed = false;
            }finally{
            	rs = null;
            }
        }
        if(pstmt!=null){
            try{
                //关闭负责执行SQL命令的Statement对象
            	if(!pstmt.isClosed())
            		pstmt.close();
            	pstmt = null;
            }catch (Exception e) {
            	Log.trace(DBUtil.class, "关闭PreparedStatement对象时出错", e);
            	pstmtClosed = false;
            }finally{
            	pstmt = null;
            }
        }
        try{
        	if(con!=null && !con.isClosed()){
        		if(!con.getAutoCommit()){
            		con.setAutoCommit(true);
            	}
            	//将Connection连接对象还给数据库连接池
                con.close();
        	}
        	con = null;
        }catch (Exception e) {
        	Log.trace(DBUtil.class, "释放数据库连接时出错", e);
        	conClosed = false;
        }finally{
        	con = null;
        }
        
        return rsClosed && pstmtClosed && conClosed;
    }
    
    public DBUtil() throws SQLException{
    	connect();
    }
    
    public void connect() throws SQLException{
    	conn = DBUtil.getConnection();
    	if(conn!=null){
    		conn.setAutoCommit(false);
    		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
    	}else{
    		throw new SQLException("Can not get connection");
    	}
    }

	public int getCounts(String sql) throws SQLException {
		try {
			if (stmt == null) {
				connect();
			}
			ResultSet rs = stmt.executeQuery(sql);
			rs.last();
			count = rs.getRow();
			rs.close();
			return count;
		} catch (SQLException e) {
			throw e;
		}
	}
	
	public static int getCounts(ResultSet rs) throws SQLException {
		try {
			rs.last();
			int count = rs.getRow();
			return count;
		} catch (SQLException e) {
			throw e;
		}
	}

	public static int getCurrentConnectionNum() {
		if (pool != null)
			return pool.getNumActive();
		return 0;
	}
}