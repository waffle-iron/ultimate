/**
 * 
 */
package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationMk2.valuedomain;

/**
 * Widening operators of an absract domain system can be used to ensure
 * termination by over-approximating fixed points.
 * 
 * @author Jan H�ttig
 *
 */
public interface IValueWideningOperator<T> extends IValueOperator<T> {

	/**
	 * Merges two given values while applying widening. The old and new value
	 * may not be treated equally and are thus not interchangable.
	 * 
	 * @param oldValue
	 *            The previous abstract value
	 * @param newValue
	 *            The new abstract value
	 * @return A merged value which is greater than both given value wrt the
	 *         complete lattice of abstract values
	 */
	public IAbstractValue<T> apply(IAbstractValue<?> oldValue,
			IAbstractValue<?> newValue);

	/**
	 * @return A copy of this widening operator
	 */
	public IValueWideningOperator<T> copy();
}