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

package de.uni_freiburg.informatik.ultimatetest.suites.evals;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_freiburg.informatik.ultimate.test.UltimateRunDefinition;
import de.uni_freiburg.informatik.ultimate.test.UltimateRunDefinitionGenerator;
import de.uni_freiburg.informatik.ultimate.test.UltimateTestCase;
import de.uni_freiburg.informatik.ultimate.test.decider.ITestResultDecider;
import de.uni_freiburg.informatik.ultimate.test.decider.NoTimeoutTestResultDecider;
import de.uni_freiburg.informatik.ultimatetest.suites.AbstractEvalTestSuite;
import de.uni_freiburg.informatik.ultimatetest.summaries.ColumnDefinition;
import de.uni_freiburg.informatik.ultimatetest.summaries.ColumnDefinition.Aggregate;
import de.uni_freiburg.informatik.ultimatetest.summaries.ConversionContext;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class PreprocessingBugs extends AbstractEvalTestSuite {
	private static final String[] ALL_C = new String[] { ".c", ".i" };
	private static final String[] BPL = new String[] { ".bpl" };

	private static Collection<UltimateRunDefinition> createDefs() {
		final Collection<UltimateRunDefinition> rtr = new ArrayList<>();

		rtr.addAll(translateAndEncodeSV("array-memsafety/openbsd_cmemchr-alloca_true-valid-memsafety.i"));

		return rtr;
	}

	@Override
	protected long getTimeout() {
		return 60 * 1000L;
	}

	@Override
	protected ColumnDefinition[] getColumnDefinitions() {
		// @formatter:off
		return new ColumnDefinition[] {
				new ColumnDefinition("Runtime (ns)", "Total time", ConversionContext.Divide(1000000000, 2, " s"),
						Aggregate.Sum, Aggregate.Average),
				new ColumnDefinition("Allocated memory end (bytes)", "Alloc. Memory",
						ConversionContext.Divide(1048576, 2, " MB"), Aggregate.Max, Aggregate.Average),
				new ColumnDefinition("Peak memory consumption (bytes)", "Peak Memory",
						ConversionContext.Divide(1048576, 2, " MB"), Aggregate.Max, Aggregate.Average), };
		// @formatter:on
	}

	@Override
	public ITestResultDecider constructITestResultDecider(UltimateRunDefinition urd) {
		return new NoTimeoutTestResultDecider(urd);
	}

	/**
	 * Create test cases from quadruples (toolchain, fileendings, settingsfile, inputfiles)
	 */
	@Override
	public Collection<UltimateTestCase> createTestCases() {
		addTestCase(createDefs());
		return super.createTestCases();
	}

	private static String sv(final String path) {
		return "examples/svcomp/" + path;
	}

	private static Collection<UltimateRunDefinition> translateSV(String example) {
		return translate(sv(example));
	}

	private static Collection<UltimateRunDefinition> translate(String example) {
		return UltimateRunDefinitionGenerator.getRunDefinitionFromTrunk(new String[] { example }, ALL_C,
				"svcomp2016/svcomp-Deref-32bit-Automizer_Default.epf",
				"PreprocessingC.xml");
	}
	
	private static Collection<UltimateRunDefinition> translateAndEncodeSV(String example) {
		return translateAndEncode(sv(example));
	}

	private static Collection<UltimateRunDefinition> translateAndEncode(String example) {
		return UltimateRunDefinitionGenerator.getRunDefinitionFromTrunk(new String[] { example }, ALL_C,
				"svcomp2016/svcomp-Deref-32bit-Automizer_Default.epf",
				"PreprocessingCWithBE.xml");
	}
}
