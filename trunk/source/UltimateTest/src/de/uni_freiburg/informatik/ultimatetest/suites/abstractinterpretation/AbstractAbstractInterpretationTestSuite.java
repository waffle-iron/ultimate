/**
 * 
 */
package de.uni_freiburg.informatik.ultimatetest.suites.abstractinterpretation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.uni_freiburg.informatik.ultimatetest.UltimateRunDefinition;
import de.uni_freiburg.informatik.ultimatetest.UltimateStarter;
import de.uni_freiburg.informatik.ultimatetest.UltimateTestCase;
import de.uni_freiburg.informatik.ultimatetest.UltimateTestSuite;
import de.uni_freiburg.informatik.ultimatetest.decider.AbstractInterpretationTestResultDecider;
import de.uni_freiburg.informatik.ultimatetest.reporting.IIncrementalLog;
import de.uni_freiburg.informatik.ultimatetest.reporting.ITestSummary;
import de.uni_freiburg.informatik.ultimatetest.summaries.AbstractInterpretationComparisonTestSummary;
import de.uni_freiburg.informatik.ultimatetest.summaries.AbstractInterpretationLaTeXTestSummary;
import de.uni_freiburg.informatik.ultimatetest.summaries.AbstractInterpretationTestSummary;
import de.uni_freiburg.informatik.ultimatetest.util.TestUtil;

/**
 * Stolen from AbstractTraceAbstractionTestSuite ;-)
 */
public abstract class AbstractAbstractInterpretationTestSuite extends UltimateTestSuite {
	private List<UltimateTestCase> m_testCases;

	private static final String m_PathToSettings = "examples/settings/";
	private static final String m_PathToToolchains = "examples/toolchains/";

	@Override
	protected ITestSummary[] constructTestSummaries() {
		return new ITestSummary[] { new AbstractInterpretationTestSummary(this.getClass()),
				new AbstractInterpretationLaTeXTestSummary(this.getClass()),
				new AbstractInterpretationComparisonTestSummary(this.getClass()) };
	}
	
	@Override
	protected IIncrementalLog[] constructIncrementalLog() {
		return new IIncrementalLog[0];
	}

	@Override
	public Collection<UltimateTestCase> createTestCases() {
		return m_testCases;
	}

	protected void addTestCases(File toolchainFile, File settingsFile, Collection<File> inputFiles, String description,
			String uniqueString, long deadline) {
		if (m_testCases == null) {
			m_testCases = new ArrayList<UltimateTestCase>();
		}

		for (File inputFile : inputFiles) {
			UltimateRunDefinition urd = new UltimateRunDefinition(inputFile, settingsFile, toolchainFile);
			UltimateStarter starter = new UltimateStarter(urd, deadline, null, null);
			m_testCases.add(new UltimateTestCase(uniqueString + "_" + inputFile.getAbsolutePath(),
					new AbstractInterpretationTestResultDecider(inputFile, uniqueString), starter, urd, super
							.getSummaries(), null));
		}
	}

	/**
	 * @param toolchain
	 * @param settings
	 * @param directory
	 * @param fileEndings
	 * @param description
	 * @param uniqueString
	 * @param deadline
	 */
	protected void addTestCases(String toolchain, String settings, String[] directories, String[] fileEndings,
			String description, String uniqueString, long deadline) {

		File toolchainFile = new File(TestUtil.getPathFromTrunk(m_PathToToolchains + toolchain));
		File settingsFile = new File(TestUtil.getPathFromTrunk(m_PathToSettings + settings));
		Collection<File> testFiles = new ArrayList<File>();
		for (String directory : directories) {
			testFiles.addAll(getInputFiles(directory, fileEndings));
		}
		addTestCases(toolchainFile, settingsFile, testFiles, description, uniqueString, deadline);
	}

	private Collection<File> getInputFiles(String directory, String[] fileEndings) {
		return TestUtil.getFiles(new File(TestUtil.getPathFromTrunk(directory)), fileEndings);
	}


}