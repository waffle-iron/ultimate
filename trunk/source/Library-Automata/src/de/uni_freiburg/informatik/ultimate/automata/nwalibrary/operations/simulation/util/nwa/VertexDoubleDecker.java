/*
 * Copyright (C) 2015-2016 Daniel Tischner
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
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.util.nwa;

/**
 * Container that holds a vertex up and down state. Game graph vertices have a
 * vertex up and down state, this container is used to represent its those.
 * 
 * @author Daniel Tischner
 *
 * @param <STATE>
 *            State class of nwa automaton
 */
public final class VertexDoubleDecker<STATE> {
	/**
	 * Vertex down state of this double decker.
	 */
	private final VertexDownState<STATE> m_VertexDownState;
	/**
	 * Vertex up state of this double decker.
	 */
	private final VertexUpState<STATE> m_VertexUpState;

	/**
	 * Creates a new vertex double decker with given up and down states.
	 * 
	 * @param leftUpState
	 *            Left up state for this element
	 * @param leftDownState
	 *            Left down state for this element
	 * @param rightUpState
	 *            Right up state for this element
	 * @param rightDownState
	 *            Right down state for this element
	 */
	public VertexDoubleDecker(final STATE leftUpState, final STATE leftDownState, final STATE rightUpState,
			final STATE rightDownState) {
		this(new VertexUpState<STATE>(leftUpState, rightUpState),
				new VertexDownState<STATE>(leftDownState, rightDownState));
	}

	/**
	 * Creates a new vertex double decker with given vertex up and down states.
	 * 
	 * @param vertexUpState
	 *            Vertex up state for this element
	 * @param vertexDownState
	 *            Vertex down state for this element
	 */
	public VertexDoubleDecker(final VertexUpState<STATE> vertexUpState, final VertexDownState<STATE> vertexDownState) {
		m_VertexUpState = vertexUpState;
		m_VertexDownState = vertexDownState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof VertexDoubleDecker)) {
			return false;
		}
		VertexDoubleDecker<?> other = (VertexDoubleDecker<?>) obj;
		if (m_VertexDownState == null) {
			if (other.m_VertexDownState != null) {
				return false;
			}
		} else if (!m_VertexDownState.equals(other.m_VertexDownState)) {
			return false;
		}
		if (m_VertexUpState == null) {
			if (other.m_VertexUpState != null) {
				return false;
			}
		} else if (!m_VertexUpState.equals(other.m_VertexUpState)) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the vertex down state.
	 * 
	 * @return The vertex down state.
	 */
	public VertexDownState<STATE> getVertexDownState() {
		return m_VertexDownState;
	}

	/**
	 * Gets the vertex up state.
	 * 
	 * @return The vertex up state.
	 */
	public VertexUpState<STATE> getVertexUpState() {
		return m_VertexUpState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_VertexDownState == null) ? 0 : m_VertexDownState.hashCode());
		result = prime * result + ((m_VertexUpState == null) ? 0 : m_VertexUpState.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "VertexDoubleDecker [vertexDownState=" + m_VertexDownState + ", vertexUpState=" + m_VertexUpState + "]";
	}
}