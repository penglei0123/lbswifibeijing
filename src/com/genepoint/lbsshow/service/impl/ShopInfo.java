package com.genepoint.lbsshow.service.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * 商铺信息实体类
 * 
 * @author jd
 *
 */
public class ShopInfo {
	public int shopId;
	public String shopName;
	public String shopDesc;
	public String building;
	public String floor;
	public List<Point> path;
	public float minX;
	public float minY;
	public float maxX;
	public float maxY;
	
	public ShopInfo(String building, String floor, int shopId, String shopName, String description, String pathStr) throws Exception {
		this.building = building.intern();
		this.floor = floor.intern();
		this.shopId = shopId;
		this.shopName = shopName;
		this.shopDesc = description;
		String[] pathsStr = pathStr.split("#");
		if (pathsStr == null || pathsStr.length == 0) {
			throw new Exception("shop path is empty");
		}
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		path = new ArrayList<Point>();
		for (int i = 0; i < pathsStr.length; i++) {
			String[] pointStr = pathsStr[i].split(",");
			float x = Float.parseFloat(pointStr[0]);
			float y = Float.parseFloat(pointStr[1]);
			if (minX > x)
				minX = x;
			if (minY > y)
				minY = y;
			if (maxX < x)
				maxX = x;
			if (maxY < x)
				maxY = y;
			path.add(new Point(x, y));
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(shopId).append(",").append(shopName).append(",").append(shopDesc).append(",path:");
		for (Point p : path) {
			sb.append("[" + p.x + "," + p.y + "]");
		}
		return sb.toString();
	}
}
