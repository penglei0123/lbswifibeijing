package com.genepoint.converter;

/**
 * 坐标转换插件接口
 * 
 * @author jd
 *
 */
public interface PositionConverterFactory {
	public double[] convert(double x, double y);
}
