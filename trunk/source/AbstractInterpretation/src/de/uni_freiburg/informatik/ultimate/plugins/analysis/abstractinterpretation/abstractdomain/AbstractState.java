/**
 * 
 */
package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretation.abstractdomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.model.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretation.abstractdomain.booldomain.BoolValue;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ProgramPoint;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RCFGEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RCFGNode;

/**
 * An AbstractState represents an abstract program state, mapping variable
 * identifiers to abstract values. As variables can have different types,
 * for instance int or bool, different abstract domains may be used.
 * 
 * @author Christopher Dillo
 */
public class AbstractState {
	
	/**
	 * Stores information on a loop stack level, that is:
	 * - The loop entry node of the current loop,
	 * - The exit edge over which the loop will be left
	 * - Iteration counts for loops nested within the current loop
	 */
	public class LoopStackElement {
		private final ProgramPoint m_loopNode;
		private final RCFGEdge m_exitEdge;
		private final Map<ProgramPoint, Integer> m_iterationCounts = new HashMap<ProgramPoint, Integer>();
		public LoopStackElement(ProgramPoint loopNode, RCFGEdge exitEdge) {
			m_loopNode = loopNode;
			m_exitEdge = exitEdge;
		}
		public ProgramPoint getLoopNode() { return m_loopNode; }
		public RCFGEdge getExitEdge() { return m_exitEdge; }
		public int getIterationCount(ProgramPoint loopNode) { 
			Integer count = m_iterationCounts.get(loopNode);
			if (count == null) return 0;
			return count.intValue();
		}
		public void increaseIterationCount(ProgramPoint loopNode) {
			m_iterationCounts.put(loopNode, Integer.valueOf(getIterationCount(loopNode) + 1));
		}
		public LoopStackElement copy() {
			LoopStackElement result = new LoopStackElement(m_loopNode, m_exitEdge);
			for (ProgramPoint p : m_iterationCounts.keySet())
				result.m_iterationCounts.put(p, m_iterationCounts.get(p));
			return result;
		}
	}
	
	public class CallStackElement {
		private final CallStatement m_callStatement;
		private final Map<String, IAbstractValue<?>> m_values, m_oldValues;
		private final LinkedList<LoopStackElement> m_loopStack = new LinkedList<LoopStackElement>();
		public CallStackElement(CallStatement callStatement, Map<String, IAbstractValue<?>> oldValues) {
			m_callStatement = callStatement;
			m_values = new HashMap<String, IAbstractValue<?>>();
			m_oldValues = oldValues == null ? new HashMap<String, IAbstractValue<?>>() : new HashMap<String, IAbstractValue<?>>(oldValues);
			m_loopStack.add(new LoopStackElement(null, null)); // global stack element
		}
		public CallStatement getCallStatement() { return m_callStatement; }
		public Map<String, IAbstractValue<?>> getValues() { return m_values; }
		public Map<String, IAbstractValue<?>> getOldValues() { return m_oldValues; }
		public LinkedList<LoopStackElement> getLoopStack() { return m_loopStack; }
	}
	
	/**
	 * Sequence of nodes passed leading to this state -> "execution trace" 
	 */
	private final List<RCFGNode> m_passedNodes = new LinkedList<RCFGNode>();
	
	/**
	 * True if the state at a node has been processed already (prevent processing the same state twice)
	 */
	private boolean m_isProcessed = false;
	
	private Logger m_logger;
	
	private IAbstractDomainFactory<?> m_numberFactory;
	private IAbstractDomainFactory<BoolValue.Bool> m_boolFactory;
	
	/**
	 * Stack of maps from variable identifiers to values. Stack levels represent scope levels,
	 * index 0 is the global scope.
	 */
	private final LinkedList<CallStackElement> m_callStack = new LinkedList<CallStackElement>();
	
	public AbstractState(Logger logger, IAbstractDomainFactory<?> numberFactory, IAbstractDomainFactory<BoolValue.Bool> boolFactory) {
		m_logger = logger;
		m_numberFactory = numberFactory;
		m_boolFactory = boolFactory;
		
		pushStackLayer(null); // global scope
		
		pushLoopEntry(null, null); // global iteration count
	}
	
