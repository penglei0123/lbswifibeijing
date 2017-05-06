<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp"%>
<%@ include file="/WEB-INF/public/top.jsp"%>

<div class="am-cf admin-main">
	<%@ include file="/WEB-INF/public/menu.jsp"%>
	<!-- content start -->
	<div class="admin-content"
		style="min-height: 900px; height: 900px; overflow: auto; z-index: 999">
		<div class="am-cf am-padding">
			<div class="am-u-sm-3 am-text-left"></div>
		</div>
		<div class="am-progress am-margin-bottom-xs" style="height: 1px;">&nbsp;</div>

		<div class="am-cf am-padding am-margin-sx">
			<div id="container" class="am-u-md-9"
				style="overflow: auto; width: 100%; height: 780px; background: none;">
				<canvas id="canvas">您的浏览器不支持canvas!</canvas>
			</div>
		</div>
	</div>
	<!-- content end -->

</div>
<%@ include file="/WEB-INF/public/foot.jsp"%>
<script>
	page = "position_" + page;
	$(function() {
		$("#" + page).attr("class", "current");

		var data = {
			"building" : building,
			"floor" : floorConfig
		};
		getPosition(data);
		clearInterval(realtimeHeatmapHandle);
		realtimeHeatmapHandle = setInterval(function() {
			getPosition(data);
		}, 15000);
	});
</script>