/*
 * Copyright (C) 2012-2014 University of Freiburg
 *
 * This file is part of the ULTIMATE LassoRanker Library.
 *
 * The ULTIMATE LassoRanker Library is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The ULTIMATE LassoRanker Library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE LassoRanker Library. If not,
 * see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE LassoRanker Library, or any covered work, by
 * linking or combining it with Eclipse RCP (or a modified version of
 * Eclipse RCP), containing parts covered by the terms of the Eclipse Public
 * License, the licensors of the ULTIMATE LassoRanker Library grant you
 * additional permission to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.lassoranker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.uni_freiburg.informatik.ultimate.core.services.IToolchainStorage;
import de.uni_freiburg.informatik.ultimate.core.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.LoggingScript;
import de.uni_freiburg.informatik.ultimate.logic.SMTLIBException;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SolverBuilder;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SolverBuilder.Settings;

/**
 * Static class that manages SMT-Solver related things.
 * 
 * @author Jan Leike
 */
public class SMTSolver {




	/**
	 * Create a new solver instance with the preferences given
	 * 
	 * @param preferences
	 *            the preferences for creating the solver
	 * @param constraintsName
	 *            name of the constraints whose satisfiability is checked
	 * @param services
	 * @param storage
	 * @return the new solver instance
	 * @throws IOException 
	 */
	public static Script newScript(LassoRankerPreferences preferences, String constraintsName,
			IUltimateServiceProvider services, IToolchainStorage storage) {
		Settings settings = preferences.getSolverConstructionSettings(constraintsName);
		Script script = SolverBuilder.buildScript(services, storage, settings);
		
		// Set options
		script.setOption(":produce-models", true);
		if (preferences.annotate_terms) {
			script.setOption(":produce-unsat-cores", true);
		}
		return script;
		
	}



}