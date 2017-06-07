/**
 * 前端公用JS脚本
 * @author hzy
 */
/**
 * JQUERY事件绑定和页面加载完毕后的初始化
 */
var processLayout = null;
var handle = null;
var imgMarker = new Image();
var imgBG = new Image();
$(function(){
	var timeStart = new Date().getTime()-48*60*60*1000;
	//timeStart = 1470569400*1000;
	var timeEnd = new Date().getTime()-24*60*60*1000;
	//timeEnd = 1470570000*1000;
	$('.custom_duration #time_start').each(function(){
		if($(this).attr('showTime')=="false"){
			$(this).val(format2Date(timeStart));
		}else{
			$(this).val(format2Time(timeStart));
		}
	});
	$('.custom_duration #time_end').each(function(){
		if($(this).attr('showTime')=="false"){
			$(this).val(format2Date(timeEnd));
		}else{
			$(this).val(format2Time(timeEnd));
		}
	});

	// 解析生成楼层列表
	generateFloorSelectItems();
	$('#btnPlusMap').click(function(){
		if (canvasScale < 5)
			canvasScale += 0.2;
		$('#btnChangePersonsFloor').click();
	});

	$('#btnMinusMap').click(function(){
		if (canvasScale > 0.2)
			canvasScale -= 0.2;
		$('#btnChangePersonsFloor').click();
	});
	
	$('.select_duration').change(function(){
		if($(this).children('option:selected').val()=='-1'){
			$(this).next(".custom_duration").show();
			$(this).next('#time_start').val('');
			$(this).next('#time_end').val('');
		}else{
			$(this).next(".custom_duration").hide();
		}
	});

	$('#select_user').change(function(){
	//	macData = [];
		if($(this).children('option:selected').val()=='-1'){
			$("#user-mac").show();
	    	$('#user-mac').val('');
		}else{
			$("#user-mac").hide();
//			inputMac = '';
		}
	});
	
	
		
	switch (page){
	case 'person_realtime':
		var data = {
			"building" : building,
			"floor" : $('#select_floor option:selected').val()
		};
		getAllPersonRealtimePosition(data);
		handle = setInterval(function(){
			getAllPersonRealtimePosition(data);
		}, 2000);
		break;
	case 'heatmap_realtime':
		$('#btnSearchRealtimeHeatmap').show();
		$('#select_duration').hide();
		$('#btnSearchHistoryHeatmap').hide();
		var data = {
			"building" : building,
			"floor" : $('#select_floor option:selected').val()
		};
		// 取消打开页面即请求内容功能
// getRealtimeHeatmap(data);
// handle = setInterval(function() {
// //getRealtimeHeatmap(data);
// }, 2000);
		break;
	case 'heatmap_history':
		$('#btnSearchRealtimeHeatmap').hide();
		$('#select_duration').show();
		$('#btnSearchHistoryHeatmap').show();
		$(".custom_duration").hide();
		var data = {
			"duration" : 600,
			"building" : building,
			"floor" : $('#select_floor option:selected').val()
		};
		// 取消打开页面即请求内容功能
		// getHistoryHeatmap(data);
		break;
	case 'track_realtime':
		$('#select_user').show();
		$('#user-mac').hide();
		$('#select_duration').hide();
		$(".custom_duration").hide();
		$('.track_list').hide();
		$('#btnSearchTrackTrend').hide();
		var data = {
			"building" : building
		};
		getAllOnlineUsers(data);
		break;
	case 'track_history':
		$('#select_user').hide();
		$('#user-mac').show();
		$('#select_duration').show();
		$(".custom_duration").hide();
		var data = {
			"building" : building
		};
		// getAllOfflineUsers(data);
		break;
	case 'track_playback':
		$(".custom_duration").hide();
		$('#btnAbortTrackPlayback').attr("disabled","disabled");
		break;
	}
});

var floorList = [];
// 根据服务器端配置生成楼层select选项
function generateFloorSelectItems(){
	var arr = floorConfig.split(",");
	var html = '<option value="0">选择楼层</option>';
	var index = 0;
	for ( var i in arr){
		if (arr[i].indexOf("-") > 0){
			var prefix = arr[i].charAt(0);
			var arr1 = arr[i].split("-");
			var start = parseInt(arr1[0].charAt(1));
			var end = parseInt(arr1[1].charAt(1));
			for (var j = start; j <= end; j++){
				floorList.push(prefix + j);
				if (index == 0){
					html += '<option value="' + prefix + j + '" selected>'
							+ prefix + j + '</option>';
				} else {
					html += '<option value="' + prefix + j + '">' + prefix + j
							+ '</option>';
				}
				index++;
			}
		} else {
			floorList.push(arr[i]);
			if (index == 0){
				html += '<option value="' + arr[i] + '" selected>' + arr[i]
						+ '</option>';
			} else {
				html += '<option value="' + arr[i] + '">' + arr[i]
						+ '</option>';
			}
			index++;
		}
	}
	$('#select_floor').html(html);
}

