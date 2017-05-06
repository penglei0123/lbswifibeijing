package com.genepoint.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;

/**
 * 测试使用token获取大厦列表
 */
public class TestForGetBuildingListByToken {
	public static void main(String args[]) {
		JSONObject data = new JSONObject();
		data.put("pageIndex", 1);
		data.put("pageSize", 20);
		data.put("token", "123");
		data.put("data", "123");
		String paramString = "token=7EC67802C8F14B8DA7FE6DD389FE9389-8FA2A792B6B6865FCC848D8CF7A150CB&data=" + data.toString();
		System.out.println(paramString);
		for (int i = 0; i < 1; i++) {
			new Thread() {
				public void run() {
					String result = sendPost("http://10.41.0.247:8080/GenepointPlatform/micoled/uploader", "");
					System.out.println("Thread-" + Thread.currentThread().getId() + ":" + result);
				}
			}.start();

		}
	}

	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			out = new PrintWriter(conn.getOutputStream());
			out.print(param);
			out.flush();
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
}
