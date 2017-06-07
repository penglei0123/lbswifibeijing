<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp" %>
<%@ include file="/WEB-INF/public/top.jsp" %>
<div class="am-cf admin-main">
  <%@ include file="/WEB-INF/public/menu.jsp" %>
   <!-- content start -->
  <div class="admin-content" style="min-height:800px;height:800px;">
    <div class="am-cf am-padding am-u-sm-12" style="height:800px;">
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
		
		 var imgMarker = new Image();
	     imgMarker.src = basePath + "/static/images/marker.png"// 设置覆盖物
	     imgMarker.onload = function(){
	    	//递归方式加载所有楼层地图
	 		var totalFloorCount = floorList.length;
	 		if(totalFloorCount > 0){
	 			loadAllMap(floorList[0],totalFloorCount,totalFloorCount-1,imgMarker);
	 		}
	     }
		
		var mapScale = {};
		var mapWidth = {};
		var mapHeight = {};
		function loadAllMap(curFloor,totalFloorCount,remainFloorCount,imgMarker){
			var img = new Image();
			img.src = basePath + "/static/map/" + building + "_"+curFloor + ".jpg";// 设置地图
			img.onload = function(){
				var width = img.width;
				var height = img.height;
			//	console.log("curFloor"+curFloor+":"+width+","+height)
				var maxWidth = $('.ap_list').width();
				if(width>maxWidth){
					mapScale[curFloor] = maxWidth*1.0/width;
				}else{
					mapScale[curFloor] = 1.0;
				}
			
				width *= mapScale[curFloor];
				height *= mapScale[curFloor];
				
				mapWidth[curFloor] = width;
				mapHeight[curFloor] =height;
				
				$("#apmap_"+curFloor).css("background","url("+img.src+") no-repeat");
				$("#apmap_"+curFloor).css("width",width);
				$("#apmap_"+curFloor).css("height",height);
				//关键设置，不然图片不会等比例缩放
				$("#apmap_"+curFloor).css("background-size","contain");
				if(remainFloorCount>0){
					var index = totalFloorCount-remainFloorCount;
					loadAllMap(floorList[index],totalFloorCount,remainFloorCount-1,imgMarker);
				}
				if(remainFloorCount == 0){
					getAllApPosition(requestData,mapWidth,mapHeight,imgMarker,mapScale);
					 clearInterval(realtimeHandle);
					realtimeHandle = setInterval(function(){
						getAllApPosition(requestData,mapWidth,mapHeight,imgMarker,mapScale);
					}, 5000);
				}
			}
		
		}
	});
</script>