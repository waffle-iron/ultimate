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
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.vertices;

/**
 * Summarize edge that summarizes transitions representing moves on the same
 * stack level. If a summarize edge from <tt>src</tt> to <tt>dest</tt> exists it
 * means that one can move from that vertex to the vertex ending up with the
 * same stack level than before. Such edges connect Spoiler vertices with
 * Spoiler vertices. To ensure a a legal game graph it creates shadow vertices
 * between the source and destination. In detail this is <bb>src ->
 * DuplicatorShadowVertex1 -> SpoilerShadowVertex -> DuplicatorShadowVertex2 ->
 * dest</bb>. The <tt>SpoilerShadowVertex</tt> can be used to assign the edge a
 * priority. By default the priority is not valid and needs to be assigned after
 * creation, else the graph is in an illegal state.
 * 
 * @author Daniel Tischner
 *
 * @param <LETTER>
 *            Letter class of nwa automaton
 * @param <STATE>
 *            State class of nwa automaton
 */
public final class SummarizeEdge<LETTER, STATE> {

	public final static int NO_PRIORITY = -1;
	/**
	 * Destination of the edge.
	 */
	private final SpoilerDoubleDeckerVertex<LETTER, STATE> m_Dest;
	/**
	 * The first duplicator shadow vertex to create a valid edge.
	 */
	private final DuplicatorDoubleDeckerVertex<LETTER, STATE> m_DuplicatorEntryShadow;
	/**
	 * The second duplicator shadow vertex to create a valid edge.
	 */
	private final DuplicatorDoubleDeckerVertex<LETTER, STATE> m_DuplicatorExitShadow;
	/**
	 * Spoilers shadow vertex to create a valid edge.
	 */
	private final SpoilerDoubleDeckerVertex<LETTER, STATE> m_SpoilerShadow;

	/**
	 * Source of the edge.
	 */
	private final SpoilerDoubleDeckerVertex<LETTER, STATE> m_Src;

	/**
	 * Creates a new summarize edge with given source and destination vertices.
	 * 
	 * @param src
	 *            Source of the edge
	 * @param dest
	 *            Destination of the edge
	 */
	public SummarizeEdge(final SpoilerDoubleDeckerVertex<LETTER, STATE> src,
			final SpoilerDoubleDeckerVertex<LETTER, STATE> dest) {
		m_Src = src;
		m_Dest = dest;
		m_DuplicatorEntryShadow = new DuplicatorDoubleDeckerVertex<LETTER, STATE>(2, false, null, null, null,
				TransitionType.SUMMARIZE_ENTRY, this);
		m_SpoilerShadow = new SpoilerDoubleDeckerVertex<LETTER, STATE>(NO_PRIORITY, false, null, null, this);
		m_DuplicatorExitShadow = new DuplicatorDoubleDeckerVertex<LETTER, STATE>(2, false, null, null, null,
				TransitionType.SUMMARIZE_EXIT, this);
	}

	/**
	 * Gets the destination of the edge.
	 * 
	 * @return The destination of the edge
	 */
	public SpoilerVertex<LETTER, STATE> getDestination() {
		return m_Dest;
	}

	/**
	 * Gets the first shadow vertex. In detail the construct is: <bb>src ->
	 * DuplicatorShadowVertex1 -> SpoilerShadowVertex -> DuplicatorShadowVertex2
	 * -> dest</bb>
	 * 
	 * @return The first shadow vertex
	 */
	public DuplicatorDoubleDeckerVertex<LETTER, STATE> getEntryShadowVertex() {
		return m_DuplicatorEntryShadow;
	}

	/**
	 * Gets the last shadow vertex. In detail the construct is: <bb>src ->
	 * DuplicatorShadowVertex1 -> SpoilerShadowVertex -> DuplicatorShadowVertex2
	 * -> dest</bb>
	 * 
	 * @return The first shadow vertex
	 */
	public DuplicatorDoubleDeckerVertex<LETTER, STATE> getExitShadowVertex() {
		return m_DuplicatorExitShadow;
	}

	/**
	 * Gets the middle shadow vertex. In detail the construct is: <bb>src ->
	 * DuplicatorShadowVertex1 -> SpoilerShadowVertex -> DuplicatorShadowVertex2
	 * -> dest</bb>
	 * 
	 * @return The first shadow vertex
	 */
	public SpoilerDoubleDeckerVertex<LETTER, STATE> getMiddleShadowVertex() {
		return m_SpoilerShadow;
	}

	/**
	 * Gets the priority of the edge. This is the priority of the spoiler shadow
	 * vertex.
	 * 
	 * @return Returns the priority of the edge
	 */
	public int getPriority() {
		return m_SpoilerShadow.getPriority();
	}

	/**
	 * Gets the source of the edge.
	 * 
	 * @return The source of the edge
	 */
	public SpoilerVertex<LETTER, STATE> getSource() {
		return m_Src;
	}

	/**
	 * Sets the priority of the edge. This is the priority of the spoiler shadow
	 * vertex.
	 * 
	 * @param priority
	 *            THe priority to set
	 */
	public void setPriority(final int priority) {
		m_SpoilerShadow.setPriority(priority);
	}
}