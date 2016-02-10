package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.relational.octagon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.Util;
import de.uni_freiburg.informatik.ultimate.model.IType;
import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieVar;
import de.uni_freiburg.informatik.ultimate.model.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SMT;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.BoogieConst;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.util.TypeUtil;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.util.BidirectionalMap;
import de.uni_freiburg.informatik.ultimate.util.relation.Pair;

public class OctDomainState implements IAbstractState<OctDomainState, CodeBlock, IBoogieVar> {
	
	private static int sId = 0;

	/** A human-readable hash code, unique for each object. */
	private final int mId;

	private final Function<OctDomainState, String> mLogStringFunction;
	private Map<String, IBoogieVar> mMapVarToBoogieVar;
	private Map<String, Integer> mMapNumericVarToIndex;
	private Set<String> mNumericNonIntVars;
	private OctMatrix mNumericAbstraction;
	private Map<String, BoolValue> mBooleanAbstraction;

	private boolean mIsLocked = false;
	
	public void lock() {
		mIsLocked = true;
	}
	
	private boolean assertNotLockedBeforeModification() {
		return !mIsLocked;
	};
	
	private boolean assertNotBottomBeforeAssign() {
		return !isBottom();
	};

	private OctDomainState(Function<OctDomainState, String> logStringFunction) {
		mLogStringFunction = logStringFunction;
		mId = sId++;
	}
	
	public static OctDomainState createFreshState(Function<OctDomainState, String> logStringFunction) {
		OctDomainState s = new OctDomainState(logStringFunction);
		s.mMapVarToBoogieVar = new HashMap<>();
		s.mMapNumericVarToIndex = new HashMap<>();
		s.mNumericNonIntVars = new HashSet<>();
		s.mNumericAbstraction = OctMatrix.NEW;
		s.mBooleanAbstraction = new HashMap<>();
		return s;
	}

	public OctDomainState deepCopy() {
		OctDomainState s = new OctDomainState(mLogStringFunction);
		s.mMapVarToBoogieVar = new HashMap<>(mMapVarToBoogieVar);
		s.mMapNumericVarToIndex = new HashMap<>(mMapNumericVarToIndex);
		s.mNumericNonIntVars = new HashSet<>(mNumericNonIntVars);
		s.mNumericAbstraction = mNumericAbstraction.copy();
		s.mBooleanAbstraction = new HashMap<>(mBooleanAbstraction);
		return s;
	}

	/**
	 * Creates a shallow copy of this OctagonDomainState.
	 * Use the {@code unref}&hellip; methods to deep copy single fields
	 * before modifying them.
	 *
	 * @return shallow copy
	 *
	 * @see #unrefOtherMapVarToBoogieVar(OctDomainState)
	 * @see #unrefOtherMapNumericVarToIndex(OctDomainState)
	 * @see #unrefOtherNumericNonIntVars(OctDomainState)
	 * @see #unrefOtherNumericAbstraction(OctDomainState)
	 * @see #unrefOtherBooleanAbstraction(OctDomainState)
	 */
	private OctDomainState shallowCopy() {
		OctDomainState s = new OctDomainState(mLogStringFunction);
		s.mMapVarToBoogieVar = mMapVarToBoogieVar;
		s.mMapNumericVarToIndex = mMapNumericVarToIndex;
		s.mNumericNonIntVars = mNumericNonIntVars;
		s.mNumericAbstraction = mNumericAbstraction;
		s.mBooleanAbstraction = mBooleanAbstraction;
		return s;
	}

	private void unrefOtherMapVarToBoogieVar(OctDomainState other) {
		if (other.mMapVarToBoogieVar == mMapVarToBoogieVar) {
			other.mMapVarToBoogieVar = new HashMap<>(mMapVarToBoogieVar);
		}
	}

	private void unrefOtherMapNumericVarToIndex(OctDomainState other) {
		if (other.mMapNumericVarToIndex == mMapNumericVarToIndex) {
			other.mMapNumericVarToIndex = new HashMap<>(mMapNumericVarToIndex);
		}
	}

