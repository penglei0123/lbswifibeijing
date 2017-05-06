package com.genepoint.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.json.JSONObject;

import com.genepoint.tool.Function;

public class Test {
	public static void main(String args[]) {
		// System.out.println(String.format("采集数:%-5d导出数:%-5d", 123, 123));
		// System.out.println(String.format("采集数:%-5d导出数:%-5d", 12, 12));
		// fun((Integer) 100);
		// testTreeMap();
		// testSort();
		// testArrayListDelete();
		// testJSON();
		System.out.println("hzy".equals(null));
	}

	public static String getToken(String username) {
		String uuid = UUID.randomUUID().toString().toUpperCase();
		String md5 = Function.Md5(username).toUpperCase();
		return uuid;
	}

	public static void fun(int i) {
		System.out.println("int");
	}

	public static void fun(Integer i) {
		System.out.println("Integer");
	}

	public static void fun(long i) {
		System.out.println("long");
	}

	public static void testTreeMap() {
		TreeMap<String, Integer> treeMap = new TreeMap<String, Integer>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		treeMap.put("a", 3);
		treeMap.put("c", 2);
		treeMap.put("b", 1);
		treeMap.put("d", 1);
		for (String str : treeMap.keySet()) {
			System.out.println(str + ":" + treeMap.get(str));
		}
	}

	public static void testSort() {
		List<MyEntry<Integer, Double>> list = new ArrayList<MyEntry<Integer, Double>>();
		list.add(new MyEntry<Integer, Double>(1, 0.01));
		list.add(new MyEntry<Integer, Double>(2, 0.05));
		list.add(new MyEntry<Integer, Double>(4, 0.03));
		list.add(new MyEntry<Integer, Double>(3, 0.06));
		Collections.sort(list, new Comparator<MyEntry<Integer, Double>>() {
			public int compare(MyEntry<Integer, Double> o1, MyEntry<Integer, Double> o2) {
				System.out.println("o2:" + o2.getValue() + ",o1:" + o1.getValue());
				if (o2.getValue() == o1.getValue()) {
					return 0;
				} else if (o2.getValue() > o1.getValue()) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		for (int i = 0; i < list.size(); i++) {
			MyEntry<Integer, Double> entry = list.get(i);
			System.out.println(entry.getKey() + "," + entry.getValue());
		}
	}

	public static void testArrayListDelete() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.remove(2);
		list.remove(2);
		System.out.println(list.toString());
	}

	public static void testJSON() {
		int i = 0;
		System.out.println((i++) % 10);
		System.out.println(i);
		JSONObject json = new JSONObject();
		json.put("imei", "352784040553721");
		json.put("imsi", "460011301650271");
		json.put("model", "GT-I9500");
		json.put("pid", "ICT");
		json.put("macs", "ec:26:ca:99:33:a6,-78,pda_bj014#00:26:7a:19:47:0f,-72,CMCC");
		System.out.println(json.toString());
		json = new JSONObject();
		json.put("x", "162.000000");
		json.put("y", "92.000000");
		json.put("prc", "8");
		json.put("pid", "ICT");
		json.put("flr", "F8");
		System.out.println(json.toString());
	}
}

final class MyEntry<K, V> implements Map.Entry<K, V> {
	private final K	key;
	private V		value;

	public MyEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		V old = this.value;
		this.value = value;
		return old;
	}
}
