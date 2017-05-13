package com.genepoint.lbsshow.service.impl;

import java.util.List;


/**
 * 禁止区域<br/>
 * Created by jd on 2016/12/01.
 */
public class ForbiddenRegion {
	// 区域编号
	public int regionId;
	// 区域描述
	public String description;
	// 区域路径
	public List<Point> path;

	public ForbiddenRegion(int id, List<Point> path, String desc) {
		this.regionId = id;
		this.path = path;
		this.description = desc;
    }

	public int getRegionId() {
		return this.regionId;
    }

	public String getDescription() {
		return this.description;
    }

	public List<Point> getPath() {
		return this.path;
    }
}
