package de.uni_freiburg.informatik.ultimatetest.decider.overallResult;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.services.IResultService;
import de.uni_freiburg.informatik.ultimate.result.AllSpecificationsHoldResult;
import de.uni_freiburg.informatik.ultimate.result.CounterExampleResult;
import de.uni_freiburg.informatik.ultimate.result.ExceptionOrErrorResult;
import de.uni_freiburg.informatik.ultimate.result.IResult;
import de.uni_freiburg.informatik.ultimate.result.ITimeoutResult;
import de.uni_freiburg.informatik.ultimate.result.SyntaxErrorResult;
import de.uni_freiburg.informatik.ultimate.result.TypeErrorResult;
import de.uni_freiburg.informatik.ultimate.result.UnprovableResult;
import de.uni_freiburg.informatik.ultimate.result.UnsupportedSyntaxResult;
import de.uni_freiburg.informatik.ultimate.util.HashRelation;


/**
 * Evaluate the overall result of a safety checker.
 * 
 * First, we iterate through all IResults returned by Ultimate and put them
 * into categories (which IResult is a witness for which overall result).
 * Afterwards we iterate through all categories in the order of their 
 * significance. The first non-empty category is our overall result.
 * 
 * @author heizmann@informatik.uni-freiburg.de
 *
 */
public class SafetyCheckerOverallResultEvaluator implements IOverallResultEvaluator<SafetyCheckerOverallResult> {
	
	private final HashRelation<SafetyCheckerOverallResult, IResult> m_Category2Results = 
			new HashRelation<SafetyCheckerOverallResult, IResult>();
	private SafetyCheckerOverallResult m_OverallResult;
	private Set<IResult> m_MostSignificantResults;
	
	public void evaluateOverallResult(IResultService resultService) {
		for (Entry<String, List<IResult>> entry  : resultService.getResults().entrySet()) {
			for (IResult result  : entry.getValue()) {
				SafetyCheckerOverallResult category = detectResultCategory(result);
				m_Category2Results.addPair(category, result);
			}
		}
		SafetyCheckerOverallResult[] categoriesOrderedBySignificance = 
				new SafetyCheckerOverallResult[] {
				SafetyCheckerOverallResult.EXCEPTION_OR_ERROR,
				SafetyCheckerOverallResult.SYNTAX_ERROR,
				SafetyCheckerOverallResult.UNSUPPORTED_SYNTAX,
				SafetyCheckerOverallResult.UNSAFE,
				SafetyCheckerOverallResult.UNKNOWN,
				SafetyCheckerOverallResult.TIMEOUT,
				SafetyCheckerOverallResult.SAFE
		};
		
		for (SafetyCheckerOverallResult category : categoriesOrderedBySignificance) {
			if (m_Category2Results.getDomain().contains(category)) {
				m_OverallResult = category;
			}
		}
		
		if (m_OverallResult == null) {
			m_OverallResult = SafetyCheckerOverallResult.NO_RESULT;
		} else {
			m_MostSignificantResults = m_Category2Results.getImage(m_OverallResult);
		}
	}
	
	
	private SafetyCheckerOverallResult detectResultCategory(IResult result) {
		if (result instanceof AllSpecificationsHoldResult) {
			return SafetyCheckerOverallResult.SAFE;
		} else if (result instanceof CounterExampleResult) {
			return SafetyCheckerOverallResult.UNSAFE;
		} else if (result instanceof UnprovableResult) {
			return SafetyCheckerOverallResult.UNKNOWN;
		} else if (result instanceof TypeErrorResult) {
			return SafetyCheckerOverallResult.SYNTAX_ERROR;
		} else if (result instanceof SyntaxErrorResult) {
			return SafetyCheckerOverallResult.SYNTAX_ERROR;
		} else if (result instanceof ITimeoutResult) {
			return SafetyCheckerOverallResult.TIMEOUT;
		} else if (result instanceof UnsupportedSyntaxResult) {
			return SafetyCheckerOverallResult.UNSUPPORTED_SYNTAX;
		} else if (result instanceof ExceptionOrErrorResult) {
			return SafetyCheckerOverallResult.EXCEPTION_OR_ERROR;
		} else {
			return null;
		}
	}


	@Override
	public SafetyCheckerOverallResult getOverallResult() {
		return m_OverallResult;
	}
	
	@Override
	public String generateOverallResultMessage() {
		switch (getOverallResult()) {
		case EXCEPTION_OR_ERROR:
			return getMostSignificantResults().toString();
		case NO_RESULT:
			return getOverallResult().toString();
		case SAFE:
			return getMostSignificantResults().toString();
		case SYNTAX_ERROR:
			return getMostSignificantResults().toString();
		case TIMEOUT:
			return getMostSignificantResults().toString();
		case UNKNOWN:
			return concatenateShortDescriptions(getMostSignificantResults());
		case UNSAFE:
			return concatenateShortDescriptions(getMostSignificantResults());
		case UNSUPPORTED_SYNTAX:
			return getMostSignificantResults().toString();
		default:
			throw new AssertionError("unknown overall result");
		}
	}


	@Override
	public Set<IResult> getMostSignificantResults() {
		return m_MostSignificantResults;
	}
	
	
	private String concatenateShortDescriptions(Set<IResult> iresults) {
		StringBuilder sb = new StringBuilder();
		for (IResult iResult : iresults) {
			sb.append(iResult.getShortDescription());
			sb.append(" ");
		}
		return sb.toString();
	}



}
