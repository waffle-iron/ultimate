package de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.smt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.core.api.UltimateServices;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.FormulaUnLet;
import de.uni_freiburg.informatik.ultimate.logic.FunctionSymbol;
import de.uni_freiburg.informatik.ultimate.logic.QuantifiedFormula;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.Activator;



/**
 * Try to eliminate existentially quantified variables in terms.
 * Therefore we use that the term ∃v.v=c∧φ[v] is equivalent to term φ[c].
 * Resp. we use that the term ∀v.v!=c∨φ[v] is equivalent to term φ[c].
 */
public class DestructiveEqualityResolution {
	
	private static Logger s_Logger = 
			UltimateServices.getInstance().getLogger(Activator.PLUGIN_ID);
	
	/**
	 * Returns equivalent formula. Quantifier is dropped if quantified
	 * variable not in formula. Quantifier is eliminated if this can be done
	 * by using a naive "destructive equality resolution".
	 */
	public static Term quantifier(Script script, int quantifier, 
			TermVariable[] vars, Term body,	Term[]... patterns) {
		Set<TermVariable> occuringVars = 
				new HashSet<TermVariable>(Arrays.asList(body.getFreeVars()));
		List<TermVariable> remaning = new ArrayList<TermVariable>(vars.length);
		for (TermVariable tv : vars) {
			if (occuringVars.contains(tv)) {
				remaning.add(tv);
			}
		}
		Term reduced = derSimple(script, quantifier, body, remaning);
		if (remaning.isEmpty()) {
			return reduced;
		} else {
			return script.quantifier(quantifier, 
					remaning.toArray(new TermVariable[0]), reduced, patterns);
		}
	}
	
	/**
	 * Try to eliminate the variables vars in term.
	 * Let vars =  {x_1,...,x_n} and term = φ. Returns a term that is 
	 * equivalent to ∃x_1,...,∃x_n φ, but were variables are removed.
	 * Successfully removed variables are also removed from vars.
	 * Analogously for universal quantification.
	 */
	public static Term derSimple(Script script, int quantifier, Term term, 
			Collection<TermVariable> vars) {
		Term resFormula = new FormulaUnLet().unlet(term);
		Iterator<TermVariable> it = vars.iterator();
		while(it.hasNext()) {
		TermVariable tv = it.next();
			Term replacementTerm;
			if (quantifier == QuantifiedFormula.EXISTS) {
				replacementTerm = findEqualTermExists(tv, resFormula);
			} else if (quantifier == QuantifiedFormula.FORALL) {
				replacementTerm = findEqualTermForall(tv, resFormula);
			} else {
				throw new AssertionError("unknown quantifier");
			}
			if (replacementTerm != null) {
				s_Logger.debug("eliminated quantifed variable " + tv);
				it.remove();
				TermVariable[] varsAux = { tv };
				Term[] valuesAux = { replacementTerm };
				resFormula = script.let(varsAux, valuesAux, resFormula);
				resFormula = new FormulaUnLet().unlet(resFormula);
			}
			else {
				s_Logger.debug("unable to eliminated quantifed variable " + tv);
			}
		}
		return resFormula;
	}
	
	
	/**
	 * Find term φ such that term implies tv == φ. 
	 */
	private static Term findEqualTermExists(TermVariable tv, Term term) {
		if (term instanceof ApplicationTerm) {
			ApplicationTerm appTerm = (ApplicationTerm) term;
			FunctionSymbol sym = appTerm.getFunction();
			Term[] params = appTerm.getParameters();
			if (sym.getName().equals("=")) {
				int tvOnOneSideOfEquality = tvOnOneSideOfEquality(tv, params);
				if (tvOnOneSideOfEquality == -1) {
					return null;
				} else {
					assert (tvOnOneSideOfEquality == 0 || tvOnOneSideOfEquality == 1);
					return params[tvOnOneSideOfEquality];				
				}
			} else if (sym.getName().equals("and")) {
				for (Term param : params) {
					Term equalTerm = findEqualTermExists(tv, param);
					if (equalTerm != null) {
						return equalTerm;
					}
				}
				return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	
	/**
	 * Find term φ such that tv != φ implies term 
	 */
	private static Term findEqualTermForall(TermVariable tv, Term term) {
		if (term instanceof ApplicationTerm) {
			ApplicationTerm appTerm = (ApplicationTerm) term;
			FunctionSymbol sym = appTerm.getFunction();
			Term[] params = appTerm.getParameters();
			if (sym.getName().equals("not")) {
				assert params.length == 1;
				if (params[0] instanceof ApplicationTerm) {
					appTerm = (ApplicationTerm) params[0];
					sym = appTerm.getFunction();
					params = appTerm.getParameters();
					if (sym.getName().equals("=")) {
						int tvOnOneSideOfEquality = tvOnOneSideOfEquality(tv, params);
						if (tvOnOneSideOfEquality == -1) {
							return null;
						} else {
							assert (tvOnOneSideOfEquality == 0 || tvOnOneSideOfEquality == 1);
							return params[tvOnOneSideOfEquality];				
						}
					} else {
						return null;
					}
				} else {
					return null;
				}
			} else if (sym.getName().equals("or")) {
				for (Term param : params) {
					Term equalTerm = findEqualTermForall(tv, param);
					if (equalTerm != null) {
						return equalTerm;
					}
				}
				return null;
		} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	
	/**
     * return <ul>
	 * <li> 0 if right hand side of equality is tv and left hand side does not
	 *  contain tv
  	 * <li> 1 if left hand side of equality is tv and right hand side does not
	 *  contain tv
	 * <li> -1 otherwise
	 * </ul>
	 * 
	 */
	private static int tvOnOneSideOfEquality(TermVariable tv, Term[] params) {
		if (params.length != 2) {
			s_Logger.warn("Equality of length " + params.length);
		}
		if (params[0] == tv) {
			final boolean rightHandSideContainsTv = 
					Arrays.asList(params[1].getFreeVars()).contains(tv);
			if (rightHandSideContainsTv) {
				return -1;
			}
			else {
				return 1;
			}
		} else if (params[1] == tv) {
			final boolean leftHandSideContainsTv = 
					Arrays.asList(params[0].getFreeVars()).contains(tv);
			if (leftHandSideContainsTv) {
				return -1;
			}
			else {
				return 0;
			}
		}
		return -1;
	}

}
