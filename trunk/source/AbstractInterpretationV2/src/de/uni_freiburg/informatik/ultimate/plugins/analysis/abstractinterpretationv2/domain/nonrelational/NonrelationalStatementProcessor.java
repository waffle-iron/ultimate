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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.uni_freiburg.informatik.ultimate.boogie.BoogieVisitor;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayAccessExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayStoreExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BinaryExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BooleanLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Declaration;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.FunctionApplication;
import de.uni_freiburg.informatik.ultimate.boogie.ast.FunctionDeclaration;
import de.uni_freiburg.informatik.ultimate.boogie.ast.HavocStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IfThenElseExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IntegerLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.LeftHandSide;
import de.uni_freiburg.informatik.ultimate.boogie.ast.RealLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.UnaryExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.boogie.output.BoogiePrettyPrinter;
import de.uni_freiburg.informatik.ultimate.boogie.symboltable.BoogieSymbolTable;
import de.uni_freiburg.informatik.ultimate.boogie.type.ArrayType;
import de.uni_freiburg.informatik.ultimate.boogie.type.PrimitiveType;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.BooleanValue.Value;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.EvaluatorUtils;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.ExpressionEvaluator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.IEvaluationResult;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.IEvaluator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.IEvaluatorFactory;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.evaluator.INAryEvaluator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.interval.IntervalDomainState;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.preferences.AbsIntPrefInitializer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;

