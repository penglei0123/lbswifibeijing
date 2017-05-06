/**
 * 前端公用JS脚本
 * @author hzy
 */
/**
 * JQUERY事件绑定和页面加载完毕后的初始化
 */
var processLayout = null;
var handle = null;
var realtimeHeatmapHandle = null;
var imgMarker = new Image();

$(function () {
    var timeStart = new Date().getTime() - 48 * 60 * 60 * 1000;
    var timeEnd = new Date().getTime() - 24 * 60 * 60 * 1000;
    $('.custom_duration #time_start').each(function () {
        if ($(this).attr('showTime') == "false") {
            $(this).val(format2Date(timeStart));
        } else {
            $(this).val(format2Time(timeStart));
        }
    });
    $('.custom_duration #time_end').each(function () {
        if ($(this).attr('showTime') == "false") {
            $(this).val(format2Date(timeEnd));
        } else {
            $(this).val(format2Time(timeEnd));
        }
    });


    $('.select_duration').change(function () {
        if ($(this).children('option:selected').val() == '-1') {
            $(this).next(".custom_duration").show();
            $(this).next('#time_start').val('');
            $(this).next('#time_end').val('');
        } else {
            $(this).next(".custom_duration").hide();
        }
    });

    $('#select_user').change(function () {
        if ($(this).children('option:selected').val() == '-1') {
            $("#user-mac").show();
            $('#user-mac').val('');
        } else {
            $("#user-mac").hide();
        }
    });

    switch (page) {
        case 'heatmap_history':
            $('#select_duration').show();
            $('#btnSearchHistoryHeatmap').show();
            $(".custom_duration").hide();
//		var data = {
//			"duration" : 600,
//			"building" : building,
//			"floor" : floor
//		};
            // 取消打开页面即请求内容功能
//			getHistoryHeatmap(data);
            break;
        case 'track_realtime':
            $('#select_user').show();
            $('#user-mac').hide();
            $('#select_duration').hide();
            $(".custom_duration").hide();
            $('.track_list').hide();
            $('#btnSearchTrackTrend').hide();
            var data = {
                "building": building
            };
            getAllOnlineUsers(data);
            break;
        case 'track_history':
            $('#select_user').hide();
            $('#user-mac').show();
            $('#select_duration').show();
            $(".custom_duration").hide();
            var data = {
                "building": building
            };
            // getAllOfflineUsers(data);
            break;
        case 'track_playback':
            $(".custom_duration").hide();
            $('#btnAbortTrackPlayback').attr("disabled", "disabled");
            break;
    }
});


var map = new BMap.Map("container");          // 创建地图实例

var top_left_control = new BMap.ScaleControl({anchor: BMAP_ANCHOR_TOP_LEFT});// 左上角，添加比例尺
var top_left_navigation = new BMap.NavigationControl();  //左上角，添加默认缩放平移控件
map.addControl(top_left_control);
map.addControl(top_left_navigation);

var point = new BMap.Point(116.34013, 39.986992);
map.centerAndZoom(point, 15);             // 初始化地图，设置中心点坐标和地图级别
map.addControl(new BMap.MapTypeControl());   //添加地图类型控件
map.setCurrentCity("北京");          // 设置地图显示的城市 此项是必须设置的
map.enableScrollWheelZoom(); // 允许滚轮缩放

heatmapOverlay = new BMapLib.HeatmapOverlay({"radius": 30});
map.addOverlay(heatmapOverlay);

function getHistoryHeatmap(requestData) {
    $.ajax({
        type: "GET",
        dataType: "json",
        url: basePath + "/heatmap/history?" + new Date(),
        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
        data: {
            "action": "get_heatmap_history",
            "data": JSON.stringify(requestData)
        },
        beforeSend: function () {
            processLayout = layer.load(1, {
                shade: false
            }); // 0代表加载的风格，支持0-2
        },
        success: function (result) {
            layer.close(processLayout);
            if (result.status !== 1) {
                layer.alert(result.message, {
                    icon: 0
                });
            } else {
                var data = result.data;
                // console.log(result);
                var countMax = 0;
                var heatmapData = [];
                for (var i in data) {
                    heatmapData.push({
                        "lng": data[i].x,
                        "lat": data[i].y,
                        "count": data[i].value
                    });
                    countMax = data[0].value;
                }
                heatmapOverlay.setDataSet({
                    max: 5000,
                    data: heatmapData
                });
            }
        },
        error: function () {
            layer.close(processLayout);
            layer.alert("网络繁忙，请稍后再试！", {
                icon: 0
            });
        }
    });
}


function getAllOnlineUsers(requestData) {
    $.ajax({
        type: "GET",
        dataType: "json",
        url: basePath + "/track/realtime?" + new Date(),
        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
        data: {
            "action": "get_all_online_user",
            "data": JSON.stringify(requestData)
        },
        success: function (result) {
            if (result.status !== 1) {
                layer.alert(result.message, {
                    icon: 0
                });
            } else {
                var html = '<option value="0">选择用户</option>';
                var data = result.data;
                for (var i in data) {
                    html += '<option value="' + data[i].mac + '">'
                        + data[i].mac + '</option>';
                }
                $('#select_user').html(html);
            }
        },
        error: function () {
            layer.alert("网络繁忙，请稍后再试！", {
                icon: 0
            });
        }
    });
}

// 获得历史记录中所有用户的MAC
function getAllOfflineUsers(requestData) {
    $.ajax({
        type: "GET",
        dataType: "json",
        url: basePath + "/track/history?" + new Date(),
        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
        data: {
            "action": "get_all_offline_user",
            "data": JSON.stringify(requestData)
        },
        success: function (result) {
            if (result.status !== 1) {
                // layer.alert(result.message, {
                // 	icon : 0
                // });
            } else {
                var html = '<option value="0">选择用户</option>';
                var data = result.data;
                // console.log(data.length);
                for (var i in data) {
                    html += '<option value="' + data[i] + '">'
                        + data[i] + '</option>';
                }
                $('#select_user').html(html);
            }
        },
        error: function () {
            // layer.alert("网络繁忙，请稍后再试！", {
            // 	icon : 0
            // });
        }
    });
}


