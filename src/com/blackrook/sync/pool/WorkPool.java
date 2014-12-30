package com.blackrook.sync.pool;

import com.blackrook.commons.linkedlist.Queue;
import com.blackrook.commons.list.List;

/**
 * A thread pool that expands in size to a certain limit, and shrinks 
 * back to the minimum when threads go unused.
 * <p>WorkPools are abstract, and require overriding of the {@link #createTaskFor(Object)}
 * which will create a runnable that gets dumped into the pool queue.
 * @author Matthew Tropiano
 * @since 2.5.0
 */
public abstract class WorkPool<R extends Object>
{
	/** Default worker thread prefix. */
	public static final String DEFAULT_WORKER_PREFIX = "Worker-";
	/** Default pool name. */
	public static final String DEFAULT_POOL_NAME = "WorkerPool";
	/** Default timeout. */
	public static final int DEFAULT_WORKER_TIMEOUT = 1000;
	
	/** Mutex for changing the available count. */
	private Integer AVAILABLE_MUTEX = new Integer(0);
	
	/** Reference to itself for workers. */
	private WorkPool<R> SELF = this;
	
	/** Central queue of jobs to perform. */
	private Queue<R> queue;
	/** Hash of worker threads in the pool. */
	private List<Worker> workers;
	/** List of pool listeners. */
	private Queue<WorkPoolListener<R>> listeners;

	/** The name of this thread pool. */
	private String name;
	/** The prefix of worker thread names of this thread pool. */
	private String workerNamePrefix;
	/** Are the generated threads daemon threads?. */
	private boolean daemonWorkers;

	/** Minimum amount of threads to spawn. */
	private int workerMin;
	/** Maximum amount of threads to spawn. */
	private int workerMax;
	/** Worker timeout. */
	private int workerTimeout;
	
	/** Current amount of available threads. */
	private int workerAvailableCount;
	/** Current amount of worker threads created. */
	private int workerCreatedCount;
	
	/** Shut down flag. */
	private boolean shutDown;

	/**
	 * Creates a new WorkerPool.
	 * When started, this spawns the amount of minimum threads that need to run. 
	 * @param workerMin the minimum amount of threads to spawn.
	 * @param workerMax the maximum amount of threads to spawn.
	 * @param poolListeners a list of listeners to add immediately to the pool.
	 */
	@SafeVarargs
	public WorkPool(int workerMin, int workerMax, WorkPoolListener<R> ...poolListeners)
	{
		this(DEFAULT_POOL_NAME, DEFAULT_WORKER_PREFIX, workerMin, workerMax, DEFAULT_WORKER_TIMEOUT, false, poolListeners);
	}

	/**
	 * Creates a new WorkerPool.
	 * When started, this spawns the amount of minimum threads that need to run. 
	 * @param workerMin the minimum amount of threads to spawn.
	 * @param workerMax the maximum amount of threads to spawn.
	 * @param workerTimeout the periodic timeout for worker threads waiting for a task.
	 * @param poolListeners a list of listeners to add immediately to the pool.
	 */
	@SafeVarargs
	public WorkPool(int workerMin, int workerMax, int workerTimeout, WorkPoolListener<R> ...poolListeners)
	{
		this(DEFAULT_POOL_NAME, DEFAULT_WORKER_PREFIX, workerMin, workerMax, workerTimeout, false, poolListeners);
	}

	/**
	 * Creates a new WorkerPool.
	 * When started, this spawns the amount of minimum threads that need to run. 
	 * @param workerMin the minimum amount of threads to spawn.
	 * @param workerMax the maximum amount of threads to spawn.
	 * @param daemon if true, all created threads are daemon threads.
	 * @param poolListeners a list of listeners to add immediately to the pool.
	 */
	@SafeVarargs
	public WorkPool(int workerMin, int workerMax, boolean daemon, WorkPoolListener<R> ...poolListeners)
	{
		this(DEFAULT_POOL_NAME, DEFAULT_WORKER_PREFIX, workerMin, workerMax, DEFAULT_WORKER_TIMEOUT, daemon, poolListeners);
	}

