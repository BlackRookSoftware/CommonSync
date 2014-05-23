/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.sync.pool;

import com.blackrook.commons.linkedlist.Queue;
import com.blackrook.commons.list.List;

/**
 * Thread pool object. This allocates a whole bunch of daemon 
 * threads, and makes them run a bunch of Runnables upon request.
 * @author Matthew Tropiano
 */
public class ThreadPool<T extends Runnable>
{
	
	/** Array of worker threads in the pool. */
	private WorkerThread<ThreadPool<T>>[] threads;
	/** Central queue of jobs to perform. */
	private Queue<T> queue;
	/** List of ThreadPool listeners. */
	private List<ThreadPoolListener<T>> listeners;
	/** The name of this ThreadPool. */
	private String poolName;
	
	private static final String threadName = "Thread";
	private static final String DEFAULT_POOL_NAME = "ThreadPool";
	
	/**
	 * Constructs a new ThreadPool using a number of threads.
	 * @param numThreads	the number of threads to create.
	 */
	public ThreadPool(int numThreads)
	{
		this(DEFAULT_POOL_NAME, numThreads);
	}
	
	/**
	 * Constructs a new ThreadPool using a number of threads.
	 * @param name			the name of this ThreadPool.
	 * @param numThreads	the number of threads to create.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ThreadPool(String name, int numThreads)
	{
		poolName = name; 
		threads = new WorkerThread[numThreads];
		queue = new Queue<T>();
		listeners = new List<ThreadPoolListener<T>>(5);
		for (int i = 0; i < numThreads; i++)
		{
			threads[i] = new WorkerThread(poolName+threadName+i);
			threads[i].setDaemon(true);
			threads[i].start();
		}
	}
	
	/**
	 * Interrupts all threads.
	 */
	public void interruptAll()
	{
		for (Thread t : threads)
			t.interrupt();
	}
	
	/**
	 * Returns how many threads are in a particular state.
	 */
	public int getCountInState(Thread.State state)
	{
		int x = 0;
		for (Thread t : threads)
			if (state == t.getState()) x++;
		return x;
	}
	
	/**
	 * Returns how many threads are running.
	 */
	public int getRunningCount()
	{
		return getCountInState(Thread.State.RUNNABLE);
	}
	
	/**
	 * Returns how many threads are waiting.
	 */
	public int getWaitingCount()
	{
		return getCountInState(Thread.State.WAITING);
	}
	
	/**
	 * Returns how many threads are in this pool.
	 */
	public int getCount()
	{
		return threads.length;
	}
	
	/**
	 * Allocates a runnable object to a thread and runs it.
	 * @param r		the runnable to use.
	 */
	public void execute(T r)
	{
		synchronized (queue)
		{
			queue.add(r);
			fireTaskEnqueuedEvent(r);
			queue.notify();
		}
	}

	/**
	 * Allocates a runnable object to a thread and runs it,
	 * then waits for it to stop.
	 * @param r		the runnable to use.
	 */
	public void executeAndWaitFor(T r)
	{
		synchronized (r)
		{
			execute(r);
			try {r.wait();	} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * Adds a listener to this ThreadPool.
	 * @param l		the listener to add.
	 */
	public void addThreadPoolListener(ThreadPoolListener<T> l)
	{
		listeners.add(l);
	}
	
	/**
	 * Removes a listener from this ThreadPool.
	 * @param l		the listener to add.
	 */
	public boolean removeThreadPoolListener(ThreadPoolListener<T> l)
	{
		return listeners.remove(l);
	}

	@Override
	public void finalize() throws Throwable
	{
		interruptAll();
		super.finalize();
	}
	
	/**
	 * Calls "task enqueued" on all bound listeners.
	 * @param r	the runnable task that was enqueued via execute().
	 */
	protected void fireTaskEnqueuedEvent(T r)
	{
		for (ThreadPoolListener<T> listener : listeners)
			listener.taskEnqueued(new ThreadPoolEvent<T>(r));
	}
	
	/**
	 * Calls "task started" on all bound listeners.
	 * @param r	the runnable that got assigned to a thread.
	 */
	protected void fireTaskStartedEvent(T r)
	{
		for (ThreadPoolListener<T> listener : listeners)
			listener.taskStarted(new ThreadPoolEvent<T>(r));
	}
	
	/**
	 * Calls "task finished" on all bound listeners.
	 * @param r	the runnable that finished running on a thread.
	 */
	protected void fireTaskFinishedEvent(T r)
	{
		for (ThreadPoolListener<T> listener : listeners)
			listener.taskFinished(new ThreadPoolEvent<T>(r));
	}
	
	/**
	 * Calls "task error" on all bound listeners.
	 * @param r	the runnable that threw an uncaught exception.
	 */
	protected void fireTaskErrorEvent(T r, Exception e)
	{
		for (ThreadPoolListener<T> listener : listeners)
			listener.taskError(new ThreadPoolExceptionEvent<T>(r,e));
	}
	
	/**
	 * Worker threads for the pool.
	 */
	private class WorkerThread<U extends ThreadPool<T>> extends Thread
	{
		public WorkerThread(String name)
		{
			super(name);
		}
		
		public void run()
		{
			T runnable;
			
			while (true)
			{
				synchronized (queue)
				{
					while (queue.isEmpty())
					{
						try {queue.wait();	} catch (InterruptedException ex) {}
					}
					runnable = queue.dequeue();
				}
				
				try {
					fireTaskStartedEvent(runnable);
					synchronized (runnable)
					{
						runnable.run();
						runnable.notify();
					}
					fireTaskFinishedEvent(runnable);
				} catch (RuntimeException e) {
					fireTaskErrorEvent(runnable, e);
				}
			}
		}
	}
	
}
