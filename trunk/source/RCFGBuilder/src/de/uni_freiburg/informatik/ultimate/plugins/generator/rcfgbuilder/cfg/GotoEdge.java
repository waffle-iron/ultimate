/*
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE RCFGBuilder plug-in.
 * 
 * The ULTIMATE RCFGBuilder plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE RCFGBuilder plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE RCFGBuilder plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE RCFGBuilder plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE RCFGBuilder plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IInternalAction;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.UnmodifiableTransFormula;

/**
 * Represents an edge without any effect to the programs variables. While
 * constructing the CFG of a Boogie program these edges are used temporarily
 * but do not occur in the result.
 * 
 * @author heizmann@informatik.uni-freiburg.de
 * 
 */
public class GotoEdge extends CodeBlock implements IInternalAction {

	private static final long serialVersionUID = -2923506946454722306L;

	GotoEdge(int serialNumber, ProgramPoint source, ProgramPoint target, ILogger logger) {
		super(serialNumber, source, target, logger);
		assert (target != null);
	}

	@Override
	public String getPrettyPrintedStatements() {
		if (mTarget == null) {
			return "disconnected goto";
		} else {
			return "goto " + mTarget.toString();
		}
	}

	@Override
	protected String[] getFieldNames() {
		return new String[] {};
	}

	@Override
	public String toString() {
		return "goto;";
	}

	@Override
	public UnmodifiableTransFormula getTransformula() {
		return getTransitionFormula();
	}

}