var heatmapHandles = [];
// 首页加载所有楼层的热力图
function loadAllHeatmap(){
	var totalFloorCount = floorList.length;
	if(totalFloorCount>0){
		loadOneFloorMapByRecursion(floorList[0],totalFloorCount,totalFloorCount-1);
	}
}
var mapScale = {};
//递归方式加载各楼层地图（应对图片异步加载机制）
function loadOneFloorMapByRecursion(curFloor,totalFloorCount,remainFloorCount){
	var img = new Image();
	img.src = basePath + "/static/map/" + building + "_"+curFloor + ".jpg";// 设置地图
	var data = {
		"building" : building,
		"floor" :curFloor
	};
	heatmapHandles[curFloor] =  null;
	img.onload = function(){
		var width = img.width;
		var height = img.height;
		var maxWidth = $('.heatmap_list').width();
		if(width>maxWidth){
			mapScale[curFloor] = maxWidth*1.0/width;
		}else{
			mapScale[curFloor] = 1.0;
		}
		width *= mapScale[curFloor];
		height *= mapScale[curFloor];
		$("#heatmap_"+curFloor).css("background","url("+img.src+") no-repeat");
		$("#heatmap_"+curFloor).css("width",width);
		$("#heatmap_"+curFloor).css("height",height);
		//关键设置，不然图片不会等比例缩放
		$("#heatmap_"+curFloor).css("background-size","contain");
		getRealtimeHeatmap(data,img,"heatmap_"+curFloor);
		clearInterval(heatmapHandles[curFloor]);
		heatmapHandles[curFloor] = setInterval(function(){
			getRealtimeHeatmap(data,img,"heatmap_"+curFloor);
		}, 2000);
		if(remainFloorCount>0){
			var index = totalFloorCount-remainFloorCount;
			loadOneFloorMapByRecursion(floorList[index],totalFloorCount,remainFloorCount-1);
		}
	}
}

var currentCustomerCount = 0;
var canvasScale = 1.0;
function getAllPersonRealtimePosition(requestData){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/customer/realtime?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_person_realtime",
			"data" : JSON.stringify(requestData)
		},
		success : function(result){
			if (result.status !== 1){
				clearInterval(handle);
				layer.alert(result.message, {
					icon : 0
				});
			} else {	
				var userList = result.data;
				var flag = false;
				var setSelected = false;
				$('#select_mac').html('');
				var html = '<option value="0">选择用户</option>'+'<option value="-1">输入MAC</option>';
				for ( var i in userList){	
					if (userList[i].mac == selectedValue){
						html += '<option value="' + userList[i].mac
								+ '" selected>' + userList[i].mac
								+ '</option>';
						flag = true;
					}else{
						html += '<option value="' + userList[i].mac
								+ '">' + userList[i].mac + '</option>';
					}
				}		
				$('#select_mac').html(html);		
				$("#select_mac").find("option[value='"+selectedValue+"']").attr("selected",true);
				var data = result.data;
	            var canvas = document.getElementById('canvas');
	        	var context = canvas.getContext('2d');
	        	var canvasbuffer = document.createElement('canvas');
	        	var contextbuffer = canvasbuffer.getContext('2d');
	        	imgBG.src = basePath + "/static/map/" + building + "_"
	        	+ requestData.floor + ".jpg"// 设置地图
	        	// context.clearRect(0,0,context.width,context.height);//清除整个画布
	        	imgBG.onload = function(){
	        		canvasbuffer.width = imgBG.width * canvasScale;
	        		canvasbuffer.height = imgBG.height * canvasScale;
	        		canvas.width = imgBG.width * canvasScale;
	        		canvas.height = imgBG.height * canvasScale;
	        		context.drawImage(imgBG, 0, 0, imgBG.width * canvasScale,
	        				imgBG.height * canvasScale);
	        		var imgMarkerCur =new Image();
	        		imgMarkerCur.src = basePath + "/static/images/maker_cur.png"// 设置覆盖物	
	        		imgMarker.src = basePath + "/static/images/marker.png"// 设置覆盖物			
	        		imgMarker.onload = function(){
	        //			var index = 1;
	        			var posX=0,posY=0;                      
	        			if (macData == '') {
	        				for(var i in data){
		        				contextbuffer.drawImage(imgMarker,data[i].corx * canvasScale,data[i].cory * canvasScale);
		        			}
		        			context.drawImage(canvasbuffer, 0, 0);
						}else {
						    for(var j in macData){
						   // 	console.log(macData[j]);
						    	for(var i in data){
						    		 if(macData[j] != data[i].mac){
						    			 contextbuffer.drawImage(imgMarker,data[i].corx * canvasScale,data[i].cory * canvasScale);
						    		 }else{
						    			 posX = data[i].corx * canvasScale;
						    			 posY = data[i].cory * canvasScale;
						    			 console.log(posX+","+posY);
						    		 }
						    	 }	
						    	 if(posX != 0){
						    		 contextbuffer.drawImage(imgMarkerCur, posX,posY);
						    		 context.drawImage(canvasbuffer, 0, 0);
						    	
						    	 }else{
						    		 context.drawImage(canvasbuffer, 0, 0);
						    	 }		
						    }	 					   
						}      				        			
	        			}										
	        		}
			}
		},
		error : function(){
			clearInterval(handle);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});

		}
	});
}

