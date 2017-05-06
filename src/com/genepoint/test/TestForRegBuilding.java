package com.genepoint.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * 测试并发注册大厦时后端代码是否正常工作
 */
public class TestForRegBuilding {
	public static void main(String args[]){
		String paramString = "action=register_building&data=%7B%22building_name%22%3A%22%E5%B9%B6%E5%8F%91%E6%B5%8B%E8%AF%95%22%2C%22building_code%22%3A%22B_0101_151521B4E65%22%2C%22building_code_auto%22%3A%22B_0101_151521B4E65%22%2C%22city_code%22%3A%220101%22%2C%22building_lat%22%3A%2239.908419482750716%22%2C%22building_lng%22%3A%22116.40941619873047%22%2C%22building_location%22%3A%22%E4%B8%AD%E5%9B%BD%E5%8C%97%E4%BA%AC%E5%B8%82%E4%B8%9C%E5%9F%8E%E5%8C%BA%E4%B8%9C%E9%95%BF%E5%AE%89%E8%A1%977%E5%8F%B7%22%7D";
		for(int i=0;i<100;i++){
			new Thread(){
				public void run(){
					String result = sendPost("http://localhost:8080/GenepointPlatform/developer/building_manage",paramString);
					System.out.println("Thread-"+Thread.currentThread().getId()+":"+result);
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
            conn.setRequestProperty("Cookie","JSESSIONID=F3F15B131DE96EDD6DFDCA11AA0344D5");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }
}
