/*
 * Copyright (C) 2015-2016 Daniel Tischner
 * Copyright (C) 2009-2016 University of Freiburg
 * 
 * This file is part of the ULTIMATE Automata Library.
 * 
 * The ULTIMATE Automata Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Automata Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Automata Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Automata Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.performance;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.ASimulation;

/**
 * Class that is used by {@link ASimulation} to measure its performance. Has
 * timer and counter to save performance data.
 * 
 * @author Daniel Tischner
 *
 */
public final class SimulationPerformance {

	/**
	 * Value for a non valid counting result.
	 */
	public static final int NO_COUNTING_RESULT = -1;
	/**
	 * Value for a non valid time result.
	 */
	public static final long NO_TIME_RESULT = -1;

	/**
	 * Creates an empty simulation performance object that is out of memory.
	 * 
	 * @param type
	 *            Type of the simulation
	 * @param useSCCs
	 *            If the simulation usesSCCs
	 * @return The out of memory simulation performance object
	 */
	public static SimulationPerformance createOutOfMemoryPerformance(final ESimulationType type, final boolean useSCCs) {
		SimulationPerformance performance = new SimulationPerformance(type, useSCCs);
		performance.outOfMemory();
		return performance;
	}

	/**
	 * Creates an empty simulation performance object that has timed out.
	 * 
	 * @param type
	 *            Type of the simulation
	 * @param useSCCs
	 *            If the simulation usesSCCs
	 * @return The timed out simulation performance object
	 */
	public static SimulationPerformance createTimedOutPerformance(final ESimulationType type, final boolean useSCCs) {
		SimulationPerformance performance = new SimulationPerformance(type, useSCCs);
		performance.timeOut();
		return performance;
	}

	/**
	 * Holds all counting measures that are monitored.
	 */
	private final LinkedHashMap<ECountingMeasure, Integer> m_CountingMeasures;
	/**
	 * If the simulation uses SCC optimization or not.
	 */
	private final boolean m_IsUsingSCCs;
	/**
	 * Name for the performance object to distinguish it from others.
	 */
	private String m_Name;
	/**
	 * If the performance object represents a simulation that has thrown an out
	 * of memory error.
	 */
	private boolean m_OutOfMemory;
	/**
	 * The type of the simulation that is monitored.
	 */
	private final ESimulationType m_SimType;
	/**
	 * If the performance object represents a simulation that has timed out.
	 */
	private boolean m_TimedOut;

	/**
	 * Holds all time measures that are monitored.
	 */
	private final LinkedHashMap<ETimeMeasure, List<Long>> m_TimeMeasures;
	/**
	 * Holds all starting timestamps for monitored time measures.
	 */
	private final LinkedHashMap<ETimeMeasure, Long> m_TimeMeasureStartTimes;

	/**
	 * Creates a simulation performance object that monitors the performance of
	 * a given simulation.
	 * 
	 * @param simType
	 *            Type of the simulation
	 * @param isUsingSCCs
	 *            If the simulation uses a SCC optimization
	 */
	public SimulationPerformance(final ESimulationType simType, final boolean isUsingSCCs) {
		m_SimType = simType;
		m_TimeMeasures = new LinkedHashMap<>();
		m_TimeMeasureStartTimes = new LinkedHashMap<>();
		m_CountingMeasures = new LinkedHashMap<>();
		m_IsUsingSCCs = isUsingSCCs;
		m_TimedOut = false;
		m_OutOfMemory = false;
		m_Name = "";
	}

	/**
	 * Adds a given value to the duration list of a given time measure.
	 * 
	 * @param type
	 *            Type of the time measure
	 * @param duration
	 *            Duration to add
	 */
	public void addTimeMeasureValue(final ETimeMeasure type, final long duration) {
		if (!m_TimeMeasures.containsKey(type)) {
			m_TimeMeasures.put(type, new LinkedList<>());
		}
		m_TimeMeasures.get(type).add(duration);
	}

	/**
	 * Gets the result for a given counting measure if known or
	 * {@link #NO_COUNTING_RESULT}.
	 * 
	 * @param type
	 *            Type of the counting measure to get
	 * @return The result for a given counting measure if known or
	 *         {@link #NO_COUNTING_RESULT}.
	 */
	public int getCountingMeasureResult(final ECountingMeasure type) {
		if (!m_CountingMeasures.containsKey(type)) {
			return NO_COUNTING_RESULT;
		}
		return m_CountingMeasures.get(type);
	}

	/**
	 * Gets the counting measures.
	 * 
	 * @return The counting measures.
	 */
	public LinkedHashMap<ECountingMeasure, Integer> getCountingMeasures() {
		return m_CountingMeasures;
	}

	/**
	 * Returns the name of the performance object.
	 * 
	 * @return The name of the object.
	 */
	public String getName() {
		return m_Name;
	}

	/**
	 * Gets the type of the simulation monitored.
	 * 
	 * @return The type of the simulation monitored.
	 */
	public ESimulationType getSimType() {
		return m_SimType;
	}

