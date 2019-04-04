/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.sync;

import com.blackrook.commons.util.ThreadUtils;
import com.blackrook.sync.pool.ThreadPool;

/**
 * A runnable task that is supposed to be executed asynchronously,
 * provides feedback in some form, and is cancellable by another thread.
 * <p>
 * Task state policy, and what each function should return:
 * <table>
 * <thead>
 * 		<tr>
 * 			<th>State</th>
 * 			<th>isReady()</th>
 * 			<th>isDone()</th>
 * 			<th>isCancelled()</th>
 * 		</tr>
 * </thead>
 * <tbody>
 * 		<tr>
 * 			<td>Task is ready, not executed yet.</td>
 * 			<td><b>true</b></td>
 * 			<td>false</td>
 * 			<td>false</td>
 * 		</tr>
 * 		<tr>
 * 			<td>Task is executed, and running.</td>
 * 			<td>false</td>
 * 			<td>false</td>
 * 			<td>false</td>
 * 		</tr>
 * 		<tr>
 * 			<td>Task has finished running.</td>
 * 			<td>false</td>
 * 			<td><b>true</b></td>
 * 			<td>false</td>
 * 		</tr>
 * 		<tr>
 * 			<td>Task is still running, but {@link #cancel()} was called on it.</td>
 * 			<td>false</td>
 * 			<td>false</td>
 * 			<td><b>true</b></td>
 * 		</tr>
 * 		<tr>
 * 			<td>Task has finished running, but {@link #cancel()} was called on it.</td>
 * 			<td>false</td>
 * 			<td><b>true</b></td>
 * 			<td><b>true</b></td>
 * 		</tr>
 * </tbody>
 * </table> 
 * </p>
 * <p>Once a task has been executed, it cannot be executed again.</p>
 * <p>Any and all exceptions or throwable objects thrown during execution are caught and returned via {@link #hasThrowable()}
 * once the task has completed.</p>
 * <p>It is the sole responsibility of the implementor to utilize {@link #cancel()} via {@link #isCancelled()}.</p>
 * @author Matthew Tropiano
 * @since 2.1.0
 */
public abstract class Task implements Runnable
{

	/** Task status: Ready. */
	private boolean ready;
	/** Task status: Done. */
	private boolean done;
	/** Task status: Cancelled. */
	private boolean cancelled;
	/** Any and all things throwable that occurred during execution. */
	private Throwable throwable;
	/** Task status: progress value. */
	private float progress;
	/** Task status: max progress value. */
	private float progressMax;

	/**
	 * Creates a new task in the ready state.
	 */
	public Task()
	{
		ready = true;
		done = false;
		cancelled = false;
		throwable = null;
		progress = 0f;
		progressMax = 0f;
	}
	
	@Override
	public final void run()
	{
		if (ready)
		{
			ready = false;
			done = false;
			try {
				doTask();
			} catch (Throwable t) {
				throwable = t;
			}
			done = true;
		}
	}

	/**
	 * Entry point into the task.
	 * Once a thread enters this method via {@link #run()}, it has left the "ready" state. 
	 */
	protected abstract void doTask() throws Throwable;
	
	/**
	 * Tells the task that it must be cancelled.
	 */
	public final void cancel()
	{
		cancelled = true;
	}

	/**
	 * Returns true if the task hasn't been executed yet, false otherwise.
	 */
	public final boolean isReady()
	{
		return ready;
	}
	
	/**
	 * Returns true if the task is currently running, false if not.
	 */
	public final boolean isRunning()
	{
		return !isReady() && !isDone();
	}

	/**
	 * Returns true if the task is finished running, false if not.
	 */
	public final boolean isDone()
	{
		return done;
	}
	
	/**
	 * Returns true if the task has had {@link #cancel()} called on it.
	 * NOTE: This does not mean the task has finished running! {@link #isDone()}
	 * should be called in order to figure that out.
	 */
	public final boolean isCancelled()
	{
		return cancelled;
	}
	
	/**
	 * Sets the current progress value of this task.
	 * @since 2.4.0
	 */
	protected void setProgress(float progress)
	{
		this.progress = progress;
	}

	/**
	 * Sets the max progress value of this task.
	 * @since 2.4.0
	 */
	protected void setProgressMax(float progressMax)
	{
		this.progressMax = progressMax;
	}