/**
 * 
 * @param requestData
 * @param img
 * @param container
 *            容器ID
 */
function getRealtimeHeatmap(requestData,img,containerId){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/heatmap/realtime?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_heatmap_realtime",
			"data" : JSON.stringify(requestData)
		},
		success : function(result){
			if (result.status !== 1){
				clearInterval(heatmapHandles[requestData.floor]);
				layer.alert("暂无实时数据", {
					icon : 0
				});
			} else {
				//更新热力图时不需要重新加载地图
				//$('#'+containerId).css("background","url("+img.src+") no-repeat");
				//$('#'+containerId).css("width",img.width*mapScale[requestData.floor]);
				//$('#'+containerId).css("height",img.height*mapScale[requestData.floor]);
				//$("#heatmap_"+curFloor).css("background-size","contain");
				//$('#'+containerId).html('<p>'+requestData.floor+'</p>');
				$('#'+containerId).empty();
				var heatmap = h337.create({
					container : document.getElementById(containerId)
				});
				var data = result.data;
				var count = 0;
				var heatmapData = [];
				for ( var i in data){
					heatmapData.push({
						x : parseInt(data[i].corx*mapScale[requestData.floor]),
						y : parseInt(data[i].cory*mapScale[requestData.floor]),
						value : Math.random() * 20,
						radius : 100*mapScale[requestData.floor]
					});
					count++;
				}
				heatmap.setData({
					max : count,
					data : heatmapData
				});
			}
		},
		error : function(){
			clearInterval(heatmapHandles[requestData.floor]);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

function getHistoryHeatmap(requestData,img){
	requestData['building'] = building;
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/heatmap/history?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_heatmap_history",
			"data" : JSON.stringify(requestData)
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
			} else {
				$('#heatmap').css("background","url("+img.src+") no-repeat");
				$('#heatmap').css("width",img.width);
				$('#heatmap').css("height",img.height);
				$('#heatmap').html('');
				var heatmap = h337.create({
					container : document.getElementById('heatmap')
				});
				var data = result.data;
				console.log(data.length);
				var count = 0;
				var heatmapData = [];
				for ( var i in data){
					var value = data[i].value;
					// value = parseInt(Math.sqrt(value));
					heatmapData.push({
						x : data[i].x,
						y : data[i].y,
						value : value,
						radius : 100
					});
					// heatmap.addData({x:data[i].x,y:data[i].y,value:data[i].value,radius:100});
					count++;
				}
				heatmap.setData({
					max : count,
					data : heatmapData
				});
			}
		},
		error : function(){
			layer.close(processLayout);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

function getAllOnlineUsers(requestData){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/track/realtime?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_all_online_user",
			"data" : JSON.stringify(requestData)
		},
		success : function(result){
			if (result.status !== 1){
				layer.alert(result.message, {
					icon : 0
				});
			} else {
				var html = '<option value="0">选择用户</option>';
				var data = result.data;
				for ( var i in data){
					html += '<option value="' + data[i].mac + '">'
							+ data[i].mac + '</option>';
				}
				$('#select_user').html(html);
			}
		},
		error : function(){
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

// 获得历史记录中所有用户的MAC
function getAllOfflineUsers(requestData){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/track/history?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_all_offline_user",
			"data" : JSON.stringify(requestData)
		},
		success : function(result){
			if (result.status !== 1){
				layer.alert(result.message, {
					icon : 0
				});
			} else {
				var html = '<option value="0">选择用户</option>';
				var data = result.data;
				console.log(data.length);
				for ( var i in data){
					html += '<option value="' + data[i] + '">'
							+ data[i] + '</option>';
				}
				$('#select_user').html(html);
			}
		},
		error : function(){
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

function getUserRealtimeTrack(requestData,img){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/track/realtime?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_track_realtime",
			"data" : JSON.stringify(requestData)
		},
		success : function(result){
			var html = '<option value="0">选择用户</option>';
			if (result.status !== 1){
				clearInterval(handle);
				layer.alert(result.message, {
					icon : 0
				});
			} else {
				$('#floormap').css("background","url("+img.src+") no-repeat");
				$('#floormap').css("width",img.width);
				$('#floormap').css("height",img.height);
				var data = result.data;
				var canvas = document.getElementById('canvas');
				$('#canvas').css("width",img.width);
				$('#canvas').css("height",img.height);
				var context = canvas.getContext('2d');
				context.clearRect(0, 0, img.width, img.height);// 清除整个画布
				for ( var i in data){
					context.beginPath();
					context.arc(data[i].corx, data[i].cory, 5, 0,
							2 * Math.PI);
					context.closePath();
					// /填充颜色
					context.fillStyle = '#5f26c9';
					// 填充图形
					context.fill();
				}
				context.lineWidth = 1;
				context.lineCap = "round";
				context.strokeStyle = '#f00';
				context.beginPath();
				for ( var i in data){
					context.lineTo(data[i].corx, data[i].cory);
					context.stroke();
				}
				// 注意，在stroke方法之前调用该方法，会将路径进行闭合，如果不需要闭合，则在之后调用
				context.closePath();
				// 更新在线列表
				var userList = result.userList;
				for ( var i in userList){
					if (userList[i].mac == $(
							'#select_user option:selected').val()){
						html += '<option value="' + userList[i].mac
								+ '" selected>' + userList[i].mac
								+ '</option>';
					} else {
						html += '<option value="' + userList[i].mac
								+ '">' + userList[i].mac + '</option>';
					}
				}
			}
			$('#select_user').html(html);
		},
		error : function(){
			clearInterval(handle);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});

		}
	});
}

