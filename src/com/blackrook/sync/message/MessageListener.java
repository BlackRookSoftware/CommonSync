/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.sync.message;

/**
 * An interface that describes a listener for broadcast messages via
 * {@link MessageBroadcaster}.
 * @author Matthew Tropiano
 */
public interface MessageListener
{
	/**
	 * Called when a message gets sent to this object. 
	 * @param messageType the message type sent.
	 * @param args the arguments on the message.
	 */
	public void onMessageReceive(String messageType, Object ... args);
	
	/**
	 * Returns the list of message types that this object responds to.
	 * It's a good idea do this in order to keep things running efficiently.
	 * @return a list of message types, or null or a blank array to mean "accepts all".
	 */
	public String[] getMessageTypes();
	
}