	private void unrefOtherNumericNonIntVars(OctDomainState other) {
		if (other.mNumericNonIntVars == mNumericNonIntVars) {
			other.mNumericNonIntVars = new HashSet<>(mNumericNonIntVars);
		}
	}

	private void unrefOtherNumericAbstraction(OctDomainState other) {
		if (other.mNumericAbstraction == mNumericAbstraction) {
			other.mNumericAbstraction = mNumericAbstraction.copy();
		}
	}

	private void unrefOtherBooleanAbstraction(OctDomainState other) {
		if (other.mBooleanAbstraction == mBooleanAbstraction) {
			other.mBooleanAbstraction = new HashMap<>(mBooleanAbstraction);
		}
	}

	@Override
	public Map<String, IBoogieVar> getVariables() {
		return Collections.unmodifiableMap(mMapVarToBoogieVar);
	}

	@Override
	public OctDomainState addVariable(String name, IBoogieVar variable) {
		return addVariables(Collections.singletonMap(name, variable));
	}

	@Override
	public OctDomainState removeVariable(String name, IBoogieVar variable) {
		return removeVariables(Collections.singletonMap(name, variable));
	}

	// TODO document: Returned state is a shallow copy. Modifications of returned state may also effect this state!
	@Override
	public OctDomainState addVariables(Map<String, IBoogieVar> variables) {
		// variables = new TreeMap<>(variables); // fixed iteration order -- essential for fast isEqualTo
		// ... probably no speedup. HashSets should iterate in the same order when adding the very same variables.

		OctDomainState newState = shallowCopy();
		for (Map.Entry<String, IBoogieVar> entry : variables.entrySet()) {
			unrefOtherMapVarToBoogieVar(newState);
			String varName = entry.getKey();
			IBoogieVar newBoogieVar = entry.getValue();
			IBoogieVar oldBoogieVar = newState.mMapVarToBoogieVar.put(varName, newBoogieVar);
			if (oldBoogieVar != null) {
				throw new IllegalArgumentException("Variable name already in use: " + varName);
			}
			IType type = newBoogieVar.getIType();
			if (TypeUtil.isNumeric(type)) {
				unrefOtherMapNumericVarToIndex(newState);
				newState.mMapNumericVarToIndex.put(varName, newState.mMapNumericVarToIndex.size());
				if (TypeUtil.isNumericNonInt(type)) {
					unrefOtherNumericNonIntVars(newState);
					newState.mNumericNonIntVars.add(varName);
				}
			} else if (TypeUtil.isBoolean(type)) {
				unrefOtherBooleanAbstraction(newState);
				newState.mBooleanAbstraction.put(varName, BoolValue.TOP);
			}
			// else: variable has unsupported type and is assumed to be \top at all times
		}
		newState.mNumericAbstraction = mNumericAbstraction
				.addVariables(newState.mMapNumericVarToIndex.size() - mMapNumericVarToIndex.size());
		return newState;
	}

	// TODO document: Returned state is a shallow copy. Do not modify!
	@Override
	public OctDomainState removeVariables(Map<String, IBoogieVar> variables) {

		OctDomainState newState = shallowCopy();
		Set<Integer> indexRemovedNumericVars = new HashSet<>();
		for (String name : variables.keySet()) {
			unrefOtherMapVarToBoogieVar(newState);
			newState.mMapVarToBoogieVar.remove(name);
			if (newState.mMapNumericVarToIndex.containsKey(name)) {
				unrefOtherMapNumericVarToIndex(newState);
				int i = newState.mMapNumericVarToIndex.remove(name);
				indexRemovedNumericVars.add(i);
				if (mNumericNonIntVars.contains(name)) {
					unrefOtherNumericNonIntVars(newState);
					newState.mNumericNonIntVars.remove(name);
				}
			} else if (mBooleanAbstraction.containsKey(name)) {
				unrefOtherBooleanAbstraction(newState);
				newState.mBooleanAbstraction.remove(name);
			}
			// else: variable had an unsupported type => its abstract value (\top) wasn't stored explicitly
			//       or variable was not stored at all => already removed
		}
		if (!indexRemovedNumericVars.isEmpty()) {
			newState.mNumericAbstraction = cachedNormalizedNumericAbstraction().removeVariables(indexRemovedNumericVars);
			defragmentMap(newState.mMapNumericVarToIndex); // already unref'd
		}
		return newState;
	}

