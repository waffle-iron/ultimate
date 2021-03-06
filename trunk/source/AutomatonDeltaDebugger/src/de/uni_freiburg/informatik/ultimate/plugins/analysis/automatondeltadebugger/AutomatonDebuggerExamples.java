/*
 * Copyright (C) 2015-2016 Christian Schilling (schillic@informatik.uni-freiburg.de)
 * Copyright (C) 2015-2016 University of Freiburg
 * 
 * This file is part of the ULTIMATE Automaton Delta Debugger.
 * 
 * The ULTIMATE Automaton Delta Debugger is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * The ULTIMATE Automaton Delta Debugger is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Automaton Delta Debugger. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7: If you modify the
 * ULTIMATE Automaton Delta Debugger, or any covered work, by linking or
 * combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE Automaton Delta Debugger grant you additional
 * permission to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.analysis.automatondeltadebugger;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.IDoubleDeckerAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.Complement;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.RemoveDeadEnds;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.RemoveNonLiveStates;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.RemoveUnreachable;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.minimization.MinimizeNwaMaxSat2;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.minimization.MinimizeNwaOverapproximation;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.minimization.ShrinkNwa;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.minimization.maxsat.arrays.MinimizeNwaMaxSAT;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.simulation.delayed.BuchiReduce;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.simulation.delayed.nwa.ReduceNwaDelayedSimulation;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.simulation.direct.nwa.ReduceNwaDirectSimulation;
import de.uni_freiburg.informatik.ultimate.automata.statefactory.IStateFactory;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;

/**
 * Examples used by delta debugger.
 * <p>
 * NOTE: Users may insert their sample code as a new method and leave it here.
 * 
 * @author Christian Schilling (schillic@informatik.uni-freiburg.de)
 * @param <LETTER>
 *            letter type
 * @param <STATE>
 *            state type
 */
@SuppressWarnings("squid:S00112")
public class AutomatonDebuggerExamples<LETTER, STATE> {
	private final AutomataLibraryServices mServices;
	
	/**
	 * Constructor.
	 * 
	 * @param services
	 *            Ultimate services
	 */
	public AutomatonDebuggerExamples(final IUltimateServiceProvider services) {
		mServices = new AutomataLibraryServices(services);
	}
	
	/**
	 * Implemented operations for quick usage.
	 * <p>
	 * NOTE: If another operation is supported, add a value here.
	 */
	public enum EOperationType {
		/**
		 * {@link MinimizeNwaMaxSAT}.
		 */
		MINIMIZE_NWA_MAXSAT,
		/**
		 * {@link MinimizeNwaMaxSAT2}.
		 */
		MINIMIZE_NWA_MAXSAT2,
		/**
		 * {@link ReduceNwaDirectSimulation}.
		 */
		REDUCE_NWA_DIRECT_SIMULATION,
		/**
		 * {@link ReduceNwaDelayedSimulation}.
		 */
		REDUCE_NWA_DELAYED_SIMULATION,
		/**
		 * {@link ShrinkNwa}.
		 */
		SHRINK_NWA,
		/**
		 * {@link BuchiReduce}.
		 */
		BUCHI_REDUCE,
		/**
		 * {@link Complement}.
		 */
		COMPLEMENT,
		/**
		 * {@link MinimizeNwaOverapproximation}.
		 */
		MINIMIZE_NWA_OVERAPPROXIMATION,
		/**
		 * {@link RemoveNonLiveStates}.
		 */
		REMOVE_NON_LIVE_STATES
	}
	
	/**
	 * Getter for an {@link IOperation}.
	 * 
	 * @param type
	 *            operation type
	 * @param automaton
	 *            nested word automaton
	 * @param factory
	 *            state factory
	 * @return operation corresponding to type
	 * @throws Throwable
	 *             when operation fails
	 */
	public IOperation<LETTER, STATE> getOperation(final EOperationType type,
			final INestedWordAutomaton<LETTER, STATE> automaton, final IStateFactory<STATE> factory) throws Throwable {
		final IOperation<LETTER, STATE> operation;
		switch (type) {
			case MINIMIZE_NWA_MAXSAT:
				operation = minimizeNwaMaxSat(automaton, factory);
				break;
			
			case MINIMIZE_NWA_MAXSAT2:
				operation = minimizeNwaMaxSat2(automaton, factory);
				break;
			
			case REDUCE_NWA_DIRECT_SIMULATION:
				operation = reduceNwaDirectSimulation(automaton, factory);
				break;
			
			case REDUCE_NWA_DELAYED_SIMULATION:
				operation = reduceNwaDelayedSimulation(automaton, factory);
				break;
			
			case SHRINK_NWA:
				operation = shrinkNwa(automaton, factory);
				break;
			
			case BUCHI_REDUCE:
				operation = buchiReduce(automaton, factory);
				break;
			
			case COMPLEMENT:
				operation = complement(automaton, factory);
				break;
			
			case MINIMIZE_NWA_OVERAPPROXIMATION:
				operation = minimizeNwaOverapproximation(automaton, factory);
				break;
			
			case REMOVE_NON_LIVE_STATES:
				operation = removeNonLiveStates(automaton);
				break;
			
			default:
				throw new IllegalArgumentException("Unknown operation.");
		}
		return operation;
	}
	
