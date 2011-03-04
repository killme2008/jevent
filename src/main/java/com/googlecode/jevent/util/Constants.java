package com.googlecode.jevent.util;

public interface Constants {

	public int EV_READ = 0x01;
	public int EV_WRITE = 0x02;
	public int EV_TIMEOUT = 0x04;
	public int EV_ACCEPT = 0x08;
	public int EV_CONNECT = 0x10;
	public int EV_PERSIST = 0x20;

	public int EVLIST_TIMEOUT = 0x01;
	public int EVLIST_INSERTED = 0x02;
	public int EVLIST_ACTIVE = 0x04;
	public int EVLIST_INTERNAL = 0x08;
	public int EVLIST_INIT = 0x10;

}
