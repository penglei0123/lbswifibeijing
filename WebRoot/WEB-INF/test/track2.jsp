<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp" %>
<%@ include file="/WEB-INF/public/top.jsp" %>

<div class="am-cf admin-main">
<%@ include file="/WEB-INF/public/menu.jsp" %>
<style>
.ratings_bars{width:407px;height:27px;float:left;margin-left:20px;}
.ratings_bars #title0{width:25px;height:25px;text-align:center;border:1px solid #bfbebe;line-height:25px;font-family:Georgia, "Times New Roman", Times, serif;font-size:14px;float:left;color:#a0a0a0;margin-right:10px;background:#fff;}
.ratings_bars #title1{width:25px;height:25px;text-align:center;border:1px solid #bfbebe;line-height:25px;font-family:Georgia, "Times New Roman", Times, serif;font-size:14px;float:left;color:#a0a0a0;margin-right:10px;background:#fff;}
.ratings_bars #title2{width:25px;height:25px;text-align:center;border:1px solid #bfbebe;line-height:25px;font-family:Georgia, "Times New Roman", Times, serif;font-size:14px;float:left;color:#a0a0a0;margin-right:10px;background:#fff;}
.ratings_bars #title3{width:25px;height:25px;text-align:center;border:1px solid #bfbebe;line-height:25px;font-family:Georgia, "Times New Roman", Times, serif;font-size:14px;float:left;color:#a0a0a0;margin-right:10px;background:#fff;}
.ratings_bars #title4{width:25px;height:25px;text-align:center;border:1px solid #bfbebe;line-height:25px;font-family:Georgia, "Times New Roman", Times, serif;font-size:14px;float:left;color:#a0a0a0;margin-right:10px;background:#fff;}
.ratings_bars .bars_10{font-family:Georgia, "Times New Roman", Times, serif;font-size:18px;line-height:25px;float:left;color:#a0a0a0;}
.ratings_bars .scale{width:299px;height:13px;float:left;margin:7px 10px auto 10px;position:relative;background:url(<%=basePath%>static/images/progress.png) 0 0 no-repeat;}
.ratings_bars .scale div{width:0px;position:absolute;width:0;left:0;height:13px;bottom:0;background:url(<%=basePath%>static/images/progress.png) 0 -14px no-repeat;}
.ratings_bars .scale span{width:10px;height:26px;position:absolute;left:-2px;top:-7px;cursor:pointer;background:url(<%=basePath%>static/images/bar.png) no-repeat;}
.container{width:450px;border:1px solid #6C9C2C;height:25px;}
#bar{background:#95CA0D;float:left;height:100%;text-align:center;line-height:150%;}  
</style>
  <!-- content start -->
  <div class="admin-content" style="min-height:800px;height:800px;">
  	<div class="am-cf am-padding">
  		<div class="am-text-left" style="float:left">
	       <select id="select_user">
  			 <option value="0">选择用户</option>
  			 <option value="-1">输入MAC</option>
	       </select>
	       <input type="text" id="user-mac" placeholder="用户MAC" style="display:none;width:150px;">
	       <select id="select_floor">
  			 <option value="0" selected>选择楼层</option>
	       </select>
  		   <select class="select_duration">
  			 <option value="0">选择范围</option>
  			 <option value="600" selected>十分钟内</option>
  			 <option value="1800">半小时内</option>
	         <option value="3600">一小时内</option>
	         <option value="21600">6小时内</option>
	         <option value="43200">12小时内</option>
	         <option value="86400">24小时内</option>
	         <option value="604800">1周内</option>
	         <option value="-1">自选区间</option>
	       </select>
	       <span class="custom_duration" style="display:none">
	       <input type="text" id="time_start" startTimestamp="" placeholder="YYYY-MM-DD hh:mm:ss" onclick="laydate({istime: true, format: 'YYYY-MM-DD hh:mm:ss'})">
	       - 
	       <input type="text" id="time_end" endTimestamp="" placeholder="YYYY-MM-DD hh:mm:ss" onclick="laydate({istime: true, format: 'YYYY-MM-DD hh:mm:ss'})">
	       </span>
	       <button type="button" id="btnSearchUserTrack" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 查询</button>
	       <button type="button" id="btnStopDrawTrack" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-ban"></span> 停止绘制</button>
  		   <button type="button" id="btnChangeFloor" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-ban">查询单人轨迹</span>
  		</div>
  		<div class="ratings_bars">
			<span id="title0">0</span>
			<span class="bars_10">0</span>
			<div class="scale" id="bar0">
				<div></div>
				<span id="btn0"></span>
			</div>
			<span class="bars_10">10</span>
		</div> 
		<div id = "line">
	    <div class="container"> 
             <div id="bar" style="width:0%;"></div>  
        </div>  
        <span id="total"></span> 
        </div>
    <hr>
    <div class="am-cf am-padding" style="height:700px;overflow:auto">
    	<div id="floormap" class="am-u-md-12" >
    		 <!--  <canvas id="canvas" >您的浏览器不支持canvas!</canvas>-->
	    </div>
	    <!--  
	    <div class="track_list">
		    <div class="am-panel am-panel-default">
	          <div class="am-panel-hd am-cf" data-am-collapse="{target: '#track_list'}">用户轨迹分层列表<span class="am-icon-chevron-down am-fr" ></span></div>
	          <div class="am-panel-bd am-collapse am-in" id="track_list">
				<ul>
		    	</ul>
	          </div>
	        </div>
	    </div>
	    -->
    </div>
  </div>
  <!-- content end -->

</div>
<%@ include file="/WEB-INF/public/foot.jsp" %>
<script>
page="track_"+page;
/**
 * 路径数据相关全局变量
 */
var mapPaths = {};
var mapNodes = {};//楼层区分
var mapRegions = {};
var abortDraw = false;
$(function(){
	$("#"+page).attr("class","current");
	getTopKMAC({"building":building});
	//加载全局路径数据和禁止区域数据
	getMapPathAndForbiddenRegionData({"building":building});
	$('#btnSearchUserTrack').click(function() {
		//终止当前绘制
		stopDrawTrack();
		var macSelect = $('#select_user option:selected').val();
		var macInput = $('#user-mac').val();
		var mac;
		if (macSelect == '0') {
			layer.alert("请选择用户", {
				icon : 0
			});
			return;
		}else if(macSelect=='-1' && macInput==''){
			layer.alert("请输入用户MAC", {
				icon : 0
			});
			return;
		}else if(macSelect=='-1'){
			mac = macInput;
		}else{
			mac = macSelect;
		}
		var floor = $('#select_floor option:selected').val();
		if (floor == '0') {
			layer.alert("请选择楼层", {
				icon : 0
			});
			return;
		}
		var data = {
			"building" : building,
			"mac" : mac,
			"floor":floor
			//"mac":"b4:30:52:18:e8:69"
			//"mac":"c8:14:79:64:fc:1d"
		};
		if(!getQueryTimeDuration(this, data)){
			return;
		}
		getUserHistoryTrack(data);
	});
	
	$('#btnStopDrawTrack').click(function() {
		stopDrawTrack();
	});
});

function stopDrawTrack(){
	abortDraw = true;
}

function resetDrawStatus(){
	abortDraw = false;
}

function getMapPathAndForbiddenRegionData(requestData){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/track/history?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_map_path_forbidden_region",
			"data" : JSON.stringify(requestData)
		},
		beforeSend : function() {
			processLayout = layer.load(1, {shade : false}); // 0代表加载的风格，支持0-2
		},
		success : function(result) {
			layer.close(processLayout);
			if (result.status !== 1) {
				layer.alert(result.message, {icon : 0});
			} else {
				if(result.hasOwnProperty("mapPaths")){
					mapPaths = result.mapPaths;
					mapNodes = result.mapNodes;
				}
				if(result.hasOwnProperty("mapRegions")){
					mapRegions = result.mapRegions;
				}
			}
		},
		error : function() {
			layer.close(processLayout);
			layer.alert("网络繁忙，请稍后再试！", {icon : 0});
		}
	});
}
/**
 * 滑动杆相关
 */
