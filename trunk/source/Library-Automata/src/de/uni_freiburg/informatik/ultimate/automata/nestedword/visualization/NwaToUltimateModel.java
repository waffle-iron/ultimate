/*
 * Copyright (C) 2010-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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
package de.uni_freiburg.informatik.ultimate.automata.nestedword.visualization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.AutomataOperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.LibraryIdentifiers;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomatonSimple;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.reachablestates.NestedWordAutomatonReachableStates;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingCallTransition;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingReturnTransition;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.visualization.AutomatonTransition.Transition;
import de.uni_freiburg.informatik.ultimate.core.model.models.IElement;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;

/**
 * Converts an {@link INestedWordAutomatonSimple} to an Ultimate model.
 * 
 * @author Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * @param <LETTER>
 *            letter type
 * @param <STATE>
 *            state type
 */
public class NwaToUltimateModel<LETTER, STATE> {
	private static final String CREATING_NODE = "Creating Node: ";
	
	private final AutomataLibraryServices mServices;
	private final ILogger mLogger;
	
	/**
	 * Constructor.
	 * 
	 * @param services
	 *            Ultimate services
	 */
	public NwaToUltimateModel(final AutomataLibraryServices services) {
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(LibraryIdentifiers.PLUGIN_ID);
	}
	
	/**
	 * @param nwaSimple
	 *            A nested word automaton.
	 * @return Ultimate model
	 * @throws AutomataOperationCanceledException
	 *             if operation was canceled
	 */
	public IElement getUltimateModelOfNwa(final INestedWordAutomatonSimple<LETTER, STATE> nwaSimple)
			throws AutomataOperationCanceledException {
		final INestedWordAutomaton<LETTER, STATE> nwa;
		if (nwaSimple instanceof INestedWordAutomaton) {
			nwa = (INestedWordAutomaton<LETTER, STATE>) nwaSimple;
		} else {
			nwa = new NestedWordAutomatonReachableStates<>(mServices, nwaSimple);
		}
		final AutomatonState graphroot =
				new AutomatonState("Sucessors of this node are the initial states", false);
		final Map<STATE, AutomatonState> constructed = new HashMap<>();
		final LinkedList<STATE> queue = new LinkedList<>();
		
		// add all initial states to model - all are successors of the graphroot
		for (final STATE state : nwa.getInitialStates()) {
			queue.add(state);
			final AutomatonState vsn = new AutomatonState(state, nwa.isFinal(state));
			constructed.put(state, vsn);
			new AutomatonTransition(graphroot, Transition.INTERNAL, "", null, vsn);
		}
		
		while (!queue.isEmpty()) {
			final STATE state = queue.removeFirst();
			final AutomatonState vsn = constructed.get(state);
			
			for (final OutgoingInternalTransition<LETTER, STATE> trans : nwa.internalSuccessors(state)) {
				final LETTER symbol = trans.getLetter();
				final STATE succState = trans.getSucc();
				AutomatonState succVsn;
				if (constructed.containsKey(succState)) {
					succVsn = constructed.get(succState);
				} else {
					succVsn = new AutomatonState(succState, nwa.isFinal(succState));
					mLogger.debug(CREATING_NODE + succVsn.toString());
					constructed.put(succState, succVsn);
					queue.add(succState);
				}
				new AutomatonTransition(vsn, Transition.INTERNAL, symbol, null, succVsn);
			}
			
			for (final OutgoingCallTransition<LETTER, STATE> trans : nwa.callSuccessors(state)) {
				final LETTER symbol = trans.getLetter();
				final STATE succState = trans.getSucc();
				AutomatonState succVsn;
				if (constructed.containsKey(succState)) {
					succVsn = constructed.get(succState);
				} else {
					succVsn = new AutomatonState(succState, nwa.isFinal(succState));
					mLogger.debug(CREATING_NODE + succVsn.toString());
					constructed.put(succState, succVsn);
					queue.add(succState);
				}
				new AutomatonTransition(vsn, Transition.CALL, symbol, null, succVsn);
			}
			returnTransitionsHelper(nwa, constructed, queue, state, vsn);
		}
		return graphroot;
	}
	
	private void returnTransitionsHelper(final INestedWordAutomaton<LETTER, STATE> nwa,
			final Map<STATE, AutomatonState> constructed, final LinkedList<STATE> queue, final STATE state,
			final AutomatonState vsn) {
		for (final STATE hierPredState : nwa.getStates()) {
			for (final OutgoingReturnTransition<LETTER, STATE> trans : nwa.returnSuccessorsGivenHier(state,
					hierPredState)) {
				final LETTER symbol = trans.getLetter();
				final STATE succState = trans.getSucc();
				AutomatonState succVsn;
				if (constructed.containsKey(succState)) {
					succVsn = constructed.get(succState);
				} else {
					succVsn = new AutomatonState(succState, nwa.isFinal(succState));
					mLogger.debug(CREATING_NODE + succVsn.toString());
					constructed.put(succState, succVsn);
					queue.add(succState);
				}
				new AutomatonTransition(vsn, Transition.RETURN, symbol, hierPredState.toString(), succVsn);
			}
		}
	}
}
