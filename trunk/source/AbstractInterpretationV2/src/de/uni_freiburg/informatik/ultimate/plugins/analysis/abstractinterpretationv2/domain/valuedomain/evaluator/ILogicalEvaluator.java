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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.valuedomain.evaluator;

import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.valuedomain.BooleanValue;

/**
 * Extends the IEvaluator by the function computeLogicalResult which is used to return new abstract states based on the
 * logical evaluation of the evaluator.
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 * @param <T>
 *            Any type.
 * @param <ACTION>
 *            Any action.
 * @param <VARDECL>
 *            Any variable declaration.
 */
public interface ILogicalEvaluator<T, ACTION, VARDECL> extends IEvaluator<T, ACTION, VARDECL> {

	/**
	 * @return The boolean value of the logical interpretation of the current expression.
	 */
	public BooleanValue booleanValue();

	/**
	 * States whether somewhere in the evaluator occurs a boolean value. This is needed to determine if the boolean
	 * value should be used instead of the returned abstract value. Note: This is needed in the handling of logical
	 * operators in evaluators and is only valid, if there exists 0 or 1 variable identifier in each subtree of the
	 * evaluator.
	 * 
	 * @return <code>true</code> if and only if in the sub-tree is a boolean literal or interpretation present,
	 *         <code>false</code> otherwise.
	 */
	public boolean containsBool();
}