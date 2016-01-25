/*
 * Copyright (C) 2015-2016 Daniel Tischner
 * Copyright (C) 2009-2016 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.buchiReduction.fair;

import java.util.Map.Entry;

import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.buchiReduction.GameGraphChangeType;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.buchiReduction.GameGraphChanges;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.buchiReduction.vertices.DuplicatorVertex;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.buchiReduction.vertices.Vertex;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.buchiReduction.vertices.VertexValueContainer;
import de.uni_freiburg.informatik.ultimate.util.relation.NestedMap3;
import de.uni_freiburg.informatik.ultimate.util.relation.Triple;

/**
 * Class that stores information of changes made to a {@link FairGameGraph}.
 * <br/>
 * <br/>
 * 
 * Additionally to {@link GameGraphChanges} it can also remember changed buechi
 * transitions.<br/>
 * A FairGameGraphChanges object can then be used to undo made changes for a
 * fair game graph by using {@link FairGameGraph#undoChanges(GameGraphChanges)}.
 * 
 * @author Daniel Tischner
 *
 * @param <LETTER>
 *            Letter class of buechi automaton
 * @param <STATE>
 *            State class of buechi automaton
 */
public final class FairGameGraphChanges<LETTER, STATE> extends GameGraphChanges<LETTER, STATE> {

	/**
	 * Stores information about changed buechi transitions.<br/>
	 * Stored as (source, letter, destination, type of change).
	 */
	private final NestedMap3<STATE, LETTER, STATE, GameGraphChangeType> m_ChangedBuechiTransitions;

	/**
	 * Creates a new fair game graph changes object with no changes at default.
	 */
	public FairGameGraphChanges() {
		super();
		m_ChangedBuechiTransitions = new NestedMap3<>();
	}

	/**
	 * Stores information about an added buechi transition.<br/>
	 * Nullifies changes if the given transition was removed before.
	 * 
	 * @param src
	 *            Source of the added buechi transition
	 * @param a
	 *            The letter of the added buechi transition
	 * @param dest
	 *            Destination of the added buechi transition
	 */
	public void addedBuechiTransition(final STATE src, final LETTER a, final STATE dest) {
		changedBuechiTransition(src, a, dest, GameGraphChangeType.ADDITION);
	}

	/**
	 * Gets the information about changed buechi transitions.<br/>
	 * Stored as (source, letter, destination, type of change).
	 * 
	 * @return The information about changed buechi transitions
	 */
	public NestedMap3<STATE, LETTER, STATE, GameGraphChangeType> getChangedBuechiTransitions() {
		return m_ChangedBuechiTransitions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * buchiReduction.GameGraphChanges#merge(de.uni_freiburg.informatik.ultimate
	 * .automata.nwalibrary.operations.buchiReduction.GameGraphChanges, boolean)
	 */
	@Override
	public void merge(final GameGraphChanges<LETTER, STATE> changes, final boolean rememberValuesOfFirst) {
		super.merge(changes, rememberValuesOfFirst);

		if (changes == null) {
			return;
		}

		if (changes instanceof FairGameGraphChanges) {
			FairGameGraphChanges<LETTER, STATE> fairChanges = (FairGameGraphChanges<LETTER, STATE>) changes;
			// Merge changed buechi transitions
			NestedMap3<STATE, LETTER, STATE, GameGraphChangeType> changedTransitions = fairChanges
					.getChangedBuechiTransitions();
			for (STATE changedKey : changedTransitions.keySet()) {
				for (Triple<LETTER, STATE, GameGraphChangeType> changedTrans : changedTransitions.get(changedKey)
						.entrySet()) {
					STATE src = changedKey;
					LETTER a = changedTrans.getFirst();
					STATE dest = changedTrans.getSecond();
					GameGraphChangeType changeType = m_ChangedBuechiTransitions.get(src, a, dest);

					if (changeType == null || changeType.equals(GameGraphChangeType.NO_CHANGE)) {
						// Only add transition change if unknown until now
						m_ChangedBuechiTransitions.put(src, a, dest, changedTrans.getThird());
					} else if ((changeType == GameGraphChangeType.ADDITION
							&& changedTrans.getThird() == GameGraphChangeType.REMOVAL)
							|| (changeType == GameGraphChangeType.REMOVAL
									&& changedTrans.getThird() == GameGraphChangeType.ADDITION)) {
						// Nullify change if it was added and then
						// removed or vice versa
						m_ChangedBuechiTransitions.get(src).remove(a, dest);
					}
				}
			}
		}
	}

