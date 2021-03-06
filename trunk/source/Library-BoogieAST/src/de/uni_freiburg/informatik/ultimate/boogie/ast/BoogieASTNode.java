/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
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
package de.uni_freiburg.informatik.ultimate.boogie.ast;

import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.boogie.BoogieLocation;
import de.uni_freiburg.informatik.ultimate.core.lib.models.BasePayloadContainer;
import de.uni_freiburg.informatik.ultimate.core.lib.models.VisualizationNode;
import de.uni_freiburg.informatik.ultimate.core.model.models.ILocation;
import de.uni_freiburg.informatik.ultimate.core.model.models.ISimpleAST;
import de.uni_freiburg.informatik.ultimate.core.model.models.IWalkable;
import de.uni_freiburg.informatik.ultimate.core.model.models.Payload;

public class BoogieASTNode extends BasePayloadContainer implements ISimpleAST<BoogieASTNode, VisualizationNode> {

	private static final long serialVersionUID = 5856434889026482850L;

	public BoogieASTNode(ILocation location) {
		super(new Payload(location));

		if (location instanceof BoogieLocation) {
			((BoogieLocation) location).setBoogieASTNode(this);
		}
	}

	public ILocation getLocation() {
		return getPayload().getLocation();
	}

	protected BoogieASTNode createSpecialChild(String name, Object[] childs) {
		final BoogieASTWrapper parent = new BoogieASTWrapper(null, name);
		for (final Object obj : childs) {
			parent.getOutgoingNodes().add(createSpecialChild(obj));
		}
		return parent;
	}

	protected BoogieASTNode createSpecialChild(Object obj) {
		return new BoogieASTWrapper(null, obj);
	}

	@Override
	public VisualizationNode getVisualizationGraph() {
		return new VisualizationNode(this);
	}

	@Override
	public List<IWalkable> getSuccessors() {
		final ArrayList<IWalkable> rtr = new ArrayList<>();
		for (final BoogieASTNode node : getOutgoingNodes()) {
			rtr.add(node);
		}
		return rtr;
	}

	@Override
	public List<BoogieASTNode> getOutgoingNodes() {
		return new ArrayList<BoogieASTNode>();
	}

	private class BoogieASTWrapper extends BoogieASTNode {

		private static final long serialVersionUID = 1L;
		private final Object mBacking;

		public BoogieASTWrapper(ILocation location, Object backing) {
			super(location);
			mBacking = backing;
		}

		@Override
		public String toString() {
			if (mBacking != null) {
				return mBacking.toString();
			} else {
				return super.toString();
			}
		}

	}
}
