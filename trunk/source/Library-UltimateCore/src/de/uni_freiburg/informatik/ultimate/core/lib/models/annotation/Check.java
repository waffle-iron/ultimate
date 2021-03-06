/*
 * Copyright (C) 2015 Christian Schilling (schillic@informatik.uni-freiburg.de)
 * Copyright (C) 2013-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Markus Lindenmann (lindenmm@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Oleksii Saukh (saukho@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Stefan Wissert
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Core.
 * 
 * The ULTIMATE Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Core. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Core, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Core grant you additional permission 
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.core.lib.models.annotation;

import de.uni_freiburg.informatik.ultimate.core.model.models.IElement;

/**
 * Specification that should be checked at position
 * 
 * @author Markus Lindenmann
 * @author Stefan Wissert
 * @author Oleksii Saukh
 * @author Matthias Heizmann
 */
public class Check extends AbstractAnnotations {
	private static final long serialVersionUID = -3753413284642976683L;

	public static String getIdentifier() {
		return Check.class.getName();
	}

	public enum Spec {
		/**
		 * Array Index out of bounds error.
		 */
		ARRAY_INDEX,
		/**
		 * Pre condition violated.
		 */
		PRE_CONDITION,
		/**
		 * Post condition violated.
		 */
		POST_CONDITION,
		/**
		 * Invariant violated.
		 */
		INVARIANT,
		/**
		 * Assert statement violated.
		 */
		ASSERT,
		/**
		 * Devision by zero error.
		 */
		DIVISION_BY_ZERO,
		/**
		 * Integer overflow error.
		 */
		INTEGER_OVERFLOW,
		/**
		 * Tried to access unallocated memory.
		 */
		MEMORY_DEREFERENCE,
		/**
		 * Memory leak detected. I.e. missing free!
		 */
		MEMORY_LEAK,
		/**
		 * Free of unallocated pointer.
		 */
		MEMORY_FREE,
		/**
		 * Free of unallocated pointer.
		 */
		MALLOC_NONNEGATIVE,
		/**
		 * Pointer arithmetic that is not allowed by C. E.g. - computing the
		 * difference of two pointers that point to completely different arrays
		 * - comparing pointers that point to completely different arrays
		 */
		ILLEGAL_POINTER_ARITHMETIC,
		/**
		 * Error function reachable.
		 */
		ERROR_Function,
		/**
		 * Not further specified or unknown.
		 */
		UNKNOWN, 
		/**
		 * An LTL property
		 */
		LTL,
		/**
		 * Invariant of a correctness witness
		 */
		WITNESS_INVARIANT,
		/**
		 * Unsigned int overflow
		 */
		UINT_OVERFLOW,
		// add missing failure types...

	}

	private final Spec mSpec;

	/**
	 * The published attributes. Update this and getFieldValue() if you add new
	 * attributes.
	 */
	private final static String[] s_AttribFields = { "Check" };

	public Spec getSpec() {
		return mSpec;
	}

	public Check(Check.Spec spec) {
		mSpec = spec;
	}

	public String getPositiveMessage() {
		switch (mSpec) {
		case ARRAY_INDEX:
			return "array index is always in bounds";
		case PRE_CONDITION:
			return "procedure precondition always holds";
		case POST_CONDITION:
			return "procedure postcondition always holds";
		case INVARIANT:
			return "loop invariant is valid";
		case ASSERT:
			return "assertion always holds";
		case DIVISION_BY_ZERO:
			return "division by zero can never occur";
		case INTEGER_OVERFLOW:
			return "integer overflow can never occur";
		case MEMORY_DEREFERENCE:
			return "pointer dereference always succeeds";
		case MEMORY_LEAK:
			return "all allocated memory was freed";
		case MEMORY_FREE:
			return "free always succeeds";
		case MALLOC_NONNEGATIVE:
			return "input of malloc is always non-negative";
		case ILLEGAL_POINTER_ARITHMETIC:
			return "pointer arithmetic is always legal";
		case ERROR_Function:
			return "call of __VERIFIER_error() unreachable";
		case WITNESS_INVARIANT:
			return "invariant of correctness witness holds";
		case UNKNOWN:
			return "unknown kind of specification holds";
		case UINT_OVERFLOW:
			return "there are no unsigned integer over- or underflows";
		default:
			throw new AssertionError();
		}
	}

	public String getNegativeMessage() {
		switch (mSpec) {
		case ARRAY_INDEX:
			return "array index can be out of bounds";
		case PRE_CONDITION:
			return "procedure precondition can be violated";
		case POST_CONDITION:
			return "procedure postcondition can be violated";
		case INVARIANT:
			return "loop invariant can be violated";
		case ASSERT:
			return "assertion can be violated";
		case DIVISION_BY_ZERO:
			return "possible division by zero";
		case INTEGER_OVERFLOW:
			return "integer overflow possible";
		case MEMORY_DEREFERENCE:
			return "pointer dereference may fail";
		case MEMORY_LEAK:
			return "not all allocated memory was freed";
		case MEMORY_FREE:
			return "free of unallocated memory possible";
		case MALLOC_NONNEGATIVE:
			return "input of malloc can be negative";
		case ILLEGAL_POINTER_ARITHMETIC:
			return "comparison of incompatible pointers";
		case ERROR_Function:
			return "a call of __VERIFIER_error() is reachable";
		case WITNESS_INVARIANT:
			return "invariant of correctness witness can be violated";
		case UNKNOWN:
			return "unknown kind of specification may be violated";
		case UINT_OVERFLOW:
			return "an unsigned integer over- or underflow may occur";
		default:
			throw new AssertionError();
		}
	}

	@Override
	protected String[] getFieldNames() {
		return s_AttribFields;
	}

	@Override
	protected Object getFieldValue(String field) {
		if (field.equals("Check")) {
			return mSpec.toString();
		} else {
			throw new UnsupportedOperationException("Unknown field " + field);
		}
	}

	/**
	 * Adds this Check object to the annotations of a IElement.
	 * 
	 * @param node
	 *            the element
	 */
	public final void addToNodeAnnot(IElement node) {
		node.getPayload().getAnnotations().put(getIdentifier(), this);
	}
}
