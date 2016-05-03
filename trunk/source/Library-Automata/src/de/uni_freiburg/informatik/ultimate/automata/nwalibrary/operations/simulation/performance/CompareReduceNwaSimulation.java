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

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.OperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonOldApi;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.ESimulationType;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.direct.nwa.DirectNwaGameGraph;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.direct.nwa.DirectNwaSimulation;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.fair.nwa.FairNwaGameGraph;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.fair.nwa.FairNwaSimulation;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.reachableStatesAutomaton.NestedWordAutomatonReachableStates;
import de.uni_freiburg.informatik.ultimate.core.services.model.IProgressAwareTimer;

/**
 * Operation that compares the different types of simulation methods for nwa
 * reduction.<br/>
 * The resulting automaton is the input automaton.
 * 
 * @author Daniel Tischner
 * 
 * @param <LETTER>
 *            Letter class of nwa automaton
 * @param <STATE>
 *            State class of nwa automaton
 */
public final class CompareReduceNwaSimulation<LETTER, STATE> extends CompareReduceBuchiSimulation<LETTER, STATE>
		implements IOperation<LETTER, STATE> {

	/**
	 * Compares the different types of simulation methods for nwa reduction.
	 * Resulting automaton is the input automaton.
	 * 
	 * @param services
	 *            Service provider of Ultimate framework
	 * @param stateFactory
	 *            The state factory used for creating states
	 * @param operand
	 *            The nwa automaton to compare with
	 * @throws OperationCanceledException
	 *             If the operation was canceled, for example from the Ultimate
	 *             framework.
	 */
	public CompareReduceNwaSimulation(AutomataLibraryServices services, StateFactory<STATE> stateFactory,
			INestedWordAutomatonOldApi<LETTER, STATE> operand) throws OperationCanceledException {
		super(services, stateFactory, operand);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * simulation.performance.CompareReduceBuchiSimulation#operationName()
	 */
	@Override
	public String operationName() {
		return "compareReduceNwaSimulation";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * simulation.performance.CompareReduceBuchiSimulation#
	 * verifyAutomatonValidity(de.uni_freiburg.informatik.ultimate.automata.
	 * nwalibrary.INestedWordAutomatonOldApi)
	 */
	@Override
	public void verifyAutomatonValidity(final INestedWordAutomatonOldApi<LETTER, STATE> automaton) {
		// Do noting to accept nwa automata
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * simulation.performance.CompareReduceBuchiSimulation#
	 * measureMethodPerformance(java.lang.String,
	 * de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * simulation.ESimulationType, boolean,
	 * de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices,
	 * long,
	 * de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory,
	 * de.uni_freiburg.informatik.ultimate.automata.nwalibrary.
	 * INestedWordAutomatonOldApi)
	 */
	@Override
	protected void measureMethodPerformance(final String name, final ESimulationType type, final boolean useSCCs,
			final AutomataLibraryServices services, final long timeout, final StateFactory<STATE> stateFactory,
			final INestedWordAutomatonOldApi<LETTER, STATE> operand) {
		Logger logger = getLogger();
		IProgressAwareTimer progressTimer = services.getProgressMonitorService().getChildTimer(timeout);
		boolean timedOut = false;
		boolean outOfMemory = false;
		Object method = null;

		try {
			if (type.equals(ESimulationType.DIRECT)) {
				DirectNwaGameGraph<LETTER, STATE> graph = new DirectNwaGameGraph<>(services, progressTimer, logger,
						operand, stateFactory);
				graph.generateGameGraphFromAutomaton();
				DirectNwaSimulation<LETTER, STATE> sim = new DirectNwaSimulation<>(progressTimer, logger, useSCCs,
						stateFactory, graph);
				sim.doSimulation();
				method = sim;
			} else if (type.equals(ESimulationType.FAIR)) {
				FairNwaGameGraph<LETTER, STATE> graph = new FairNwaGameGraph<>(services, progressTimer, logger, operand,
						stateFactory);
				graph.generateGameGraphFromAutomaton();
				FairNwaSimulation<LETTER, STATE> sim = new FairNwaSimulation<>(progressTimer, logger, operand, useSCCs,
						stateFactory, graph);
				sim.doSimulation();
				method = sim;
			}
		} catch (OperationCanceledException e) {
			logger.info("Method timed out.");
			timedOut = true;
		} catch (OutOfMemoryError e) {
			logger.info("Method has thrown an out of memory error.");
			outOfMemory = true;
		}
		try {
			appendMethodPerformanceToLog(method, name, type, useSCCs, timedOut, outOfMemory, operand);
		} catch (AutomataLibraryException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * simulation.performance.CompareReduceBuchiSimulation#measurePerformances(
	 * java.lang.String, long,
	 * de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory,
	 * de.uni_freiburg.informatik.ultimate.automata.nwalibrary.
	 * reachableStatesAutomaton.NestedWordAutomatonReachableStates)
	 */
	@Override
	protected void measurePerformances(final String automatonName, final long timeOutMillis,
			final StateFactory<STATE> stateFactory,
			final NestedWordAutomatonReachableStates<LETTER, STATE> reachableOperand) {
		// Direct nwa simulation without SCC
		measureMethodPerformance(automatonName, ESimulationType.DIRECT, false, getServices(), timeOutMillis,
				stateFactory, reachableOperand);
	}
}