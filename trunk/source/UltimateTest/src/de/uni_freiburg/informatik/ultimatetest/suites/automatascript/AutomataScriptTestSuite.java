/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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
package de.uni_freiburg.informatik.ultimatetest.suites.automatascript;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.automata.AutomataOperationStatistics;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.AbstractCegarLoop.CegarLoopStatisticsDefinitions;
import de.uni_freiburg.informatik.ultimate.test.UltimateRunDefinition;
import de.uni_freiburg.informatik.ultimate.test.UltimateStarter;
import de.uni_freiburg.informatik.ultimate.test.UltimateTestCase;
import de.uni_freiburg.informatik.ultimate.test.UltimateTestSuite;
import de.uni_freiburg.informatik.ultimate.test.decider.AutomataScriptTestResultDecider;
import de.uni_freiburg.informatik.ultimate.test.reporting.CsvConcatenator;
import de.uni_freiburg.informatik.ultimate.test.reporting.IIncrementalLog;
import de.uni_freiburg.informatik.ultimate.test.reporting.ITestSummary;
import de.uni_freiburg.informatik.ultimate.test.util.TestUtil;
import de.uni_freiburg.informatik.ultimate.util.csv.ICsvProviderProvider;
import de.uni_freiburg.informatik.ultimatetest.summaries.AutomataScriptTestSummary;
import de.uni_freiburg.informatik.ultimatetest.summaries.ColumnDefinition;
import de.uni_freiburg.informatik.ultimatetest.summaries.ColumnDefinition.Aggregate;
import de.uni_freiburg.informatik.ultimatetest.summaries.ConversionContext;
import de.uni_freiburg.informatik.ultimatetest.summaries.LatexOverviewSummary;

public class AutomataScriptTestSuite extends UltimateTestSuite {

	private static final String mToolchain = "examples/toolchains/AutomataScriptInterpreter.xml";
	private static final File mToolchainFile = new File(TestUtil.getPathFromTrunk(mToolchain));
	private static int mTimeout = 10 * 1000;
	private static final String[] mDirectories = { "examples/Automata/AlternatingAutomata",
			"examples/Automata/atsTestFiles", "examples/Automata/AUTOMATA_SCRIPT", "examples/Automata/BuchiAutomata",
			"examples/Automata/BuchiNwa", "examples/Automata/finiteAutomata", "examples/Automata/nwa",
			"examples/Automata/nwaOperations",
			// "examples/Automata/BuchiNwa/reduceBuchiNwa/",
			// "examples/Automata/nwaOperations/minimizeMaxSAT/",
			// the following two have still bugs
			// "examples/Automata/PetriNet",
			// "examples/Automata/senwa",
			// the following is not yet tested
			// "examples/Automata/syntaxError",
	};
	private static final String[] mFileEndings = { ".ats" };

	@Override
	protected ITestSummary[] constructTestSummaries() {
		final ArrayList<Class<? extends ICsvProviderProvider<? extends Object>>> benchmarks =
				new ArrayList<Class<? extends ICsvProviderProvider<? extends Object>>>();

		final ColumnDefinition[] columnDef = new ColumnDefinition[] {
				new ColumnDefinition(CegarLoopStatisticsDefinitions.OverallTime.toString(), "Avg. runtime",
						ConversionContext.Divide(1000000000, 2, " s"), Aggregate.Sum, Aggregate.Average), };

		return new ITestSummary[] { new AutomataScriptTestSummary(this.getClass()),
				new CsvConcatenator(this.getClass(), AutomataOperationStatistics.class),
				new LatexOverviewSummary(getClass(), benchmarks, columnDef), };
	}

	@Override
	protected IIncrementalLog[] constructIncrementalLog() {
		return new IIncrementalLog[0];
	}

	@Override
	public Collection<UltimateTestCase> createTestCases() {
		final List<UltimateTestCase> testCases = new ArrayList<UltimateTestCase>();

		final Collection<File> inputFiles = new ArrayList<File>();
		for (final String directory : mDirectories) {
			inputFiles.addAll(getInputFiles(directory, mFileEndings));
		}

		for (final File inputFile : inputFiles) {
			final File settingsFile = null;
			final UltimateRunDefinition urd = new UltimateRunDefinition(inputFile, settingsFile, mToolchainFile);
			final UltimateStarter starter = new UltimateStarter(urd, mTimeout);
			final UltimateTestCase utc = new UltimateTestCase(urd.generateShortStringRepresentation(),
					new AutomataScriptTestResultDecider(), starter,
					// mDescription + "_" + inputFile.getAbsolutePath(),
					urd, super.getSummaries(), null);
			testCases.add(utc);
		}
		testCases.sort(null);
		return testCases;
	}

	private Collection<File> getInputFiles(final String directory, final String[] fileEndings) {
		return TestUtil.getFiles(new File(TestUtil.getPathFromTrunk(directory)), fileEndings);
	}

}
