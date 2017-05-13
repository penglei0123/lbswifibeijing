package com.genepoint.custom;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.genepoint.tool.Log;

/**
 * 系统全局参数配置类
 * 
 * @author jd
 *
 */
public class Configs {
	/**
	 * 是否是调试
	 */
	public static boolean DEBUG = true;

	public static String ENCODING = "utf-8";

	/**
	 * 后台访问权限控制
	 */
	// 访客
	public static String USER_LEVEL_GUEST = "guest";
	// 开发者
	public static String USER_LEVEL_DEVELOPER = "developer";
	// 管理员
	public static String USER_LEVEL_ADMIN = "admin";

	/**
	 * 
	 */
	public static String VIEW_BASE_PATH = "/WEB-INF/";

	/**
	 * 前端分页每页默认记录数
	 */
	public static int PAGE_SIZE = 10;
	public static int PAGE_SIZE_MAX = 100;
	public static int PAGE_SIZE_MIN = 1;
	// 分页导航栏显示格子数(该数应该为一个奇数)
	public static int PAGINATION_SIZE = 5;

	/**
	 * MD5+Salt的长度
	 */
	public static int SALT_LEN = 8;

	public static String PATH_MAP_FLOOR = "/data/images/jinyuanlbs/map/";

	public static String URL_MAP_FLOOR = "/images/jinyuanlbs/map/";

	public static String CLIENT_BROWSER = "browser";
	public static String CLIENT_ANDROID = "android";

	public static String REDIS_HOST = "127.0.0.1";
	public static int REDIS_PORT = 6379;
	public static int REDIS_DB_INDEX = 3;

	// 轨迹滑动窗大小
	public static int TRACK_WINDOW_SIZE = 5;
	// 划分地图网格大小（像素）
	public static int MAP_GRID_SIZE = 30;
	// 历史热力图TOP-K
	public static int HEATMAP_MAX_POSITION_COUNT = 2000;
	// 全局建筑编码
	public static String BUILDING = "";

	public static String BUILDING_NAME_CHINESE = "";
	// 楼层之间逗号隔开，支持连续楼层，如：B1-B2,F1-F4,F9
	public static String FLOOR_LIST = "";
	// 是否启用缓存（redis）
	public static boolean CACHE_ENABLE = true;
	// 缓存过期时间(单位：秒）
	public static int CACHE_TIMEOUT = 300;

	// 加载本地配置文件，如果有则覆盖对应配置,否则使用默认配置
	public static boolean LoadConfiguration(String fileCfg) {
		if (fileCfg == null || fileCfg.equals("") || !new File(fileCfg).exists())
			return false;
		try {
			Class<Configs> c = Configs.class;
			Field[] fields = c.getFields();
			Set<String> fieldSet = new HashSet<>();
			for (Field field : fields) {
				fieldSet.add(field.getName());
			}
			LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(fileCfg));
			String line = null;
			while ((line = lineNumberReader.readLine()) != null) {
				// 跳过注释和空行
				if (line == null || line.startsWith("#") || line.equals("")) {
					continue;
				}
				String[] cfgArr = line.split("=");
				String cfgName = cfgArr[0].trim();
				String cfgValue = cfgArr[1].trim();
				// 过滤不存在或错误的配置参数
				if (!fieldSet.contains(cfgName)) {
					continue;
				}
				String varType = c.getField(cfgName).getType().getName();
				if (varType.equals("int")) {
					c.getField(cfgName).setInt(c, Integer.parseInt(cfgValue));
				} else if (varType.equals("java.lang.Integer")) {
					c.getField(cfgName).set(c, new Integer(Integer.parseInt(cfgValue)));
				} else if (varType.equals("boolean")) {
					c.getField(cfgName).setBoolean(c, Boolean.parseBoolean(cfgValue));
				} else if (varType.equals("java.lang.Boolean")) {
					c.getField(cfgName).set(c, new Boolean(Boolean.parseBoolean(cfgValue)));
				} else if (varType.equals("double")) {
					c.getField(cfgName).setDouble(c, Double.parseDouble(cfgValue));
				} else if (varType.equals("java.lang.Double")) {
					c.getField(cfgName).set(c, new Double(Double.parseDouble(cfgValue)));
				} else if (varType.equals("java.lang.String")) {
					c.getField(cfgName).set(c, cfgValue);
				}
			}
			lineNumberReader.close();
		} catch (Exception e) {
			Log.trace(Configs.class, e);
			return false;
		}
		return true;

	}

	public static void main(String args[]) {
		Class c = Configs.class;
		try {
			c.getField("BUILDING_NAME_CHINESE").set(c, "上海庆科");
			System.out.println(c.getField("BUILDING_NAME_CHINESE").get(c));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
