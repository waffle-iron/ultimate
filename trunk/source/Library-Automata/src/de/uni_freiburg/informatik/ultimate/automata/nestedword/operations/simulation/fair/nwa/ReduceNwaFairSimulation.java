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
package de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.simulation.fair.nwa;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.AutomataOperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.LibraryIdentifiers;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.buchi.TestBuchiEquivalence;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.minimization.LookaheadPartitionConstructor;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.simulation.fair.FairSimulation;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.simulation.fair.ReduceBuchiFairSimulation;
import de.uni_freiburg.informatik.ultimate.automata.statefactory.IStateFactory;

/**
 * Operation that reduces a given nwa by using {@link FairSimulation}. <br/>
 * Once constructed the reduction automatically starts, the result can be get by
 * using {@link #getResult()}.
 * 
 * @author Daniel Tischner
 * 
 * @param <LETTER>
 *            Letter class of nwa
 * @param <STATE>
 *            State class of nwa
 */
public final class ReduceNwaFairSimulation<LETTER, STATE> extends ReduceBuchiFairSimulation<LETTER, STATE> {

	/**
	 * Creates a new nwa reduce object that starts reducing the given nwa
	 * automaton.<br/>
	 * Once finished the result can be get by using {@link #getResult()}.
	 * 
	 * @param services
	 *            Service provider of Ultimate framework
	 * @param stateFactory
	 *            The state factory used for creating states
	 * @param operand
	 *            The nwa to reduce
	 * @throws AutomataOperationCanceledException
	 *             If the operation was canceled, for example from the Ultimate
	 *             framework.
	 */
	public ReduceNwaFairSimulation(final AutomataLibraryServices services, final IStateFactory<STATE> stateFactory,
			final INestedWordAutomaton<LETTER, STATE> operand) throws AutomataOperationCanceledException {
		this(services, stateFactory, operand, false, Collections.emptyList());
	}

	/**
	 * Creates a new nwa reduce object that starts reducing the given nwa
	 * automaton.<br/>
	 * Once finished the result can be get by using {@link #getResult()}.
	 * 
	 * @param services
	 *            Service provider of Ultimate framework
	 * @param stateFactory
	 *            The state factory used for creating states
	 * @param operand
	 *            The nwa to reduce
	 * @param useSCCs
	 *            If the simulation calculation should be optimized using SCC,
	 *            Strongly Connected Components.
	 * @throws AutomataOperationCanceledException
	 *             If the operation was canceled, for example from the Ultimate
	 *             framework.
	 */
	public ReduceNwaFairSimulation(final AutomataLibraryServices services, final IStateFactory<STATE> stateFactory,
			final INestedWordAutomaton<LETTER, STATE> operand, final boolean useSCCs)
					throws AutomataOperationCanceledException {
		this(services, stateFactory, operand, useSCCs,
				new LookaheadPartitionConstructor<LETTER, STATE>(services, operand).getResult());
	}

	/**
	 * Creates a new nwa reduce object that starts reducing the given nwa
	 * automaton.<br/>
	 * Once finished the result can be get by using {@link #getResult()}.
	 * 
	 * @param services
	 *            Service provider of Ultimate framework
	 * @param stateFactory
	 *            The state factory used for creating states
	 * @param operand
	 *            The nwa to reduce
	 * @param useSCCs
	 *            If the simulation calculation should be optimized using SCC,
	 *            Strongly Connected Components.
	 * @param possibleEquivalentClasses
	 *            A collection of sets which contains states of the nwa
	 *            automaton that may be merge-able. States which are not in the
	 *            same set are definitely not merge-able which is used as an
	 *            optimization for the simulation
	 * @throws AutomataOperationCanceledException
	 *             If the operation was canceled, for example from the Ultimate
	 *             framework.
	 */
	public ReduceNwaFairSimulation(final AutomataLibraryServices services, final IStateFactory<STATE> stateFactory,
			final INestedWordAutomaton<LETTER, STATE> operand, final boolean useSCCs,
			final Collection<Set<STATE>> possibleEquivalentClasses) throws AutomataOperationCanceledException {
		super(services, stateFactory, operand, useSCCs, false,
				new FairNwaSimulation<LETTER, STATE>(services.getProgressMonitorService(),
						services.getLoggingService().getLogger(LibraryIdentifiers.PLUGIN_ID), useSCCs, stateFactory,
						possibleEquivalentClasses,
						new FairNwaGameGraph<LETTER, STATE>(services, services.getProgressMonitorService(),
								services.getLoggingService().getLogger(LibraryIdentifiers.PLUGIN_ID), operand,
								stateFactory, possibleEquivalentClasses)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.
	 * simulation.fair.ReduceBuchiFairSimulation#checkResult(de.uni_freiburg.
	 * informatik.ultimate.automata.statefactory.IStateFactory)
	 */
	@Override
	public boolean checkResult(final IStateFactory<STATE> stateFactory) throws AutomataLibraryException {
		getLogger().info("Start testing correctness of " + operationName());
		final boolean correct = (new TestBuchiEquivalence<LETTER, STATE>(getServices(), stateFactory, getOperand(),
				getResult())).getResult();
		getLogger().info("Finished testing correctness of " + operationName());
		return correct;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * buchiReduction.fair.ReduceBuchiFairSimulation#operationName()
	 */
	@Override
	public String operationName() {
		return "reduceNwaFairSimulation";
	}
}
