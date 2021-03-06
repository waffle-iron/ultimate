package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.congruence;

import java.math.BigInteger;

import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.BooleanValue;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.INonrelationalValue;

/**
 * Representation of a congruence value in the congruence domain
 * 
 * @author Frank Schüssele (schuessf@informatik.uni-freiburg.de)
 *
 */

public final class CongruenceDomainValue
        implements Comparable<CongruenceDomainValue>, INonrelationalValue<CongruenceDomainValue> {

	private BigInteger mValue;
	private boolean mIsBottom;
	private boolean mIsConstant;
	private boolean mNonZero;

	/**
	 * Creates a bottom value (shouldn't be called, just necessary for the static create-methods)
	 */
	private CongruenceDomainValue() {
		mValue = null;
		mIsBottom = true;
		mIsConstant = false;
		mNonZero = false;
	}

	protected static CongruenceDomainValue createTop() {
		return createNonConstant(BigInteger.ONE);
	}

	protected static CongruenceDomainValue createBottom() {
		return new CongruenceDomainValue();
	}

	protected static CongruenceDomainValue createNonConstant(final BigInteger value, final boolean nonZero) {
		if (value.signum() == 0) {
			return nonZero ? createBottom() : createConstant(BigInteger.ZERO);
		}
		final CongruenceDomainValue res = new CongruenceDomainValue();
		res.mValue = value.abs();
		res.mNonZero = nonZero;
		res.mIsBottom = false;
		return res;
	}

	protected static CongruenceDomainValue createNonConstant(final BigInteger value) {
		return createNonConstant(value, false);
	}

	protected static CongruenceDomainValue createConstant(final BigInteger value) {
		final CongruenceDomainValue res = new CongruenceDomainValue();
		res.mValue = value;
		res.mNonZero = value.signum() != 0;
		res.mIsBottom = false;
		res.mIsConstant = true;
		return res;
	}

	@Override
	public boolean isBottom() {
		return mIsBottom;
	}

	protected BigInteger value() {
		return mValue;
	}

	protected boolean isConstant() {
		return mIsConstant;
	}

	@Override
	public int compareTo(final CongruenceDomainValue other) {
		throw new UnsupportedOperationException(
		        "The compareTo operation is not defined on congruence clases and can therefore not be used.");
	}

	@Override
	public CongruenceDomainValue merge(final CongruenceDomainValue other) {
		if (other == null) {
			return createBottom();
		}
		if (mIsBottom) {
			return other.copy();
		}
		if (other.mIsBottom) {
			return copy();
		}
		// If both are constant and have the same value, the result is also constant (otherwise not)
		if (mValue.equals(other.mValue) && mIsConstant && other.mIsConstant) {
			return copy();
		}
		return createNonConstant(mValue.gcd(other.mValue), mNonZero && other.mNonZero);
	}

	@Override
	public CongruenceDomainValue intersect(final CongruenceDomainValue other) {
		if (other == null || mIsBottom || other.mIsBottom) {
			return createBottom();
		}
		// If both are constant, return the value if it's the same, bottom otherwise
		if (mIsConstant && other.mIsConstant) {
			if (mValue.equals(other.mValue)) {
				return copy();
			} else {
				return createBottom();
			}
		}
		// If one is constant, return the value if it's inside the other, bottom otherwise
		if (mIsConstant) {
			if (other.mNonZero && mValue.signum() == 0) {
				return createBottom();
			}
			if (mValue.mod(other.mValue.abs()).signum() == 0) {
				return copy();
			} else {
				return createBottom();
			}
		}
		if (other.mIsConstant) {
			if (mNonZero && other.mValue.signum() == 0) {
				return createBottom();
			}
			if (other.mValue.mod(mValue.abs()).signum() == 0) {
				return other.copy();
			} else {
				return createBottom();
			}
		}
		// Return the LCM as new value
		// LCM(a, b) = abs(a * b) / GCD(a, b)
		return createNonConstant(mValue.multiply(other.mValue).divide(mValue.gcd(other.mValue)),
		        mNonZero || other.mNonZero);
	}

	@Override
	public CongruenceDomainValue add(final CongruenceDomainValue other) {
		if (other == null || mIsBottom || other.mIsBottom) {
			return createBottom();
		}
		if (mValue.signum() == 0) {
			return other.copy();
		}
		if (other.mValue.signum() == 0) {
			return copy();
		}
		if (mIsConstant && other.mIsConstant) {
			return createConstant(mValue.add(other.mValue));
		}
		boolean nonZero = false;
		if (mIsConstant) {
			nonZero = mValue.mod(other.mValue).signum() != 0;
		}
		if (other.mIsConstant) {
			nonZero = other.mValue.mod(mValue).signum() != 0;
		}
		return createNonConstant(mValue.gcd(other.mValue), nonZero);
	}

	@Override
	public CongruenceDomainValue subtract(final CongruenceDomainValue other) {
		if (other == null || mIsBottom || other.mIsBottom) {
			return createBottom();
		}
		if (mValue.signum() == 0) {
			return other.negate();
		}
		if (other.mValue.signum() == 0) {
			return copy();
		}
		if (mIsConstant && other.mIsConstant) {
			return createConstant(mValue.subtract(other.mValue));
		}
		boolean nonZero = false;
		if (mIsConstant) {
			nonZero = mValue.mod(other.mValue).signum() != 0;
		}
		if (other.mIsConstant) {
			nonZero = other.mValue.mod(mValue).signum() != 0;
		}
		return createNonConstant(mValue.gcd(other.mValue), nonZero);
	}

	protected CongruenceDomainValue mod(final CongruenceDomainValue other) {
		if (other == null || mIsBottom || other.mIsBottom) {
			return createBottom();
		}
		if (!other.mNonZero) {
			return createTop();
		}
		// If both are constant, simply calculate the result
		if (mIsConstant && other.mIsConstant) {
			return createConstant(mValue.mod(other.mValue.abs()));
		}
		// a % bZ = a if a >= 0 and a < b
		if (mIsConstant && mValue.signum() >= 0 && mValue.compareTo(other.mValue) < 0) {
			return createConstant(mValue);
		}
		// aZ % b = 0 if a % b = 0
		if (other.mIsConstant && mValue.mod(other.mValue.abs()).signum() == 0) {
			return createConstant(BigInteger.ZERO);
		}
		return createNonConstant(mValue.gcd(other.mValue), mIsConstant && mValue.mod(other.mValue).signum() != 0);
	}

	@Override
	public CongruenceDomainValue multiply(final CongruenceDomainValue other) {
		if (other == null || mIsBottom || other.mIsBottom) {
			return createBottom();
		}
		if (mIsConstant && other.mIsConstant) {
			return createConstant(mValue.multiply(other.mValue));
		}
		return createNonConstant(mValue.multiply(other.mValue), mNonZero && other.mNonZero);
	}

	@Override
	public CongruenceDomainValue divide(final CongruenceDomainValue other) {
		if (other == null || mIsBottom || other.mIsBottom) {
			return createBottom();
		}
		if (!other.mNonZero) {
			return createTop();
		}
		if (other.mIsConstant) {
			// If "real" result of the division is an integer, just calculate the result
			if (mValue.mod(other.mValue.abs()).signum() == 0) {
				if (mIsConstant) {
					return createConstant(mValue.divide(other.mValue));
				}
				return createNonConstant(mValue.divide(other.mValue), mNonZero);
			}
			if (mIsConstant) {
				BigInteger val = mValue.divide(other.mValue);
				// If a < 0, a / b doesn't give the expected result (euclidian divsion)
				if (mValue.signum() < 0) {
					if (other.mValue.signum() > 0) {
						val = val.subtract(BigInteger.ONE);
					} else {
						val = val.add(BigInteger.ONE);
					}
				}
				return createConstant(val);
			}
		}
		// If 0 < a < b: a / bZ = 0
		if (mIsConstant && mValue.signum() > 0 && mValue.compareTo(other.mValue) < 0) {
			return createConstant(BigInteger.ZERO);
		}
		// aZ \ {0} / b = 1Z \ {0} if a >= |b| (division can't be zero then)
		if (other.mIsConstant && mNonZero && mValue.compareTo(other.mValue.abs()) >= 0) {
			return createNonConstant(BigInteger.ONE, true);
		}
		return createTop();
	}

	@Override
	public CongruenceDomainValue negate() {
		if (mIsBottom) {
			return createBottom();
		}
		if (mIsConstant) {
			return createConstant(mValue.negate());
		}
		return copy();
	}

	@Override
	public String toString() {
		if (mIsBottom) {
			return "{}";
		}
		if (mIsConstant) {
			return mValue.toString();
		}
		if (mNonZero) {
			return mValue.toString() + "Z \\ {0}";
		} else {
			return mValue.toString() + "Z";
		}

	}

	@Override
	public Term getTerm(final Script script, final Sort sort, final Term var) {
		assert sort.isNumericSort();
		if (mIsBottom) {
			return script.term("false");
		}
		if (mIsConstant) {
			return script.term("=", var, script.numeral(mValue));
		}
		final Term nonZeroTerm = script.term("not", script.term("=", var, script.numeral(BigInteger.ZERO)));
		if (mValue.equals(BigInteger.ONE)) {
			if (mNonZero) {
				return nonZeroTerm;
			}
			return script.term("true");
		}
		final Term modTerm = script.term("=", script.term("mod", var, script.numeral(mValue)),
		        script.numeral(BigInteger.ZERO));
		if (mNonZero) {
			return script.term("and", modTerm, nonZeroTerm);
		}
		return modTerm;

	}

	/**
	 * Returns <code>true</code> if and only if <code>this</code> is equal to <code>other</code>.
	 */
	@Override
	public boolean isEqualTo(final CongruenceDomainValue other) {
		if (other == null) {
			return false;
		}
		if (mIsBottom && other.mIsBottom) {
			return true;
		}
		return mValue.equals(other.mValue) && mIsConstant == other.mIsConstant && mNonZero == other.mNonZero;
	}

	/**
	 * Return a copy of the value
	 */
	@Override
	public CongruenceDomainValue copy() {
		if (mIsBottom) {
			return createBottom();
		}
		if (mIsConstant) {
			return createConstant(mValue);
		}
		return createNonConstant(mValue, mNonZero);
	}

	/**
	 * Return the the new value for x for a "x % this == rest" - expression (soft-merge)
	 */
	protected CongruenceDomainValue modEquals(final CongruenceDomainValue rest) {
		if (mIsBottom || rest == null || rest.mIsBottom) {
			return createBottom();
		}
		if (!mNonZero) {
			return createTop();
		}
		// If the rest is < 0, return bottom
		if (rest.mValue.signum() < 0) {
			return createBottom();
		}
		// If the rest is >= |this|, return bottom if rest is constant, otherwise the non-constant value of this
		// (because rest has to be 0 then, since all other values are too big)
		if (mIsConstant && rest.mValue.compareTo(mValue.abs()) >= 0) {
			if (rest.mNonZero) {
				return createBottom();
			} else {
				return createNonConstant(mValue);
			}
		}
		// Otherwise return the GCD (=non-constant merge)
		return createNonConstant(mValue.gcd(rest.mValue), rest.mNonZero);
	}

	/**
	 * Returns <code>true</code> if this is contained in other.
	 * 
	 * @param other
	 *            The other value to check against.
	 * @return <code>true</code> if and only if the value of this is contained in the value of other, <code>false</code>
	 */
	@Override
	public boolean isContainedIn(final CongruenceDomainValue other) {
		if (mIsBottom) {
			return true;
		}
		if (other.mIsBottom) {
			return false;
		}
		if (other.mIsConstant) {
			return mIsConstant && mValue.equals(other.mValue);
		}
		if (!mNonZero && other.mNonZero) {
			return false;
		}
		return mValue.mod(other.mValue).signum() == 0;
	}

	protected CongruenceDomainValue getNonZeroValue() {
		if (mIsBottom || mValue.signum() == 0) {
			return createBottom();
		}
		if (mIsConstant) {
			return copy();
		}
		return createNonConstant(mValue, true);
	}

	@Override
	public CongruenceDomainValue integerDivide(CongruenceDomainValue other) {
		return divide(other);
	}

	@Override
	public CongruenceDomainValue modulo(CongruenceDomainValue other, boolean isInteger) {
		return mod(other);
	}

	@Override
	public CongruenceDomainValue greaterThan(CongruenceDomainValue other) {
		return createTop();
	}

	@Override
	public BooleanValue isGreaterThan(CongruenceDomainValue other) {
		if (isConstant() && other.isConstant()) {
			return new BooleanValue(value().compareTo(other.value()) > 0);
		}
		return new BooleanValue();
	}

	@Override
	public CongruenceDomainValue greaterOrEqual(CongruenceDomainValue other) {
		return createTop();
	}

	@Override
	public BooleanValue isGreaterOrEqual(CongruenceDomainValue other) {
		if (isConstant() && other.isConstant()) {
			return new BooleanValue(value().compareTo(other.value()) >= 0);
		}
		return new BooleanValue();
	}

	@Override
	public CongruenceDomainValue lessThan(CongruenceDomainValue other) {
		return createTop();
	}

	@Override
	public BooleanValue isLessThan(CongruenceDomainValue other) {
		if (isConstant() && other.isConstant()) {
			return new BooleanValue(value().compareTo(other.value()) < 0);
		}
		return new BooleanValue();
	}

	@Override
	public CongruenceDomainValue lessOrEqual(CongruenceDomainValue other) {
		return createTop();
	}

	@Override
	public BooleanValue isLessOrEqual(CongruenceDomainValue other) {
		if (isConstant() && other.isConstant()) {
			return new BooleanValue(value().compareTo(other.value()) <= 0);
		}
		return new BooleanValue();
	}

	@Override
	public CongruenceDomainValue inverseModulo(CongruenceDomainValue referenceValue, CongruenceDomainValue oldValue,
	        boolean isLeft) {
		// If mod is at one side of an equality, the left side of the mod expression
		// changes according to the other side of the equality
		if (isLeft) {
			return oldValue.intersect(modEquals(referenceValue));
		} else {
			return oldValue;
		}
	}

	@Override
	public CongruenceDomainValue inverseEquality(CongruenceDomainValue oldValue, CongruenceDomainValue referenceValue) {
		return oldValue.intersect(this);
	}

	@Override
	public CongruenceDomainValue inverseLessOrEqual(CongruenceDomainValue oldValue, boolean isLeft) {
		if (isConstant() && value().signum() < 0) {
			return oldValue.getNonZeroValue();
		} else {
			return oldValue;
		}
	}

	@Override
	public CongruenceDomainValue inverseLessThan(CongruenceDomainValue oldValue, boolean isLeft) {
		if (isConstant() && value().signum() <= 0) {
			return oldValue.getNonZeroValue();
		} else {
			return oldValue;
		}
	}

	@Override
	public CongruenceDomainValue inverseGreaterOrEqual(CongruenceDomainValue oldValue, boolean isLeft) {
		if (isConstant() && value().signum() > 0) {
			return oldValue.getNonZeroValue();
		} else {
			return oldValue;
		}
	}

	@Override
	public CongruenceDomainValue inverseGreaterThan(CongruenceDomainValue oldValue, boolean isLeft) {
		if (isConstant() && value().signum() >= 0) {
			return oldValue.getNonZeroValue();
		} else {
			return oldValue;
		}
	}

	@Override
	public CongruenceDomainValue inverseNotEqual(CongruenceDomainValue oldValue, CongruenceDomainValue referenceValue) {
		if (isConstant() && value().signum() == 0) {
			return oldValue.getNonZeroValue();
		} else {
			return oldValue;
		}
	}

	@Override
	public boolean canHandleReals() {
		return true;
	}

	@Override
	public boolean canHandleModulo() {
		return true;
	}

	@Override
	public BooleanValue compareEquality(CongruenceDomainValue firstOther, CongruenceDomainValue secondOther) {
		if (firstOther.isConstant() && secondOther.isConstant()) {
			return new BooleanValue(firstOther.value().equals(secondOther.value()));
		}
		return new BooleanValue();
	}

	@Override
	public BooleanValue compareInequality(CongruenceDomainValue firstOther, CongruenceDomainValue secondOther) {
		if (firstOther.isConstant() && secondOther.isConstant()) {
			return new BooleanValue(!firstOther.value().equals(secondOther.value()));
		}
		return new BooleanValue();
	}
}
