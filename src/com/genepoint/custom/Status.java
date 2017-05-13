package com.genepoint.custom;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统业务处理的状态信息
 *
 * @author jd
 *
 */
public class Status {
	public static int					STATUS_FAIL								= 0xffffffff;
	public static int					STATUS_SUCCESS							= 0x00000001;
	public static int					STATUS_REGISTER_CHECK_FAIL				= 0x00000002;
	public static int					STATUS_REGISTER_REPEAT					= 0x00000003;
	public static int					STATUS_REGISTER_SUCCESS					= 0x00000004;
	public static int					STATUS_REGISTER_UNKNOWN_ERROR			= 0x00000005;
	public static int					STATUS_SYSTEM_BUSY						= 0x00000006;
	public static int					STATUS_SYSTEM_ERROR						= 0x00000007;

	public static int					STATUS_LOGIN_SUCCESS					= 0x00000008;
	public static int					STATUS_LOGIN_INFO_ERROR					= 0x00000009;

	public static int					STATUS_GET_BUILDING_LIST_SUCCESS		= 0x0000000A;
	public static int					STATUS_GET_PROVINCE_LIST_SUCCESS		= 0x0000000B;
	public static int					STATUS_GET_CITY_LIST_SUCCESS			= 0x0000000C;
	public static int					STATUS_GET_BUILDING_LIST_FAIL			= 0x0000000D;
	public static int					STATUS_GET_PROVINCE_LIST_FAIL			= 0x0000000E;
	public static int					STATUS_GET_CITY_LIST_FAIL				= 0x0000000F;
	public static int					STATUS_GET_BUILDING_CODE_SUCCESS		= 0x00000010;
	public static int					STATUS_FORM_ERROR						= 0x00000011;
	public static int					STATUS_BUILDING_CODE_REPEAT				= 0x00000012;
	public static int					STATUS_REGISTER_BUILDING_SUCCESS		= 0x00000013;
	public static int					STATUS_DELETE_BUILDING_SUCCESS			= 0x00000014;
	public static int					STATUS_DELETE_BUILDING_FAIL				= 0x00000015;
	public static int					STATUS_GET_BUILDING_COUNT_SUCCESS		= 0x00000016;
	public static int					STATUS_GET_DEVELOPER_TOKEN_SUCCESS		= 0x00000017;

	public static int					STATUS_GET_HOME_BASE_INFO				= 0x00000018;

	public static int					STATUS_GET_FLOOR_LIST_SUCCESS			= 0x00000019;
	public static int					STATUS_GET_FLOOR_LIST_FAIL				= 0x0000001A;
	public static int					STATUS_FLOOR_EMPTY						= 0x0000001B;

	public static int					STATUS_ADD_FLOOR_SUCCESS				= 0x0000001C;
	public static int					STATUS_ADD_FLOOR_FAIL					= 0x0000001D;
	public static int					STATUS_BUILDING_FLOOR_ERROR				= 0x0000001E;

	public static int					STATUS_DELETE_FLOOR_SUCCESS				= 0x0000001F;
	public static int					STATUS_DELETE_FLOOR_FAIL				= 0x00000020;

	public static int					STATUS_EDIT_FLOOR_SUCCESS				= 0x00000021;
	public static int					STATUS_FLOOR_CODE_ERROR					= 0x00000022;
	public static int					STATUS_BUILDING_CODE_ERROR				= 0x00000023;

	public static int					STATUS_GET_ORIGINALDATA_SUCCESS			= 0x00000024;
	public static int					STATUS_GET_HANDLEDATA_SUCCESS			= 0x00000025;
	public static int					STATUS_ORIGINALDATA_EMPTY				= 0x00000026;
	public static int					STATUS_HANDLEDATA_EMPTY					= 0x00000027;

	public static int					STATUS_ALIYUN_OSS_ERROR					= 0x00000028;

	public static int					STATUS_UPLOAD_COLLECTED_DATA_SUCCESS	= 0x00000029;
	public static int					STATUS_DATA_HANDLE_ERROR				= 0x0000002A;

	public static int					STATUS_CLEAR_HISTORY_DATA_SUCCESS		= 0x0000002B;
	public static int					STATUS_CLEAR_HISTORY_DATA_FAIL			= 0x0000002C;

