package com.googlecode.jevent;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import com.googlecode.jevent.util.Constants;


public interface JEvent extends Constants{
	public void initEvent()throws IOException;

	public void eventLoop()throws IOException;

	public void addEvent(Event event,long timeout)throws IOException;

	public void delEvent(Event event)throws IOException;
	
	public Event newEvent(SelectableChannel channel,int events,EventCallBackHandler eventCallBackHandler,Object...args);

}
