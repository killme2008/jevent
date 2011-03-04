package com.googlecode.jevent;

import java.io.IOException;

import com.googlecode.jevent.util.Constants;


public interface EventOp extends Constants{
	public void init() throws IOException;

	public void add(Event event)throws IOException;

	public void delete(Event event)throws IOException;

	public int dispatch(EventBase eventBase, long timeout)throws IOException;

	public void destroy()throws IOException;
}
