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

import java.util.List;

import de.uni_freiburg.informatik.ultimate.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IStorable;
import de.uni_freiburg.informatik.ultimate.core.model.services.IToolchainStorage;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.ModifiableGlobalVariableManager;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.SimplicationTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.XnfConversionTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.StatementSequence.Origin;

/**
 * Factory for the construction of CodeBlocks. Every CodeBlock has to be constructed via this factory, because every
 * CodeBlock need a unique serial number. Every control flow graph has to provide one CodeBlockFactory
 *
 * @author Matthias Heizmann
 *
 */
public class CodeBlockFactory implements IStorable {

	private final IUltimateServiceProvider mServices;
	private final ILogger mLogger;
	private final ManagedScript mMgdScript;
	private final ModifiableGlobalVariableManager mMgvManager;
	private int mSerialNumberCounter = 0;
	private final Boogie2SmtSymbolTable mSymbolTable;

	public CodeBlockFactory(final IUltimateServiceProvider services, final ManagedScript mgdScript,
			final ModifiableGlobalVariableManager mgvManager, final Boogie2SmtSymbolTable symbolTable) {
		super();
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(Activator.PLUGIN_ID);
		mMgdScript = mgdScript;
		mMgvManager = mgvManager;
		mSymbolTable = symbolTable;
	}

	public Call constructCall(final ProgramPoint source, final ProgramPoint target, final CallStatement call) {
		return new Call(mSerialNumberCounter++, source, target, call, mLogger);
	}

	public InterproceduralSequentialComposition constuctInterproceduralSequentialComposition(final ProgramPoint source,
			final ProgramPoint target, final boolean simplify, final boolean extPqe, final List<CodeBlock> codeBlocks,
			final XnfConversionTechnique xnfConversionTechnique, final SimplicationTechnique simplificationTechnique) {
		return new InterproceduralSequentialComposition(mSerialNumberCounter++, source, target, mMgdScript, mMgvManager,
				simplify, extPqe, codeBlocks, mLogger, mServices, xnfConversionTechnique, simplificationTechnique, mSymbolTable);
	}

	public GotoEdge constructGotoEdge(final ProgramPoint source, final ProgramPoint target) {
		return new GotoEdge(mSerialNumberCounter++, source, target, mLogger);
	}

	public ParallelComposition constructParallelComposition(final ProgramPoint source, final ProgramPoint target,
			final List<CodeBlock> codeBlocks, final XnfConversionTechnique xnfConversionTechnique,
			final SimplicationTechnique simplificationTechnique) {
		return new ParallelComposition(mSerialNumberCounter++, source, target, mMgdScript, mServices, codeBlocks,
				xnfConversionTechnique);
	}

	public Return constructReturn(final ProgramPoint source, final ProgramPoint target, final Call correspondingCall) {
		return new Return(mSerialNumberCounter++, source, target, correspondingCall, mLogger);
	}

	public SequentialComposition constructSequentialComposition(final ProgramPoint source, final ProgramPoint target,
			final boolean simplify, final boolean extPqe, final List<CodeBlock> codeBlocks,
			final XnfConversionTechnique xnfConversionTechnique, final SimplicationTechnique simplificationTechnique) {
		return new SequentialComposition(mSerialNumberCounter++, source, target, mMgdScript, mMgvManager, simplify,
				extPqe, mServices, codeBlocks, xnfConversionTechnique, simplificationTechnique, mSymbolTable);
	}

	public StatementSequence constructStatementSequence(final ProgramPoint source, final ProgramPoint target,
			final Statement st) {
		return new StatementSequence(mSerialNumberCounter++, source, target, st, mLogger);
	}

	public StatementSequence constructStatementSequence(final ProgramPoint source, final ProgramPoint target,
			final Statement st, final Origin origin) {
		return new StatementSequence(mSerialNumberCounter++, source, target, st, origin, mLogger);
	}

	public StatementSequence constructStatementSequence(final ProgramPoint source, final ProgramPoint target,
			final List<Statement> stmts, final Origin origin) {
		return new StatementSequence(mSerialNumberCounter++, source, target, stmts, origin, mLogger);
	}

	public Summary constructSummary(final ProgramPoint source, final ProgramPoint target, final CallStatement st,
			final boolean calledProcedureHasImplementation) {
		return new Summary(mSerialNumberCounter++, source, target, st, calledProcedureHasImplementation, mLogger);
	}

	public CodeBlock copyCodeBlock(final CodeBlock codeBlock, final ProgramPoint source, final ProgramPoint target) {
		if (codeBlock instanceof Call) {
			final Call copy = constructCall(source, target, ((Call) codeBlock).getCallStatement());
			copy.setTransitionFormula(codeBlock.getTransitionFormula());
			return copy;
		} else if (codeBlock instanceof Return) {
			final Return copy = constructReturn(source, target, ((Return) codeBlock).getCorrespondingCall());
			copy.setTransitionFormula(codeBlock.getTransitionFormula());
			return copy;
		} else if (codeBlock instanceof StatementSequence) {
			final List<Statement> statements = ((StatementSequence) codeBlock).getStatements();
			final Origin origin = ((StatementSequence) codeBlock).getOrigin();
			final StatementSequence copy = this.constructStatementSequence(source, target, statements, origin);
			copy.setTransitionFormula(codeBlock.getTransitionFormula());
			return copy;
		} else if (codeBlock instanceof Summary) {
			final CallStatement callStatement = ((Summary) codeBlock).getCallStatement();
			final boolean calledProcedureHasImplementation = ((Summary) codeBlock).calledProcedureHasImplementation();
			final Summary copy = constructSummary(source, target, callStatement, calledProcedureHasImplementation);
			copy.setTransitionFormula(codeBlock.getTransitionFormula());
			return copy;
		} else if (codeBlock instanceof GotoEdge) {
			final GotoEdge copy = constructGotoEdge(source, target);
			return copy;
		} else {
			throw new UnsupportedOperationException("unsupported kind of CodeBlock");
		}
	}

	@Override
	public void destroy() {
		// nothing to destroy
	}

	public static CodeBlockFactory getFactory(final IToolchainStorage storage) {
		return (CodeBlockFactory) storage.getStorable(CodeBlockFactory.class.getName());
	}

	public void storeFactory(final IToolchainStorage storage) {
		storage.putStorable(CodeBlockFactory.class.getName(), this);
	}

}