	private static <T> void defragmentMap(Map<T, Integer> map) {
		TreeMap<Integer, T> reversedMapSortedAscending = new TreeMap<Integer, T>();
		for (Map.Entry<T, Integer> entry : map.entrySet()) {
			reversedMapSortedAscending.put(entry.getValue(), entry.getKey());
		}
		map.clear();
		int newIndex = 0;
		for (Map.Entry<Integer, T> entry : reversedMapSortedAscending.entrySet()) {
			map.put(entry.getValue(), newIndex);
			++newIndex;
		}
	}

	@Override
	public IBoogieVar getVariableDeclarationType(String name) {
		return mMapVarToBoogieVar.get(name);
	}

	@Override
	public boolean containsVariable(String name) {
		return mMapVarToBoogieVar.containsKey(name);
	}

	@Override
	public boolean isEmpty() {
		return mMapVarToBoogieVar.isEmpty();
	}

	@Override
	public boolean isBottom() {
		return isBooleanAbstractionBottom() || isNumericAbstractionBottom();
	}

	private boolean isNumericAbstractionBottom() {
		return cachedNormalizedNumericAbstraction().hasNegativeSelfLoop();
	}

	private boolean isBooleanAbstractionBottom() {
		return mBooleanAbstraction.containsValue(BoolValue.BOT);
	}

	// TODO document
	// - returns original cache. Do not modify in-place!
	private OctMatrix cachedNormalizedNumericAbstraction() {
		if (isNumericAbstractionIntegral()) {
			return mNumericAbstraction.cachedTightClosure();
		} else {
			return mNumericAbstraction.cachedStrongClosure();
		}
	}

	private boolean isNumericAbstractionIntegral() {
		return mNumericNonIntVars.isEmpty();
	}

	@Override
	public boolean isEqualTo(OctDomainState other) {
		if (other == null) {
			return false;
		} else if (other == this) {
			return true;
		} else {
			boolean isEqual = mMapVarToBoogieVar.equals(other.mMapVarToBoogieVar)
					&& mBooleanAbstraction.equals(other.mBooleanAbstraction)
					&& numericAbstractionIsEqualTo(other);
			return isEqual;
		}
	}

	@Override
	public int hashCode() {
		return mId;
	}

	// TODO document
	// - call only when same variables are stored
	private boolean numericAbstractionIsEqualTo(OctDomainState other) {
		assert mMapNumericVarToIndex.keySet().equals(other.mMapNumericVarToIndex.keySet());
		boolean permutated = false;
		int[] mapThisVarIndexToOtherVarIndex = new int[mNumericAbstraction.variables()];
		for (Map.Entry<String, Integer> entry : mMapNumericVarToIndex.entrySet()) {
			String var = entry.getKey();
			int thisVarIndex = entry.getValue();
			int otherVarIndex = other.mMapNumericVarToIndex.get(var);
			if (thisVarIndex != otherVarIndex) {
				permutated = true;
			}
			mapThisVarIndexToOtherVarIndex[thisVarIndex] = otherVarIndex;
		}
		OctMatrix thisClosure = cachedNormalizedNumericAbstraction();
		OctMatrix otherClosure = other.cachedNormalizedNumericAbstraction();
		boolean thisIsBottom = thisClosure.hasNegativeSelfLoop();
		boolean otherIsBottom = otherClosure.hasNegativeSelfLoop();
		if (thisIsBottom != otherIsBottom) {
			return false;
		} else if (thisIsBottom) { // && otherIsBottom
			return true;
		} else if (permutated) {
			return thisClosure.isEqualToPermutation(otherClosure, mapThisVarIndexToOtherVarIndex);
		} else {
			return thisClosure.isEqualTo(otherClosure);
		}
	}