	/**
	 * Gets the current progress value of this task.
	 * @since 2.4.0
	 */
	public final float getProgress()
	{
		return progress;
	}

	/**
	 * Gets the max progress value of this task.
	 * @since 2.4.0
	 */
	public final float getProgressMax()
	{
		return progressMax;
	}

	/**
	 * Returns a progress value equal to <code>{@link #getProgress()} / {@link #getProgressMax()}</code>. 
	 * @since 2.4.0
	 */
	public final float getTotalProgress()
	{
		return progress == progressMax ? 1f : (progressMax != 0f ? progress / progressMax : 0f);
	}
	
	/**
	 * Returns true if a {@link Throwable} was thrown during execution, or false if nothing was thrown.
	 */
	public final boolean hasThrowable()
	{
		return throwable != null;
	}
	
	/**
	 * Returns the @link {@link Throwable} that was thrown during execution, or null if nothing was thrown.
	 */
	public final Throwable getThrowable()
	{
		return throwable;
	}
	
	/**
	 * Convenience method for <code>spawn(this)</code>.
	 * @see #spawn(Task)
	 */
	public void spawn()
	{
		spawn(false);
	}
	
	/**
	 * Convenience method for <code>spawn(this, daemon)</code>.
	 * @see #spawn(Task, boolean)
	 */
	public void spawn(boolean daemon)
	{
		Task.spawn(this, daemon);
	}
	
	/**
	 * Makes the current thread wait until the task completes, 
	 * either with an exception, cancellation, or successful
	 * finish.
	 * <p>
	 * NOTE: Do NOT call this without ensuring that the task will start,
	 * or this will wait forever!
	 * </p>
	 * @since 2.2.0
	 */
	public void waitFor()
	{
		while (!isDone()) ThreadUtils.sleep(1);
	}
	
	/**
	 * Spawns a runnable asynchronously from the current thread to be monitored
	 * from outside.
	 * <p>
	 * NOTE: This spawns a new {@link Thread} and should not be used often, as
	 * creation of a new Thread can be expensive. If you must run many tasks outside
	 * of the main thread, consider using {@link ThreadPool}.
	 * </p> 
	 * @param runnable the runnable to run.
	 * @return a new Task for monitoring execution.
	 */
	public static final Task spawn(Runnable runnable)
	{
		Task out = new WrappedRunnableTask(runnable);
		Thread t = new Thread(new WrappedRunnableTask(runnable));
		t.start();
		return out;
	}
	
	/**
	 * Runs a Task asynchronously from the current thread to be monitored
	 * from outside. The task is NOT a daemon task, and will keep the JVM running
	 * until it ends. 
	 * <p>
	 * NOTE: This spawns a new {@link Thread} and should not be used often, as
	 * creation of a new Thread can be expensive. If you must run many tasks outside
	 * of the main thread, consider using {@link ThreadPool}.
	 * </p> 
	 * @param task the task to run.
	 * @return the task itself.
	 */
	public static final Task spawn(Task task)
	{
		return spawn(task, false);
	}
	
	/**
	 * Runs a Task asynchronously from the current thread to be monitored
	 * from outside. Tasks set spawned as daemon tasks will be killed when non-daemon 
	 * JVM threads end. 
	 * <p>
	 * NOTE: This spawns a new {@link Thread} and should not be used often, as
	 * creation of a new Thread can be expensive. If you must run many tasks outside
	 * of the main thread, consider using {@link ThreadPool}.
	 * </p> 
	 * @param task the task to run.
	 * @param daemon is this a daemon task? true if so, false if not.
	 * @return the task itself.
	 */
	public static final Task spawn(Task task, boolean daemon)
	{
		Thread t = new Thread(task);
		t.setDaemon(daemon);
		t.start();
		return task;
	}

	/**
	 * A special task that is a wrapping of a runnable.
	 */
	private static class WrappedRunnableTask extends Task
	{
		private Runnable runnable;

		/** Creates a new wrapped task. */
		WrappedRunnableTask(Runnable runnable)
		{
			super();
			this.runnable = runnable;
		}
		
		@Override
		protected void doTask() throws Throwable
		{
			runnable.run();
		}
		
	}
	
}
