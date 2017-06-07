<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/public/head.jsp" %>
<%@ include file="/WEB-INF/public/top.jsp" %>

<div class="am-cf admin-main">
<%@ include file="/WEB-INF/public/menu.jsp" %>

  <!-- content start -->
  <div class="admin-content" style="min-height:800px;height:800px;">
    <div class="am-cf am-padding" style="height:800px;overflow:auto">
    	<div class="am-panel am-panel-default" >
          <div class="am-panel-hd am-cf" data-am-collapse="{target: '#collapse-panel-rank'}">店铺排行</div>
          <div class="am-panel-bd am-collapse am-in" id="collapse-panel-rank" style="height:400px;overflow:auto">
	        <input type="text" id="input_date" placeholder="YYYY-MM-DD" onclick="laydate({istime: false, format: 'YYYY-MM-DD'})" value="">
	        <button type="button" id="btnSearchShopRank" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 查询</button>
            <table class="am-table am-table-bordered am-margin-top-sm" id="shopRank">
			    <thead>
			        <tr>
			            <th onclick="$.sortTable.sort('shopRank',0,'string')">商家名称</th>
			            <th onclick="$.sortTable.sort('shopRank',1,'string')">所在楼层</th>
			            <th onclick="$.sortTable.sort('shopRank',2,'number')">进店率</th>
			            <th onclick="$.sortTable.sort('shopRank',3,'number')">平均驻留时间（分钟）</th>
			            <th onclick="$.sortTable.sort('shopRank',4,'number')">客流量</th>
			        </tr>
			    </thead>
			    <tbody>
			    	<tr><td colspan=5>No Available Data</td></tr>
			    </tbody>
			</table>
          </div>
        </div>
        <div class="am-panel am-panel-default">
          <div class="am-panel-hd am-cf" data-am-collapse="{target: '#collapse-panel-rate'}">进店率统计<span class="am-icon-chevron-down am-fr" ></span></div>
          <div class="am-panel-bd am-collapse am-in" id="collapse-panel-rate">
          		<select class="select_shops">
          			<option value="0">选择店铺</option>
          		</select>
          		<select class="select_duration">
	  			 <option value="0">选择范围</option>
		         <option value="604800" selected>1周内</option>
		         <option value="-1">自选区间</option>
		        </select>
	          	<span class="custom_duration" style="display:none">
		        <input type="text" id="time_start" startTimestamp="" showTime="false" placeholder="YYYY-MM-DD" onclick="laydate({istime: false, format: 'YYYY-MM-DD'})">
		        - 
		        <input type="text" id="time_end" endTimestamp="" showTime="false" placeholder="YYYY-MM-DD" onclick="laydate({istime: false, format: 'YYYY-MM-DD'})">
		        </span>
          		<button type="button" id="btnSearchRateInShop" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 查询</button>
          		<div id="rateInShopLine" class="am-margin-top-sm" style="width:90%;height:500px;"></div>
          </div>
        </div>
        <div class="am-panel am-panel-default">
          <div class="am-panel-hd am-cf" data-am-collapse="{target: '#collapse-panel-staytime'}">驻留时间统计<span class="am-icon-chevron-down am-fr" ></span></div>
          <div class="am-panel-bd am-collapse am-in" id="collapse-panel-staytime">
          		<select class="select_shops">
          			<option value="0">选择店铺</option>
          		</select>
          		<input type="text" id="input_date" placeholder="YYYY-MM-DD" onclick="laydate({istime: false, format: 'YYYY-MM-DD'})" value="">
          		<button type="button" id="btnSearchStayTimeInShop" class="am-btn am-btn-primary am-btn-xs"><span class="am-icon-search"></span> 查询</button>
          		<div id="stayTimeInShopBar" class="am-margin-top-sm" style="width:90%;height:500px;"></div>
          </div>
        </div>
    </div>
  </div>
  <!-- content end -->
</div>
<%@ include file="/WEB-INF/public/foot.jsp" %>
<script src="<%=basePath%>/static/script/common/charts.js"></script>
<script>
var page="shop";
$(function(){
	$("#"+page).attr("class","current");
	var data = {"building":building};
	getShopList(data);
	showRateInShopLine([]);
	showStayTimeInShopBar([]);
	
	$('#btnSearchShopRank').click(function(){
		var data = {'building':building};
		var date = $(this).siblings('#input_date').val().trim();
		if(date==''){
			layer.alert("请选择查询日期", {icon : 0});
			return;
		}
		data['time']=get_unix_time(date);
		getHistoryShopRank(data);
	});
	
	$('#btnSearchRateInShop').click(function(){
		var floor = $(this).siblings('.select_shops').children('option:selected').attr("floor");
		if(floor==''){
			layer.alert("请选择店铺", {icon : 0});
			return;
		}
		var shopId = $(this).siblings('.select_shops').children('option:selected').val();
		var data = {'building':building,"floor":floor,"shopId":parseInt(shopId)};
		console.log(data);
		if(!getQueryTimeDuration(this,data)){
			return;
		}
		statisticRateInShop(data);
	});
	
	$('#btnSearchStayTimeInShop').click(function(){
		var floor = $(this).siblings('.select_shops').children('option:selected').attr("floor");
		if(floor==''){
			layer.alert("请选择店铺", {icon : 0});
			return;
		}
		var shopId = $(this).siblings('.select_shops').children('option:selected').val();
		var data = {'building':building,"floor":floor,"shopId":parseInt(shopId)};
		var date = $(this).siblings('#input_date').val().trim();
		if(date==''){
			layer.alert("请选择查询日期", {icon : 0});
			return;
		}
		data['time']=get_unix_time(date);
		statisticStayTimeInShop(data);
	});
});
</script>