function getTopKMAC() {
    $.ajax({
        type: "GET",
        dataType: "json",
        url: basePath + "/track/history?" + new Date(),
        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
        data: {
            "action": "get_topk_mac",
        },
        success: function (result) {
            if (result.status !== 1) {
                // layer.alert(result.message, {
                // 	icon : 0
                // });
            } else {
                var data = result.data;
                var html = '<option value="0">选择用户</option>';
                html += '<option value="-1">输入MAC</option>';
                for (var i in data) {
                    html += '<option value="' + data[i] + '">' + data[i].substring(0, 9) + '**:**:**</option>';
                }
                $('#select_user').html(html);
            }
        },
        error: function () {
            layer.close(processLayout);
            // layer.alert("网络繁忙，请稍后再试！", {
            // 	icon : 0
            // });
        }
    });
}

//摄像头位置（5个）
//加上了小照相机号，用来跳转摄像头
// var vectorCAMERA = new BMap.Marker(new BMap.Point(116.2607959119, 39.9564846157), {
//     // 初始化照相机标志的symbol
//     icon: new BMap.Symbol(BMap_Symbol_SHAPE_CAMERA, {
//         scale: 2,
//         strokeWeight: 1,
//         fillColor: 'pink',
//         fillOpacity: 0.8
//     })
// });
// vectorCAMERA.disableMassClear();
// map.addOverlay(vectorCAMERA);
//
// var opts = {
//     width: 450,
//     height: 400,
//     title: "市场西门动9"
// };
//
// var content = '<iframe src="http://101.96.143.27/lbs-zgc/wifibeijing/tv1.html" width="440" height="350"> ' + '</iframe>';
//
// var infoWindow = new BMap.InfoWindow(content, opts);
// vectorCAMERA.addEventListener("click", function () {
//     map.openInfoWindow(infoWindow, new BMap.Point(116.2607959119, 39.9564846157)); //开启信息窗口
// });


var vectorCAMERA2 = new BMap.Marker(new BMap.Point(116.33645726, 39.99792859), {
    // 初始化照相机标志的symbol
    icon: new BMap.Symbol(BMap_Symbol_SHAPE_CAMERA, {
        scale: 1.5,
        strokeWeight: 1,
        fillColor: 'pink',
        fillOpacity: 0.8
    })
});
vectorCAMERA2.disableMassClear();
map.addOverlay(vectorCAMERA2);

var opts2 = {
    width: 450,
    height: 400,
    title: "科馨社区警务工作室"
};

var content2 = '<iframe src="http://101.96.143.27/lbs-zgc/wifibeijing/tv2.html" width="440" height="350"> ' + '</iframe>';

var infoWindow2 = new BMap.InfoWindow(content2, opts2);
vectorCAMERA2.addEventListener("click", function () {
    map.openInfoWindow(infoWindow2, new BMap.Point(116.33645726, 39.99792859)); //开启信息窗口
});


var vectorCAMERA3 = new BMap.Marker(new BMap.Point(116.34371293, 39.99844667), {
    // 初始化照相机标志的symbol
    icon: new BMap.Symbol(BMap_Symbol_SHAPE_CAMERA, {
        scale: 1.5,
        strokeWeight: 1,
        fillColor: 'pink',
        fillOpacity: 0.8
    })
});
vectorCAMERA3.disableMassClear();
map.addOverlay(vectorCAMERA3);

var opts3 = {
    width: 450,
    height: 400,
    title: "中关村派出所巡逻警务站"
};

var content3 = '<iframe src="http://101.96.143.27/lbs-zgc/wifibeijing/tv3.html" width="440" height="350"> ' + '</iframe>';

var infoWindow3 = new BMap.InfoWindow(content3, opts3);
vectorCAMERA3.addEventListener("click", function () {
    map.openInfoWindow(infoWindow3, new BMap.Point(116.34371293, 39.99844667)); //开启信息窗口
});


var vectorCAMERA4 = new BMap.Marker(new BMap.Point(116.34961190, 40.00021756), {
    // 初始化照相机标志的symbol
    icon: new BMap.Symbol(BMap_Symbol_SHAPE_CAMERA, {
        scale: 1.5,
        strokeWeight: 1,
        fillColor: 'pink',
        fillOpacity: 0.8
    })
});
vectorCAMERA4.disableMassClear();
map.addOverlay(vectorCAMERA4);

var opts4 = {
    width: 450,
    height: 400,
    title: "语言大学社区警务工作室"
};

var content4 = '<iframe src="http://101.96.143.27/lbs-zgc/wifibeijing/tv4.html" width="440" height="350"> ' + '</iframe>';

var infoWindow4 = new BMap.InfoWindow(content4, opts4);
vectorCAMERA4.addEventListener("click", function () {
    map.openInfoWindow(infoWindow4, new BMap.Point(116.34961190, 40.00021756)); //开启信息窗口
});


var vectorCAMERA5 = new BMap.Marker(new BMap.Point(116.33013212, 39.96445953), {
    // 初始化照相机标志的symbol
    icon: new BMap.Symbol(BMap_Symbol_SHAPE_CAMERA, {
        scale: 1.5,
        strokeWeight: 1,
        fillColor: 'pink',
        fillOpacity: 0.8
    })
});
vectorCAMERA5.disableMassClear();
map.addOverlay(vectorCAMERA5);

var opts5 = {
    width: 450,
    height: 400,
    title: "魏南社区警务工作站"
};

var content5 = '<iframe src="http://101.96.143.27/lbs-zgc/wifibeijing/tv5.html" width="440" height="350"> ' + '</iframe>';

var infoWindow5 = new BMap.InfoWindow(content5, opts5);
vectorCAMERA5.addEventListener("click", function () {
    map.openInfoWindow(infoWindow5, new BMap.Point(116.33013212, 39.96445953)); //开启信息窗口
});


