/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.sync.pool;

/**
 * Adapter class for the ThreadPoolListener.
 * @author Matthew Tropiano
 */
public class ThreadPoolAdapter<T extends Runnable> implements ThreadPoolListener<T>
{
	@Override
	public void taskEnqueued(ThreadPoolEvent<T> event) 
	{
	}

	@Override
	public void taskError(ThreadPoolExceptionEvent<T> event)
	{
	}

	@Override
	public void taskFinished(ThreadPoolEvent<T> event)
	{
	}

	@Override
	public void taskStarted(ThreadPoolEvent<T> event)
	{
	}

}
