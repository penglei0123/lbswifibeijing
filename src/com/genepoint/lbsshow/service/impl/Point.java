package com.genepoint.lbsshow.service.impl;

public class Point {
	public float x;
	public float y;

	public Point() {

	}

	public Point(int x, int y) {
		this.x = x * 1.0f;
		this.y = y * 1.0f;
	}

	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}
}
