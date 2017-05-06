package com.genepoint.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.genepoint.custom.Configs;
import com.genepoint.dao.DBUtil;

/**
 * 随机生成位置数据 用于测试<br>
 * 每个mac为一个list(队列)，长度最多为TRACK_WINDOW_SIZE大小，key有过期时间
 * 所有mac和更新时间作为字段保存在hashset中，启动监控线程定期清理过期的字段
 * 
 * @author jd
 *
 */
public class LBSDataFaker {
	private static String[]				macList	= { "24:05:0f:18:d7:0b", "84:4b:f5:07:a2:26", "3c:8c:40:9d:ea:36", "ec:26:ca:6b:b2:8b",
			"ec:26:ca:6b:b2:8c", "a4:56:02:8a:df:0d", "c8:3a:35:21:42:a0", "08:10:78:ea:68:d8", "c4:ca:d9:75:25:40", "e4:d3:32:5e:e5:c0" };
	private static int					THREAD_COUNT	= 0;

	private static SimpleDateFormat		dateFormat		= new SimpleDateFormat("yyyyMMdd");

	public static Queue<String>	posQueue		= new LinkedList<String>();

	private static Connection			dbConn			= null;
	private static PreparedStatement	pstmt			= null;
	private static ResultSet			rs				= null;
	private static boolean				workStarted		= false;
	public static boolean				stopAllThread	= false;
	private static List<Thread>			threadList		= new ArrayList<>();

