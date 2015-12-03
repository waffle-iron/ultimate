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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.util.lpsolver;

import static org.junit.Assert.*;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Test class for {@link LinearConstraint}s.
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
public class LinearConstraintTest {

	@Test
	public void TestLinearConstraint() {
		final LinearConstraint<BigDecimal> xconstr = new LinearConstraint<>("x-constraint");

		xconstr.addCoefficient("x", new BigDecimal(1));
		xconstr.addCoefficient("y", new BigDecimal(0));
		xconstr.setLower(new BigDecimal(0));
		xconstr.setUpper(new BigDecimal(10));

		assertTrue(xconstr.getName() == "x-constraint");
		assertTrue(xconstr.getVariableCount() == 2);
		assertTrue(xconstr.getCoefficient("x").equals(new BigDecimal(1)));
		assertTrue(xconstr.getCoefficient("y").equals(new BigDecimal(0)));
		assertTrue(xconstr.getLower().equals(new BigDecimal(0)));
		assertTrue(xconstr.getUpper().equals(new BigDecimal(10)));

		System.out.println(xconstr.toLogString());
	}
}