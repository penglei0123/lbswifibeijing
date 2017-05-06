<%@ page language="java" import="java.util.*,com.genepoint.custom.*" pageEncoding="UTF-8"%>
<%@ page contentType="text/html;charset=utf-8"%>
<%
	String path = request.getContextPath();
	String user = (String)request.getAttribute("user");
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/"+user+"/";
	basePath = path + "/"+user+"/";			//因路由环路后加上，使用测试服务器跳转
	String title = (String)request.getSession().getAttribute("title");
	String username = (String)request.getSession().getAttribute("username");
	String webPage = (String)request.getAttribute("page");
	String buildingName = (String)request.getAttribute("buildingChineseName");
%>
<html lang="zh-CN" class="no-js">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
<% 
if(user.equals("wifibeijing")){
	out.println("<title>wifibeijing</title>");
}else{
	out.println("<title>子午快线</title>");
}
%>
  <meta name="description" content="<%=buildingName%>LBS">
  <meta name="keywords" content="<%=buildingName%>LBS">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
  <meta name="renderer" content="webkit">
  <meta http-equiv="Cache-Control" content="no-siteapp" />
  <link rel="icon" type="image/png" href="<%=basePath%>static/assets/i/favicon.png">
  <link rel="apple-touch-icon-precomposed" href="<%=basePath%>static/assets/i/app-icon72x72@2x.png">
  <meta name="apple-mobile-web-app-title" content="Amaze UI" />
  <link rel="stylesheet" type="text/css" href="<%=basePath%>static/assets/css/amazeui.min.css"/>
  <link rel="stylesheet" type="text/css" href="<%=basePath%>static/assets/css/admin.css">
  <script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=4fmvIljU94Sicv8ehPl08vy4BLRNpGWZ"></script>
  <script type="text/javascript" src="http://api.map.baidu.com/library/Heatmap/2.0/src/Heatmap_min.js"></script>
  <script type="text/javascript" src="http://api.map.baidu.com/library/TextIconOverlay/1.2/src/TextIconOverlay_min.js"></script>
   <script type="text/javascript" src="http://api.map.baidu.com/library/MarkerClusterer/1.2/src/MarkerClusterer_min.js"></script>
	<script>
	var basePath="<%=basePath%>";
	var page = "<%=webPage%>";
	var building = "<%=Configs.BUILDING%>";
	var floorConfig = "<%=Configs.FLOOR_LIST%>"; 
	</script>
</head>