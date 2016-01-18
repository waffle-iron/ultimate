/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 * 
 */
public class ConditionTransformer<E> {

	private final IConditionWrapper<E> mWrapper;

	public ConditionTransformer(IConditionWrapper<E> wrapper) {
		mWrapper = wrapper;
	}

	public E toNnf(E condition) {
		if (condition == null) {
			return null;
		}
		if (mWrapper.isAtom(condition)) {
			return condition;
		}
		E current = condition;
		current = makeNnf(current);
		current = simplify(current);
		return current;
	}

	public E toDnf(E condition) {
		if (condition == null) {
			return null;
		}
		if (mWrapper.isAtom(condition)) {
			return condition;
		}
		E current = condition;
		current = makeNnf(current);
		current = simplify(current);
		current = skolemize(current);
		current = makeDnf(current);
		current = simplifyDnf(current);
		return current;
	}

	/**
	 * <ol>
	 * <li>Convert formula in NNF.
	 * <li>Pull negations further in, e.g. convert !(x != y) to (x == y)
	 * <li>Replace all terms of the form x != y with x < y || x > y
	 * </ol>
	 */
	public E rewriteNotEquals(E formula) {
		if (formula == null) {
			return null;
		}

		formula = toNnf(formula);

		if (mWrapper.isAtom(formula)) {
			return mWrapper.rewritePredNotEquals(formula);
		} else if (mWrapper.isNot(formula)) {
			// because the formula is in NNF, the negation can only be in front
			// of a term
			E oper = mWrapper.getOperand(formula);
			E neg = mWrapper.negatePred(oper);
			if (oper == neg) {
				//the operand cannot be negated any more 
				return formula;
			} else {
				//the operand was negated
				return neg;
			}
		} else if (mWrapper.isAnd(formula)) {
			ArrayDeque<E> operands = new ArrayDeque<>();
			Iterator<E> iter = mWrapper.getOperands(formula);
			while (iter.hasNext()) {
				E operand = rewriteNotEquals(iter.next());
				iter.remove();
				operands.addFirst(operand);
			}
			return mWrapper.makeAnd(operands.iterator());
		} else if (mWrapper.isOr(formula)) {
			ArrayDeque<E> operands = new ArrayDeque<>();
			Iterator<E> iter = mWrapper.getOperands(formula);
			while (iter.hasNext()) {
				E operand = rewriteNotEquals(iter.next());
				iter.remove();
				operands.addFirst(operand);
			}
			return mWrapper.makeOr(operands.iterator());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private Collection<E> toTermsTopLevel(E formula) {
		if (formula == null) {
			return null;
		}

		ArrayList<E> terms = new ArrayList<>();
		Iterator<E> iter = mWrapper.getOperands(formula);
		while (iter.hasNext()) {
			terms.add(iter.next());
		}
		return terms;
	}

	public Collection<E> toDnfDisjuncts(E condition) {
		E dnf = toDnf(condition);
		if (dnf == null) {
			return null;
		}
		if (!mWrapper.isOr(dnf)) {
			return Collections.singleton(dnf);
		}
		return toTermsTopLevel(dnf);
	}

	private E simplifyDnf(E formula) {
		formula = simplify(formula);
		if (!mWrapper.isOr(formula)) {
			// is singleton, cannot be simpler
			return formula;
		}
		LinkedHashSet<E> result = getSet(mWrapper.getOperands(formula));

		Iterator<E> operands = mWrapper.getOperands(formula);
		while (operands.hasNext()) {
			E current = operands.next();
			result.remove(current);

			boolean add = true;
			for (E other : result) {
				if (isSubformula(current, other)) {
					add = false;
					break;
				}
			}

			if (add) {
				result.add(current);
			}
		}
		return mWrapper.makeOr(result.iterator());
	}

	private boolean isSubformula(E root, E candidate) {
		// requires normalized nesting
		if (mWrapper.isEqual(root, candidate)) {
			return true;
		}
		if (mWrapper.isAtom(root)) {
			return false;
		}

		if (mWrapper.isAtom(candidate)
				|| (mWrapper.isNot(candidate) && mWrapper.isAtom(mWrapper.getOperand(candidate)))) {
			if (isOperandSubset(root, candidate)) {
				return true;
			}
		} else if (mWrapper.isAnd(root) && mWrapper.isAnd(candidate)) {
			if (isOperandSubset(root, candidate)) {
				return true;
			}
		} else if (mWrapper.isOr(root) && mWrapper.isOr(candidate)) {
			if (isOperandSubset(root, candidate)) {
				return true;
			}
		}

		// descend
		Iterator<E> operands = mWrapper.getOperands(root);
		while (operands.hasNext()) {
			if (isSubformula(operands.next(), candidate)) {
				return true;
			}
		}

		return false;
	}

	private boolean isOperandSubset(E root, E candidate) {
		LinkedHashSet<E> operands = getSet(mWrapper.getOperands(root));
		boolean isSub = true;
		Iterator<E> childOperands = mWrapper.getOperands(candidate);
		while (childOperands.hasNext()) {
			if (!operands.contains(childOperands.next())) {
				isSub = false;
			}
		}
		return isSub;
	}

	private LinkedHashSet<E> getSet(Iterator<E> iter) {
		LinkedHashSet<E> set = new LinkedHashSet<>();
		while (iter.hasNext()) {
			set.add(iter.next());
		}
		return set;
	}

	public E simplify(E current) {
		// TODO: Simplify negations by e.g. converting !(a == b) to (a != b)
		// TODO: Simplify overlapping and/or, e.g. a && (a || b) -> a
		if (mWrapper.isAnd(current) || mWrapper.isOr(current)) {
			current = simplifyAndOr(current);
		}
		return current;
	}

	private E skolemize(E current) {
		// implement skolemization:
		// 1. unify variables
		// 2. move quantifiers outward
		// 3. insert skolem function to eliminate existential quantification
		// 4. drop all universal quantifiers
		return current;
	}

	private E makeDnf(E current) {
		if (mWrapper.isAtom(current)) {
			// nothing to do, already finished
			return current;
		} else if (mWrapper.isNot(current)) {
			if (mWrapper.isAtom(mWrapper.getOperand(current))) {
				// nothing to do, is literal
				return current;
			}
			throw new AssertionError("Input not in NNF");
		} else if (mWrapper.isAnd(current)) {
			// apply distributivity
			ArrayDeque<E> operands = new ArrayDeque<>();

			Iterator<E> iter = mWrapper.getOperands(current);
			// first, make dnf for each operand
			while (iter.hasNext()) {
				E operand = makeDnf(iter.next());
				iter.remove();
				operands.add(operand);
			}

			// then, distribute singletons with ors and eliminate redundant ones
			while (!operands.isEmpty()) {
				E firstOperand = operands.removeLast();
				if (operands.isEmpty()) {
					// If there is no more or, we are finished.
					return firstOperand;
				}
				E secondOperand = operands.removeLast();

				E or;
				E notOr;

				if (mWrapper.isOr(firstOperand)) {
					if (mWrapper.isOr(secondOperand)) {
						// Both operands are or, apply
						// (a || b) && (c || d)
						// = (a && c) || (a && d) || (b && c) || (b && d)

						ArrayList<E> newOrOperands = new ArrayList<>();
						Iterator<E> firstOrOperands = mWrapper.getOperands(firstOperand);
						while (firstOrOperands.hasNext()) {
							Iterator<E> secondOrOperands = mWrapper.getOperands(secondOperand);
							E currentOr = firstOrOperands.next();
							while (secondOrOperands.hasNext()) {
								newOrOperands.add(mWrapper.makeAnd(currentOr, secondOrOperands.next()));
								secondOrOperands.remove();
							}
							firstOrOperands.remove();
						}
						operands.addFirst(mWrapper.makeOr(newOrOperands.iterator()));
						continue;
					} else {
						or = firstOperand;
						notOr = secondOperand;
					}
				} else if (mWrapper.isOr(secondOperand)) {
					or = secondOperand;
					notOr = firstOperand;
				} else {
					// both are not or, merge them
					operands.addFirst(mWrapper.makeAnd(firstOperand, secondOperand));
					continue;
				}

				// only one operand is or, apply
				// a && (b || c) = (a && b) || (a && c)
				ArrayList<E> newOrOperands = new ArrayList<>();
				Iterator<E> currentOrOperands = mWrapper.getOperands(or);
				while (currentOrOperands.hasNext()) {
					newOrOperands.add(mWrapper.makeAnd(currentOrOperands.next(), notOr));
					currentOrOperands.remove();
				}
				operands.addFirst(mWrapper.makeOr(newOrOperands.iterator()));
			}
			throw new AssertionError();
		} else if (mWrapper.isOr(current)) {
			// descend
			ArrayList<E> inner = new ArrayList<>();
			Iterator<E> iter = mWrapper.getOperands(current);
			while (iter.hasNext()) {
				inner.add(makeDnf(iter.next()));
			}
			return mWrapper.makeOr(inner.iterator());
		} else if (mWrapper.isExists(current)) {
			// descend
			return mWrapper.changeExists(current, makeDnf(mWrapper.getOperand(current)));
		} else if (mWrapper.isForall(current)) {
			return mWrapper.changeForall(current, makeDnf(mWrapper.getOperand(current)));
		} else {
			throw new AssertionError();
		}
	}

	private E makeNnf(E condition) {
		if (mWrapper.isAtom(condition)) {
			// nothing to do here
			return condition;
		} else if (mWrapper.isNot(condition)) {
			E operand = mWrapper.getOperand(condition);
			if (mWrapper.isAtom(operand)) {
				// is already in nnf
				return condition;
			} else if (mWrapper.isNot(operand)) {
				// remove double negation
				return makeNnf(mWrapper.getOperand(operand));
			} else if (mWrapper.isOr(operand)) {
				// use de morgan
				ArrayDeque<E> inner = new ArrayDeque<>();
				Iterator<E> iter = mWrapper.getOperands(operand);
				while (iter.hasNext()) {
					inner.add(makeNnf(mWrapper.makeNot(iter.next())));
				}
				return mWrapper.makeAnd(inner.iterator());
			} else if (mWrapper.isAnd(operand)) {
				// use de morgan
				ArrayDeque<E> inner = new ArrayDeque<>();
				Iterator<E> iter = mWrapper.getOperands(operand);
				while (iter.hasNext()) {
					inner.add(makeNnf(mWrapper.makeNot(iter.next())));
				}
				return mWrapper.makeOr(inner.iterator());
			} else if (mWrapper.isForall(operand)) {
				return mWrapper.changeExists(operand, makeNnf(mWrapper.makeNot(mWrapper.getOperand(operand))));
			} else if (mWrapper.isExists(operand)) {
				return mWrapper.changeForall(operand, makeNnf(mWrapper.makeNot(mWrapper.getOperand(operand))));
			} else {
				throw new AssertionError();
			}
		} else {
			// its no atom, its no not, descend
			if (mWrapper.isOr(condition)) {
				ArrayDeque<E> inner = new ArrayDeque<>();
				Iterator<E> iter = mWrapper.getOperands(condition);
				while (iter.hasNext()) {
					inner.add(makeNnf(iter.next()));
				}
				return mWrapper.makeOr(inner.iterator());
			} else if (mWrapper.isAnd(condition)) {
				ArrayDeque<E> inner = new ArrayDeque<>();
				Iterator<E> iter = mWrapper.getOperands(condition);
				while (iter.hasNext()) {
					inner.add(makeNnf(iter.next()));
				}
				return mWrapper.makeAnd(inner.iterator());
			} else if (mWrapper.isForall(condition)) {
				return mWrapper.changeForall(condition, makeNnf(mWrapper.getOperand(condition)));
			} else if (mWrapper.isExists(condition)) {
				return mWrapper.changeExists(condition, makeNnf(mWrapper.getOperand(condition)));
			} else {
				throw new AssertionError();
			}
		}
	}

	private E simplifyAndOr(E formula) {
		E trueTerm = mWrapper.makeTrue();
		E falseTerm = mWrapper.makeFalse();
		E neutral, absorbing;

		boolean isAnd = mWrapper.isAnd(formula);

		if (isAnd) {
			neutral = trueTerm;
			absorbing = falseTerm;
		} else {
			neutral = falseTerm;
			absorbing = trueTerm;
		}
		LinkedHashSet<E> formulas = new LinkedHashSet<E>();

		Iterator<E> iter = mWrapper.getOperands(formula);
		while (iter.hasNext()) {
			E f = iter.next();

			if (mWrapper.isEqual(f, neutral)) {
				continue;
			}
			if (mWrapper.isEqual(f, absorbing)) {
				return f;
			}

			/* Normalize nested and/ors */
			formulas.addAll(mWrapper.normalizeNesting(formula, f));
		}

		if (formulas.size() <= 1) {
			if (formulas.isEmpty()) {
				return neutral;
			} else {
				return formulas.iterator().next();
			}
		}

		if (isAnd) {
			return mWrapper.makeAnd(formulas.iterator());
		} else {
			return mWrapper.makeOr(formulas.iterator());
		}
	}

}