/*
 * Copyright (C) 2011-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE TraceAbstractionConcurrent plug-in.
 * 
 * The ULTIMATE TraceAbstractionConcurrent plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE TraceAbstractionConcurrent plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE TraceAbstractionConcurrent plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE TraceAbstractionConcurrent plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE TraceAbstractionConcurrent plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.BasicPredicate;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.variables.IProgramVar;

public class ProdState extends BasicPredicate {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8826942011742605334L;
	
	List<IPredicate> mPredicates = new ArrayList<IPredicate>();
	
	protected ProdState(int serialNumber, List<IPredicate> mPredicates, Term term, Set<IProgramVar> vars) {
		super(serialNumber, null, term, vars, null);
		mPredicates = mPredicates;
	}

	/**
	 * The published attributes.  Update this and getFieldValue()
	 * if you add new attributes.
	 */
	private final static String[] s_AttribFields = {
		"Predicates", "Formula", "Vars", "OldVars"
	};
	
	@Override
	protected String[] getFieldNames() {
		return s_AttribFields;
	}

	@Override
	protected Object getFieldValue(String field) {
		if (field == "Predicates") {
			return mPredicates;
		} else {
			return super.getFieldValue(field);
		}
	}

	public void addPredicate(IPredicate Predicate) {
		mPredicates.add(Predicate);
	}
	
	public List<IPredicate> getPredicates() {
		return mPredicates;
	}

	/* (non-Javadoc)
	 * @see de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.StateFormula#toString()
	 */
	@Override
	public String toString() {
//		StringBuilder result = new StringBuilder();
//		for (Predicate pp : mPredicates)
//			result.append(pp.getPosition()  + ",");
//		return result.toString();
		return mPredicates.toString();
	}
	
	
}
