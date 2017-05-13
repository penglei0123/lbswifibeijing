<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp" %>
<%@ include file="/WEB-INF/public/top.jsp" %>

<div class="am-cf admin-main">
<%@ include file="/WEB-INF/public/menu.jsp" %>

  <!-- content start -->
  <div class="admin-content" style="min-height:800px;height:800px;">
    <div class="am-cf am-padding" style="height:800px;overflow:auto">
        <div class="am-panel am-panel-default">
          <div class="am-panel-hd am-cf" data-am-collapse="{target: '#collapse-panel-1'}">历史客流走势<span class="am-icon-chevron-down am-fr" ></span></div>
          <div class="am-panel-bd am-collapse am-in" id="collapse-panel-1">
          		<select class="select_duration">
	  			 <option value="0">选择范围</option>
		         <option value="604800">1周内</option>
		         <option value="-1">自选区间</option>
		        </select>
	          	<span class="custom_duration" style="display:none">
		        <input type="text" id="time_start" startTimestamp="" showTime="false" placeholder="yyyy-MM-dd" onclick="laydate({istime: false, format: 'YYYY-MM-DD'})">
		        - 
		        <input type="text" id="time_end" endTimestamp="" showTime="false" placeholder="yyyy-MM-dd" onclick="laydate({istime: false, format: 'YYYY-MM-DD'})">
		        </span>
		        <button type="button" id="btnSearchHistoryCustomerFlow" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 查询</button>
          		<div id="flow_line" style="width:90%;height:500px;"></div>
          </div>
        </div>
    </div>
  </div>
  <!-- content end -->
</div>
<%@ include file="/WEB-INF/public/foot.jsp" %>
<script src="<%=basePath%>/static/script/common/charts.js"></script>
<script>
var page="gailan";
$(function(){
	$("#"+page).attr("class","current");
	showHistoryFlowLine([]);
	$('#btnSearchHistoryCustomerFlow').click(function(){
		var data = {"building":building};
		
		if(!getQueryTimeDuration(this,data)){
			return;
		}
		statisticHistoryFlow(data);
	});
});
</script>
