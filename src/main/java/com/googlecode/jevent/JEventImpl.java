package com.googlecode.jevent;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class JEventImpl implements JEvent {

	private EventBase eventBase;

	private EventOp eventOp;

	private volatile boolean done;

	public void addEvent(Event event, long timeout) throws IOException {
		EventBase evBase = event.eventBase;
		EventOp evOp = evBase.eventOp;

		if (isValidEvent(event) && !isActiveEvent(event)) {
			evOp.add(event);
			eventQueueInsert(event, EVLIST_INSERTED);
		}
		if (timeout > 0) {
			if ((event.flags & EVLIST_TIMEOUT) > 0) {
				eventQueueRemove(event, EVLIST_TIMEOUT);
			}
			if ((event.flags & EVLIST_ACTIVE) > 0
					&& (event.res & EV_TIMEOUT) > 0) {
				if (event.ncalls > 0 && event.pncalls!=null) {
					event.pncalls.set(0);
				}
				eventQueueRemove(event, EVLIST_ACTIVE);
			}
			long now = getTime(evBase);
			event.timeoutStamp = now + timeout;
			eventQueueInsert(event, EVLIST_TIMEOUT);
		}

	}

	private boolean isActiveEvent(Event event) {
		return ((event.flags & (EVLIST_INSERTED | EVLIST_ACTIVE)) > 0);
	}

	private boolean isValidEvent(Event event) {
		return (event.events & (EV_READ | EV_WRITE | EV_ACCEPT | EV_CONNECT)) > 0;
	}

	public Event newEvent(SelectableChannel channel, int events,
			EventCallBackHandler eventCallBackHandler, Object... args) {
		Event event = new Event();
		event.eventBase = this.eventBase;
		event.eventCallBackHandler = eventCallBackHandler;
		event.args = args;
		event.events = events;
		event.channel = channel;
		event.res = 0;
		event.flags = EVLIST_INIT;
		event.ncalls = 0;
		event.pncalls = null;
		if (this.eventBase != null) {
			event.priority = eventBase.activeEventQueues.length / 2;
		}
		return event;
	}

	public void delEvent(Event event) throws IOException {
		EventBase evBase = event.eventBase;
		EventOp evOp = evBase.eventOp;
		if (event.eventBase == null)
			return;
		// 从执行循环中跳出
		if (event.ncalls > 0 && event.pncalls!=null) {
			event.pncalls.set(0);
		}
		if ((event.flags & EVLIST_TIMEOUT) > 0) {
			eventQueueRemove(event, EVLIST_TIMEOUT);
		}
		if ((event.flags & EVLIST_ACTIVE) > 0) {
			eventQueueRemove(event, EVLIST_ACTIVE);
		}
		if ((event.flags & EVLIST_INSERTED) > 0) {
			eventQueueRemove(event, EVLIST_INSERTED);
			evOp.delete(event);
		}
	}

	public void eventLoop() throws IOException {
		try {
			this.eventBase.timeCache = 0;
			while (!done) {
				long selectionTimeout = timeoutNext();
				// 清空时间缓存
				this.eventBase.timeCache = 0;
				this.eventOp.dispatch(this.eventBase, selectionTimeout);
				// 获取当前时间
				this.eventBase.timeCache=getTime(this.eventBase);
				processTimeout();
				processActive();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private long timeoutNext() {
		long selectionTimeout = 1000L;
		if (this.eventBase.activeEventCount > 0) {
			selectionTimeout = -1;
		} else {
			Event timeoutEvent = eventBase.timeHeap.peek();
			if (timeoutEvent != null) {
				long now = getTime(this.eventBase);
				if (timeoutEvent.timeoutStamp < now) {
					selectionTimeout = -1L;
				} else
					selectionTimeout = timeoutEvent.timeoutStamp - now;
			}
		}
		return selectionTimeout;
	}

	private void processActive() throws IOException {
		LinkedList<Event> activeq = null;
		for (int i = this.eventBase.activeEventQueues.length - 1; i >= 0; i--) {
			if (this.eventBase.activeEventQueues[i].peek() != null) {
				activeq = this.eventBase.activeEventQueues[i];
				break;
			}
		}
		if (activeq != null) {
			Event event = null;
			while ((event = activeq.peek()) != null) {
				if ((event.events & EV_PERSIST) > 0) {
					eventQueueRemove(event, EVLIST_ACTIVE);
				} else {
					delEvent(event);
				}
				AtomicInteger ncalls = new AtomicInteger(event.ncalls);
				event.pncalls = ncalls;
				while (ncalls.get() > 0) {
					event.ncalls = ncalls.decrementAndGet();
					event.eventCallBackHandler.callback(event.channel,
							event.res, event.args);
					if (eventBase.eventBreak)
						return;
				}
			}
		}
	}

	private void processTimeout() throws IOException {
		if (!this.eventBase.timeHeap.isEmpty()) {
			long now = getTime(this.eventBase);
			Event ev = null;
			while ((ev = this.eventBase.timeHeap.peek()) != null) {
				if (ev.timeoutStamp > now)
					break;
				delEvent(ev);
				activeEvent(ev, EV_TIMEOUT, 1);
			}
		}
	}

	public long getTime(EventBase base) {
		if (base.timeCache > 0) {
			return base.timeCache;
		}
		return System.currentTimeMillis();

	}

	public void activeEvent(Event event, int res, int ncalls) {
		if ((event.flags & EVLIST_ACTIVE) > 0) {
			event.flags |= res;
			return;
		}
		event.res = res;
		event.ncalls = ncalls;
		event.pncalls = null;
		eventQueueInsert(event, EVLIST_ACTIVE);
	}

	public void eventQueueInsert(Event event, int queue) {
		if ((event.flags & queue) > 0) {
			// 已经在Active队列，直接返回
			if ((event.flags & EVLIST_ACTIVE) > 0) {
				return;
			}
		}

		event.flags |= queue;
		switch (queue) {
		case EVLIST_INSERTED:
			this.eventBase.eventQueue.add(event);
			break;
		case EVLIST_ACTIVE:
			this.eventBase.activeEventCount++;
			this.eventBase.activeEventQueues[event.priority].add(event);
			break;
		case EVLIST_TIMEOUT:
			this.eventBase.timeHeap.add(event);
			break;
		default:
			throw new IllegalArgumentException("Unknow queue " + queue);
		}

	}

	public void eventQueueRemove(Event event, int queue) {
		if (!((event.flags & queue) > 0)) {
			throw new IllegalArgumentException("Event " + event
					+ " is not in queue " + queue);
		}
		event.flags &= ~queue;
		switch (queue) {
		case EVLIST_INSERTED:
			eventBase.eventQueue.remove(event);
			break;
		case EVLIST_ACTIVE:
			eventBase.activeEventCount--;
			eventBase.activeEventQueues[event.priority].remove(event);
			break;
		case EVLIST_TIMEOUT:
			this.eventBase.timeHeap.remove(event);
			break;
		default:
			throw new IllegalArgumentException("Unknow event queue " + queue);
		}

	}

	public void initEvent() throws IOException {
		eventOp = new SelectOp(this);
		eventBase = new EventBase();
		eventBase.eventOp = this.eventOp;
		this.eventOp.init();
	}

}