	public static int					STATUS_SEARCH_BUILDING_SUCCESS			= 0x0000002D;
	public static int					STATUS_SEARCH_BUILDING_EMPTY			= 0x0000002E;

	public static int					STATUS_USER_NOT_ACTIVATE				= 0x0000002F;
	public static int					STATUS_USER_ACTIVATE_SUCCESS			= 0x00000030;
	public static int					STATUS_USER_ACTIVATE_FAIL				= 0x00000031;
	public static int					STATUS_ACTIVATE_INFO_ERROR				= 0x00000032;
	public static int					STATUS_ACTIVATE_TIMEOUT					= 0x00000033;
	public static int					STATUS_BUILDING_LIST_EMPTY				= 0x00000034;
	public static int					STATUS_LOCATION_DATA_EMPTY				= 0x00000035;
	public static int					STATUS_QUERY_DATA_VERSION_SUCCESS		= 0x00000036;

	public static int					STATUS_ACCESS_FORBIDDEN					= 0x00000037;

	public static int					STATUS_MAP_TOO_LARGE					= 0x00000038;
	public static int					STATUS_MAP_TYPE_ERROR					= 0x00000039;

	public static int					STATUS_BUILDING_NAME_REPEAT				= 0x0000003A;

	public static int					STATUS_BUILDING_USER_NOT_MATCH			= 0x0000003B;

	public static int					STATUS_OLD_PASSWORD_ERROR				= 0x0000003C;
	public static int					STATUS_EMPTY							= 0x0000003D;
	public static int					STATUS_LOCATE_BUILDING_FAIL				= 0x0000003E;
	public static int					STATUS_LOCATE_CELL_FAIL					= 0x0000003F;
	public static int					STATUS_LOCATE_WIFI_FAIL					= 0x00000040;
	public static int					STATUS_UNKNOWN_ACTION					= 0xfffffffe;
	private static Map<Integer, String>	Message									= new HashMap<Integer, String>();

