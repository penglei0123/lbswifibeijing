package com.genepoint.custom;

/**
 * 每个到达服务端的所有请求被称为Action<br>
 * 通过接口访问的请求类别通过常量方式在此类中定义
 *
 * @author jd
 *
 */
public class Action {
	public static final String ACTION_GET_HEATMAP_REALTIME = "get_heatmap_realtime";
	public static final String ACTION_GET_HEATMAP_HISTORY = "get_heatmap_history";

	public static final String ACTION_GET_TRACK_REALTIME = "get_track_realtime";

	public static final String ACTION_GET_ALL_ONLINE_USER = "get_all_online_user";
	// 获取所有离线用户MAC
	public static final String ACTION_GET_ALL_OFFLINE_USER = "get_all_offline_user";
	// 获取轨迹的分层列表
	public static final String ACTION_GET_TRACK_HISTORY = "get_track_history";
	// 获取某个楼层的轨迹内容
	public static final String ACTION_GET_TRACK_HISTORY_DETAIL = "get_track_history_detail";
	// 获取某个楼层人群的位置
	public static final String ACTION_GET_PERSON_REALTIME = "get_person_realtime";
	// 获取当前大厦客流数量
	public static final String ACTION_GET_REALTIME_CUSTOMER_SHOP = "get_realtime_customer_shop";
	// 获取人群轨迹趋势
	public static final String ACTION_GET_TRACK_TREND = "get_track_trend";
	// 获取热力图趋势
	public static final String ACTION_GET_HEATMAP_TREND = "get_heatmap_trend";
	// 获取商铺列表
	public static final String ACITON_GET_SHOP_LIST = "get_shop_list";
	// 获取商铺排行
	public static final String ACITON_GET_SHOP_RANK = "get_shop_rank";
	// 获取历史客流
	public static final String ACTION_GET_CUSTOMER_FLOW_HISTORY = "get_customer_flow_history";
	// 获得topK个MAC
	public static final String ACTION_GET_TOPK_MAC = "get_topk_mac";
	// 获取到店率
	public static final String ACTION_GET_RATE_IN_SHOP = "get_rate_in_shop";
	// 获取驻留时间统计
	public static final String ACTION_GET_STAYTIME_IN_SHOP = "get_staytime_in_shop";
	// 获取地图的路径数据和禁止区域数据
	public static final String ACTION_GET_MAP_PATH_FORBIDDEN_REGION = "get_map_path_forbidden_region";
	// 获取所有建筑
	public static final String ACTION_GET_ALL_BUILDING = "get_all_building";
	// 切换建筑
	public static final String ACTION_SWITCH_BUILDING = "switch_building";
	//获取ap的位置
	public static final String ACTION_GET_AP_POSITION = "get_ap_position";
	//单mac历史轨迹切换楼层
	public static final String ACTION_GET_HISTORY_PERSON_TRACK = "get_history_person_track";
}