function getUserHistoryTrack(requestData) { 		//历史单人轨迹
    $.ajax({
        type: "GET",
        dataType: "json",
        url: basePath + "/track/history?" + new Date(),
        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
        data: {
            "action": "get_track_history",
            "data": JSON.stringify(requestData)
        },
        beforeSend: function () {
            processLayout = layer.load(1, {
                shade: false
            }); // 0代表加载的风格，支持0-2
        },
        success: function (result) {
            layer.close(processLayout);
            map.clearOverlays(polyline);
            if (result.status !== 1) {
                clearInterval(handle);
                layer.alert(result.message, {
                    icon: 0
                });
            } else {

                // console.log(result);
                var data = result.data;
                var temp = [];
                for (var j in data) {
                    temp = data[j];
                }
                var len = temp.length;
                var polylinePointsArray = [];
                for (var i in temp) {
                    polylinePointsArray.push(new BMap.Point(temp[i].x, temp[i].y));

                }
                var polyline = new BMap.Polyline(polylinePointsArray, {
                    strokeColor: "red",
                    strokeWeight: 2,
                    strokeOpacity: 0.6
                });
                map.addOverlay(polyline);

                // var time = [];
                // for (var k in temp) {
                //     time.push(temp[k].time);
                // }
                // console.log(polylinePointsArray[1]);

                // var myP1 = polylinePointsArray[0];    //起点
                // var myP2 = polylinePointsArray[len - 1];  //终点
                // var myIconmove = new BMap.Icon("http://developer.baidu.com/map/jsdemo/img/Mario.png", new BMap.Size(32, 70), {    //小车图片
                //     //offset: new BMap.Size(0, -5),    //相当于CSS精灵
                //     imageOffset: new BMap.Size(0, 0)    //图片的偏移量。为了是图片底部中心对准坐标点。
                // });

                // //添加起标志
                // //添加终标志
                // var point = polylinePointsArray[0];
                //
                // //添加起标志
                // //添加终标志
                // var myIcon = new BMap.Icon("http://api.map.baidu.com/img/dest_markers.png", new BMap.Size(28, 32), {
                //     offset: new BMap.Size(10, 25),
                //     imageOffset: new BMap.Size(0, 0)
                // });
                // var myIcon2 = new BMap.Icon("http://api.map.baidu.com/img/dest_markers.png", new BMap.Size(28, 32), {
                //     offset: new BMap.Size(-150, -205),
                //     imageOffset: new BMap.Size(0, -34)
                // });
                //
                // var point2 = polylinePointsArray[len - 1];
                // var marker = new BMap.Marker(point, {icon: myIcon});  // 创建标注
                // var marker2 = new BMap.Marker(point2, {icon: myIcon2});  // 创建标注
                // map.addOverlay(marker);              // 将标注添加到地图中
                // map.addOverlay(marker2);
                // marker.setAnimation(BMAP_ANIMATION_DROP); //跳动的动画
                // marker2.setAnimation(BMAP_ANIMATION_DROP); //跳动的动画

//                 var driving = new BMap.DrivingRoute(map);    //驾车实例
//                 driving.search(myP1, myP2);
//                 driving.setSearchCompleteCallback(function () {
//                     var pts = driving.getResults().getPlan(0).getRoute(0).getPath();    //通过驾车实例，获得一系列点的数组
//                     pts = polylinePointsArray;
//
//                     var paths = pts.length;    //获得有几个点
//
// //alert(pts.toString());
//                     var carMk = new BMap.Marker(pts[0], {icon: myIconmove});
//                     map.addOverlay(carMk);
//                     i = 0;
//                     function resetMkPoint(i) {
//                         carMk.setPosition(pts[i]);
//                         if (i < paths) {
//                             setTimeout(function () {
//                                 i++;
//                                 resetMkPoint(i);
//
//                                 var time_real = format2Time(time[i]);
//                                 $('#box').text(time_real);          //  页面上显示时间
//                                 console.log(time_real);
//                             }, 500);
//                         } else {
//                             map.removeOverlay(carMk);
//                         }
//                     }
//
//                     setTimeout(function () {
//                         resetMkPoint(0);
//                     }, 800)
//
//                 });
            }
        },
        error: function () {
            clearInterval(handle);
            layer.alert("网络繁忙，请稍后再试！", {
                icon: 0
            });

        }
    });
}

//实时人流分布
function getPosition(requestData) {

    $.ajax({
        type: "GET",
        dataType: "json",
        url: basePath + "/heatmap/realtime?" + new Date(),
        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
        data: {
            "action": "get_heatmap_realtime",
            "data": JSON.stringify(requestData)
        },
        success: function (result) {

            if (document.createElement('canvas').getContext) {  // 判断当前浏览器是否支持绘制海量点
                map.clearOverlays(pointCollection);
                var data = result.data;
                var realtimePoint = [];

                for (var i in data) {
                    realtimePoint.push(new BMap.Point(data[i].bd09_x, data[i].bd09_y));
                }
                var options = {
                    //					size: BMAP_POINT_SIZE_NORMAL,
                    //					shape: BMap_Symbol_SHAPE_STAR,
                    //					color: '#d340c3'
                    shape: BMAP_POINT_SHAPE_WATERDROP
                };
                var pointCollection = new BMap.PointCollection(realtimePoint, options);  // 初始化PointCollection

                // pointCollection.addEventListener('click', function (e) {
                // 	alert('单击点的坐标为：' + e.point.lng + ',' + e.point.lat);  // 监听点击事件      点多时会弹出多个弹窗
                // });

                map.addOverlay(pointCollection);  // 添加Overlay
            } else {
                alert('请在chrome、safari、IE8+以上浏览器查看本示例');
            }

            //使用点聚合显示
            // var data = result.data;
            // var realtimePoint = [];
            // var pt = null;
            // var markerClusterer = null;
            // for (var i in data) {
            //     pt = new BMap.Point(data[i].corx, data[i].cory);
            //     realtimePoint.push(new BMap.Marker(pt));
            // }
            // markerClusterer.clearMarkers();
            // markerClusterer = new BMapLib.MarkerClusterer(map, {markers: realtimePoint});
        },
        error: function () {
        }
    });
}