function getTopKMAC(requestData){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/track/history?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_topk_mac",
			"data" : JSON.stringify(requestData)
		},
		success : function(result){
			if (result.status !== 1){
				layer.alert(result.message, {icon : 0});
			} else {
				var data = result.data;
				var html = '<option value="0">选择用户</option><option value="-1">输入MAC</option>';
				for(var i in data){
					html+='<option value="'+data[i]+'">'+data[i].substring(0,9)+'**:**:**</option>';
				}
				$('#select_user').html(html);
			}
		},
		error : function(){
			layer.close(processLayout);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

function getUserHistoryTrack(requestData){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/track/history?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_track_history",
			"data" : JSON.stringify(requestData)
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
			} else {
				var data = result.data;
				var img = new Image();
				img.src = basePath + "/static/map/" + requestData.building + "_"+requestData.floor + ".jpg";// 设置地图
				if(img.complete){
					//showHeatMapAndTrackData(requestData,data,img);//带热力图背景的轨迹
					showTrackDataNoHeatmap(data,img,requestData.floor);//不带热力图背景的轨迹
				}else{
					img.onload = function(){
						//showHeatMapAndTrackData(requestData,data,img);//带热力图背景的轨迹
						showTrackDataNoHeatmap(data,img,requestData.floor);//不带热力图背景的轨迹
					}
				}
			}
		},
		error : function(){
			layer.close(processLayout);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

function getTrackPlayback(requestData,playSpeed){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/track/history?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_track_trend",
			// "action":"get_heatmap_trend",
			"data" : JSON.stringify(requestData)
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
			} else {
				var data = result.data;
				// alert(data.length);
				var img = new Image();
				img.src = basePath + "/static/map/" + building + "_"+requestData['floor']+".jpg";// 设置地图
				if(img.complete){
					showTrackPlayback(data,img,playSpeed);
				}else{
					img.onload = function(){
						showTrackPlayback(data,img,playSpeed);
					}
				}
			}
		},
		error : function(){
			layer.close(processLayout);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}


function showHeatMapAndTrackData(requestData,trackData,img){
	$('#floormap').html('');
	$('#floormap').css("background","url("+img.src+") no-repeat");
	$('#floormap').css("width",img.width);
	$('#floormap').css("height",img.height);

	// 先绘制热力图背景
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/heatmap/history?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_heatmap_history",
			"data" : JSON.stringify(requestData)
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
			} else {
				var heatmap = h337.create({
					container : document.getElementById('floormap')
				});
				var data = result.data;
				var count = 0;
				var heatmapData = [];
				for ( var i in data){
					var value = data[i].value;
					heatmapData.push({
						x : data[i].x,
						y : data[i].y,
						value : value,
						radius : 70
					});
					// heatmap.addData({x:data[i].x,y:data[i].y,value:data[i].value,radius:100});
					count++;
				}
				heatmap.setData({
					max : count,
					data : heatmapData
				});
				showTrack(trackData,img);
				// var drawHandle = setInterval(function(){
				// if(total>=1000 || total>=data.length){
				// clearInterval(drawHandle);
				// //alert("draw finish,point count:"+total);
				// return;
				// }
				// drawTrack(data,total,img,context);
				// //同步绘制
				// //syncDrawTrack(data[total],img,context);
				// total++;
				// },1000);
			}
		},
		error : function(){
			layer.close(processLayout);
			layer.alert("网络繁忙，请稍后再试！", {
				icon : 0
			});
		}
	});
}

