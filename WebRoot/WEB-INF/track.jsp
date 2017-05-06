<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp" %>
<%@ include file="/WEB-INF/public/top.jsp" %>

<div class="am-cf admin-main">
<%@ include file="/WEB-INF/public/menu.jsp" %>

  <!-- content start -->
  <div class="admin-content" style="min-height:800px;height:800px;">
  	<div class="am-cf am-padding">
  		<div class="am-u-sm-10 am-text-left">
			 <select id="select_user">
  			 <option value="0">选择用户</option>
  			 <option value="-1">输入MAC</option>
	       </select>	  
	       <input type="text" id="user-mac" placeholder="用户MAC" style="display:none;width:150px;">
<!--       <select id="select_floor">
  			 <option value="0" selected>选择楼层</option>
	       </select>		-->	  
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
	       <button type="button" id="btnSearchUserTrack" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 播放</button>
<!--     <div id = box ></div>  -->	    
  		</div>
    </div>
    <hr>
   <div class="am-cf am-padding" style="width:100%;height:700px;overflow:auto">
    	<div id="container" class="am-u-md-9" style="overflow:auto;width:100%;height:100%;background:none;">
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
$(function(){
	$("#"+page).attr("class","current");
	getTopKMAC();
	$('#btnSearchUserTrack').click(function() {
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
/**
		var floor = $('#select_floor option:selected').val();
		if (floor == '0') {
			layer.alert("请选择楼层", {
				icon : 0
			});
			return;
		}
		*/
		var data = {
			"building" : building,
			"mac" : mac,
			"floor" : "F1"
			//"mac":"b4:30:52:18:e8:69"
			//"mac":"c8:14:79:64:fc:1d"
		};
		if(!getQueryTimeDuration(this, data)){
			return;
		}
		getUserHistoryTrack(data);
	});
});
</script>