	// TODO document: Returned state is a shallow copy. Do not modify!
	public OctDomainState meet(OctDomainState other) {
		return operation(other, BoolValue::intersect, OctMatrix::min, false);
	}

	// TODO document: Returned state is a shallow copy. Do not modify!
	public OctDomainState join(OctDomainState other) {
		return operation(other, BoolValue::union, OctMatrix::max, false);
	}

	// TODO document: Returned state is a shallow copy. Do not modify!
	public OctDomainState widen(OctDomainState other, BiFunction<OctMatrix, OctMatrix, OctMatrix> widenOp) {
		return operation(other, BoolValue::union, widenOp, true);
	}

	// TODO document: Returned state is a shallow copy. Do not modify!
	private OctDomainState operation(OctDomainState other,
			BiFunction<BoolValue, BoolValue, BoolValue> booleanOperation,
			BiFunction<OctMatrix, OctMatrix, OctMatrix> numericOperation,
			boolean closureOtherInAdvance) {
		OctDomainState result = shallowCopy();
		for (Map.Entry<String, BoolValue> entry : mBooleanAbstraction.entrySet()) {
			String name = entry.getKey();
			BoolValue oldValue = entry.getValue();
			BoolValue newValue = booleanOperation.apply(oldValue, other.mBooleanAbstraction.get(name));
			if (!oldValue.equals(newValue)) {
				unrefOtherBooleanAbstraction(result);
				result.mBooleanAbstraction.put(name, newValue);
			}
		}
		OctMatrix otherNumericAbstraction = closureOtherInAdvance ?
				other.cachedNormalizedNumericAbstraction() : other.mNumericAbstraction;
		result.mNumericAbstraction = numericOperation.apply(mNumericAbstraction, otherNumericAbstraction);
		return result;
	}

	@Override
	public Term getTerm(Script script, Boogie2SMT bpl2smt) {
		if (isBottom()) {
			return script.term("false");
		}
		Term n = getTermNumericAbstraction(script, bpl2smt);
		Term b = getTermBooleanAbstraction(script, bpl2smt);
		return Util.and(script, n, b);
	}

	private Term getTermNumericAbstraction(Script script, Boogie2SMT bpl2smt) {
		Term[] mapIndexToTerm = new Term[mMapNumericVarToIndex.size()];
		for (Map.Entry<String, Integer> entry : mMapNumericVarToIndex.entrySet()) {
			Term termVar = getTermVar(entry.getKey());
			mapIndexToTerm[entry.getValue()] = termVar;
		}
		return cachedNormalizedNumericAbstraction().getTerm(script, Arrays.asList(mapIndexToTerm));
	}

	private Term getTermBooleanAbstraction(Script script, Boogie2SMT bpl2smt) {
		Term acc = script.term("true");
		for (Entry<String, BoolValue> entry : mBooleanAbstraction.entrySet()) {
			Term termVar = getTermVar(entry.getKey());
			Sort sort = termVar.getSort().getRealSort();
			Term newTerm = entry.getValue().getTerm(script, sort, termVar);
			acc = Util.and(script, acc, newTerm);
		}
		return acc;
	}

	private Term getTermVar(String varName) {
		IBoogieVar var = mMapVarToBoogieVar.get(varName);
		if (var instanceof BoogieVar) {
			return ((BoogieVar) var).getTermVariable();
		} else if (var instanceof BoogieConst) {
			return ((BoogieConst) var).getDefaultConstant();
		}
		return null;
	}

