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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.compound;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.model.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractStateBinaryOperator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;

/**
 * Widening operator of the {@link CompoundDomain}.
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CompoundDomainWideningOperator implements IAbstractStateBinaryOperator<CompoundDomainState> {

	private final Logger mLogger;

	public CompoundDomainWideningOperator(Logger logger) {
		mLogger = logger;
	}

	@Override
	public CompoundDomainState apply(CompoundDomainState first, CompoundDomainState second) {
		final List<IAbstractState<?, CodeBlock, IBoogieVar>> firstStates = first.getAbstractStatesList();
		final List<IAbstractState<?, CodeBlock, IBoogieVar>> secondStates = second.getAbstractStatesList();

		assert firstStates.size() == secondStates.size();
		assert first.getDomainList().size() == second.getDomainList().size();

		final List<IAbstractDomain> domains = first.getDomainList();

		final List<IAbstractState<?, CodeBlock, IBoogieVar>> widenedList = new ArrayList<>();

		for (int i = 0; i < firstStates.size(); i++) {
			widenedList.add(
			        widenInternally(firstStates.get(i), secondStates.get(i), domains.get(i).getWideningOperator()));
		}

		return new CompoundDomainState(mLogger, domains, widenedList);
	}

	private static <T extends IAbstractState> T widenInternally(T firstState, T secondState,
	        IAbstractStateBinaryOperator<T> wideningOperator) {
		return wideningOperator.apply(firstState, secondState);
	}

}