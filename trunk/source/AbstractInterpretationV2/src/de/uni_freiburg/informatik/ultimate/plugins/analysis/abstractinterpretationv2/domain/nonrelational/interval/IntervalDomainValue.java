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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.interval;

import java.math.BigDecimal;

import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.Util;

/**
 * Representation of an interval value in the interval domain.
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class IntervalDomainValue implements Comparable<IntervalDomainValue> {

	private IntervalValue mLower;
	private IntervalValue mUpper;

	private boolean mIsBottom;

	/**
	 * Constructor for a new {@link IntervalDomainValue}. The interval created will be (-&infin; ; &infin;).
	 */
	protected IntervalDomainValue() {
		this(false);
	}

	/**
	 * Constructor for a new {@link IntervalDomainValue}. The interval created is dependent on the value of the
	 * parameter. If {@code isBottom} is set to <code>false</code>, the created interval will be (-&infin; ; &infin;).
	 * If {@code isBottom} is set to <code>true</code>, the created interval will be &bot;.
	 * 
	 * @param isBottom
	 *            Specifies whether the interval should be &bot; or an actual interval.
	 */
	protected IntervalDomainValue(boolean isBottom) {
		if (isBottom) {
			mLower = null;
			mUpper = null;
			mIsBottom = true;
		} else {
			mLower = new IntervalValue();
			mUpper = new IntervalValue();
			mIsBottom = false;
		}
	}

	protected IntervalDomainValue(IntervalValue lower, IntervalValue upper) {
		if (!lower.isInfinity() && !upper.isInfinity()) {
			if (lower.getValue().compareTo(upper.getValue()) > 0) {
				throw new UnsupportedOperationException(
				        "The lower value must be smaller than or qual to the upper value. Lower: " + lower.getValue()
				                + ", Upper: " + upper.getValue());
			}
		}

		mLower = lower;
		mUpper = upper;

		mIsBottom = false;
	}

	/**
	 * Computes the result of a greater or equal operation with another interval:
	 * <p>
	 * [a, b] &gt;= [c, d] results in [max(a, c), b]
	 * </p>
	 * 
	 * @param other
	 *            The value to compare against.
	 * @return A new {@link IntervalDomainValue} that is the result of the greater or equal operation.
	 */
	protected IntervalDomainValue greaterOrEqual(IntervalDomainValue other) {
		assert other != null;

		if (mIsBottom || other.mIsBottom) {
			return new IntervalDomainValue(true);
		}

		IntervalValue lowerMax;

		if (mLower.isInfinity() && other.mLower.isInfinity()) {
			lowerMax = new IntervalValue();
		} else {
			if (mLower.isInfinity()) {
				lowerMax = new IntervalValue(other.mLower);
			} else if (other.mLower.isInfinity()) {
				lowerMax = new IntervalValue(mLower);
			} else {
				if (mLower.getValue().compareTo(other.mLower.getValue()) > 0) {
					lowerMax = new IntervalValue(mLower);
				} else {
					lowerMax = new IntervalValue(other.mLower);
				}
			}
		}

		if (!lowerMax.isInfinity()) {
			if (lowerMax.compareTo(mUpper) > 0) {
				return new IntervalDomainValue(true);
			}
		}

		return new IntervalDomainValue(lowerMax, new IntervalValue(mUpper));
	}

	/**
	 * Computes the result of a less or equal operation with another interval:
	 * <p>
	 * [a, b] &lt;= [c, d] results in [a, min(b, d)]
	 * </p>
	 * 
	 * @param other
	 *            The value to compare against.
	 * @return A new {@link IntervalDomainValue} that is the result of the less or equal operation.
	 */
	protected IntervalDomainValue lessOrEqual(IntervalDomainValue other) {
		assert other != null;

		if (mIsBottom || other.mIsBottom) {
			return new IntervalDomainValue(true);
		}

		IntervalValue upperMin;

		if (mUpper.isInfinity()) {
			upperMin = new IntervalValue(other.mUpper);
		} else if (other.mUpper.isInfinity()) {
			upperMin = new IntervalValue(mUpper);
		} else {
			if (mUpper.getValue().compareTo(other.mUpper.getValue()) < 0) {
				upperMin = new IntervalValue(mUpper);
			} else {
				upperMin = new IntervalValue(other.mUpper);
			}
		}

		if (!mLower.isInfinity()) {
			if (mLower.compareTo(upperMin) > 0) {
				return new IntervalDomainValue(true);
			}
		}

		return new IntervalDomainValue(new IntervalValue(mLower), upperMin);
	}

	@Override
	public int compareTo(IntervalDomainValue o) {
		throw new UnsupportedOperationException(
		        "The compareTo operation is not defined on arbitrary intervals and can therefore not be used.");
	}

	/**
	 * Compares <code>this</code> with another {@link IntervalDomainValue} for equality.
	 * 
	 * @param other
	 *            The other value to compare to.
	 * @return <code>true</code> if and only if <code>this</code> and other are both bottom, or if the lower and upper
	 *         bounds are the same. <code>false</code> otherwise.
	 */
	protected boolean isEqualTo(IntervalDomainValue other) {
		if (other == null) {
			return false;
		}

		if (other == this) {
			return true;
		}

		if (mIsBottom || other.mIsBottom) {
			return mIsBottom == other.mIsBottom;
		}

		if (mLower.equals(other.mLower) && mUpper.equals(other.mUpper)) {
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		if (mIsBottom) {
			return "{}";
		}

		String lower = (mLower.isInfinity() ? "-\\infty" : mLower.toString());
		String upper = (mUpper.isInfinity() ? "\\infty" : mUpper.toString());

		return "[ " + lower + "; " + upper + " ]";
	}

	/**
	 * Adds two {@link IntervalDomainValue}s following the scheme:
	 * 
	 * <p>
	 * <ul>
	 * <li>[a, b] + [c, d] = [a + c, b + d]</li>
	 * </ul>
	 * </p>
	 * 
	 * @param firstResult
	 *            The first interval.
	 * @param secondResult
	 *            The second interval.
	 * @return A new evaluation result corresponding to the addition of the two input intervals.
	 */
	protected IntervalDomainValue add(IntervalDomainValue other) {

		assert other != null;

		if (isBottom() || other.isBottom()) {
			return new IntervalDomainValue(true);
		}

		if (isInfinity() || other.isInfinity()) {
			return new IntervalDomainValue();
		}

		IntervalValue lowerBound = new IntervalValue();
		IntervalValue upperBound = new IntervalValue();

		// Compute lower bound
		if (getLower().isInfinity() || other.getLower().isInfinity()) {
			lowerBound.setToInfinity();
		} else {
			lowerBound.setValue(getLower().getValue().add(other.getLower().getValue()));
		}

		// Compute upper bound
		if (getUpper().isInfinity() || other.getUpper().isInfinity()) {
			upperBound.setToInfinity();
		} else {
			upperBound.setValue(getUpper().getValue().add(other.getUpper().getValue()));
		}

		return new IntervalDomainValue(lowerBound, upperBound);
	}

	/**
	 * Returns <code>true</code> if and only if <code>0</code> is part of the interval.
	 * 
	 * @return <code>true</code> if 0 is part of the interval, <code>false</code> otherwise.
	 */
	protected boolean containsZero() {
		if (mIsBottom) {
			return false;
		}

		if (isInfinity()) {
			return true;
		}

		return mLower.getValue().signum() <= 0 && mUpper.getValue().signum() >= 0;
	}

	/**
	 * Returns the lower value of the interval.
	 * 
	 * @return
	 */
	protected IntervalValue getLower() {
		return mLower;
	}

	/**
	 * Returns the upper value of the interval.
	 * 
	 * @return
	 */
	protected IntervalValue getUpper() {
		return mUpper;
	}

	/**
	 * Intersects the current interval with another interval.
	 * 
	 * @param other
	 *            The other interval to intersect with.
	 * @return A new {@link IntervalDomainValue} representing the result of the intersection.
	 */
	protected IntervalDomainValue intersect(IntervalDomainValue other) {
		assert other != null;

		if (mIsBottom || other.mIsBottom) {
			return new IntervalDomainValue(true);
		}

		if (isInfinity() && other.isInfinity()) {
			return new IntervalDomainValue();
		}

		IntervalValue newLower, newUpper;

		if (mLower.compareTo(other.mLower) > 0) {
			if (mLower.isInfinity()) {
				newLower = new IntervalValue(other.mLower.getValue());
			} else {
				newLower = new IntervalValue(mLower.getValue());
			}
		} else if (mLower.compareTo(other.mLower) == 0) {
			if (mLower.isInfinity()) {
				newLower = new IntervalValue();
			} else {
				newLower = new IntervalValue(mLower.getValue());
			}
		} else {
			if (other.mLower.isInfinity()) {
				newLower = new IntervalValue(mLower.getValue());
			} else {
				newLower = new IntervalValue(other.mLower.getValue());
			}
		}

		if (mUpper.compareTo(other.mUpper) < 0) {
			newUpper = new IntervalValue(mUpper.getValue());
		} else if (mUpper.compareTo(other.mUpper) == 0) {
			if (mUpper.isInfinity()) {
				newUpper = new IntervalValue();
			} else {
				newUpper = new IntervalValue(mUpper.getValue());
			}
		} else {
			newUpper = new IntervalValue(other.mUpper.getValue());
		}

		if (!newLower.isInfinity() && newLower.compareTo(newUpper) > 0) {
			return new IntervalDomainValue(true);
		}

		if (!newUpper.isInfinity() && !newLower.isInfinity() && newUpper.compareTo(newLower) < 0) {
			return new IntervalDomainValue(true);
		}

		return new IntervalDomainValue(newLower, newUpper);
	}

	/**
	 * Computes the merger of this with another {@link IntervalDomainValue}.
	 * 
	 * @param other
	 *            The other interval to merge with.
	 * @return A new interval which is the result of merging this with other.
	 */
	protected IntervalDomainValue merge(IntervalDomainValue other) {
		assert other != null;

		if (isBottom() || other.isBottom()) {
			return new IntervalDomainValue(true);
		}

		if (equals(other)) {
			if (isBottom()) {
				return new IntervalDomainValue(true);
			} else if (isInfinity()) {
				return new IntervalDomainValue(new IntervalValue(), new IntervalValue());
			} else {
				IntervalValue newLower;
				if (mLower.isInfinity()) {
					newLower = new IntervalValue();
				} else {
					newLower = new IntervalValue(mLower.getValue());
				}

				IntervalValue newUpper;
				if (mUpper.isInfinity()) {
					newUpper = new IntervalValue();
				} else {
					newUpper = new IntervalValue(mUpper.getValue());
				}
				return new IntervalDomainValue(newLower, newUpper);
			}
		}

		IntervalValue lower;
		IntervalValue upper;

		if (getLower().isInfinity() || other.getLower().isInfinity()) {
			lower = new IntervalValue();
		} else {
			if (getLower().compareTo(other.getLower()) < 0) {
				lower = new IntervalValue(getLower().getValue());
			} else {
				lower = new IntervalValue(other.getLower().getValue());
			}
		}

		if (getUpper().isInfinity() || other.getUpper().isInfinity()) {
			upper = new IntervalValue();
		} else {
			if (getUpper().compareTo(other.getUpper()) < 0) {
				upper = new IntervalValue(getUpper().getValue());
			} else {
				upper = new IntervalValue(other.getUpper().getValue());
			}
		}

		return new IntervalDomainValue(lower, upper);
	}

	/**
	 * Note that a return value of <code>false</code> may also mean that one or both of the interval bounds is -&infin;
	 * or &infin;.
	 * 
	 * @return <code>true</code> if the current interval is &bot;, <code>false</code> otherwise.
	 */
	protected boolean isBottom() {
		return mIsBottom;
	}

	/**
	 * @return <code>true</code> if the interval is infinity, i.e. if the interval is (-&infin; ; &infin;).
	 */
	protected boolean isInfinity() {
		if (mIsBottom) {
			return false;
		}
		return mLower.isInfinity() && mUpper.isInfinity();
	}

	/**
	 * Returns <code>true</code> if the interval is unbounded, i.e. if one bound of the interval is -&infin; or &infin;,
	 * respectively.
	 * 
	 * @return <code>true</code> or <code>false</code>.
	 */
	protected boolean isUnbounded() {
		if (mIsBottom) {
			return false;
		}
		return mLower.isInfinity() || mUpper.isInfinity();
	}

	/**
	 * Computes the result of the multiplication of two {@link IntervalDomainValue}s following the scheme:
	 * 
	 * <p>
	 * <ul>
	 * <li>[a, b] * [c, d] = [min(a*c, a*d, b*c, b*d), max(a*c, a*d, b*c, b*d)]</li>
	 * </ul>
	 * </p>
	 * 
	 * @param firstResult
	 *            The first interval.
	 * @param secondResult
	 *            The second interval.
	 * @return A new interval representing the result of <code>firstResult</code> * <code>secondRestult</code>.
	 */
	protected IntervalDomainValue multiply(IntervalDomainValue other) {
		assert other != null;

		if (isBottom() || other.isBottom()) {
			return new IntervalDomainValue(true);
		}

		IntervalValue lowerBound = computeMinMult(other);
		IntervalValue upperBound = computeMaxMult(other);

		return new IntervalDomainValue(lowerBound, upperBound);
	}

	/**
	 * Sets the current interval to &bot;.
	 */
	protected void setToBottom() {
		mLower = null;
		mUpper = null;

		mIsBottom = true;
	}

	/**
	 * Computes the subtraction of two {@link IntervalDomainValue}s following the scheme:
	 * <p>
	 * <ul>
	 * <li>[a, b] - [c, d] = [a - d, b - c]</li>
	 * </ul>
	 * </p>
	 * 
	 * @param firstResult
	 *            The first interval.
	 * @param secondResult
	 *            The second interval.
	 * @return A new interval representing the result of <code>firstResult</code> - <code>secondResult</code>.
	 */
	protected IntervalDomainValue subtract(IntervalDomainValue other) {
		assert other != null;

		if (isBottom() || other.isBottom()) {
			return new IntervalDomainValue(true);
		}

		if (isInfinity() || other.isInfinity()) {
			return new IntervalDomainValue();
		}

		IntervalValue lowerBound = new IntervalValue();
		IntervalValue upperBound = new IntervalValue();

		// Compute lower bound
		if (getLower().isInfinity() || other.getUpper().isInfinity()) {
			lowerBound.setToInfinity();
		} else {
			lowerBound.setValue(getLower().getValue().subtract(other.getUpper().getValue()));
		}

		// Compute upper bound
		if (getUpper().isInfinity() || other.getLower().isInfinity()) {
			upperBound.setToInfinity();
		} else {
			upperBound.setValue(getUpper().getValue().subtract(other.getLower().getValue()));
		}

		return new IntervalDomainValue(lowerBound, upperBound);
	}

	/**
	 * Computes the modulus operation of two {@link IntervalDomainValue}s following the scheme:
	 * <p>
	 * <ul>
	 * <li>[a, b] % [c, d] = [0, min(max(|a|, |b|), max(|c|, |d|) - 1)]</li>
	 * </ul>
	 * </p>
	 * 
	 * @param other
	 *            The other value to compute the modulus for.
	 * @return A new {@link IntervalDomainValue} which corresponds to the application of the modulus operator.
	 */
	protected IntervalDomainValue modulus(IntervalDomainValue other) {

		assert other != null;

		if (isBottom() || other.isBottom()) {
			return new IntervalDomainValue(true);
		}

		// If both intervals are infinite, the minimum is also infinite.
		if (isInfinity() && other.isInfinity()) {
			return new IntervalDomainValue(new IntervalValue(new BigDecimal(0)), new IntervalValue());
		}

		// If we are dealing with point intervals, the modulo computation is easy.
		if (!isInfinity() && !other.isInfinity()) {
			if (mLower.equals(mUpper) && other.mLower.equals(other.mUpper)) {
				if (other.mLower.getValue().signum() == 0) {
					return new IntervalDomainValue(true);
				}
			}

			BigDecimal remainder = mLower.getValue().remainder(other.mLower.getValue());
			if (remainder.signum() >= 0) {
				remainder = other.mLower.getValue().abs().add(remainder);
			}

			return new IntervalDomainValue(new IntervalValue(remainder), new IntervalValue(remainder));
		}

		// Compute max(|a|, |b|)
		IntervalValue maxAB;
		if (isInfinity()) {
			maxAB = new IntervalValue();
		} else {
			if (mLower.getValue().abs().compareTo(mUpper.getValue().abs()) > 0) {
				maxAB = new IntervalValue(mLower.getValue().abs());
			} else {
				maxAB = new IntervalValue(mUpper.getValue().abs());
			}
		}

		// Division by zero: return [0, \infty].
		if (other.containsZero()) {
			return new IntervalDomainValue(new IntervalValue(new BigDecimal(0)), new IntervalValue());
		}

		// Compute max(|c|, |d|)
		IntervalValue maxCD;
		if (other.isInfinity()) {
			maxCD = new IntervalValue();
		} else {
			if (other.mLower.getValue().abs().compareTo(other.mUpper.getValue().abs()) > 0) {
				maxCD = new IntervalValue(other.mLower.getValue().abs());
			} else {
				maxCD = new IntervalValue(other.mUpper.getValue().abs());
			}
		}

		if (maxAB.compareTo(maxCD) < 0) {
			return new IntervalDomainValue(new IntervalValue(new BigDecimal(0)), new IntervalValue(maxAB));
		} else {
			return new IntervalDomainValue(new IntervalValue(new BigDecimal(0)), new IntervalValue(maxCD));
		}
	}

	/**
	 * Negates the given interval.
	 * 
	 * @param interval
	 *            The interval to negate.
	 * @return A new interval which corresponds to the negated input interval.
	 */
	protected IntervalDomainValue negate() {

		if (isBottom()) {
			return new IntervalDomainValue(true);
		}

		if (getLower().isInfinity() && getUpper().isInfinity()) {
			return new IntervalDomainValue();
		}

		if (getLower().isInfinity()) {
			return new IntervalDomainValue(new IntervalValue(getUpper().getValue().negate()), new IntervalValue());
		}

		if (getUpper().isInfinity()) {
			return new IntervalDomainValue(new IntervalValue(), new IntervalValue(getLower().getValue().negate()));
		}

		return new IntervalDomainValue(new IntervalValue(getUpper().getValue().negate()),
		        new IntervalValue(getLower().getValue().negate()));
	}

	protected Term getTerm(final Script script, final Sort sort, final Term var) {
		assert sort.isNumericSort();
		if (isBottom()) {
			return script.term("false");
		} else if (mLower.isInfinity() && mUpper.isInfinity()) {
			return script.term("true");
		} else if (mLower.isInfinity()) {
			final Term value = mUpper.getTerm(sort, script);
			return script.term("<=", var, value);
		} else if (mUpper.isInfinity()) {
			final Term value = mLower.getTerm(sort, script);
			return script.term(">=", var, value);
		} else {
			int cmp = mUpper.compareTo(mLower);
			if (cmp == 0) {
				// point-interval
				final Term value = mLower.getTerm(sort, script);
				return script.term("=", var, value);
			} else if (cmp < 0) {
				// upper less than lower, i.e. empty intervall
				return script.term("false");
			} else {
				// its a normal interval
				final Term upper = script.term("<=", var, mUpper.getTerm(sort, script));
				final Term lower = script.term(">=", var, mLower.getTerm(sort, script));
				return Util.and(script, lower, upper);
			}
		}
	}

	/**
	 * Computes the maximum value of the multiplication of two intervals:
	 * 
	 * <p>
	 * [a, b] and [c, d]: max(ac, ad, bc, bd).
	 * </p>
	 * 
	 * @param first
	 *            The first interval of the form [a, b].
	 * @param second
	 *            The second interval of the form [c, d].
	 * @return max(ac, ad, bc, bd).
	 */
	private IntervalValue computeMaxMult(IntervalDomainValue other) {

		IntervalValue returnValue = new IntervalValue();
		boolean valuePresent = false;

		// If both intervals are infinite, the maximum is \infty.
		if (isInfinity() && other.isInfinity()) {
			return new IntervalValue();
		}

		// Compute a*c
		if (getLower().isInfinity()) {

			// -\infty * -\infty = \infty
			if (other.getLower().isInfinity()) {
				return new IntervalValue();
			} else {
				// -\infty * val = \infty, if val < 0
				if (other.getLower().getValue().signum() < 0) {
					return new IntervalValue();
				}

				// -\infty * 0 = 0
				if (other.getLower().getValue().signum() == 0) {
					returnValue = updateIfLarger(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			}
		} else {
			if (other.getLower().isInfinity()) {
				// val * -\infty = \infty, if val > 0
				if (getLower().getValue().signum() < 0) {
					return new IntervalValue();
				}

				// 0 * -\infty = 0
				if (getLower().getValue().signum() == 0) {
					returnValue = updateIfLarger(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			} else {
				returnValue = updateIfLarger(returnValue, getLower().getValue().multiply(other.getLower().getValue()),
				        valuePresent);
				valuePresent = true;
			}
		}

		// Compute a*d
		if (getLower().isInfinity()) {
			if (!other.getUpper().isInfinity()) {
				// -\infty * val = \infty, if val < 0
				if (other.getUpper().getValue().signum() < 0) {
					return new IntervalValue();
				}

				// -\infty * 0 = 0
				if (other.getUpper().getValue().signum() == 0) {
					returnValue = updateIfLarger(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			}
		} else {

			if (other.getUpper().isInfinity()) {
				// val * \infty = \infty, if val > 0
				if (getLower().getValue().signum() > 0) {
					return new IntervalValue();
				}

				// 0 * anything = 0
				if (getLower().getValue().signum() == 0) {
					returnValue = updateIfLarger(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			} else {
				returnValue = updateIfLarger(returnValue, getLower().getValue().multiply(other.getUpper().getValue()),
				        valuePresent);
				valuePresent = true;
			}
		}

		// Compute b*c
		if (getUpper().isInfinity()) {
			if (!other.getLower().isInfinity()) {
				// \infty * val = \infty, if val > 0
				if (other.getLower().getValue().signum() > 0) {
					return new IntervalValue();
				}

				// \infty * 0 = 0
				if (other.getLower().getValue().signum() == 0) {
					returnValue = updateIfLarger(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			}
		} else {
			if (other.getLower().isInfinity()) {
				// val * -\infty = \infty, if val < 0
				if (getUpper().getValue().signum() < 0) {
					return new IntervalValue();
				}

				// 0 * anything = 0
				if (getUpper().getValue().signum() == 0) {
					returnValue = updateIfLarger(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			} else {
				returnValue = updateIfLarger(returnValue, getUpper().getValue().multiply(other.getLower().getValue()),
				        valuePresent);
				valuePresent = true;
			}
		}

		// Compute b*d
		if (getUpper().isInfinity()) {
			// \infty * \infty = \infty
			if (other.getUpper().isInfinity()) {
				return new IntervalValue();
			} else {
				// \infty * val = \infty, if val > 0
				if (other.getUpper().getValue().signum() > 0) {
					return new IntervalValue();
				}

				// \infty * 0 = 0
				if (other.getUpper().getValue().signum() == 0) {
					returnValue = updateIfLarger(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			}
		} else {
			if (other.getUpper().isInfinity()) {
				// val * \infty = \infty, if val > 0
				if (getUpper().getValue().signum() > 0) {
					return new IntervalValue();
				}

				// 0 * \infty = 0
				if (getUpper().getValue().signum() == 0) {
					returnValue = updateIfLarger(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			} else {
				returnValue = updateIfLarger(returnValue, getUpper().getValue().multiply(other.getUpper().getValue()),
				        valuePresent);
				valuePresent = true;
			}
		}

		assert valuePresent;
		return returnValue;
	}

	/**
	 * Computes the minimum value of the multiplication of two intervals:
	 * 
	 * <p>
	 * [a, b] and [c, d]: min(ac, ad, bc, bd).
	 * </p>
	 * 
	 * @param first
	 *            The first interval of the form [a, b].
	 * @param second
	 *            The second interval of the form [c, d].
	 * @return min(ac, ad, bc, bd).
	 */
	private IntervalValue computeMinMult(IntervalDomainValue other) {

		IntervalValue returnValue = new IntervalValue();
		boolean valuePresent = false;

		// If both intervals are infinite, the minimum is -\infty.
		if (isInfinity() && other.isInfinity()) {
			return new IntervalValue();
		}

		// Compute a*c
		if (getLower().isInfinity()) {
			if (!other.getLower().isInfinity()) {

				// -\infty * val = -\infty, if val > 0.
				if (other.getLower().getValue().signum() > 0) {
					return new IntervalValue();
				}

				// -\infty * val = 0, if val = 0.
				if (other.getLower().getValue().signum() == 0) {
					returnValue = updateIfSmaller(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			}
		} else {

			// 0 * anything = 0.
			if (getLower().getValue().signum() == 0) {
				returnValue = updateIfSmaller(returnValue, new BigDecimal(0), valuePresent);
				valuePresent = true;
			} else {
				if (other.getLower().isInfinity()) {

					// val * -\infty = -\infty, if val > 0
					if (getLower().getValue().signum() > 0) {
						return new IntervalValue();
					}
				} else {
					returnValue = updateIfSmaller(returnValue,
					        getLower().getValue().multiply(other.getLower().getValue()), valuePresent);
					valuePresent = true;
				}
			}
		}

		// Compute a*d
		if (getLower().isInfinity()) {

			// -\infty * \infty = -\infty
			if (other.getUpper().isInfinity()) {
				return new IntervalValue();
			}

			// -\infty * val = -\infty, if val > 0
			if (other.getUpper().getValue().signum() > 0) {
				return new IntervalValue();
			}

			// anything * 0 = 0.
			if (other.getUpper().getValue().signum() == 0) {
				returnValue = updateIfSmaller(returnValue, new BigDecimal(0), valuePresent);
				valuePresent = true;
			}
		} else {

			// 0 * anything = 0
			if (getLower().getValue().signum() == 0) {
				returnValue = updateIfSmaller(returnValue, new BigDecimal(0), valuePresent);
				valuePresent = true;
			} else {
				if (other.getUpper().isInfinity()) {

					// val * \infty = -\infty, if val < 0
					if (getLower().getValue().signum() < 0) {
						return new IntervalValue();
					}
				} else {
					returnValue = updateIfSmaller(returnValue,
					        getLower().getValue().multiply(other.getUpper().getValue()), valuePresent);
					valuePresent = true;
				}
			}
		}

		// Compute b*c
		if (getUpper().isInfinity()) {

			// \infty * -\infty = -\infty
			if (other.getLower().isInfinity()) {
				return new IntervalValue();
			}

			// \infty * val = -\infty, if val < 0
			if (other.getLower().getValue().signum() < 0) {
				return new IntervalValue();
			}

			// \infty * 0 = 0
			if (other.getLower().getValue().signum() == 0) {
				returnValue = updateIfSmaller(returnValue, new BigDecimal(0), valuePresent);
				valuePresent = true;
			}
		} else {
			if (other.getLower().isInfinity()) {
				// val * -\infty = -\infty, if val > 0
				if (getUpper().getValue().signum() > 0) {
					return new IntervalValue();
				}

				// 0 * anything = 0
				if (getUpper().getValue().signum() == 0) {
					returnValue = updateIfSmaller(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			} else {
				returnValue = updateIfSmaller(returnValue, getUpper().getValue().multiply(other.getLower().getValue()),
				        valuePresent);
				valuePresent = true;
			}
		}

		// Compute b * d
		if (getUpper().isInfinity()) {
			if (!other.getUpper().isInfinity()) {

				// \infty * val = -\infty, if val < 0
				if (other.getUpper().getValue().signum() < 0) {
					return new IntervalValue();
				}

				if (other.getUpper().getValue().signum() == 0) {
					returnValue = updateIfSmaller(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			}
		} else {
			if (other.getUpper().isInfinity()) {
				// val * \infty = -\infty, if val < 0
				if (getUpper().getValue().signum() < 0) {
					return new IntervalValue();
				}

				// 0 * \infty = 0
				if (getUpper().getValue().signum() == 0) {
					returnValue = updateIfSmaller(returnValue, new BigDecimal(0), valuePresent);
					valuePresent = true;
				}
			} else {
				returnValue = updateIfSmaller(returnValue, getUpper().getValue().multiply(other.getLower().getValue()),
				        valuePresent);
				valuePresent = true;
			}
		}

		assert valuePresent;
		return returnValue;
	}

	private IntervalValue updateIfLarger(IntervalValue oldValue, BigDecimal newValue, boolean valuePresent) {
		if (valuePresent) {
			if (oldValue.getValue().compareTo(newValue) <= 0) {
				return new IntervalValue(newValue);
			} else {
				return oldValue;
			}
		} else {
			return new IntervalValue(newValue);
		}
	}

	private IntervalValue updateIfSmaller(IntervalValue oldValue, BigDecimal newValue, boolean valuePresent) {
		if (valuePresent) {
			if (oldValue.getValue().compareTo(newValue) >= 0) {
				return new IntervalValue(newValue);
			} else {
				return oldValue;
			}
		} else {
			return new IntervalValue(newValue);
		}
	}
}