/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE IRSDependencies plug-in.
 * 
 * The ULTIMATE IRSDependencies plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE IRSDependencies plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE IRSDependencies plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE IRSDependencies plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE IRSDependencies plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.analysis.irsdependencies.rcfg.walker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.irsdependencies.rcfg.annotations.IRSDependenciesAnnotation;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.irsdependencies.rcfg.annotations.RCFGUnrollWalkerAnnotation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ProgramPoint;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RCFGEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RCFGNode;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootNode;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;

public class RCFGWalkerUnroller extends RCFGWalker {

	protected final String sMainProcedureName = "main";
	protected int mUnrollings;
	protected HashMap<RCFGEdge, ARTEdge> mGraph;
	protected List<List<ARTEdge>> mPaths;
	protected Stack<RCFGEdge> mCalls;
	protected Stack<RCFGEdge> mNestedLoops;
	protected Stack<HashMap<RCFGEdge, ARTEdge>> mGraphs;

	protected List<List<RCFGEdge>> mFinalPaths;

	protected List<RCFGEdge> mCurrentPath;

	public RCFGWalkerUnroller(ObserverDispatcher dispatcher, ILogger logger, int unrollings) {
		super(dispatcher, logger);
		mUnrollings = unrollings;
	}

	@Override
	public void startFrom(RootNode node) {
		init();
		final RCFGEdge start = searchMainEdge(node);
		if (start == null) {
			mLogger.error("No main procedure found");
			return;
		}
		process(start, new ArrayList<ARTEdge>());
		finish();
	}

	public List<List<RCFGEdge>> getPaths() {
		return mFinalPaths;
	}

	public List<RCFGEdge> getCurrentPrefix() {
		return mCurrentPath;
	}

	protected void init() {
		mFinalPaths = null;
		mGraph = new HashMap<>();
		mPaths = new ArrayList<>();
		mCalls = new Stack<>();
		mNestedLoops = new Stack<>();
		mGraphs = new Stack<>();
		mCurrentPath = new ArrayList<>();
	}

	protected void finish() {
		mFinalPaths = new ArrayList<>();

		for (final List<ARTEdge> path : mPaths) {
			final List<RCFGEdge> newPath = new ArrayList<>();
			for (final ARTEdge edge : path) {
				newPath.add(edge.mBacking);
			}
			mFinalPaths.add(newPath);
		}
		mLogger.info("Processed "+mFinalPaths.size()+" traces");

		mGraph = null;
		mPaths = null;
		mCalls = null;
		mGraphs = null;
		mNestedLoops = null;
		mCurrentPath = null;
	}

	protected void printPath(List<ARTEdge> path) {
		mLogger.debug(path.get(0).getSource());
		for (final ARTEdge e : path) {
			mLogger.debug(e.mBacking);
			mLogger.debug(e.getTarget());
		}
	}

	protected RCFGEdge searchMainEdge(final RootNode node) {
		mLogger.debug("Searching for procedure \"" + sMainProcedureName + "\"");
		for (final RCFGEdge edge : node.getOutgoingEdges()) {
			final ProgramPoint possibleMain = (ProgramPoint) edge.getTarget();
			mLogger.debug("Candidate: \"" + possibleMain.getProcedure() + "\"");
			if (possibleMain.getProcedure()
					.equalsIgnoreCase(sMainProcedureName)) {
				mLogger.debug("Found match");
				return edge;
			}
		}
		return null;
	}

