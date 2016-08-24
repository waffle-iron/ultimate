/*
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
package de.uni_freiburg.informatik.ultimate.automata.nestedword.operations;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomatonSimple;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedRun;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.UnaryNwaOperation;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.incrementalinclusion.InclusionViaDifference;

/**
 * Operation that takes three Operands A, B_1 and B_2 and checks if the language
 * of A is included in the union of the languages of B_1 and B_2.
 * Since this operation is restricted to exactly three operands
 * it is not useful in practice and only used for testing correctness
 * of our incremental inclusion check.
 * @author heizmann@informatik.uni-freiburg.de
 *
 * @param <LETTER> letter type
 * @param <STATE> state type
 */
public class IsIncluded2<LETTER, STATE>
		extends UnaryNwaOperation<LETTER, STATE>
		implements IOperation<LETTER,STATE> {
	
	private final INestedWordAutomatonSimple<LETTER, STATE> mB1;
	private final INestedWordAutomatonSimple<LETTER, STATE> mB2;
	
	private final InclusionViaDifference<LETTER, STATE> mInclusionViaDifference;
	
	private final Boolean mResult;
	private final NestedRun<LETTER, STATE> mCounterexample;
	
	/**
	 * @param services Ultimate services
	 * @param stateFactory state factory
	 * @param operandA minuend
	 * @param operandB1 first subtrahend
	 * @param operandB2 second subtrahend
	 * @throws AutomataLibraryException if construction fails
	 */
	public IsIncluded2(final AutomataLibraryServices services,
			final StateFactory<STATE> stateFactory,
			final INestedWordAutomatonSimple<LETTER, STATE> operandA,
			final INestedWordAutomatonSimple<LETTER, STATE> operandB1,
			final INestedWordAutomatonSimple<LETTER, STATE> operandB2)
					throws AutomataLibraryException {
		super(services, operandA);
		mB1 = operandB1;
		mB2 = operandB2;
		// workaround until Matthias implemented this
		mLogger.info(startMessage());
		mInclusionViaDifference =
				new InclusionViaDifference<>(services, stateFactory, operandA);
		mInclusionViaDifference.addSubtrahend(mB1);
		mInclusionViaDifference.addSubtrahend(mB2);
		mCounterexample = mInclusionViaDifference.getCounterexample();
		mResult = (mCounterexample == null);
		mLogger.info(exitMessage());
	}

	@Override
	public String operationName() {
		return "isIncluded2";
	}

	@Override
	public String startMessage() {
			return "Start " + operationName() + ". Operand A "
					+ mOperand.sizeInformation() + ". Operand B_1 "
					+ mB1.sizeInformation() + ". Operand B_2 "
					+ mB2.sizeInformation();
	}

	@Override
	public String exitMessage() {
		return "Finished " + operationName() + ". Language is "
				+ (mResult ? "" : "not ") + "included";
	}

	@Override
	public Boolean getResult() {
		return mResult;
	}

	public NestedRun<LETTER, STATE> getCounterexample() {
		return mCounterexample;
	}

	@Override
	public boolean checkResult(final StateFactory<STATE> stateFactory)
			throws AutomataLibraryException {
		// FIXME: result check not implemented yet
		mLogger.warn("FIXME: result check not implemented yet");
		return true;
	}
}
