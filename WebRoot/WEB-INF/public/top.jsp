<%@page import="org.omg.PortableInterceptor.USER_EXCEPTION"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<body>
<!--[if lte IE 9]>
<p class="browsehappy">你正在使用<strong>过时</strong>的浏览器，Amaze UI 暂不支持。 请 <a href="http://browsehappy.com/" target="_blank">升级浏览器</a>
  以获得更好的体验！</p>
<![endif]-->

<header class="am-topbar admin-header">
  <div class="am-topbar-brand">
<% 
  		if(user.equals("wifibeijing")){
%>
	<img src="<%=basePath%>static/images/wifibeijing-logo.png" class="am-margin-top-xs"/>
<%
  		}else{

%>
<strong>子午快线</strong> <small>位置可视化</small>
<%
  		}
%>
&nbsp;&nbsp;&nbsp;&nbsp;<span>当前场景：</span><strong><%=buildingAlias%></strong>
  </div>

  <button class="am-topbar-btn am-topbar-toggle am-btn am-btn-sm am-btn-success am-show-sm-only" data-am-collapse="{target: '#topbar-collapse'}"><span class="am-sr-only">导航切换</span> <span class="am-icon-bars"></span></button>

  <div class="am-collapse am-topbar-collapse" id="topbar-collapse">

    <ul class="am-nav am-nav-pills am-topbar-nav am-topbar-right admin-header-list">
      <li><a href="<%=basePath%>buildings"><span class="am-icon-refresh"></span> <span>切换商场</span></a></li>
      <li><a href="javascript:;" id="admin-fullscreen"><span class="am-icon-arrows-alt"></span> <span class="admin-fullText">开启全屏</span></a></li>
    </ul>
  </div>
</header>