	// TODO test
	// TODO document: Returned state is a shallow copy. Do not modify!
	@Override
	public OctDomainState patch(OctDomainState dominator) {
		
		assertNotBottomBeforeAssign();

		OctDomainState patchedState = shallowCopy();
		BidirectionalMap<Integer, Integer> mapTargetVarToSourceVar = new BidirectionalMap<>();
		SortedMap<Integer, String> mapDominatorIndicesOfNewNumericVars = new TreeMap<>();

		for (Map.Entry<String, IBoogieVar> entry : dominator.mMapVarToBoogieVar.entrySet()) {
			String var = entry.getKey();
			IBoogieVar newBoogieVar = entry.getValue();
			unrefOtherMapVarToBoogieVar(patchedState);
			IBoogieVar oldBoogieVar = patchedState.mMapVarToBoogieVar.put(var, newBoogieVar);
			assert oldBoogieVar == null || mMapVarToBoogieVar.get(var) == newBoogieVar
					: "Patch caused name-collision: " + var;
			IType type = newBoogieVar.getIType();
			if (TypeUtil.isNumeric(type)) {
				int sourceVar = dominator.mMapNumericVarToIndex.get(var);
				if (oldBoogieVar == null) {
					mapDominatorIndicesOfNewNumericVars.put(sourceVar, var);
					if (TypeUtil.isNumericNonInt(type)) {
						unrefOtherNumericNonIntVars(patchedState);
						patchedState.mNumericNonIntVars.add(var);
					}
				} else {
					int targetVar = patchedState.mMapNumericVarToIndex.get(var);
					mapTargetVarToSourceVar.put(targetVar, sourceVar);
				}
			} else if (TypeUtil.isBoolean(type)){
				unrefOtherBooleanAbstraction(patchedState);
				patchedState.mBooleanAbstraction.put(var, dominator.mBooleanAbstraction.get(var));
			}
			// else: variable has unsupported type and is assumed to be \top
		}
		for (String var : mapDominatorIndicesOfNewNumericVars.values()) {
			unrefOtherMapNumericVarToIndex(patchedState);
			patchedState.mMapNumericVarToIndex.put(var, patchedState.mMapNumericVarToIndex.size());
		}
		patchedState.mNumericAbstraction = cachedNormalizedNumericAbstraction()
				.appendSelection(dominator.mNumericAbstraction, mapDominatorIndicesOfNewNumericVars.keySet());
		patchedState.mNumericAbstraction.copySelection(dominator.mNumericAbstraction, mapTargetVarToSourceVar);
		return patchedState;
	}

	// TODO document: Returned state is a shallow copy. Modifications on return value may also modify this OctDomainState!
	public OctDomainState copyValuesOnScopeChange(OctDomainState source, List<Pair<String, String>> mapTargetToSource) {
		
		assert assertNotBottomBeforeAssign();
		
		// TODO closure in advance to reduce information loss
		
		BidirectionalMap<Integer, Integer> mapNumericTargetToSource = new BidirectionalMap<>();
		List<Pair<String, String>> mapBooleanTargetToSource = new ArrayList<>(mapTargetToSource.size());

		// shared (=global) numeric variables (copy to keep relations between globals and in/out-parameters)
		for (String var : sharedVars(source)) {
			Integer targetIndex = mMapNumericVarToIndex.get(var);
			if (targetIndex != null) {
				Integer sourceIndex = source.mMapNumericVarToIndex.get(var);
				assert sourceIndex != null : "shared variables are not really shared";
				mapNumericTargetToSource.put(targetIndex, sourceIndex);
			}
			// do not copy shared (=global) booleans (again). Already done by patch(...).
		}

		// in/out-parameters (from one scope) to locals (from another scope)
		for (Pair<String, String> assignmentPair : mapTargetToSource) {
			String targetVar = assignmentPair.getFirst();
			String sourceVar = assignmentPair.getSecond();
			Integer targetIndex = mMapNumericVarToIndex.get(targetVar);
			if (targetIndex != null) {
				Integer sourceIndex = source.mMapNumericVarToIndex.get(sourceVar);
				assert sourceIndex != null : "assigned non-numeric var to numeric var";
				mapNumericTargetToSource.put(targetIndex, sourceIndex);
			} else if (mBooleanAbstraction.containsKey(targetVar)) {
				assert source.mBooleanAbstraction.containsKey(sourceVar) : "assigned non-boolean var to boolean var";
				mapBooleanTargetToSource.add(new Pair<>(targetVar, sourceVar));
			}
		}

		// create new state
		OctDomainState newState = shallowCopy();
		if (!mapNumericTargetToSource.isEmpty()) {
			newState.mNumericAbstraction = cachedNormalizedNumericAbstraction().copy();
			newState.mNumericAbstraction.copySelection(source.mNumericAbstraction, mapNumericTargetToSource);
		}
		if (!mapBooleanTargetToSource.isEmpty()) {
			unrefOtherBooleanAbstraction(newState);
			for (Pair<String, String> entry : mapBooleanTargetToSource) {
				String targetVar = entry.getFirst();
				String sourceVar = entry.getSecond();
				BoolValue sourceValue = source.mBooleanAbstraction.get(sourceVar);
				newState.mBooleanAbstraction.put(targetVar, sourceValue);
			}
		}
		return newState;
	}

//	/**
//	 * Finds the shared global variables and constants between {@code this} and an {@code other} abstract domain state.
//	 * Found global variables and constants are added to existing collections.
//	 * <p>
//	 * Shared variables/constants are the exact same variables/constants.
//	 * Different variables/constants with equal names or even equal types are not shared.
//	 *
//	 * @param other abstract domain state
//	 * @param sharedGlobals collection where the shared global variables are added or {@code null}
//	 * @param sharedConstants collection where the shared constants are added or {@code null}
//	 */
//	public void sharedNumericGlobals(OctagonDomainState other,
//			Collection<String> sharedGlobals, Collection<String> sharedConstants) {
//		for (Map.Entry<String, IBoogieVar> var : sharedVars(other)) {
//			IBoogieVar ibv = var.getValue();
//			if (isNumeric(ibv.getIType())) {
//				if (sharedConstants != null && ibv instanceof BoogieConst) {
//					sharedConstants.add(var.getKey());
//				} else if (sharedGlobals != null && ibv instanceof BoogieVar && ((BoogieVar) ibv).isGlobal()) {
//					sharedGlobals.add(var.getKey());
//				}
//			}
//		}
//	}

