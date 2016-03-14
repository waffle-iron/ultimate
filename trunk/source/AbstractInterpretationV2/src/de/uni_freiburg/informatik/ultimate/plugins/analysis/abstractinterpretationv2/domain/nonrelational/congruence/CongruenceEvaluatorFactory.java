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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.congruence;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceStore;
import de.uni_freiburg.informatik.ultimate.model.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.BooleanValue;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.EvaluatorUtils.EvaluatorType;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.IEvaluator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.IEvaluatorFactory;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.INAryEvaluator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;

/**
 * Implementation of the evaluator factory for evaluators in the {@link CongruenceDomain}.
 * 
 * @author Frank Schüssele (schuessf@informatik.uni-freiburg.de)
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
public class CongruenceEvaluatorFactory
        implements IEvaluatorFactory<CongruenceDomainValue, CongruenceDomainState, CodeBlock, IBoogieVar> {

	private static final int ARITY_MIN = 1;
	private static final int ARITY_MAX = 2;
	private static final int BUFFER_MAX = 100;

	private final Logger mLogger;
	private final String mSettingsEvaluatorType;

	public CongruenceEvaluatorFactory(Logger logger) {
		mLogger = logger;
		final UltimatePreferenceStore ups = new UltimatePreferenceStore(Activator.PLUGIN_ID);
		mSettingsEvaluatorType = ups.getString(CongruenceDomainPreferences.LABEL_EVALUATOR_TYPE);
	}

	@Override
	public INAryEvaluator<CongruenceDomainValue, CongruenceDomainState, CodeBlock, IBoogieVar> createNAryExpressionEvaluator(
	        int arity, EvaluatorType type) {

		assert arity >= ARITY_MIN && arity <= ARITY_MAX;

		switch (arity) {
		case ARITY_MIN:
			return new CongruenceUnaryExpressionEvaluator(mLogger);
		case ARITY_MAX:
			if (mSettingsEvaluatorType.equals(CongruenceDomainPreferences.VALUE_EVALUATOR_DEFAULT)) {
				return new CongruenceBinaryExpressionEvaluator(mLogger, type);
			} else if (mSettingsEvaluatorType.equals(CongruenceDomainPreferences.VALUE_EVALUATOR_OPTIMIZATION)) {
				throw new UnsupportedOperationException("Optimization evaluator is not implemented, yet.");
			} else {
				throw new UnsupportedOperationException(
				        "The evaluator type " + mSettingsEvaluatorType + " is not supported.");
			}
		default:
			final StringBuilder stringBuilder = new StringBuilder(BUFFER_MAX);
			stringBuilder.append("Arity of ").append(arity).append(" is not implemented.");
			throw new UnsupportedOperationException(stringBuilder.toString());
		}
	}

	@Override
	public IEvaluator<CongruenceDomainValue, CongruenceDomainState, CodeBlock, IBoogieVar> createSingletonValueExpressionEvaluator(
	        String value, Class<?> valueType) {
		assert value != null;
		
		return new CongruenceSingletonValueExpressionEvaluator(
		        new CongruenceDomainValue(new BigInteger(value), true));
	}

	@Override
	public IEvaluator<CongruenceDomainValue, CongruenceDomainState, CodeBlock, IBoogieVar> createSingletonVariableExpressionEvaluator(
	        String variableName) {
		assert variableName != null;
		return new CongruenceSingletonVariableExpressionEvaluator(variableName);
	}

	@Override
	public IEvaluator<CongruenceDomainValue, CongruenceDomainState, CodeBlock, IBoogieVar> createSingletonLogicalValueExpressionEvaluator(
	        BooleanValue value) {
		return new CongruenceSingletonBooleanExpressionEvaluator(value);
	}

	@Override
	public IEvaluator<CongruenceDomainValue, CongruenceDomainState, CodeBlock, IBoogieVar> createFunctionEvaluator(
	        String functionName, int inputParamCount) {
		return new CongruenceFunctionEvaluator(functionName, inputParamCount);
	}

	@Override
	public IEvaluator<CongruenceDomainValue, CongruenceDomainState, CodeBlock, IBoogieVar> createConditionalEvaluator() {
		return new CongruenceConditionalEvaluator();
	}

}