package com.genepoint.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC数据访问工具类， 在不使用连接池时可使用该类访问数据库
 * 
 * @author jd
 *
 */
public class JDBC {
	String		driver	= "com.mysql.jdbc.Driver";
	String url = "jdbc:mysql://localhost:3306/huina_lbs?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true";
	String		user	= "root";
	String pwd = "zhongke";
	int			count;
	Connection	conn;
	Statement	state;
	ResultSet	rs;

	public JDBC() {
		try {
			Class.forName(driver);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}

	public void connect2() {
		try {
			conn = DriverManager.getConnection(url, user, pwd);
			state = conn.createStatement();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void connect() {
		try {
			conn = DriverManager.getConnection(url, user, pwd);
			conn.setAutoCommit(false);
			state = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public Connection getConnection() {
		return this.conn;
	}

	public void close() {
		try {
			if (state != null) {
				state.close();
				state = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public ResultSet executeQuery(String sql) {
		try {
			if (state == null) {
				connect();
			}
			rs = state.executeQuery(sql);
		} catch (Exception e) {
			System.out.println(e);
		}
		return rs;
	}

	public int getCounts(String sql) {
		try {
			if (state == null) {
				connect();
			}
			rs = state.executeQuery(sql);
			rs.last();
			count = rs.getRow();
		} catch (Exception e) {
			System.out.println(e);
		}
		return count;
	}

	public int executeUpdate(String sql) {
		int num = 0;
		try {
			if (state == null) {
				connect();
			}
			num = state.executeUpdate(sql);
		} catch (Exception e) {
			System.out.println(e);
		}
		return num;
	}

	public void rollback() throws SQLException {
		conn.rollback();
	}

	public void commit() throws SQLException {
		conn.commit();
	}

	public int[] executeBatch(String[] sql) {
		int[] result = null;
		try {
			Statement sm = conn.createStatement();
			for (int i = 0; i < sql.length; i++) {
				sm.addBatch(sql[i]);
			}
			result = sm.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean flag = false;

	public static void main(String args[]) throws SQLException, InterruptedException {
		new Thread() {
			public void run() {
				try {
					int i = 10;
					while (i-- > 0) {
						JDBC db = new JDBC();
						db.connect();
						ResultSet rs = db.executeQuery("select corx,cory from track_20160808 where floor='F1' limit 500000");
						while (rs.next()) {

						}
						rs.close();
						rs = null;
						db.close();
						db = null;
						System.out.println("query finish");
						Thread.currentThread();
						Thread.sleep(1000);
					}
					flag = true;
				} catch (Exception e) {

				}
			}
		}.start();
		while (true) {
			Thread.currentThread().sleep(1000);
			if (flag) {
				// System.gc();
				// System.out.println("gc call");
				// Thread.currentThread().sleep(5000);
			}
		}
	}
}