scale = function (btn, bar, title) {
	this.btn = document.getElementById(btn);
	this.bar = document.getElementById(bar);
	this.title = document.getElementById(title);
	this.step = this.bar.getElementsByTagName("DIV")[0];
	this.init();
	var rate = (drawSpeed-10.0)/(1000-10);
	var pos = Math.ceil(100*rate)
	var x = Math.ceil(this.bar.clientWidth*rate);
	this.ondrag(pos,x);
	this.btn.style.left = x + 'px';
};
scale.prototype = {
	init: function () {
		var f = this, g = document, b = window, m = Math;
		f.btn.onmousedown = function (e) {
			var x = (e || b.event).clientX;
			var l = this.offsetLeft;
			var max = f.bar.offsetWidth - this.offsetWidth;
			g.onmousemove = function (e) {
				var thisX = (e || b.event).clientX;
				var to = m.min(max, m.max(-2, l + (thisX - x)));
				f.btn.style.left = to + 'px';
				f.ondrag(m.round(m.max(0, to / max) * 100), to);
				b.getSelection ? b.getSelection().removeAllRanges() : g.selection.empty();
			};
			g.onmouseup = new Function('this.onmousemove=null');
		};
	},
	ondrag: function (pos, x) {
		this.step.style.width = Math.max(0, x) + 'px';
		if(x<0){
			x=0;
		}
		var speed = 10+(1000-10)*(x*1.0/this.bar.clientWidth);
		drawSpeed = Math.ceil(speed);
		console.log(drawSpeed);
		this.title.innerHTML = pos / 10 + '';
	}
}
new scale('btn0', 'bar0', 'title0');
//自动切换楼层显示轨迹
$('#btnChangeFloor').click(function(){
	    stopDrawTrack();
		var macSelect = $('#select_user option:selected').val();
		var mac;
		if(macSelect != '0' && macSelect != '-1'){
			mac = macSelect
		}
		var data = {
				"building" : building,
				"mac" : mac,
	    };
		console.log("data"+data);
		if(!getQueryTimeDuration(this,data)) {
			return;
		}
		 getPersonHistoryTrack(data);
});

