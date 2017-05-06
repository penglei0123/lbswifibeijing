<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<% 
if(user.equals("wifibeijing")){
%>
<!-- nothing-->
<%
}else{
%>
<footer>
  <hr>
   <p align="center">中科劲点(北京)科技有限公司  版权所有  地址：北京市海淀区科学院南路10号(灵智楼)   京ICP备15047372号-1</p>
</footer>
<%
}
%>
<!--[if lt IE 9]>
<script src="http://libs.baidu.com/jquery/1.11.1/jquery.min.js"></script>
<script src="http://cdn.staticfile.org/modernizr/2.8.3/modernizr.js"></script>
<script src="<%=basePath%>static/assets/js/polyfill/rem.min.js"></script>
<script src="<%=basePath%>static/assets/js/polyfill/respond.min.js"></script>
<script src="<%=basePath%>static/assets/js/amazeui.legacy.js"></script>
<![endif]-->

<!--[if (gte IE 9)|!(IE)]><!-->
<script src="<%=basePath%>static/assets/js/jquery.min.js"></script>
<script src="<%=basePath%>static/assets/js/amazeui.min.js"></script>
<!--<![endif]-->
<script src="<%=basePath%>static/assets/js/app.js"></script>
<script src="<%=basePath%>/static/script/layer2/layer/layer.js"></script>
<!-- layer扩展插件 -->
<script src="<%=basePath%>/static/script/layer2/layer/extend/layer.ext.js"></script>
<!-- 日期选择插件 -->
<script src="<%=basePath%>/static/script/laydate/laydate.js"></script>

<script src="<%=basePath%>/static/script/common/common.js"></script>

<!-- 可视化插件 -->
<script src="<%=basePath%>/static/script/d3/d3.min.js"></script>
<script src="<%=basePath%>/static/script/echarts/echarts.min.js"></script>

<script src="<%=basePath%>/static/script/common/table_sort.js"></script>
</body>
</html>