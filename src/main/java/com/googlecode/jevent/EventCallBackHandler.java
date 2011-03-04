package com.googlecode.jevent;

import java.nio.channels.SelectableChannel;

public interface EventCallBackHandler {
	public void callback(SelectableChannel channel, int interestEvent,
			Object... args);
}