	/**
	 * @param automaton
	 *            The automaton.
	 * @param factory
	 *            state factory
	 * @return new {@link MinimizeNwaMaxSAT} instance
	 * @throws Throwable
	 *             when error occurs
	 */
	public IOperation<LETTER, STATE> minimizeNwaMaxSat(final INestedWordAutomaton<LETTER, STATE> automaton,
			final IStateFactory<STATE> factory) throws Throwable {
		final IDoubleDeckerAutomaton<LETTER, STATE> preprocessed =
				new RemoveUnreachable<LETTER, STATE>(mServices, automaton).getResult();
		return new MinimizeNwaMaxSAT<>(mServices, factory, preprocessed);
	}
	
	/**
	 * @param automaton
	 *            The automaton.
	 * @param factory
	 *            state factory
	 * @return new {@link MinimizeNwaMaxSAT2} instance
	 * @throws Throwable
	 *             when error occurs
	 */
	public IOperation<LETTER, STATE> minimizeNwaMaxSat2(final INestedWordAutomaton<LETTER, STATE> automaton,
			final IStateFactory<STATE> factory) throws Throwable {
		final IDoubleDeckerAutomaton<LETTER, STATE> preprocessed =
				new RemoveDeadEnds<LETTER, STATE>(mServices, automaton).getResult();
		return new MinimizeNwaMaxSat2<>(mServices, factory, preprocessed);
	}
	
	/**
	 * @param automaton
	 *            The automaton.
	 * @param factory
	 *            state factory
	 * @return new {@link ReduceNwaDirectSimulation} instance
	 * @throws Throwable
	 *             when error occurs
	 */
	public IOperation<LETTER, STATE> reduceNwaDirectSimulation(final INestedWordAutomaton<LETTER, STATE> automaton,
			final IStateFactory<STATE> factory) throws Throwable {
		final IDoubleDeckerAutomaton<LETTER, STATE> preprocessed =
				new RemoveDeadEnds<LETTER, STATE>(mServices, automaton).getResult();
		return new ReduceNwaDirectSimulation<>(mServices, factory, preprocessed, false);
	}
	
	/**
	 * @param automaton
	 *            The automaton.
	 * @param factory
	 *            state factory
	 * @return new {@link ReduceNwaDelayedSimulation} instance
	 * @throws Throwable
	 *             when error occurs
	 */
	public IOperation<LETTER, STATE> reduceNwaDelayedSimulation(final INestedWordAutomaton<LETTER, STATE> automaton,
			final IStateFactory<STATE> factory) throws Throwable {
		final IDoubleDeckerAutomaton<LETTER, STATE> preprocessed =
				new RemoveDeadEnds<LETTER, STATE>(mServices, automaton).getResult();
		return new ReduceNwaDelayedSimulation<>(mServices, factory, preprocessed, false);
	}
	
	/**
	 * @param automaton
	 *            The automaton.
	 * @param factory
	 *            state factory
	 * @return new {@link ReduceNwaDirectSimulation} instance
	 * @throws Throwable
	 *             when error occurs
	 */
	public IOperation<LETTER, STATE> shrinkNwa(final INestedWordAutomaton<LETTER, STATE> automaton,
			final IStateFactory<STATE> factory) throws Throwable {
		final IDoubleDeckerAutomaton<LETTER, STATE> preprocessed =
				new RemoveUnreachable<LETTER, STATE>(mServices, automaton).getResult();
		return new ShrinkNwa<>(mServices, factory, preprocessed);
	}
	
	/**
	 * @param automaton
	 *            The automaton.
	 * @param factory
	 *            state factory
	 * @return new {@link BuchiReduce} instance
	 * @throws Throwable
	 *             when error occurs
	 */
	public IOperation<LETTER, STATE> buchiReduce(final INestedWordAutomaton<LETTER, STATE> automaton,
			final IStateFactory<STATE> factory) throws Throwable {
		final IDoubleDeckerAutomaton<LETTER, STATE> preprocessed =
				new RemoveDeadEnds<LETTER, STATE>(mServices, automaton).getResult();
		return new BuchiReduce<>(mServices, factory, preprocessed);
	}
	
	/**
	 * @param automaton
	 *            The automaton.
	 * @param factory
	 *            state factory
	 * @return new {@code Complement()} instance
	 * @throws Throwable
	 *             when error occurs
	 */
	public IOperation<LETTER, STATE> complement(final INestedWordAutomaton<LETTER, STATE> automaton,
			final IStateFactory<STATE> factory) throws Throwable {
		return new Complement<>(mServices, factory, automaton);
	}
	
	/**
	 * @param automaton
	 *            The automaton.
	 * @param factory
	 *            state factory
	 * @return new {@link MinimizeNwaOverapproximation} instance
	 * @throws Throwable
	 *             when error occurs
	 */
	public IOperation<LETTER, STATE> minimizeNwaOverapproximation(final INestedWordAutomaton<LETTER, STATE> automaton,
			final IStateFactory<STATE> factory) throws Throwable {
		final IDoubleDeckerAutomaton<LETTER, STATE> preprocessed =
				new RemoveUnreachable<LETTER, STATE>(mServices, automaton).getResult();
		return new MinimizeNwaOverapproximation<>(mServices, factory, preprocessed);
	}
	
	/**
	 * @param automaton
	 *            The automaton.
	 * @param factory
	 *            state factory
	 * @return new {@link MinimizeNwaOverapproximation} instance
	 * @throws Throwable
	 *             when error occurs
	 */
	private IOperation<LETTER, STATE> removeNonLiveStates(final INestedWordAutomaton<LETTER, STATE> automaton)
			throws Throwable {
		return new RemoveNonLiveStates<>(mServices, automaton);
	}
}
