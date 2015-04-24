package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.core.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermTransformer;
import de.uni_freiburg.informatik.ultimate.logic.Util;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.ModelCheckerUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearTerms.AffineRelation;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearTerms.NotAffineException;
import de.uni_freiburg.informatik.ultimate.util.DebugMessage;


/**
 * Brings Terms into a normal form where all parameters that of commutative
 * functions (resp. functions for that this class knows that they are
 * commutative) are sorted according to their hash code.
 * Furthermore all AffineRelations are transformed into positive normal form.
 * 
 * This can simplify terms, e.g., (or (and A B) (and B A)) will be simplified
 * to (and A B) (except in the very rare case where the hash code of A and B
 * coincides).
 * 
 * Note that this is still experimental.
 * Problems: AffineRelations are not yet sorted according to hash code.
 * We do not want this, because it is a problem for legibility, we do not want
 * to have terms like (+ x*2 1 3*y), instead we would prefer (+ 2*x 3*y 1):
 * coefficient before variable, constant term at last position.
 * @author Matthias Heizmann
 *
 */
public class CommuhashNormalForm {

	private final IUltimateServiceProvider m_Services;
	private final Script m_Script;
	
	public CommuhashNormalForm(IUltimateServiceProvider services, Script script) {
		super();
		m_Services = services;
		m_Script = script;
	}
	
	public Term transform(Term term) {
		Logger logger = m_Services.getLoggingService().getLogger(ModelCheckerUtils.sPluginID);
		logger.debug(new DebugMessage(
				"applying CommuhashNormalForm to formula of DAG size {0}", 
				new DagSizePrinter(term)));
		Term result = (new CommuhashNormalFormHelper()).transform(term);
		logger.debug(new DebugMessage(
				"DAG size before CommuhashNormalForm {0}, DAG size after CommuhashNormalForm {1}", 
				new DagSizePrinter(term), new DagSizePrinter(result)));
		assert (Util.checkSat(m_Script, m_Script.term("distinct", term, result)) != LBool.SAT) : "CommuhashNormalForm transformation unsound";
		return result;
	}
	
	
	private boolean isKnownToBeCommutative(String applicationString) {
		switch (applicationString) {
		case "and":
		case "or":
		case "=":
		case "distinct":
		case "+":
		case "*":
			return true;
		default:
			return false;
		}
	}

	private class CommuhashNormalFormHelper extends TermTransformer {

		@Override
		public void convertApplicationTerm(ApplicationTerm appTerm, Term[] newArgs) {
			String funcname = appTerm.getFunction().getApplicationString();
			if (isKnownToBeCommutative(funcname)) {
				Term simplified = constructTermWithSortedParams(funcname, newArgs);
				try {
					simplified = tryToTransformToPositiveNormalForm(simplified);
				} catch (NotAffineException e) {
					// do nothing, input is no AffineRelation
				} 
				setResult(simplified);
			} else {
				super.convertApplicationTerm(appTerm, newArgs);
			}
		}

		private Term tryToTransformToPositiveNormalForm(Term simplified) throws NotAffineException {
			AffineRelation affRel = new AffineRelation(simplified);
			Term pnf = affRel.positiveNormalForm(m_Script);
			ApplicationTerm appTerm = (ApplicationTerm) pnf;
			String funcname = appTerm.getFunction().getApplicationString();
			Term[] params = appTerm.getParameters();
			Term result = constructTermWithSortedParams(funcname, params);
			return result;
		}

		private Term[] sortByHashCode(final Term[] params) {
			Term[] sortedParams = params.clone();
			Comparator<Term> hashBasedComperator = new Comparator<Term>() {
				@Override
				public int compare(Term arg0, Term arg1) {
					return arg0.hashCode() - arg1.hashCode();
				}
			};
			Arrays.sort(sortedParams, hashBasedComperator);
			return sortedParams;
		}
		
		private Term constructTermWithSortedParams(String funcname, Term[] params) {
			Term[] sortedParams = sortByHashCode(params);
			Term simplified = SmtUtils.termWithLocalSimplification(
					m_Script, funcname, sortedParams);
			return simplified;
		}

	}
}