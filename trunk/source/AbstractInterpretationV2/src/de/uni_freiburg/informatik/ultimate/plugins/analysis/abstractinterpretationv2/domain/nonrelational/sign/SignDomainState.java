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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.sign;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.BooleanValue;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.NonrelationalState;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.sign.SignDomainValue.SignValues;

/**
 * Implementation of an abstract state of the {@link SignDomain}.
 * 
 * <p>
 * Such a state stores {@link SignDomainValue}s.
 * </p>
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 * @param <ACTION>
 *            Any action.
 * @param <IBoogieVar>
 *            Any variable declaration.
 */
public class SignDomainState extends NonrelationalState<SignDomainState, SignDomainValue> {

	protected SignDomainState(final ILogger logger) {
		super(logger);
	}

	protected SignDomainState(final ILogger logger, final Set<IBoogieVar> variables,
			final Map<IBoogieVar, SignDomainValue> valuesMap, final Map<IBoogieVar, BooleanValue> booleanValuesMap) {
		super(logger, variables, valuesMap, booleanValuesMap);
	}

	@Override
	protected SignDomainState createCopy() {
		return new SignDomainState(getLogger(), getVariables(), new HashMap<>(getVar2ValueNonrelational()),
				new HashMap<>(getVar2ValueBoolean()));
	}

	@Override
	protected SignDomainState createState(ILogger logger, Set<IBoogieVar> newVarMap,
			Map<IBoogieVar, SignDomainValue> newValMap, Map<IBoogieVar, BooleanValue> newBooleanValMap) {
		return new SignDomainState(logger, newVarMap, newValMap, newBooleanValMap);
	}

	@Override
	protected SignDomainValue createBottomValue() {
		return new SignDomainValue(SignValues.BOTTOM);
	}

	@Override
	protected SignDomainValue createTopValue() {
		return new SignDomainValue(SignValues.TOP);
	}

	@Override
	protected SignDomainValue[] getArray(int size) {
		return new SignDomainValue[size];
	}

}
