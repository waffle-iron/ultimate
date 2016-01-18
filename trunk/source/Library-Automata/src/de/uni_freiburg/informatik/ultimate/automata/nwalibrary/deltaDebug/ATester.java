/*
 * Copyright (C) 2015 Christian Schilling <schillic@informatik.uni-freiburg.de>
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
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.deltaDebug;

import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomaton;

/**
 * Executes the respective method which should be debugged and compares to the
 * designated error.
 * 
 * Usage:
 * Initially, the error which is to be expected is stored in order to be able to
 * compare to its concrete type during the search later on.
 * The {@link #execute(INestedWordAutomaton)} method
 * must be overwritten to run the designated method accordingly.
 * 
 * The architecture allows for very general testing features such as additional
 * pre- and post-processing, but comes with the price that this class must be
 * implemented for each method anew.
 * 
 * @author Christian Schilling <schillic@informatik.uni-freiburg.de>
 */
public abstract class ATester<LETTER, STATE> {
	final Throwable m_throwable;
	
	public ATester(final Throwable throwable) {
		this.m_throwable = throwable;
	}
	
	/**
	 * Tests whether an input still produces an error.
	 * 
	 * @param automaton input automaton
	 * @return true iff an error of the original error type (exact) occurred
	 */
	public boolean test(final INestedWordAutomaton<LETTER, STATE> automaton) {
		try {
			execute(automaton);
		} catch (final Throwable throwable) {
			return m_throwable.getClass().isInstance(throwable) &&
					throwable.getClass().isInstance(m_throwable);
		}
		return false;
	}
	
	/**
	 * Executes the method to be tested on the given automaton.
	 * 
	 * @param automaton input automaton
	 * @throws any type of throwable
	 */
	public abstract void execute(
		final INestedWordAutomaton<LETTER, STATE> automaton)
			throws Throwable;
	
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(this.getClass().getSimpleName());
		b.append(" with exception type '");
		b.append(m_throwable.getClass().getSimpleName());
		b.append("'");
		return b.toString();
	}
}