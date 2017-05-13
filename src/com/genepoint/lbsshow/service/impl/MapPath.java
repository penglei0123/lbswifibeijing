package com.genepoint.lbsshow.service.impl;


/**
 * Created by jd on 2016/1/21.
 */
public class MapPath {
    //路径编号
    public int pathID;
    //路径起始终止点坐标
	public Point pointS;
	public Point pointE;
    //路径长度（权重）
    public int pathLength;
    //路径的起始编号和终止编号
    public int pathS,pathE;

    public int pointID;//存点的时候使用

    public MapPath(int pointID,Float x1,Float y1){
    	this.pointID = pointID;
		pointS = new Point(x1, y1);
    }

    public MapPath(int id,int pathS,int pathE,double x1,double y1,double x2,double y2){
        this.pathID = id;
        this.pathS = pathS;
        this.pathE = pathE;
		pointS = new Point((float) x1, (float) y1);
		pointE = new Point((float) x2, (float) y2);
        pathLength = (int)Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public void setPathS(int s){
        this.pathS = s;
    }

    public void setPathE(int e){
        this.pathE = e;
    }

    public int getPathS(){
        return this.pathS;
    }

    public int getPathE(){
        return this.pathE;
    }

    public void setPathLength(int len){this.pathLength = len;}

    public int getPathLength(){return this.pathLength;}

    public void setPathID(int id){
        this.pathID = id;
    }

    public int getPathID(){
        return  this.pathID;
    }

    public void setPointS(double x,double y){
        pointS.set((float)x,(float)y);
    }

    public void setPointE(double x,double y){
        pointE.set((float)x,(float)y);
    }

	public Point getPointS() {
        return this.pointS;
    }

	public Point getPointE() {
        return this.pointE;
    }

    @Override
	public String toString() {
		return "PathConstraint [pointS=" + pointS + "]";
	}
}
