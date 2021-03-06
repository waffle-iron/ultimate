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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.vp;

import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractPostOperator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractStateBinaryOperator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootAnnot;

/**
 * This abstract domain keeps track of the variable separation during abstract interpretation.
 *
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * @author Yu-Wen Chen (yuwenchen1105@gmail.com)
 */
public class VPDomain implements IAbstractDomain<VPDomainState, CodeBlock, IBoogieVar> {

	private Map<IProgramVar, Set<PointerExpression>> mPointerMap;
	private Map<IProgramVar, Set<IProgramVar>> mIndexToArraysMap;

	private IAbstractPostOperator<VPDomainState, CodeBlock, IBoogieVar> mPostOperator;

	private final IUltimateServiceProvider mServices;
	private final ILogger mLogger;
	private final RootAnnot mRootAnnotation;

	public VPDomain(final IUltimateServiceProvider services, final RootAnnot rootAnnotation) {
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(Activator.PLUGIN_ID);
		mRootAnnotation = rootAnnotation;
	}

	public VPDomain(final IUltimateServiceProvider services, final Map<IProgramVar, Set<PointerExpression>> pointerMap,
			final Map<IProgramVar, Set<IProgramVar>> indexToArraysMap, final RootAnnot rootAnnotation) {
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(Activator.PLUGIN_ID);
		mPointerMap = pointerMap;
		mIndexToArraysMap = indexToArraysMap;
		mRootAnnotation = rootAnnotation;
	}

	@Override
	public VPDomainState createFreshState() {
		return new VPDomainState();
	}

	@Override
	public IAbstractStateBinaryOperator<VPDomainState> getWideningOperator() {
		return new VPMergeOperator();
	}

	@Override
	public IAbstractStateBinaryOperator<VPDomainState> getMergeOperator() {
		return new VPMergeOperator();
	}

	@Override
	public IAbstractPostOperator<VPDomainState, CodeBlock, IBoogieVar> getPostOperator() {
		if (mPostOperator == null) {
			mPostOperator = new VPPostOperator(mServices);
		}
		return mPostOperator;
	}

	@Override
	public int getDomainPrecision() {
		// TODO Fill with sense.
		return 0;
	}

	public Map<IProgramVar, Set<PointerExpression>> getPointerMap() {
		return mPointerMap;
	}

	public void setPointerMap(final Map<IProgramVar, Set<PointerExpression>> pointerMap) {
		mPointerMap = pointerMap;
	}

	public Map<IProgramVar, Set<IProgramVar>> getIndexToArraysMap() {
		return mIndexToArraysMap;
	}

	public void setIndexToArraysMap(final Map<IProgramVar, Set<IProgramVar>> indexToArraysMap) {
		mIndexToArraysMap = indexToArraysMap;
	}
}
