package com.blackrook.sync.broadcaster;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.blackrook.commons.hash.Hash;
import com.blackrook.sync.pool.WorkPool;

/**
 * Broadcaster class for all sorts of message broadcasting to attached receivers.
 * <p>
 * All Broadcasters are thread-safe and wait for all broadcasts to complete before
 * adds and removes can happen, due to the {@link ReentrantReadWriteLock} that this uses.
 * The broadcast happens in the same thread that calls {@link #broadcast(Object)}, and does not offload
 * the message object in a separate thread. Best combined with a {@link WorkPool} for broadcasting
 * lots of messages asynchronously, with each spawned job doing the broadcast.
 * @param <M> the message type to broadcast to attached listeners.
 * @author Matthew Tropiano
 */
public class Broadcaster<M extends Object>
{
	/** Broadcaster name. */
	private String name;
	
	/** Set of user broadcast hooks. */
	private Hash<BroadcastReceiver<M>> receivers;
	
	/** Read lock for message broadcast. */
	private ReadLock readLock;
	/** Write lock for other stuff. */
	private WriteLock writeLock;

	public Broadcaster(String name)
	{
		this.name = name;
		
		receivers = new Hash<BroadcastReceiver<M>>(20);
		
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
	}
	
	/**
	 * Returns the name of this broadcaster.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Sends a message to all receivers.
	 * @param message the message to send.
	 */
	public void broadcast(M message)
	{
		// secure read lock to avoid write.
		readLock.lock();
		try {
			for (BroadcastReceiver<M> uh : receivers)
				uh.receiveBroadcast(message);
		} finally {
			// release read lock.
			readLock.unlock();
		}
	}
	
	/**
	 * Adds a receiver to this broadcaster.
	 * If this receiver was added, this returns false.
	 * @param receiver the receiver to add.
	 * @param groupIds the groups that the user belongs to.
	 * @return true if added, false if not.
	 */
	public boolean addReceiver(BroadcastReceiver<M> receiver)
	{
		if (receivers.contains(receiver))
			return false;
		
		writeLock.lock();
		try {
			if (receivers.contains(receiver))
				return false;
			receivers.put(receiver);
		} finally {
			writeLock.unlock();
		}
		
		return true;
	}


	/**
	 * Removes a user from this broadcaster.
	 * @param userId the user id to remove.
	 * @return true if removed, false if not.
	 */
	public boolean removeReceiver(BroadcastReceiver<M> receiver)
	{
		if (!receivers.contains(receiver))
			return false;
		
		writeLock.lock();
		try {
			if (!receivers.contains(receiver))
				return false;
			receivers.remove(receiver);
		} finally {
			writeLock.unlock();
		}
		
		return true;
	}
	
	/**
	 * Returns the amount of attached users.
	 */
	public int getReceverCount()
	{
		return receivers.size();
	}
	
	/**
	 * Returns true if this has no attached users.
	 */
	public boolean isEmpty()
	{
		return getReceverCount() == 0;
	}
	
}