//初始化轨迹画布,调用绘图方法
function showTrackDataNoHeatmap(trackData,img,floor){
	//加载地图
	$('#floormap').html('');
	$('#floormap').css("background","url("+img.src+") no-repeat");		
	$('#floormap').css("width",img.width* canvasScale);
	$('#floormap').css("height",img.height* canvasScale);
	//关键设置，不然图片不会等比例缩放
	$("#floormap").css("background-size","contain");
	$('#floormap').append('<canvas id="canvas" style="position: absolute; z-index: 999"></canvas>');
	/*$('#canvas').attr("width",img.width* canvasScale-50);
	$('#canvas').attr("height",img.height* canvasScale-50);*/
	$('#canvas').attr("width",img.width* canvasScale);
	$('#canvas').attr("height",img.height* canvasScale);
	// 绘制完热力图背景后再绘点
	var total = 0;
	//点校正和路径补充（当有路径数据时）	
	for(var i in trackData){
		if(mapPaths.hasOwnProperty(floor)){
			console.log("input length:"+trackData[i].length);
			//trackData[i] = updateTrackPoints(trackData[i],floor);
			trackData[i] = updateTrackPointsWidthOriginPoints(trackData[i],floor);
		}
	}
	// drawPoints(data,41,img);
	var canvas = document.getElementById('canvas');
	var context = canvas.getContext('2d');
	// 先清除画布
	context.clearRect(0, 0, img.width* canvasScale, img.height* canvasScale);// 清除整个画布
	resetDrawStatus();
	/**
	 * 延迟绘制（递归调用）
	 */
	drawTrack(trackData,0,img,context);
	/**
	 * 批量一次性绘制(存在性能问题，当点数超过5K时，绘制大量点和线条会导致浏览器卡死
	 */
//	var m_canvas = document.createElement('canvas');  
//	m_canvas.width = canvas.width;  
//	m_canvas.height = canvas.height;  
//	var m_context = m_canvas.getContext('2d');
//	for(var i in trackData){
//		drawTrackNoDelay(trackData,i,img,m_context);
//	}
//	context.drawImage(m_canvas,0,0);
	
}

//真正绘轨迹部分
/**
 * 延迟绘制，体现点移动的效果
 */
var drawSpeed = 100;
var drawHandle;

function drawTrack(groupData,groupIndex,img,context){
	clearInterval(drawHandle);
	var index = 0;
	var p = index;
	var step = 0;
	
	 drawHandle = setInterval(function(){
		var data = groupData[groupIndex];
		if(abortDraw){
			//突出起点
			context.fillStyle="#76EE00";
			context.beginPath();
			context.arc(data[0].x* canvasScale, data[0].y*canvasScale,10*canvasScale, 0,2 * Math.PI);
			context.closePath();
			context.fill();
			//突出终点
		    context.fillStyle="red";
		    context.beginPath();
		    context.arc(data[index-1].x * canvasScale, data[index-1].y* canvasScale,10*canvasScale, 0,2 * Math.PI);
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
			context.arc(data[0].x* canvasScale, data[0].y* canvasScale, 10 *canvasScale, 0,2 * Math.PI);
			context.closePath();
			context.fill();
			//突出终点
		    context.fillStyle="red";
		    context.beginPath();
		    context.arc(data[index-1].x* canvasScale, data[index-1].y* canvasScale, 10*canvasScale, 0,2 * Math.PI);
		    context.closePath();
		    context.fill();
		    
			clearInterval(drawHandle);
			groupIndex++;
			// 本条线路绘制完毕，开始下一条路径绘制
		//	console.log(groupIndex+","+groupData.length)
			if(groupIndex<groupData.length){
				drawTrack(groupData,groupIndex,img,context);
			}
			else{
				layer.alert("绘制完毕！", {icon : 1});
			}
			return;
		}
		//console.log(index);
		if(index>0){
			var line = new Line(data[index-1].x* canvasScale,data[index-1].y* canvasScale,data[index].x* canvasScale,data[index].y* canvasScale);
			line.drawWithArrowheads(context);
		}
		index++;
	//	console.log(index)
	},drawSpeed);
}

/**
 * 一次性绘制
 */
function drawTrackNoDelay(groupData,groupIndex,img,context){
	var index = 0;
	var p = index;
	var step = 0;
	var data = groupData[groupIndex];
	var radius = 2;
	context.fillStyle = '#f00';//#FFF68F
	context.lineCap = "round";
	context.strokeStyle = '#f00';
	context.lineWidth = 1;
	for(var i in data){
		//画点
		context.arc(data[index].x, data[index].y, radius, 0, 2 * Math.PI);
		context.fill();
		//画线
		if(index>0){
			context.beginPath();
			context.moveTo(data[index-step].x, data[index-step].y);
			context.lineTo(data[index].x, data[index].y);
			context.stroke();
			context.closePath();
		}else{
			context.beginPath();
			context.moveTo(data[0].x, data[0].y);
			context.closePath();
		}
		// 随机跳跃打点
		step = parseInt(Math.random()*10);
		step=1;//不随机跳跃
		index+=step;
	}	
}

/**
 * 根据路径数据对定位点进行校正和补充
 * 不含原始点
 */
