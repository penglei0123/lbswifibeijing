/**
 * 绘制带箭头的线条
 */
function Line(x1,y1,x2,y2){
    this.x1=x1;
    this.y1=y1;
    this.x2=x2;
    this.y2=y2;
}
Line.prototype.drawWithArrowheads=function(ctx){

    // arbitrary styling
    ctx.strokeStyle="blue";
    ctx.fillStyle="blue";
    ctx.lineWidth=2;
    
    ctx.beginPath();
	ctx.arc(this.x1, this.y1, 2, 0,2 * Math.PI);
	ctx.closePath();
	ctx.fill();
	
	ctx.beginPath();
	ctx.arc(this.x2, this.y2, 2, 0,2 * Math.PI);
	ctx.closePath();
	ctx.fill();
	
	ctx.fillStyle="blue";
    
    // draw the line
    ctx.beginPath();
    ctx.moveTo(this.x1,this.y1);
    ctx.lineTo(this.x2,this.y2);
    ctx.stroke();

    // draw the starting arrowhead
    //var startRadians=Math.atan((this.y2-this.y1)/(this.x2-this.x1));
    //startRadians+=((this.x2>this.x1)?-90:90)*Math.PI/180;
    //this.drawArrowhead(ctx,this.x1,this.y1,startRadians);
    // draw the ending arrowhead
    var endRadians=Math.atan((this.y2-this.y1)/(this.x2-this.x1));
    endRadians+=((this.x2>this.x1)?90:-90)*Math.PI/180;
    this.drawArrowhead(ctx,this.x2,this.y2,endRadians);

}
Line.prototype.drawArrowhead=function(ctx,x,y,radians){
    ctx.save();
    ctx.beginPath();
    ctx.translate(x,y);
    ctx.rotate(radians);
    ctx.moveTo(0,0);
    ctx.lineTo(3,10);
    ctx.lineTo(-3,10);
    ctx.closePath();
    ctx.restore();
    ctx.fill();
}