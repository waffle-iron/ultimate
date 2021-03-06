/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE AbstractInterpretationV2 plug-in.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE AbstractInterpretationV2 plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE AbstractInterpretationV2 plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE AbstractInterpretationV2 plug-in grant you additional permission 
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator;

import java.util.List;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState;

/**
 * Default interface for an expression evaluator. Each Evaluator should implement this interface in order to allow for
 * expressions to be evaluated.
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * 
 * @param <VALUE>
 *            The value type of the abstract domain.
 * @param <ACTION>
 *            Any action type.
 */
public interface IEvaluator<VALUE, STATE extends IAbstractState<STATE, ACTION, IBoogieVar>, ACTION> {

	/**
	 * Evaluates the evaluator with all its sub-evaluators according to the given state.
	 * 
	 * @param currentState
	 *            The originating state to evaluate from.
	 * @return A new evaluation result that contains the result of the evaluation.
	 */
	List<IEvaluationResult<VALUE>> evaluate(final STATE currentState);

	/**
	 * Computes the inverse of the application of the evaluate function with a given reference value and input state.
	 * 
	 * @param computedState
	 *            Contains the reference value and the input state to compute the inverse for.
	 * @return The result of the inverse application of the evaluate function.
	 */
	List<STATE> inverseEvaluate(final IEvaluationResult<VALUE> computedValue, final STATE currentState);

	/**
	 * Adds a sub-evaluator to the evaluator.
	 * 
	 * @param evaluator
	 *            The evaluator to add.
	 */
	void addSubEvaluator(final IEvaluator<VALUE, STATE, ACTION> evaluator);

	/**
	 * @return The set of all variable identifiers that occur in all sub evaluators.
	 */
	Set<IBoogieVar> getVarIdentifiers();

	/**
	 * @return <code>true</code> if and only if there are still free sub evaluators. <code>false</code> otherwise.
	 */
	boolean hasFreeOperands();

	/**
	 * States whether somewhere in the evaluator occurs a boolean value. This is needed to determine if the boolean
	 * value should be used instead of the returned abstract value. Note: This is needed in the handling of logical
	 * operators in evaluators and is only valid, if there exists 0 or 1 variable identifier in each subtree of the
	 * evaluator.
	 * 
	 * @return <code>true</code> if and only if in the sub-tree is a boolean literal or interpretation present,
	 *         <code>false</code> otherwise.
	 */
	boolean containsBool();
}
