package com.googlecode.jevent;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



public class SelectOp implements EventOp {

	private Selector selector;

	private JEventImpl jEvent;

	public SelectOp(JEventImpl jEventImpl) {
		this.jEvent = jEventImpl;
	}

	private Map<SelectableChannel, Event> acceptEventByChannel = new HashMap<SelectableChannel, Event>();

	private Map<SelectableChannel, Event> readEventByChannel = new HashMap<SelectableChannel, Event>();

	private Map<SelectableChannel, Event> writeEventMapByChannel = new HashMap<SelectableChannel, Event>();

	public void add(Event event) throws IOException {
		if ((event.events & EV_ACCEPT) > 0) {
			SelectionKey key = event.channel.keyFor(selector);
			if (key == null) {
				key = event.channel.register(selector, SelectionKey.OP_ACCEPT);
			} else {
				key.interestOps(key.interestOps() | SelectionKey.OP_ACCEPT);
			}
			this.acceptEventByChannel.put(event.channel, event);
		} else if ((event.events & Event.EV_READ) > 0) {
			SelectionKey key = event.channel.keyFor(selector);
			if (key == null) {
				key = event.channel.register(selector, SelectionKey.OP_READ);
			} else {
				key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			}
			this.readEventByChannel.put(event.channel, event);
		} else if ((event.events & Event.EV_WRITE) > 0) {
			SelectionKey key = event.channel.keyFor(selector);
			if (key == null) {
				key = event.channel.register(selector, SelectionKey.OP_WRITE,
						event);
			} else {
				key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
			}
			this.writeEventMapByChannel.put(event.channel, event);
		}
	}

	public void delete(Event event) throws IOException {
		if ((event.events & Event.EV_READ) == Event.EV_READ) {
			SelectionKey key = event.channel.keyFor(selector);
			this.readEventByChannel.remove(event.channel);
			if (key != null) {
				key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
			}

		} else if ((event.events & Event.EV_WRITE) == EV_WRITE) {
			SelectionKey key = event.channel.keyFor(selector);
			this.writeEventMapByChannel.remove(event.channel);
			if (key != null) {
				key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
			}
		}
	}

	public void destroy() throws IOException {
		for (SelectableChannel channel : this.readEventByChannel.keySet()) {
			SelectionKey key = channel.keyFor(selector);
			if (key != null && key.isValid()) {
				key.cancel();
			}
		}
		for (SelectableChannel channel : this.writeEventMapByChannel.keySet()) {
			SelectionKey key = channel.keyFor(selector);
			if (key != null && key.isValid()) {
				key.cancel();
			}
		}
		this.selector.close();

	}

	public int dispatch(EventBase eventBase, long timeout) throws IOException {
		if (timeout > 0) {
			int n = selector.select(timeout);
			if (n > 0) {
				afterSelect();

			}
			return n;
		} else
			return 0;
	}

	private void afterSelect() {
		Set<SelectionKey> keySet = selector.selectedKeys();
		Iterator<SelectionKey> it = keySet.iterator();

		while (it.hasNext()) {
			SelectionKey key = it.next();
			Event readEvent = null, writeEvent = null, accptEvent = null;
			int res = 0;
			it.remove();
			if (!key.isValid()) {
				continue;
			}

			if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
				res |= EV_ACCEPT;
				accptEvent = this.acceptEventByChannel.get(key.channel());
			}
			if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
				res |= EV_READ;
				readEvent = this.readEventByChannel.get(key.channel());
			}
			if ((key.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
				res |= EV_WRITE;
				writeEvent = this.writeEventMapByChannel.get(key.channel());
			}

			if (accptEvent != null && (accptEvent.events & EV_ACCEPT) > 0) {
				jEvent.activeEvent(accptEvent, res & accptEvent.events, 1);
			}
			if (readEvent != null && (readEvent.events & EV_READ) > 0) {
				jEvent.activeEvent(readEvent, res & readEvent.events, 1);
			}
			if (writeEvent != null && (writeEvent.events & EV_WRITE) > 0) {
				jEvent.activeEvent(writeEvent, res & writeEvent.events, 1);
			}
		}
	}

	public void init() throws IOException {
		this.selector = Selector.open();

	}

}