	public Set<String> sharedVars(OctDomainState other) {
		Set<String> sharedVars = new HashSet<>();
		Set<Map.Entry<String, IBoogieVar>> otherEntrySet = other.mMapVarToBoogieVar.entrySet();
		for (Map.Entry<String, IBoogieVar> entry : mMapVarToBoogieVar.entrySet()) {
			if (otherEntrySet.contains(entry)) {
				sharedVars.add(entry.getKey());
			}
		}
		return sharedVars;
	}

	protected void havocVar(String var) {
		havocVars(Collections.singleton(var));
	}

	// TODO Only calculate closure if necessary. Some vars may have no constraints to other vars => no closure 
	protected void havocVars(Collection<String> vars) {
		assert assertNotLockedBeforeModification();
		assert assertNotBottomBeforeAssign();
		Set<Integer> numVarIndices = new HashSet<>();
		for (String var : vars) {
			Integer numVarIndex = mMapNumericVarToIndex.get(var);
			if (numVarIndex != null) {
				numVarIndices.add(numVarIndex);
			} else if (mBooleanAbstraction.containsKey(var)) {
				mBooleanAbstraction.put(var, BoolValue.TOP);
			}
			// else: variables of unsupported types are assumed to be \top all the time
			assert mMapVarToBoogieVar.containsKey(var) : "unknown variable in havoc: " + var;
		}
		if (!numVarIndices.isEmpty()) {
			mNumericAbstraction = cachedNormalizedNumericAbstraction().copy();
			numVarIndices.forEach(v -> mNumericAbstraction.havocVar(v));
		}
	}

	// v := v + c;
	protected void incrementNumericVar(String targetVar, OctValue addConstant) {
		assert assertNotLockedBeforeModification();
		assert assertNotBottomBeforeAssign();
		mNumericAbstraction.incrementVar(numVarIndex(targetVar), addConstant);
	}

	protected void negateNumericVar(String targetVar) {
		assert assertNotLockedBeforeModification();
		assert assertNotBottomBeforeAssign();
		mNumericAbstraction.negateVar(numVarIndex(targetVar));
	}