/**
 * Processes Boogie {@link Statement}s and returns a new {@link IntervalDomainState} for the given statement.
 *
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
public abstract class NonrelationalStatementProcessor<STATE extends NonrelationalState<STATE, V>, V extends INonrelationalValue<V>>
        extends BoogieVisitor {

	private final Boogie2SmtSymbolTable mBoogie2SmtSymbolTable;
	private final BoogieSymbolTable mSymbolTable;
	private final ILogger mLogger;
	private final IEvaluatorFactory<V, STATE, CodeBlock> mEvaluatorFactory;

	private STATE mOldState;
	private List<STATE> mReturnState;
	private ExpressionEvaluator<V, STATE, CodeBlock> mExpressionEvaluator;
	private IBoogieVar mLhsVariable;
	private Map<LeftHandSide, IBoogieVar> mTemporaryVars;

	protected NonrelationalStatementProcessor(final ILogger logger, final BoogieSymbolTable boogieSymbolTable,
	        final Boogie2SmtSymbolTable bpl2SmtTable, final int maxParallelStates) {
		mBoogie2SmtSymbolTable = bpl2SmtTable;
		mSymbolTable = boogieSymbolTable;
		mLogger = logger;
		mLhsVariable = null;
		mEvaluatorFactory = createEvaluatorFactory(maxParallelStates);
		assert mEvaluatorFactory != null;
	}

	public List<STATE> process(final STATE oldState, final Statement statement) {
		return process(oldState, statement, Collections.emptyMap());
	}

	public List<STATE> process(final STATE oldState, final Statement statement,
	        final Map<LeftHandSide, IBoogieVar> tmpVars) {
		assert oldState != null;
		assert statement != null;
		assert tmpVars != null;

		mReturnState = new ArrayList<>();
		mOldState = oldState;
		mTemporaryVars = tmpVars;
		mLhsVariable = null;

		processStatement(statement);
		final List<STATE> rtr = mReturnState;
		assert (oldState.getVariables().isEmpty()) || (!rtr.isEmpty());

		mReturnState = null;
		mOldState = null;
		mTemporaryVars = null;
		mLhsVariable = null;

		return rtr;
	}

	@Override
	protected Statement processStatement(final Statement statement) {
		if (statement instanceof AssignmentStatement) {
			handleAssignment((AssignmentStatement) statement);
			return statement;
		} else if (statement instanceof AssumeStatement) {
			handleAssumeStatement((AssumeStatement) statement);
			return statement;
		} else if (statement instanceof HavocStatement) {
			handleHavocStatement((HavocStatement) statement);
			return statement;
		}
		return super.processStatement(statement);
	}

	@Override
	protected Expression processExpression(final Expression expr) {
		// TODO: implement proper array handling. Currently, TOP is returned for all array accesses.
		if (expr instanceof ArrayStoreExpression) {
			mExpressionEvaluator.addEvaluator(mEvaluatorFactory.createSingletonValueTopEvaluator());
			return expr;
		} else if (expr instanceof ArrayAccessExpression) {
			mExpressionEvaluator.addEvaluator(mEvaluatorFactory.createSingletonValueTopEvaluator());
			return expr;
		}

		final Expression newExpr = normalizeExpression(expr);
		assert newExpr != null;

		if (expr == newExpr) {
			addEvaluators(mExpressionEvaluator, mEvaluatorFactory, newExpr);
			return super.processExpression(expr);
		} else {
			if (mLogger.isDebugEnabled()) {
				mLogger.debug(new StringBuilder().append(AbsIntPrefInitializer.INDENT).append(" Expression ")
				        .append(BoogiePrettyPrinter.print(expr)).append(" rewritten to: ")
				        .append(BoogiePrettyPrinter.print(newExpr)).toString());
			}
			return processExpression(newExpr);
		}
	}

	protected abstract IEvaluatorFactory<V, STATE, CodeBlock> createEvaluatorFactory(final int maxParallelStates);

	/**
	 * Override this method to add evaluators for this (already preprocessed) expression.
	 *
	 * @param evaluator
	 *            The current root evaluator.
	 * @param evaluatorFactory
	 *            The current instance of the evaluator factory.
	 * @param expr
	 *            The preprocessed expression.
	 */
	protected void addEvaluators(final ExpressionEvaluator<V, STATE, CodeBlock> evaluator,
	        final IEvaluatorFactory<V, STATE, CodeBlock> evaluatorFactory, final Expression expr) {

	}

	/**
	 * Override this method if you want to apply some sort of normalization.
	 *
	 * @param expr
	 *            The expression that is about to be processed.
	 * @return The normalized expression or the old expression, if nothing had to be changed.
	 */
	protected Expression normalizeExpression(final Expression expr) {
		return expr;
	}

	protected ILogger getLogger() {
		return mLogger;
	}

	private void handleAssignment(final AssignmentStatement statement) {
		final LeftHandSide[] lhs = statement.getLhs();
		final Expression[] rhs = statement.getRhs();

		final List<List<STATE>> currentStateList = new ArrayList<>();

		for (int i = 0; i < lhs.length; i++) {
			// TODO: We have to project the result of a single assignment to the left-hand-side and update only this
			// part of the non-relational state
			currentStateList.add(handleSingleAssignment(lhs[i], rhs[i], mOldState));
		}
		if (currentStateList.size() == 1) {
			// this was not a parallel assignment
			mReturnState.addAll(currentStateList.get(0));
		} else {
			// just mush them all together
			final Optional<STATE> result = currentStateList.stream().flatMap(a -> a.stream())
			        .reduce((a, b) -> a.merge(b));
			mReturnState.add(result.get());
		}
	}

	private List<STATE> handleSingleAssignment(final LeftHandSide lhs, final Expression rhs, final STATE oldstate) {
		assert mLhsVariable == null;
		processLeftHandSide(lhs);
		assert mLhsVariable != null;
		final IBoogieVar var = mLhsVariable;
		mLhsVariable = null;
		mExpressionEvaluator = new ExpressionEvaluator<V, STATE, CodeBlock>();

		processExpression(rhs);

		assert mExpressionEvaluator.isFinished() : "Expression evaluator is not finished";
		assert mExpressionEvaluator.getRootEvaluator() != null;

		final List<STATE> newStates = new ArrayList<>();

		final List<IEvaluationResult<V>> results = mExpressionEvaluator.getRootEvaluator().evaluate(oldstate);

		if (results.isEmpty()) {
			throw new UnsupportedOperationException(
			        "There is supposed to be at least on evaluation result for assignment expressions.");
		}

		for (final IEvaluationResult<V> res : results) {
			final STATE newState;

			if (var.getIType() instanceof PrimitiveType) {
				final PrimitiveType primitiveType = (PrimitiveType) var.getIType();

				if (primitiveType.getTypeCode() == PrimitiveType.BOOL) {
					newState = oldstate.setBooleanValue(var, res.getBooleanValue());
				} else {
					newState = oldstate.setValue(var, res.getValue());
				}
			} else if (var.getIType() instanceof ArrayType) {
				// TODO: treat array correctly
				// We treat Arrays as normal variables for the time being.
				newState = oldstate.setValue(var, res.getValue());
			} else {
				// just assume that the domain value handles those
				newState = oldstate.setValue(var, res.getValue());
			}

			newStates.add(newState);
		}
		return newStates;
	}

	private void handleAssumeStatement(final AssumeStatement statement) {
		mExpressionEvaluator = new ExpressionEvaluator<V, STATE, CodeBlock>();

		final Expression formula = statement.getFormula();

		if (formula instanceof BooleanLiteral) {
			// handle the special case of a boolean literal
			final BooleanLiteral boolform = (BooleanLiteral) formula;
			if (!boolform.getValue()) {
				mReturnState.add(mOldState.bottomState());
			} else {
				mReturnState.add(mOldState);
			}
			return;
		}

		processExpression(formula);
		assert mExpressionEvaluator.isFinished();

		final List<IEvaluationResult<V>> result = mExpressionEvaluator.getRootEvaluator().evaluate(mOldState);

		for (final IEvaluationResult<V> res : result) {
			if (res.getValue().isBottom() || res.getBooleanValue().getValue() == Value.BOTTOM
			        || res.getBooleanValue().getValue() == Value.FALSE) {
				if (mOldState.getVariables().size() != 0) {
					mReturnState.add(mOldState.bottomState());
				}
			} else {
				final List<STATE> resultStates = mExpressionEvaluator.getRootEvaluator().inverseEvaluate(res,
				        mOldState);
				mReturnState.addAll(resultStates);
			}
		}
	}

	private void handleHavocStatement(final HavocStatement statement) {
		STATE currentNewState = mOldState;

		for (final VariableLHS var : statement.getIdentifiers()) {
			final IBoogieVar type = getBoogieVar(var);

			if (type.getIType() instanceof PrimitiveType) {
				final PrimitiveType primitiveType = (PrimitiveType) type.getIType();

				if (primitiveType.getTypeCode() == PrimitiveType.BOOL) {
					currentNewState = currentNewState.setBooleanValue(type, new BooleanValue());
				} else {
					currentNewState = currentNewState.setValue(type, currentNewState.createTopValue());
				}
			} else if (type.getIType() instanceof ArrayType) {
				// TODO:
				// Implement better handling of arrays.
				currentNewState = currentNewState.setValue(type, currentNewState.createTopValue());
			} else {
				currentNewState = currentNewState.setValue(type, currentNewState.createTopValue());
			}
		}

		mReturnState.add(currentNewState);
	}

	@Override
	protected void visit(final VariableLHS lhs) {
		mLhsVariable = getBoogieVar(lhs);
	}

	@Override
	protected void visit(final IntegerLiteral expr) {
		final IEvaluator<V, STATE, CodeBlock> evaluator = mEvaluatorFactory
		        .createSingletonValueExpressionEvaluator(expr.getValue(), BigDecimal.class);
		mExpressionEvaluator.addEvaluator(evaluator);
	}

	@Override
	protected void visit(final RealLiteral expr) {
		final IEvaluator<V, STATE, CodeBlock> evaluator = mEvaluatorFactory
		        .createSingletonValueExpressionEvaluator(expr.getValue(), BigDecimal.class);
		mExpressionEvaluator.addEvaluator(evaluator);
	}

	@Override
	protected void visit(final BinaryExpression expr) {
		final INAryEvaluator<V, STATE, CodeBlock> evaluator = mEvaluatorFactory.createNAryExpressionEvaluator(2,
		        EvaluatorUtils.getEvaluatorType(expr.getType()));
		evaluator.setOperator(expr.getOperator());
		mExpressionEvaluator.addEvaluator(evaluator);
	}

	@Override
	protected void visit(final FunctionApplication expr) {
		final IEvaluator<V, STATE, CodeBlock> evaluator;
		final List<Declaration> decls = mSymbolTable.getFunctionOrProcedureDeclaration(expr.getIdentifier());

		// If we don't have a specification for the function, we return top.
		if (decls == null || decls.isEmpty()) {
			evaluator = mEvaluatorFactory.createSingletonValueTopEvaluator();
		} else {

			assert decls.get(0) instanceof FunctionDeclaration;

			final FunctionDeclaration fun = (FunctionDeclaration) decls.get(0);

			// If the body is empty (as in undefined), we return top.
			if (fun.getBody() == null) {
				evaluator = mEvaluatorFactory.createFunctionEvaluator(fun.getIdentifier(), fun.getInParams().length);
			} else {
				// TODO Handle bitshifts, bitwise and, bitwise or, etc.
				throw new UnsupportedOperationException(
				        "The function application for not inlined functions is not yet supported.");
			}
		}

		mExpressionEvaluator.addEvaluator(evaluator);
	}

	@Override
	protected void visit(final IdentifierExpression expr) {
		final IEvaluator<V, STATE, CodeBlock> evaluator = mEvaluatorFactory
		        .createSingletonVariableExpressionEvaluator(getBoogieVar(expr));
		mExpressionEvaluator.addEvaluator(evaluator);
		super.visit(expr);
	}

	@Override
	protected void visit(final UnaryExpression expr) {
		final INAryEvaluator<V, STATE, CodeBlock> evaluator = mEvaluatorFactory.createNAryExpressionEvaluator(1,
		        EvaluatorUtils.getEvaluatorType(expr.getType()));
		evaluator.setOperator(expr.getOperator());
		mExpressionEvaluator.addEvaluator(evaluator);
		super.visit(expr);
	}

	@Override
	protected void visit(final BooleanLiteral expr) {
		final IEvaluator<V, STATE, CodeBlock> evaluator = mEvaluatorFactory
		        .createSingletonLogicalValueExpressionEvaluator(new BooleanValue(expr.getValue()));
		mExpressionEvaluator.addEvaluator(evaluator);
	}

	@Override
	protected void visit(final ArrayStoreExpression expr) {
		throw new UnsupportedOperationException("Proper array handling is not implemented.");
	}

	@Override
	protected void visit(final ArrayAccessExpression expr) {
		throw new UnsupportedOperationException("Proper array handling is not implemented.");
	}

	@Override
	protected void visit(final IfThenElseExpression expr) {
		final IEvaluator<V, STATE, CodeBlock> evaluator = mEvaluatorFactory.createConditionalEvaluator();
		mExpressionEvaluator.addEvaluator(evaluator);

		// Create a new expression for the negative case
		final UnaryExpression newUnary = new UnaryExpression(expr.getLocation(), UnaryExpression.Operator.LOGICNEG,
		        expr.getCondition());

		// This expression should be added first to the evaluator inside the handling of processExpression.
		processExpression(newUnary);
	}

	private IBoogieVar getBoogieVar(final VariableLHS expr) {
		final IBoogieVar rtr = mTemporaryVars.get(expr);
		if (rtr == null) {
			return mBoogie2SmtSymbolTable.getBoogieVar(expr.getIdentifier(), expr.getDeclarationInformation(), false);
		}
		return rtr;
	}

	private IBoogieVar getBoogieVar(final IdentifierExpression expr) {
		return mBoogie2SmtSymbolTable.getBoogieVar(expr.getIdentifier(), expr.getDeclarationInformation(), false);
	}
}
