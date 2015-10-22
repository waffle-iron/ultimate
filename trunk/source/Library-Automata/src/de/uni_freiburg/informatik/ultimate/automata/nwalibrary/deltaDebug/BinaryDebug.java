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

import java.util.ArrayDeque;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomaton;

/**
 * Reduces a list of objects in a binary search manner until a local minimum is
 * found.
 * 
 * Note that the local minimum is only according to the current shrinker, i.e.,
 * the respective shrinker cannot be applied to a subinterval of objects anymore
 * while still producing the error.
 * However, removing, say, objects 1 and 3 might still work.
 * 
 * @author Christian Schilling <schillic@informatik.uni-freiburg.de>
 */
public class BinaryDebug<T, LETTER, STATE> {
	/**
	 * Left and right bound for the list of objects
	 */
	private class SublistBounds {
		final int m_left, m_right;
		final boolean m_isLhs;

		SublistBounds(final int left, final int right,
				final boolean isLhs) {
			this.m_left = left;
			this.m_right = right;
			this.m_isLhs = isLhs;
		}
		
		@Override
		public String toString() {
			final StringBuilder b = new StringBuilder();
			b.append("(");
			b.append(m_left);
			b.append(", ");
			b.append(m_right);
			b.append(")");
			if (m_isLhs) {
				b.append("*");
			}
			return b.toString();
		}
	}
	
	final ATester<LETTER, STATE> m_tester;
	final AShrinker<T, LETTER, STATE> m_shrinker;
	final ArrayDeque<SublistBounds> m_stack;
	
	/**
	 * @param tester tester
	 * @param shrinker shrinker
	 */
	public BinaryDebug(final ATester<LETTER, STATE> tester,
			final AShrinker<T, LETTER, STATE> shrinker) {
		this.m_tester = tester;
		this.m_shrinker = shrinker;
		this.m_stack = new ArrayDeque<SublistBounds>();
	}
	
	/**
	 * Splits a list into two sublists of equal size.
	 * 
	 * @param bounds bounds
	 */
	private void split(final SublistBounds bounds) {
		final int left = bounds.m_left;
		final int right = bounds.m_right;
		
		// do not split intervals of size <= 1 (useless and would run forever)
		if (right - left <= 1) {
			return;
		}
		
		final int mid = (left + right) / 2;
		final boolean isLhs;
		if (mid < right) {
			m_stack.push(new SublistBounds(mid, right, false));
			isLhs = true;
		} else {
			isLhs = false;
		}
		if (left < mid) {
			m_stack.push(new SublistBounds(left, mid, isLhs));
		}
	}
	
	/**
	 * Runs the binary search for the current shrinker.
	 * 
	 * @return true iff the sublist could be shrunk 
	 */
	public boolean run() {
		boolean result = false;
		List<T> list = m_shrinker.extractList();
		if (list.isEmpty()) {
			return result;
		}
		m_stack.add(new SublistBounds(0, list.size(), false));
		while (! m_stack.isEmpty()) {
			final SublistBounds sb = m_stack.poll();
			final List<T> sublist =
					list.subList(sb.m_left, sb.m_right);
			if (sublist.isEmpty()) {
				continue;
			}
			
			// initialize test for the sublist
			final INestedWordAutomaton<LETTER, STATE> automaton =
					m_shrinker.createAutomaton(sublist);
			
			// run test
			final boolean isTestSuccessful = m_tester.test(automaton);
			
			if (isTestSuccessful) {
				// error reproduced
				m_shrinker.error(automaton);
				result = true;
				if (sb.m_isLhs) {
					/*
					 * When the success happened on the first half, we can
					 * remove the corresponding second half and directly work on
					 * its children. This is because by removing the second half
					 * we had removed the whole sublist and this was by
					 * construction already unsuccessful in a previous test.
					 */
					split(m_stack.poll());
				}
			} else {
				// error not reproduced => split list into two parts
				m_shrinker.noError(automaton);
				split(sb);
			}
		}
		return result;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}