//查询单mac历史轨迹自动切换楼层
function getPersonHistoryTrack(requestData){
	 $.ajax({
		type : "GET",
		dataType : "json",
		url : basePath +"/track/history?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_history_person_track",
			"data" : JSON.stringify(requestData),
		},
		beforeSend : function(){
			processLayout = layer.load(1, {
				shade : false
			}); // 0代表加载的风格，支持0-2
		},
		success : function(result){
			layer.close(processLayout);
			if (result.status !== 1){
				layer.alert(result.message, {
					icon : 0
				});
			}else{
			  result = {
				    "data": [
				             [
				                 {
				                     "x": 702.6729851711796,
				                     "y": 799.0574182984308,
				                     "time": 1489387068741,
				                     "floor": "B1"
				                 },
				                 {
				                     "x": 673.9637556045909,
				                     "y": 815.3171518915501,
				                     "time": 1489387069743,
				                     "floor": "B1"
				                 }],
				                 [{
				                     "x": 776.0424385176639,
				                     "y": 850.5774418770791,
				                     "time": 1489387070744,
				                     "floor": "F1"
				                 },
				                 {
				                     "x": 778.4781897610854,
				                     "y": 840.1379758641935,
				                     "time": 1489387071745,
				                     "floor": "F1"
				                 },
				                 {
				                     "x": 778.4781897610854,
				                     "y": 840.1379758641935,
				                     "time": 1489387072747,
				                     "floor": "F1"
				                 }],
				                 [{
				                     "x": 778.4781897610854,
				                     "y": 840.1379758641935,
				                     "time": 1489387073748,
				                     "floor": "B1"
				                 }],
				             ]
		 				}; 
				var data = result.data;
				var groupIndex = 0;		
				drawChangeFloor(data,groupIndex);
			}
		},
		error : function(){
			layer.close(processLayout);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
		
	}) 
}
	 
	function drawChangeFloor(data,groupIndex){
		console.log(groupIndex);
		console.log(data[groupIndex]);
		var img = new Image();
		img.src = basePath + "/static/map/" + building + "_" + data[groupIndex][0].floor + ".jpg";//设置地图
		if(img.complete){
			showDynamicFloorTrack(data,groupIndex,img,data[groupIndex][0].floor);
		}else{
			img.onload = function(){
				showDynamicFloorTrack(data,groupIndex,img,data[groupIndex][0].floor);
			}
		}
	}
	
	//初始化轨迹画布,调用绘图方法
	function showDynamicFloorTrack(trackData,groupIndex,img,floor){
		$('#floormap').html('');
		$('#floormap').css("background","url("+img.src+") no-repeat");
		$('#floormap').css("width",img.width);
		$('#floormap').css("height",img.height);
		
		$('#floormap').append('<canvas id="canvas" style="position: absolute; z-index: 999"></canvas>');
		$('#canvas').attr("width",img.width-50);
		$('#canvas').attr("height",img.height-50);
		// 绘制完热力图背景后再绘点
		var total = 0;
		//点校正和路径补充（当有路径数据时）
		
			if(mapPaths.hasOwnProperty(floor)){
				console.log("input length:"+trackData[groupIndex].length);
				//trackData[i] = updateTrackPoints(trackData[i],floor);
				//trackData[groupIndex] = updateTrackPointsWidthOriginPoints(trackData[groupIndex],floor);
			}
		var canvas = document.getElementById('canvas');
		var context = canvas.getContext('2d');
		// 先清除画布
		context.clearRect(0, 0, img.width, img.height);// 清除整个画布
		resetDrawStatus();
		/**
		 * 延迟绘制（递归调用）
		 */
		 drawDynamicFloorTrack(trackData,groupIndex,img,context);
		/**
		 * 批量一次性绘制(存在性能问题，当点数超过5K时，绘制大量点和线条会导致浏览器卡死
		 */
//		var m_canvas = document.createElement('canvas');  
//		m_canvas.width = canvas.width;  
//		m_canvas.height = canvas.height;  
//		var m_context = m_canvas.getContext('2d');
//		for(var i in trackData){
//			drawTrackNoDelay(trackData,i,img,m_context);
//		}
//		context.drawImage(m_canvas,0,0);
		
	}
	
	function drawDynamicFloorTrack(groupData,groupIndex,img,context){
		
		var index = 0;
		var p = index;
		var step = 0;
		
		var drawHandle = setInterval(function(){
			var data = groupData[groupIndex];
			if(abortDraw){
				//突出起点
				context.fillStyle="#76EE00";
				context.beginPath();
				context.arc(data[0].x, data[0].y, 10, 0,2 * Math.PI);
				context.closePath();
				context.fill();
				//突出终点
			    context.fillStyle="red";
			    context.beginPath();
			    context.arc(data[index-1].x, data[index-1].y, 10, 0,2 * Math.PI);
			    context.closePath();
			    context.fill();
			    
				clearInterval(drawHandle);
				resetDrawStatus();
				return;
			}
			if(index>=data.length){
				//突出起点
				context.fillStyle="#76EE00";
				context.beginPath();
				context.arc(data[0].x, data[0].y, 10, 0,2 * Math.PI);
				context.closePath();
				context.fill();
				//突出终点
			    context.fillStyle="red";
			    context.beginPath();
			    context.arc(data[index-1].x, data[index-1].y, 10, 0,2 * Math.PI);
			    context.closePath();
			    context.fill();
			    
				clearInterval(drawHandle);
				groupIndex++;
				// 本条线路绘制完毕，开始下一条路径绘制
				if(groupIndex<groupData.length){
					drawChangeFloor(groupData,groupIndex);
				}
				else{
					layer.alert("绘制完毕！", {icon : 1});
				}
				return;
			}
			//console.log(index);
			if(index>0){
				var line = new Line(data[index-1].x,data[index-1].y,data[index].x,data[index].y);
				line.drawWithArrowheads(context);
			}
			index++;
		},drawSpeed);
	}
	
</script>