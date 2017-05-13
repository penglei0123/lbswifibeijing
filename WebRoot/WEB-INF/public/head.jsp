<%@ page language="java" import="java.util.*,com.genepoint.custom.*" pageEncoding="UTF-8"%>
<%@ page contentType="text/html;charset=utf-8"%>
<%
	String path = request.getContextPath();
	String user = (String)request.getAttribute("user");
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/"+user+"/";
	if(user==null || "".equals(user)){
		basePath = path + "/";
	}else{
		basePath = path + "/"+user+"/";
	}
	String title = (String)request.getSession().getAttribute("title");
	String username = (String)request.getSession().getAttribute("username");
	String webPage = (String)request.getAttribute("page");
	String building = Global.buildingCode;
	String buildingAlias = Global.buildingName;
	if(building==null){
		buildingAlias="N/A";
	}
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
  <meta name="description" content="LBS">
  <meta name="keywords" content="LBS">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
  <meta name="renderer" content="webkit">
  <meta http-equiv="Cache-Control" content="no-siteapp" />
  <link rel="icon" type="image/png" href="<%=basePath%>static/assets/i/favicon.png">
  <link rel="apple-touch-icon-precomposed" href="<%=basePath%>static/assets/i/app-icon72x72@2x.png">
  <meta name="apple-mobile-web-app-title" content="Amaze UI" />
  <link rel="stylesheet" type="text/css" href="<%=basePath%>static/assets/css/amazeui.min.css"/>
  <link rel="stylesheet" type="text/css" href="<%=basePath%>static/assets/css/admin.css">
  <!--  
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
  <link rel="stylesheet" type="text/css" href="<%=basePath%>static/assets/css/progress.css">
  -->
	<script>
	var basePath="<%=basePath%>";
	var page = "<%=webPage%>";
	var building = "<%=building%>";
	var floorConfig = "<%=Global.floorList%>"; 
	
	</script>
</head>