function getQueryTimeDuration(object, data) {
    var duration = $(object).siblings('.select_duration').children("option:selected").val();
    if (duration == '0') {
        layer.alert("请设置正确的查询时间范围", {
            icon: 0
        });
        return false;
    } else if (duration == '-1') {
        if ($(object).siblings('.custom_duration').children('#time_start').val() == '' ||
            $(object).siblings('.custom_duration').children('#time_end').val() == '') {
            layer.alert("请设置起止时间", {
                icon: 0
            });
            return false;
        }
        var duration_start = get_unix_time($(object).siblings('.custom_duration').children('#time_start').val());
        var duration_end = get_unix_time($(object).siblings('.custom_duration').children('#time_end').val());
        if (duration_start > duration_end) {
            layer.alert("请设置正确的起止时间", {
                icon: 0
            });
            return false;
        }
        data['duration'] = duration;
        data['duration_start'] = duration_start;
        data['duration_end'] = duration_end;
        data['duration_type'] = 'custom';
    } else {
        data['duration'] = duration;
    }
    return true;
}

function get_unix_time(dateStr) {
    var newstr = dateStr.replace(/-/g, '/');
    var date = new Date(newstr);
    var time_str = date.getTime().toString();
    return time_str.substr(0, 10);
}

function getLocalTime(timeStamp) {
    return new Date(timeStamp).toLocaleString().replace(/年|月/g, "-").replace(/日/g, " ");
}

//使用的原先的实时人流分布的方法，现在实现的是实时热力图。
function getAllPersonRealtimePosition(requestData) {

    $.ajax({
        type: "GET",
        dataType: "json",
        url: basePath + "/customer/realtime?" + new Date(),
        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
        data: {
            "action": "get_person_realtime",
            "data": JSON.stringify(requestData)
        },
        success: function (result) {
            var data = result.data;
            var count = 0;
            var heatmapData = [];
            for (var i in data) {
                heatmapData.push({
                    "lng": data[i].bd09_x,
                    "lat": data[i].bd09_y,
                    "count": 3
                });
                count++
            }
            heatmapOverlay.setDataSet({
                max: 50,
                data: heatmapData
            });

        },
        error: function () {

        }
    });
}
//判断浏览区是否支持canvas
function isSupportCanvas() {
    var elem = document.createElement('canvas');
    return !!(elem.getContext && elem.getContext('2d'));
}

//时间戳转换成时间字符串
function format2Time(uData) {
    var myDate = new Date(uData);
    var year = myDate.getFullYear();
    var month = myDate.getMonth() + 1;
    var day = myDate.getDate();
    var hour = myDate.getHours();
    var minute = myDate.getMinutes();
    var second = myDate.getSeconds();
    return year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;
}

//时间戳转换成八位日期
function format2Date(uData) {
    var myDate = new Date(uData);
    var year = myDate.getFullYear();
    var month = myDate.getMonth() + 1;
    var day = myDate.getDate();
    return year + '-' + month + '-' + day;
}

