/*
 * Copyright (C) 2012-2015 Evren Ermis
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE ModelCheckerUtils Library.
 * 
 * The ULTIMATE ModelCheckerUtils Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE ModelCheckerUtils Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE ModelCheckerUtils Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE ModelCheckerUtils Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE ModelCheckerUtils Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.transfomers;

import java.util.HashMap;

import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermTransformer;

public class SubstituteTermTransformer extends TermTransformer{

//	private Term mterm = null;
//	private Term msubstitute = null;
	private HashMap<Term, Term> msubstitution = new HashMap<Term, Term>();
	
	public Term substitute(Term formula, Term term, Term substitute) {
//		mterm = term;
//		msubstitute = substitute;
		msubstitution.clear();
		msubstitution.put(term, substitute);
		final Term result = transform(formula);
		return result;
	}
	
	public Term substitute(Term formula, HashMap<Term,Term> substitution) {
		msubstitution = substitution;
		final Term result = transform(formula);
		return result;
	}
	
	@Override
	protected void convert(Term term) {
		if (msubstitution.containsKey(term)) {
			super.setResult(msubstitution.get(term));
			return;
		}
		super.convert(term);
	}
	
}