function updateTrackPoints(points,floor){
	//抽取当前楼层的路径数据点数据
	var paths = mapPaths[floor];
	var nodes = mapNodes[floor];
	//定位点纠正
	var index = 0;
	var lastNodeId=null;
	var newPoints = [];
	var pathCache={};//使用hash表保存已经计算出路径的点
	for(var k in points){
		var minDis = 0xfffffff;
		var curNodeId=null;
		for(var j in nodes){
			var dis = distance(points[k].x,points[k].y,nodes[j].x,nodes[j].y);
			if(dis<minDis){
				minDis = dis;
				curNodeId = j;
			}
		}
		//从第二个点开始，计算当前点与前一个点之间的最短路径并把中间节点加入待绘制数组
		if(index>0){
			lastNodeId = parseInt(lastNodeId);
			curNodeId = parseInt(curNodeId);
			//console.log(lastNodeId+"\t"+curNodeId);
			if(lastNodeId==curNodeId){
				var size = newPoints.length;
				if(size==0){
					newPoints.push(nodes[curNodeId]);
				}else if(size>0 && nodes[curNodeId]!=newPoints[size-1]){
					newPoints.push(nodes[curNodeId]);
				}
			}else{
				var key1 = lastNodeId+"_"+curNodeId;
				var key2 = curNodeId+"_"+lastNodeId;
				
				if(pathCache.hasOwnProperty(key1)){
					mergePoints(newPoints,pathCache[key1],nodes);
				}else if(pathCache.hasOwnProperty(key2)){
					//注意需要反转数组顺序
					mergePoints(newPoints,pathCache[key2].slice(0).reverse(),nodes);
				}else{
					var nodeSize = Object.keys(nodes).length;
					var matrix = new Array(nodeSize+1);
					for (var i = 1; i <= nodeSize; i++){
						matrix[i]=new Array(nodeSize+1);
						for (var j = 1; j <= nodeSize; j++){
							matrix[i][j] = 0x7fffffff;
						}
					}
					for (var j in paths){
						matrix[paths[j].pathS][paths[j].pathE] = paths[j].pathLength;
						matrix[paths[j].pathE][paths[j].pathS] = paths[j].pathLength;
					}
					var shortPath = new ShortestPath(matrix,nodeSize);
					var w = shortPath.evaluate(lastNodeId,curNodeId);
					if(w==0x7fffffff){
						lastNodeId = curNodeId;
						continue;
					}
//					if(index>=39){
//						console.log("nodeSizes7s:"+nodeSize+",index:"+index+","+lastNodeId+","+curNodeId+",w:"+w);
//						break;
//					}
					var list = shortPath.getPathTrail(lastNodeId,curNodeId);
					//console.log(shortPath.getPathTrailWithLength(lastNodeId,curNodeId));
					
					mergePoints(newPoints,list,nodes);
					//更新路径缓存
					var key = lastNodeId+"_"+curNodeId;
					pathCache[key]=list;
				}
			}
		}else{
			newPoints.push(nodes[curNodeId]);
		}
		index++;
		lastNodeId = curNodeId;
	}
	return newPoints;
}

/**
 * 包含校正点和原始点
 * @param points
 * @param floor
 * @returns {Array}
 */
function updateTrackPointsWidthOriginPoints(points,floor){
	//抽取当前楼层的路径数据点数据
	var paths = mapPaths[floor];
	var nodes = mapNodes[floor];
	var forbiddenRegions = null;
	var banRegionsExist=false;
	if(mapRegions.hasOwnProperty(floor)){
		banRegionsExist = true;
		forbiddenRegions = mapRegions[floor];
	}
	//定位点纠正
	var index = 0;
	var lastNodeId=null;
	var newPoints = [];
	var pathCache={};//使用hash表保存已经计算出路径的点
	for(var k in points){
		//先根据点与多边形位置关系判断当前点是否在禁止区域
		var needAdjust = false;
		if(banRegionsExist){
			for(var region in forbiddenRegions){
				if(pointInPloygon(points[k].x,points[k].y,forbiddenRegions[region].path)){
					needAdjust=true;
					break;
				}
			}
		}
		var minDis = 0xfffffff;
		var curNodeId=null;
		for(var j in nodes){
			var dis = distance(points[k].x,points[k].y,nodes[j].x,nodes[j].y);
			if(dis<minDis){
				minDis = dis;
				curNodeId = j;
			}
		}
		//如果在禁止区域则对该点进行调整
		if(needAdjust){
			points[k] = nodes[curNodeId];
		}
		//从第二个点开始，计算当前点与前一个点之间的最短路径并把中间节点加入待绘制数组
		if(index>0){
			lastNodeId = parseInt(lastNodeId);
			curNodeId = parseInt(curNodeId);
			//console.log(lastNodeId+"\t"+curNodeId);
			if(lastNodeId==curNodeId){
				newPoints.push(nodes[curNodeId]);
			}else{
				var key1 = lastNodeId+"_"+curNodeId;
				var key2 = curNodeId+"_"+lastNodeId;
				if(pathCache.hasOwnProperty(key1)){
					mergePoints(newPoints,pathCache[key1],nodes);
				}else if(pathCache.hasOwnProperty(key2)){
					//注意需要反转数组顺序
					mergePoints(newPoints,pathCache[key2].slice(0).reverse(),nodes);
				}else{
					var nodeSize = Object.keys(nodes).length;
					var matrix = new Array(nodeSize+1);
					for (var i = 1; i <= nodeSize; i++){
						matrix[i]=new Array(nodeSize+1);
						for (var j = 1; j <= nodeSize; j++){
							matrix[i][j] = 0x7fffffff;
						}
					}
					for (var j in paths){
						matrix[paths[j].pathS][paths[j].pathE] = paths[j].pathLength;
						matrix[paths[j].pathE][paths[j].pathS] = paths[j].pathLength;
					}
					var shortPath = new ShortestPath(matrix,nodeSize);
					var w = shortPath.evaluate(lastNodeId,curNodeId);
					if(w==0x7fffffff){
						lastNodeId = curNodeId;
						continue;
					}
					var list = shortPath.getPathTrail(lastNodeId,curNodeId);
					//console.log(shortPath.getPathTrailWithLength(lastNodeId,curNodeId));
					mergePoints(newPoints,list,nodes);
					//更新路径缓存
					var key = lastNodeId+"_"+curNodeId;
					pathCache[key]=list;
				}
			}
		}
		newPoints.push(points[k]);
		index++;
		lastNodeId = curNodeId;
	}
	return newPoints;
}
/**
 * 补充过渡节点到坐标点数组
 * @param newPoints 待更新的坐标点数组
 * @param list 两点之间的补充点数组
 * @param nodes 所有节点数组
 */
