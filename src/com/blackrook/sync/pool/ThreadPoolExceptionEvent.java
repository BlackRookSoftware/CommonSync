/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.sync.pool;

/**
 * Thread pool event for when a runnable throws an uncaught exception. Fired from ThreadPools.
 * @author Matthew Tropiano
 */
public class ThreadPoolExceptionEvent<T extends Runnable> extends ThreadPoolEvent<T>
{
	/** Reference to the exception that was thrown. */
	private Exception exceptionRef;
	
	public ThreadPoolExceptionEvent(T r, Exception e)
	{
		super(r);
		exceptionRef = e;
	}

	public final Exception getException()
	{
		return exceptionRef;
	}


	
}
