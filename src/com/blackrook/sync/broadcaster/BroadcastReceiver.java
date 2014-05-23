package com.blackrook.sync.broadcaster;

/**
 * A hook for receiving broadcast messages.
 * @author Matthew Tropiano
 */
public interface BroadcastReceiver<M extends Object>
{

	/**
	 * Called on an incoming message from an attached broadcaster.
	 * @param message the incoming message.
	 */
	public void receiveBroadcast(M message);
	
}
