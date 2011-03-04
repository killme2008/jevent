package com.googlecode.jevent;

import java.util.LinkedList;
import java.util.PriorityQueue;

public class EventBase {
	public LinkedList<Event> eventQueue;

	public LinkedList<Event>[] activeEventQueues;

	public PriorityQueue<Event> timeHeap;

	public int activeEventCount;

	public EventOp eventOp;

	public boolean eventBreak;

	public long timeCache;

	public EventBase() {
		this.eventQueue = new LinkedList<Event>();
		this.timeHeap = new PriorityQueue<Event>();
		this.activeEventQueues = new LinkedList[5];
		for (int i = 0; i < 5; i++) {
			this.activeEventQueues[i] = new LinkedList<Event>();
		}
	}
}