	/**
	 * @return True iff this state contains all variables of the given state
	 * and all variable values are greater or equal to their corresponding values
	 */
	public boolean isSuper(AbstractState state) {
		// referring to this state as "greater"
		
		if (state == null)
			return false;
		
		List<CallStackElement> otherValues = state.getCallStack();
		
		// must have at least as many stack layers (scopes)
		if (m_callStack.size() < otherValues.size())
			return false;
		
		// for each stack layer (scope level) of the others (which may be less!)
		for (int i = 0; i < otherValues.size(); i++) {
			CallStackElement greaterLayer = m_callStack.get(i);
			CallStackElement smallerLayer = otherValues.get(i);
			
			// must be of the same method call and have at least as many variables
			if ((greaterLayer.getCallStatement() != smallerLayer.getCallStatement())
					|| (greaterLayer.getValues().size() < smallerLayer.getValues().size()))
				return false;

			// check if any variable in the other state occurs and is greater in this state
			Set<String> smallerKeys = smallerLayer.getValues().keySet();
			for (String key : smallerKeys) {
				IAbstractValue<?> smallerValue = smallerLayer.getValues().get(key);
				IAbstractValue<?> greaterValue = greaterLayer.getValues().get(key); 
				
				// identifier must exist and thus have a value
				if (greaterValue == null)
					return false;
				
				// value of this state must be greater than the value of the other state
				if (!greaterValue.isSuper(smallerValue))
					return false;
			}
		}
		
		// all checks passed, this state is greater or equal to the argument state
		return true;
	}
	
	/**
	 * @return A copy of this abstract program state that is independent of this object.
	 */
	public AbstractState copy() {
		AbstractState result = new AbstractState(m_logger, m_numberFactory, m_boolFactory);
		
		result.m_callStack.clear();
		for (int i = 0; i < m_callStack.size(); i++) {
			CallStackElement cse = m_callStack.get(i);
			
			Map<String, IAbstractValue<?>> thisLayer = cse.getValues();
			
			CallStackElement resultCSE = new CallStackElement(cse.getCallStatement(), cse.getOldValues());
			result.m_callStack.add(resultCSE);
			Map<String, IAbstractValue<?>> copyLayer = result.m_callStack.get(i).getValues();
			for (String identifier : thisLayer.keySet()) {
				IAbstractValue<?> originalValue = thisLayer.get(identifier);
				if (originalValue == null) {
					m_logger.error(String.format("Invalid null value for identifier \"%s\" found.", identifier));
				} else {
					copyLayer.put(identifier, originalValue.copy());
				}
			}
			
			resultCSE.getLoopStack().clear();
			resultCSE.getLoopStack().addAll(cse.getLoopStack());
		}
		
		
		result.m_passedNodes.addAll(m_passedNodes);
		
		return result;
	}
	
