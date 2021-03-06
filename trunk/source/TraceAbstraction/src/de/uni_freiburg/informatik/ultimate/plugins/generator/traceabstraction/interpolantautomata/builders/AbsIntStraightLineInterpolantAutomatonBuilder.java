/*
 * Copyright (C) 2016 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2016 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * Copyright (C) 2016 University of Freiburg
 *
 * This file is part of the ULTIMATE TraceAbstraction plug-in.
 *
 * The ULTIMATE TraceAbstraction plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE TraceAbstraction plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE TraceAbstraction plug-in. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE TraceAbstraction plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE TraceAbstraction plug-in grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.interpolantautomata.builders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.IRun;
import de.uni_freiburg.informatik.ultimate.automata.Word;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomatonSimple;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedRun;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.SimplicationTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.XnfConversionTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.tool.IAbstractInterpretationResult;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.PredicateUnifier;

/**
 * Builder for counter-example-based abstract interpretation interpolant automata. This builder constructs a straight
 * line interpolant automaton from the counter-example and adds the predicates generated by abstract interpretation to
 * the automaton. No further predicates and/or transitions are inferred.
 *
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
public class AbsIntStraightLineInterpolantAutomatonBuilder
        implements IInterpolantAutomatonBuilder<CodeBlock, IPredicate> {

	private static final long PRINT_PREDS_LIMIT = 30;

	private final IUltimateServiceProvider mServices;
	private final ILogger mLogger;
	private final NestedWordAutomaton<CodeBlock, IPredicate> mResult;
	private final SmtManager mSmtManager;
	private final IRun<CodeBlock, IPredicate> mCurrentCounterExample;

	public AbsIntStraightLineInterpolantAutomatonBuilder(final IUltimateServiceProvider services,
	        final INestedWordAutomatonSimple<CodeBlock, IPredicate> oldAbstraction,
	        final IAbstractInterpretationResult<?, CodeBlock, IBoogieVar, ?> aiResult,
	        final PredicateUnifier predUnifier, final SmtManager smtManager,
	        final IRun<CodeBlock, IPredicate> currentCounterExample,
	        final SimplicationTechnique simplificationTechnique, final XnfConversionTechnique xnfConversionTechnique) {
		mServices = services;
		mLogger = services.getLoggingService().getLogger(Activator.PLUGIN_ID);
		mSmtManager = smtManager;
		mCurrentCounterExample = currentCounterExample;
		mResult = constructAutomaton(oldAbstraction, aiResult, predUnifier);
	}

	@Override
	public NestedWordAutomaton<CodeBlock, IPredicate> getResult() {
		return mResult;
	}

	private NestedWordAutomaton<CodeBlock, IPredicate> constructAutomaton(
	        final INestedWordAutomatonSimple<CodeBlock, IPredicate> oldAbstraction,
	        final IAbstractInterpretationResult<?, CodeBlock, IBoogieVar, ?> aiResult,
	        final PredicateUnifier predicateUnifier) {

		mLogger.info("Creating automaton from AI predicates.");

		final NestedWordAutomaton<CodeBlock, IPredicate> result = new NestedWordAutomaton<CodeBlock, IPredicate>(
		        new AutomataLibraryServices(mServices), oldAbstraction.getInternalAlphabet(),
		        oldAbstraction.getCallAlphabet(), oldAbstraction.getReturnAlphabet(), oldAbstraction.getStateFactory());

		final NestedRun<CodeBlock, IPredicate> cex = (NestedRun<CodeBlock, IPredicate>) mCurrentCounterExample;
		final Word<CodeBlock> word = cex.getWord();

		final int wordlength = word.length();
		assert wordlength > 1 : "Unexpected: length of word smaller or equal to 1.";

		final Map<Call, IPredicate> callHierarchyPredicates = new HashMap<>();

		final IPredicate falsePredicate = predicateUnifier.getFalsePredicate();
		final Set<IPredicate> alreadyThereAsState = new HashSet<>();
		IPredicate previous = predicateUnifier.getTruePredicate();
		alreadyThereAsState.add(previous);
		result.addState(true, false, previous);

		for (int i = 0; i < wordlength; i++) {
			final CodeBlock symbol = word.getSymbol(i);

			@SuppressWarnings("unchecked")
			final Set<IAbstractState<?, ?, ?>> nextStates = (Set<IAbstractState<?, ?, ?>>) aiResult.getLoc2States()
			        .get(symbol.getTarget());

			final IPredicate target;
			if (nextStates == null) {
				target = falsePredicate;
			} else {
				target = predicateUnifier.getOrConstructPredicateForDisjunction(
				        nextStates.stream().map(s -> s.getTerm(mSmtManager.getScript(), mSmtManager.getBoogie2Smt()))
				                .map(predicateUnifier::getOrConstructPredicate).collect(Collectors.toSet()));
			}

			if (mLogger.isDebugEnabled()) {
				writeTransitionAddLog(i, symbol, nextStates, previous, target);
			}

			if (alreadyThereAsState.add(target)) {
				result.addState(false, falsePredicate.equals(target), target);
			}

			// Add transition
			if (symbol instanceof Call) {
				result.addCallTransition(previous, symbol, target);
				callHierarchyPredicates.put((Call) symbol, previous);
			} else if (symbol instanceof Return) {
				final Return returnSymbol = (Return) symbol;
				final IPredicate hierarchyState = callHierarchyPredicates.get(returnSymbol.getCorrespondingCall());
				assert hierarchyState != null : "Return does not have a corresponding call.";
				result.addReturnTransition(previous, hierarchyState, symbol, target);
			} else {
				result.addInternalTransition(previous, symbol, target);
			}

			previous = target;
		}

		// Add self-loops to final states
		if (!result.getFinalStates().isEmpty()) {
			for (final IPredicate finalState : result.getFinalStates()) {
				oldAbstraction.getAlphabet().forEach(l -> result.addInternalTransition(finalState, l, finalState));
			}
		}

		if (PRINT_PREDS_LIMIT < alreadyThereAsState.size()) {
			mLogger.info("Using "
			        + alreadyThereAsState.size() + " predicates from AI: " + String.join(",", alreadyThereAsState
			                .stream().limit(PRINT_PREDS_LIMIT).map(a -> a.toString()).collect(Collectors.toList()))
			        + "...");
		} else {
			mLogger.info("Using " + alreadyThereAsState.size() + " predicates from AI: " + String.join(",",
			        alreadyThereAsState.stream().map(a -> a.toString()).collect(Collectors.toList())));
		}

		return result;
	}

	private void writeTransitionAddLog(final int i, final CodeBlock symbol,
	        final Set<IAbstractState<?, ?, ?>> nextStates, final IPredicate source, final IPredicate target) {
		final String divider = "------------------------------------------------";
		if (i == 0) {
			mLogger.debug(divider);
		}
		mLogger.debug("Transition: " + symbol);
		mLogger.debug("Original Target States: " + (nextStates == null ? "NONE" : nextStates));
		mLogger.debug("Source Term: " + source);
		mLogger.debug("Target Term: " + target);
		mLogger.debug(divider);
	}
}
