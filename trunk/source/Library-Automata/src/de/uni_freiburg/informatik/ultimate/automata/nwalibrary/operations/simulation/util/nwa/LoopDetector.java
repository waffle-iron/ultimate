/*
 * Copyright (C) 2015-2016 Daniel Tischner
 * Copyright (C) 2009-2015 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.util.nwa;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.automata.OperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.AGameGraph;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.util.Vertex;
import de.uni_freiburg.informatik.ultimate.core.services.model.IProgressAwareTimer;

/**
 * Class for detecting special loops in game graphs.
 * 
 * @author Daniel Tischner
 * 
 * @param <LETTER>
 *            Letter class of nwa automaton
 * @param <STATE>
 *            State class of nwa automaton
 */
public final class LoopDetector<LETTER, STATE> {

	/**
	 * Game graph to use for loop detection.
	 */
	private final AGameGraph<LETTER, STATE> m_GameGraph;
	/**
	 * The logger used by the Ultimate framework.
	 */
	private final Logger m_Logger;
	/**
	 * Timer used for responding to timeouts and operation cancellation.
	 */
	private final IProgressAwareTimer m_ProgressAwareTimer;

	/**
	 * Creates a new loop detector which can detect special loops on a given
	 * game graph.
	 * 
	 * @param gameGraph
	 *            Game graph to use for loop detection
	 * @param progressTimer
	 *            Timer used for responding to timeouts and operation
	 *            cancellation.
	 * @param logger
	 *            Logger of the Ultimate framework.
	 */
	public LoopDetector(final AGameGraph<LETTER, STATE> gameGraph, final Logger logger,
			final IProgressAwareTimer progressAwareTimer) {
		m_GameGraph = gameGraph;
		m_Logger = logger;
		m_ProgressAwareTimer = progressAwareTimer;
	}

	/**
	 * Returns whether a given vertex can reach a given destination.
	 * 
	 * @param vertex
	 *            Vertex in question
	 * @param destination
	 *            Destination to reach
	 * @return <tt>True if destination is reachable from vertex, <tt>false</tt>
	 *         if not.
	 * @throws OperationCanceledException
	 *             If the operation was canceled, for example from the Ultimate
	 *             framework.
	 */
	public boolean canVertexReachDestination(final Vertex<LETTER, STATE> vertex,
			final Vertex<LETTER, STATE> destination) throws OperationCanceledException {
		return !isLoopVertex(vertex, destination, null);
	}

	/**
	 * Returns whether the given vertex is a loop vertex. This is the case if it
	 * is not possible for the vertex to reach the given destination without
	 * visiting the vertex toAvoid. <br/>
	 * <br/>
	 * The cost for this operation is comparable high, it computes the property
	 * on demand, without pre-processing or caching, by a breadth-first search
	 * through the graph.
	 * 
	 * @param vertex
	 *            Vertex in question
	 * @param destination
	 *            The destination, the given vertex should reach without
	 *            visiting toAvoid
	 * @param toAvoid
	 *            The vertex, the given vertex must not visit while trying to
	 *            reach the destination
	 * @return <tt>True</tt> if the given vertex is a loop vertex,
	 *         <tt>false</tt> if not.
	 * @throws OperationCanceledException
	 *             If the operation was canceled, for example from the Ultimate
	 *             framework.
	 */
	public boolean isLoopVertex(final Vertex<LETTER, STATE> vertex, final Vertex<LETTER, STATE> destination,
			final Vertex<LETTER, STATE> toAvoid) throws OperationCanceledException {
		// Solve if 'vertex' can reach 'destination' without visiting 'toAvoid'.
		// We do so by using a breadth-first search.
		Queue<Vertex<LETTER, STATE>> queue = new LinkedList<>();
		Set<Vertex<LETTER, STATE>> processedElements = new HashSet<>();

		// Add root element
		queue.add(vertex);

		// Process queue
		boolean destinationFound = false;
		while (!queue.isEmpty() && !destinationFound) {
			Vertex<LETTER, STATE> element = queue.poll();

			if (element.equals(destination)) {
				destinationFound = true;
			}

			boolean wasAlreadyProcessed = !processedElements.add(element);
			boolean isToAvoid = element.equals(toAvoid);

			if (!destinationFound && !wasAlreadyProcessed && !isToAvoid) {
				// Add successors to queue
				queue.addAll(m_GameGraph.getSuccessors(element));
			}

			// If operation was canceled, for example from the
			// Ultimate framework
			if (m_ProgressAwareTimer != null && !m_ProgressAwareTimer.continueProcessing()) {
				m_Logger.debug("Stopped in isLoopVertex");
				throw new OperationCanceledException(this.getClass());
			}
		}
		// Vertex is a loop vertex if destination could not be found without
		// visiting 'toAvoid'.
		return !destinationFound;
	}
}