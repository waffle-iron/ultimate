/*
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE TraceAbstraction plug-in.
 * 
 * The ULTIMATE TraceAbstraction plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE TraceAbstraction plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE TraceAbstraction plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE TraceAbstraction plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE TraceAbstraction plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.hoaretriple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.logic.Annotation;
import de.uni_freiburg.informatik.ultimate.logic.FormulaUnLet;
import de.uni_freiburg.informatik.ultimate.logic.QuotedObject;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.logic.Util;
import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieNonOldVar;
import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieOldVar;
import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieVar;
import de.uni_freiburg.informatik.ultimate.model.boogie.GlobalBoogieVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SMT;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.ModifiableGlobalVariableManager;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.TransFormula;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript.ILockHolderWithVoluntaryLockRelease;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.util.ScopedHashMap;

public class IncrementalHoareTripleChecker implements IHoareTripleChecker, ILockHolderWithVoluntaryLockRelease  {
	
	protected enum TransType { CALL, INTERNAL, RETURN };
	protected final ManagedScript m_ManagedScript;
	private Boogie2SMT m_Boogie2Smt;
	private final ModifiableGlobalVariableManager m_ModifiableGlobalVariableManager;
	
	private TransType m_TransType;
	private IPredicate m_AssertedPrecond;
	private IPredicate m_AssertedHier;
	private CodeBlock m_AssertedCodeBlock;
	private TransFormula m_TransFormula;
	private IPredicate m_AssertedPostcond;
	private ScopedHashMap<BoogieVar, Term> m_HierConstants;
	public final boolean m_UseNamedTerms = true;
	public final static boolean m_UnletTerms = true;
	
	protected static final String s_IdPrecondition = "precondition";
	protected static final String s_PrecondNonModGlobalEquality = "precondNonModGlobalEquality";
	protected static final String s_IdTransitionModifiableGlobalEquality = "modifiableVarEquality";
	protected static final String s_IdTransitionFormula = "codeBlock";
	protected static final String s_IdLocalVarsAssignment = "localVarsAssignment";
	protected static final String s_IdHierarchicalPrecondition = "hierarchicalPrecondition";
	protected static final String s_IdNegatedPostcondition = "negatedPostcondition";
	
	private final HoareTripleCheckerStatisticsGenerator m_EdgeCheckerBenchmark;
	
	
	private static final String s_StartEdgeCheck = "starting to check validity of Hoare triples";
	private static final String s_EndEdgeCheck = "finished to check validity of Hoare triples";
	
	public IncrementalHoareTripleChecker(ManagedScript managedScript, 
			ModifiableGlobalVariableManager modGlobVarManager,
			Boogie2SMT boogie2smt) {
		m_ManagedScript = managedScript;
		m_ModifiableGlobalVariableManager = modGlobVarManager;
		m_Boogie2Smt = boogie2smt;
		m_EdgeCheckerBenchmark = new HoareTripleCheckerStatisticsGenerator();
	}
	
	@Override
	public HoareTripleCheckerStatisticsGenerator getEdgeCheckerBenchmark() {
		return m_EdgeCheckerBenchmark;
	}
	
	
	@Override
	public Validity checkInternal(IPredicate pre, CodeBlock cb, IPredicate post) {
		final LBool quickCheck_Trans = prepareAssertionStackAndAddTransition(cb);
		if (quickCheck_Trans == LBool.UNSAT) {
			return Validity.VALID;
		}
		final LBool quickCheck_LinPred = prepareAssertionStackAndAddPrecondition(pre);
		if (quickCheck_LinPred == LBool.UNSAT) {
			return Validity.VALID;
		}
		final LBool quickCheck_Postcond = prepareAssertionStackAndAddPostcond(post);
		if (quickCheck_Postcond == LBool.UNSAT) {
			return Validity.VALID;
		}
		assert quickCheck_Postcond == LBool.UNKNOWN : "unexpected quickcheck result";
		assert m_AssertedPrecond == pre && m_AssertedHier == null && 
				m_AssertedCodeBlock == cb && m_AssertedPostcond == post;
		return checkValidity();
	}
	
	
	@Override
	public Validity checkCall(IPredicate pre, CodeBlock cb, IPredicate post) {
		final LBool quickCheck_Trans = prepareAssertionStackAndAddTransition(cb);
		if (quickCheck_Trans == LBool.UNSAT) {
			return Validity.VALID;
		}
		final LBool quickCheck_LinPred = prepareAssertionStackAndAddPrecondition(pre);
		if (quickCheck_LinPred == LBool.UNSAT) {
			return Validity.VALID;
		}
		final LBool quickCheck_Postcond = prepareAssertionStackAndAddPostcond(post);
		if (quickCheck_Postcond == LBool.UNSAT) {
			return Validity.VALID;
		}
		assert quickCheck_Postcond == LBool.UNKNOWN : "unexpected quickcheck result";
		assert m_AssertedPrecond == pre && m_AssertedHier == null && 
				m_AssertedCodeBlock == cb && m_AssertedPostcond == post;
		return checkValidity();
	}

	
	@Override
	public Validity checkReturn(IPredicate linPre, IPredicate hierPre,
			CodeBlock cb, IPredicate postcond) {
		final LBool quickCheck_Trans = prepareAssertionStackAndAddTransition(cb);
		if (quickCheck_Trans == LBool.UNSAT) {
			return Validity.VALID;
		}
		final LBool quickCheck_LinPred = prepareAssertionStackAndAddPrecondition(linPre);
		if (quickCheck_LinPred == LBool.UNSAT) {
			return Validity.VALID;
		}
		final LBool quickCheck_HierPred = prepareAssertionStackAndAddHierpred(hierPre);
		if (quickCheck_HierPred == LBool.UNSAT) {
			return Validity.VALID;
		}
		final LBool quickCheck_Postcond = prepareAssertionStackAndAddPostcond(postcond);
		if (quickCheck_Postcond == LBool.UNSAT) {
			return Validity.VALID;
		}
		assert quickCheck_Postcond == LBool.UNKNOWN : "unexpected quickcheck result";
		assert m_AssertedPrecond == linPre && m_AssertedHier == hierPre && 
				m_AssertedCodeBlock == cb && m_AssertedPostcond == postcond;
		return checkValidity();
	}
	
	
	protected LBool prepareAssertionStackAndAddTransition(CodeBlock cb) {
		if (m_AssertedCodeBlock != cb) {
			if (m_AssertedCodeBlock != null) {
				if (m_AssertedPrecond != null) {
					if (m_AssertedPostcond != null) {
						unAssertPostcondition();
					}
					if (m_AssertedHier != null) {
						unAssertHierPred();
					}
					unAssertPrecondition();
				}
				unAssertCodeBlock();
			}
			final LBool quickCheck = assertCodeBlock(cb);
			return quickCheck;
		}
		return null;
	}


	protected LBool prepareAssertionStackAndAddPrecondition(IPredicate precond) {
		if (m_AssertedPrecond != precond) {
			if (m_AssertedPrecond != null) {
				if (m_AssertedPostcond != null) {
					unAssertPostcondition();
				}
				if (m_AssertedHier != null) {
					unAssertHierPred();
				}
				unAssertPrecondition();
			}
			final LBool quickCheck = assertPrecondition(precond);
			return quickCheck;
		}
		return null;
	}
	
	
	protected LBool prepareAssertionStackAndAddHierpred(IPredicate hierpred) {
		if (m_AssertedHier != hierpred) {
			if (m_AssertedPostcond != null) {
				unAssertPostcondition();
			}
			if (m_AssertedHier != null) {
				unAssertHierPred();
			}
			final LBool quickCheck = assertHierPred(hierpred);
			return quickCheck;
		}
		return null;
	}
	
	
	protected LBool prepareAssertionStackAndAddPostcond(IPredicate postcond) {
		if (m_AssertedPostcond != postcond) {
			if (m_AssertedPostcond != null) {
				unAssertPostcondition();
			}
			final LBool quickCheck = assertPostcond(postcond);
			return quickCheck;
		}
		return null;
	}
	

	protected LBool assertPostcond(IPredicate postcond) {
		switch (m_TransType) {
		case CALL:
			return assertPostcond_Call(postcond);
		case INTERNAL:
			return assertPostcond_Internal(postcond);
		case RETURN:
			return assertPostcond_Return(postcond);
		default:
			throw new AssertionError("unknown trans type");
		}
	}

	
	public void clearAssertionStack() {
		if (m_AssertedPostcond != null) {
			this.unAssertPostcondition();
		}
		if (m_AssertedPrecond != null) {
			this.unAssertPrecondition();
		}
		if (m_AssertedHier != null) {
			this.unAssertHierPred();
		}
		if (m_AssertedCodeBlock != null) {
			this.unAssertCodeBlock();
		}
	}
	
	
	@Override
	public void releaseLock() {
		clearAssertionStack();
	}

	
	private LBool assertPrecondition(IPredicate p) {
		assert m_ManagedScript.isLockOwner(this);
		assert m_AssertedCodeBlock != null : "Assert CodeBlock first";
		assert m_TransType != null : "TransType not set";
		assert m_AssertedPrecond == null : "precond already asserted";
		m_AssertedPrecond = p;
		m_EdgeCheckerBenchmark.continueEdgeCheckerTime();
		m_ManagedScript.push(this, 1);
		if (m_TransType == TransType.RETURN) {
			m_HierConstants.beginScope();
		}
		Term predcondition = p.getClosedFormula();
		if (m_UseNamedTerms) {
			Annotation annot = new Annotation(":named", s_IdPrecondition);
			predcondition = m_ManagedScript.annotate(this, predcondition, annot);
		}
		LBool quickCheck = m_ManagedScript.assertTerm(this, predcondition);
		String predProc = m_AssertedCodeBlock.getPreceedingProcedure();
		Set<BoogieVar> oldVarsOfModifiable = m_ModifiableGlobalVariableManager.
				getOldVarsAssignment(predProc).getAssignedVars();
		Collection<Term> oldVarEqualities = constructNonModOldVarsEquality(p.getVars(), oldVarsOfModifiable);
		if (!oldVarEqualities.isEmpty()) {
			Term nonModOldVarsEquality = Util.and(m_Boogie2Smt.getScript(), oldVarEqualities.toArray(new Term[oldVarEqualities.size()]));
			if (m_UseNamedTerms) {
				Annotation annot = new Annotation(":named", s_PrecondNonModGlobalEquality);
				nonModOldVarsEquality = m_ManagedScript.annotate(this, nonModOldVarsEquality, annot);
			}
			quickCheck = m_ManagedScript.assertTerm(this, nonModOldVarsEquality);
		}
		m_EdgeCheckerBenchmark.stopEdgeCheckerTime();
		return quickCheck;
	}
	
	
	/**
	 * Return a set of equalities such that for each oldvar old(g) that occurs
	 * in vars that is not contained in oldVarsOfModifiableGlobals there is
	 * an equality (= c_g c_old(g)) where c_g is the default constant of the 
	 * global variable g and c_old(g) is the default constant of old(g).
	 */
	private Collection<Term> constructNonModOldVarsEquality(Set<BoogieVar> vars,
			Set<BoogieVar> oldVarsOfModifiableGlobals) {
		Collection<Term> conjunction = new ArrayList<>();
		for (BoogieVar bv : vars) {
			if (bv instanceof BoogieOldVar && !oldVarsOfModifiableGlobals.contains(bv)) {
				conjunction.add(oldVarsEquality((BoogieOldVar) bv));
			}
		}
		return conjunction;
	}
	
	private Term oldVarsEquality(BoogieOldVar oldVar) {
		assert oldVar.isOldvar();
		BoogieVar nonOldVar = oldVar.getNonOldVar();
		Term equality = m_ManagedScript.term(this, "=", oldVar.getDefaultConstant(), 
										   nonOldVar.getDefaultConstant());
		return equality;
	}
	

	private void unAssertPrecondition() {
		assert m_ManagedScript.isLockOwner(this);
		assert m_AssertedPrecond != null : "No PrePred asserted";
		assert m_TransType != null : "TransType not set";
		m_AssertedPrecond = null;
		m_ManagedScript.pop(this, 1);
		if (m_TransType == TransType.RETURN) {
			m_HierConstants.endScope();
		}
		if (m_AssertedCodeBlock == null) {
			throw new AssertionError("CodeBlock is assigned first");
		}
	}
	
	
	protected LBool assertCodeBlock(CodeBlock cb) {
		if (m_ManagedScript.isLocked()) {
			m_ManagedScript.requestLockRelease();
		}
		m_ManagedScript.lock(this);
		m_ManagedScript.echo(this, new QuotedObject(s_StartEdgeCheck));
		assert m_AssertedCodeBlock == null : "CodeBlock already asserted";
		assert m_TransType == null : "TransType already asserted";
		m_AssertedCodeBlock = cb;
		if (m_AssertedCodeBlock instanceof Return) {
			m_TransType = TransType.RETURN;
		} else if (m_AssertedCodeBlock instanceof Call) {
			m_TransType = TransType.CALL;
		} else {
			m_TransType = TransType.INTERNAL;
		}
		m_EdgeCheckerBenchmark.continueEdgeCheckerTime();
		m_ManagedScript.push(this, 1);
		m_TransFormula = cb.getTransitionFormula();
		Term cbFormula = m_TransFormula.getClosedFormula();
		
		if (m_UseNamedTerms) {
			Annotation annot = new Annotation(":named", s_IdTransitionFormula);
			cbFormula = m_ManagedScript.annotate(this, cbFormula, annot);
		}
		LBool quickCheck = m_ManagedScript.assertTerm(this, cbFormula);
		if (cb instanceof Return) {
			m_HierConstants = new ScopedHashMap<BoogieVar,Term>();
			Return ret = (Return) cb;
			Call call = ret.getCorrespondingCall();
			String proc = call.getCallStatement().getMethodName();
			TransFormula ovaTF = m_ModifiableGlobalVariableManager.
					getOldVarsAssignment(proc);
			Term ovaFormula = ovaTF.getFormula();

			//rename modifiable globals to Hier vars
			ovaFormula = renameVarsToHierConstants(ovaTF.getInVars(), ovaFormula);
			//rename oldVars of modifiable globals to default vars
			ovaFormula = renameVarsToDefaultConstants(ovaTF.getOutVars(), ovaFormula);
			if (m_UnletTerms ) {
				ovaFormula = (new FormulaUnLet()).unlet(ovaFormula);
			}
			assert ovaFormula.getFreeVars().length == 0;
			if (m_UseNamedTerms) {
				Annotation annot = new Annotation(":named", s_IdTransitionModifiableGlobalEquality);
				ovaFormula = m_ManagedScript.annotate(this, ovaFormula, annot);
			}
			quickCheck = m_ManagedScript.assertTerm(this, ovaFormula);
			
			Set<BoogieVar> modifiableGlobals = ovaTF.getInVars().keySet();
			TransFormula callTf = call.getTransitionFormula();
			Term locVarAssign = callTf.getFormula();
			//TODO: rename non-modifiable globals to DefaultConstants
			locVarAssign = renameNonModifiableGlobalsToDefaultConstants(
					callTf.getInVars(), modifiableGlobals, locVarAssign);
			
			//rename arguments vars to Hier vars
			locVarAssign = renameVarsToHierConstants(callTf.getInVars(), locVarAssign);
			//rename proc parameter vars to DefaultConstants
			locVarAssign = renameVarsToDefaultConstants(callTf.getOutVars(), locVarAssign);
			//rename auxiliary vars to fresh constants
			locVarAssign = renameAuxVarsToCorrespondingConstants(callTf.getAuxVars(), locVarAssign);
			if (m_UnletTerms ) {
				locVarAssign = (new FormulaUnLet()).unlet(locVarAssign);
			}
			assert locVarAssign.getFreeVars().length == 0;
			if (m_UseNamedTerms) {
				Annotation annot = new Annotation(":named", s_IdLocalVarsAssignment);
				locVarAssign = m_ManagedScript.annotate(this, locVarAssign, annot);
			}
			quickCheck = m_ManagedScript.assertTerm(this, locVarAssign);
		}
		m_EdgeCheckerBenchmark.stopEdgeCheckerTime();
		return quickCheck;
	}

	
	protected void unAssertCodeBlock() {
		assert m_ManagedScript.isLockOwner(this);
		assert m_AssertedCodeBlock != null : "No CodeBlock asserted";
		assert m_TransType != null : "No TransType determined";
		m_AssertedCodeBlock = null;
		m_TransType = null;
		m_HierConstants = null;
		m_ManagedScript.pop(this, 1);
		if (m_AssertedPrecond == null) {
			m_ManagedScript.echo(this, new QuotedObject(s_EndEdgeCheck));
			m_ManagedScript.unlock(this);
		} else {
			throw new AssertionError("CodeBlock is unasserted last");
		}
	}
	
	
	protected LBool assertHierPred(IPredicate p) {
		assert m_ManagedScript.isLockOwner(this);
		assert m_AssertedCodeBlock != null : "assert Return first";
		assert m_AssertedCodeBlock instanceof Return : "assert Return first";
		assert m_TransType != null : "determine TransType first";
		assert m_AssertedPrecond != null : "assert precond fist";
		assert m_AssertedHier == null : "HierPred already asserted";
		m_AssertedHier = p;
		m_EdgeCheckerBenchmark.continueEdgeCheckerTime();
		m_ManagedScript.push(this, 1);
		m_HierConstants.beginScope();
		Term hierFormula = p.getFormula();
		
		// rename globals that are not modifiable by callee to default constants
		String callee = m_AssertedCodeBlock.getPreceedingProcedure();
		Set<BoogieVar> modifiableGlobalsCallee = m_ModifiableGlobalVariableManager.
				getModifiedBoogieVars(callee);
		hierFormula = renameNonModifiableNonOldGlobalsToDefaultConstants(
				p.getVars(), modifiableGlobalsCallee, hierFormula);
		
		// rename oldvars of globals that are not modifiable by caller to 
		// default constants of nonOldVar
		String caller = m_AssertedCodeBlock.getSucceedingProcedure();
		Set<BoogieVar> modifiableGlobalsCaller = m_ModifiableGlobalVariableManager.
				getModifiedBoogieVars(caller);
		hierFormula = renameNonModifiableOldGlobalsToDefaultConstantOfNonOldVar(
				p.getVars(), modifiableGlobalsCaller, hierFormula);
		
		//rename vars which are assigned on return to Hier vars
		hierFormula = renameVarsToHierConstants(p.getVars(), hierFormula);
		if (m_UnletTerms ) {
			hierFormula = (new FormulaUnLet()).unlet(hierFormula);
		}
		
		//TODO auxvars
		assert hierFormula.getFreeVars().length == 0;
		
		if (m_UseNamedTerms) {
			Annotation annot = new Annotation(":named", s_IdHierarchicalPrecondition);
			hierFormula = m_ManagedScript.annotate(this, hierFormula, annot);
		}
		LBool quickCheck = m_ManagedScript.assertTerm(this, hierFormula);
		
		m_EdgeCheckerBenchmark.stopEdgeCheckerTime();
		return quickCheck;
	}
	

	/**
	 * Return a set of equalities such that for each oldvar old(g) that occurs
	 * in vars and for which the corresponding nonOldVar occurs in 
	 * modifiableGlobalsCaller but not in modifiableGlobalsCallee we add the
	 * equality (= c_old(g) c_g_hier) and
	 * for each nonOldVar that occurs in 
	 * modifiableGlobalsCaller but not in modifiableGlobalsCallee we add the
	 * equality (= c_g c_g_hier),
	 * where c_g is the default constant of the 
	 * global variable g and c_old(g) is the default constant of old(g) and
	 * c_g_hier is the constant for the nonOldVar g at the position of the
	 * hierarchical predecessor.
	 */
	private Collection<Term> constructCalleeNonModOldVarsEquality(Set<BoogieVar> vars,
			Set<BoogieVar> modifiableGlobalsCaller,
			Set<BoogieVar> modifiableGlobalsCallee) {
		if (!modifiableGlobalsCallee.containsAll(modifiableGlobalsCaller)) {
			boolean test = true;
		}
		Collection<Term> conjunction = new ArrayList<>();
		for (BoogieVar bv : vars) {
			if (bv instanceof GlobalBoogieVar) {
				BoogieNonOldVar bnov;
				if (bv instanceof BoogieOldVar) {
					bnov = ((BoogieOldVar) bv).getNonOldVar();
				} else {
					bnov = (BoogieNonOldVar) bv;
				}
				if (modifiableGlobalsCaller.contains(bnov) && 
						!modifiableGlobalsCallee.contains(bnov)) {
					Term hierConst = getOrConstructHierConstant(bnov);
					Term conjunct = SmtUtils.binaryEquality(m_Boogie2Smt.getScript(), bv.getDefaultConstant(), hierConst);
					conjunction.add(conjunct);
				}
			}
		}
		return conjunction;
	}

	
	private void unAssertHierPred() {
		assert m_ManagedScript.isLockOwner(this);
		assert m_AssertedHier != null : "No HierPred asserted";
		assert m_TransType == TransType.RETURN : "Wrong TransType";
		m_AssertedHier = null;
		m_ManagedScript.pop(this, 1);
		m_HierConstants.endScope();
	}
	
	
	private LBool assertPostcond_Internal(IPredicate p) {
		assert m_ManagedScript.isLockOwner(this);
		assert m_AssertedPrecond != null;
		assert m_AssertedCodeBlock != null;
		assert m_TransType == TransType.INTERNAL;
		m_EdgeCheckerBenchmark.continueEdgeCheckerTime();
		m_ManagedScript.push(this, 1);
		m_AssertedPostcond = p;
		
		//OldVars renamed (depending on modifiability)
		//All variables get index 0 
		//assigned vars (locals and globals) get index 1
		//other vars get index 0
		Set<BoogieVar> assignedVars = m_TransFormula.getAssignedVars();
		Term renamedFormula = renameVarsToPrimedConstants(assignedVars, p.getFormula());
		String succProc = m_AssertedCodeBlock.getSucceedingProcedure();
		Set<BoogieVar> modifiableGlobals = m_ModifiableGlobalVariableManager.getModifiedBoogieVars(succProc);
		renamedFormula = renameNonModifiableOldGlobalsToDefaultConstantOfNonOldVar(p.getVars(), modifiableGlobals, renamedFormula);
		renamedFormula = renameVarsToDefaultConstants(p.getVars(), renamedFormula);
		if (m_UnletTerms ) {
			renamedFormula = (new FormulaUnLet()).unlet(renamedFormula);
		}
		assert renamedFormula.getFreeVars().length == 0;
		Term negation = m_ManagedScript.term(this, "not", renamedFormula);
		if (m_UseNamedTerms) {
			Annotation annot = new Annotation(":named", s_IdNegatedPostcondition);
			negation = m_ManagedScript.annotate(this, negation, annot);
		}
		LBool isSat = m_ManagedScript.assertTerm(this, negation);
		m_EdgeCheckerBenchmark.stopEdgeCheckerTime();
		return isSat;
	}
	

	private LBool assertPostcond_Call(IPredicate p) {
		assert m_ManagedScript.isLockOwner(this);
		assert m_AssertedPrecond != null;
		assert m_AssertedCodeBlock != null;
		assert m_TransType == TransType.CALL;
		m_EdgeCheckerBenchmark.continueEdgeCheckerTime();
		m_ManagedScript.push(this, 1);
		m_AssertedPostcond = p;
		
		Set<BoogieVar> boogieVars = p.getVars();
		// rename oldVars to default constants of non-oldvars
		Term renamedFormula = renameGlobalsAndOldVarsToNonOldDefaultConstants(
												boogieVars, p.getFormula());
		// rename remaining variables
		renamedFormula = renameVarsToPrimedConstants(boogieVars, renamedFormula);
		renamedFormula = renameVarsToDefaultConstants(p.getVars(), renamedFormula);
		if (m_UnletTerms ) {
			renamedFormula = (new FormulaUnLet()).unlet(renamedFormula);
		}
		assert renamedFormula.getFreeVars().length == 0;
		Term negation = m_ManagedScript.term(this, "not", renamedFormula);
		if (m_UseNamedTerms) {
			String name = "negatedPostcondition";
			Annotation annot = new Annotation(":named", name);
			negation = m_ManagedScript.annotate(this, negation, annot);
		}
		LBool isSat = m_ManagedScript.assertTerm(this, negation);
		m_EdgeCheckerBenchmark.stopEdgeCheckerTime();
		return isSat;
	}



	private LBool assertPostcond_Return(IPredicate p) {
		assert m_ManagedScript.isLockOwner(this);
		assert m_AssertedPrecond != null;
		assert m_TransType == TransType.RETURN;
		assert m_AssertedHier != null;
		m_EdgeCheckerBenchmark.continueEdgeCheckerTime();
		m_ManagedScript.push(this, 1);
		m_HierConstants.beginScope();
		m_AssertedPostcond = p;
		
		//rename assignedVars to primed vars
		Set<BoogieVar> assignedVars = m_TransFormula.getAssignedVars();
		Term renamedFormula = renameVarsToPrimedConstants(assignedVars, p.getFormula());
		
		String callee = m_AssertedCodeBlock.getPreceedingProcedure();
		Set<BoogieVar> modifiableGlobalsCallee = m_ModifiableGlobalVariableManager.
				getModifiedBoogieVars(callee);
		
		//rename modifiable globals to default constants
		renamedFormula = renameVarsToDefaultConstants(modifiableGlobalsCallee, renamedFormula);
		
		// rename globals that are not modifiable by callee to default constants
		renamedFormula = renameNonModifiableNonOldGlobalsToDefaultConstants(
				p.getVars(), modifiableGlobalsCallee, renamedFormula);
		
		// rename oldvars of globals that are not modifiable by caller to 
		// default constants of nonOldVar
		String caller = m_AssertedCodeBlock.getSucceedingProcedure();
		Set<BoogieVar> modifiableGlobalsCaller = m_ModifiableGlobalVariableManager.
				getModifiedBoogieVars(caller);
		renamedFormula = renameNonModifiableOldGlobalsToDefaultConstantOfNonOldVar(
				p.getVars(), modifiableGlobalsCaller, renamedFormula);
		
		// rename remaining non-old Globals to default constants
//		renamedFormula = renameNonOldGlobalsToDefaultConstants(p.getVars(), renamedFormula);
		
		//rename remaining vars to Hier vars
		renamedFormula = renameVarsToHierConstants(p.getVars(), renamedFormula);
		
		if (m_UnletTerms ) {
			renamedFormula = (new FormulaUnLet()).unlet(renamedFormula);
		}
		assert renamedFormula.getFreeVars().length == 0;
		Term negation = m_ManagedScript.term(this, "not", renamedFormula);

		if (m_UseNamedTerms) {
			String name = "negatedPostcondition";
			Annotation annot = new Annotation(":named", name);
			negation = m_ManagedScript.annotate(this, negation, annot);
		}
		LBool isSat = m_ManagedScript.assertTerm(this, negation);
		m_EdgeCheckerBenchmark.stopEdgeCheckerTime();
		return isSat;
	}
	
	private void unAssertPostcondition() {
		assert m_ManagedScript.isLockOwner(this);
		assert m_AssertedCodeBlock != null : "Assert CodeBlock first!";
		assert m_AssertedPrecond != null : "Assert precond first!";
		assert m_AssertedPostcond != null : "Assert postcond first!";
		m_AssertedPostcond = null;
		m_ManagedScript.pop(this, 1);
		if (m_TransType == TransType.RETURN) {
			assert m_HierConstants != null : "Assert hierPred first!";
			assert m_AssertedHier != null : "Assert hierPred first!";
			m_HierConstants.endScope();
		}
	}

	

	
	protected Validity checkValidity() {
		assert m_ManagedScript.isLockOwner(this);
		assert m_AssertedCodeBlock != null : "Assert CodeBlock first!";
		assert m_AssertedPrecond != null : "Assert precond first! ";
		assert m_AssertedPostcond != null : "Assert postcond first! ";
		m_EdgeCheckerBenchmark.continueEdgeCheckerTime();
		final LBool isSat = m_ManagedScript.checkSat(this);
		switch (isSat) {
		case SAT:
			m_EdgeCheckerBenchmark.getSolverCounterSat().incRe();
			break;
		case UNKNOWN:
			m_EdgeCheckerBenchmark.getSolverCounterUnknown().incRe();
			break;
		case UNSAT:
			m_EdgeCheckerBenchmark.getSolverCounterUnsat().incRe();
			break;
		default:
			throw new AssertionError("unknown case");
		}
		m_EdgeCheckerBenchmark.stopEdgeCheckerTime();
		return IHoareTripleChecker.lbool2validity(isSat);
	}
	

	
	

	
	
	private Term renameVarsToDefaultConstants(Set<BoogieVar> boogieVars, Term formula) {
		ArrayList<TermVariable> replacees = new ArrayList<TermVariable>();
		ArrayList<Term> replacers = new ArrayList<Term>();
		for (BoogieVar bv : boogieVars) {
			replacees.add(bv.getTermVariable());
			replacers.add(bv.getDefaultConstant());
		}
		TermVariable[] vars = replacees.toArray(new TermVariable[replacees.size()]);
		Term[] values = replacers.toArray(new Term[replacers.size()]);
		return m_ManagedScript.let(this, vars , values, formula);
	}
	
	
	private Term renameVarsToDefaultConstants(Map<BoogieVar, TermVariable> bv2tv, Term formula) {
		ArrayList<TermVariable> replacees = new ArrayList<TermVariable>();
		ArrayList<Term> replacers = new ArrayList<Term>();
		for (BoogieVar bv : bv2tv.keySet()) {
			replacees.add(bv2tv.get(bv));
			replacers.add(bv.getDefaultConstant());
		}
		TermVariable[] vars = replacees.toArray(new TermVariable[replacees.size()]);
		Term[] values = replacers.toArray(new Term[replacers.size()]);
		return m_ManagedScript.let(this, vars , values, formula);
	}
	
	
	private Term renameVarsToPrimedConstants(Set<BoogieVar> boogieVars, Term formula) {
		ArrayList<TermVariable> replacees = new ArrayList<TermVariable>();
		ArrayList<Term> replacers = new ArrayList<Term>();
		for (BoogieVar bv : boogieVars) {
			replacees.add(bv.getTermVariable());
			replacers.add(bv.getPrimedConstant());
		}
		TermVariable[] vars = replacees.toArray(new TermVariable[replacees.size()]);
		Term[] values = replacers.toArray(new Term[replacers.size()]);
		return m_ManagedScript.let(this, vars , values, formula);
	}


	private Term renameVarsToHierConstants(Set<BoogieVar> boogieVars, Term formula) {
		ArrayList<TermVariable> replacees = new ArrayList<TermVariable>();
		ArrayList<Term> replacers = new ArrayList<Term>();
		for (BoogieVar bv : boogieVars) {
			replacees.add(bv.getTermVariable());
			replacers.add(getOrConstructHierConstant(bv));
		}
		TermVariable[] vars = replacees.toArray(new TermVariable[replacees.size()]);
		Term[] values = replacers.toArray(new Term[replacers.size()]);
		return m_ManagedScript.let(this, vars , values, formula);
	}
	
	private Term renameVarsToHierConstants(Map<BoogieVar, TermVariable> bv2tv, Term formula) {
		ArrayList<TermVariable> replacees = new ArrayList<TermVariable>();
		ArrayList<Term> replacers = new ArrayList<Term>();
		for (BoogieVar bv : bv2tv.keySet()) {
			replacees.add(bv2tv.get(bv));
			replacers.add(getOrConstructHierConstant(bv));
		}
		TermVariable[] vars = replacees.toArray(new TermVariable[replacees.size()]);
		Term[] values = replacers.toArray(new Term[replacers.size()]);
		return m_ManagedScript.let(this, vars , values, formula);
	}
	
	private Term renameAuxVarsToCorrespondingConstants(Set<TermVariable> auxVars,
			Term formula) {
		ArrayList<TermVariable> replacees = new ArrayList<TermVariable>();
		ArrayList<Term> replacers = new ArrayList<Term>();
		for (TermVariable tv : auxVars) {
			replacees.add(tv);
			Term correspondingConstant = m_Boogie2Smt.
					getVariableManager().getCorrespondingConstant(tv); 
			replacers.add(correspondingConstant);
		}
		TermVariable[] vars = replacees.toArray(new TermVariable[replacees.size()]);
		Term[] values = replacers.toArray(new Term[replacers.size()]);
		return m_ManagedScript.let(this, vars , values, formula);
	}


	private Term getOrConstructHierConstant(BoogieVar bv) {
		Term preHierConstant = m_HierConstants.get(bv);
		if (preHierConstant == null) {
			String name = "c_" + bv.getTermVariable().getName() + "_Hier";
			Sort sort = bv.getTermVariable().getSort();
			m_ManagedScript.declareFun(this, name, new Sort[0], sort);
			preHierConstant = m_ManagedScript.term(this, name);
			m_HierConstants.put(bv, preHierConstant);
		}
		return preHierConstant;
	}
	
	

	/**
	 * Rename each g in boogieVars that is not contained in modifiableGlobals
	 * to c_g, where c_g is the default constant for g.
	 */
	private Term renameNonModifiableNonOldGlobalsToDefaultConstants(
			Set<BoogieVar> boogieVars, 
			Set<BoogieVar> modifiableGlobals,
			Term formula) {
		ArrayList<TermVariable> replacees = new ArrayList<TermVariable>();
		ArrayList<Term> replacers = new ArrayList<Term>();
		for (BoogieVar bv : boogieVars) {
			if (bv.isGlobal()) {
				if (bv instanceof BoogieNonOldVar) {
					if (modifiableGlobals.contains(bv)) {
						//do nothing
					} else {
						replacees.add(bv.getTermVariable());
						replacers.add(bv.getDefaultConstant());
					}
				}
			}
		}
		TermVariable[] vars = replacees.toArray(new TermVariable[replacees.size()]);
		Term[] values = replacers.toArray(new Term[replacers.size()]);
		return m_ManagedScript.let(this, vars , values, formula);
	}
	
	
	/**
	 * Rename oldVars old(g) of non-modifiable globals to the
	 * default constants of g. 
	 */
	private Term renameNonModifiableOldGlobalsToDefaultConstantOfNonOldVar(
			Set<BoogieVar> boogieVars, 
			Set<BoogieVar> modifiableGlobals,
			Term formula) {
		ArrayList<TermVariable> replacees = new ArrayList<TermVariable>();
		ArrayList<Term> replacers = new ArrayList<Term>();
		for (BoogieVar bv : boogieVars) {
			if (bv instanceof BoogieOldVar) {
				BoogieNonOldVar nonOldVar = ((BoogieOldVar) bv).getNonOldVar();
				if (modifiableGlobals.contains(nonOldVar)) {
					//do nothing
				} else {
					replacees.add(bv.getTermVariable());
					replacers.add(nonOldVar.getDefaultConstant());
				}
				
			}
		}
		TermVariable[] vars = replacees.toArray(new TermVariable[replacees.size()]);
		Term[] values = replacers.toArray(new Term[replacers.size()]);
		return m_ManagedScript.let(this, vars , values, formula);
	}

	
	private Term renameNonModifiableGlobalsToDefaultConstants(
			Map<BoogieVar,TermVariable> boogieVars, 
			Set<BoogieVar> modifiableGlobals,
			Term formula) {
		ArrayList<TermVariable> replacees = new ArrayList<TermVariable>();
		ArrayList<Term> replacers = new ArrayList<Term>();
		for (BoogieVar bv : boogieVars.keySet()) {
			if (bv.isGlobal()) {
				if (bv.isOldvar()) {
					assert !modifiableGlobals.contains(bv);
					// do nothing
				} else {
					if (modifiableGlobals.contains(bv)) {
						//do noting
					} else {
						//oldVar of global which is not modifiable by called proc
						replacees.add(boogieVars.get(bv));
						replacers.add(bv.getDefaultConstant());
					}
				}
			} else {
				assert !modifiableGlobals.contains(bv);
			}
		}
		TermVariable[] vars = replacees.toArray(new TermVariable[replacees.size()]);
		Term[] values = replacers.toArray(new Term[replacers.size()]);
		return m_ManagedScript.let(this, vars , values, formula);
	}
	
	
	private Term renameGlobalsAndOldVarsToNonOldDefaultConstants(
			Set<BoogieVar> boogieVars, 
			Term formula) {
		ArrayList<TermVariable> replacees = new ArrayList<TermVariable>();
		ArrayList<Term> replacers = new ArrayList<Term>();
		for (BoogieVar bv : boogieVars) {
			if (bv.isGlobal()) {
				if (bv.isOldvar()) {
					replacees.add(bv.getTermVariable());
					BoogieVar nonOldbv = ((BoogieOldVar) bv).getNonOldVar();
					replacers.add(nonOldbv.getDefaultConstant());
				} else {
					replacees.add(bv.getTermVariable());
					replacers.add(bv.getDefaultConstant());
				}
			}
		}
		TermVariable[] vars = replacees.toArray(new TermVariable[replacees.size()]);
		Term[] values = replacers.toArray(new Term[replacers.size()]);
		return m_ManagedScript.let(this, vars , values, formula);
	}
	
	
	public boolean isAssertionStackEmpty() {
		if (m_AssertedCodeBlock == null) {
			assert m_AssertedPrecond == null;
			assert m_AssertedHier == null;
			return true;
		} else {
			return false;
		}
	}
	
}