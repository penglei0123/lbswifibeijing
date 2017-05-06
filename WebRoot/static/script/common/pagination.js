/**
 * 分页插件
 * powered by hzy
 */
$(function(){
	$("#pagination_first").click(function(){
		if(pageIndex==1){
			return;
		}
		pageIndex = 1;
		searchBuilding(pageIndex);
	});
	
	$("#pagination_last").click(function(){
		var pageCount = Math.ceil(total/pageSize);//总页数
		if(pageIndex==pageCount){
			return;
		}
		pageIndex = pageCount;
		searchBuilding(pageIndex);
	});
});

function generatePaginationNavigation(totalCount){
	total = totalCount;
	showPaginationNavigation(total);
}

/**
 * 根据总页数判断，如果小于paginationSize页，则显示所有页数，如果大于paginationSize页，则显示paginationSize页。根据当前点击的页数生成
 */
function showPaginationNavigation(total){
	var pageCount = Math.ceil(total/pageSize);//总页数
	var pageStart = 0;
	var pageEnd = 0;
	if(pageCount<paginationSize){
		pageStart = 1;
		pageEnd = pageCount;
	}else{
		var pageStart = pageIndex - parseInt(paginationSize / 2);
		var pageEnd = pageIndex + parseInt(paginationSize / 2);
		if(pageStart<1){
			pageStart=1;
			pageEnd=paginationSize;
		}
		if(pageEnd>pageCount){
			pageEnd=pageCount;
			pageStart=pageCount-paginationSize+1;
		}
	}
	var html = '<li id="pagination_prev"><a href="#"><span class="icon-angle-left"></span></a> </li>';
	for(var i=pageStart;i<=pageEnd;i++){
		html+='<li onclick="clickPage('+i+');"><a href="#">'+i+'</a> </li>';
	}
	html+='<li id="pagination_next"><a href="#"><span class="icon-angle-right"></span></a> </li>';
	$(".pagination-group").html(html);
	$(".pagination-group li").eq(pageIndex-pageStart+1).addClass("active");
	//点击上一页触发
	$("#pagination_prev").on('click',function(){
		if(pageIndex==1){
			return;
		}
		pageIndex--;
		searchBuilding(pageIndex);
	});
	
	//点击下一页触发
	$("#pagination_next").on('click',function(){
		var pageCount = Math.ceil(total/pageSize);//总页数
		if(pageIndex==pageCount){
			return;
		}
		pageIndex++;
		searchBuilding(pageIndex);
	});
}

function clickPage(index){
	pageIndex = index;
	searchBuilding(pageIndex);
}
