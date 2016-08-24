/*
 * Copyright (C) 2015 Christian Schilling (schillic@informatik.uni-freiburg.de)
 * Copyright (C) 2015-2016 Daniel Tischner
 * Copyright (C) 2014-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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
/**
 * Reduce the state space of a given Buchi automaton, as described in the paper
 * "Fair simulation relations, parity games and state space reduction for
 * Buchi automata" - Etessami, Wilke and Schuller.
 */
package de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.simulation.direct;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.AutomataOperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.AutomatonDefinitionPrinter;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.LibraryIdentifiers;
import de.uni_freiburg.informatik.ultimate.automata.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.IsIncluded;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;

/**
 * Operation that reduces a given buechi automaton by using
 * {@link DirectSimulation}.<br/>
 * Once constructed the reduction automatically starts, the result can be get by
 * using {@link #getResult()}.<br/>
 * <br/>
 * 
 * For correctness its important that the inputed automaton has <b>no dead
 * ends</b> nor <b>duplicate transitions</b>.
 * 
 * @author Daniel Tischner
 * @author heizmann@informatik.uni-freiburg.de
 * @author schillic@informatik.uni-freiburg.de
 * 
 * @param <LETTER>
 *            Letter class of buechi automaton
 * @param <STATE>
 *            State class of buechi automaton
 */
public class MinimizeDfaSimulation<LETTER, STATE> implements IOperation<LETTER, STATE> {

	/**
	 * The logger used by the Ultimate framework.
	 */
	private final ILogger mLogger;

	/**
	 * The inputed buechi automaton.
	 */
	private INestedWordAutomaton<LETTER, STATE> mOperand;

	/**
	 * The resulting possible reduced buechi automaton.
	 */
	private INestedWordAutomaton<LETTER, STATE> mResult;

	/**
	 * Service provider of Ultimate framework.
	 */
	private final AutomataLibraryServices mServices;

	/**
	 * Creates a new buechi reduce object that starts reducing the given buechi
	 * automaton.<br/>
	 * Once finished the result can be get by using {@link #getResult()}.
	 * 
	 * @param services
	 *            Service provider of Ultimate framework
	 * @param stateFactory
	 *            The state factory used for creating states
	 * @param operand
	 *            The buechi automaton to reduce
	 * @throws AutomataOperationCanceledException
	 *             If the operation was canceled, for example from the Ultimate
	 *             framework.
	 */
	public MinimizeDfaSimulation(final AutomataLibraryServices services, final StateFactory<STATE> stateFactory,
			final INestedWordAutomaton<LETTER, STATE> operand) throws AutomataOperationCanceledException {
		this(services, stateFactory, operand,
				new DirectSimulation<>(services.getProgressMonitorService(),
						services.getLoggingService().getLogger(LibraryIdentifiers.PLUGIN_ID), true, stateFactory,
						new DirectGameGraph<>(services, services.getProgressMonitorService(),
								services.getLoggingService().getLogger(LibraryIdentifiers.PLUGIN_ID), operand,
								stateFactory)));
	}

	/**
	 * Creates a new buechi reduce object that starts reducing the given buechi
	 * automaton using a given simulation.<br/>
	 * Once finished the result can be get by using {@link #getResult()}.
	 * 
	 * @param services
	 *            Service provider of Ultimate framework
	 * @param stateFactory
	 *            The state factory used for creating states
	 * @param operand
	 *            The buechi automaton to reduce
	 * @param simulation
	 *            Simulation to use for reduction
	 * @throws AutomataOperationCanceledException
	 *             If the operation was canceled, for example from the Ultimate
	 *             framework.
	 */
	protected MinimizeDfaSimulation(final AutomataLibraryServices services, final StateFactory<STATE> stateFactory,
			final INestedWordAutomaton<LETTER, STATE> operand, final DirectSimulation<LETTER, STATE> simulation)
					throws AutomataOperationCanceledException {
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(LibraryIdentifiers.PLUGIN_ID);
		mOperand = operand;
		mLogger.info(startMessage());

		simulation.getGameGraph().generateGameGraphFromAutomaton();
		simulation.doSimulation();
		mResult = simulation.getResult();

		final boolean compareWithNonSccResult = false;
		if (compareWithNonSccResult) {
			final DirectGameGraph<LETTER, STATE> graph = new DirectGameGraph<>(mServices,
					mServices.getProgressMonitorService(), mLogger, mOperand, stateFactory);
			graph.generateGameGraphFromAutomaton();
			final DirectSimulation<LETTER, STATE> nonSccSim = new DirectSimulation<LETTER, STATE>(
					mServices.getProgressMonitorService(), mLogger, false, stateFactory, graph);
			nonSccSim.doSimulation();
			final INestedWordAutomaton<LETTER, STATE> nonSCCresult = nonSccSim.getResult();
			if (mResult.size() != nonSCCresult.size()) {
				throw new AssertionError();
			}
		}

		mLogger.info(exitMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_freiburg.informatik.ultimate.automata.IOperation#checkResult(de.
	 * uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory)
	 */
	@Override
	public boolean checkResult(final StateFactory<STATE> stateFactory) throws AutomataLibraryException {
		mLogger.info("Start testing correctness of " + operationName());
		boolean correct = true;
		
		correct &= new IsIncluded<>(mServices, stateFactory, mOperand, mResult).getResult();
		correct &= new IsIncluded<>(mServices, stateFactory, mResult, mOperand).getResult();
		
		if (!correct) {
			AutomatonDefinitionPrinter.writeToFileIfPreferred(mServices,
					operationName() + "Failed", "language is different", mOperand);
		}
		mLogger.info("Finished testing correctness of " + operationName());
		return correct;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_freiburg.informatik.ultimate.automata.IOperation#exitMessage()
	 */
	@Override
	public String exitMessage() {
		return "Finished " + operationName() + " Result " + mResult.sizeInformation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.IOperation#getResult()
	 */
	@Override
	public INestedWordAutomaton<LETTER, STATE> getResult() {
		return mResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_freiburg.informatik.ultimate.automata.IOperation#operationName()
	 */
	@Override
	public String operationName() {
		return "minimizeDfaSimulation";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_freiburg.informatik.ultimate.automata.IOperation#startMessage()
	 */
	@Override
	public String startMessage() {
		return "Start " + operationName() + ". Operand has " + mOperand.sizeInformation();
	}

	/**
	 * Gets the logger used by the Ultimate framework.
	 * 
	 * @return The logger used by the Ultimate framework.
	 */
	protected ILogger getLogger() {
		return mLogger;
	}

	/**
	 * Gets the inputed automaton.
	 * 
	 * @return The inputed automaton.
	 */
	protected INestedWordAutomaton<LETTER, STATE> getOperand() {
		return mOperand;
	}

	/**
	 * Gets the service provider of the Ultimate framework.
	 * 
	 * @return The service provider of the Ultimate framework.
	 */
	protected AutomataLibraryServices getServices() {
		return mServices;
	}
}
