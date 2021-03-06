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

package de.uni_freiburg.informatik.ultimate.plugins.spaceex.automata.hybridsystem;

public class Transition {

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
	protected Transition(Location source, Location target) {
		mSource = source;
		mTarget = target;
		mGuard = "";
		mUpdate = "";
		mLabel = "";

		source.addOutgoingTransition(this);
		target.addIncomingTransition(this);
	}

	protected int getSourceId() {
		return mSource.getId();
	}

	protected int getTargetId() {
		return mTarget.getId();
	}

	protected void setLabel(String label) {
		mLabel = label;
	}

	protected String getLabel() {
		return mLabel;
	}

	protected void setUpdate(final String update) {
		mUpdate = update;
	}

	protected String getUpdate() {
		return mUpdate;
	}

	protected void setGuard(final String guard) {
		mGuard = guard;
	}

	protected String getGuard() {
		return mGuard;
	}

	protected void setSource(final Location location) {
		mSource = location;
	}

	protected void setTarget(final Location location) {
		mTarget = location;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		sb.append("(").append(mSource.getId()).append(")").append(" === (").append(mGuard).append("); {")
		        .append(mUpdate).append("}");
		if (!mLabel.isEmpty()) {
			sb.append("; Label: ").append(mLabel);
		}
		sb.append(" ===> ").append("(").append(mTarget.getId()).append(")");

		return sb.toString();
	}
}