	public void process(RCFGEdge currentEdge, List<ARTEdge> processed) {
		final ARTEdge current = createEdge(currentEdge);
		if (current.getAnnotation() == null) {
			findLoopEntryExit(currentEdge.getTarget());
		}

		mLogger.debug("processing: " + current);

		if (current.isVisited(mUnrollings)) {
			mLogger.debug("--reached unrolling limit");
			mLogger.debug("");
			return;
		} else {
			current.visit();
		}

		if (currentEdge instanceof Call) {
			mCalls.push(currentEdge);
			mLogger.debug("--push (call) " + currentEdge);
			mLogger.debug("----old mGraph: " + mGraph.values());
			mLogger.debug("----new mGraph: fresh");

			mGraphs.push(mGraph);
			mGraph = new HashMap<>();

			if (isMaxCallDepth(currentEdge)) {
				mLogger.debug("--reached unrolling limit (call)");
				mLogger.debug("");
				return;
			}
		} else if (currentEdge instanceof Return) {
			mCalls.pop();
			final String old = mGraph.values().toString();
			mGraph = mGraphs.pop();
			current.mLastGraphState = mGraph;
			mLogger.debug("--pop (return) " + currentEdge);
			mLogger.debug("----old mGraph: " + old);
			mLogger.debug("----new mGraph: " + mGraph.values());
		}

		if (current.isNestedLoopEntry()) {
			mLogger.debug("--push (nestedLoop) " + currentEdge);
			mNestedLoops.push(currentEdge);
			if (isMaxLoopDepth(current)) {
				mLogger.debug("--reached unrolling limit (nestedLoop)");
				mLogger.debug("----mNestedLoops: " + mNestedLoops);
				mNestedLoops.pop();
				mLogger.debug("----pop mNestedLoops: " + mNestedLoops);
				mLogger.debug("");
				return;
			}
			mLogger.debug("----old mGraph: " + mGraph.values());
			mLogger.debug("----new mGraph: fresh");
			mGraphs.push(mGraph);
			mGraph = new HashMap<>();

		} else if (current.isNestedLoopExit()) {
			mNestedLoops.push(currentEdge);
			final String old = mGraph.values().toString();
			mGraph = mGraphs.pop();
			current.mLastGraphState = mGraph;
			mLogger.debug("--push (nestedLoop) " + currentEdge);
			mLogger.debug("----old mGraph: " + old);
			mLogger.debug("----new mGraph: " + mGraph.values());
		}

		processed.add(current);
		mCurrentPath.add(current.mBacking);
		pre(current.mBacking);
		pre(current.mBacking.getTarget());

		if (current.getTarget().getOutgoingEdges().isEmpty()) {
			mPaths.add(new ArrayList<>(processed));
			mLogger.debug("");
			mLogger.debug("Trace: " + traceToString(processed));
			mLogger.debug("mCalls: " + mCalls);
			mLogger.debug("mNestedLoops: " + mNestedLoops);
			mLogger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ End @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			mLogger.debug("");
			programExitReached();
			return;
		}

		for (final RCFGEdge next : current.getTarget().getOutgoingEdges()) {
			if (isFeasible(next)) {
				process(next, processed);
				post(current.mBacking);
				post(current.mBacking.getTarget());

			} else {
				mLogger.debug("--infeasible: " + next);
			}
		}
		backtrack(current, processed);
	}

	protected void backtrack(ARTEdge stop, List<ARTEdge> processed) {
		mLogger.debug("--backtracking--");
		for (int i = processed.size() - 1; i >= 0; --i) {
			final ARTEdge current = processed.get(i);
			mLogger.debug("----remove from trace prefix: " + current);
			processed.remove(i);
			mCurrentPath.remove(i);

			if (current.isNestedLoopExit()) {
				mNestedLoops.pop();
				mGraphs.push(mGraph);
				final String old = mGraph.values().toString();
				mGraph = current.mLastGraphState;
				mLogger.debug("----pop (nestedLoopExit) " + current.mBacking);
				mLogger.debug("------old mGraph: " + old);
				mLogger.debug("------new mGraph: " + mGraph.values());
			} else if (current.isNestedLoopEntry()) {
				mNestedLoops.pop();
				final String old = mGraph.values().toString();
				mGraph = mGraphs.pop();
				mLogger.debug("----pop (nestedLoopEntry) " + current.mBacking);
				mLogger.debug("------old mGraph: " + old);
				mLogger.debug("------new mGraph: " + mGraph.values());
			}

			if (current.mBacking instanceof Return) {
				mCalls.push(((Return) current.mBacking)
						.getCorrespondingCall());
				mGraphs.push(mGraph);
				final String old = mGraph.values().toString();
				mGraph = current.mLastGraphState;
				mLogger.debug("----push (return) " + current.mBacking);
				mLogger.debug("------old mGraph: " + old);
				mLogger.debug("------new mGraph: " + mGraph.values());
			} else if (current.mBacking instanceof Call) {
				mCalls.pop();
				final String old = mGraph.values().toString();
				mGraph = mGraphs.pop();
				mLogger.debug("----pop (call) " + current.mBacking);
				mLogger.debug("------old mGraph: " + old);
				mLogger.debug("------new mGraph: " + mGraph.values());
			}

			mLogger.debug("----change visit counter in mGraph: "
					+ current.mBacking);
			if (!mGraph.containsKey(current.mBacking)) {
				mLogger.debug("------not in current mGraph (so visit is 0): "
						+ current.mBacking);
			} else {
				// instead of removing, just reduce the visit counter by one
				// mGraph.remove(current.mBacking);
				mGraph.get(current.mBacking).mVisited--;
			}

			if (stop.equals(current)) {
				break;
			}
		}
		mLogger.debug("--trace prefix: " + traceToString(processed));
		mLogger.debug("--mCalls: " + mCalls);
		mLogger.debug("--mNestedLoops: " + mNestedLoops);
		mLogger.debug("--end backtracking--");
		mLogger.debug("");
	}

	protected void findLoopEntryExit(RCFGNode loopNode) {
		if (loopNode.getPayload().getLocation().isLoop()) {
			for (final RCFGEdge potentialSelfLoop : loopNode.getOutgoingEdges()) {
				if (potentialSelfLoop.getTarget().equals(loopNode)) {
					addAnnotation(potentialSelfLoop, loopNode, true, true);
					return;
				}
			}

			for (final RCFGEdge potentialEntryEdge : loopNode.getOutgoingEdges()) {
				final RCFGEdge potentialBackEdge = findBackedge(potentialEntryEdge,
						loopNode, new HashSet<RCFGEdge>());
				if (potentialBackEdge != null) {
					addAnnotation(potentialEntryEdge, loopNode, true, false);
					addAnnotation(potentialBackEdge, loopNode, false, true);
				}
			}
		}
	}

