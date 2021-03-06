package de.uni_freiburg.informatik.ultimate.plugins.generator.treeautomizer.hornutil;

import de.uni_freiburg.informatik.ultimate.logic.TermVariable;

public class HCVar {
	private final HornClausePredicateSymbol predicate;
	private final TermVariable termVariable;
	private final int idx;
	
	public HCVar(HornClausePredicateSymbol pr, int pos, TermVariable v) {
		predicate = pr;
		idx = pos;
		termVariable = v;
	}
	
	public TermVariable getTermVariable() {
		return termVariable;
	}
	
	@Override
	public String toString() {
		return predicate.name + "{" + idx + "}";
	}
	
	public String getGloballyUniqueId() {
		return String.format("%s_%d_%s", predicate.name, idx, termVariable.getName());
	}
}
