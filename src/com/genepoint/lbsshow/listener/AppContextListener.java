package com.genepoint.lbsshow.listener;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.PropertyConfigurator;

import com.genepoint.custom.Configs;
import com.genepoint.lbsshow.service.impl.HotPosition;
import com.genepoint.lbsshow.service.impl.TrackServiceImpl;
import com.genepoint.tool.LBSDataFaker;
import com.genepoint.tool.Log;

/**
 * 监听器，在web容器启动/退出时被调用
 * 
 * @author jd
 *
 */
@WebListener
public class AppContextListener implements ServletContextListener {
	final static Logger	logger	= Logger.getLogger("sysLog");

	public void contextInitialized(ServletContextEvent servletContextEvent) {
		/**
		 * 配置Log4j
		 */
		String realPath = servletContextEvent.getServletContext().getRealPath("/");
		PropertyConfigurator.configure(realPath + "/WEB-INF/log4j.properties");
		Log.info(this.getClass(), "Log4j init...");
		// 加载配置文件
		Configs.LoadConfiguration(realPath + "/WEB-INF/system.properties");

		// 加载临时热点位置
		String hotPosPath = realPath + "/WEB-INF/hotpos_data.txt";
		try {
			loadHotPos(hotPosPath);
		} catch (IOException e) {
			Log.trace(this.getClass(), e);
		}
	}

	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		LBSDataFaker.stopWork();
		// Log.info(this.getClass(), "LBSDataFaker stopped");
	}

	public void loadHotPos(String path) throws IOException{
		File file = new File(path);
		if(file.exists()){
			LineNumberReader reader = new LineNumberReader(new FileReader(file));
			String line = null;
			while((line=reader.readLine())!=null){
				String[] arr = line.split("\t");
				double x = Double.parseDouble(arr[0]);
				double y = Double.parseDouble(arr[1]);
				int value = Integer.parseInt(arr[2]);
				TrackServiceImpl.hotPositions.add(new HotPosition(x, y, value));
			}
			reader.close();
		} else {
			Log.info(this.getClass(), "hot position file not exist");
		}
	}
}
