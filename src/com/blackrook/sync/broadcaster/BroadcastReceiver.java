/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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
