/*
 * Copyright (C) 2015 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE SpaceExParser plug-in.
 * 
 * The ULTIMATE SpaceExParser plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE SpaceExParser plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE SpaceExParser plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE SpaceExParser plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE SpaceExParser plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.spaceex.ast.automata;

public class Transition extends SpaceExElement {

	private String mLabel;
	private String mGuard;
	private String mUpdate;

	private Location mSource;
	private Location mTarget;

	/**
	 * Connects two locations with a transition. The source and target location's transitions are updated accordingly.
	 * 
	 * @param source
	 *            The source location of the transition.
	 * @param target
	 *            The target location of the transition.
	 */
	public Transition(Location source, Location target) {
		source.addOutgoingTransition(this);
		target.addIncomingTransition(this);
	}

	public void setLabel(String label) {
		mLabel = label;
	}

	public void setUpdate(String update) {
		mUpdate = update;
	}

	public void setGuard(String guard) {
		mGuard = guard;
	}

	public void setSource(Location location) {
		mSource = location;
	}

	public void setTarget(Location location) {
		mTarget = location;
	}
}