	/**
	 * Merge this state with the given state using the merge operator set in the preferences
	 * @param state The state to merge with
	 * @return A new merged state
	 */
	public AbstractState merge(AbstractState state) {		
		if (state == null)
			return null;
		
		IMergeOperator<?> mergeOp = m_numberFactory.getMergeOperator();
		IWideningOperator<BoolValue.Bool> boolMergeOp = m_boolFactory.getWideningOperator();
		
		List<CallStackElement> otherValues = state.getCallStack();

		AbstractState resultingState = new AbstractState(m_logger, m_numberFactory, m_boolFactory);
		List<CallStackElement> resultingValues = resultingState.getCallStack();
		
		int maxLayerCount = Math.max(m_callStack.size(), otherValues.size());
		
		// for each stack layer (scope level)
		resultingState.m_callStack.clear();
		for (int i = 0; i < maxLayerCount; i++) {
			CallStackElement thisCSE = (i < m_callStack.size()) ? m_callStack.get(i) : null;
			CallStackElement otherCSE = (i < otherValues.size()) ? otherValues.get(i) : null;
			Map<String, IAbstractValue<?>> thisLayer = (thisCSE != null) ? thisCSE.getValues() : null;
			Map<String, IAbstractValue<?>> otherLayer = (otherCSE != null) ? otherCSE.getValues() : null;
			
			CallStackElement useCSE = thisCSE == null ? otherCSE : thisCSE;
			CallStackElement resultCSE = new CallStackElement(useCSE.getCallStatement(), useCSE.getOldValues());
			resultingState.m_callStack.add(resultCSE);
			Map<String, IAbstractValue<?>> resultingLayer = resultingValues.get(i).getValues();

			Set<String> identifiers = new HashSet<String>();
			if (thisLayer != null)
				identifiers.addAll(thisLayer.keySet());
			if (otherLayer != null)
				identifiers.addAll(otherLayer.keySet());

			// merge values (or take the single value if only one is present)
			for (String identifier : identifiers) {
				IAbstractValue<?> thisValue = (thisLayer == null) ? null : thisLayer.get(identifier);
				IAbstractValue<?> otherValue = (otherLayer == null) ? null : otherLayer.get(identifier); 

				IAbstractValue<?> resultingValue;
				if (thisValue == null) {
					resultingValue = otherValue.copy();
				} else if (otherValue == null) {
					resultingValue = thisValue.copy();
				} else {
					if (thisValue instanceof BoolValue)
						resultingValue = boolMergeOp.apply(thisValue, otherValue);
					else
						resultingValue = mergeOp.apply(thisValue, otherValue);
				}
				if (resultingValue != null)
					resultingLayer.put(identifier, resultingValue);
			}
			
			// add passed loop entry nodes : take larger stack
			resultCSE.getLoopStack().clear();
			resultCSE.getLoopStack().addAll(((thisCSE.getLoopStack().size() >= otherCSE.getLoopStack().size())
							? thisCSE : otherCSE).getLoopStack());
		}

		// add passed nodes of resultingState : take longer trace
		resultingState.m_passedNodes.addAll((m_passedNodes.size() >= state.m_passedNodes.size()) ? m_passedNodes : state.m_passedNodes);
		
		return resultingState;
	}
	
	/**
	 * Widen this state with the given state using the given widening operator set in the preferences
	 * @param state The state to widen with
	 * @return A new widened state: (this state) wideningOp (the given state)
	 */
	public AbstractState widen(AbstractState state) {
		if (state == null)
			return this.copy();
		
		IWideningOperator<?> wideningOp = m_numberFactory.getWideningOperator();
		IWideningOperator<BoolValue.Bool> boolWideningOp = m_boolFactory.getWideningOperator();
		
		List<CallStackElement> otherValues = state.getCallStack();

		AbstractState resultingState = new AbstractState(m_logger, m_numberFactory, m_boolFactory);
		List<CallStackElement> resultingValues = resultingState.getCallStack();
		
		int maxLayerCount = Math.max(m_callStack.size(), otherValues.size());
		
		// for each stack layer (scope level)
		resultingState.m_callStack.clear();
		for (int i = 0; i < maxLayerCount; i++) {
			CallStackElement thisCSE = (i < m_callStack.size()) ? m_callStack.get(i) : null;
			CallStackElement otherCSE = (i < otherValues.size()) ? otherValues.get(i) : null;
			Map<String, IAbstractValue<?>> thisLayer = (thisCSE != null) ? thisCSE.getValues() : null;
			Map<String, IAbstractValue<?>> otherLayer = (otherCSE != null) ? otherCSE.getValues() : null;

			CallStackElement useCSE = thisCSE == null ? otherCSE : thisCSE;
			CallStackElement resultCSE = new CallStackElement(useCSE.getCallStatement(), useCSE.getOldValues());
			resultingState.m_callStack.add(resultCSE);
			Map<String, IAbstractValue<?>> resultingLayer = resultingValues.get(i).getValues();

			Set<String> identifiers = new HashSet<String>();
			if (thisLayer != null)
				identifiers.addAll(thisLayer.keySet());
			if (otherLayer != null)
				identifiers.addAll(otherLayer.keySet());

			// widen values: thisValue wideningOp otherValue
			for (String identifier : identifiers) {
				IAbstractValue<?> thisValue = (thisLayer == null) ? null : thisLayer.get(identifier);
				IAbstractValue<?> otherValue = (otherLayer == null) ? null : otherLayer.get(identifier); 

				IAbstractValue<?> resultingValue = null;
				if (thisValue == null) {
					resultingValue = otherValue.copy();
					m_logger.warn(String.format("Widening encountered a missing value for %s in the old state.", identifier));
				} else if (otherValue == null) {
					m_logger.error(String.format("Widening failed with a missing value for %s in the new state.", identifier));
				} else {
					if (thisValue instanceof BoolValue)
						resultingValue = boolWideningOp.apply(thisValue, otherValue);
					else
						resultingValue = wideningOp.apply(thisValue, otherValue);
				}
				if (resultingValue != null)
					resultingLayer.put(identifier, resultingValue);
			}

			// add passed loop entry nodes : take stack from given (supposedly newer) state
			resultCSE.getLoopStack().clear();
			for (LoopStackElement e : otherCSE.getLoopStack())
				resultCSE.getLoopStack().add(e.copy());
		}

		// add passed nodes of resultingState : take trace from given (supposedly newer) state
		for (RCFGNode node : state.m_passedNodes)
			resultingState.addPassedNode(node);
		
		return resultingState;
	}
	
