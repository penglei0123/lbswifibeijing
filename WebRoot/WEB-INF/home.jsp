<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp"%>
<%@ include file="/WEB-INF/public/top.jsp"%>

<div class="am-cf admin-main">
	<%@ include file="/WEB-INF/public/menu.jsp"%>

	<!-- content start -->
	<div class="admin-content" style="min-height: 800px; height: 800px;">
		<div class="am-cf am-padding am-u-sm-12"
			style="height: 800px; overflow: auto">
			<div id="realtime_customer_count"
				style="float: left; width: 40%; height: 500px;"></div>
			<div id="oneday_flow_line"
				style="float: left; width: 60%; height: 500px;"></div>

			<div class="am-panel am-panel-default">
				<div class="am-panel-hd am-cf"
					data-am-collapse="{target: '#collapse-panel-heatmap'}">
					人流量统计（周）<span class="am-icon-chevron-down am-fr"></span>
				</div>
				<div id="histogram"
					style="overflow: auto; width: 90%; height: 600px; background: none;">
				</div>
			</div>
		</div>
	</div>
	<!-- content end -->
</div>
<%@ include file="/WEB-INF/public/foot.jsp"%>
<script src="<%=basePath%>/static/script/common/charts.js"></script>
<script>
	var page = "home";

	var chartHandle = null;
	var oneDayFlowHandle = null;
	$(function() {
		$("#" + page).attr("class", "current");
		showRealtimeCustomerCount();
		var requestData = {
			'building' : building
		};	
		getRealtimeCustomerShopData(requestData);
		chartHandle = setInterval(function() {
			getRealtimeCustomerShopData(requestData);
		}, 5000);
//		setTimeout(showDynamicLine, 1000);
	
		statisticHebdomadCustomer(requestData);
		showOneDayFlowLine([]);
		statisticOneDayFlow(requestData);
		oneDayFlowHandle = setInterval(function() {
			statisticOneDayFlow(requestData);
		}, 600000);
	});
</script>
