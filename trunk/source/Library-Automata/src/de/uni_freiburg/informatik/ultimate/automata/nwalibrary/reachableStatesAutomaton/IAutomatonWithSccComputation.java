/*
 * Copyright (C) 2009-2014 University of Freiburg
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
 * along with the ULTIMATE Automata Library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Automata Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.reachableStatesAutomaton;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for computation of strongly connected components (SCC) for a given 
 * automaton. Objects that implement this interface also allow the computation
 * of balls (SCCs with at least one edge) for subgraphs of the automaton. 
 * 
 * @author Matthias Heizmann
 *
 * @param <STATE>
 */
public interface IAutomatonWithSccComputation<LETTER, STATE> {

	/**
	 * Computes all balls of given subset of states.
	 * @param stateSubset subset of the automata's states
	 * @param startStates states at which the computation of SSCs starts
	 * @return
	 */
	public Collection<SccComputationWithAcceptingLassos<LETTER, STATE>.SCComponent> computeBalls(Set<STATE> stateSubset, Set<STATE> startStates);

}