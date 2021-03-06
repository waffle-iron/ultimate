/*
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE LassoRanker Library.
 * 
 * The ULTIMATE LassoRanker Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE LassoRanker Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE LassoRanker Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE LassoRanker Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE LassoRanker Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.lassoranker.preprocessors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.lassoranker.exceptions.TermException;
import de.uni_freiburg.informatik.ultimate.lassoranker.variables.ReplacementVar;
import de.uni_freiburg.informatik.ultimate.lassoranker.variables.ReplacementVarFactory;
import de.uni_freiburg.informatik.ultimate.lassoranker.variables.TransFormulaLR;
import de.uni_freiburg.informatik.ultimate.lassoranker.variables.TransFormulaLRUtils;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SubstitutionWithLocalSimplification;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;

/**
 * Abstract superclass for preprocessors that replace TermVariables.
 * Note that we have already removed constants 0-ary functions, hence we only
 * have to remove TermVariables
 * 
 * @author Matthias Heizmann
 *
 */
public abstract class RewriteTermVariables extends TransitionPreprocessor {

	/**
	 * The sort to be used for new replacement TermVariable's
	 */
	private final Sort mrepVarSort;

	/**
	 * Compute definition (see {@link IProgramVar#getDefinition()} for RankVar
	 * that will replace oldRankVar.
	 */
	protected abstract Term constructNewDefinitionForRankVar(IProgramVar oldRankVar);

	/**
	 * Construct the Term that will replace the old TermVariable for which
	 * we already constructed the new TermVariable newTv.
	 */
	protected abstract Term constructReplacementTerm(TermVariable newTv);

	/**
	 * @return true iff term will be replaced by this preprocessor
	 */
	protected abstract boolean hasToBeReplaced(Term term);

	/**
	 * @return name of the Sort that we use for the new variables.
	 * This has to be either "Int" or "Real".
	 */
	protected abstract String getRepVarSortName();

	/**
	 * @return the suffix that the new TermVariables get. 
	 * This is mainly important for debugging purposes that we can see that
	 * this preprocessor indeed constructed the variable.
	 */
	protected abstract String getTermVariableSuffix();



	/**
	 * Maps TermVariable that have to replaced to the term by which they are
	 * replaced.
	 */
	private final Map<Term, Term> mSubstitutionMapping;
	
	/**
	 * Factory for construction of auxVars.
	 */
	private final ReplacementVarFactory mVarFactory;
	protected final ManagedScript mScript;

	public RewriteTermVariables(final ReplacementVarFactory varFactory, final ManagedScript script) {
		mVarFactory = varFactory;
		mScript = script;
		mrepVarSort = mScript.getScript().sort(getRepVarSortName());
		mSubstitutionMapping = new LinkedHashMap<Term, Term>();
	}

	/**
	 * Get the new ReplacementVar for a given RankVar.
	 * Constructs a new replacement variable, if needed.
	 */
	private final ReplacementVar getOrConstructReplacementVar(final IProgramVar rankVar) {
		final Term definition = constructNewDefinitionForRankVar(rankVar);
		final ReplacementVar repVar = mVarFactory.
				getOrConstuctReplacementVar(definition);
		return repVar;
	}