	/**
	 * Creates a new WorkerPool.
	 * When started, this spawns the amount of minimum threads that need to run. 
	 * @param workerMin the minimum amount of threads to spawn.
	 * @param workerMax the maximum amount of threads to spawn.
	 * @param workerTimeout the periodic timeout for worker threads waiting for a task.
	 * @param daemon if true, all created threads are daemon threads.
	 * @param poolListeners a list of listeners to add immediately to the pool.
	 */
	@SafeVarargs
	public WorkPool(int workerMin, int workerMax, int workerTimeout, boolean daemon, WorkPoolListener<R> ...poolListeners)
	{
		this(DEFAULT_POOL_NAME, DEFAULT_WORKER_PREFIX, workerMin, workerMax, workerTimeout, daemon, poolListeners);
	}

	/**
	 * Creates a new WorkerPool.
	 * When started, this spawns the amount of minimum threads that need to run. 
	 * @param poolName the name of the thread pool.
	 * @param workerNamePrefix the name prefix for each thread.
	 * @param workerMin the minimum amount of threads to spawn.
	 * @param workerMax the maximum amount of threads to spawn.
	 * @param workerTimeout the periodic timeout for worker threads waiting for a task.
	 * @param daemon if true, all created threads are daemon threads.
	 * @param poolListeners a list of listeners to add immediately to the pool.
	 */
	@SafeVarargs
	public WorkPool(String poolName, String workerNamePrefix, int workerMin, int workerMax, int workerTimeout, boolean daemon, WorkPoolListener<R> ...poolListeners)
	{
		queue = new Queue<R>();
		workers = new List<Worker>();
		listeners = new Queue<WorkPoolListener<R>>();
		
		this.name = poolName;
		this.workerNamePrefix = workerNamePrefix;
		this.workerMin = workerMin;
		this.workerMax = workerMax;
		this.daemonWorkers = daemon;
		
		for (WorkPoolListener<R> listener : poolListeners)
			addListener(listener);
		
		this.workerTimeout = workerTimeout;
		
		workerCreatedCount = 0;
		workerAvailableCount = 0;
		
		spawnNeeded();
	}

	/**
	 * Returns the name of this pool.
	 */
	public String getName() 
	{
		return name;
	}

	/**
	 * Returns the prefix string for each thread in the pool.
	 */
	public String getWorkerNamePrefix()
	{
		return workerNamePrefix;
	}

	/**
	 * If true, every spawned thread is a daemon thread (thread dies on main thread death).
	 */
	public boolean isDaemonWorkers()
	{
		return daemonWorkers;
	}

	/**
	 * Gets the minimum amount of threads to create.
	 */
	public int getWorkerMin() 
	{
		return workerMin;
	}

	
	/**
	 * Gets the maximum amount of threads to create.
	 */
	public int getWorkerMax()
	{
		return workerMax;
	}

	/**
	 * Gets the current amount of workers.
	 */
	public int getWorkerCount() 
	{
		return workers.size();
	}

	/**
	 * Returns the amount of available workers.
	 */
	public int getAvailableCount()
	{
		return workerAvailableCount;
	}
	
	/**
	 * Gets the amount of workers created.
	 */
	public int getWorkerCreatedCount() 
	{
		return workerCreatedCount;
	}

	/**
	 * Spawns a new worker.
	 */
	private void startWorker()
	{
		synchronized (workers)
		{
			Worker worker = new Worker(String.valueOf(workerNamePrefix) + (workerCreatedCount++));
			worker.setDaemon(daemonWorkers);
			workers.add(worker);
			worker.start();
			synchronized (AVAILABLE_MUTEX) {workerAvailableCount++;}
			fireWorkerCreated(SELF);
		}
	}
	
	/**
	 * Removes a worker from the pool.
	 */
	private void endWorker(Worker w)
	{
		synchronized (workers)
		{
			workers.remove(w);
			synchronized (AVAILABLE_MUTEX) {workerAvailableCount--;}
			fireWorkerDestroyed(SELF);
		}
	}
	
	/**
	 * Spawns the amount of necessary workers. 
	 */
	protected final void spawnNeeded()
	{
		while (getWorkerCount() < workerMin)
			startWorker();
		
		if (getWorkerCount() < workerMax && !queue.isEmpty() && workerAvailableCount == 0)
			startWorker();
	}
	
	/**
	 * Enqueues a task to assign to an available worker.
	 */
	public void enqueue(R task)
	{
		synchronized (queue)
		{
			queue.add(task);
			fireWorkEnqueued(this, task);
			spawnNeeded();
			queue.notifyAll();
		}
	}

