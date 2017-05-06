package com.genepoint.netlocate.servlet;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Getwifibeijingdata {
	static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static HashMap<String, Integer> phoneMacs = new HashMap<>();
	static HashMap<String, Integer> phoneMacnum = new HashMap<>();
	static Set<String> macs = new HashSet<>();

	public static void main(String[] args) throws Exception {
		// timepaixu("et/2016-08-08.dat", "et/F1.txt");// 排序
		// String path = "e:/2016-08-08.dat"
		String path = "e:/target.txt";
		getMACList(path);
		// getfloordata(path, "e:/target.txt", getAPs("E:/资料/中科劲点/wifibeijing/F1_F2.txt"));
	}

	/**
	 * @param path
	 *            一层的ap文件 每行为mac x y
	 * @return
	 * @throws Exception
	 */
	public static Set<String> getAPs(String path) throws Exception {
		Set<String> apSet = new HashSet<>();
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(path));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String arr[] = line.split("\t");
				apSet.add(arr[0].toLowerCase());
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(apSet.size());
		return apSet;

	}

	/**
	 * 在原始数据中过滤出某层的数据(标准格式)时间排序
	 * 
	 * @param path
	 *            日志文件路径
	 * @param newpath
	 *            过滤的当前层AP数据文件路径
	 * @param floormacs
	 *            当前层的mac Set
	 * @throws FileNotFoundException
	 */
	public static void getfloordata(String path, String newpath, Set<String> floormacs) throws FileNotFoundException {
		FileOutputStream outputStream = new FileOutputStream(newpath);
		ArrayList<Recorddata> datas = new ArrayList<>();
		try {
			File file = new File(path);
			int n = 0;
			if (file.isFile() && file.exists()) {
				FileInputStream fileInputStream = new FileInputStream(file);
				DataInputStream read = new DataInputStream(fileInputStream);
				BufferedReader br = new BufferedReader(new InputStreamReader(read, "UTF-8"));
				while (true) {
					String str = "";
					str = br.readLine();
					n++;
					if (str != null) {
						str = convertLog2data(str);
						if (str != null) {
							String[] arr = str.split("\t");
							if (floormacs.contains(arr[1]))
								datas.add(new Recorddata(Long.parseLong(arr[0]), arr[1], arr[2], Integer.parseInt(arr[3]), Integer.parseInt(arr[4])));
							// outputStream.write((str + "\n").getBytes("utf-8"));
						}
					} else {
						break;
					}
				}

				br.close();
				read.close();
				fileInputStream.close();
				Collections.sort(datas, new Comparator<Recorddata>() {
					@Override
					public int compare(Recorddata o1, Recorddata o2) {
						return (int) (o1.time - o2.time);
					}
				});
				for (Recorddata data : datas) {
					outputStream.write((data.time + "\t" + data.apMac + "\t" + data.phoneMac + "\t" + data.rssi + "\t" + data.channel + "\n")
							.getBytes());
				}
				outputStream.close();
			} else {
				System.out.println("file not exist");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 将原始数据去掉强度为0，转为每行 时间、apmac、phonemac rssi channel 并按时间排序
	 * 
	 * @param path
	 * @param newPath
	 */
	public static void timepaixu(String path, String newPath) {
		ArrayList<Recorddata> datas = new ArrayList<>();
		try {
			FileOutputStream outputStream = new FileOutputStream(newPath);
			LineNumberReader reader = new LineNumberReader(new FileReader(path));
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = convertLog2data(line);
				if (line != null) {
					String[] arr = line.split("\t");
					datas.add(new Recorddata(Long.parseLong(arr[0]), arr[1], arr[2], Integer.parseInt(arr[3]), Integer.parseInt(arr[4])));
				}
			}
			reader.close();
			Collections.sort(datas, new Comparator<Recorddata>() {
				@Override
				public int compare(Recorddata o1, Recorddata o2) {
					return (int) (o1.time - o2.time);
				}
			});
			for (Recorddata data : datas) {
				outputStream.write((data.time + "\t" + data.apMac + "\t" + data.phoneMac + "\t" + data.rssi + "\t" + data.channel + "\n").getBytes());
			}
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * log数据转为标准格式
	 * 
	 * @param logdata
	 * @return
	 */
	public static String convertLog2data(String logdata) {
		if (logdata != null) {
			try {
				List<String> list = new ArrayList<String>();
				for (int i = 0; i < logdata.length();) {
					if (logdata.charAt(i) == '"') {
						int start = i + 1;
						int end = i + 1;
						for (int j = i + 1; j < logdata.length(); j++) {
							if (logdata.charAt(j) == '"') {
								end = j;
								break;
							}
						}
						list.add(logdata.substring(start, end));
						i = end + 1;
					} else {
						i++;
					}
				}
				// 忽略信号强度为0的数据
				if (list.get(7).equals("0") || list.get(7).equals("")) {
					list.clear();
					// Log.error(this.getClass(), "data error:" + logdata);
					return null;
				}
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append(Long.parseLong(list.get(9)) * 1000L + "\t" + // 时间
						list.get(0) + "\t" + // apmac
						list.get(1) + "\t" + // phone mac
						list.get(7) + "\t" + // rssi
						list.get(3));// channel
				return stringBuffer.toString();
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * 合并1s的mac
	 * 
	 * @param filterFilePath
	 *            过滤的某个mac的数据
	 * @param anaDataFilePath
	 *            数据分析，每秒对应的ap数量
	 * @throws IOException
	 */
	public static void SecondAPhebing(String filterFilePath, String anaDataFilePath) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(anaDataFilePath);
		int shujunum = 0;
		float ver = 0;
		int sum = 0;
		long starttime = 0;
		long endtime = 0;
		long time = 0;

		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(filterFilePath));
			boolean firstline = true;
			long n = 0;
			Set<String> apmac = new HashSet<>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String arr[] = line.split("\t");
				time = Long.parseLong(arr[0]);
				if (firstline) {
					n = Long.parseLong(arr[0]);
					apmac.add(arr[1]);
					starttime = time;
					firstline = false;

				}
				if (time == n)
					apmac.add(arr[1]);
				else {
					shujunum += 1;
					sum = sum + apmac.size();
					System.out.println(time);
					outputStream.write((n + "\t" + apmac.size() + "\n").getBytes());
					n = time;
					apmac.clear();
					apmac.add(arr[1]);
				}

			}
			reader.close();
			if (apmac.size() > 0) {
				shujunum += 1;
				sum = sum + apmac.size();
				endtime = n;
				outputStream.write((n + "\t" + apmac.size() + "\n").getBytes());
			}
			ver = (float) sum / shujunum;
			System.out.println("文件:" + filterFilePath + "数据时间：" + (endtime - starttime) + "数据条数： " + shujunum + "平均值： " + ver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		outputStream.close();
	}

	public static List<Map.Entry<String, Integer>> getMACList(String path) throws Exception {
		LineNumberReader reader = new LineNumberReader(new FileReader(path));
		Map<String, Integer> macCountMap = new HashMap<String, Integer>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] arr = line.split("\t");
			macCountMap.put(arr[2], macCountMap.getOrDefault(arr[2], 0) + 1);
		}
		reader.close();
		System.out.println(macCountMap.size());
		List<Map.Entry<String, Integer>> list = new ArrayList<>();
		list.addAll(macCountMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o2.getValue() - o1.getValue();
			}

		});
		// for (int i = 0; i < 100; i++) {
		// System.out.println(list.get(i).getKey() + "\t" + list.get(i).getValue());
		// }
		return list;
	}


	public static JSONArray statistic(String mac, int startTime, int endTime, String path) throws Exception {
		LineNumberReader reader = new LineNumberReader(new FileReader(path));
		Map<String, List<Map.Entry<Integer, Integer>>> map = new HashMap<>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] arr = line.split("\t");
			int time = (int) (Long.parseLong(arr[0]) / 1000);
			if (arr[2].equals(mac) && time >= startTime && time <= endTime) {
				String apMAC = arr[1];
				int rssi = Integer.parseInt(arr[3]);
				if(map.containsKey(apMAC)){
					List<Map.Entry<Integer, Integer>> list = map.get(apMAC);
					Map<Integer, Integer> tmpMap = new HashMap<>();
					tmpMap.put(time, rssi);
					list.addAll(tmpMap.entrySet());
				}else{
					List<Map.Entry<Integer, Integer>> list = new ArrayList<>();
					Map<Integer, Integer> tmpMap = new HashMap<>();
					tmpMap.put(time, rssi);
					list.addAll(tmpMap.entrySet());
					map.put(apMAC, list);
				}
			}
		}
		reader.close();
		System.out.println(map.size());
		JSONArray jsonArr = new JSONArray();
		for(String ap:map.keySet()){
			List<Map.Entry<Integer, Integer>> list = map.get(ap);
			JSONObject json = new JSONObject();
			JSONArray apArr = new JSONArray();
			for(int i=0;i<list.size();i++){
				JSONObject tmpJSON = new JSONObject();
				tmpJSON.put("x", list.get(i).getKey());
				tmpJSON.put("y", list.get(i).getValue());
				apArr.put(tmpJSON);
			}
			json.put("name", ap);
			json.put("data", apArr);
			jsonArr.put(json);
		}
		return jsonArr;
	}
}

