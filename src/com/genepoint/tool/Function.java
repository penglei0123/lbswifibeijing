package com.genepoint.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.genepoint.custom.Configs;

public class Function {
	public static SimpleDateFormat dateFormat;

	static{
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public static void printMessage(String msg){
		if (Configs.DEBUG) {
			System.out.println(msg);
		}
	}

	public static String readJSONString(HttpServletRequest request) {
		StringBuffer sb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		return sb.toString();
	}

	public static void responseString(HttpServletResponse response,String info) {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.print(info);
			out.flush();
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 清除该目录下的所有文件，包括删除该文件夹
	 */
	public static void deleteThisFolder(String dir) {
		File dirFile = new File(dir);
		if(dirFile.exists()){
			File file[] = dirFile.listFiles();
			for (int j = 0; j < file.length; j++) {
				if (file[j].isDirectory()) {
					deleteThisFolder(file[j].getAbsolutePath());
				}
				// 删除包括文件夹
				File tmpFile = new File(file[j].getAbsolutePath());
				if (!tmpFile.delete()) {
//					System.out.println("delete failed! "
//							+ file[j].getAbsolutePath());
				}
			}
			Log.info(Function.class, "清理目录："+dirFile.getAbsolutePath());
			dirFile.delete();
		}
	}

	public static String generateToken(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static String Md5(String plainText) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			return buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 以当前毫秒时间戳的十六进制大写形式作为建筑编码
	 * @return
	 */
	public static String generateBuildingCode(){
		long time = System.currentTimeMillis();
		return Long.toHexString(time).toUpperCase();
	}

	public static String getRandomSalt() { //length表示生成字符串的长度
	    String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=~`,.<>/?;:'";
	    Random random = new Random(System.currentTimeMillis());
	    StringBuffer sb = new StringBuffer();
	    Set<Integer> set = new HashSet<Integer>();
	    while(true){
	    	int number = random.nextInt(base.length());
	    	if(!set.contains(number)){
	    		sb.append(base.charAt(number));
	    		set.add(number);
	    	}
	    	if(set.size()>=Configs.SALT_LEN){
	    		break;
	    	}
	    }
	    set = null;
	    return sb.toString();
	 }

	public static String parseURI(String uri) {
		if (uri == null || uri.equals(""))
			return "";
		int p = uri.lastIndexOf("/");
		return uri.substring(p + 1);
	}

	// 判断时间跨度为几天（确定查询的表名）
	public static List<String> parseTablenameList(Connection dbConn, String tablePrefix, long timeNow, long timeTail) throws SQLException {
		ResultSet rs = null;
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		List<String> tableList = new ArrayList<String>();
		Date date = new Date(timeNow);
		tableList.add(tablePrefix + sdf.format(date));
		// 利用Calender把当前时间回退到结束日期的零点
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		// 结束日期的零点时间戳
		long time = cal.getTimeInMillis();
		while (time > timeTail) {
			time = time - 1000 * 24 * 60 * 60;
			date = new Date(time);
			tableList.add(tablePrefix + sdf.format(date));
		}
		// 先筛选掉不存在的表
		StringBuffer sql = new StringBuffer("select table_name from table_list where table_name in (");
		for (String table : tableList) {
			sql.append("'" + table + "',");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(")");
		Statement stmt = dbConn.createStatement();
		rs = stmt.executeQuery(sql.toString());
		tableList.clear();
		while (rs.next()) {
			tableList.add(rs.getString(1));
		}
		rs.close();
		stmt.close();
		return tableList;
	}

	public static void main(String args[]){
		long start = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			getRandomSalt();
		}
		long end = System.currentTimeMillis();
		System.out.println(end-start);
	}
}
