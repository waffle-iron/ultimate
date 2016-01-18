/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE ModelCheckerUtils Library.
 * 
 * The ULTIMATE ModelCheckerUtils Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE ModelCheckerUtils Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE ModelCheckerUtils Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE ModelCheckerUtils Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE ModelCheckerUtils Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.normalforms;

import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 * 
 * @param <E>
 */
public interface IConditionWrapper<E> {

	E changeForall(E oldForAll, E operand);

	E makeAnd(E next, E notor);

	Collection<? extends E> normalizeNesting(E formula, E subformula);

	E makeFalse();

	E makeTrue();

	E changeExists(E oldExists, E operand);

	E makeOr(Iterator<E> operands);

	E makeAnd(Iterator<E> operands);

	E makeNot(E operand);

	E getOperand(E formula);
	
	E rewritePredNotEquals(E atom);
	
	E negatePred(E atom);

	Iterator<E> getOperands(E formula);

	boolean isAtom(E formula);

	boolean isNot(E formula);

	boolean isAnd(E formula);

	boolean isOr(E formula);

	boolean isExists(E formula);

	boolean isForall(E formula);

	boolean isEqual(E one, E other);
}