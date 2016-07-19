/*
 * Copyright (C) 2012-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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
package de.uni_freiburg.informatik.ultimate.automata.petrinet.julian;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.LibraryIdentifiers;
import de.uni_freiburg.informatik.ultimate.automata.ResultChecker;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.Place;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.util.datastructures.UnionFind;

public class FinitePrefix2PetriNet<L, C> implements IOperation<L, C> {

	private final AutomataLibraryServices mServices;
	private final ILogger mLogger;
	
	BranchingProcess<L, C> mInput;
	PetriNetJulian<L, C> mNet;
	private final UnionFind<Condition<L, C>> representatives;

	@Override
	public String operationName() {
		return "finitePrefix2PetriNet";
	}

	@Override
	public String startMessage() {
		return "Start " + operationName() + "Input " + mInput.sizeInformation();
	}

	@Override
	public String exitMessage() {
		return "Finished " + operationName() + " Result "
				+ mNet.sizeInformation();
	}

	@Override
	public PetriNetJulian<L, C> getResult() throws AutomataLibraryException {
		return mNet;
	}
	
	
//	private Condition<L,C> getRepresentative(Condition<L,C> c) {
//		Condition<L,C> result = c;
//		while (result != representatives.get(result)) {
//			result = representatives.get(result);
//			assert result != null;
//		}
//		return result;
//	}

	public FinitePrefix2PetriNet(AutomataLibraryServices services, 
			BranchingProcess<L, C> bp) throws AutomataLibraryException {
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(LibraryIdentifiers.PLUGIN_ID);
		// TODO implement merging for markings?
		mInput = bp;
		mLogger.info(startMessage());

		final Map<Condition<L, C>, Place<L, C>> placeMap = new HashMap<Condition<L, C>, Place<L, C>>();
		final Map<Event<L, C>, Transition<L, C>> transitionMap = new HashMap<Event<L, C>, Transition<L, C>>();

		mLogger.debug("CONDITIONS:");
		for (final Condition<L, C> c : bp.getConditions()) {
			mLogger.debug(c);
		}
		mLogger.debug("EVENTS:");
		for (final Event<L, C> e : bp.getEvents()) {
			mLogger.debug(e.getPredecessorConditions() + " || " + e + " || "
					+ e.getSuccessorConditions());
		}

		final PetriNetJulian<L, C> old_net = mInput.getNet();
		mNet = new PetriNetJulian<L, C>(mServices, old_net.getAlphabet(),
				old_net.getStateFactory(), false);
		
		
		
//		List<Event<L, C>> events = new ArrayList<Event<L,C>>();
//		List<Event<L, C>> worklist = new LinkedList<Event<L,C>>();
//		Set<Event<L, C>> visited = new HashSet<Event<L,C>>();
//
//		for (Event<L, C> e : bp.getMinEvents()) {
//			worklist.add(e);
//			events.add(e);
//			visited.add(e);
//		}
//		while(!worklist.isEmpty()) {
//			Event<L,C> event = worklist.remove(0);
//			for (Condition<L,C> c : event.getSuccessorConditions()) {
//				for (Event<L,C> e : c.getSuccessorEvents()) {
//					if (!visited.contains(e)) {
//						worklist.add(e);
//						events.add(e);
//						visited.add(e);
//					}
//				}
//			}
//		}
//		for (Event e : bp.getEvents()) {
//			assert e == bp.getDummyRoot() || visited.contains(e);
//		}
		final TreeSet<Event<L,C>> events = new TreeSet<Event<L,C>>(bp.getOrder());
		events.addAll(bp.getEvents());
		
		representatives = new UnionFind<Condition<L, C>>();
		for (final Condition c : bp.getDummyRoot().getSuccessorConditions()) {
			representatives.makeEquivalenceClass(c);
		}
		final Iterator<Event<L, C>> it = events.iterator();
		final Event<L, C> first = it.next();
		assert first == bp.getDummyRoot();
		Event<L,C> previous;
		Event<L,C> current = first;
		while (it.hasNext()) {
			previous = current;
			current = it.next();
			assert bp.getOrder().compare(previous, current) < 0;
			
			for (final Condition<L,C> c : current.getConditionMark()) {
				final Place p = c.getPlace();
				final Condition<L, C> representative = representatives.find(c);
				if (representative == null) {
					representatives.makeEquivalenceClass(c);
				}
			}
			
			if (current.isCutoffEvent()) {
				final Event<L,C> companion = current.getCompanion();
				final ConditionMarking<L,C> companionCondMark = companion.getConditionMark();
				final ConditionMarking<L,C> eCondMark = current.getConditionMark();
				mergeConditions(companionCondMark, eCondMark);
				mergeConditions(companion.getPredecessorConditions(), current.getPredecessorConditions());
			}

		}
		
		for (final Condition c : bp.getConditions()) {
			assert representatives.find(c) != null;
			if (c == representatives.find(c)) {
				final Place<L, C> place = mNet.addPlace((C) old_net.getStateFactory()
						.finitePrefix2net(c), bp.initialConditions()
						.contains(c), bp.isAccepting(c));
				placeMap.put(c, place);
			}
		}
		final TransitionSet transitionSet = new TransitionSet();
		for (final Event<L,C> e : events) {
			if (e == bp.getDummyRoot()) {
				continue;
			}
			final Set<Place<L, C>> preds = new HashSet<Place<L, C>>();
			final Set<Place<L, C>> succs = new HashSet<Place<L, C>>();
			
			for (final Condition<L, C> c : e.getPredecessorConditions()) {
				final Condition<L, C> representative = representatives.find(c);
				preds.add(placeMap.get(representative));
			}
			
			for (final Condition<L, C> c : e.getSuccessorConditions()) {
				final Condition<L, C> representative = representatives.find(c);
				succs.add(placeMap.get(representative));
			}
			transitionSet.addTransition(e.getTransition().getSymbol(), preds, succs);
			//	mNet.addTransition(e.getTransition().getSymbol(), preds, succs);
		}
		transitionSet.addAllTransitionsToNet(mNet);
		
		
		
		
		
//		for (Condition<L, C> c : bp.getConditions()) {
//			if (!c.getPredecessorEvent().isCutoffEvent()) {
//				Place<L, C> place = mNet.addPlace(old_net.getStateFactory()
//						.finitePrefix2net(c), bp.initialConditions()
//						.contains(c), bp.isAccepting(c));
//				placeMap.put(c, place);
//			}
//		}
//		mLogger.debug("CONDITIONS TO PLACE:");
//		for (Map.Entry<Condition<L, C>, Place<L, C>> en : placeMap.entrySet())
//			mLogger.debug(en);
//		for (Event<L, C> e : bp.getEvents()) {
//			if (e.getTransition() == null)
//				continue;
//			ArrayList<Place<L, C>> preds = new ArrayList<Place<L, C>>();
//			ArrayList<Place<L, C>> succs = new ArrayList<Place<L, C>>();
//			for (Condition<L, C> pc : e.getPredecessorConditions()) {
//				assert placeMap.containsKey(pc) : pc.toString()
//						+ " has successors, hence cannot be child of cut-off event. So it must have been added, but it cannot be found.";
//				preds.add(placeMap.get(pc));
//			}
//			Event<L, C> companionOrE = e;
//			if (e.isCutoffEvent())
//				companionOrE = e.getCompanion();
//			for (Condition<L, C> sc : companionOrE.getSuccessorConditions()) {
//				assert placeMap.containsKey(sc);
//				succs.add(placeMap.get(sc));
//			}
//			Transition<L, C> transition = mNet.addTransition(e.getTransition()
//					.getSymbol(), preds, succs);
//			transitionMap.put(e, transition);
//		}

		mLogger.info(exitMessage());
		try {
			assert ResultChecker.petriNetLanguageEquivalence(mServices, old_net, mNet) : 
				"The language recognized by the FinitePrefix2PetriNet is not equal to the language of the original net.";
		} catch (final AutomataLibraryException e1) {
			e1.printStackTrace();
		}
	}
	
	
	private void mergeConditions(Iterable<Condition<L,C>> set1, Iterable<Condition<L,C>> set2) {
		final Map<Place<L,C>, Condition<L,C>> origPlace2Condition = new HashMap<Place<L,C>, Condition<L,C>>();
		for (final Condition<L,C> c1 : set1) {
			origPlace2Condition.put(c1.getPlace(), c1);
		}
		
		for (final Condition<L,C> c2 : set2) {
			final Place p2 = c2.getPlace();
			final Condition c1 = origPlace2Condition.get(p2);
			final Condition<L, C> c1representative = representatives.find(c1);
			assert c1representative != null;
			
			final Condition<L, C> c2representative = representatives.find(c2);
			assert c2representative != null;
			
			representatives.union(c1representative, c2representative);
		}
	}
	
	
	

	
	
	class TransitionSet {
		Map<L, Map< Set<Place<L, C>>, Set<Set<Place<L, C>>>>> mLetter2Predset2Succsets = 
				new HashMap<L, Map< Set<Place<L, C>>, Set<Set<Place<L, C>>>>>();
		
		void addTransition(L letter, Set<Place<L, C>> predset, Set<Place<L, C>> succset) {
			Map<Set<Place<L, C>>, Set<Set<Place<L, C>>>> predsets2succsets = mLetter2Predset2Succsets.get(letter);
			if (predsets2succsets == null) {
				predsets2succsets = new HashMap<Set<Place<L, C>>, Set<Set<Place<L, C>>>>();
				mLetter2Predset2Succsets.put(letter, predsets2succsets);
			}
			Set<Set<Place<L, C>>> succsets = predsets2succsets.get(predset);
			if (succsets == null) {
				succsets = new HashSet<Set<Place<L, C>>>();
				predsets2succsets.put(predset, succsets);
			}
			succsets.add(succset);
		}
		
		void addAllTransitionsToNet(PetriNetJulian<L,C> net) {
			for (final L letter : mLetter2Predset2Succsets.keySet()) {
				final Map<Set<Place<L, C>>, Set<Set<Place<L, C>>>> predsets2succsets = mLetter2Predset2Succsets.get(letter);
				for (final Set<Place<L,C>> predset : predsets2succsets.keySet()) {
					final Set<Set<Place<L, C>>> succsets = predsets2succsets.get(predset);
					for (final Set<Place<L, C>> succset : succsets) {
						final List<Place<L,C>> predList = new ArrayList<Place<L,C>>(predset);
						final List<Place<L,C>> succList = new ArrayList<Place<L,C>>(succset);
						net.addTransition(letter, predList, succList);
					}
				}
			}
		}
	}


	@Override
	public boolean checkResult(StateFactory<C> stateFactory)
			throws AutomataLibraryException {
		return true;
	}
}