	public static void startWork() {
		// 启动N个生产者线程
		for (int i = 0; i < THREAD_COUNT; i++) {
			Thread t = new LBSDataGenerator(macList[i]);
			threadList.add(t);
			t.start();
		}
		// 轮询队列进行持久化
		Thread persisThread = new Thread() {
			public void run() {
				List<String> posList = new ArrayList<String>();
				while (!stopAllThread) {
					synchronized (posQueue) {
						while (!posQueue.isEmpty()) {
							posList.add(posQueue.poll());
						}
					}
					if (posList.size() > 0) {
						try {
							dbConn = DBUtil.getConnection();
							dbConn.setAutoCommit(true);
							String table = checkTable(dbConn);
							if (dbConn != null) {
								StringBuffer sql = new StringBuffer("insert into " + table + " (mac,building,floor,corx,cory,time) values");
								int size = posList.size();
								for (int i = 0; i < size; i++) {
									sql.append("(?,?,?,?,?,?),");
								}
								sql.deleteCharAt(sql.length() - 1);
								pstmt = dbConn.prepareStatement(sql.toString());
								int p = 1;
								for (String pos : posList) {
									JSONObject json = new JSONObject(pos);
									pstmt.setString(p++, json.getString("mac"));
									pstmt.setString(p++, json.getString("building"));
									pstmt.setString(p++, json.getString("floor"));
									pstmt.setDouble(p++, json.getDouble("corx"));
									pstmt.setDouble(p++, json.getDouble("cory"));
									pstmt.setLong(p++, json.getLong("time"));
								}
								pstmt.executeUpdate();
								pstmt.close();
								dbConn.close();
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					posList.clear();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		threadList.add(persisThread);
		persisThread.start();
		// 启动监控线程定期扫描所有用户并剔除过期的用户
		Thread monitorThread = new Thread() {
			public void run() {
				while (!stopAllThread) {
					Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
					redis.select(3);
					Map<String, String> users = redis.hgetAll("user_hashset");
					for (String user : users.keySet()) {
						if (!redis.exists(user)) {
							redis.hdel("user_hashset", user);
						}
					}
					redis.close();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		threadList.add(monitorThread);
		monitorThread.start();
		workStarted = true;
	}

	public static void stopWork() {
		if (workStarted) {
			stopAllThread = true;
			// 阻塞等待所有线程退出
			for (Thread t : threadList) {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String checkTable(Connection dbConn) throws SQLException {
		Date date = new Date(System.currentTimeMillis());
		String tableName = "track_" + dateFormat.format(date);
		String sql = "select count(id) from table_list where table_name = ?";
		pstmt = dbConn.prepareStatement(sql);
		pstmt.setString(1, tableName);
		rs = pstmt.executeQuery();
		rs.first();
		int count = rs.getInt(1);
		rs.close();
		pstmt.close();
		if (count == 0) {
			sql = "create table " + tableName
					+ "(id int(10) primary key auto_increment,mac varchar(255),building varchar(255),floor varchar(255),corx double,cory double,time bigint)Engine MyISAM";
			pstmt = dbConn.prepareStatement(sql);
			pstmt.execute();
			pstmt.close();
			sql = "insert into table_list set table_name=?,add_time=?";
			pstmt = dbConn.prepareStatement(sql);
			pstmt.setString(1, tableName);
			pstmt.setLong(2, System.currentTimeMillis());
			pstmt.execute();
			pstmt.close();
		}
		return tableName;
	}
}

class LBSDataGenerator extends Thread {
	private String				mac;
	private double	x, y;
	private double	MAX_X	= 1200;
	private double	MAX_Y	= 505;
	private double	distance	= 0;	// 沿着某一方向行走的距离
	private double	MAX_DISTANCE	= 90;

	private enum DIRECTION {
		LEFT, RIGHT, UP, DOWN
	};

	private DIRECTION	direction;
	private double		step	= 30;
	private DIRECTION	DIRECTION_VERTICAL	= DIRECTION.DOWN;

	public LBSDataGenerator(String mac) {
		this.mac = mac;
		double[] initPos = getInitPosition();
		x = initPos[0];
		y = initPos[1];
	}

	public void run() {
		while (!LBSDataFaker.stopAllThread) {
			double[] pos = getNextPosition();
			JSONObject posCur = new JSONObject();
			posCur.put("mac", mac);
			posCur.put("building", "jinyuan");
			posCur.put("floor", "F1");
			posCur.put("corx", pos[0]);
			posCur.put("cory", pos[1]);
			long curTime = System.currentTimeMillis();
			posCur.put("time", curTime);
			Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
			redis.select(Configs.REDIS_DB_INDEX);
			redis.rpush(mac, posCur.toString());
			redis.hset("user_hashset", mac, curTime + "");
			long size = redis.llen(mac);
			String posLast = null;
			// 保存滑动窗口大小的位置，其余作为历史数据持久化到mysql
			if (size > Configs.TRACK_WINDOW_SIZE) {
				posLast = redis.lpop(mac);
			}
			// 设置过期时间
			redis.expire(mac, 10 * 60);
			redis.close();
			if (posLast != null) {
				synchronized (LBSDataFaker.posQueue) {
					LBSDataFaker.posQueue.offer(posLast);
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public double[] getInitPosition() {
		int x = (int) MAX_X;
		int y = (int) MAX_Y;
		Random rand = new Random();
		double[] res = new double[2];
		res[0] = rand.nextInt(x);
		res[1] = rand.nextInt(y);
		direction = DIRECTION.RIGHT;
		return res;
	}

	public double[] getNextPosition() {
		if(DIRECTION_VERTICAL == DIRECTION.DOWN && y+step>=MAX_Y){
			DIRECTION_VERTICAL = DIRECTION.UP;
		}else if(DIRECTION_VERTICAL == DIRECTION.UP && y-step<=0){
			DIRECTION_VERTICAL = DIRECTION.DOWN;
		}
		switch (direction) {
		case LEFT:
			x -= step;
			break;
		case RIGHT:
			x += step;
			break;
		case UP:
			y -= step;
			distance += step;
			break;
		case DOWN:
			y += step;
			distance += step;
			break;
		default:
			break;
		}

		switch (direction) {
		case LEFT:
			if (x - step <= 0) {
				x = 10;
				if (DIRECTION_VERTICAL == DIRECTION.DOWN)
					direction = DIRECTION.DOWN;
				else {
					direction = DIRECTION.UP;
				}
			}
			break;
		case RIGHT:
			if (x + step >= MAX_X) {
				x = MAX_X - 10;
				if (DIRECTION_VERTICAL == DIRECTION.DOWN)
					direction = DIRECTION.DOWN;
				else {
					direction = DIRECTION.UP;
				}
			}
			break;
		case UP:
			if (distance >= MAX_DISTANCE) {
				if (y - step <= 0) {
					y = 10;
				}
				if (x + step >= MAX_X)
					direction = DIRECTION.LEFT;
				else
					direction = DIRECTION.RIGHT;
				distance = 0;
			}
			break;
		case DOWN:
			if (distance >= MAX_DISTANCE) {
				if (y + step >= MAX_Y) {
					y = MAX_Y - 10;
				}
				if (x + step >= MAX_X)
					direction = DIRECTION.LEFT;
				else
					direction = DIRECTION.RIGHT;
				distance = 0;
			}
			break;
		default:
			break;
		}
		double[] res = new double[2];
		res[0] = x;
		res[1] = y;
		return res;
	}

	public double[] getRandPosition() {
		int x = (int) MAX_X;
		int y = (int) MAX_Y;
		Random rand = new Random();
		double[] res = new double[2];
		res[0] = rand.nextInt(x);
		res[1] = rand.nextInt(y);
		return res;
	}
}
