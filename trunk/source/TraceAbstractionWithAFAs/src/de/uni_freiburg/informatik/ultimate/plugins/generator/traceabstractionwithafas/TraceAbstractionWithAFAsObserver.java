package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstractionwithafas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.access.BaseObserver;
import de.uni_freiburg.informatik.ultimate.automata.ExampleNWAFactory;
import de.uni_freiburg.informatik.ultimate.core.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.model.IElement;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ProgramPoint;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootAnnot;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootNode;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.AbstractCegarLoop.Result;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.TraceAbstractionBenchmarks;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TAPreferences;

/**
 * Auto-Generated Stub for the plug-in's Observer
 */
public class TraceAbstractionWithAFAsObserver extends BaseObserver {

	/**
	 * Root Node of this Ultimate model. I use this to store information that
	 * should be passed to the next plugin. The Successors of this node exactly
	 * the initial nodes of procedures.
	 */
	private IElement m_graphroot = null;

	private final IUltimateServiceProvider mServices;

	public TraceAbstractionWithAFAsObserver(IUltimateServiceProvider services) {
		mServices = services;
	}

	@Override
	public boolean process(IElement root) {

		RootNode rootNode = (RootNode) root;
		RootAnnot rootAnnot = rootNode.getRootAnnot();
		SmtManager smtManager = new SmtManager(rootAnnot.getBoogie2SMT(), rootAnnot.getModGlobVarManager(), mServices);
		TraceAbstractionBenchmarks taBenchmarks = new TraceAbstractionBenchmarks(rootAnnot);
		TAPreferences taPrefs = new TAPreferences();

		// TODO: Now you can get instances of your library classes for the
		// current toolchain like this:
		// NWA is nevertheless very broken, as its static initialization
		// prevents parallelism
		// Surprisingly, this call lazily initializes the static fields of NWA
		// Lib and, like magic, the toolchain works ...
		mServices.getServiceInstance(ExampleNWAFactory.class);

		Map<String, Collection<ProgramPoint>> proc2errNodes = rootAnnot.getErrorNodes();
		Collection<ProgramPoint> errNodesOfAllProc = new ArrayList<ProgramPoint>();
		for (Collection<ProgramPoint> errNodeOfProc : proc2errNodes.values()) {
			errNodesOfAllProc.addAll(errNodeOfProc);
		}

		TAwAFAsCegarLoop cegarLoop = new TAwAFAsCegarLoop("bla", rootNode, smtManager, taBenchmarks, taPrefs,
				errNodesOfAllProc, taPrefs.interpolation(), taPrefs.computeHoareAnnotation(), mServices);

		Result result = cegarLoop.iterate();

		return false;
	}

	/**
	 * @return the root of the CFG.
	 */
	public IElement getRoot() {
		return m_graphroot;
	}

}
