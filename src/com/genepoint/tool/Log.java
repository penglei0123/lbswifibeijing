package com.genepoint.tool;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.genepoint.custom.Configs;

public class Log {
    private static SimpleDateFormat dateFormat;
    private static InetAddress netAddress = null;
    private static String host = "unknown host";
    private static String ip = "unknown ip";
    private static final int LOG_INFO = 0x1;
    private static final int LOG_WARN = 0x2;
    private static final int LOG_ERROR = 0x3;
    private static final int LOG_DEBUG = 0x4;
    private static final int LOG_TRACE = 0x5;
    /**
     * 配置Log4j读取配置文件的代码在com.genepoint.location.servlet.async.AppContextListener中
     */
    private static Logger logger = Logger.getLogger(Log.class);

    static {
	dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	netAddress = getInetAddress();
	host = getHostName(netAddress);
	ip = getHostIp(netAddress);
    }

    public static InetAddress getInetAddress() {
	try {
	    return InetAddress.getLocalHost();
	} catch (UnknownHostException e) {
	}
	return null;

    }

    public static String getHostIp(InetAddress netAddress) {
	if (null == netAddress) {
	    return "unknown ip";
	}
	return netAddress.getHostAddress();
    }

    public static String getHostName(InetAddress netAddress) {
	if (null == netAddress) {
	    return "unknown host";
	}
	return netAddress.getHostName();
    }

    public static void info(Class<?> c, String message) {
	writeLog(Log.LOG_INFO, c, message);
    }

    public static void warn(Class<?> c, String message) {
	writeLog(Log.LOG_WARN, c, message);
    }

    public static void error(Class<?> c, String message) {
	writeLog(Log.LOG_ERROR, c, message);
    }

    public static void debug(Class<?> c, String message) {
	if (Configs.DEBUG) {
	    writeLog(Log.LOG_DEBUG, c, message);
	}
    }

    public static void trace(Class<?> c, String message, Throwable ex) {
	String traceStr = "";
	if (ex != null) {
	    StringWriter writer = new StringWriter(256);
	    ex.printStackTrace(new PrintWriter(writer));
	    traceStr = writer.toString().trim();
	} else {
	    traceStr = "null";
	}
	writeLog(Log.LOG_TRACE, c, message + "\n" + traceStr);
    }

    public static void trace(Class<?> c, Throwable ex) {
	String traceStr = "";
	if (ex != null) {
	    StringWriter writer = new StringWriter(256);
	    ex.printStackTrace(new PrintWriter(writer));
	    traceStr = writer.toString().trim();
	} else {
	    traceStr = "null";
	}
	writeLog(Log.LOG_TRACE, c, "\n" + traceStr);
    }

    private static void writeLog(int type, Class<?> c, String msg) {
	Date date = new Date(System.currentTimeMillis());
	/**
	 * 获取调用栈，0表示最里层，依次往外为调用层
	 */
	StackTraceElement ste = new Throwable().getStackTrace()[2];
	StringBuffer sb = new StringBuffer("[" + dateFormat.format(date)
		+ "] [" + host + "/" + ip + "]");
	switch (type) {
	case Log.LOG_INFO:
	    sb.append(" [INFO] - ");
	    break;
	case Log.LOG_WARN:
	    sb.append(" [WARN] - ");
	    break;
	case Log.LOG_ERROR:
	    sb.append(" [ERROR] - ");
	    break;
	case Log.LOG_DEBUG:
	    sb.append(" [DEBUG] - ");
	    break;
	case Log.LOG_TRACE:
	    sb.append(" [TRACE] - ");
	    break;
	}
	sb.append(c.getName() + ":" + ste.getLineNumber());
	sb.append(" - " + msg);
	// System.out.println(sb.toString());
	/**
	 * 调用Log4j保存日志，因为日志已经具有格式，在log4j中配置忽略格式,为了避免和阿里云OSS的日志混在一起，使用FATAL方式
	 */
	logger.fatal(sb);
	ste = null;
	sb = null;
    }

    public static void main(String args[]) {
	Log.error(Log.class, "test");
	Log.info(Log.class, "test");
	Log.trace(Log.class, "test", new Exception("test"));

    }
}