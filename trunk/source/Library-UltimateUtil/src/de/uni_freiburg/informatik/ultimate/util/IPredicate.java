/*
 * Copyright (C) 2009-2014 University of Freiburg
 *
 * This file is part of the ULTIMATE Utils Library.
 *
 * The ULTIMATE Utils Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE Utils Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Utils Library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Utils Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Utils Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.util;

import java.util.Set;

/**
 * Predicate that evaluates objects to true/false.
 * @author Matthias Heizmann
 *
 * @param <T> Type of the objects that can be evaluated
 */
public interface IPredicate<T> {
	
	public boolean evaluate(T element);
	
	
	/**
	 * Implementation of IPredicate that evaluates all an object to true iff
	 * it is contained in a given set.
	 * @author Matthias Heizmann
	 *
	 */
	public static class SetBasedPredicate<T> implements IPredicate<T> {
		private final Set<T> m_Set;
		
		public SetBasedPredicate(Set<T> set) {
			m_Set = set;
		}
		
		/**
		 * @return true iff elements is contained in this objects set.
		 */
		@Override
		public boolean evaluate(T element) {
			return m_Set.contains(element);
		}
		
	}

}