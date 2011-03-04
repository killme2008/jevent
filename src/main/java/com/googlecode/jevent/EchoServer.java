package com.googlecode.jevent;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public class EchoServer {
	private static final class AcceptEventHandler implements
			EventCallBackHandler {
		private final class ReadEventHandler implements EventCallBackHandler {
			private final class WriteEventHandler implements
					EventCallBackHandler {
				private final ByteBuffer buffer;

				private WriteEventHandler(ByteBuffer buffer) {
					this.buffer = buffer;
				}

				public void callback(SelectableChannel channel,
						int interestEvent, Object... args) {
					try {
						((WritableByteChannel) channel).write(buffer);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}

			public void callback(SelectableChannel channel, int interestEvent,
					Object... args) {
				try {
					final ByteBuffer buffer = ByteBuffer.allocate(100);
					((ReadableByteChannel) channel).read(buffer);
					buffer.flip();

					Event writeEvent = jevent.newEvent(channel, Event.EV_WRITE,
							new WriteEventHandler(buffer));
					jevent.addEvent(writeEvent, -1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private final JEvent jevent;

		private AcceptEventHandler(JEvent jevent) {
			this.jevent = jevent;
		}

		public void callback(SelectableChannel channel, int interestEvent,
				Object... args) {
			if ((interestEvent & Event.EV_ACCEPT) == Event.EV_ACCEPT) {
				try {
					ServerSocketChannel serverChannel = (ServerSocketChannel) channel;
					SocketChannel socketChannel = serverChannel.accept();
					System.out.println(socketChannel.socket()
							.getRemoteSocketAddress());
					socketChannel.configureBlocking(false);
					Event event = jevent.newEvent(socketChannel, Event.EV_READ
							| Event.EV_PERSIST, new ReadEventHandler());
					jevent.addEvent(event, -1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		final JEvent jevent = new JEventImpl();
		jevent.initEvent();
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.socket().bind(new InetSocketAddress(8080));
		Event acceptEvent = jevent.newEvent(serverSocketChannel,
				Event.EV_ACCEPT | Event.EV_PERSIST, new AcceptEventHandler(
						jevent));
		jevent.addEvent(acceptEvent, 2000);
		System.out.println("server will started at port ..." + 8080);
		jevent.eventLoop();
	}
}
