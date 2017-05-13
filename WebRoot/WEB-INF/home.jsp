<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp" %>
<%@ include file="/WEB-INF/public/top.jsp" %>

<div class="am-cf admin-main">
<%@ include file="/WEB-INF/public/menu.jsp" %>

  <!-- content start -->
  <div class="admin-content" style="min-height:800px;height:800px;">
    <div class="am-cf am-padding am-u-sm-12" style="height:800px;overflow:auto">
    	<div id="realtime_customer_count" style="float:left;width:40%;height:500px;"></div>
    	<div id="realtime_customer_line" style="float:left;width:60%;height:500px;"></div>
    	<div id="shop_list">
    		<table class="am-table am-table-bordered">
			    <thead>
			        <tr>
			            <th>商家名称</th>
			            <th>所在楼层</th>
			            <th>活跃人数</th>
			        </tr>
			    </thead>
			    <tbody>
			        <tr>
			            <td colspan=3><p align="center">No Available Data</p></td>
			        </tr>
			    </tbody>
			</table>
    	</div>
    	<div class="am-panel am-panel-default">
          <div class="am-panel-hd am-cf" data-am-collapse="{target: '#collapse-panel-heatmap'}">各楼层实时热力图<span class="am-icon-chevron-down am-fr" ></span></div>
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
<script src="<%=basePath%>/static/script/common/charts.js"></script>
<script>
var page="home";
var chartHandle = null;
$(function(){
	$("#"+page).attr("class","current");
	showRealtimeCustomerCount();
	var requestData = {'building':building};
	getRealtimeCustomerShopData(requestData);
	chartHandle = setInterval(function() {
		getRealtimeCustomerShopData(requestData);
	}, 5000);
	setTimeout(showDynamicLine,1000);
	//加载各楼层热力图
	for(var i in floorList)
		$('.heatmap_list').append('<div class="heatmap" id="heatmap_'+floorList[i]+'"><p>'+floorList[i]+'热力图</p></div><hr>');
	loadAllHeatmap();
});
</script>