//两个分开的循环添加标志物
var points = [];
points.push(
    new BMap.Point(116.357119, 39.999118),
    new BMap.Point(116.355047, 39.99903),
    new BMap.Point(116.351412, 39.998854),
    new BMap.Point(116.350002, 39.998816),
    new BMap.Point(116.346766, 39.99857),
    new BMap.Point(116.345684, 39.998786),
    new BMap.Point(116.345201, 39.998516),
    new BMap.Point(116.343512, 39.99851),
    new BMap.Point(116.342335, 39.998436),
    new BMap.Point(116.339668, 39.998509),
    new BMap.Point(116.340053, 39.998401),
    new BMap.Point(116.33882, 39.99863),
    new BMap.Point(116.33591, 39.998426),
    new BMap.Point(116.334349, 39.998409),
    new BMap.Point(116.332462, 39.998414),
    new BMap.Point(116.330609, 39.998307),
    new BMap.Point(116.32901, 39.998301),
    new BMap.Point(116.327183, 39.998265),
    new BMap.Point(116.324734, 39.998109),
    new BMap.Point(116.322904, 39.9982),
    new BMap.Point(116.33778, 39.997879),
    new BMap.Point(116.341233, 39.997207),
    new BMap.Point(116.328173, 39.995011),
    new BMap.Point(116.328639, 39.992416),
    new BMap.Point(116.333881, 39.996742),
    new BMap.Point(116.343721, 39.997916),
    new BMap.Point(116.35984, 39.994804),
    new BMap.Point(116.355269, 39.994664),
    new BMap.Point(116.352764, 39.994458),
    new BMap.Point(116.355664, 39.992661),
    new BMap.Point(116.356935, 39.994628),
    new BMap.Point(116.353515, 39.992612),
    new BMap.Point(116.352107, 39.992573),
    new BMap.Point(116.347512, 39.992569),
    new BMap.Point(116.336714, 39.991959),
    new BMap.Point(116.336141, 39.991458),
    new BMap.Point(116.324681, 39.986694),
    new BMap.Point(116.326163, 39.986683),
    new BMap.Point(116.327321, 39.986752),
    new BMap.Point(116.330453, 39.986748),
    new BMap.Point(116.331866, 39.986722),
    new BMap.Point(116.324927, 39.982943),
    new BMap.Point(116.326831, 39.983003),
    new BMap.Point(116.328779, 39.98265),
    new BMap.Point(116.341938, 39.983863),
    new BMap.Point(116.343579, 39.984015),
    new BMap.Point(116.326938, 39.988215),
    new BMap.Point(116.326848, 39.989349),
    new BMap.Point(116.329572, 39.989614),
    new BMap.Point(116.32959, 39.988654),
    new BMap.Point(116.328242, 39.990215),
    new BMap.Point(116.334337, 39.988206),
    new BMap.Point(116.331253, 39.989159),
    new BMap.Point(116.331521, 39.985939),
    new BMap.Point(116.331648, 39.983986),
    new BMap.Point(116.333886, 39.989463),
    new BMap.Point(116.334056, 39.987149),
    new BMap.Point(116.339855, 39.998013),
    new BMap.Point(116.339781, 39.996653),
    new BMap.Point(116.339853, 39.994701),
    new BMap.Point(116.339991, 39.992806),
    new BMap.Point(116.339946, 39.992092),
    new BMap.Point(116.339824, 39.989835),
    new BMap.Point(116.339856, 39.988435),
    new BMap.Point(116.339961, 39.986708),
    new BMap.Point(116.340028, 39.985233),
    new BMap.Point(116.340067, 39.982449),
    new BMap.Point(116.340858, 39.988221),
    new BMap.Point(116.341842, 39.982913),
    new BMap.Point(116.347131, 39.993529),
    new BMap.Point(116.347116, 39.99393),
    new BMap.Point(116.346887, 39.997982),
    new BMap.Point(116.359881, 39.996691),
    new BMap.Point(116.343279, 39.982226),
    new BMap.Point(116.337846, 39.982009),
    new BMap.Point(116.335832, 39.981998),
    new BMap.Point(116.333436, 39.98187),
    new BMap.Point(116.3315462217, 39.981920052),
    new BMap.Point(116.32936, 39.981833),
    new BMap.Point(116.327736, 39.981812),
    new BMap.Point(116.325835, 39.981818),
    new BMap.Point(116.339738, 39.982071),
    //中关村大街
    new BMap.Point(116.32157800, 40.00577000),
    new BMap.Point(116.32147126, 40.00503005),
    new BMap.Point(116.32193200, 40.00004900),
    new BMap.Point(116.32185300, 40.00565500),
    new BMap.Point(116.32178853, 40.00461855),
    new BMap.Point(116.32188053, 40.00399755),
    new BMap.Point(116.32194153, 40.00312555),
    new BMap.Point(116.32202453, 40.00255455),
    new BMap.Point(116.32214153, 40.00190655),
    new BMap.Point(116.32210853, 40.00119755),
    new BMap.Point(116.32225400, 40.00077600),
    new BMap.Point(116.32232000, 39.99956500),
    new BMap.Point(116.32251700, 39.99836300),
    new BMap.Point(116.32265800, 39.99682400),
    new BMap.Point(116.32270200, 39.99614100),
    new BMap.Point(116.32272500, 39.99531700),
    new BMap.Point(116.32283002, 39.99426373),
    new BMap.Point(116.32288402, 39.99375273),
    new BMap.Point(116.32299302, 39.99262073),
    new BMap.Point(116.32397102, 39.99159973),
    new BMap.Point(116.32328400, 39.99021100),
    new BMap.Point(116.32327200, 39.98872300),
    new BMap.Point(116.32345300, 39.98741100),
    new BMap.Point(116.32347300, 39.98675700),
    new BMap.Point(116.32352100, 39.98605500),
    new BMap.Point(116.32360000, 39.98505500),
    new BMap.Point(116.32375300, 39.98382800),
    new BMap.Point(116.32381131, 39.98353531),
    new BMap.Point(116.32450100, 39.98036624),
    new BMap.Point(116.32500920, 39.98012421),
    new BMap.Point(116.32561220, 39.97858621),
    new BMap.Point(116.32600220, 39.97775421),
    new BMap.Point(116.32642820, 39.97666221),
    new BMap.Point(116.32672520, 39.97585521),
    new BMap.Point(116.32805539, 39.97308858),
    new BMap.Point(116.32834039, 39.97257658),
    new BMap.Point(116.32958299, 39.96589005),
    new BMap.Point(116.32987281, 39.96447226),
    new BMap.Point(116.32999881, 39.96295526),
    new BMap.Point(116.32999381, 39.96263626),
    new BMap.Point(116.33040481, 39.96121626),
    new BMap.Point(116.33048507, 39.95998788),
    new BMap.Point(116.33153066, 39.95158359),
    //中关村大街第二批
    new BMap.Point(116.32769039,39.97259258),
    new BMap.Point(116.32813739,39.97139158),
    new BMap.Point(116.32854999,39.97033405),
    new BMap.Point(116.32868899,39.96959805),
    new BMap.Point(116.32894699,39.96752205),
    new BMap.Point(116.32914499,39.96572105),
    new BMap.Point(116.32933581,39.96498626),
    new BMap.Point(116.32950781,39.96398826),
    new BMap.Point(116.32972881,39.96285026),
    new BMap.Point(116.32973981,39.96219226),
    new BMap.Point(116.32989181,39.96121226),
    new BMap.Point(116.32996007,39.96048788),
    new BMap.Point(116.33016907,39.95921188),
    new BMap.Point(116.33030907,39.95809988),
    new BMap.Point(116.33039607,39.95731488),
    new BMap.Point(116.33051207,39.95617888),
    new BMap.Point(39.9566715200,116.3307250000),
    new BMap.Point(116.33080576,39.95458159),
    new BMap.Point(116.33089566,39.95340359),
    new BMap.Point(116.33104066,39.95221159),
    new BMap.Point(116.33125392,39.95068032),
    new BMap.Point(116.33142192,39.94989032),
    new BMap.Point(116.33159700,39.94887400),
    new BMap.Point(116.33172300,39.94775100),
    new BMap.Point(116.33185900,39.94636000),
    new BMap.Point(116.32862339,39.97123158),
    new BMap.Point(116.32907699,39.96948405),
    new BMap.Point(116.32938799,39.96795005),
    new BMap.Point(116.32936599,39.96741305),
    new BMap.Point(116.33042307,39.95937288),
    new BMap.Point(116.33053007,39.95847188),
    new BMap.Point(116.33071407,39.95742788),
    new BMap.Point(116.33079007,39.95698688),
    new BMap.Point(116.3310500000,39.9552470000),
    new BMap.Point(116.33105666,39.95445159),
    new BMap.Point(116.33126966,39.95349259),
    new BMap.Point(116.33133266,39.95238259),
    new BMap.Point(116.33180900,39.94988000),
    new BMap.Point(116.33210200,39.94758400),
//下面为第二批添加坐标（机关）
// new BMap.Point(116.2694940000, 39.935806000),
// new BMap.Point(116.3422740000, 39.9625700000),
// new BMap.Point(116.3302310000, 39.9786670000),
// new BMap.Point(116.3123770000, 39.9793390000),
// new BMap.Point(116.3691120000, 39.9844420000),
// new BMap.Point(116.3061850000, 40.0298560000),
// new BMap.Point(116.3475300000, 39.9697360000),
// new BMap.Point(116.278942, 39.9603970000),
// new BMap.Point(116.3162450000, 40.0345710000),
// new BMap.Point(116.3078140000, 39.9733830000),
// new BMap.Point(116.3192260000, 39.9843420000),
// new BMap.Point(116.3250700000, 40.0085720000),
// new BMap.Point(116.2727840000, 39.9122210000),
// new BMap.Point(116.285088, 39.9628470000),
// new BMap.Point(116.3271500000, 39.9864620000),
// new BMap.Point(116.3049080000, 40.0302470000),
// new BMap.Point(116.3031800000, 39.9208580000),
// new BMap.Point(116.3681060000, 40.0672260000),
// new BMap.Point(116.3377470000, 40.0491490000),
// new BMap.Point(116.3235220000, 40.0266750000),
// new BMap.Point(116.3697600000, 39.9566600000),
// new BMap.Point(116.3579900000, 39.9758860000),
// new BMap.Point(116.3085550000, 116.3085550000),
// new BMap.Point(116.2990040000, 39.9318720000),
// new BMap.Point(116.3366030000, 39.9094740000),
// new BMap.Point(116.309667, 39.993308),
// new BMap.Point(116.298879, 40.047840),
// new BMap.Point(116.319985, 39.984574),
// new BMap.Point(116.31218, 39.984383),
// new BMap.Point(116.280298, 39.961025)
    //20170125
 new BMap.Point(116.3056283000	,39.9738952300),    
 new BMap.Point(116.3018838600	,39.9696896700),    
 new BMap.Point(116.3119954600	,39.9649510300),    
 new BMap.Point(116.2962538900	,39.9658815300),    
 new BMap.Point(116.2949894600	,39.9730299200),    
 new BMap.Point(116.2991733300	,39.9617398600),    
 new BMap.Point(116.3733281683	,39.9695706813),    
 new BMap.Point(116.3341479100	,39.9040807400),    
 new BMap.Point(116.3218780000	,39.9378180000),    
 new BMap.Point(116.1658830000	,39.9089180000),    
 new BMap.Point(116.3382234832	,39.9706265621),    
 new BMap.Point(116.3096800000	,39.9596070000),    
 new BMap.Point(116.2714380000	,39.9190490000),    
 new BMap.Point(116.3673868619	,39.9812688834),    
 new BMap.Point(116.3698853700	,39.9734622900),    
 new BMap.Point(116.3038140000	,39.9621990000),    
 new BMap.Point(116.3392130000	,39.9291400000),    
 new BMap.Point(116.3315560000	,39.9295230000),    
 new BMap.Point(116.3370468782	,39.9929854908),    
 new BMap.Point(116.2978159600	,39.8892845000),    
 new BMap.Point(116.3605010619	,39.9767925909),    
 new BMap.Point(116.3050787600	,39.9127271100),    
 new BMap.Point(116.2860529024	,39.9693341657),    
 new BMap.Point(116.3336980000	,39.9407430000),    
 new BMap.Point(116.2638610000	,40.0119020000),    
 new BMap.Point(116.3706161683	,39.9755576813),    
 new BMap.Point(116.2757550000	,39.9125980000),    
 new BMap.Point(116.3620818619	,39.9825218834),    
 new BMap.Point(116.3170050000	,39.9602850000),    
 new BMap.Point(116.3332429200	,39.9094583000),    
 new BMap.Point(116.3258194400	,39.9020579100),    
 new BMap.Point(116.3278820000	,39.9600410000),    
 new BMap.Point(116.3642843700	,39.9743392900),    
 new BMap.Point(116.3063126592	,39.9570567410),    
 new BMap.Point(116.3254992893	,40.0054176231),    
 new BMap.Point(116.3659210619	,39.9710385909),    
 new BMap.Point(116.271235	    ,39.911292    ),     
 new BMap.Point(116.3215760000	,39.9322980000),    
 new BMap.Point(116.3278260000	,39.9542020000),    
 new BMap.Point(116.2981788900	,39.9655287500),    
 new BMap.Point(116.3094315400	,39.9122854400),    
 new BMap.Point(116.3766922584	,39.9792517000),    
 new BMap.Point(116.3096065400	,39.9092687800),    
 new BMap.Point(116.3770502600	,39.9716578600),    
 new BMap.Point(116.3291972200	,39.9007718000),    
 new BMap.Point(116.3351500000	,39.9340350000),    
 new BMap.Point(116.3045401463	,39.9472906191),    
 new BMap.Point(116.3585170619	,39.9725755909),    
 new BMap.Point(116.3183480000	,39.9424890000),    
 new BMap.Point(116.3249070000	,39.9838230000),    
 new BMap.Point(116.3733082600	,39.9707488600),    
 new BMap.Point(116.3660978539	,39.9712090180),    
 new BMap.Point(116.3510873519	,40.0029043177),    
 new BMap.Point(116.3548230399	,40.0067339613),    
 new BMap.Point(116.3422474221	,40.0048373926),    
 new BMap.Point(116.3493260399	,40.0055539613),    
 new BMap.Point(116.3739946416	,39.9903145621),    
 new BMap.Point(116.3505618600	,40.0063533500),    
 new BMap.Point(116,3603204924	,39.9915584659),    
 new BMap.Point(116.3551292695	,40.0114222732),    
 new BMap.Point(116.34101,	39.996837       ),
 new BMap.Point(116.3374630000	,39.9857510000),    
 new BMap.Point(116.3437129318	,39.9984466692),  
 new BMap.Point(116.3302472473	,40.0183748503),    
 new BMap.Point(116.3546396199	,39.9950084049),    
 new BMap.Point(116.33248404	,39.985122519),     
 new BMap.Point(116.3303650000	,39.9838680000),    
 new BMap.Point(116.328508,	39.991112       ),
 new BMap.Point(116.3420489318	,40.0007716692),    
 new BMap.Point(116.3251970000	,39.9889190000),    
 new BMap.Point(116.3558206199	,39.9989644049),    
 new BMap.Point(116.332092306	,39.9991016934),    
 new BMap.Point(116.335166306	,39.9965256934),    
 new BMap.Point(116.335325,	39.9,86825       ),
 new BMap.Point(116.336493,	39.9,97854       ),
 new BMap.Point(116.3376700000	,39.9778290000),    
 new BMap.Point(116.3355170000	,39.9786530000),    
 new BMap.Point(116.3729940000	,40.0046470000),    
 new BMap.Point(116.3652682572	,39.9566573479),    
 new BMap.Point(116.3596694924	,39.9969694659),    
 new BMap.Point(116.3427737801	,39.9983710551),    
 new BMap.Point(116.3386920000	,39.9794860000),    
 new BMap.Point(116.3429627801	,39.9945400551),    
 new BMap.Point(116.34223,	39.998025       ), 
 new BMap.Point(116.3529110661	,39.9625354282),    
 new BMap.Point(116.3329592432	,40.0039438614),    
 new BMap.Point(116.342556,	39.994324       ), 
 new BMap.Point(116.3569426868	,40.0191962154),    
 new BMap.Point(116.2939300000	,39.9234250000),    
 new BMap.Point(116.3496018268	,40.0124668654),    
 new BMap.Point(116.2809180000	,39.9135580000),    
 new BMap.Point(116.3061460000	,39.9141110000),    
 new BMap.Point(116.3047222169	,39.9189131640),    
 new BMap.Point(116.2877720000	,39.9230520000),    
 new BMap.Point(116.2977392169	,39.9155481640),    
 new BMap.Point(116.3080221450	,39.9170008700),    
 new BMap.Point(116.3285608025	,39.9380693789),    
 new BMap.Point(116.2586039259	,40.0112181268),    
 new BMap.Point(116.2832421017	,39.9339861647),    
 new BMap.Point(116.2945250000	,40.0096920000),    
 new BMap.Point(116.2906499972	,39.9406605144),    
 new BMap.Point(116.298077,	39.948592        ),
 new BMap.Point(116.2919707998	,40.0133070758),    
 new BMap.Point(116.3339220000	,40.0371800000),    
 new BMap.Point(116.3406180000	,40.0364310000),    
 new BMap.Point(116.2597380000	,39.9370460000),    
 new BMap.Point(116.2709870000	,39.9374790000),    
 new BMap.Point(116.3006601042	,39.9305114712),    
 new BMap.Point(116.2982740000	,40.0040840000),    
 new BMap.Point(116.28949855	,39.9633827549),    
 new BMap.Point(116.2304710000	,39.9375260000),    
 new BMap.Point(116.2874411994	,39.9474291289),    
 new BMap.Point(116.3640140839	,39.9708497818),    
 new BMap.Point(116.3714110000	,39.9568810000),    
 new BMap.Point(116.3559920000	,39.9774090000),    
 new BMap.Point(116.330464	    ,39.973249    ),     
 new BMap.Point(116.3371480000	,39.9627390000),    
 new BMap.Point(116.3595942572	,39.9506823479),    
 new BMap.Point(116.3579620000	,39.9778940000),    
 new BMap.Point(116.3423320000	,39.9655380000),    
 new BMap.Point(116.3604464563	,39.9480943945),    
 new BMap.Point(116.3627050000	,39.9578980000),    
 new BMap.Point(116.3127591746	,39.9880908683),    
 new BMap.Point(116.3286520000	,39.9795860000),    
 new BMap.Point(116.3296168471	,39.9639983397),    
 new BMap.Point(116.3091431597	,39.993437747 ),     
 new BMap.Point(116.308391	    ,39.986057    ),     
 new BMap.Point(116.3299310000	,39.9760980000),    
 new BMap.Point(116.2677320000	,39.9150040000),    
 new BMap.Point(116.3449870000	,39.9661690000),    
 new BMap.Point(116.319297	    ,39.99173     ),    
 new BMap.Point(116.2732260000	,39.9050060000),    
 new BMap.Point(116.2599530000	,39.9190690000),    
 new BMap.Point(116.2709421331	,39.9272918717),    
 new BMap.Point(116.2850670000	,40.0206140000),    
 new BMap.Point(116.2844561017	,39.9353981647),    
 new BMap.Point(116.2572522791	,39.9433351924),    
 new BMap.Point(116.2869070000	,39.9310460000),    
 new BMap.Point(116.2828030000	,39.9381540000),    
 new BMap.Point(116.2788880000	,39.9274580000),    
 new BMap.Point(116.3196830000	,39.9516420000),    
 new BMap.Point(116.3126200000	,39.9467040000),    
 new BMap.Point(116.2800150000	,39.9209090000),    
 new BMap.Point(116.2608610000	,39.9132226000),   
 new BMap.Point(116.2708560000	,39.9189650000),    
 new BMap.Point(116.3196060000	,39.9453610000),    
 new BMap.Point(116.3099313683	,39.9817661327),    
 new BMap.Point(116.3433080000	,39.9815840000),    
 new BMap.Point(116.3170586228	,39.9526708508),    
 new BMap.Point(116.3082270508	,39.9537351490),    
 new BMap.Point(116.3461830000	,39.9697970000),    
 new BMap.Point(116.3192016963	,39.9560107353),    
 new BMap.Point(116.3463879318	,40.0008126692),    
 new BMap.Point(116.3214119273	,39.9576115692),    
 new BMap.Point(116.2134651547	,39.9246162350),    
 new BMap.Point(116.3163900508	,39.9613841490),    
 new BMap.Point(116.3352260000	,39.9816030000),    
 new BMap.Point(116.3446970000	,39.9653350000),
 //20170125-1
  new BMap.Point(116.31560101,39.96610659),
 new BMap.Point(116.31279492,39.97553832),
 new BMap.Point(116.3748742584,39.9847457),
 new BMap.Point(116.3645370619,39.97817359),
 new BMap.Point(116.3693302584,39.9827267),
 new BMap.Point(116.3762192584,39.9805967),
 new BMap.Point(116.3732962584,39.9791547),
 new BMap.Point(116.351178,39.977785),
 new BMap.Point(116.359515,39.958878),
 new BMap.Point(116.314333,39.98232),
 new BMap.Point(116.321868,39.935531),
 new BMap.Point(116.3346678025,39.93898938),
 new BMap.Point(116.32325,39.938916),
 new BMap.Point(116.334722,39.929725),
 new BMap.Point(116.318285,39.927247),
 new BMap.Point(116.334026,39.932855),
 new BMap.Point(116.293255,39.925118),
 new BMap.Point(116.301854,39.905441),
 new BMap.Point(116.305717,39.919357),
 new BMap.Point(116.2936508888,39.95859892),
 new BMap.Point(116.307954,39.912241),
 new BMap.Point(116.289349,39.90698),
 new BMap.Point(116.346351,39.977255),
 new BMap.Point(116.3380171299,39.97426901),
 new BMap.Point(116.3352622217,39.98050805),
 new BMap.Point(116.303638,39.951746),
 new BMap.Point(116.2936508888,39.95859892),
 new BMap.Point(116.2933039,39.972789),
 new BMap.Point(116.263405,39.911322),
 new BMap.Point(116.28163,39.946165),
 new BMap.Point(116.26683,39.904535),
 new BMap.Point(116.269446,39.911783),
 new BMap.Point(116.270862,39.925664),
 new BMap.Point(116.272665,39.908277),
 new BMap.Point(116.306674,39.929844),
 new BMap.Point(116.264487,39.927518),
 new BMap.Point(116.252258,39.937211),
 new BMap.Point(116.262874,39.928033),
 new BMap.Point(116.2611503327,39.92617546),
 new BMap.Point(116.2610557483,39.93478136),
 new BMap.Point(116.272938,39.937601),
 new BMap.Point(116.2528,39.934146),
 new BMap.Point(116.2710305391,39.9325425),
 new BMap.Point(116.3544443775,39.99768837),
 new BMap.Point(116.33610624,39.89823074),
 new BMap.Point(116.31722288,39.90410327),
 new BMap.Point(116.30464543,39.91384655),
 new BMap.Point(116.31484628,39.91094528),
 new BMap.Point(116.30227428,39.89053588),
 new BMap.Point(116.32551944,39.90489124),
 new BMap.Point(116.30238432,39.91565766),
 new BMap.Point(116.32215833,39.90047736),
 new BMap.Point(116.30773485,39.90483983),
 new BMap.Point(116.3515452695,40.01366227),
 new BMap.Point(116.3534780399,39.99932796),
 new BMap.Point(116.3707592584,39.9875317),
 new BMap.Point(116.290009,39.909851),
 new BMap.Point(116.329861,39.98936),
 new BMap.Point(116.3476810332,40.01537448),
 new BMap.Point(116.30812,39.946005),
 new BMap.Point(116.299609,39.937471),
 new BMap.Point(116.292989,39.930181),
 new BMap.Point(116.292029,39.938095),
 new BMap.Point(116.230471,39.937526),
 new BMap.Point(116.29004,39.929423),
 new BMap.Point(116.2815481017,39.94211916),
 new BMap.Point(116.329223,40.051131),
 new BMap.Point(116.328739,40.047183),
 new BMap.Point(116.278964,39.96921),
 new BMap.Point(116.32282,40.007859),
 new BMap.Point(116.296292,39.998844),
 new BMap.Point(116.26667,39.925775),
 new BMap.Point(116.334836,40.052588),
 new BMap.Point(116.275453,39.981538),
 new BMap.Point(116.332934,40.061197),
 new BMap.Point(116.296634,40.004136),
 new BMap.Point(116.256902,39.986956),
 new BMap.Point(116.308155,40.006519),
 new BMap.Point(116.3492334539,40.04107447),
 new BMap.Point(116.259608,40.017343),
 new BMap.Point(116.315031,40.056784),
 new BMap.Point(116.3350028754,40.03924014),
 new BMap.Point(116.304321,40.002473),
 new BMap.Point(116.323842,40.06411),
 new BMap.Point(116.2744827334,39.94523613),
 new BMap.Point(116.264641,39.958542),
 new BMap.Point(116.251785,39.971426),
 new BMap.Point(116.2471043274,39.95344056),
 new BMap.Point(116.216693,39.966566)
);
var a = points.length;
for (var i = 0; i < a; i++) {
    var APPoint = new BMap.Marker(points[i], {
        icon: new BMap.Symbol(BMap_Symbol_SHAPE_POINT, {
            scale: 0.4,
            rotation: 0
        })
    });
    APPoint.disableMassClear();
    map.addOverlay(APPoint);
}

// var points2 = [];
// points2.push(
//    //第三批添加AP坐标
//    new BMap.Point116.32157800,40.00577000),
//    new BMap.Point(116.32578551,39.98196852)
// );
//
// var b = points2.length;
// for (var i = 0; i < b; i++) {
//    var APPoint1 = new BMap.Marker(points2[i], {
//        icon: new BMap.Symbol(BMap_Symbol_SHAPE_POINT, {
//            scale: 0.4,
//            rotation: 0
//        })
//    });
//    APPoint1.disableMassClear();
//    map.addOverlay(APPoint1);
// }


