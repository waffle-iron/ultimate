package de.uni_freiburg.informatik.ultimate.boogie.procedureinliner.backtranslation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.boogie.procedureinliner.BackTransValue;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.BoogieASTNode;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.result.IProgramExecution.AtomicTraceElement;
import de.uni_freiburg.informatik.ultimate.result.IProgramExecution.AtomicTraceElement.StepInfo;

/**
 * Analyzes a trace from an inlined boogie program and offers calls/returns to be inserted,
 * before an new inlined call begins/ends.
 * 
 * @author schaetzc@informatik.uni-freiburg.de
 */
public class CallReinserter {
	
	/** The processing takes place, right after an non-inlined call. */
	private boolean mAfterNonInlinedCall = false;
	
	/**
	 * Last BackTransValues for all trace sections with the same inline entry point.
	 * <p>
	 * There is a BackTransValue for every unreturned non-inlined call.
	 * The stack height increases after (!) non-inlined calls
	 * and decreases before non-inlined returns.
	 */
	private Deque<BackTransValue> mPrevBackTranslations = new ArrayDeque<>();
	
	/**
	 * Needs to be called for every trace element and in the order of the trace to work correct.
	 * @param curTraceElem Current trace element.
	 * @param curBackTrans trace element's value from the backtranslation map.
	 * @return CallStatements to be inserted before the current trace element.
	 */
	public List<AtomicTraceElement<BoogieASTNode>> recoverInlinedCallsBefore(
			AtomicTraceElement<BoogieASTNode> curTraceElem, BackTransValue curBackTrans) {
		List<AtomicTraceElement<BoogieASTNode>> recoveredCalls = new ArrayList<>();
		boolean nonInlinedCall = curTraceElem.hasStepInfo(StepInfo.PROC_CALL);
		boolean nonInlinedReturn = curTraceElem.hasStepInfo(StepInfo.PROC_RETURN);
		assert !(nonInlinedCall && nonInlinedReturn) : "Simultaneous call and return: " + curTraceElem;

		if (nonInlinedReturn && !mPrevBackTranslations.isEmpty()) {
			BackTransValue prevBackTrans = mPrevBackTranslations.peek();
			if (prevBackTrans != curBackTrans) {
				mPrevBackTranslations.pop();
				for (CallStatement cs : prevBackTrans.getOriginalCallStack()) {
					recoveredCalls.add(makeAtomicReturn(cs));
				}
			} // else
				// there were no inlined nodes inside the called procedure
			mAfterNonInlinedCall = false;
		}

		if (curBackTrans == null) {
			// goto end
		} else if (mPrevBackTranslations.isEmpty() || mAfterNonInlinedCall) {
			Iterator<CallStatement> stackRevIter = curBackTrans.getOriginalCallStack().descendingIterator();
			while (stackRevIter.hasNext()) {
				recoveredCalls.add(makeAtomicCall(stackRevIter.next()));
			}
		} else {
			BackTransValue prevBackTrans = mPrevBackTranslations.pop();
			Deque<CallStatement> prevStack = prevBackTrans.getOriginalCallStack();
			Deque<CallStatement> curStack = curBackTrans.getOriginalCallStack();
			if (prevStack != curStack) {
				Iterator<CallStatement> prevStackRevIter = prevStack.descendingIterator(); // from stack bottom to top
				Iterator<CallStatement> curStackRevIter = curStack.descendingIterator();
				List<AtomicTraceElement<BoogieASTNode>> returns = new ArrayList<>();
				List<AtomicTraceElement<BoogieASTNode>> calls = new ArrayList<>();
				while (prevStackRevIter.hasNext() && curStackRevIter.hasNext()) {
					CallStatement prevCS = prevStackRevIter.next();
					CallStatement curCS = curStackRevIter.next();
					if (prevCS != curCS) {
						returns.add(makeAtomicReturn(prevCS));
						calls.add(makeAtomicCall(curCS));
					}
				}
				while (prevStackRevIter.hasNext()) {
					returns.add(makeAtomicReturn(prevStackRevIter.next()));
				}
				while (curStackRevIter.hasNext()) {
					calls.add(makeAtomicCall(curStackRevIter.next()));
				}
				Collections.reverse(returns);
				recoveredCalls.addAll(returns);
				recoveredCalls.addAll(calls);
			}
		}

		if (curBackTrans != null) {
			mPrevBackTranslations.push(curBackTrans);
		}
		mAfterNonInlinedCall = nonInlinedCall;
		return recoveredCalls;
	}
	
	private AtomicTraceElement<BoogieASTNode> makeAtomicCall(CallStatement originalCall) {
		return new AtomicTraceElement<BoogieASTNode>(originalCall, originalCall, StepInfo.PROC_CALL);
	}
	
	private AtomicTraceElement<BoogieASTNode> makeAtomicReturn(CallStatement originalReturn) {
		return new AtomicTraceElement<BoogieASTNode>(originalReturn, originalReturn, StepInfo.PROC_RETURN);
	}
	
	/**
	 * Creates the set of procedures (more specifically, their identifiers), that where called but didn't return yet.
	 * Note that non-inlined calls aren't included.
	 * @return Set of unreturned inlined procedures.
	 */
	public Set<String> unreturnedInlinedProcedures() {
		Set<String> unretInldProcs = new HashSet<>();
		for (BackTransValue btv : mPrevBackTranslations) {
			for (CallStatement cs : btv.getOriginalCallStack()) {
				unretInldProcs.add(cs.getMethodName());				
			}
			unretInldProcs.add(btv.getInlineEntryProcId());
		}
		return unretInldProcs;
	}	
}