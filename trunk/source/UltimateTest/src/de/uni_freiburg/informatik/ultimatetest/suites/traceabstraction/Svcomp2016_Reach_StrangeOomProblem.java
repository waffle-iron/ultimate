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
/**
 * 
 */
package de.uni_freiburg.informatik.ultimatetest.suites.traceabstraction;

import java.util.Collection;

import de.uni_freiburg.informatik.ultimatetest.UltimateTestCase;

/**
 * @author heizmann@informatik.uni-freiburg.de
 * 
 */
public class Svcomp2016_Reach_StrangeOomProblem extends AbstractTraceAbstractionTestSuite {

	
	private static final String[] m_Input = {
			"examples/svcomp/ldv-linux-3.16-rc1/205_9a_array_safes_linux-3.16-rc1.tar.xz-205_9a-drivers--net--wireless--libertas_tf--libertas_tf.ko-entry_point_true-unreach-call.cil.out.c"
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTimeout() {
		return 300 * 1000;
	}

	
	private static final String[] m_Settings_64bit = {
		"svcomp2016/svcomp-Reach-64bit-Automizer_Default.epf",
	};

	
	private static final String[] m_CToolchains = {
		"AutomizerCInline.xml",
	};
	
	
	

	@Override
	public Collection<UltimateTestCase> createTestCases() {
		
		for (String setting : m_Settings_64bit) {
			for (String toolchain : m_CToolchains) {
				addTestCases(toolchain, setting, m_Input, new String[] {".c", ".i"});
			}
		}
		return super.createTestCases();
	}

	
}