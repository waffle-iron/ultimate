package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceStore;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractStateBinaryOperator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState.SubsetResult;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.preferences.AbsIntPrefInitializer;
import de.uni_freiburg.informatik.ultimate.util.relation.Pair;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
final class SummaryMap<STATE extends IAbstractState<STATE, ACTION, VARDECL>, ACTION, VARDECL, LOCATION> {

	private final Map<ACTION, Set<Pair<STATE, STATE>>> mSummaries;
	private final IAbstractStateBinaryOperator<STATE> mMergeOp;
	private final ITransitionProvider<ACTION, LOCATION> mTransProvider;
	private final int mMaxParallelStates;

	SummaryMap(final IAbstractStateBinaryOperator<STATE> mergeOp, final ITransitionProvider<ACTION, LOCATION> trans) {
		mTransProvider = trans;
		mMergeOp = mergeOp;
		mSummaries = new HashMap<>();
		mMaxParallelStates = new UltimatePreferenceStore(Activator.PLUGIN_ID)
				.getInt(AbsIntPrefInitializer.LABEL_MAX_PARALLEL_STATES);
	}

	/**
	 * 
	 * @param preCallState
	 * @param postCallState
	 * @param current
	 *            An action that leaves a scope
	 */
	void addSummary(final STATE preCallState, final STATE postCallState, final ACTION current) {
		// current is a call, but we have to find the summary
		final ACTION summaryAction = getSummaryAction(current);
		final Set<Pair<STATE, STATE>> summaries = getOrCreateSummaries(summaryAction);
		for (final Pair<STATE, STATE> summary : summaries) {
			if (preCallState.isSubsetOf(summary.getFirst()) != SubsetResult.NONE) {
				summaries.remove(summary);
				final Pair<STATE, STATE> newSummary = getMergedSummary(summary, preCallState, postCallState);
				summaries.add(newSummary);
				return;
			}
		}
		summaries.add(new Pair<>(preCallState, postCallState));
		mergeIfNecessary(summaries);
	}

	private ACTION getSummaryAction(final ACTION current) {
		final Collection<ACTION> succActions = mTransProvider.getSuccessorActions(mTransProvider.getSource(current));
		return succActions.stream().filter(a -> mTransProvider.isSummaryForCall(a, current)).findAny().get();
	}

	private void mergeIfNecessary(Set<Pair<STATE, STATE>> summaries) {
		if (summaries.size() <= mMaxParallelStates) {
			return;
		}

		final Iterator<Pair<STATE, STATE>> iter = summaries.iterator();
		Pair<STATE, STATE> acc = null;
		while (iter.hasNext()) {
			final Pair<STATE, STATE> current = iter.next();
			iter.remove();
			if (acc == null) {
				acc = current;
			} else {
				acc = getMergedSummary(acc, current.getFirst(), current.getSecond());
			}
		}
		summaries.add(acc);
	}

	STATE getSummaryPostState(final ACTION current, final STATE preCallState) {
		final Set<Pair<STATE, STATE>> summaries = mSummaries.get(current);
		if (summaries == null) {
			return null;
		}
		for (final Pair<STATE, STATE> summary : summaries) {
			if (preCallState.isSubsetOf(summary.getFirst()) != SubsetResult.NONE) {
				return summary.getSecond();
			}
		}
		return null;
	}

	private Pair<STATE, STATE> getMergedSummary(final Pair<STATE, STATE> oldSummary, final STATE preCallState,
			final STATE postCallState) {
		final STATE newPre = mMergeOp.apply(oldSummary.getFirst(), preCallState);
		final STATE newPost = mMergeOp.apply(oldSummary.getSecond(), postCallState);
		return new Pair<>(newPre, newPost);
	}

	private Set<Pair<STATE, STATE>> getOrCreateSummaries(ACTION current) {
		Set<Pair<STATE, STATE>> rtr = mSummaries.get(current);
		if (rtr == null) {
			rtr = new HashSet<>();
			mSummaries.put(current, rtr);
		}
		return rtr;
	}

	@Override
	public String toString() {
		return mSummaries.toString();
	}

}