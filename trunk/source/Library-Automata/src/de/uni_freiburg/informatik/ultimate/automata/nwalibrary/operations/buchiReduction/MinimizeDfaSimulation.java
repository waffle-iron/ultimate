/*
 * Copyright (C) 2015 Christian Schilling (schillic@informatik.uni-freiburg.de)
 * Copyright (C) 2014-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2009-2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Automata Library.
 * 
 * The ULTIMATE Automata Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Automata Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Automata Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Automata Library grant you additional permission 
 * to convey the resulting work.
 */
/**
 * Reduce the state space of a given Buchi automaton, as described in the paper
 * "Fair simulation relations, parity games and state space reduction for
 * Buchi automata" - Etessami, Wilke and Schuller.
 */
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.buchiReduction;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.LibraryIdentifiers;
import de.uni_freiburg.informatik.ultimate.automata.OperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.ResultChecker;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonOldApi;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.core.services.model.IUltimateServiceProvider;

/**
 * @author heizmann@informatik.uni-freiburg.de
 * @author schillic@informatik.uni-freiburg.de

 */
public class MinimizeDfaSimulation<LETTER,STATE> implements IOperation<LETTER,STATE> {
	private final IUltimateServiceProvider m_Services;
	private final Logger m_Logger;

    private INestedWordAutomatonOldApi<LETTER,STATE> m_Result;
    /**
     * The input automaton.
     */
    private INestedWordAutomatonOldApi<LETTER,STATE> m_Operand;

    /**
     * Constructor.
     * 
     * @param operand
     *            the automaton to reduce
     * @throws OperationCanceledException 
     */
    public MinimizeDfaSimulation(IUltimateServiceProvider services, 
    		StateFactory<STATE> stateFactory, INestedWordAutomatonOldApi<LETTER,STATE> operand)
            throws AutomataLibraryException {
    	m_Services = services;
		m_Logger = m_Services.getLoggingService().getLogger(LibraryIdentifiers.s_LibraryID);
    	m_Operand = operand;
        m_Logger.info(startMessage());
        
        m_Result = new DirectSimulation<LETTER,STATE>(m_Services, m_Operand, true, stateFactory).result;
        
        boolean compareWithNonSccResult = false;
        if (compareWithNonSccResult) {
        	NestedWordAutomaton<LETTER,STATE> nonSCCresult = 
        			new DirectSimulation<LETTER,STATE>(m_Services, m_Operand, false, stateFactory).result;
        	if (m_Result.size() != nonSCCresult.size()) {
        		throw new AssertionError();
        	}
        }
    	
        m_Logger.info(exitMessage());
    }
    
    @Override
    public String operationName() {
        return "minimizeDfaSimulation";
    }

    @Override
    public String startMessage() {
		return "Start " + operationName() + ". Operand has " + 
		m_Operand.sizeInformation();	
    }

    @Override
    public String exitMessage() {
		return "Finished " + operationName() + " Result " + 
		m_Result.sizeInformation();
    }

    @Override
    public INestedWordAutomatonOldApi<LETTER,STATE> getResult() {
        return m_Result;
    }

	@Override
	public boolean checkResult(StateFactory<STATE> stateFactory)
			throws AutomataLibraryException {
		m_Logger.info("Start testing correctness of " + operationName());
		boolean correct = true;
		correct &= (ResultChecker.nwaLanguageInclusion(m_Services, m_Operand, m_Result, stateFactory) == null);
		correct &= (ResultChecker.nwaLanguageInclusion(m_Services, m_Result, m_Operand, stateFactory) == null);
		if (!correct) {
			ResultChecker.writeToFileIfPreferred(m_Services, operationName() + "Failed", "", m_Operand);
		}
		m_Logger.info("Finished testing correctness of " + operationName());
		return correct;
	}
}