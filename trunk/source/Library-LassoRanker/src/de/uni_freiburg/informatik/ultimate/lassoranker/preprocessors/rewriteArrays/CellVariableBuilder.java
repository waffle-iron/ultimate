/*
 * Copyright (C) 2014-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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
package de.uni_freiburg.informatik.ultimate.lassoranker.preprocessors.rewriteArrays;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.lassoranker.variables.ReplacementVar;
import de.uni_freiburg.informatik.ultimate.lassoranker.variables.ReplacementVarFactory;
import de.uni_freiburg.informatik.ultimate.lassoranker.variables.TransFormulaLR;
import de.uni_freiburg.informatik.ultimate.lassoranker.variables.TransFormulaLRUtils;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.ArrayIndex;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalSort;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.HashRelation;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.NestedMap2;

public class CellVariableBuilder {
	private final Map<TermVariable, Map<ArrayIndex, TermVariable>> mArrayInstance2Index2CellVariable;
	private final Map<Term, Map<ArrayIndex, ReplacementVar>> mArray2Index2RepVar;
	private final Set<TermVariable> mAuxVars = new HashSet<TermVariable>();
	private final TransFormulaLR mTransFormula;
	private final TransFormulaLRWithArrayInformation tflrwai;
	private final TransFormulaLRWithArrayCells tflrwac;
	private final ReplacementVarFactory mReplacementVarFactory;
	private final ILogger mLogger;
	private final HashRelation<Term, ArrayIndex> mFirstGeneration2Indices;
	private final NestedMap2<TermVariable, ArrayIndex, ArrayCellReplacementVarInformation> mArrayCellInVars;
	private final NestedMap2<TermVariable, ArrayIndex, ArrayCellReplacementVarInformation> mArrayCellOutVars;


	public CellVariableBuilder(final TransFormulaLR tf, 
			final TransFormulaLRWithArrayCells tflrwac, 
			final ReplacementVarFactory replacementVarFactory, 
			final ILogger logger, 
			final HashRelation<Term, ArrayIndex> firstGeneration2Indices, 
			final NestedMap2<TermVariable, ArrayIndex, 
			ArrayCellReplacementVarInformation> arrayCellInVars, 
			final NestedMap2<TermVariable, ArrayIndex, ArrayCellReplacementVarInformation> arrayCellOutVars) {
		mReplacementVarFactory = replacementVarFactory;
		mLogger = logger;
		mTransFormula = tf;
		mFirstGeneration2Indices = firstGeneration2Indices;
		this.tflrwac = tflrwac;
		tflrwai = tflrwac.getTransFormulaLRWithArrayInformation();
		mArrayInstance2Index2CellVariable = new HashMap<TermVariable, Map<ArrayIndex, TermVariable>>();
		mArray2Index2RepVar = new HashMap<Term, Map<ArrayIndex, ReplacementVar>>();
		mArrayCellInVars = arrayCellInVars;
		mArrayCellOutVars = arrayCellOutVars;
		dotSomething();
	}


	/**
	 * Returns a String that we use to refer to the array cell array[index].
	 */
	private String getArrayCellName(final TermVariable array, final List<Term> index) {
		return "arrayCell_" + SmtUtils.removeSmtQuoteCharacters(array.toString())
				+ SmtUtils.removeSmtQuoteCharacters(index.toString());
	}

	public void dotSomething() {
		for (final TermVariable firstGeneration : tflrwai.getArrayFirstGeneration2Instances().getDomain()) {
			for (final TermVariable instance : tflrwai.getArrayFirstGeneration2Instances().getImage(firstGeneration)) {
				Map<ArrayIndex, TermVariable> index2ArrayCellTv = mArrayInstance2Index2CellVariable.get(instance);
				if (index2ArrayCellTv == null) {
					index2ArrayCellTv = new HashMap<ArrayIndex, TermVariable>();
					mArrayInstance2Index2CellVariable.put(instance, index2ArrayCellTv);
				}
				final Set<ArrayIndex> indicesOfFirstGeneration = mFirstGeneration2Indices.getImage(firstGeneration);
				if (indicesOfFirstGeneration == null) {
					mLogger.info("Array " + firstGeneration + " is never accessed");
					continue;
				}
				for (final ArrayIndex index : indicesOfFirstGeneration) {
					TermVariable tv = index2ArrayCellTv.get(index);
					if (tv == null) {
						tv = constructTermVariable(instance, index);
						index2ArrayCellTv.put(index, tv);
					}
					final boolean isInVarCell = isInVarCell(instance, index);
					final boolean isOutVarCell = isOutVarCell(instance, index);
					if (isInVarCell || isOutVarCell) {
						final TermVariable arrayRepresentative = (TermVariable) TransFormulaLRUtils.getDefinition(mTransFormula, instance);
						final ArrayIndex indexRepresentative = tflrwac.getOrConstructIndexRepresentative(index);
						if (isInVarCell) {
							final ReplacementVar rv = mArrayCellInVars.get(arrayRepresentative, indexRepresentative).getReplacementVar();
							final TermVariable inVar = (TermVariable) mTransFormula.getInVars().get(rv);
							if (inVar == null) {
								mTransFormula.addInVar(rv, tv);
							} else {
								// case where two TermVariables have the same
								// ReplacementVar is also possible e.g. if there
								// is an array equality, see 
								// SyntaxSupportArrays20-ArrayEquality.bpl
								addToAuxVars(tv);
							}
						}
						if (isOutVarCell) {
							final ReplacementVar rv = mArrayCellOutVars.get(arrayRepresentative, indexRepresentative).getReplacementVar();
							final TermVariable outVar = (TermVariable) mTransFormula.getOutVars().get(rv);
							if (outVar == null) {
								mTransFormula.addOutVar(rv, tv);
							} else {
								// case where two TermVariables have the same
								// ReplacementVar is also possible e.g. if there
								// is an array equality, see 
								// SyntaxSupportArrays20-ArrayEquality.bpl
								addToAuxVars(tv);
							}
						}
					} else {
						addToAuxVars(tv);
					}
				}

			}
		}
	}

	private void addToAuxVars(final TermVariable tv) {
		mAuxVars.add(tv);
		// assert false : "not yet implemented";
	}

	private TermVariable constructTermVariable(final TermVariable instance, final List<Term> index) {
		final Sort arraySort = instance.getSort();
		assert arraySort.isArraySort();
		final MultiDimensionalSort mdias = new MultiDimensionalSort(arraySort);
		assert mdias.getDimension() == index.size();
		final Sort valueSort = mdias.getArrayValueSort();
		final String name = getArrayCellName(instance, index);
		final TermVariable tv = mReplacementVarFactory.
				getOrConstructAuxVar(name, valueSort);
		return tv;
	}

	/**
	 * Is the cellVariable that we construct for arrayInstance[index] is an
	 * inVar. This is the case if arrayInstance and each free variable of
	 * index is an inVar.
	 */
	private boolean isInVarCell(final TermVariable arrayInstance, final List<Term> index) {
		if (TransFormulaLRUtils.isInvar(arrayInstance, mTransFormula)) {
			return TransFormulaLRUtils.allVariablesAreInVars(index, mTransFormula);
		} else {
			return false;
		}
	}

	private boolean isOutVarCell(final TermVariable arrayInstance, final List<Term> index) {
		if (TransFormulaLRUtils.isOutvar(arrayInstance, mTransFormula)) {
			return TransFormulaLRUtils.allVariablesAreOutVars(index, mTransFormula);
		} else {
			return false;
		}
	}

	public Map<TermVariable, Map<ArrayIndex, TermVariable>> getArrayInstance2Index2CellVariable() {
		return mArrayInstance2Index2CellVariable;
	}

	public Set<TermVariable> getAuxVars() {
		return mAuxVars;
	}

}
