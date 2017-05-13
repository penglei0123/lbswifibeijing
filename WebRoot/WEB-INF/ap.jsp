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
			<div class="ap_list" style="overflow:auto;width:100%;height:700px;background:none;">
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
		var realtimeHandle = null;
		$("#" + page).attr("class", "current");
		//加载各楼层地图
		for ( var i in floorList){
			$('.ap_list').append('<p>'+floorList[i]+'</p><div class="apmap" id="apmap_'+floorList[i]+'"><canvas id="canvas_'+floorList[i]+'"></canvas></div><hr>');
		}                       
		var requestData = {
			"building" : building,
		};
		
		//递归方式加载所有楼层地图
		var totalFloorCount = floorlist.length;
		if(totalFloorCount > 0){
			loadAllMap(floorList[0],totalFloorCount,totalFloorCount-1);
		}
		var mapScale = {};
		
		function loadAllMap(curFloor,totalFloorCount,remainFloorCount){
			var img = new Image();
			img.src = basePath + "/static/map/" + building + "_"+curFloor + ".jpg";// 设置地图
			img.onload = function(){
				var width = img.width;
				var height = img.height;
				var maxWidth = $('.ap_list').width();
				if(width>maxWidth){
					mapScale[curFloor] = maxWidth*1.0/width;
				}else{
					mapScale[curFloor] = 1.0;
				}
				width *= mapScale[curFloor];
				height *= mapScale[curFloor];
				$("#apmap_"+curFloor).css("background","url("+img.src+") no-repeat");
				$("#apmap_"+curFloor).css("width",width);
				$("#apmap_"+curFloor).css("height",height);
				//关键设置，不然图片不会等比例缩放
				$("#apmap_"+curFloor).css("background-size","contain");
				getAllApPosition(requestData,width,height);
				clearInterval(realtimeHandle);
				realtimeHandle = setInterval(function(){
					getAllApPosition(requestData,width,height);
				}, 5000);
				if(remainFloorCount>0){
					var index = totalFloorCount-remainFloorCount;
					loadAllMap(floorList[index],totalFloorCount,remainFloorCount-1);
				}
			}
		
		}
	});
</script>