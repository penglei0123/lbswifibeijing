package com.genepoint.tool;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.genepoint.converter.PositionConverterFactory;

public class PositionConvertUtil {
	private static Map<String, PositionConverterFactory> convertMap = new HashMap<>();
	private static DocumentBuilderFactory factory = null;
	private static DocumentBuilder builder = null;

	public static boolean loadConverters(String cfgPath, String convertDir) throws Exception {
		if (factory == null) {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
		}
		File file = new File(cfgPath);
		if (!file.exists()) {
			throw new Exception("converter.xml not found");
		}
		URLClassLoader classLoader = null;
		try {
			Document doc = builder.parse(new File(cfgPath));
			NodeList convertList = doc.getElementsByTagName("converter");
			URL[] urls = new URL[convertList.getLength()];
			List<Converter> list = new ArrayList<>();
			for (int i = 0; i < convertList.getLength(); i++) {
				Element beacon = (Element) convertList.item(i);
				String building = beacon.getElementsByTagName("building").item(0).getTextContent();
				String classPath = beacon.getElementsByTagName("class").item(0).getTextContent();
				list.add(new Converter(building, classPath));
				urls[i] = new File(convertDir + "/" + building + ".jar").toURI().toURL();
			}
			classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
			// Thread.currentThread().getContextClassLoader();
			for (Converter converter : list) {
				Class<?> c = classLoader.loadClass(converter.classPath);
				Object o = c.newInstance();
				PositionConverterFactory factory = (PositionConverterFactory) o;
				convertMap.put(converter.building, factory);
			}
			return true;
		} catch (Exception e) {
			throw e;
		} finally {
			if (classLoader != null) {
				classLoader.close();
			}
		}
	}

	public static PositionConverterFactory getConverter(String building) {
		return convertMap.get(building);
	}

	public static double[] convert(String building, double x, double y) {
		PositionConverterFactory converter = PositionConvertUtil.getConverter(building);
		boolean needConvertPostion = (converter == null) ? false : true;
		double[] newPos = null;
		if (needConvertPostion) {
			newPos = converter.convert(x, y);
		} else {
			newPos = new double[2];
			newPos[0] = x;
			newPos[1] = y;
		}
		return newPos;
	}

	public static void main(String[] args) {
		try {
			loadConverters("WebRoot/WEB-INF/conf/converters.xml", "WebRoot/WEB-INF/converter");
			long start = System.currentTimeMillis();
			for (int i = 0; i < 1000000; i++) {
				double[] res = PositionConvertUtil.convert("113617", 10, 10);
			}
			long end = System.currentTimeMillis();
			System.out.println("finish," + (end - start));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Converter {
	public String building;
	public String classPath;

	public Converter(String building, String classPath) {
		this.building = building;
		this.classPath = classPath;
	}
}
