/*
 * Copyright (C) 2012-2015 University of Freiburg
 *
 * This file is part of the ULTIMATE Model Checker Utils Library.
 *
 * The ULTIMATE Model Checker Utils Library is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU Lesser General 
 * Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 *
 * The ULTIMATE Model Checker Utils Library is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Model Checker Utils Library. If not,
 * see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Model Checker Utils Library, or any covered work, 
 * by linking or combining it with Eclipse RCP (or a modified version of
 * Eclipse RCP), containing parts covered by the terms of the Eclipse Public
 * License, the licensors of the ULTIMATE Model Checker Utils Library grant you
 * additional permission to convey the resulting work.
 */
/**
 * 
 */
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie;

import java.math.BigInteger;

import de.uni_freiburg.informatik.ultimate.boogie.type.PrimitiveType;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.model.IType;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.BinaryExpression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.IntegerLiteral;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.UnaryExpression;

/**
 * Assists in the translation process in Expression2Term by covering the cases 
 * of operators, functions and literals in bit vector mode.
 * 
 * @author Thomas Lang
 *
 */
public class BitvectorWorkaroundOperationTranslator extends DefaultOperationTranslator implements
		IOperationTranslator {
	
	public BitvectorWorkaroundOperationTranslator(Boogie2SmtSymbolTable symboltable, Script script) {
		super(symboltable, script);
	}

	@Override
	public String opTranslation(BinaryExpression.Operator op, IType type1, IType type2) {
		if (op == BinaryExpression.Operator.COMPEQ) {
			return "=";
		} else if (op == BinaryExpression.Operator.COMPGEQ) {
			return "bvsge";
		} else if (op == BinaryExpression.Operator.COMPGT) {
			return "bvsgt";
		} else if (op == BinaryExpression.Operator.COMPLEQ) {
			return "bvsle";
		} else if (op == BinaryExpression.Operator.COMPLT) {
			return "bvslt";
		} else if (op == BinaryExpression.Operator.COMPNEQ) {
		    throw new UnsupportedOperationException();
		} else if (op == BinaryExpression.Operator.LOGICAND) {
			if (type1.equals(PrimitiveType.boolType) && type2.equals(PrimitiveType.boolType)) {
				return "and";
			} else {
			    return "bvand";
			}
		} else if (op == BinaryExpression.Operator.LOGICOR) {
			if (type1.equals(PrimitiveType.boolType) && type2.equals(PrimitiveType.boolType)) {
				return "or";
			} else {
			    return "bvor";
			}
		} else if (op == BinaryExpression.Operator.LOGICIMPLIES) {
			if (type1.equals(PrimitiveType.boolType) && type2.equals(PrimitiveType.boolType)) {
				return "=>";
			} else {
			    throw new AssertionError("LOGICIMPLIES of this type not allowed");
			}
		} else if (op == BinaryExpression.Operator.LOGICIFF) {
			return "=";
		} else if (op == BinaryExpression.Operator.ARITHDIV) {
			if (type1 instanceof PrimitiveType) {
				PrimitiveType primType = (PrimitiveType) type1;
				if (primType.getTypeCode() == PrimitiveType.INT) {
					return "div";
				} else if (primType.getTypeCode() == PrimitiveType.REAL) {
					return "/";
				} else {
					throw new AssertionError("ARITHDIV of this type not allowed");
				}
			} else {
				throw new AssertionError("ARITHDIV of this type not allowed");
			}
		} else if (op == BinaryExpression.Operator.ARITHMINUS) {
			return "bvsub";
		} else if (op == BinaryExpression.Operator.ARITHMOD) {
			return "bvsmod";
		} else if (op == BinaryExpression.Operator.ARITHMUL) {
		    return "bvmul";
		} else if (op == BinaryExpression.Operator.ARITHPLUS) {
			return "bvadd";
		} else if (op == BinaryExpression.Operator.BITVECCONCAT) {
			return "concat";
		} else {
			throw new AssertionError("Unsupported binary expression " + op);
		}
	}

	@Override
	public String opTranslation(UnaryExpression.Operator op, IType type) {
		if (op == UnaryExpression.Operator.LOGICNEG) {
			if (type.equals(PrimitiveType.boolType)) {
				return "not";
			} else {
				throw new AssertionError("LOGICNEG of this type not allowed");
			}
		} else if (op == UnaryExpression.Operator.ARITHNEGATIVE) {
			return "bvneg";
		} else
			throw new AssertionError("Unsupported unary expression " + op);
	}

	@Override
	public String funcApplication(String funcIdentifier, IType[] argumentTypes) {
		if (argumentTypes.length == 2 
				&& argumentTypes[0].equals(PrimitiveType.intType) 
				&& argumentTypes[1].equals(PrimitiveType.intType)) {
			if (funcIdentifier.equals("~bitwiseAnd")) {
				return "bvand";
			} else if (funcIdentifier.equals("~bitwiseOr")) {
				return "bvor";
			} else if (funcIdentifier.equals("~shiftLeft")) {
				return "bvshl";
			} else if (funcIdentifier.equals("~shiftRight")) {
				return "bvashr";
			} else {
				return m_Boogie2SmtSymbolTable.getBoogieFunction2SmtFunction().get(funcIdentifier);
			}
		} else {
			return m_Boogie2SmtSymbolTable.getBoogieFunction2SmtFunction().get(funcIdentifier);
		}
	}
	
	@Override
	public Term integerTranslation(IntegerLiteral exp) {
		BigInteger[] indices = { BigInteger.valueOf(32) };
		
		return m_Script.term("bv" + ((IntegerLiteral) exp).getValue(), indices, null);
	}
}