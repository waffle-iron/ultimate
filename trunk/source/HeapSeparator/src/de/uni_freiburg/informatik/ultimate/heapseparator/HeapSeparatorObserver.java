/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * Copyright (C) 2013-2015 Vincent Langenfeld (langenfv@informatik.uni-freiburg.de)
 * 
 * This file is part of the ULTIMATE BuchiProgramProduct plug-in.
 * 
 * The ULTIMATE BuchiProgramProduct plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE BuchiProgramProduct plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE BuchiProgramProduct plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE BuchiProgramProduct plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE BuchiProgramProduct plug-in grant you additional permission
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.heapseparator;

import java.util.HashMap;

import de.uni_freiburg.informatik.ultimate.boogie.DeclarationInformation;
import de.uni_freiburg.informatik.ultimate.boogie.DeclarationInformation.StorageClass;
import de.uni_freiburg.informatik.ultimate.core.model.models.IElement;
import de.uni_freiburg.informatik.ultimate.core.model.models.ModelType;
import de.uni_freiburg.informatik.ultimate.core.model.observers.IUnmanagedObserver;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.irsdependencies.rcfg.walker.ObserverDispatcher;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.irsdependencies.rcfg.walker.ObserverDispatcherSequential;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.irsdependencies.rcfg.walker.RCFGWalkerBreadthFirst;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RCFGNode;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootAnnot;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootNode;

public class HeapSeparatorObserver implements IUnmanagedObserver {

	private final ILogger mLogger;
	
	/**
	 *  arrayId before separation --> pointerId --> arrayId after separation
	 */
	HashMap<IProgramVar, HashMap<IProgramVar, IProgramVar>> mOldArrayToPointerToNewArray;
	
	private ManagedScript mScript;

	public HeapSeparatorObserver(final IUltimateServiceProvider services) {
		mLogger = services.getLoggingService().getLogger(Activator.PLUGIN_ID);
	}

	@Override
	public void finish() throws Throwable {
		// do nothing
	}

	@Override
	public boolean performedChanges() {
		return false;
	}

	public IElement getModel() {
		return null;
	}

	@Override
	public void init(final ModelType modelType, final int currentModelIndex, final int numberOfModels) throws Throwable {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean process(final IElement root) throws Throwable {
		
		mScript = ((RootNode) root).getRootAnnot().getManagedScript();
//		testSetup(((RootNode) root).getOutgoingEdges().get(0).getTarget());
		testSetup(((RootNode) root).getRootAnnot());
		
		
		final ObserverDispatcher od = new ObserverDispatcherSequential(mLogger);
		final RCFGWalkerBreadthFirst walker = new RCFGWalkerBreadthFirst(od, mLogger);
		od.setWalker(walker);

		final HeapSepRcfgVisitor hsv = new HeapSepRcfgVisitor(mLogger, mOldArrayToPointerToNewArray, mScript);
		walker.addObserver(hsv);
		walker.run((RCFGNode) root);
		
		return false;
	}
	
	
	void testSetup(final RootAnnot ra) {
		
		final IProgramVar m = ra.getBoogie2SMT().getBoogie2SmtSymbolTable().getBoogieVar(
				"m",
				new DeclarationInformation(StorageClass.LOCAL, "p"),
				false);
		
		final IProgramVar p = ra.getBoogie2SMT().getBoogie2SmtSymbolTable().getBoogieVar(
				"p",
				new DeclarationInformation(StorageClass.LOCAL, "p"),
				false);

		final IProgramVar q = ra.getBoogie2SMT().getBoogie2SmtSymbolTable().getBoogieVar(
				"q",
				new DeclarationInformation(StorageClass.LOCAL, "p"),
				false);

		final IProgramVar i = ra.getBoogie2SMT().getBoogie2SmtSymbolTable().getBoogieVar(
				"#i",
				new DeclarationInformation(StorageClass.LOCAL, "p"),
				false);

		final IProgramVar j = ra.getBoogie2SMT().getBoogie2SmtSymbolTable().getBoogieVar(
				"#j",
				new DeclarationInformation(StorageClass.LOCAL, "p"),
				false);
		
		final IProgramVar m1 = ra.getBoogie2SMT().getBoogie2SmtSymbolTable().getBoogieVar(
				"m1",
				new DeclarationInformation(StorageClass.LOCAL, "p"),
				false);

		final IProgramVar m2 = ra.getBoogie2SMT().getBoogie2SmtSymbolTable().getBoogieVar(
				"m2",
				new DeclarationInformation(StorageClass.LOCAL, "p"),
				false);
	
		
//		BoogieVar m1 = new LocalBoogieVar("m1", "p",
//				//m.getIType(),
//				null,
//				mscript.variable("m1_tv", m.getTermVariable().getSort()),
//				null,null
////				(ApplicationTerm) mscript.term("m1_dc"),
////				(ApplicationTerm) mscript.term("m1_pc")
//				);
//
//		BoogieVar m2 = new LocalBoogieVar("m2", "p",
//				//m.getIType(),
//				null,
//				mscript.variable("m2_tv", m.getTermVariable().getSort()),
//				null,null
////				(ApplicationTerm) mscript.term("m2_dc"),
////				(ApplicationTerm) mscript.term("m2_pc")
//				);
	
		mOldArrayToPointerToNewArray = new HashMap<>();
		mOldArrayToPointerToNewArray.put(m, new HashMap<IProgramVar, IProgramVar>());
		mOldArrayToPointerToNewArray.get(m).put(p, m1);
		mOldArrayToPointerToNewArray.get(m).put(q, m2);
		
	}
}