function mergePoints(newPoints,list,nodes){
	var size = 0;
	for(var node in list){
		//连续两个点在同一位置则不添加
		size = newPoints.length;
		if(size==0){
			newPoints.push(nodes[list[node]]);
		}else if(size>0 && nodes[list[node]]!=newPoints[size-1]){
			newPoints.push(nodes[list[node]]);
		}
		//console.log("nodeId:"+list[node]+"("+nodes[list[node]].x+","+nodes[list[node]].y+")");
	}
}

function distance(x1,y1,x2,y2){
	var ax = x2-x1;
	var by = y2-y1;
	return Math.pow((ax *ax + by * by), 0.5);	
}

var abortDraw = false;
function showTrackPlayback(data,img,playSpeed){
	$('#trackWithHeatmap').html('');
	$('#trackWithHeatmap').css("background","url("+img.src+") no-repeat");
	$('#trackWithHeatmap').css("width",img.width);
	$('#trackWithHeatmap').css("height",img.height);
	// $('#trackWithHeatmap').html('');
	$('#btnSearchTrackPlayback').attr("disabled","disabled");
	$('#btnAbortTrackPlayback').removeAttr("disabled");
	var total = 0;
	abortDraw = false;
	var drawHandle = setInterval(function(){
		if(total>=data.length || abortDraw){
			clearInterval(drawHandle);
			$('#btnSearchTrackPlayback').removeAttr("disabled");
			$('#btnAbortTrackPlayback').attr("disabled","disabled");
			alert("draw finish,point count:"+total);
			return;
		}
		// drawHeatMap(data[total].data);
		drawPointsWithD3(data[total].data,img,"trackWithHeatmap");
		var progress = (total+1)*1.0/data.length*100;
		progress = progress.toFixed(2);
		$('#person-value').text(data[total].data.length);
		$('#time-value').text(getLocalTime(data[total].time));
		$("#play_progress").css("width",progress+"%");
		$("#play_progress").text(progress+"%");
		total++;
	},playSpeed);
}

function abortTrackPlayback(){
	abortDraw = true;
}

function setCanvasBackground(data,img){
	$('#floormap').css("background","url("+img.src+") no-repeat");
	$('#floormap').css("width",img.width);
	$('#floormap').css("height",img.height);
	$('#canvas').attr("width",img.width-50);
	$('#canvas').attr("height",img.height-50);
	
	var total = 0;
	// drawPoints(data,41,img);
	var drawHandle = setInterval(function(){
		total++;
		if(total>=1000 || total>=data.length){
			clearInterval(drawHandle);
			alert("draw finish,point count:"+total);
			return;
		}
		drawPoints(data,total,img);
	},300);
}

function drawPointsWithD3(data,img,domId){
	var w = img.width-50;
	var h = img.height-50;
	var radius = 5;
	var dataset=[];
	for(var i=0;i<data.length;i++){
		dataset.push([data[i].x,data[i].y]);
	}
	
	// Create SVG element
	// $('body').html('');
	d3.select("svg").remove();
	var svg = d3.select("#"+domId).append("svg").attr("width", w).attr(
			"height", h);

	svg.selectAll("circle").data(dataset).enter().append("circle").attr(
			"cx", function(d){
				return d[0];
			}).attr("cy", function(d){
		return d[1];
	}).attr("r", 5).attr("fill","#4169E1");
	// #4169E1
}

function drawHeatMap(data){
	$('#floormap').html('');
	var heatmap = h337.create({
		container : document.getElementById('floormap')
	});
	console.log(data.length);
	var count = 0;
	var heatmapData = [];
	for ( var i in data){
		var value = data[i].value;
		heatmapData.push({
			x : data[i].x,
			y : data[i].y,
			value : value+10,
			radius : 125
		});
		// heatmap.addData({x:data[i].x,y:data[i].y,value:data[i].value,radius:100});
		count++;
	}
	heatmap.setData({
		max : count,
		data : heatmapData
	});
}

