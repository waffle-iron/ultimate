/*
 * Copyright (C) 2013-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE HornClauseGraphBuilder plug-in.
 * 
 * The ULTIMATE HornClauseGraphBuilder plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE HornClauseGraphBuilder plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE HornClauseGraphBuilder plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE HornClauseGraphBuilder plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE HornClauseGraphBuilder plug-in grant you additional permission 
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.plugins.generator.treeautomizer.preferences;

import de.uni_freiburg.informatik.ultimate.core.lib.preferences.UltimatePreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.BaseUltimatePreferenceItem.PreferenceType;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.UltimatePreferenceItem;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SolverBuilder.SolverMode;
import de.uni_freiburg.informatik.ultimate.plugins.generator.treeautomizer.Activator;

public class TreeAutomizerPreferenceInitializer extends UltimatePreferenceInitializer {

	public TreeAutomizerPreferenceInitializer() {
		super(Activator.PLUGIN_ID, Activator.PLUGIN_NAME);
	}

	@Override
	protected UltimatePreferenceItem<?>[] initDefaultPreferences() {
		return new UltimatePreferenceItem<?>[] {
				new UltimatePreferenceItem<SolverMode>(LABEL_Solver,
						DEF_Solver, PreferenceType.Combo, SolverMode.values()),
				new UltimatePreferenceItem<String>(LABEL_ExtSolverCommand,
						DEF_ExtSolverCommand, PreferenceType.String),
				new UltimatePreferenceItem<String>(LABEL_ExtSolverLogic,
						DEF_ExtSolverLogic, PreferenceType.String),
		};
	}

	// some solver commands
	public static final String Z3_NO_EXTENSIONAL_ARRAYS = "z3 SMTLIB2_COMPLIANT=true -memory:1024 -smt2 -in -t:12000 auto_config=false smt.array.extensional=false";
	public static final String Z3_NO_MBQI = "z3 SMTLIB2_COMPLIANT=true -memory:1024 -smt2 -in -t:12000 auto_config=false smt.mbqi=false";
	public static final String Z3_DEFAULT = "z3 SMTLIB2_COMPLIANT=true -memory:1024 -smt2 -in -t:12000";
	public static final String Z3_LOW_TIMEOUT = "z3 SMTLIB2_COMPLIANT=true -memory:1024 -smt2 -in -t:2000";
	public static final String CVC4 = "cvc4 --tear-down-incremental --print-success --lang smt --tlimit-per=12000";
	public static final String Princess = "princess +incremental +stdin -timeout=12000";
	
	
	/*
	 * new preferences that belong to the RCFG Builder 
	 */
	public static final String LABEL_Solver = "SMT solver";
	public static final SolverMode DEF_Solver = SolverMode.External_ModelsAndUnsatCoreMode;
	public static final String LABEL_ExtSolverCommand = "Command for external solver";
	public static final String DEF_ExtSolverCommand = Z3_DEFAULT;

	public static final String LABEL_ExtSolverLogic = "Logic for external solver";
	public static final String DEF_ExtSolverLogic = "AUFNIRA";

	
//				new UltimatePreferenceItem<SolverMode>(LABEL_Solver,
//						DEF_Solver, PreferenceType.Combo, SolverMode.values()),
//				new UltimatePreferenceItem<String>(LABEL_ExtSolverCommand,
//						DEF_ExtSolverCommand, PreferenceType.String),
//				new UltimatePreferenceItem<String>(LABEL_ExtSolverLogic,
//						DEF_ExtSolverLogic, PreferenceType.String),


	// some solver commands
//	public static final String Z3_NO_EXTENSIONAL_ARRAYS = "z3 SMTLIB2_COMPLIANT=true -memory:1024 -smt2 -in -t:12000 auto_config=false smt.array.extensional=false";
//	public static final String Z3_NO_MBQI = "z3 SMTLIB2_COMPLIANT=true -memory:1024 -smt2 -in -t:12000 auto_config=false smt.mbqi=false";
//	public static final String Z3_DEFAULT = "z3 SMTLIB2_COMPLIANT=true -memory:1024 -smt2 -in -t:12000";
//	public static final String Z3_LOW_TIMEOUT = "z3 SMTLIB2_COMPLIANT=true -memory:1024 -smt2 -in -t:2000";
//	public static final String CVC4 = "cvc4 --tear-down-incremental --print-success --lang smt --tlimit-per=12000";
//	public static final String Princess = "princess +incremental +stdin -timeout=12000";
	
	
	/*
	 * new preferences that belong to the RCFG Builder 
	 */
//	public static final String LABEL_Solver = "SMT solver";
//	public static final SolverMode DEF_Solver = SolverMode.External_ModelsAndUnsatCoreMode;
//	public static final String LABEL_ExtSolverCommand = "Command for external solver";
//	public static final String DEF_ExtSolverCommand = Z3_DEFAULT;
//
//	public static final String LABEL_ExtSolverLogic = "Logic for external solver";
//	public static final String DEF_ExtSolverLogic = "AUFNIRA";

	
	
}