	static {
		Message.put(STATUS_FAIL, "fail");
		Message.put(STATUS_SUCCESS, "success");
		Message.put(STATUS_REGISTER_CHECK_FAIL, "注册信息未通过验证");
		Message.put(STATUS_REGISTER_REPEAT, "用户名或邮箱已被注册");
		Message.put(STATUS_REGISTER_SUCCESS, "注册成功");
		Message.put(STATUS_REGISTER_UNKNOWN_ERROR, "注册失败，未知错误");
		Message.put(STATUS_SYSTEM_BUSY, "系统繁忙");
		Message.put(STATUS_SYSTEM_ERROR, "系统出错");
		Message.put(STATUS_LOGIN_SUCCESS, "登录成功");
		Message.put(STATUS_LOGIN_INFO_ERROR, "登录信息不正确");
		Message.put(STATUS_GET_BUILDING_LIST_SUCCESS, "获取建筑列表成功");
		Message.put(STATUS_GET_PROVINCE_LIST_SUCCESS, "获取省份列表成功");
		Message.put(STATUS_GET_CITY_LIST_SUCCESS, "获取城市列表成功");
		Message.put(STATUS_GET_CITY_LIST_FAIL, "获取城市列表失败");
		Message.put(STATUS_GET_PROVINCE_LIST_FAIL, "获取省份列表失败");
		Message.put(STATUS_GET_BUILDING_LIST_FAIL, "获取建筑列表失败");
		Message.put(STATUS_GET_BUILDING_CODE_SUCCESS, "获取建筑编码成功");
		Message.put(STATUS_UNKNOWN_ACTION, "未知操作");
		Message.put(STATUS_FORM_ERROR, "参数错误");
		Message.put(STATUS_BUILDING_CODE_REPEAT, "大厦编码已被注册");
		Message.put(STATUS_BUILDING_NAME_REPEAT, "大厦名称已被注册");
		Message.put(STATUS_REGISTER_BUILDING_SUCCESS, "大厦注册成功");
		Message.put(STATUS_DELETE_BUILDING_SUCCESS, "大厦删除成功");
		Message.put(STATUS_DELETE_BUILDING_FAIL, "大厦删除失败");
		Message.put(STATUS_GET_BUILDING_COUNT_SUCCESS, "获取大厦数量成功");
		Message.put(STATUS_GET_HOME_BASE_INFO, "获取首页信息成功");
		Message.put(STATUS_GET_DEVELOPER_TOKEN_SUCCESS, "获取开发者TOKEN成功");
		Message.put(STATUS_GET_FLOOR_LIST_SUCCESS, "获取楼层列表成功");
		Message.put(STATUS_GET_FLOOR_LIST_FAIL, "获取楼层列表失败");
		Message.put(STATUS_ADD_FLOOR_SUCCESS, "添加楼层成功");
		Message.put(STATUS_ADD_FLOOR_FAIL, "添加楼层失败");
		Message.put(STATUS_DELETE_FLOOR_SUCCESS, "删除楼层成功");
		Message.put(STATUS_DELETE_FLOOR_FAIL, "删除楼层失败");
		Message.put(STATUS_BUILDING_FLOOR_ERROR, "大厦编码或楼层编码错误");
		Message.put(STATUS_FLOOR_EMPTY, "楼层为空");
		Message.put(STATUS_EDIT_FLOOR_SUCCESS, "楼层更新成功");
		Message.put(STATUS_FLOOR_CODE_ERROR, "楼层编码错误");
		Message.put(STATUS_BUILDING_CODE_ERROR, "建筑编码错误");
		Message.put(STATUS_GET_ORIGINALDATA_SUCCESS, "获取原始数据列表成功");
		Message.put(STATUS_GET_HANDLEDATA_SUCCESS, "获取离线数据列表成功");
		Message.put(STATUS_ORIGINALDATA_EMPTY, "矢量数据为空");
		Message.put(STATUS_HANDLEDATA_EMPTY, "离线数据为空");
		Message.put(STATUS_ALIYUN_OSS_ERROR, "阿里云OSS服务异常");
		Message.put(STATUS_UPLOAD_COLLECTED_DATA_SUCCESS, "离线数据处理成功");
		Message.put(STATUS_DATA_HANDLE_ERROR, "离线数据处理失败");
		Message.put(STATUS_CLEAR_HISTORY_DATA_SUCCESS, "清理历史数据成功");
		Message.put(STATUS_CLEAR_HISTORY_DATA_FAIL, "清理历史数据失败");
		Message.put(STATUS_SEARCH_BUILDING_EMPTY, "大厦搜索结果为空");
		Message.put(STATUS_SEARCH_BUILDING_SUCCESS, "大厦搜索成功");
		Message.put(STATUS_USER_NOT_ACTIVATE, "用户账户没有激活");
		Message.put(STATUS_USER_ACTIVATE_SUCCESS, "用户账户激活成功");
		Message.put(STATUS_USER_ACTIVATE_FAIL, "用户账户激活失败");
		Message.put(STATUS_ACTIVATE_INFO_ERROR, "用户激活信息不正确");
		Message.put(STATUS_ACTIVATE_TIMEOUT, "激活信息已过期");
		Message.put(STATUS_BUILDING_LIST_EMPTY, "建筑列表为空");
		Message.put(STATUS_LOCATION_DATA_EMPTY, "离线数据为空");
		Message.put(STATUS_QUERY_DATA_VERSION_SUCCESS, "查询离线数据版本成功");
		Message.put(STATUS_ACCESS_FORBIDDEN, "访问受限");
		Message.put(STATUS_MAP_TOO_LARGE, "地图文件大小超过限制");
		Message.put(STATUS_MAP_TYPE_ERROR, "地图文件格式错误");
		Message.put(STATUS_BUILDING_USER_NOT_MATCH, "大厦编码和用户不匹配");
		Message.put(STATUS_OLD_PASSWORD_ERROR, "原始密码错误");
		Message.put(STATUS_LOCATE_BUILDING_FAIL, "没有匹配任何大厦");
		Message.put(STATUS_LOCATE_CELL_FAIL, "基站定位失败");
		Message.put(STATUS_LOCATE_WIFI_FAIL, "WIFI定位失败");
		Message.put(STATUS_EMPTY, "没检索到相关数据");
	}

	public static String getMessage(int status) {
		return Message.get(status);
	}
}
