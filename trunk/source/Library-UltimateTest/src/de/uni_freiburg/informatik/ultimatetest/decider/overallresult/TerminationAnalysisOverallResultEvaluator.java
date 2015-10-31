/*
 * Copyright (C) 2014-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE UnitTest Library.
 * 
 * The ULTIMATE UnitTest Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE UnitTest Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE UnitTest Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE UnitTest Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE UnitTest Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimatetest.decider.overallresult;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.services.model.IResultService;
import de.uni_freiburg.informatik.ultimate.result.ExceptionOrErrorResult;
import de.uni_freiburg.informatik.ultimate.result.IResult;
import de.uni_freiburg.informatik.ultimate.result.ITimeoutResult;
import de.uni_freiburg.informatik.ultimate.result.SyntaxErrorResult;
import de.uni_freiburg.informatik.ultimate.result.TerminationAnalysisResult;
import de.uni_freiburg.informatik.ultimate.result.TerminationAnalysisResult.TERMINATION;
import de.uni_freiburg.informatik.ultimate.result.TypeErrorResult;
import de.uni_freiburg.informatik.ultimate.result.UnsupportedSyntaxResult;
import de.uni_freiburg.informatik.ultimate.util.HashRelation;


/**
 * Evaluate the overall result of a termination analysis.
 * 
 * First, we iterate through all IResults returned by Ultimate and put them
 * into categories (which IResult is a witness for which overall result).
 * Afterwards we iterate through all categories in the order of their 
 * significance. The first non-empty category is our overall result.
 * 
 * @author heizmann@informatik.uni-freiburg.de
 *
 */
public class TerminationAnalysisOverallResultEvaluator implements IOverallResultEvaluator<TerminationAnalysisOverallResult> {
	
	private final HashRelation<TerminationAnalysisOverallResult, IResult> m_Category2Results = 
			new HashRelation<TerminationAnalysisOverallResult, IResult>();
	private TerminationAnalysisOverallResult m_OverallResult;
	private Set<IResult> m_MostSignificantResults;
	
	public void evaluateOverallResult(IResultService resultService) {
		for (Entry<String, List<IResult>> entry  : resultService.getResults().entrySet()) {
			for (IResult result  : entry.getValue()) {
				TerminationAnalysisOverallResult category = detectResultCategory(result);
				m_Category2Results.addPair(category, result);
			}
		}
		TerminationAnalysisOverallResult[] categoriesOrderedBySignificance = 
				new TerminationAnalysisOverallResult[] {
				TerminationAnalysisOverallResult.EXCEPTION_OR_ERROR,
				TerminationAnalysisOverallResult.SYNTAX_ERROR,
				TerminationAnalysisOverallResult.UNSUPPORTED_SYNTAX,
				TerminationAnalysisOverallResult.NONTERMINATING,
				TerminationAnalysisOverallResult.UNKNOWN,
				TerminationAnalysisOverallResult.TIMEOUT,
				TerminationAnalysisOverallResult.TERMINATING
		};
		
		for (TerminationAnalysisOverallResult category : categoriesOrderedBySignificance) {
			if (m_Category2Results.getDomain().contains(category)) {
				m_OverallResult = category;
			}
		}
		
		if (m_OverallResult == null) {
			m_OverallResult = TerminationAnalysisOverallResult.NO_RESULT;
		} else {
			m_MostSignificantResults = m_Category2Results.getImage(m_OverallResult);
		}
	}
	
	
	private TerminationAnalysisOverallResult detectResultCategory(IResult result) {
		if (result instanceof TerminationAnalysisResult && ((TerminationAnalysisResult) result).getTermination() == TERMINATION.TERMINATING) {
			return TerminationAnalysisOverallResult.TERMINATING;
		} else if (result instanceof TerminationAnalysisResult && ((TerminationAnalysisResult) result).getTermination() == TERMINATION.NONTERMINATING) {
			return TerminationAnalysisOverallResult.NONTERMINATING;
		} else if (result instanceof TerminationAnalysisResult && ((TerminationAnalysisResult) result).getTermination() == TERMINATION.UNKNOWN) {
			return TerminationAnalysisOverallResult.UNKNOWN;
		} else if (result instanceof TypeErrorResult) {
			return TerminationAnalysisOverallResult.SYNTAX_ERROR;
		} else if (result instanceof SyntaxErrorResult) {
			return TerminationAnalysisOverallResult.SYNTAX_ERROR;
		} else if (result instanceof ITimeoutResult) {
			return TerminationAnalysisOverallResult.TIMEOUT;
		} else if (result instanceof UnsupportedSyntaxResult) {
			return TerminationAnalysisOverallResult.UNSUPPORTED_SYNTAX;
		} else if (result instanceof ExceptionOrErrorResult) {
			return TerminationAnalysisOverallResult.EXCEPTION_OR_ERROR;
		} else {
			return null;
		}
	}


	@Override
	public TerminationAnalysisOverallResult getOverallResult() {
		return m_OverallResult;
	}
	
	@Override
	public String generateOverallResultMessage() {
		switch (getOverallResult()) {
		case EXCEPTION_OR_ERROR:
			return getMostSignificantResults().toString();
		case NO_RESULT:
			return getOverallResult().toString();
		case TERMINATING:
			return getMostSignificantResults().toString();
		case SYNTAX_ERROR:
			return getMostSignificantResults().toString();
		case TIMEOUT:
			return getMostSignificantResults().toString();
		case UNKNOWN:
			return concatenateShortDescriptions(getMostSignificantResults());
		case NONTERMINATING:
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