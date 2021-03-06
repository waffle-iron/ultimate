/*
 * Copyright (C) 2015 Jan Leike (leike@informatik.uni-freiburg.de)
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

import de.uni_freiburg.informatik.ultimate.lassoranker.exceptions.TermException;
import de.uni_freiburg.informatik.ultimate.lassoranker.variables.TransFormulaLR;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.TermTransformer;


/**
 * A preprocessor performs some modifications to the input formulae for
 * stem and loop.
 * 
 * This is the base class for processors that use a TermTransformer on the
 * transition formula. Creates a new TermTransformer instance for each
 * TransFormulaLR that is processed.
 * 
 * @author Jan Leike
 */
public abstract class TransformerPreprocessor extends TransitionPreprocessor {
	
	/**
	 * Create a TermTransformer instance that will be applied to the stem and
	 * the loop transition formula.
	 */
	protected abstract TermTransformer getTransformer(Script script);
	
	@Override
	public TransFormulaLR process(Script script, TransFormulaLR tf) throws TermException {
		final TermTransformer transformer = getTransformer(script);
		final TransFormulaLR new_tf = new TransFormulaLR(tf);
		new_tf.setFormula(transformer.transform(tf.getFormula()));
		return new_tf;
	}
}
