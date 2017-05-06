<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp" %>
<%@ include file="/WEB-INF/public/top.jsp" %>

<div class="am-cf admin-main">
<%@ include file="/WEB-INF/public/menu.jsp" %>

  <!-- content start -->
  <div class="admin-content" style="min-height:800px;height:800px;">
  	<div class="am-cf am-padding">
  		<div class="am-u-sm-12 am-text-left">
<!--  			<select id="select_floor">
  			 <option value="0" selected>选择楼层</option>
	       </select>		-->  
	       <select class="select_duration">
  			 <option value="0">选择范围</option>
<!-- 			 <option value="600" selected>十分钟内</option>
  			 <option value="1800">半小时内</option>
	         <option value="3600">一小时内</option>
	         <option value="21600">6小时内</option>
	         <option value="43200">12小时内</option>
	         <option value="86400">24小时内</option>		 -->  
	         <option value="604800">1周内</option>
	         <option value="-1">自选区间</option>
	       </select>
	       <span class="custom_duration" style="display:none">
	       <input type="text" id="time_start" startTimestamp="" placeholder="YYYY-MM-DD hh:mm:ss" onclick="laydate({istime: true, format: 'YYYY-MM-DD hh:mm:ss'})">
	       <input type="text" id="time_end" endTimestamp="" placeholder="YYYY-MM-DD hh:mm:ss" onclick="laydate({istime: true, format: 'YYYY-MM-DD hh:mm:ss'})">
	       </span>
	       
<!--  	       <input type="text" id="play_speed" placeholder="回放速度(10-2000)" style="width:150px"/>
	       <input type="text" id="step_size" placeholder="步长(10-3600)(秒)" style="width:150px"/>				-->
	       <button type="button" id="btnSearchHistoryHeatmap" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 查询热力图</button>
<!--      <button type="button" id="btnSearchTrackPlayback" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 查询多人轨迹</button>       	  
	       <button type="button" id="btnAbortTrackPlayback" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 取消播放</button>			-->
<!--  	       <div style="float:right;display:none">
	       		<span>人数：<label id="person-value">0</label></span>&nbsp;&nbsp;
	       		<span>时间：<label id="time-value"></label></span>
	       </div>			-->
  		</div>
    </div>
<!--   <div class="am-progress am-margin-bottom-xs" style="height:1px;">
	 <div class="am-progress-bar" style="width: 0%" id="play_progress"></div>
	</div>		-->   
    <div class="am-cf am-padding" style="height:750px;overflow:auto">
    	<div class="am-panel am-panel-default">
          <div class="am-panel-hd am-cf" data-am-collapse="{target: '#collapse-panel-1'}">历史热力图<span class="am-icon-chevron-down am-fr" ></span></div>

          <div id="container" class="am-u-md-9" style="overflow:auto;width:100%;height:650px;background:none;">
<!--            <div style="overflow:auto;width:100%;max-height:700px;">
            	<div id="heatmap"><p align="center">No Available Data</p></div>			-->  
        
          </div>
        </div>
<!--     <div class="am-panel am-panel-default">
          <div class="am-panel-hd am-cf" data-am-collapse="{target: '#collapse-panel-2'}">多人轨迹图<span class="am-icon-chevron-down am-fr" ></span></div>
          <div class="am-panel-bd am-collapse am-in" id="collapse-panel-2">
			<div style="overflow:auto;width:100%;background:none;">
				<div id="trackWithHeatmap"><p align="center">No Available Data</p></div>
			</div>
          </div>
        </div>		 -->
    </div>   
  </div>
  <!-- content end -->
</div>
<%@ include file="/WEB-INF/public/foot.jsp" %>
<script>
var page="heatmap_history";
$(function(){
	$("#"+page).attr("class","current");
	$('#btnSearchHistoryHeatmap').click(function() {
		var duration = $('.select_duration option:selected').val();
		var building = "zhonggauncun";
		var floor = "F1";
		var data = {};
		if (duration == '0') {
			layer.alert("请设置正确的查询条件", {
				icon : 0
			});
			return;
		}else if (duration == '-1') {
			var duration_start = get_unix_time($('#time_start').val());
			var duration_end = get_unix_time($('#time_end').val());
			data['duration'] = duration;
			data['duration_start'] = duration_start;
			data['duration_end'] = duration_end;
			data['duration_type'] = 'custom';
		}else{
			data['duration'] = duration;
		}
		data['building'] = building;		
		data['floor'] = floor;
		
			getHistoryHeatmap(data);
			
	});
});
</script>
