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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.vp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractStateBinaryOperator;

/**
 * The implementation of a simple merge operator on the VP domain. This operator
 * can also be used as widening operator.
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 * @param <ACTION>
 *            Any action.
 * @param <IBoogieVar>
 *            Any variable declaration.
 */
public class VPMergeOperator implements
		IAbstractStateBinaryOperator<VPDomainState> {

	private static final int BUFFER_SIZE = 100;

	protected VPMergeOperator() {
	}

	/**
	 * Merges two abstract states, first and second, into one new abstract
	 * state. All variables that occur in first must also occur in second.<br />
	 * 
	 * <p>
	 * Assume, there is a variable with name "v". The value of "v" in first is
	 * v1 and the value of "v" in second is v2. <br />
	 * </p>
	 * 
	 * <p>
	 * It is distinguished between four cases for the resulting merged value of
	 * "v":<br />
	 * <ol>
	 * <li>v1 is equal to v2:<br />
	 * The resulting value will be v1.</li>
	 * <li>v1 is positive (negative) and v2 is negative (positive):<br />
	 * The resulting value will be \top.</li>
	 * <li>v1 is not 0 (is 0) and v2 is 0 (is not 0):<br />
	 * The resulting value will be \top.</li>
	 * <li>v1 is \bot or v2 is \bot:<br />
	 * The resulting value will be \bot.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param first
	 *            The first state to merge.
	 * @param second
	 *            The second state to merge.
	 */
	@Override
	public VPDomainState apply(VPDomainState first, VPDomainState second) {
//		assert first != null;
//		assert second != null;

		// TODO 
		if (first == null) {
			return second;
		}
		
		if (second == null) {
			return first;
		}
		
		if (!first.hasSameVariables(second)) {
//			throw new UnsupportedOperationException(
//					"Cannot merge two states with a disjoint set of variables.");
		}

		final VPDomainState newState = (VPDomainState) first.copy();

		Map<String, Set<Expression>> mergeExprMap = new HashMap<String, Set<Expression>>(
				first.getExpressionMap());
		Set<Expression> mergeExprSet = new HashSet<Expression>(
				first.getExprSet());

		for (String key : second.getExpressionMap().keySet()) {
			if (!mergeExprMap.containsKey(key)) {
				mergeExprMap.put(key, second.getExpressionMap().get(key));
			} else {
				mergeExprMap.get(key).addAll(
						second.getExpressionMap().get(key));
			}
		}

		mergeExprSet.addAll(second.getExprSet());
		
		newState.setExpressionMap(mergeExprMap);
		newState.setExpressionSet(mergeExprSet);

		return newState;
	}

	/**
	 * Computes the merging of two {@link VPDomainValue}s. {@link VPDomainValue}
	 * s.
	 * 
	 * @param value1
	 *            The first value.
	 * @param value2
	 *            The second value.
	 * @return Returns a new {@link VPDomainValue}.
	 */
	// private VPDomainValue computeMergedExprMap(VPDomainValue value1,
	// VPDomainValue value2) {
	// if (value1.getResult() == value2.getResult()) {
	// return new VPDomainValue(value1.getResult());
	// }
	//
	// if (value1.getResult() == Values.BOTTOM || value2.getResult() ==
	// Values.BOTTOM) {
	// return new VPDomainValue(Values.BOTTOM);
	// }
	//
	// if ((value1.getResult() == Values.POSITIVE && value2.getResult() ==
	// Values.NEGATIVE)
	// || (value1.getResult() == Values.NEGATIVE && value2.getResult() ==
	// Values.POSITIVE)) {
	// return new VPDomainValue(Values.TOP);
	// }
	//
	// if (value1.getResult() == Values.ZERO || value2.getResult() ==
	// Values.ZERO) {
	// return new VPDomainValue(Values.TOP);
	// }
	//
	// final StringBuilder stringBuilder = new StringBuilder(BUFFER_SIZE);
	//
	// stringBuilder.append("Unable to handle value1 = ").append(value1.getResult()).append(" and value2 = ")
	// .append(value2.getResult()).append('.');
	//
	// throw new UnsupportedOperationException(stringBuilder.toString());
	// }

}