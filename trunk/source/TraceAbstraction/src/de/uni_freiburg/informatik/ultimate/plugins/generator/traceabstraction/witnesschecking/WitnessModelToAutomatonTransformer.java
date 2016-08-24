/*
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE TraceAbstraction plug-in.
 * 
 * The ULTIMATE TraceAbstraction plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE TraceAbstraction plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE TraceAbstraction plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE TraceAbstraction plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE TraceAbstraction plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.witnesschecking;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.witnessparser.graph.WitnessEdge;
import de.uni_freiburg.informatik.ultimate.witnessparser.graph.WitnessNode;
import de.uni_freiburg.informatik.ultimate.witnessparser.graph.WitnessNodeAnnotation;

public class WitnessModelToAutomatonTransformer {
	private final NestedWordAutomaton<WitnessEdge, WitnessNode> mResult;
	private final WitnessNode mWitnessRoot;
	private final ArrayDeque<WitnessNode> worklist = new ArrayDeque<WitnessNode>();
	
	public WitnessModelToAutomatonTransformer(final WitnessNode witnessRoot, final IUltimateServiceProvider services) {
		super();
		mWitnessRoot = witnessRoot;
		final Set<WitnessEdge> internalAlphabet = new LinkedHashSet<WitnessEdge>();
		final Set<WitnessEdge> callAlphabet = Collections.emptySet();
		final Set<WitnessEdge> returnAlphabet = Collections.emptySet();
		final StateFactory<WitnessNode> stateFactory = new StateFactory<WitnessNode>() {
		};
		mResult = new NestedWordAutomaton<WitnessEdge, WitnessNode>(new AutomataLibraryServices(services), internalAlphabet, callAlphabet, returnAlphabet, stateFactory);
		constructAutomaton(internalAlphabet);
	}

	private void constructAutomaton(final Set<WitnessEdge> internalAlphabet) {
		addNewState(mWitnessRoot);
		while (!worklist.isEmpty()) {
			final WitnessNode current = worklist.removeFirst();
			for (final WitnessEdge we : current.getOutgoingEdges()) {
				final WitnessNode successor = we.getTarget();
				if (!mResult.getStates().contains(successor)) {
					addNewState(successor);
				}
				internalAlphabet.add(we);
				mResult.addInternalTransition(current, we, successor);
			}
		}
	}

	private void addNewState(final WitnessNode successor) {
		final WitnessNodeAnnotation annotation = WitnessNodeAnnotation.getAnnotation(successor);
		final boolean isInitial = (annotation != null && annotation.isInitial());
		final boolean isFinal = (annotation != null && annotation.isError());
		mResult.addState(isInitial, isFinal, successor);
		worklist.add(successor);
	}

	public INestedWordAutomaton<WitnessEdge, WitnessNode> getResult() {
		return mResult;
	}
}
