<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp" %>
<%@ include file="/WEB-INF/public/top.jsp" %>
<style>
</style>
<div class="am-cf admin-main">
  <!-- content start -->
  <div class="admin-content" style="height:1000px;background-image:url(<%=basePath%>/static/images/bg.jpg) left top;">
    <div class="am-cf am-padding am-u-sm-12" style="overflow:auto;">
    	<p style="font-size:40px" align="center"><strong>会场客流统计</strong></p>
    	<div id="realtime_customer_count" style="float:left;width:40%;height:70%;"></div>
    	<div id="realtime_customer_line" style="float:left;width:60%;height:70%;"></div>
    	<div style="float:left;width:100%;height:100px;border-bottom:1px solid #ccc">&nbsp;</div>
    	<%@ include file="/WEB-INF/public/foot.jsp" %>
    </div>
    
  </div>
  <!-- content end -->
</div>
<script src="<%=basePath%>/static/script/common/charts.js"></script>
<script>
var page="home";
var chartHandle = null;
$(function(){
	$("#"+page).attr("class","current");
	showRealtimeCustomerCount();
	var requestData = {'building':building};
	getRealtimeCustomerShopData(requestData);
	setTimeout(showDynamicLine,1000);
});
</script>
