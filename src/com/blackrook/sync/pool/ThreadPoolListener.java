/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.sync.pool;

/**
 * Listener archetype for ThreadPools.
 * @author Matthew Tropiano
 */
public interface ThreadPoolListener<T extends Runnable>
{
	/**
	 * Called when a task is enqueued.
	 */
	public void taskEnqueued(ThreadPoolEvent<T> event);

	/**
	 * Called when a task is started.
	 */
	public void taskStarted(ThreadPoolEvent<T> event);

	/**
	 * Called when a task finishes.
	 */
	public void taskFinished(ThreadPoolEvent<T> event);

	/**
	 * Called when a task throws an exception that isn't caught.
	 */
	public void taskError(ThreadPoolExceptionEvent<T> event);
}
