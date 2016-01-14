/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE AbstractInterpretationV2 plug-in.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE AbstractInterpretationV2 plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE AbstractInterpretationV2 plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE AbstractInterpretationV2 plug-in grant you additional permission 
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.util.lpsolver;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.Activator;
import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceStore;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.util.lpsolver.ojalgo.OjAlgoSolver;

/**
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
public class LpSolverFactory {

	private final Logger mLogger;
	private final Class<? extends Number> mType;

	public LpSolverFactory(Logger logger) {
		mLogger = logger;
		final UltimatePreferenceStore ups = new UltimatePreferenceStore(Activator.PLUGIN_ID);
		final String type = ups.getString(LpSolverPreferences.LABEL_LPSOLVER_NUMBER_TYPE);

		if (type.equals(LpSolverPreferences.VALUES_NUMBER_TYPE[0])) {
			mType = BigDecimal.class;
		} else if (type.equals(LpSolverPreferences.VALUES_NUMBER_TYPE[1])) {
			mType = BigInteger.class;
		} else if (type.equals(LpSolverPreferences.VALUES_NUMBER_TYPE[2])) {
			mType = Double.class;
		} else if (type.equals(LpSolverPreferences.VALUES_NUMBER_TYPE[3])) {
			mType = Integer.class;
		} else {
			mType = BigDecimal.class;
		}
	}

	protected ILpSolver<? extends Number> getOjAlgoLpSolver() {
		return new OjAlgoSolver<>(mLogger, mType);
	}

}