	protected void assignNumericVarConstant(String targetVar, OctValue constant) {
		assert assertNotLockedBeforeModification();
		assert assertNotBottomBeforeAssign();
		mNumericAbstraction = cachedNormalizedNumericAbstraction().copy();
		mNumericAbstraction.assignVarConstant(numVarIndex(targetVar), constant);
	}
	
	// min <= var <= max
	protected void assignNumericVarInterval(String targetVar, OctValue min, OctValue max) {
		assert assertNotLockedBeforeModification();
		assert assertNotBottomBeforeAssign();
		mNumericAbstraction = cachedNormalizedNumericAbstraction().copy();
		mNumericAbstraction.assignVarInterval(numVarIndex(targetVar), min, max);
	}

	
	protected void assumeNumericVarConstant(String targetVar, OctValue constant) {
		assert assertNotLockedBeforeModification();
		mNumericAbstraction.assumeVarConstant(numVarIndex(targetVar), constant);
	}
	
	// min <= var <= max
	protected void assumeNumericVarInterval(String targetVar, OctValue min, OctValue max) {
		mNumericAbstraction.assumeVarInterval(numVarIndex(targetVar), min, max);
	}
	
	// var1 + var2 <= zero
	protected void assumeNumericVarRelationLeConstant(
			String var1, boolean var1Negate, String var2, boolean var2Negate, OctValue constant) {
		assert assertNotLockedBeforeModification();
		mNumericAbstraction.assumeVarRelationLeConstant(
				numVarIndex(var1), var1Negate, numVarIndex(var2), var2Negate, constant);
	}

	public OctInterval projectToInterval(String numericVar) {
		return OctInterval.fromMatrix(cachedNormalizedNumericAbstraction(), numVarIndex(numericVar));
	}

	private int numVarIndex(String var) {
		Integer index = mMapNumericVarToIndex.get(var);
		assert index != null : "Not a numeric variable: " + var;
		return index;
	}
	
	// targetVar1, targetVar2, ... := sourceVar1, sourceVar2, ...
	protected void copyVars(List<Pair<String, String>> mapTargetVarToSourceVar) {

		assert assertNotLockedBeforeModification();
		assert assertNotBottomBeforeAssign();

		boolean usedClosure = false;

		for (Pair<String, String> entry : mapTargetVarToSourceVar) {
			String targetVar = entry.getFirst();
			String sourceVar = entry.getSecond();

			Integer targetIndex = mMapNumericVarToIndex.get(targetVar);
			if (targetIndex != null) {
				if (!usedClosure) {
					mNumericAbstraction = cachedNormalizedNumericAbstraction().copy();
					usedClosure = true;
				}
				Integer sourceIndex = mMapNumericVarToIndex.get(sourceVar);
				assert sourceIndex != null : "Incompatible types";
				mNumericAbstraction.assignVarCopy(targetIndex, sourceIndex);

			} else if (mBooleanAbstraction.containsKey(targetIndex)) {
				BoolValue value = mBooleanAbstraction.get(sourceVar);
				assert value != null : "Incompatible types";
				mBooleanAbstraction.put(targetVar, value);

			}
			// else: variables of unsupported types are assumed to be \top all the time
			assert mMapVarToBoogieVar.containsKey(targetVar) && mMapVarToBoogieVar.containsKey(sourceVar)
				: "unknown variable in assignment: " + targetVar + " := " + sourceVar;
		}
	}

	// targetVar := sourceVar
	// TODO rename to: assignVarCopy
	protected void copyVar(String targetVar, String sourceVar) {
		copyVars(Collections.singletonList(new Pair<>(targetVar, sourceVar)));
	}

	protected void assignBooleanVar(String var, BoolValue value) {
		assert mBooleanAbstraction.containsKey(var) : "unknown variable in boolean assignment: " + var;
		assert assertNotLockedBeforeModification();
		assert assertNotBottomBeforeAssign();
		mBooleanAbstraction.put(var, value);
	}
	
	protected void assumeBooleanVar(String var, BoolValue value) {
		assert assertNotLockedBeforeModification();
		mBooleanAbstraction.put(var, mBooleanAbstraction.get(var).intersect(value));
	}

