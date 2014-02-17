/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.sync;

/**
 * Listener interface for ProgressMonitors.
 * @author Matthew Tropiano
 */
public interface ProgressMonitorListener
{
	/**
	 * Called when a ProgressMonitor's minimum value changed.
	 * @param pm		the monitor it changed on.
	 * @param oldVal	the old minimum value.
	 * @param newVal	the new minimum value.
	 */
	public void minimumChanged(ProgressMonitor pm, float oldVal, float newVal);
	
	/**
	 * Called when a ProgressMonitor's maximum value changed.
	 * @param pm		the monitor it changed on.
	 * @param oldVal	the old maximum value.
	 * @param newVal	the new maximum value.
	 */
	public void maximumChanged(ProgressMonitor pm, float oldVal, float newVal);
	
	/**
	 * Called when a ProgressMonitor's indeterminate flag changed.
	 * @param pm		the monitor it changed on.
	 * @param newVal	the new value.
	 */
	public void indeterminateChanged(ProgressMonitor pm, boolean newVal);
	
	/**
	 * Called when a ProgressMonitor's current value changed.
	 * @param pm		the monitor it changed on.
	 * @param oldVal	the old current value.
	 * @param newVal	the new current value.
	 */
	public void currentChanged(ProgressMonitor pm, float oldVal, float newVal);
	
}
