<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp" %>
<%@ include file="/WEB-INF/public/top.jsp" %>
<div class="am-cf admin-main">
  <%@ include file="/WEB-INF/public/menu.jsp" %>
   <!-- content start -->
  <div class="admin-content" style="min-height:800px;height:800px;">
    <div class="am-cf am-padding am-u-sm-12" style="height:800px;overflow:auto">
    	<div class="am-panel am-panel-default">
          <div class="am-panel-bd am-collapse am-in" id="collapse-panel-heatmap">
			<div class="heatmap_list" style="overflow:auto;width:100%;height:700px;background:none;">
			</div>
          </div>
        </div>
    </div>
  </div>
  <!-- content end -->
</div>
<%@ include file="/WEB-INF/public/foot.jsp" %>
<script>
    page="ap_"+page;
	$(function(){
		$("#" + page).attr("class", "current");
		//加载各楼层地图
		for ( var i in floorList){
			$('.heatmap_list').append('<p>'+floorList[i]+'</p><canvas id="canvas_'+floorList[i]+'"></canvas><hr>');
		}
		var requestData = {
			"building" : building,
		};
		getAllApPosition(requestData);
		var realtimeHandle = null;
		realtimeHandle = setInterval(function(){
			getAllApPosition(requestData);
		}, 5000);
	});
</script>