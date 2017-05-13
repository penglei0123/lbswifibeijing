<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp" %>
<%@ include file="/WEB-INF/public/top.jsp" %>

<div class="am-cf admin-main">
<%@ include file="/WEB-INF/public/menu.jsp" %>
 <div class="admin-content" style="min-height:800px;height:800px;">
 <div class="am-cf am-padding">
 <div class="am-text-left" style="float:left">
 <span class="custom_duration">
 <input type="text" id="search_mac" placeholder="请输入MAC">
 </span>
 <button type="button" id="btnSearchUserTrack" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 查询</button>
 </div>
 </div>
 </div>
 <hr>
 <div class="am-cf am-padding" style="height:700px;overflow:auto">
 <div id="floormap" class="am-u-md-12" >
    		 <!--  <canvas id="canvas" >您的浏览器不支持canvas!</canvas>-->
	    </div>
 </div>
</div>
<%@ include file="/WEB-INF/public/foot.jsp" %>
<script>
page="realtimetrack"
</script>