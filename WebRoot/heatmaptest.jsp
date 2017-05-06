<html lang="zh-CN">
<head>
<meta charset="utf-8">
<title>heatmap test</title>
<script type="text/javascript"
	src="http://code.jquery.com/jquery-latest.js"></script>
<script type="text/javascript" src="/lbsshow/static/script/heatmap/heatmap.min.js"></script>
<script src="https://d3js.org/d3.v4.min.js"></script>
</head>
<body>
<div  id="heatmap" style="width:1000px;height:800px;">&nbsp;</div>
<style>
svg{
	position:absolute;
}
</style>
</body>
<script>
	var total = 0;
	var dt = new Date();
	var startTime = dt.getTime();
	var drawHandle = setInterval(function() {
		if (total >= 100) {
			clearInterval(drawHandle);
			var dt2 = new Date();
			var endTime = dt2.getTime();
			alert(endTime-startTime);
			return;
		}
		$('#heatmap').html('');
		drawHeatmap();
		total++;
	}, 1000);

	function drawHeatmap() {
		var w = 1000;
		var h = 800;
		var radius = 5;
		var heatmapData = [];
		var dataset=[];
		for (var i = 0; i < 500; i++) {
			var pw = Math.random() * w;
			if (pw < radius) {
				pw = radius;
			} else if (pw > w - radius) {
				pw = w - radius;
			}
			var ph = Math.random() * h;
			if (ph < radius) {
				ph = radius;
			} else if (ph > h - radius) {
				ph = h - radius;
			}
			heatmapData.push({
				x : parseInt(pw),
				y : parseInt(ph),
				value : 10,
				radius : 100
			});
			dataset.push([pw,ph]);
		}
		var heatmap = h337.create({
			container : document.getElementById('heatmap')
		});
		heatmap.setData({
			max : 100,
			data : heatmapData
		});
		d3.select("svg").remove();
		var svg = d3.select("#heatmap").append("svg").attr("width", w).attr(
				"height", h);

		svg.selectAll("circle").data(dataset).enter().append("circle").attr(
				"cx", function(d) {
					return d[0];
				}).attr("cy", function(d) {
			return d[1];
		}).attr("r", 5);
	}
</script>
</html>