	/**
	 * @param identifier
	 * @return The current scope if it has the given identifier,
	 * 		else the global scope if that one does,
	 * 		else null if none of them has it. 
	 */
	private CallStackElement getScopeOfIdentifier(String identifier) {
		CallStackElement cse = m_callStack.peek();
		if (cse.getValues().containsKey(identifier))
			return cse;
		
		cse = m_callStack.getLast(); // GLOBAL scope
		if (cse.getValues().containsKey(identifier))
			return cse;
		
		return null;
	}

	/**
	 * @return The number of stack levels
	 */
	public int getStackSize() {
		return m_callStack.size();
	}
	
	/**
	 * @return The current scope level
	 */
	public CallStackElement getCurrentScope() {
		return m_callStack.peek();
	}
	
	/**
	 * @return The name of the current scope's function
	 */
	public String getCurrentScopeName() {
		CallStatement cs = getCurrentScope().getCallStatement();
		return (cs == null) ? "GLOBAL" : cs.getMethodName();
	}
	
	/**
	 * Creates a new empty symbol table and puts it on the top of the stack
	 * @param CallStatement The call statement of the method call that creates this scope level
	 */
	public void pushStackLayer(CallStatement callStatement) {
		Map<String, IAbstractValue<?>> oldValues = m_callStack.isEmpty() ? null : m_callStack.getLast().getValues();
		m_callStack.push(new CallStackElement(callStatement, oldValues));
	}
	
	/**
	 * Removes the topmost symbol table from the stack unless it is the only layer
	 * @return True if there was a table to pop, false if the stack only has a single layer
	 */
	public boolean popStackLayer() {
		int size = m_callStack.size();
		
		if (size <= 1)
			return false;
		
		m_callStack.pop();
		return true;
	}
	
	/**
	 * Assigns the given value to the topmost occurance of the identifier in the stack.
	 * @param identifier An existing identifier
	 * @param value The new value
	 * @return True iff a layer with the given identifier exists so the value could be written
	 */
	public boolean writeValue(String identifier, IAbstractValue<?> value) {
		CallStackElement layer = getScopeOfIdentifier(identifier);
		
		if (layer == null)
			return false;
		
		layer.getValues().put(identifier, value);
		
		return true;
	}
	
	/**
	 * @param identifier The identifier whose value is to be retrieved
	 * @param old Set to true if instead of "x" you want "old(x)" - for global variables only, else you just get "x"
	 * @return The value associated with the identifier on its scope level, or null if it is not found
	 */
	public IAbstractValue<?> readValue(String identifier, boolean old) {
		CallStackElement scope = old ? getCurrentScope() : getScopeOfIdentifier(identifier);
		
		if (scope == null)
			return null;
		
		if (old) {
			IAbstractValue<?> result = scope.getOldValues().get(identifier);
			if (result != null)
				return result;
		}
		
		return scope.getValues().get(identifier);
	}
	
