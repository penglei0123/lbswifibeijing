/**
 * 最短路径算法
 * @param matrixIn
 * @param nodeSizeIn
 */
function ShortestPath(matrixIn, nodeSizeIn){
	var nodeSize = nodeSizeIn;
	var MAXNUM = nodeSize+1;
	var matrix = matrixIn;
	var dist = new Array(MAXNUM);
	var prev = new Array(MAXNUM);
	var s = new Array(MAXNUM);
	//得到最短路径长度
	this.evaluate = function(startNode, endNode) {
		this.dijkstra(startNode);
		return dist[endNode];
	};
	
	this.dijkstra = function(startNode) {
		for (var i = 1; i <= nodeSize; i++) {
			dist[i] = matrix[startNode][i];
			s[i] = false;
			if (dist[i] == 0x7fffffff) {
				prev[i] = -1;
			} else {
				prev[i] = startNode;
			}
		}
		dist[startNode] = 0;
        s[startNode] = true;
        for (var i = 2; i <= nodeSize; i++) {
            var mindist = 0x7fffffff;
            var u = startNode;
            for (var j = 1; j <= nodeSize; ++j) {
                if ((!s[j]) && dist[j] < mindist) {
                    u = j;
                    mindist = dist[j];
                }
            }
            s[u] = true;
            for (var j = 1; j <= nodeSize; j++) {
                if ((!s[j]) && matrix[u][j] < 0x7fffffff) {
                    if (dist[u] + matrix[u][j] < dist[j]) {
                        dist[j] = dist[u] + matrix[u][j];
                        // 记录前驱
                        prev[j] = u;
                    }
                }
            }
        }
	};
	
	this.find = function(start,end){
		var tmp = end;
        var stack = new Stack();
        if (start != end) {
            while (prev[tmp] != start) {
                stack.push(tmp);
                tmp = prev[tmp];
            }
            stack.push(tmp);
        } else {
            stack.push(end);
        }
        stack.push(start);
        return stack;
	};
	
	this.getPathTrail = function(startNode, endNode){
        var stack = this.find(startNode, endNode);
        var list = [];
        while (!stack.isEmpty()) {
            list.push(stack.pop());
        }
        return list;
    };
    
    this.getPathTrailWithLength = function(startNode, endNode){
        var stack = this.find(startNode, endNode);
        var str="";
        while (!stack.isEmpty()) {
            if (stack.size() != 1) {
                str+=stack.pop() + "->";
            } else {
                str+=stack.pop() + ":" + dist[endNode];
            }
        }
        return str;
    }
};

function Stack() {
    var items = [];
    this.push = function(element){
        items.push(element);
    };
    this.pop = function(){
        return items.pop();
    };
    this.peek = function(){
        return items[items.length-1];
    };
    this.isEmpty = function(){
        return items.length == 0;
    };
    this.size = function(){
        return items.length;
    };
    this.clear = function(){
        items = [];
    };
    this.print = function(){
        console.log(items.toString());
    };
    this.toString = function(){
        return items.toString();
    };
}

function pointInPloygon(x,y, points) {
	var i, j = points.length - 1;
	var oddNodes = false;
	for (i = 0; i < points.length; i++) {
		if ((points[i].y < y && points[j].y >= y || points[j].y < y && points[i].y >= y)
				&& (points[i].x <= x || points[j].x <= x)) {
			// 点斜式直线方程变形，右边为true则表示新增一个交点
			oddNodes ^= (points[i].x + (y - points[i].y) / (points[j].y - points[i].y) * (points[j].x - points[i].x) < x);
		}
		j = i;
	}
	return oddNodes;
}