	/**
	 * Traverse all inVars, outVars and outVars and construct the new
	 * ReplacementVars and replacing Terms (and put them into the substitution
	 * mapping).
	 */
	private final void generateRepAndAuxVars(final TransFormulaLR tf) {
		final ArrayList<IProgramVar> rankVarsWithDistinctInVar = new ArrayList<>();
		final ArrayList<IProgramVar> rankVarsWithDistinctOutVar = new ArrayList<>();
		final ArrayList<IProgramVar> rankVarsWithCommonInVarOutVar = new ArrayList<>();
		for (final Map.Entry<IProgramVar, Term> entry : tf.getInVars().entrySet()) {
			if (hasToBeReplaced(entry.getValue())) {
				if (TransFormulaLRUtils.inVarAndOutVarCoincide(entry.getKey(), tf)) {
					rankVarsWithCommonInVarOutVar.add(entry.getKey());
				} else {
					rankVarsWithDistinctInVar.add(entry.getKey());
				}
			}
		}
		for (final Map.Entry<IProgramVar, Term> entry : tf.getOutVars().entrySet()) {
			if (hasToBeReplaced(entry.getValue())) {
				if (TransFormulaLRUtils.inVarAndOutVarCoincide(entry.getKey(), tf)) {
					// do nothing, was already added
				} else {
					rankVarsWithDistinctOutVar.add(entry.getKey());
				}
			}
		}
	
		for (final IProgramVar rv : rankVarsWithCommonInVarOutVar) {
			final ReplacementVar repVar = getOrConstructReplacementVar(rv);
			final TermVariable newInOutVar = mVarFactory.getOrConstructAuxVar(
					computeTermVariableName(repVar.getGloballyUniqueId(), true, true), 
					mrepVarSort);
			final Term replacementTerm = constructReplacementTerm(newInOutVar);
			mSubstitutionMapping.put(tf.getInVars().get(rv), replacementTerm);
			tf.removeInVar(rv);
			tf.addInVar(repVar, newInOutVar);
			tf.removeOutVar(rv);
			tf.addOutVar(repVar, newInOutVar);
		}
	
		for (final IProgramVar rv : rankVarsWithDistinctInVar) {
			final ReplacementVar repVar = getOrConstructReplacementVar(rv);
			final TermVariable newInVar = mVarFactory.getOrConstructAuxVar(
					computeTermVariableName(repVar.getGloballyUniqueId(), true, false), 
					mrepVarSort);
			final Term replacementTerm = constructReplacementTerm(newInVar);
			mSubstitutionMapping.put(tf.getInVars().get(rv), replacementTerm);
			tf.removeInVar(rv);
			tf.addInVar(repVar, newInVar);
		}
		
		for (final IProgramVar rv : rankVarsWithDistinctOutVar) {
			final ReplacementVar repVar = getOrConstructReplacementVar(rv);
			final TermVariable newOutVar = mVarFactory.getOrConstructAuxVar(
					computeTermVariableName(repVar.getGloballyUniqueId(), false, true), 
					mrepVarSort);
			final Term replacementTerm = constructReplacementTerm(newOutVar);
			mSubstitutionMapping.put(tf.getOutVars().get(rv), replacementTerm);
			tf.removeOutVar(rv);
			tf.addOutVar(repVar, newOutVar);
		}
		
		final Set<TermVariable> auxVars = tf.getAuxVars();
		for (final TermVariable tv : auxVars) {
			if (hasToBeReplaced(tv)) {
				final TermVariable newAuxVar = mVarFactory.getOrConstructAuxVar(
						computeTermVariableName(tv.getName(), false, false),
						mrepVarSort);
				tf.removeAuxVar(tv);
				tf.addAuxVars(Collections.singleton(newAuxVar));
			}
		}
	}
	
	private final String computeTermVariableName(final String baseName, final boolean isInVar, final boolean isOutVar) {
		final String result;
		if (isInVar) {
			if (isOutVar) {
				result = baseName + "_inout_" + getTermVariableSuffix();
			} else {
				result = baseName + "_in_" + getTermVariableSuffix();
			}
		} else {
			if (isOutVar) {
				result = baseName + "_out_" + getTermVariableSuffix();
			} else {
				result = baseName + "_aux_" + getTermVariableSuffix();
			}
		}
		return result;
	}

	@Override
	public final TransFormulaLR process(final Script script, final TransFormulaLR tf) throws TermException {
		generateRepAndAuxVars(tf);
		final TransFormulaLR newTf = new TransFormulaLR(tf);
		final Term newFormula = (new SubstitutionWithLocalSimplification(
				mScript, mSubstitutionMapping)).transform(tf.getFormula());
		newTf.setFormula(newFormula);
		return newTf;
	}

}
