package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.sign;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieVar;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.rcfg.RcfgStatementExtractor;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.IAbstractPostOperator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.IAbstractState;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;

/**
 * Applies a post operation to an abstract state of the {@link SignDomain}.
 * 
 * @author greitsch@informatik.uni-freiburg.de
 */
public class SignPostOperator implements IAbstractPostOperator<CodeBlock, BoogieVar> {

	private SignStateConverter<CodeBlock, BoogieVar> mStateConverter;
	private RcfgStatementExtractor mStatementExtractor;
	private SignDomainStatementProcessor mStatementProcessor;

	/**
	 * Default constructor.
	 * 
	 * @param stateConverter
	 *            The state converter used to identify {@link SignDomainState}s.
	 */
	protected SignPostOperator(SignStateConverter<CodeBlock, BoogieVar> stateConverter) {
		mStateConverter = stateConverter;
		mStatementExtractor = new RcfgStatementExtractor();
		mStatementProcessor = new SignDomainStatementProcessor(mStateConverter);
	}

	/**
	 * Applys the post operator to a given {@link IAbstractState}, according to some Boogie {@link CodeBlock}.
	 * 
	 * @param oldstate
	 *            The current abstract state, the post operator is applied on.
	 * @param codeBlock
	 *            The Boogie code block that is used to apply the post operator.
	 * @return A new abstract state which is the result of applying the post operator to a given abstract state.
	 */
	@Override
	public IAbstractState<CodeBlock, BoogieVar> apply(IAbstractState<CodeBlock, BoogieVar> oldstate, CodeBlock codeBlock) {
		final SignDomainState<CodeBlock, BoogieVar> concreteOldState = mStateConverter.getCheckedState(oldstate);
		SignDomainState<CodeBlock, BoogieVar> currentState = concreteOldState;
		List<Statement> statements = mStatementExtractor.process(codeBlock);
		for (Statement stmt : statements) {
			currentState = mStatementProcessor.process(currentState, stmt);
		}

		return currentState;
	}
}