/*
 * Copyright (C) 2016 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2016 University of Freiburg
 * 
 * This file is part of the ULTIMATE WitnessPrinter plug-in.
 * 
 * The ULTIMATE WitnessPrinter plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE WitnessPrinter plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE WitnessPrinter plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE WitnessPrinter plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE WitnessPrinter plug-in grant you additional permission 
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.witnessprinter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.core.services.model.IBacktranslatedCFG;
import de.uni_freiburg.informatik.ultimate.model.structure.IExplicitEdgesMultigraph;
import de.uni_freiburg.informatik.ultimate.model.structure.IMultigraphEdge;
import de.uni_freiburg.informatik.ultimate.result.AtomicTraceElement;
import de.uni_freiburg.informatik.ultimate.result.model.IBacktranslationValueProvider;
import de.uni_freiburg.informatik.ultimate.witnessprinter.graphml.GeneratedWitnessEdge;
import de.uni_freiburg.informatik.ultimate.witnessprinter.graphml.GeneratedWitnessNode;
import de.uni_freiburg.informatik.ultimate.witnessprinter.graphml.GeneratedWitnessNodeEdgeFactory;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.io.GraphMLWriter;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class CorrectnessWitnessGenerator<TTE, TE> extends BaseWitnessGenerator<TTE, TE> {

	private final Logger mLogger;
	private final IBacktranslationValueProvider<TTE, TE> mStringProvider;
	private final IBacktranslatedCFG<String, TTE> mTranslatedCFG;

	public CorrectnessWitnessGenerator(final IBacktranslatedCFG<String, TTE> translatedCFG,
			final IBacktranslationValueProvider<TTE, TE> provider, final Logger logger) {
		super();
		mLogger = logger;
		mStringProvider = provider;
		mTranslatedCFG = translatedCFG;
	}

	@Override
	public String makeGraphMLString() {
		final GraphMLWriter<GeneratedWitnessNode, GeneratedWitnessEdge<TTE, TE>> graphWriter = new GraphMLWriter<>();
		final String filename = StringEscapeUtils.escapeXml10(mTranslatedCFG.getFilename());

		graphWriter.setEdgeIDs(new Transformer<GeneratedWitnessEdge<TTE, TE>, String>() {
			@Override
			public String transform(GeneratedWitnessEdge<TTE, TE> arg0) {
				return arg0.getName();
			}
		});

		graphWriter.setVertexIDs(new Transformer<GeneratedWitnessNode, String>() {
			@Override
			public String transform(GeneratedWitnessNode arg0) {
				return arg0.getName();
			}
		});

		addGraphData(graphWriter, "sourcecodelang", null, graph -> "C");
		addGraphData(graphWriter, "witness-type", null, graph -> "correctness_witness");

		addEdgeData(graphWriter, "sourcecode", null, edge -> StringEscapeUtils.escapeXml10(edge.getSourceCode()));
		addEdgeData(graphWriter, "assumption", null, edge -> StringEscapeUtils.escapeXml10(edge.getAssumption()));
		addEdgeData(graphWriter, "tokens", null, edge -> null);
		addEdgeData(graphWriter, "control", null, edge -> edge.getControl());
		addEdgeData(graphWriter, "startline", null, edge -> edge.getStartLineNumber());
		addEdgeData(graphWriter, "endline", null, edge -> edge.getEndLineNumber());
		addEdgeData(graphWriter, "originfile", filename, edge -> null);
		addEdgeData(graphWriter, "enterFunction", null, edge -> null);
		addEdgeData(graphWriter, "returnFrom", null, edge -> null);

		addVertexData(graphWriter, "nodetype", "path", vertex -> null);
		addVertexData(graphWriter, "entry", "false", vertex -> vertex.isEntry() ? "true" : null);
		addVertexData(graphWriter, "violation", "false", vertex -> vertex.isError() ? "true" : null);
		// TODO: When we switch to "multi-property" witnesses, we write invariants for FALSE-witnesses
		addVertexData(graphWriter, "invariant", "true", vertex -> StringEscapeUtils.escapeXml10(vertex.getInvariant()));

		final Hypergraph<GeneratedWitnessNode, GeneratedWitnessEdge<TTE, TE>> graph = getGraph();
		final StringWriter writer = new StringWriter();
		try {
			graphWriter.save(graph, writer);
		} catch (final IOException e) {
			mLogger.error("Could not save witness graph: " + e.getMessage());
		}
		try {
			writer.flush();
			return writer.toString();
		} finally {
			try {
				writer.close();
			} catch (final IOException e) {
				mLogger.error("Could not close witness writer: " + e.getMessage());
			}
		}
	}

	private Hypergraph<GeneratedWitnessNode, GeneratedWitnessEdge<TTE, TE>> getGraph() {
		final DirectedSparseGraph<GeneratedWitnessNode, GeneratedWitnessEdge<TTE, TE>> graph = new OrderedDirectedSparseGraph<>();

		final GeneratedWitnessNodeEdgeFactory<TTE, TE> fac = new GeneratedWitnessNodeEdgeFactory<TTE, TE>(
				mStringProvider);

		final IExplicitEdgesMultigraph<?, ?, String, TTE> root = mTranslatedCFG.getCFG();

		final Deque<IExplicitEdgesMultigraph<?, ?, String, TTE>> worklist = new ArrayDeque<>();
		final Map<IExplicitEdgesMultigraph<?, ?, String, TTE>, GeneratedWitnessNode> nodecache = new HashMap<>();
		final Set<IMultigraphEdge<?, ?, String, TTE>> closed = new HashSet<>();

		GeneratedWitnessNode rootWitnessNode = fac.createInitialWitnessNode();
		nodecache.put(root, rootWitnessNode);
		graph.addVertex(rootWitnessNode);

		// initial edges are different
		for (final IMultigraphEdge<?, ?, String, TTE> outgoing : root.getOutgoingEdges()) {
			final GeneratedWitnessEdge<TTE, TE> edge = fac.createDummyWitnessEdge();
			final GeneratedWitnessNode node = getWitnessNode(outgoing.getTarget(), mStringProvider, fac, nodecache);
			graph.addEdge(edge, rootWitnessNode, node);
			closed.add(outgoing);
			worklist.add(outgoing.getTarget());
		}

		// now for the rest
		while (!worklist.isEmpty()) {
			final IExplicitEdgesMultigraph<?, ?, String, TTE> sourceNode = worklist.remove();
			final GeneratedWitnessNode sourceWNode = getWitnessNode(sourceNode, mStringProvider, fac, nodecache);

			for (final IMultigraphEdge<?, ?, String, TTE> outgoing : sourceNode.getOutgoingEdges()) {
				if (!closed.add(outgoing)) {
					continue;
				}
				final TTE label = outgoing.getLabel();
				final GeneratedWitnessEdge<TTE, TE> edge;
				if (label == null) {
					edge = fac.createDummyWitnessEdge();
				} else {
					edge = fac.createWitnessEdge(new AtomicTraceElement<>(label));
				}
				final GeneratedWitnessNode targetWNode = getWitnessNode(outgoing.getTarget(), mStringProvider, fac,
						nodecache);

				graph.addEdge(edge, sourceWNode, targetWNode);
				worklist.add(outgoing.getTarget());
			}
		}

		return graph;
	}

	private GeneratedWitnessNode getWitnessNode(final IExplicitEdgesMultigraph<?, ?, String, TTE> node,
			final IBacktranslationValueProvider<TTE, TE> stringProvider,
			final GeneratedWitnessNodeEdgeFactory<TTE, TE> fac,
			final Map<IExplicitEdgesMultigraph<?, ?, String, TTE>, GeneratedWitnessNode> nodecache) {
		GeneratedWitnessNode wnode = nodecache.get(node);
		if (wnode != null) {
			return wnode;
		}

		wnode = fac.createWitnessNode();
		nodecache.put(node, wnode);
		final String invariant = node.getLabel();
		if (invariant == null) {
			return wnode;
		}

		wnode.setInvariant(invariant);

		return wnode;
	}
}