	@Override
	public String toString() {
		return toLogString();
	}

	@Override
	public String toLogString() {
		return mLogStringFunction.apply(this);
	}

	public String logStringFullMatrix() {
		return logStringMatrix(mNumericAbstraction.toStringFull());
	}

	public String logStringHalfMatrix() {
		return logStringMatrix(mNumericAbstraction.toStringHalf());
	}

	private String logStringMatrix(String logStringNumericAbstration) {
		StringBuilder log = new StringBuilder();
		log.append("\n");

		// TODO remove (only for debugging)
		log.append("#").append(mId);
		log.append(mIsLocked ? " locked\n" : " open\n");

		log.append(logStringNumericAbstration);

		log.append("numeric vars: ").append(mMapNumericVarToIndex);
		log.append("\nnumeric non-int vars: ").append(mNumericNonIntVars);
		log.append("\nboolean abstraction: ").append(mBooleanAbstraction);

//		for (Map.Entry<String, BoolValue> entry : mBooleanAbstraction.entrySet()) {
//			log.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
//		}

		// TODO remove
		log.append("\n#END\n");

		// TODO remove
		log.append(logStringTerm()).append("\n");
		
		return log.toString();
	}

	public String logStringTerm() {
		final String in = " = ";
		final String minus = " - ";
		final String plus = " + ";
		final String delimiter = "; ";
		
		// Interval bounds --------------------------------------------------------------
		StringBuilder intsLog = new StringBuilder("ints: {");
		StringBuilder realsLog = new StringBuilder("reals: {");

		String curDelimiter = "";
		for (Map.Entry<String, Integer> entry : mMapNumericVarToIndex.entrySet()) {
			String varName = entry.getKey();
			OctInterval interval = OctInterval.fromMatrix(mNumericAbstraction, entry.getValue());

			StringBuilder curLog = mNumericNonIntVars.contains(varName) ? realsLog : intsLog;
			curLog.append(curDelimiter);
			curDelimiter = delimiter;
			curLog.append(varName).append(in).append(interval);
		}

		// Constraints between two different variables ----------------------------------
		StringBuilder relLog = new StringBuilder("relations: {");
		
		curDelimiter = "";
		if (mNumericAbstraction.hasNegativeSelfLoop()) {
			relLog.append("\\bot");
			curDelimiter = delimiter;
		}
		for (Map.Entry<String, Integer> rowEntry : mMapNumericVarToIndex.entrySet()) {
			for (Map.Entry<String, Integer> colEntry : mMapNumericVarToIndex.entrySet()) {
				String rowName = rowEntry.getKey();
				String colName = colEntry.getKey();
				int row2 = rowEntry.getValue() * 2;
				int col2 = colEntry.getValue() * 2;

				if (row2 <= col2) {
					// skip block upper triangular part (is coherent/redundant)
					// skip diagonal blocks (already logged, see above)
					continue;
				}

				OctInterval sumInterval = new OctInterval(
						mNumericAbstraction.get(row2, col2 + 1).negateIfNotInfinity(),
						mNumericAbstraction.get(row2 + 1, col2));
				OctInterval colMinusRowInterval = new OctInterval(
						mNumericAbstraction.get(row2 + 1, col2 + 1).negateIfNotInfinity(),
						mNumericAbstraction.get(row2, col2));

				if (!sumInterval.isTop()) {
					relLog.append(curDelimiter);
					curDelimiter = delimiter;
					relLog.append(colName).append(plus).append(rowName).append(in).append(sumInterval);
				}

				if (!colMinusRowInterval.isTop()) {
					relLog.append(curDelimiter);
					curDelimiter = delimiter;
					relLog.append(colName).append(minus).append(rowName).append(in).append(colMinusRowInterval);
				}
			}
		}

		return new StringBuilder("{")
				.append(intsLog).append("}, ")
				.append(realsLog).append("}, ")
				.append(relLog).append("}, ")
				.append("bools: ").append(mBooleanAbstraction)
				.append("}").toString();
	}
	
}