	protected void addAnnotation(RCFGEdge edge, RCFGNode honda,
			boolean isEntry, boolean isExit) {
		new RCFGUnrollWalkerAnnotation(honda, isEntry, isExit)
				.addAnnotation(edge);
	}

	protected RCFGEdge findBackedge(RCFGEdge current, RCFGNode target,
			HashSet<RCFGEdge> visited) {
		if (current.getTarget().equals(target)) {
			return current;
		} else {
			visited.add(current);
			for (final RCFGEdge succ : current.getTarget().getOutgoingEdges()) {
				if (!visited.contains(succ)) {
					final RCFGEdge potential = findBackedge(succ, target, visited);
					if (potential != null
							&& potential.getTarget().equals(target)) {
						return potential;
					}
				}
			}
			return null;
		}
	}

	protected boolean isMaxCallDepth(RCFGEdge c) {
		int i = 0;
		for (final RCFGEdge call : mCalls) {
			if (c.equals(call)) {
				++i;
			}
		}
		return i > mUnrollings;
	}

	protected boolean isMaxLoopDepth(ARTEdge c) {
		// c is nestedLoopEntry
		int i = 0;
		for (final RCFGEdge loopEntries : mNestedLoops) {
			if (c.mBacking.equals(loopEntries)) {
				++i;
			}
		}
		return i > mUnrollings;
	}

	protected boolean isFeasible(RCFGEdge next) {
		if (next instanceof Summary) {
			return false;
		} else if (next instanceof Return) {
			if (mCalls.isEmpty()) {
				return false;
			} else {
				// TODO: search for the last return; necessary?
				return ((Return) next).getCorrespondingCall().equals(
						mCalls.peek());
			}
		}
		return true;
	}

	protected String traceToString(List<ARTEdge> trace) {
		final StringBuilder sb = new StringBuilder();
		for (final ARTEdge e : trace) {
			sb.append(e.mBacking);
			sb.append(" ");
		}
		return sb.toString();
	}

	protected ARTEdge createEdge(RCFGEdge edge) {
		ARTEdge currentNode;
		if (mGraph.containsKey(edge)) {
			currentNode = mGraph.get(edge);
		} else {
			currentNode = new ARTEdge(edge);
			mGraph.put(edge, currentNode);
		}
		return currentNode;
	}

	private class ARTEdge {
		private final RCFGEdge mBacking;
		private int mVisited;
		private HashMap<RCFGEdge, ARTEdge> mLastGraphState;

		public ARTEdge(RCFGEdge backing) {
			mBacking = backing;
			mVisited = 1;
		}

		public void visit() {
			++mVisited;
		}

		public boolean isVisited(int unrolling) {
			return mVisited > unrolling;
		}

		public RCFGNode getTarget() {
			return mBacking.getTarget();
		}

		public RCFGNode getSource() {
			return mBacking.getSource();
		}

		@Override
		public String toString() {
			if (isLoopEntry() && isLoopExit()) {
				return mBacking.toString() + " (visited=" + mVisited
						+ ", isLoopEntry&Exit, honda=" + getLoopHead() + ")";
			}

			if (isLoopEntry()) {
				return mBacking.toString() + " (visited=" + mVisited
						+ ", isLoopEntry, honda=" + getLoopHead() + ")";
			}

			if (isLoopExit()) {
				return mBacking.toString() + " (visited=" + mVisited
						+ ", isLoopExit, honda=" + getLoopHead() + ")";
			}

			return mBacking.toString() + " (visited=" + mVisited + ")";

		}

		private boolean isLoopEntry() {
			final RCFGUnrollWalkerAnnotation annot = getAnnotation();
			if (annot != null) {
				return annot.isLoopEntry();
			} else {
				return false;
			}
		}

		private boolean isLoopExit() {
			final RCFGUnrollWalkerAnnotation annot = getAnnotation();
			if (annot != null) {
				return annot.isLoopExit();
			} else {
				return false;
			}
		}

		private boolean isNestedLoopEntry() {
			final RCFGUnrollWalkerAnnotation annot = getAnnotation();
			if (annot != null) {
				return !annot.isLoopExit() && annot.isLoopEntry();
			} else {
				return false;
			}
		}

		private boolean isNestedLoopExit() {
			final RCFGUnrollWalkerAnnotation annot = getAnnotation();
			if (annot != null) {
				return annot.isLoopExit() && !annot.isLoopEntry();
			} else {
				return false;
			}
		}

		private RCFGNode getLoopHead() {
			final RCFGUnrollWalkerAnnotation annot = getAnnotation();
			if (annot != null) {
				return annot.getHonda();
			} else {
				return null;
			}
		}

		private RCFGUnrollWalkerAnnotation getAnnotation() {
			return IRSDependenciesAnnotation.getAnnotation(mBacking,
					IRSDependenciesAnnotation.class);
		}
	}

}
