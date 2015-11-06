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
public class InterpolationTest_Reach extends AbstractTraceAbstractionTestSuite {

	/** Limit the number of files per directory. */
//	private static int m_FilesPerDirectoryLimit = Integer.MAX_VALUE;
	private static int m_FilesPerDirectoryLimit = 4;
	
	private static final DirectoryFileEndingsPair[] m_DirectoryFileEndingsPairs = {
		/*** Category 1. Arrays ***/
		new DirectoryFileEndingsPair("examples/svcomp/array-examples/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/reducercommutativity/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		
		
		/*** Category 2. Bit Vectors ***/
		new DirectoryFileEndingsPair("examples/svcomp/bitvector/", new String[]{ ".i", ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/bitvector-regression/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/bitvector-loops/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		

		/*** Category 3. Heap Data Structures ***/
		new DirectoryFileEndingsPair("examples/svcomp/heap-manipulation/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/list-properties/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-regression/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ddv-machzwd/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		
		
		/*** Category 5. Control Flow and Integer Variables ***/
		new DirectoryFileEndingsPair("examples/svcomp/ntdrivers-simplified/", new String[]{".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ssh-simplified/", new String[]{".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/locks/", new String[]{".c" }, m_FilesPerDirectoryLimit) ,
		
		new DirectoryFileEndingsPair("examples/svcomp/ntdrivers/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ssh/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,

		new DirectoryFileEndingsPair("examples/svcomp/eca-rers2012/", new String[]{".c" }, m_FilesPerDirectoryLimit) ,
		
		new DirectoryFileEndingsPair("examples/svcomp/loops/", new String[]{".i"}) ,
		new DirectoryFileEndingsPair("examples/svcomp/loop-acceleration/", new String[]{".i" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/loop-invgen/", new String[]{".i"}, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/loop-lit/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/loop-new/", new String[]{".i"}, m_FilesPerDirectoryLimit) ,
		
		new DirectoryFileEndingsPair("examples/svcomp/recursive/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/recursive-simple/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		
		new DirectoryFileEndingsPair("examples/svcomp/product-lines/", new String[]{".c" }, m_FilesPerDirectoryLimit) ,
		
		new DirectoryFileEndingsPair("examples/svcomp/systemc/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/seq-mthreaded/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/seq-pthread/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		
		/*** Category 8. Software Systems ***/
		new DirectoryFileEndingsPair("examples/svcomp/ldv-linux-3.0/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-linux-3.4-simple/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-linux-3.7.3/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-commit-tester/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-consumption/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-linux-3.12-rc1/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-linux-3.16-rc1/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-validator-v0.6/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-validator-v0.8/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-linux-4.2-rc1/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		new DirectoryFileEndingsPair("examples/svcomp/ldv-challenges/", new String[]{ ".c" }, m_FilesPerDirectoryLimit) ,
		
		new DirectoryFileEndingsPair("examples/svcomp/busybox-1.22.0/", new String[]{ ".i" }, m_FilesPerDirectoryLimit) ,
	};
	
	
	private static final String[] m_CurrentBugs = {
//			"examples/svcomp/ntdrivers-simplified/cdaudio_simpl1_true-unreach-call_true-termination.cil.c",
//			"examples/svcomp/ntdrivers-simplified/diskperf_simpl1_true-unreach-call_true-termination.cil.c",
//			"examples/svcomp/ntdrivers-simplified/floppy_simpl3_true-unreach-call_true-termination.cil.c",
//			"examples/svcomp/ntdrivers-simplified/floppy_simpl4_true-unreach-call_true-termination.cil.c",
//			"examples/svcomp/seq-pthread/cs_dekker_true-unreach-call.i",
//			"examples/svcomp/seq-pthread/cs_lamport_true-unreach-call.i",
//			"examples/svcomp/svcomp/eca-rers2012/Problem16_label30_false-unreach-call.c",
//			"examples/svcomp/seq-pthread/cs_dekker_true-unreach-call.i",
//			"examples/svcomp/seq-pthread/cs_lamport_true-unreach-call.i",
			
//			"examples/svcomp/loop-acceleration/diamond_false-unreach-call1.c",
//			"examples/svcomp/loop-acceleration/phases_true-unreach-call2.c",
		};
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTimeout() {
		return 120 * 1000;
	}

	private static final String[] m_Settings = {
		"automizer/interpolation/Reach-32bit-iZ3-NestedInterpolation.epf",
		"automizer/interpolation/Reach-32bit-Princess-TreeInterpolation.epf",
		"automizer/interpolation/Reach-32bit-SMTInterpol-FPandBP.epf",
		"automizer/interpolation/Reach-32bit-SMTInterpol-TreeInterpolation.epf",
		"automizer/interpolation/Reach-32bit-Z3-FPandBP.epf",
		"automizer/interpolation/Reach-32bit-Z3-FP.epf",
	};
	
	private static final String[] m_CToolchains = {
//		"AutomizerC.xml",
		"AutomizerCInline.xml",
	};
	
	
	

	@Override
	public Collection<UltimateTestCase> createTestCases() {
		for (String setting : m_Settings) {
			for (String toolchain : m_CToolchains) {
				addTestCases(toolchain, setting, m_DirectoryFileEndingsPairs);
				addTestCases(toolchain, setting, m_CurrentBugs, new String[] {".c", ".i"});
			}
		}
		return super.createTestCases();
	}

	
}