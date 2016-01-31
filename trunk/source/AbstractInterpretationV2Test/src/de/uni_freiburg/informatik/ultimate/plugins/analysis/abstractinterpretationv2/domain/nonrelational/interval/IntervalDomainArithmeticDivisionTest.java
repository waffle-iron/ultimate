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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.interval;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
public class IntervalDomainArithmeticDivisionTest {

	@Test
	public void TestIntervalDivisionPointIntervalPositive() {
		IntervalDomainValue interval1 = HelperFunctions.createInterval(10, 10);

		IntervalDomainValue interval2 = HelperFunctions.createInterval(2, 2);

		IntervalDomainValue expectedResult = HelperFunctions.createInterval(5, 5);

		assertTrue(HelperFunctions.computeDivisionResultReal(interval1, interval2, expectedResult));
	}

	@Test
	public void TestIntervalDivisionPointIntervalNegative() {
		IntervalDomainValue interval1 = HelperFunctions.createInterval(-10, -10);

		IntervalDomainValue interval2 = HelperFunctions.createInterval(2, 2);

		IntervalDomainValue expectedResult = HelperFunctions.createInterval(-5, -5);

		assertTrue(HelperFunctions.computeDivisionResultReal(interval1, interval2, expectedResult));
	}

	@Test
	public void TestIntervalDivisionPointIntervalNegativePositiveResult() {
		IntervalDomainValue interval1 = HelperFunctions.createInterval(-10, -10);

		IntervalDomainValue interval2 = HelperFunctions.createInterval(-2, -2);

		IntervalDomainValue expectedResult = HelperFunctions.createInterval(5, 5);

		assertTrue(HelperFunctions.computeDivisionResultReal(interval1, interval2, expectedResult));
	}

	@Test
	public void TestIntervalDivisionPositive() {
		IntervalDomainValue interval1 = HelperFunctions.createInterval(1, 10);

		IntervalDomainValue interval2 = HelperFunctions.createInterval(2, 2);

		IntervalDomainValue expectedResult = new IntervalDomainValue(new IntervalValue(new BigDecimal(0.5)),
		        new IntervalValue(new BigDecimal(5)));

		assertTrue(HelperFunctions.computeDivisionResultReal(interval1, interval2, expectedResult));
	}
	
	@Test
	public void TestIntervalDivisionNegative() {
		IntervalDomainValue interval1 = HelperFunctions.createInterval(-10, -1);

		IntervalDomainValue interval2 = HelperFunctions.createInterval(-2, -2);

		IntervalDomainValue expectedResult = new IntervalDomainValue(new IntervalValue(new BigDecimal(0.5)),
		        new IntervalValue(new BigDecimal(5)));

		assertTrue(HelperFunctions.computeDivisionResultReal(interval1, interval2, expectedResult));
	}
	
	@Test
	public void TestIntervalDivisionZeroCrossingNominator() {
		IntervalDomainValue interval1 = HelperFunctions.createInterval(-10, 10);

		IntervalDomainValue interval2 = HelperFunctions.createInterval(5, 5);

		IntervalDomainValue expectedResult = HelperFunctions.createInterval(-2, 2);

		assertTrue(HelperFunctions.computeDivisionResultReal(interval1, interval2, expectedResult));
	}
	
	@Test
	public void TestIntervalDivisionZeroCrossingDenominator() {
		IntervalDomainValue interval1 = HelperFunctions.createInterval(10, 10);

		IntervalDomainValue interval2 = HelperFunctions.createInterval(-5, 5);

		// Result should be (-\infty; \infty)
		IntervalDomainValue expectedResult = HelperFunctions.createInterval();

		assertTrue(HelperFunctions.computeDivisionResultReal(interval1, interval2, expectedResult));
	}
	
	@Test
	public void TestIntervalDivisionByZero() {
		IntervalDomainValue interval1 = HelperFunctions.createInterval(10, 10);

		IntervalDomainValue interval2 = HelperFunctions.createInterval(0, 0);

		IntervalDomainValue expectedResult = new IntervalDomainValue(true);

		assertTrue(HelperFunctions.computeDivisionResultReal(interval1, interval2, expectedResult));
	}
	
	@Test
	public void TestIntervalDivisionRegression() {
		IntervalDomainValue interval1 = HelperFunctions.createInterval(-16, 16);

		IntervalDomainValue interval2 = HelperFunctions.createInterval(8, 8);

		IntervalDomainValue expectedResult = HelperFunctions.createInterval(-2, 2);

		assertTrue(HelperFunctions.computeDivisionResultInteger(interval1, interval2, expectedResult));
	}
}