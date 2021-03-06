/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Test Library.
 * 
 * The ULTIMATE Test Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Test Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Test Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Test Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Test Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimatetest.summaries;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.uni_freiburg.informatik.ultimate.core.model.services.IResultService;
import de.uni_freiburg.informatik.ultimate.test.UltimateRunDefinition;
import de.uni_freiburg.informatik.ultimate.test.UltimateTestSuite;
import de.uni_freiburg.informatik.ultimate.test.decider.ITestResultDecider.TestResult;
import de.uni_freiburg.informatik.ultimate.test.reporting.ITestSummary;
import de.uni_freiburg.informatik.ultimate.test.util.TestUtil;
import de.uni_freiburg.informatik.ultimate.util.statistics.Benchmark;

public class AutomataScriptTestSummary implements ITestSummary {
	
	private final Class<? extends UltimateTestSuite> mUltimateTestSuite;
	private final List<SummaryEntry> mResults;

	public AutomataScriptTestSummary(Class<? extends UltimateTestSuite> ultimateTestSuite) {
		mUltimateTestSuite = ultimateTestSuite;
		mResults = new ArrayList<SummaryEntry>();
	}
	
	@Override
	public Class<? extends UltimateTestSuite> getUltimateTestSuiteClass() {
		return mUltimateTestSuite;
	}
	
	@Override
	public String getDescriptiveLogName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public String getFilenameExtension() {
		return ".log";
	}

	@Override
	public String getSummaryLog() {
		final StringBuilder sb = new StringBuilder();
		sb.append("################# ");
		sb.append(mUltimateTestSuite);
		sb.append(" #################");
		sb.append("\n");
		for (final SummaryEntry summaryEntry  : mResults) {
			sb.append(summaryEntry.getTestResult().toString());
			sb.append("\t");
			sb.append(String.format( "%.2f", summaryEntry.getTime()) + "s");
			sb.append("\t");
			sb.append(summaryEntry.getMessage());
			sb.append("\t");
			sb.append(summaryEntry.getAtsFile().getAbsolutePath());
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public void addResult(UltimateRunDefinition ultimateRunDefinition, TestResult threeValuedResult,
			String category, String message, String testname, IResultService resultService) {
		final Collection<Benchmark> benchmarkSingleton = TestUtil.getCsvProviderProviderFromUltimateResults(resultService.getResults(), Benchmark.class);
		if (benchmarkSingleton.size() != 1) {
			throw new AssertionError("expected single benchmark result");
		} else {
			final Benchmark benchmark = benchmarkSingleton.iterator().next();
			final double time = benchmark.getElapsedTime(de.uni_freiburg.informatik.ultimate.plugins.generator.automatascriptinterpreter.Activator.PLUGIN_NAME, TimeUnit.SECONDS);
			mResults.add(new SummaryEntry(threeValuedResult, message, time, ultimateRunDefinition.getInput()[0]));
		}
		

	}
	
	private class SummaryEntry {
		private final TestResult mTestResult;
		private final String mMessage;
		private final double mTime;
		private final File mAtsFile;
		public SummaryEntry(TestResult testResult, String message, double time,
				File atsFile) {
			super();
			mTestResult = testResult;
			mMessage = message;
			mTime = time;
			mAtsFile = atsFile;
		}
		public TestResult getTestResult() {
			return mTestResult;
		}
		public String getMessage() {
			return mMessage;
		}
		public double getTime() {
			return mTime;
		}
		public File getAtsFile() {
			return mAtsFile;
		}
	}
}
