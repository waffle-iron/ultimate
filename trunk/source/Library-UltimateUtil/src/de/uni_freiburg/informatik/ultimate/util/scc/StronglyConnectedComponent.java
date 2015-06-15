/*
 * Copyright (C) 2009-2015 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.util.scc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * StronglyConnectedComponent used in {@link SccComputation}.
 * @author Matthias Heizmann
 *
 * @param <NODE>
 */
public class StronglyConnectedComponent<NODE> {

	protected NODE m_RootNode;
	protected final Set<NODE> m_Nodes = new HashSet<NODE>();

	public StronglyConnectedComponent() {
		super();
	}

	public int getNumberOfStates() {
		return m_Nodes.size();
	}

	public NODE getRootNode() {
		return m_RootNode;
	}

	/**
	 * @return The {@link StateContainer}s of all states that are 
	 * contained in this SCC.
	 */
	public Set<NODE> getNodes() {
		return Collections.unmodifiableSet(m_Nodes);
	}
	
	public void addNode(NODE node) {
		if (m_RootNode != null) {
			throw new UnsupportedOperationException("If root node is set SCC may not be modified");
		}
		boolean notAlreadyContained = m_Nodes.add(node);
		assert notAlreadyContained : "nodes must not be added twice";
	}
	
	public void setRootNode(NODE rootNode) {
		if (m_RootNode != null) {
			throw new UnsupportedOperationException("If root node is set SCC may not be modified");
		}
		m_RootNode = rootNode;
	}
	

}