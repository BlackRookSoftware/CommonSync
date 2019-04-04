/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.sync.message;

import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.hash.HashedQueueMap;
import com.blackrook.commons.linkedlist.Queue;

/**
 * This is the broadcaster that sends messages to all registered listeners.
 * The broadcaster has a separate thread that it uses to send to the listeners.
 * @author Matthew Tropiano
 * @since 2.5.0
 */
public class MessageBroadcaster
{
	/** Message queue. */
	private Queue<Message> messageQueue;
	/** List of all listeners. */
	private Hash<MessageListener> allMessageListeners;

	/** List of "accept all" listeners. */
	private Queue<MessageListener> messageListeners;
	/** Map of "certain type" listeners. */
	private HashedQueueMap<String, MessageListener> messageListenerMap;
	/** Broadcaster thread. */
	private BroadcasterThread broadcaster;
	
	/**
	 * Creates a new message broadcaster.
	 */
	public MessageBroadcaster()
	{
		messageQueue = new Queue<Message>();
		allMessageListeners = new Hash<MessageListener>();
		messageListeners = new Queue<MessageListener>();
		messageListenerMap = new HashedQueueMap<String, MessageListener>();
		broadcaster = new BroadcasterThread();
		broadcaster.start();
	}

	/**
	 * Enqueues a message for broadcast.
	 * @param messageType the message type.
	 * @param arguments the message arguments.
	 */
	public void broadcast(String messageType, Object ... arguments)
	{
		synchronized (messageQueue)
		{
			messageQueue.enqueue(new Message(messageType, arguments));
			messageQueue.notify();
		}
	}
	
	/**
	 * Registers a listener with this broadcaster.
	 * @param listener the listener to add.
	 */
	public synchronized void registerListener(MessageListener listener)
	{
		if (allMessageListeners.contains(listener))
			return;
		
		String[] types = listener.getMessageTypes();
		
		if (types == null || types.length == 0)
		{
			synchronized (messageListeners)
			{
				messageListeners.enqueue(listener);
			}
		}
		else
		{
			synchronized (messageListenerMap)
			{
				for (String t : types)
					messageListenerMap.enqueue(t, listener);
			}
		}
		
		allMessageListeners.put(listener);
	}
	
	/**
	 * De-registers a listener from this broadcaster.
	 * @param listener the listener to remove.
	 */
	public synchronized void deregisterListener(MessageListener listener)
	{
		if (!allMessageListeners.contains(listener))
			return;
		
		String[] types = listener.getMessageTypes();
		
		if (types == null || types.length == 0)
		{
			synchronized (messageListeners)
			{
				messageListeners.remove(listener);
			}
		}
		else
		{
			synchronized (messageListenerMap)
			{
				for (String t : types)
					messageListenerMap.removeValue(t, listener);
			}
		}
		
		allMessageListeners.remove(listener);
	}
	
	/**
	 * Message object that gets passed to other listeners.
	 */
	private static class Message
	{
		String messageType;
		Object[] arguments;
		
		Message(String messageType, Object ... arguments)
		{
			this.messageType = messageType;
			this.arguments = arguments;
		}
	}
	
	/**
	 * The broadcaster thread.
	 */
	private class BroadcasterThread extends Thread
	{
		BroadcasterThread()
		{
			super("MessageBroadcaster");
			setDaemon(true);
		}
		
		@Override
		public void run()
		{
			while (true)
			{
				Message message = null;
				synchronized (messageQueue)
				{
					while (messageQueue.isEmpty())
						try {messageQueue.wait();	} catch (InterruptedException ex) {}
					message = messageQueue.dequeue();
				}
				
				synchronized (messageListeners)
				{
					for (MessageListener listener : messageListeners)
						listener.onMessageReceive(message.messageType, message.arguments);
				}

				synchronized (messageListenerMap)
				{
					Queue<MessageListener> listenerQueue = messageListenerMap.get(message.messageType);
					if (listenerQueue != null) for (MessageListener listener : listenerQueue)
						listener.onMessageReceive(message.messageType, message.arguments);
				}
			}
		}
		
	}
	
}
