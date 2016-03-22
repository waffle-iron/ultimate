/*
 * Copyright (C) 2016 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2016 University of Freiburg
 * 
 * This file is part of the ULTIMATE CACSL2BoogieTranslator plug-in.
 * 
 * The ULTIMATE CACSL2BoogieTranslator plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE CACSL2BoogieTranslator plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE CACSL2BoogieTranslator plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE CACSL2BoogieTranslator plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE CACSL2BoogieTranslator plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.ExpressionTranslation;

import java.math.BigInteger;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;

import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.cHandler.MemoryHandler;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CPointer;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CPrimitive;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.ExpressionResult;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.RValue;
import de.uni_freiburg.informatik.ultimate.cdt.translation.interfaces.handler.ITypeHandler;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.model.location.ILocation;

/**
 * Defines the following conversion between pointers and integers.
 * An integer n is converted to the pointer with base address 0 and offset n.
 * A pointer p is converted to the sum of the base address and the offset. 
 * @author Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 */
public class NonBijectiveMapping implements IPointerIntegerConversion {
	
	protected final  AExpressionTranslation m_ExpressionTranslation;
	private final ITypeHandler m_TypeHandler;
	
	public NonBijectiveMapping(AExpressionTranslation expressionTranslation,
			ITypeHandler typeHandler) {
		m_ExpressionTranslation = expressionTranslation;
		m_TypeHandler = typeHandler;
	}

	@Override
	public void convertPointerToInt(ILocation loc, ExpressionResult rexp, CPrimitive newType) {
		final RValue pointer = (RValue) rexp.lrVal;
		final Expression baseAddress = MemoryHandler.getPointerBaseAddress(pointer.getValue(), loc);
		final Expression offset = MemoryHandler.getPointerOffset(pointer.getValue(), loc);
		final Expression sumExpr = m_ExpressionTranslation.constructArithmeticExpression(
				loc, IASTBinaryExpression.op_plus, 
				baseAddress, m_ExpressionTranslation.getCTypeOfPointerComponents(), 
				offset, m_ExpressionTranslation.getCTypeOfPointerComponents());
		final RValue sum = new RValue(sumExpr, m_ExpressionTranslation.getCTypeOfPointerComponents());
		rexp.lrVal = sum;
		m_ExpressionTranslation.convertIntToInt(loc, rexp, newType);
	}

	@Override
	public void convertIntToPointer(ILocation loc, ExpressionResult rexp, CPointer newType) {
		m_ExpressionTranslation.convertIntToInt(loc, rexp, m_ExpressionTranslation.getCTypeOfPointerComponents());
		Expression zero = m_ExpressionTranslation.constructLiteralForIntegerType(
				loc, m_ExpressionTranslation.getCTypeOfPointerComponents(), BigInteger.ZERO);
		RValue rValue = new RValue(MemoryHandler.constructPointerFromBaseAndOffset(zero, rexp.lrVal.getValue(), loc), newType, false, false);
		rexp.lrVal = rValue;
	}

}