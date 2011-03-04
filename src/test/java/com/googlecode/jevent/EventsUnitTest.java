package com.googlecode.jevent;

import org.junit.Test;
import static org.junit.Assert.*;

public class EventsUnitTest {
	@Test
	public void testAnd() {
		int events = 0;

		events |= Event.EV_ACCEPT;
		assertTrue((events & Event.EV_ACCEPT) == Event.EV_ACCEPT);
		System.out.println(events);

		events |= Event.EV_CONNECT;
		assertTrue((events & Event.EV_ACCEPT) == Event.EV_ACCEPT);
		assertTrue((events & Event.EV_CONNECT) == Event.EV_CONNECT);
		System.out.println(events);

		events |= Event.EV_READ;
		assertTrue((events & Event.EV_ACCEPT) == Event.EV_ACCEPT);
		assertTrue((events & Event.EV_CONNECT) == Event.EV_CONNECT);
		assertTrue((events & Event.EV_READ) == Event.EV_READ);
		System.out.println(events);

		events |= Event.EV_WRITE;
		assertTrue((events & Event.EV_ACCEPT) == Event.EV_ACCEPT);
		assertTrue((events & Event.EV_CONNECT) == Event.EV_CONNECT);
		assertTrue((events & Event.EV_READ) == Event.EV_READ);
		assertTrue((events & Event.EV_WRITE) == Event.EV_WRITE);
		System.out.println(events);

		events |= Event.EV_PERSIST;
		assertTrue((events & Event.EV_ACCEPT) == Event.EV_ACCEPT);
		assertTrue((events & Event.EV_CONNECT) == Event.EV_CONNECT);
		assertTrue((events & Event.EV_READ) == Event.EV_READ);
		assertTrue((events & Event.EV_WRITE) == Event.EV_WRITE);
		assertTrue((events & Event.EV_PERSIST) == Event.EV_PERSIST);
		System.out.println(events);

		events &= ~Event.EV_ACCEPT;
		assertTrue((events & Event.EV_ACCEPT) == 0);
		assertEquals((events & Event.EV_CONNECT), Event.EV_CONNECT);
		assertTrue((events & Event.EV_READ) == Event.EV_READ);
		assertTrue((events & Event.EV_WRITE) == Event.EV_WRITE);
		assertTrue((events & Event.EV_PERSIST) == Event.EV_PERSIST);
		System.out.println(events);

		events |= Event.EV_ACCEPT;
		assertTrue((events & Event.EV_ACCEPT) == Event.EV_ACCEPT);
		assertTrue((events & Event.EV_CONNECT) == Event.EV_CONNECT);
		assertTrue((events & Event.EV_READ) == Event.EV_READ);
		assertTrue((events & Event.EV_WRITE) == Event.EV_WRITE);
		assertTrue((events & Event.EV_PERSIST) == Event.EV_PERSIST);
		System.out.println(events);

		events &= ~Event.EV_PERSIST;
		assertTrue((events & Event.EV_ACCEPT) == Event.EV_ACCEPT);
		assertTrue((events & Event.EV_CONNECT) == Event.EV_CONNECT);
		assertTrue((events & Event.EV_READ) == Event.EV_READ);
		assertTrue((events & Event.EV_WRITE) == Event.EV_WRITE);
		assertFalse((events & Event.EV_PERSIST) == Event.EV_PERSIST);

		events &= ~Event.EV_READ;
		events &= ~Event.EV_WRITE;
		assertTrue((events & Event.EV_ACCEPT) == Event.EV_ACCEPT);
		assertTrue((events & Event.EV_CONNECT) == Event.EV_CONNECT);
		assertFalse((events & Event.EV_READ) == Event.EV_READ);
		assertFalse((events & Event.EV_WRITE) == Event.EV_WRITE);
		assertFalse((events & Event.EV_PERSIST) == Event.EV_PERSIST);

		events &= ~Event.EV_CONNECT;
		events &= ~Event.EV_ACCEPT;
		assertFalse((events & Event.EV_ACCEPT) == Event.EV_ACCEPT);
		assertFalse((events & Event.EV_CONNECT) == Event.EV_CONNECT);
		assertFalse((events & Event.EV_READ) == Event.EV_READ);
		assertFalse((events & Event.EV_WRITE) == Event.EV_WRITE);
		assertFalse((events & Event.EV_PERSIST) == Event.EV_PERSIST);

		assertEquals(0,events);
	}
}