	/**
	 * Gets the result for a given time measure if known or
	 * {@link #NO_TIME_RESULT}. If there are multiple stored values for this
	 * measure they can be handled using a given {@link EMultipleDataOption}.
	 * 
	 * @param type
	 *            Type of the time measure to get
	 * @param option
	 *            Option how multiple data values should be handled.
	 * @return The time measure result to get
	 */
	public long getTimeMeasureResult(final ETimeMeasure type, final EMultipleDataOption option) {
		List<Long> measureList = m_TimeMeasures.get(type);
		if (measureList == null || measureList.isEmpty()) {
			return NO_TIME_RESULT;
		}

		long timeResult = 0;
		for (long timeMeasure : measureList) {
			if (timeMeasure == NO_TIME_RESULT) {
				continue;
			}
			if (option.equals(EMultipleDataOption.ADDITIVE) || option.equals(EMultipleDataOption.AVERAGE)) {
				timeResult += timeMeasure;
			} else if (option.equals(EMultipleDataOption.MAXIMUM)) {
				if (timeMeasure > timeResult) {
					timeResult = timeMeasure;
				}
			} else if (option.equals(EMultipleDataOption.MINIMIUM)) {
				if (timeMeasure < timeResult) {
					timeResult = timeMeasure;
				}
			}
		}
		if (option.equals(EMultipleDataOption.AVERAGE)) {
			timeResult = Math.round((timeResult + 0.0) / measureList.size());
		}

		if (timeResult <= 0) {
			return NO_TIME_RESULT;
		}
		return timeResult;
	}

	/**
	 * Gets all results of a given time measure.
	 * 
	 * @param type
	 *            Type of the time measures to get
	 * @return All results of a given time measure.
	 */
	public List<Long> getTimeMeasureResults(final ETimeMeasure type) {
		return m_TimeMeasures.get(type);
	}

	/**
	 * Gets the time measures.
	 * 
	 * @return The time measures.
	 */
	public LinkedHashMap<ETimeMeasure, List<Long>> getTimeMeasures() {
		return m_TimeMeasures;
	}

	/**
	 * Returns whether the performance object represents a simulation that has
	 * timed out.
	 * 
	 * @return Whether the performance object represents a simulation that has
	 *         timed out.
	 */
	public boolean hasTimedOut() {
		return m_TimedOut;
	}

	/**
	 * Increases the stored counter of a given counting measure or sets it to 1
	 * if it was not stored.
	 * 
	 * @param type
	 *            Type of the counting measure to increase
	 */
	public void increaseCountingMeasure(final ECountingMeasure type) {
		if (!m_CountingMeasures.containsKey(type)) {
			m_CountingMeasures.put(type, 1);
		} else {
			int counter = m_CountingMeasures.get(type);
			m_CountingMeasures.put(type, counter + 1);
		}
	}

	/**
	 * Returns whether the performance object represents a simulation that has
	 * thrown an out of memory error.
	 * 
	 * @return Whether the performance object represents a simulation that has
	 *         throen an out of memory error.
	 */
	public boolean isOutOfMemory() {
		return m_OutOfMemory;
	}

	/**
	 * If the monitored simulation uses a SCC simulation.
	 * 
	 * @return If the monitored simulation uses a SCC simulation.
	 */
	public boolean isUsingSCCs() {
		return m_IsUsingSCCs;
	}

	/**
	 * If called the performance object indicates that the represented
	 * simulation has thrown an out of memory error.
	 */
	public void outOfMemory() {
		m_OutOfMemory = true;
	}

	/**
	 * Sets the value for a given counting measure if it is not zero.
	 * 
	 * @param type
	 *            Type of the counting measure to set
	 * @param counter
	 *            Value to set which must not be zero
	 */
	public void setCountingMeasure(final ECountingMeasure type, final int counter) {
		if (counter != 0) {
			m_CountingMeasures.put(type, counter);
		}
	}

	/**
	 * Sets the name of the performance object.
	 * 
	 * @param name
	 *            The name to set
	 */
	public void setName(final String name) {
		m_Name = name;
	}

	/**
	 * Starts the timer for a given time measure.
	 * 
	 * @param type
	 *            Type of the time measure to start
	 */
	public void startTimeMeasure(final ETimeMeasure type) {
		long startTime = System.currentTimeMillis();
		m_TimeMeasureStartTimes.put(type, startTime);
	}

	/**
	 * Stops the timer for a given time measure and returns the duration of the
	 * measure.
	 * 
	 * @param type
	 *            Type of the time measure to stop
	 * @return The duration of the measure.
	 */
	public long stopTimeMeasure(final ETimeMeasure type) {
		long endTime = System.currentTimeMillis();
		long startTime = m_TimeMeasureStartTimes.get(type);
		if (!m_TimeMeasureStartTimes.containsKey(type)) {
			startTime = 0;
		}
		long duration = endTime - startTime;
		saveTimeMeasureResult(type, duration);
		return duration;
	}

	/**
	 * If called the performance object indicates that the represented
	 * simulation timed out.
	 */
	public void timeOut() {
		m_TimedOut = true;
	}

	/**
	 * Saves a given duration for a given time measure.
	 * 
	 * @param type
	 *            Type of the time measure to save
	 * @param duration
	 *            Duration to save
	 */
	private void saveTimeMeasureResult(final ETimeMeasure type, final long duration) {
		List<Long> measureList = m_TimeMeasures.get(type);
		if (measureList == null) {
			measureList = new LinkedList<Long>();
			m_TimeMeasures.put(type, measureList);
		}
		measureList.add(duration);
	}
}