function getQueryTimeDuration(object,data){
	var duration = $(object).siblings('.select_duration').children("option:selected").val();
	if (duration == '0'){
		layer.alert("请设置正确的查询时间范围", {
			icon : 0
		});
		return false;
	} else if (duration == '-1'){
		if($(object).siblings('.custom_duration').children('#time_start').val()=='' || 
				$(object).siblings('.custom_duration').children('#time_end').val()==''){
			layer.alert("请设置起止时间", {
				icon : 0
			});
			return false;
		}
		var duration_start = get_unix_time($(object).siblings('.custom_duration').children('#time_start').val());
		var duration_end = get_unix_time($(object).siblings('.custom_duration').children('#time_end').val());
		if(duration_start>duration_end){
			layer.alert("请设置正确的起止时间", {
				icon : 0
			});
			return false;
		}
		data['duration'] = duration;
		data['duration_start'] = duration_start;
		data['duration_end'] = duration_end;
		data['duration_type'] = 'custom';
	}else{
		data['duration'] = duration;
	}
	return true;
}

function get_unix_time(dateStr)
{
    var newstr = dateStr.replace(/-/g,'/'); 
    var date =  new Date(newstr); 
    var time_str = date.getTime().toString();
    return time_str.substr(0, 10);
}

function getLocalTime(timeStamp){
    return new Date(timeStamp).toLocaleString().replace(/年|月/g, "-").replace(/日/g, " ");
}

//时间戳转换成时间字符串
function format2Time(uData){
	var myDate = new Date(uData);
	var year = myDate.getFullYear();
	var month = myDate.getMonth() + 1;
	var day = myDate.getDate();
	var hour = myDate.getHours();
	var minute = myDate.getMinutes();
	var second = myDate.getSeconds();
	return year + '-' + month + '-' + day+' '+hour+':'+minute+':'+second;
}

//时间戳转换成八位日期
function format2Date(uData){
	var myDate = new Date(uData);
	var year = myDate.getFullYear();
	var month = myDate.getMonth() + 1;
	var day = myDate.getDate();
	return year + '-' + month + '-' + day;
}

//获取ap的位置，显示在地图上
function getAllApPosition(requestData,mapWidth,mapHeight,imgMarker,mapScale){
	$.ajax({
		type : "GET",
		dataType : "json",
		url : basePath + "/ap/realtime?" + new Date(),
		contentType : "application/x-www-form-urlencoded; charset=UTF-8",
		data : {
			"action" : "get_ap_position",
			"data" : JSON.stringify(requestData),
		},
		success : function(result){
		//	console.log(JSON.stringify(result));
			if (result.status !== 1){
				console.log(result.message);
			} else {
			//	$('#'+containerId).empty();
				var data = result.data;
				var index = 0;
				var imgs = [];
				for ( var i in data){
				//	console.log(i)
					var canvas = document.getElementById('canvas_' + i);
					var context = canvas.getContext('2d');
					var canvasbuffer = document.createElement('canvas');
					var contextbuffer = canvasbuffer.getContext('2d');
					drawApPosition(canvas, context, canvasbuffer, contextbuffer, i, data[i],mapWidth,mapHeight,imgMarker,mapScale);
				}
			}
		},
		error : function(){
			console.log("网络繁忙，请稍后再试!");
					}
	});
}
function drawApPosition(canvas, context, canvasbuffer, contextbuffer, floor, positions,mapWidth,mapHeight,imgMarker,mapScale){
	    canvasScale=mapScale[floor];
    	canvas.width = mapWidth[floor] ;
		canvas.height = mapHeight[floor] ;
		canvasbuffer.width = mapWidth[floor];
		canvasbuffer.height = mapHeight[floor];
		drawimgMarker(canvas, context, canvasbuffer, contextbuffer, floor, positions,imgMarker);
}
function drawimgMarker(canvas, context, canvasbuffer, contextbuffer, floor, positions,imgMarker){	    
		for ( var j in positions){
			contextbuffer.drawImage(imgMarker, JSON.stringify(positions[j].x)* canvasScale, JSON.stringify(positions[j].y)* canvasScale);
			// ap扫描到的手机数量
			var reg = new RegExp('"', "g");
			var str = JSON.stringify(positions[j].mac);
			str = str.replace(reg, "");
			contextbuffer.font = "20px '黑体'";
			contextbuffer.fillStyle = '#0000FF';
			contextbuffer.fillText(JSON.stringify(positions[j].count), JSON.stringify(positions[j].x) * canvasScale - 10, JSON.stringify(positions[j].y)
					* canvasScale);
			contextbuffer.fillText(str, JSON.stringify(positions[j].x - 15)
					* canvasScale, JSON.stringify(positions[j].y + 25)
					* canvasScale);
			//图标缩放 context.scale(2,2);
		}
		context.drawImage(canvasbuffer, 0, 0);
}




