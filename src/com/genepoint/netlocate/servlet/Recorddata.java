package com.genepoint.netlocate.servlet;

public class Recorddata {
	public long time;
	public String apMac;
	public String phoneMac;
	public Integer rssi;
	public int channel;

	public Recorddata(long time, String apMac, String phoneMac, Integer rssi) {
		super();
		this.time = time;
		this.apMac = apMac;
		this.phoneMac = phoneMac;
		this.rssi = rssi;
	}

	public Recorddata(long time, String apMac, String phoneMac, Integer rssi, int channel) {
		super();
		this.time = time;
		this.apMac = apMac;
		this.phoneMac = phoneMac;
		this.rssi = rssi;
		this.channel = channel;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getApMac() {
		return apMac;
	}

	public void setApMac(String apMac) {
		this.apMac = apMac;
	}

	public String getPhoneMac() {
		return phoneMac;
	}

	public void setPhoneMac(String phoneMac) {
		this.phoneMac = phoneMac;
	}

	public Integer getRssi() {
		return rssi;
	}

	public void setRssi(Integer rssi) {
		this.rssi = rssi;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

}

