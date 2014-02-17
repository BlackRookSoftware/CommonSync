/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.sync;

import com.blackrook.commons.list.List;

/**
 * A basic object that can be used for... uh... Monitoring Progress.
 * Essentially, this contains a few fields for storing a starting
 * value, an ending value, a flag for an "indeterminate progress" indicator,
 * and the current value.
 * @author Matthew Tropiano
 */
public class ProgressMonitor
{
	/** The minimum value. */
	protected float minValue;
	/** The maximum value. */
	protected float maxValue;
	/** Indeterminate flag. */
	protected boolean indeterminate;
	/** The current value. */
	protected float currentValue;

	protected List<ProgressMonitorListener> listeners;
	
	/**
	 * Creates a new ProgressMonitor setting Indeterminate to false,
	 * the min and current values to 0, and the max to 1.
	 */
	public ProgressMonitor()
	{
		this(0.0f, 1.0f);
	}

	/**
	 * Creates a new ProgressMonitor setting Indeterminate to false,
	 * both the min and max values, and the current value to the min value.
	 */
	public ProgressMonitor(float minValue, float maxValue)
	{
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.indeterminate = false;
		this.currentValue = minValue;
		listeners = new List<ProgressMonitorListener>(3);
	}

	/**
	 * Adds a ProgressMonitorListener to this monitor.
	 */
	public void addListener(ProgressMonitorListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Removes a ProgressMonitorListener from this monitor.
	 */
	public void removeListener(ProgressMonitorListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Gets the minimum value of this monitor.
	 */
	public float getMinValue()
	{
		return minValue;
	}
	
	/**
	 * Sets the minimum value of this monitor.
	 */
	public void setMinValue(float minValue)
	{
		float oldVal = this.minValue;
		this.minValue = minValue;
		fireMinimumChanged(oldVal, minValue);
	}
	
	/**
	 * Gets the maximum value of this monitor.
	 */
	public float getMaxValue()
	{
		return maxValue;
	}
	
	/**
	 * Sets the maximum value of this monitor.
	 */
	public void setMaxValue(float maxValue)
	{
		float oldVal = this.maxValue;
		this.maxValue = maxValue;
		fireMaximumChanged(oldVal, maxValue);
	}
	
	/**
	 * Is this monitor set as indeterminate?
	 */
	public boolean isIndeterminate()
	{
		return indeterminate;
	}
	
	/**
	 * Sets/clears this monitor's indeterminate flag.
	 */
	public void setIndeterminate(boolean indeterminate)
	{
		this.indeterminate = indeterminate;
		fireIndeterminateChanged(indeterminate);
	}
	
	/**
	 * Gets the current value. 
	 */
	public float getCurrentValue()
	{
		return currentValue;
	}
	
	/**
	 * Sets the current value. 
	 */
	public void setCurrentValue(float currentValue)
	{
		float oldVal = this.currentValue;
		this.currentValue = currentValue;
		fireCurrentChanged(oldVal, currentValue);
	}
	
	/**
	 * Gets the current progress as a value between 0 and 1 according to
	 * the min, max, and current values. 
	 */
	public float getCurrentProgress()
	{
		return (currentValue-minValue)/(maxValue-minValue);
	}
	
	/** Fires a "minimum value changed" event to bound listeners. */
	protected void fireMinimumChanged(float oldVal, float newVal)
	{
		for (ProgressMonitorListener listener : listeners)
			listener.minimumChanged(this, oldVal, newVal);
	}
	
	/** Fires a "maximum value changed" event to bound listeners. */
	protected void fireMaximumChanged(float oldVal, float newVal)
	{
		for (ProgressMonitorListener listener : listeners)
			listener.maximumChanged(this, oldVal, newVal);
	}
	
	/** Fires an "indeterminate flag changed" event to bound listeners. */
	protected void fireIndeterminateChanged(boolean newVal)
	{
		for (ProgressMonitorListener listener : listeners)
			listener.indeterminateChanged(this,  newVal);
	}
	
	/** Fires a "current value changed" event to bound listeners. */
	protected void fireCurrentChanged(float oldVal, float newVal)
	{
		for (ProgressMonitorListener listener : listeners)
			listener.currentChanged(this, oldVal, newVal);
	}
}
