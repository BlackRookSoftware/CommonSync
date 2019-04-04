/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.sync.pool;

/**
 * Thread pool event. Fired from ThreadPools.
 * @author Matthew Tropiano
 */
public class ThreadPoolEvent<T extends Runnable>
{
	/** Reference to the runnable in which this event happened. */
	private T runRef;

	public ThreadPoolEvent(T r)
	{
		runRef = r;
	}

	public final T getRunnable()
	{
		return runRef;
	}

	
}
