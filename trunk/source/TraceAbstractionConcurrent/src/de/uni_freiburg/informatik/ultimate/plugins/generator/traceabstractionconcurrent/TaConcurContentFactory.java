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
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstractionconcurrent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.automata.petrinet.Marking;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.Place;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.julian.Condition;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ProgramPoint;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.AbstractCegarLoop;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.PredicateFactoryForInterpolantAutomata;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.ISLPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.ProdState;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager;

public class TaConcurContentFactory extends PredicateFactoryForInterpolantAutomata {

	public TaConcurContentFactory(final Map<String, Map<String, ProgramPoint>> locNodes,
			final AbstractCegarLoop abstractCegarLoop, final SmtManager theory,
			final boolean taPrefs,
			final boolean hoareAnnotation,
			final boolean interprocedural) {
		super(theory, taPrefs);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public IPredicate getContentOnConcurrentProduct(final IPredicate c1,
			final IPredicate c2) {
		
		final List<IPredicate> programPoints = new ArrayList<IPredicate>();
		final ProdState result = mSmtManager.getPredicateFactory().getNewProdState(programPoints);
		if (c1 instanceof ProdState) {
			final ProdState ps1 = (ProdState) c1;
			programPoints.addAll(ps1.getPredicates());
		}
		else {
			programPoints.add(c1);
		}
		if (((SPredicate) c2).getProgramPoint() == null) {
			assert (c2.getFormula() != null);
//			result.and(mTheory, (Predicate) c2);
		}
		programPoints.add(c2); 
		return result;
	}



	@Override
	public IPredicate getContentOnPetriNet2FiniteAutomaton(
			final Marking<?, IPredicate> marking) {
		final LinkedList<IPredicate> programPoints = new LinkedList<IPredicate>();
		for (final Place<?, IPredicate> place : marking) {
			programPoints.add(place.getContent());
		}
		return mSmtManager.getPredicateFactory().getNewProdState(programPoints);
	}
	



	@Override
	public IPredicate finitePrefix2net(final Condition<?, IPredicate> c) {
		final ProgramPoint pp = ((ISLPredicate) c.getPlace().getContent()).getProgramPoint();
		return super.mSmtManager.getPredicateFactory().newDontCarePredicate(pp);
	}
	

}
