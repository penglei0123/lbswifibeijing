package com.genepoint.test;

import java.io.FileReader;
import java.io.LineNumberReader;

import redis.clients.jedis.Jedis;

import com.genepoint.custom.Configs;

public class TestRedis {
	public static void main(String args[]) throws Exception {
		LineNumberReader lineNumberReader = new LineNumberReader(new FileReader("test_data/list1.dat"));
		String line = null;
		Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
		String key = "B_0101_15167196C0F";
		while ((line = lineNumberReader.readLine()) != null) {
			String mac = line.split("\\s+")[1];
			redis.sadd(key, mac);
		}
		redis.sadd("building_set", key);
		redis.close();
		lineNumberReader.close();
	}
}
