/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE BuchiProgramProduct plug-in.
 * 
 * The ULTIMATE BuchiProgramProduct plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE BuchiProgramProduct plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE BuchiProgramProduct plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE BuchiProgramProduct plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE BuchiProgramProduct plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.buchiprogramproduct.optimizeproduct;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.model.services.IToolchainStorage;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.plugins.generator.buchiautomizer.annot.BuchiProgramAcceptingStateAnnotation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ProgramPoint;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RCFGEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootNode;

public class MaximizeFinalStates extends BaseProductOptimizer {

	private int mNewAcceptingStates;

	public MaximizeFinalStates(RootNode product, IUltimateServiceProvider services, IToolchainStorage storage) {
		super(product, services, storage);
		mLogger.info(mNewAcceptingStates + " new accepting states");
	}

	@Override
	protected void init(RootNode root, IUltimateServiceProvider services) {
		mNewAcceptingStates = 0;
	}

	@Override
	protected RootNode process(RootNode root) {
		int lastRun = processInternal(root);
		mNewAcceptingStates += lastRun;
		while (lastRun > 0) {
			lastRun = processInternal(root);
			mNewAcceptingStates += lastRun;
		}
		return root;
	}

	private int processInternal(RootNode root) {
		final Deque<ProgramPoint> nodes = new ArrayDeque<>();
		final Set<ProgramPoint> closed = new HashSet<>();
		BuchiProgramAcceptingStateAnnotation annot = null;
		int newAcceptingStates = 0;
		for (final RCFGEdge edge : root.getOutgoingEdges()) {
			nodes.add((ProgramPoint) edge.getTarget());
		}

		while (!nodes.isEmpty()) {
			final ProgramPoint current = nodes.removeFirst();
			if (closed.contains(current)) {
				continue;
			}
			closed.add(current);
			annot = BuchiProgramAcceptingStateAnnotation.getAnnotation(current);
			if (annot != null) {
				// this state is already accepting
				nodes.addAll(getSuccessors(current));
				continue;
			}

			final List<ProgramPoint> succs = getSuccessors(current);
			if (succs.isEmpty()) {
				// there are no successors
				continue;
			}

			boolean allSuccessorsAreAccepting = true;
			for (final ProgramPoint succ : succs) {
				annot = BuchiProgramAcceptingStateAnnotation.getAnnotation(succ);
				allSuccessorsAreAccepting = allSuccessorsAreAccepting && annot != null;
				nodes.add(succ);
			}

			if (allSuccessorsAreAccepting) {
				// all successors are accepting, make this one also accepting
				annot.annotate(current);
				newAcceptingStates++;
			}

		}
		return newAcceptingStates;
	}

	public int getNewAcceptingStates() {
		return mNewAcceptingStates;
	}

	@Override
	public boolean isGraphChanged() {
		return mNewAcceptingStates > 0;
	}
}