	/**
	 * Generates a new mapping for the given identifier on the topmost stack level if it does not exist there already.
	 * @param identifier The new identifier to declare
	 * @param initialValue Its initial value
	 * @param asGlobal Set to true if the variable is a global variable
	 * @return True if it could be declared, false if such an identifier already exists on the top layer or the stack is empty
	 */
	public boolean declareIdentifier(String identifier, IAbstractValue<?> initialValue, boolean asGlobal) {
		CallStackElement layer = asGlobal ? m_callStack.getLast() : m_callStack.peek();

		m_logger.debug(String.format("New variable %s at scope level %d %s", identifier, getStackSize()-1,
				getCurrentScopeName()));
		
		if ((layer == null) || (layer.getValues().containsKey(identifier))) {
			m_logger.error("Cannot declare identifier!");
			return false;
		}
		
		layer.getValues().put(identifier, initialValue);
		
		return true;
	}
	
	/**
	 * Add a loop entry to the loop entry stack
	 * (Stack of loop entry nodes along with the edge taken to enter the loop; used to make sure
	 *  widening is applied properly in nested loops)
	 * @param loopNode Loop entry node
	 * @param entryEdge The edge over which the loop will be left
	 */
	public void pushLoopEntry(ProgramPoint loopNode, RCFGEdge exitEdge) {
		getCurrentScope().getLoopStack().push(new LoopStackElement(loopNode, exitEdge));
	}

	/**
	 * Remove the top element of the loop entry stack
	 * @return The removed old top element of the loop entry stack
	 */
	public LoopStackElement popLoopEntry() {
		LinkedList<LoopStackElement> loopStack = getCurrentScope().getLoopStack();
		if (loopStack.size() <= 1) {
			m_logger.warn("Tried to pop the last, global loop stack level.");
			return null;
		} else {
			LoopStackElement lastLoop = loopStack.pop();
			if (lastLoop != null) {
				LoopStackElement currentLoop = loopStack.peek();
				currentLoop.increaseIterationCount(lastLoop.getLoopNode());
			}
			return lastLoop;
		}
	}
	
	/**
	 * @return The top element of the loop entry stack
	 */
	public LoopStackElement peekLoopEntry() {
		return getCurrentScope().getLoopStack().peek();
	}
	
	/**
	 * @return The stack of loop entry nodes along with the edges over which the loop has been entered
	 */
	public LinkedList<LoopStackElement> getLoopEntryNodes() {
		return getCurrentScope().getLoopStack();
	}
	
	/**
	 * Add a node to the list of passed nodes for trace reconstruction
	 * @param node A node to add
	 * @return True if the node was successfully added
	 */
	public boolean addPassedNode(RCFGNode node) {
		return m_passedNodes.add(node);
	}
	
	/**
	 * @return A list of nodes passed during creating this state
	 */
	public List<RCFGNode> getPassedNodes() {
		return m_passedNodes;
	}
	
	/**
	 * @param predecessor
	 * @return True iff this state's passed node list contains and begins with all passed nodes of the
	 * given state in the same order, plus at least one more node
	 */
	public boolean isSuccessor(AbstractState predecessor) {
		if (m_passedNodes.size() <= predecessor.m_passedNodes.size())
			return false;
		
		for (int i = 0; i < predecessor.m_passedNodes.size(); i++) {
			if (!m_passedNodes.get(i).equals(predecessor.m_passedNodes.get(i)))
				return false;
		}
		
		return true;
	}
	
	/**
	 * @return The stack as a list, bottom layer at index 0.
	 */
	public List<CallStackElement> getCallStack() {
		return m_callStack;
	}
	
	/**
	 * @return True if the state is set as having been processed already
	 */
	public boolean isProcessed() {
		return m_isProcessed;
	}
	
	/**
	 * @param processed Set that the state has been processed already
	 */
	public void setProcessed(boolean processed) {
		m_isProcessed = processed;
	}
}
