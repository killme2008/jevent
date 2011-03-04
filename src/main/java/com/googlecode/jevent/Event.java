package com.googlecode.jevent;

import java.nio.channels.SelectableChannel;
import java.util.concurrent.atomic.AtomicInteger;

import com.googlecode.jevent.util.Constants;

public class Event implements Comparable<Event>, Constants {
	EventBase eventBase;

	SelectableChannel channel;

	int events;

	int ncalls;

	AtomicInteger pncalls;

	int res;

	int flags = EVLIST_INIT;

	long timeoutStamp;

	int priority;

	Object[] args;

	EventCallBackHandler eventCallBackHandler;

	public int compareTo(Event o) {
		if (this.timeoutStamp > o.timeoutStamp)
			return 1;
		else if (this.timeoutStamp == o.timeoutStamp)
			return 0;
		else
			return -1;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public EventBase getEventBase() {
		return eventBase;
	}

	public SelectableChannel getChannel() {
		return channel;
	}

	public int getEvents() {
		return events;
	}

	public int getNcalls() {
		return ncalls;
	}

	public int getRes() {
		return res;
	}

	public EventCallBackHandler getEventCallBackHandler() {
		return eventCallBackHandler;
	}

}
