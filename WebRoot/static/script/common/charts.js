/**
 * 图表绘制相关
 * 
 * @author hzy
 */
$(function() {
	
});

var currentCustomerCount = 0;
function getRealtimeCustomerShopData(requestData){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/customer/realtime?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_realtime_customer_shop",
			"data" : JSON.stringify(requestData)
		},
		success : function(result) {
			if (result.status !== 1) {
				clearInterval(chartHandle);
				layer.alert("暂无实时数据", {
					icon : 0
				});
			} else {
				currentCustomerCount = result.data.count;
				var shopRank = result.data.shopRank;
				var count = 0;
				var html = '';
				for(var i in shopRank){
					html+='<tr>';
					html+='<td>'+shopRank[i].name+'</td>';
					html+='<td>'+shopRank[i].floor+'</td>';
					html+='<td>'+shopRank[i].activeNum+'</td>';
					html+='</tr>';
					count++;
				}
				if(count==0){
					html+='<td colspan=3><p align="center">No Available Data</p></td>';
				}
				$('#shop_list table tbody').html(html);
			}
		},
		error : function() {
			clearInterval(chartHandle);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

function getShopList(requestData){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/shop?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_shop_list",
			"data" : JSON.stringify(requestData)
		},
		success : function(result) {
			if (result.status !== 1) {
				layer.alert(result.message, {
					icon : 0
				});
			} else {
				var shopList = result.data.shopList;
				var count = 0;
				var html = '<option value="0" floor="">选择店铺</option>';
				for(var i in shopList){
					html += '<option value=' + shopList[i].id + ' floor="'+shopList[i].floor+'">'
					+ shopList[i].name+' ('+shopList[i].floor+')</option>';
					count++;
				}
				$('.select_shops').html(html);
			}
		},
		error : function() {
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

function showRealtimeCustomerCount() {
	var dom = document.getElementById('realtime_customer_count');
	if(!dom)
		return;
	var myChart = echarts.init(dom);
	var option = {
		tooltip : {
			formatter : "{a} <br/>{b} : {c}"
		},
//		toolbox : {
//			feature : {
//				restore : {},
//				saveAsImage : {}
//			}
//		},
		series : [ {
			name : '实时客流统计',
			type : 'gauge',
			detail : {
				formatter : '{value}'
            },
			data : [ {
				value : 50,
				name : '实时客流量'
			} ],
			min:0,
			max:2500
		} ]
	};
	
	option.series[0].data[0].value = currentCustomerCount;
	myChart.setOption(option, true);
	setInterval(function() {
		option.series[0].data[0].value = currentCustomerCount;
		myChart.setOption(option, true);
	}, 5000);
}

function showDynamicLine(){
	var dom = document.getElementById('realtime_customer_line');
	if(!dom)
		return;
	var myChart = echarts.init(dom);
	function getData() {
	    now = new Date();
	    //value = value + Math.random() * 21 - 10;
	    return {
	        name: now.toString(),
	        value: [
	            //[now.getFullYear(),now.getMonth()+1,now.getDate(),now.getHours(), now.getMinutes(), now.getSeconds()].join('/'),
	            now,
	            currentCustomerCount
	        ]
	    }
	}

	var data = [];
	var now = new Date();
	//var value = 0;
	data.push(getData());

	var option = {
	    title: {
	        text: '实时客流走势',
	        left:"center"
	    },
	    tooltip: {
	        trigger: 'axis',
	        formatter: function (params) {
	            params = params[0];
	            var date = new Date(params.name);
	            return date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds() + '<br>客流量：' + params.value[1];
	        },
	        axisPointer: {
	            animation: false
	        }
	    },
	    xAxis: {
	    	name:"时间",
	        type: 'time',
	        splitLine: {
	            show: false
	        }
	    },
	    yAxis: {
	    	name:"人数",
	        type: 'value',
	        //boundaryGap: [0, '100%'],
	        splitLine: {
	            show: false
	        },
	        min:'dataMin'
	    },
	    series: [{
	        name: '实时数据',
	        type: 'line',
	        showSymbol: false,
	        hoverAnimation: false,
	        data: data,
	    }]
	};
	myChart.setOption(option);
	setInterval(function () {
		if(data.length>7280)
			data.shift();
        data.push(getData());
//	    for (var i = 0; i < 5; i++) {
//	        data.shift();
//	        data.push(randomData());
//	    }

	    myChart.setOption({
	        series: [{
	            data: data
	        }]
	    });
	}, 5000);
}

function statisticRateInShop(requestData) {
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/shop?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_rate_in_shop",
			"data" : JSON.stringify(requestData)
		},
		beforeSend : function() {
			processLayout = layer.load(1, {
				shade : false
			}); // 0代表加载的风格，支持0-2
		},
		success : function(result) {
			layer.close(processLayout);
			if (result.status !== 1) {
				layer.alert("没有检索到数据", {
					icon : 0
				});
			} else {
				var data = result.data;
				console.log(data);
				showRateInShopLine(data);
			}
		},
		error : function() {
			layer.close(processLayout);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

function showRateInShopLine(data){
	var dom = document.getElementById('rateInShopLine');
	var dataX = [];
	var dataY = [];
	for(var i in data){
		dataX.push(data[i].time);
		dataY.push(data[i].rate);
	}
	var myChart = echarts.init(dom);
	var option = {
	    title: {
	        text: '店铺进店率统计',
	        //subtext: '模拟数据',
	        left:"center"
	    },
	    tooltip: {
	        trigger: 'axis'
	    },
	    xAxis:  {
	        type: 'category',
	        boundaryGap: false,
	        data: dataX
	    },
	    yAxis: {
	        type: 'value',
	        axisLabel: {
	            formatter: '{value} %'
	        }
	    },
	    series: [
	        {
	            name:'进店率',
	            type:'line',
	            data:dataY,
	            markPoint: {
	                data: [
	                    {type: 'max', name: '最大值',formatter: '{value} %'},
	                    {type: 'min', name: '最小值',formatter: '{value} %'}
	                ]
	            },
	            markLine: {
	                data: [
	                    {type: 'average', name: '平均值'}
	                ]
	            }
	        }
	    ]
	};
	myChart.setOption(option, true);
}

//统计历史客流走势
function statisticHistoryFlow(requestData) {
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/customer/history?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_customer_flow_history",
			"data" : JSON.stringify(requestData)
		},
		beforeSend : function() {
			processLayout = layer.load(1, {
				shade : false
			}); // 0代表加载的风格，支持0-2
		},
		success : function(result) {
			layer.close(processLayout);
			if (result.status !== 1) {
				layer.alert(result.message, {
					icon : 0
				});
			} else {
				var flow = result.data;
				showHistoryFlowLine(flow);
			}
		},
		error : function() {
			layer.close(processLayout);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

//统计驻留时间
function statisticStayTimeInShop(requestData){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/shop?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_staytime_in_shop",
			"data" : JSON.stringify(requestData)
		},
		beforeSend : function() {
			processLayout = layer.load(1, {
				shade : false
			}); // 0代表加载的风格，支持0-2
		},
		success : function(result) {
			layer.close(processLayout);
			if (result.status !== 1) {
				layer.alert(result.message, {
					icon : 0
				});
			} else {
				var data = result.data.typeNumList.split(",");
				showStayTimeInShopBar(data);
			}
		},
		error : function() {
			layer.close(processLayout);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

function showStayTimeInShopBar(data) {
	var dataY = data;
	var dom = document.getElementById('stayTimeInShopBar');
	var myChart = echarts.init(dom);
	var option = {
		title: {
	        text: '店铺驻留时间统计',
	        //subtext: '模拟数据',
	        left:"center"
	    },
	    color: ['#3398DB'],
	    tooltip : {
	        trigger: 'axis',
	        axisPointer : {            // 坐标轴指示器，坐标轴触发有效
	            type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
	        }
	    },
	    grid: {
	        left: '3%',
	        right: '4%',
	        bottom: '3%',
	        containLabel: true
	    },
	    xAxis : [
	        {
	            type : 'category',
	            data : ['0-5分钟','5-10分钟','10-20分钟','20-30分钟','30-60分钟','>60分钟'],
	            axisTick: {
	                alignWithLabel: true
	            }
	        }
	    ],
	    yAxis : [
	        {
	            type : 'value'
	        }
	    ],
	    series : [
	        {
	            name:'人数',
	            type:'bar',
	            barWidth: '60%',
	            data:dataY
	        }
	    ]
	};
	myChart.setOption(option, true);
}

function showHistoryFlowLine(data){
	var dom = document.getElementById('flow_line');
	var dataX = [];
	var dataY = [];
	for(var i in data){
		dataX.push(data[i].time);
		dataY.push(data[i].count);
	}
	var myChart = echarts.init(dom);
	var option = {
	    title: {
	        text: '历史客流统计',
	        //subtext: '模拟数据',
	        left:"center"
	    },
	    tooltip: {
	        trigger: 'axis'
	    },
//	    dataZoom: [{
//            startValue: '2014-06-01'
//        }, {
//            type: 'inside'
//        }],
	    xAxis:  {
	        data: dataX
	    },
	    yAxis: {
	        type: 'value',
	        axisLabel: {
	            formatter: '{value}'
	        }
	    },
	    series: [
	        {
	            name:'客流量',
	            type:'line',
	            data:dataY,
	            markPoint: {
	                data: [
	                    {type: 'max', name: '最大值'},
	                    {type: 'min', name: '最小值'}
	                ]
	            },
	            markLine: {
	                data: [
	                    {type: 'average', name: '平均值'}
	                ]
	            }
	        }
	    ]
	};
	myChart.setOption(option, true);
}

function getHistoryShopRank(requestData){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/shop?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_shop_rank",
			"data" : JSON.stringify(requestData)
		},
		beforeSend : function() {
			processLayout = layer.load(1, {
				shade : false
			}); // 0代表加载的风格，支持0-2
		},
		success : function(result) {
			layer.close(processLayout);
			if (result.status !== 1) {
				layer.alert(result.message, {
					icon : 0
				});
			} else {
				var shopRank = result.data;
				console.log(shopRank.length);
				var count = 0;
				var html = '';
				for(var i in shopRank){
					html+='<tr>';
					html+='<td>'+shopRank[i].name+'</td>';
					html+='<td>'+shopRank[i].floor+'</td>';
					html+='<td>'+shopRank[i].rate+'</td>';
					html+='<td>'+shopRank[i].stayTime+'</td>';
					html+='<td>'+shopRank[i].flowSize+'</td>';
					html+='</tr>';
					count++;
				}
				if(count==0){
					html+='<td colspan=5><p align="center">No Available Data</p></td>';
				}
				$('#shopRank tbody').html(html);
			}
		},
		error : function() {
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}