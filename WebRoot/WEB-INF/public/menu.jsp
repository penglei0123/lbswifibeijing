<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
  <!-- sidebar start -->
  <div class="admin-sidebar">
    <ul class="am-list admin-sidebar-list">
         <li class="admin-parent">
        <a class="am-cf" data-am-collapse="{target: '#info-center'}"><span class="am-icon-file"></span> 信息中心 <span class="am-icon-angle-right am-fr am-margin-right"></span></a>
        <ul class="am-list am-collapse admin-sidebar-sub am-in" id="info-center">
          <li id="home"><a href="<%=basePath%>home"><span class="am-icon-home"></span> 客流总览</a></li>
          <li id="customer_realtime"><a href="<%=basePath%>customer/realtime"><span class="am-icon-area-chart"></span> 实时客流分布</a></li>
           <li id="ap_realtime"><a href="<%=basePath%>ap/realtime"><span class="am-icon-database"></span> AP工作状态</a></li>
        </ul>
      </li>
      <!--  
      <li class="admin-parent">
        <a class="am-cf" data-am-collapse="{target: '#data-analyze'}"><span class="am-icon-file"></span> 数据质量分析 <span class="am-icon-angle-right am-fr am-margin-right"></span></a>
        <ul class="am-list am-collapse admin-sidebar-sub am-in" id="data-analyze">
          <li id="li_data-analyze"><a href="<%=basePath%>netlocate/vis.html" target="_blank"><span class="am-icon-bar-chart"></span>  AP数据展示</a></li>
        </ul>
      </li>
      -->
      <li class="admin-parent">
        <a class="am-cf" data-am-collapse="{target: '#collapse-track'}"><span class="am-icon-file"></span> 人群分析 <span class="am-icon-angle-right am-fr am-margin-right"></span></a>
        <ul class="am-list am-collapse admin-sidebar-sub am-in" id="collapse-track">
          <li id="gailan"><a href="<%=basePath%>gailan"><span class="am-icon-area-chart"></span> 概览</a></li>
          <li id="heatmap_history"><a href="<%=basePath%>heatmap/history"><span class="am-icon-area-chart"></span> 热力图</a></li>
          <li id="track_one"><a href="<%=basePath%>track/one"><span class="am-icon-database"></span> 单人轨迹</a></li>
        </ul>
      </li>
      <li class="admin-parent">
        <a class="am-cf" data-am-collapse="{target: '#collapse-shop'}"><span class="am-icon-file"></span> 商机分析 <span class="am-icon-angle-right am-fr am-margin-right"></span></a>
        <ul class="am-list am-collapse admin-sidebar-sub am-in" id="collapse-shop">
          <li id="shop"><a href="<%=basePath%>shop"><span class="am-icon-building"></span> 到店分析</a></li>
          <li id="shop_bi"><a href="<%=basePath%>shop"><span class="am-icon-building"></span> 商业智能</a></li>
          <li id="shop_activity"><a href="<%=basePath%>shop"><span class="am-icon-building"></span> 营销活动</a></li>
        </ul>
      </li>
      <li class="admin-parent" style="display:none">
        <a class="am-cf" data-am-collapse="{target: '#collapse-data'}"><span class="am-icon-file"></span> 数据洞察 <span class="am-icon-angle-right am-fr am-margin-right"></span></a>
        <ul class="am-list am-collapse admin-sidebar-sub am-in" id="collapse-data">
          <li id="shop"><a href="<%=basePath%>shop"><span class="am-icon-building"></span> 元数据流</a></li>
          <li id="shop_bi"><a href="<%=basePath%>shop"><span class="am-icon-building"></span> 定位评价</a></li>
        </ul>
      </li>
      <!-- 
      <li class="admin-parent">
        <a class="am-cf" data-am-collapse="{target: '#collapse-heatmap'}"><span class="am-icon-file"></span> 热力图 <span class="am-icon-angle-right am-fr am-margin-right"></span></a>
        <ul class="am-list am-collapse admin-sidebar-sub am-in" id="collapse-heatmap">
          <li id="heatmap_realtime"><a href="<%=basePath%>heatmap/realtime"><span class="am-icon-area-chart"></span> 实时热力图</a></li>
          <li id="heatmap_history"><a href="<%=basePath%>heatmap/history"><span class="am-icon-bar-chart"></span> 历史热力图</a></li>
        </ul>
      </li>
      <li class="admin-parent">
        <a class="am-cf" data-am-collapse="{target: '#collapse-track'}"><span class="am-icon-file"></span> 行人轨迹 <span class="am-icon-angle-right am-fr am-margin-right"></span></a>
        <ul class="am-list am-collapse admin-sidebar-sub am-in" id="collapse-track">
          <li id="track_realtime"><a href="<%=basePath%>track/realtime"><span class="am-icon-line-chart"></span> 实时轨迹</a></li>
          
          <li id="track_playback"><a href="<%=basePath%>track/playback"><span class="am-icon-play"></span> 位置回放</a></li>
        </ul>
      </li>
       -->
    </ul>

    <div class="am-panel am-panel-default admin-sidebar-panel">
      <div class="am-panel-bd">
        <p><span class="am-icon-bookmark"></span> 公告</p>
        <p>暂无内容</p>
      </div>
    </div>
  </div>
  <!-- sidebar end -->