	/**
	 * Stores information about an removed buechi transition.<br/>
	 * Nullifies changes if the given transition was added before.
	 * 
	 * @param src
	 *            Source of the removed buechi transition
	 * @param a
	 *            The letter of the removed buechi transition
	 * @param dest
	 *            Destination of the removed buechi transition
	 */
	public void removedBuechiTransition(final STATE src, final LETTER a, final STATE dest) {
		changedBuechiTransition(src, a, dest, GameGraphChangeType.REMOVAL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * buchiReduction.GameGraphChanges#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String lineSeparator = System.lineSeparator();
		// Header
		result.append("GameGraphChanges ggc = (");

		// Vertices
		result.append(lineSeparator + "\tchangedVertices = {");
		for (Entry<Vertex<LETTER, STATE>, GameGraphChangeType> vertex : getChangedVertices().entrySet()) {
			result.append(lineSeparator + "\t\t<(" + vertex.getKey().getQ0() + ", " + vertex.getKey().getQ1() + "), p:"
					+ vertex.getKey().getPriority() + ">\t" + vertex.getValue());
		}
		result.append(lineSeparator + "\t},");

		// Edges
		result.append(lineSeparator + "\tchangedEdges = {");
		for (Triple<Vertex<LETTER, STATE>, Vertex<LETTER, STATE>, GameGraphChangeType> vertex : getChangedEdges()
				.entrySet()) {
			result.append(lineSeparator + "\t\t(" + vertex.getFirst().getQ0() + ", " + vertex.getFirst().getQ1());
			if (vertex.getFirst() instanceof DuplicatorVertex) {
				DuplicatorVertex<LETTER, STATE> vertexAsDuplicatorVertex = (DuplicatorVertex<LETTER, STATE>) vertex
						.getFirst();
				result.append(", " + vertexAsDuplicatorVertex.getLetter());
			}
			result.append(")\t--> (" + vertex.getSecond().getQ0() + ", " + vertex.getSecond().getQ1());
			if (vertex.getSecond() instanceof DuplicatorVertex) {
				DuplicatorVertex<LETTER, STATE> vertexAsDuplicatorVertex = (DuplicatorVertex<LETTER, STATE>) vertex
						.getSecond();
				result.append(", " + vertexAsDuplicatorVertex.getLetter());
			}
			result.append(")\t" + vertex.getThird());
		}
		result.append(lineSeparator + "\t}");

		// Changed buechi transitions
		result.append(lineSeparator + "\tchangedBuechiTrans = {");
		for (STATE vertex : getChangedBuechiTransitions().keySet()) {
			for (Triple<LETTER, STATE, GameGraphChangeType> entry : getChangedBuechiTransitions().get(vertex)
					.entrySet()) {
				result.append(lineSeparator + "\t\t" + vertex + " -" + entry.getFirst() + "-> " + entry.getSecond()
						+ "\t" + entry.getThird());
			}
		}
		result.append(lineSeparator + "\t}");

		// Remembered values
		result.append(lineSeparator + "\trememberedValues = {");
		for (Entry<Vertex<LETTER, STATE>, VertexValueContainer> vertexContainer : getRememberedVertexValues()
				.entrySet()) {
			result.append(lineSeparator + "\t\t(" + vertexContainer.getKey().getQ0() + ", "
					+ vertexContainer.getKey().getQ1());
			if (vertexContainer.getKey() instanceof DuplicatorVertex) {
				DuplicatorVertex<LETTER, STATE> vertexAsDuplicatorVertex = (DuplicatorVertex<LETTER, STATE>) vertexContainer
						.getKey();
				result.append(", " + vertexAsDuplicatorVertex.getLetter());
			}
			result.append(")\tPM:");
			result.append(vertexContainer.getValue().getProgressMeasure() + ", BNM:");
			result.append(vertexContainer.getValue().getBestNeighborMeasure() + ", NC:");
			result.append(vertexContainer.getValue().getNeighborCounter());
		}
		result.append(lineSeparator + "\t}");

		// Footer
		result.append(lineSeparator + ");");

		return result.toString();
	}

	/**
	 * Stores information about a changed buechi transition.<br/>
	 * Nullifies changes if the given buechi transition was added before if it
	 * was now removed or vice versa.
	 * 
	 * @param src
	 *            Source of the changed buechi transition
	 * @param a
	 *            The letter of the changed buechi transition
	 * @param dest
	 *            Destination of the changed buechi transition
	 * @param type
	 *            Type of the change
	 */
	private void changedBuechiTransition(final STATE src, final LETTER a, final STATE dest,
			final GameGraphChangeType type) {
		GameGraphChangeType previousType = m_ChangedBuechiTransitions.get(src, a, dest);
		// Nullify change if previously added and then removed or vice versa
		if (previousType != null && ((previousType.equals(GameGraphChangeType.ADDITION)
				&& type.equals(GameGraphChangeType.REMOVAL))
				|| (previousType.equals(GameGraphChangeType.REMOVAL) && type.equals(GameGraphChangeType.ADDITION)))) {
			m_ChangedBuechiTransitions.get(src).remove(a, dest);
		} else {
			m_ChangedBuechiTransitions.put(src, a, dest, type);
		}
	}
}