	/**
	 * Adds a listener to this pool.
	 * @param listener the listener to add.
	 */
	public void addListener(WorkPoolListener<R> listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Removes a listener from this pool.
	 * @param listener the listener to add.
	 */
	public boolean removeListener(WorkPoolListener<R> listener)
	{
		return listeners.remove(listener);
	}

	/**
	 * Creates a runnable for an object dequeued from the worker pool. May return null.
	 * The runnable is assigned to a worker thread and executed.
	 * The runnable returned should perform an operation using the dequeued object. 
	 * @param dequeued the dequeued object to create a task for.
	 * @return a Runnable to run, or null to skip work.
	 */
	public abstract Runnable createTaskFor(R dequeued);
	
	/**
	 * Called when a worker thread is spawned.
	 * @param pool the pool that the thread belongs to.
	 * It is highly recommended that implementors of this function do not
	 * mess with the thread's execution, as it may have adverse effects in the pool.  
	 */
	protected final void fireWorkerCreated(WorkPool<R> pool)
	{
		for (WorkPoolListener<R> listener : listeners)
			listener.workerCreated(pool);
	}

	/**
	 * Called when a worker thread is destroyed.
	 * @param pool the pool that the thread belonged to.
	 */
	protected final void fireWorkerDestroyed(WorkPool<R> pool)
	{
		for (WorkPoolListener<R> listener : listeners)
			listener.workerDestroyed(pool);
	}

	/**
	 * Called when a task is enqueued.
	 * @param pool the pool that this happened on.
	 * @param runnable the runnable task associated with this event.
	 */
	protected final void fireWorkEnqueued(WorkPool<R> pool, R runnable)
	{
		for (WorkPoolListener<R> listener : listeners)
			listener.workEnqueued(pool, runnable);
	}

	/**
	 * Called when a task is started.
	 * @param pool the pool that this happened on.
	 * @param runnable the runnable task associated with this event.
	 */
	protected final void fireWorkStarted(WorkPool<R> pool, R runnable)
	{
		for (WorkPoolListener<R> listener : listeners)
			listener.workStarted(pool, runnable);
	}

	/**
	 * Called when a task finishes.
	 * @param pool the pool that this happened on.
	 * @param runnable the runnable task associated with this event.
	 */
	protected final void fireWorkFinished(WorkPool<R> pool, R runnable)
	{
		for (WorkPoolListener<R> listener : listeners)
			listener.workFinished(pool, runnable);
	}

	/**
	 * Called when a task throws an exception that isn't caught.
	 * @param pool the pool that this happened on.
	 * @param runnable the runnable task associated with this event.
	 * @param t the {@link Throwable} generated by the task finished. 
	 */
	protected final void fireWorkError(WorkPool<R> pool, R runnable, Throwable t)
	{
		for (WorkPoolListener<R> listener : listeners)
			listener.workError(pool, runnable, t);
	}

	/**
	 * Worker threads for the pool.
	 */
	private class Worker extends Thread
	{
		private Worker(String name)
		{
			super(name);
		}

		private boolean mortalityCheck()
		{
			return SELF.shutDown || (getWorkerCount() > workerMin && queue.isEmpty());
		}
		
		@Override
		public void run()
		{	
			while (true)
			{
				Runnable currentWork = null;
				R dequeued = null;
				
				synchronized (queue)
				{
					while (queue.isEmpty() && !mortalityCheck())
					{
						try {queue.wait(workerTimeout);} catch (InterruptedException ex) { /* Do nothing.*/	} 
					}
					try {
						if (!queue.isEmpty())
							currentWork = createTaskFor(dequeued = queue.dequeue());
					} catch (Throwable t) {
						fireWorkError(SELF, dequeued, t);
					}
				}

				if (currentWork == null)
					continue;
				
				try {
					synchronized (AVAILABLE_MUTEX) {workerAvailableCount--;}
					fireWorkStarted(SELF, dequeued);
					currentWork.run();
					synchronized (AVAILABLE_MUTEX) {workerAvailableCount++;}
				} catch (Throwable t) {
					fireWorkError(SELF, dequeued, t);
				}

				fireWorkFinished(SELF, dequeued);
				
				currentWork = null;

				// here because threads would immediately die upon creation after min worker count.
				if (mortalityCheck())
					break;
				
				yield();
			}
			
			endWorker(this);
		}
		
	}
	
}
