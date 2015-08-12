package de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.ExpressionTranslation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;

import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.LocationFactory;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.FunctionDeclarations;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.cHandler.MemoryHandler;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.cHandler.TypeSizes;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CPointer;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CPrimitive;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CPrimitive.PRIMITIVE;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.exception.UnsupportedSyntaxException;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.RValue;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.ResultExpression;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.util.ISOIEC9899TC3;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.util.SFO;
import de.uni_freiburg.informatik.ultimate.cdt.translation.interfaces.Dispatcher;
import de.uni_freiburg.informatik.ultimate.cdt.translation.interfaces.handler.ITypeHandler;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Attribute;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.BinaryExpression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.BooleanLiteral;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Declaration;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.RealLiteral;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VarList;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VariableDeclaration;
import de.uni_freiburg.informatik.ultimate.model.location.ILocation;

public abstract class AExpressionTranslation {
	
	protected final FunctionDeclarations m_FunctionDeclarations;
	protected final TypeSizes m_TypeSizeConstants;

	public AExpressionTranslation(TypeSizes typeSizeConstants, ITypeHandler typeHandler) {
		super();
		this.m_TypeSizeConstants = typeSizeConstants;
		this.m_FunctionDeclarations = new FunctionDeclarations(typeHandler, m_TypeSizeConstants);
	}

	public ResultExpression translateLiteral(Dispatcher main, IASTLiteralExpression node) {
		ILocation loc = LocationFactory.createCLocation(node);

		switch (node.getKind()) {
		case IASTLiteralExpression.lk_float_constant:
		{
			String val = new String(node.getValue());
			val = ISOIEC9899TC3.handleFloatConstant(val, loc, main);
			return new ResultExpression(new RValue(new RealLiteral(loc, val), new CPrimitive(PRIMITIVE.FLOAT)));
		}
		case IASTLiteralExpression.lk_char_constant:
			throw new AssertionError("To be handled by subclass");
		case IASTLiteralExpression.lk_integer_constant:
		{
			String val = new String(node.getValue());
			RValue rVal = translateIntegerLiteral(loc, val);
			return new ResultExpression(rVal);
		}
		case IASTLiteralExpression.lk_string_literal:
			// Translate string to uninitialized char pointer
			String tId = main.nameHandler.getTempVarUID(SFO.AUXVAR.NONDET);
			VariableDeclaration tVarDecl = new VariableDeclaration(loc, new Attribute[0], new VarList[] { new VarList(
					loc, new String[] { tId }, MemoryHandler.POINTER_TYPE) });
			RValue rvalue = new RValue(new IdentifierExpression(loc, tId), new CPointer(new CPrimitive(PRIMITIVE.CHAR)));
			ArrayList<Declaration> decls = new ArrayList<Declaration>();
			decls.add(tVarDecl);
			Map<VariableDeclaration, ILocation> auxVars = new LinkedHashMap<VariableDeclaration, ILocation>();
			auxVars.put(tVarDecl, loc);
			return new ResultExpression(new ArrayList<Statement>(), rvalue, decls, auxVars);
		case IASTLiteralExpression.lk_false:
			return new ResultExpression(new RValue(new BooleanLiteral(loc, false), new CPrimitive(PRIMITIVE.INT)));
		case IASTLiteralExpression.lk_true:
			return new ResultExpression(new RValue(new BooleanLiteral(loc, true), new CPrimitive(PRIMITIVE.INT)));
		default:
			String msg = "Unknown or unsupported kind of IASTLiteralExpression";
			throw new UnsupportedSyntaxException(loc, msg);
		}
	}
	
	public abstract Expression constructBinaryComparisonExpression(ILocation loc, int nodeOperator, Expression exp1, CPrimitive type1, Expression exp2, CPrimitive type2);
	public abstract Expression constructBinaryBitwiseExpression(ILocation loc, int nodeOperator, Expression exp1, CPrimitive type1, Expression exp2, CPrimitive type2);
	public abstract Expression constructUnaryExpression(ILocation loc, int nodeOperator, Expression exp, CPrimitive type);
	public abstract Expression createArithmeticExpression(int op, Expression left, CPrimitive typeLeft, Expression right, CPrimitive typeRight, ILocation loc);
	
	
	protected Expression constructBinaryEqualsExpression(ILocation loc, int nodeOperator, Expression exp1, CPrimitive type1, Expression exp2, CPrimitive type2) {
		if (nodeOperator == IASTBinaryExpression.op_equals) {
			return new BinaryExpression(loc, BinaryExpression.Operator.COMPEQ, exp1, exp2);
		} else 	if (nodeOperator == IASTBinaryExpression.op_notequals) {
			return new BinaryExpression(loc, BinaryExpression.Operator.COMPNEQ, exp1, exp2);
		} else {
			throw new IllegalArgumentException("operator is neither equals nor not equals");
		}
	}
	
	public abstract RValue translateIntegerLiteral(ILocation loc, String val);
	
	public abstract Expression constructLiteralForIntegerType(ILocation loc, CPrimitive type, BigInteger value);

	public FunctionDeclarations getFunctionDeclarations() {
		return m_FunctionDeclarations;
	}
	
	
}