<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp" %>
<%@ include file="/WEB-INF/public/top.jsp" %>

<div class="am-cf admin-main">
<%@ include file="/WEB-INF/public/menu.jsp" %>

  <!-- content start -->
  <div class="admin-content" style="min-height:800px;height:800px;">
  	<div class="am-cf am-padding">
  		<div class="am-u-sm-12 am-text-left">
  		   <select id="select_floor">
  			 <option value="0" selected>选择楼层</option>
	       </select>
  		   <select id="select_duration">
  			 <option value="0">选择范围</option>
  			 <option value="60" selected>一分钟内</option>
  			 <option value="300" selected>五分钟内</option>
  			 <option value="600" selected>十分钟内</option>
  			 <option value="1800">半小时内</option>
	         <option value="3600">一小时内</option>
	         <option value="21600">6小时内</option>
	         <option value="43200">12小时内</option>
	         <option value="86400">24小时内</option>
	         <option value="604800">1周内</option>
	         <option value="-1">自选区间</option>
	       </select>
	       
	       <span class="custom_duration">
	       <input type="text" id="time_start" startTimestamp="" placeholder="YYYY-MM-DD hh:mm:ss" onclick="laydate({istime: true, format: 'YYYY-MM-DD hh:mm:ss'})">
	       - 
	       <input type="text" id="time_end" endTimestamp="" placeholder="YYYY-MM-DD hh:mm:ss" onclick="laydate({istime: true, format: 'YYYY-MM-DD hh:mm:ss'})">
	       </span>
	       <input type="text" id="play_speed" placeholder="回放速度(10-2000)"/>
	       <input type="text" id="step_size" placeholder="步长(10-3600)(秒)"/>
	       <button type="button" id="btnSearchTrackPlayback" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 轨迹回放</button>
	       <button type="button" id="btnAbortTrackPlayback" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 取消回放</button>
	       <div style="float:right">
	       		<span>人数：<label id="person-value">0</label></span>&nbsp;&nbsp;
	       		<span>时间：<label id="time-value"></label></span>
	       </div>
  		</div>
    </div>
    <div class="am-progress" style="height:2px">
	 <div class="am-progress-bar" style="width: 0%" id="play_progress"></div>
	</div>
    <div class="am-cf am-padding" style="height:700px;overflow:auto">
    	<div id="floormap" class="am-u-md-12" >
    		 <!--  <canvas id="canvas" >您的浏览器不支持canvas!</canvas>-->
	    </div>
    </div>
  </div>
  <!-- content end -->

</div>
<%@ include file="/WEB-INF/public/foot.jsp" %>
<script>
page="track_"+page;
$(function(){
	$("#"+page).attr("class","current");
});
</script>