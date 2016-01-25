package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.relational.octagon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.Util;
import de.uni_freiburg.informatik.ultimate.util.BidirectionalMap;

/**
 * Matrix for octagons (as presented by Antoine Mine).
 * <p>
 * 
 * TODO More documentation
 * TODO Stores entries for variables +v1, -v1, ..., +vn, -vn
 * TODO Variables (names, type, ...) aren't stored
 * TODO Matrix is divided into 2x2 blocks
 * TODO Different matrices, same octagon -> closures
 * 
 * This class ensures coherence (as seen in the following ASCII art).
 * <pre>
 *     \ .   D B   L J
 *     . \   C A   K I
 *                 
 *     A B   \ .   W Y
 *     C D   . \   Z X
 *                 
 *     I J   X Y   \ .
 *     K L   Z W   . \
 * </pre>
 * 
 * @author schaetzc@informatik.uni-freiburg.de
 */
public class OctMatrix {

	public final static OctMatrix NEW = new OctMatrix(0);

	private final static Consumer<OctMatrix> sDefaultShortestPathClosure = OctMatrix::shortestPathClosureInPlacePrimitiveSparse;

	/**
	 * Size of this matrix (size = #rows = #columns).
	 * Size is always an even number.
	 */
	private final int mSize;
	
	/**
	 * Stores the elements of this matrix.
	 * <p> 
	 * Some elements are neglected because they are coherent to other elements
	 * (see documentation of this class). The stored elements and their
	 * indices are shown in the following ASCII art.
	 * <pre>
	 *     0 1 . . . .    legend
	 *     2 3 . . . .    -----------------
	 *     4 5 6 7 . .    .      not stored
	 *     8 9 # # . .    0-9 #  stored    
	 *     # # # # # #
	 *     # # # # # #    m_0,0 is top left
	 * </pre>
	 * The matrix is divided into 2x2 blocks. Every block that contains
	 * at least one element from the block lower triangular matrix is completely
	 * stored. Elements are stored row-wise from top to bottom, left to right.
	 * 
	 * @see #indexOf(int, int)
	 */
	private final OctValue[] mElements;

	private OctMatrix mStrongClosure;
	private OctMatrix mTightClosure;
	
	public OctMatrix copy() {
		OctMatrix copy = new OctMatrix(mSize);
		System.arraycopy(this.mElements, 0, copy.mElements, 0, mElements.length);
		copy.mStrongClosure = mStrongClosure;
		copy.mTightClosure = mTightClosure;
		return copy;
	}

	public static OctMatrix parseBlockLowerTriangular(String m) {
		m = m.trim();
		String[] elements = "".equals(m) ? new String[0] : m.split("\\s+");
		int size = (int) (Math.sqrt(2 * elements.length + 1) - 1);
		if (size % 2 != 0) {
			throw new IllegalArgumentException("Number of elements does not match any 2x2 block lower triangular matrix.");
		}
		OctMatrix oct = new OctMatrix(size);
		for (int i = 0; i < elements.length; ++i) {
			oct.mElements[i] = OctValue.parse(elements[i]);
		}
		return oct;
	}
	
	public static OctMatrix random(int variables) {
		return random(variables, Math.random());
	}

	/**
	 * @param infProbability probability that an element will be infinity (0 = never, 1 = always)
	 */
	public static OctMatrix random(int variables, double infProbability) {
		OctMatrix m = new OctMatrix(variables * 2);
		for (int i = 0; i < m.mSize; ++i) {
			int maxCol = i | 1;
			for (int j = 0; j <= maxCol; ++j) {
				OctValue v;
				if (i == j) {
					v = OctValue.ZERO;
				} else if (Math.random() < infProbability) {
					v = OctValue.INFINITY;
				} else {
					v = new OctValue((int) (Math.random() * 20 - 10));
				}
				m.set(i, j , v);
			}
		}
		return m;
	}
	
	protected OctMatrix(int size) {
		mSize = size;
		mElements = new OctValue[size * size / 2 + size];
	}
	
	protected void fillInPlace(OctValue fill) {
		for (int i = 0; i < mElements.length; ++i) {
			mElements[i] = fill;
		}
		mStrongClosure = mTightClosure = null;
	}
	
	public int getSize() {
		return mSize;
	}
	
	public int variables() {
		return mSize / 2;
	}
	
	public OctValue get(int row, int col) {
		return mElements[indexOf(row, col)];
	}
	
	protected void set(int row, int col, OctValue value) {
		if(value == null) {
			throw new IllegalArgumentException("null is not a valid matrix element.");
		}
		mStrongClosure = mTightClosure = null;
		mElements[indexOf(row, col)] = value;
	}
	
	private void minimizeMainDiagonal() {
		boolean changed = false;
		for (int i = 0; i < mSize; ++i) {
			int ii = indexOfLower(i, i);
			if (OctValue.ZERO.compareTo(mElements[ii]) < 0) {
				mElements[ii] = OctValue.ZERO;
				changed = true;
			}
		}
		if (changed) {
			mStrongClosure = mTightClosure = null;
		}
	}

	private int indexOf(int row, int col) {
		assert row < mSize && col < mSize : row + "," + col + " is not an index for matrix of size " + mSize + ".";
		if (row < col) {
			return indexOfLower(col ^ 1, row ^ 1);
		}
		return indexOfLower(row, col);
	}

	private int indexOfLower(int row, int col) {
		return col + (row + 1) * (row + 1) / 2;
	}

	public OctMatrix elementwiseOperation(OctMatrix other, BiFunction<OctValue, OctValue, OctValue> operator) {
		checkCompatibility(other);
		OctMatrix result = new OctMatrix(mSize);
		for (int i = 0; i < mElements.length; ++i) {
			result.mElements[i] = operator.apply(mElements[i], other.mElements[i]);
		}
		return result;
	}
	
	public boolean elementwiseRelation(OctMatrix other, BiFunction<OctValue, OctValue, Boolean> relation) {
		checkCompatibility(other);
		for (int i = 0; i < mElements.length; ++i) {
			if (!relation.apply(mElements[i], other.mElements[i])) {
				return false;
			}
		}
		return true;
	}
	
	private void checkCompatibility(OctMatrix other) {
		if (other.mSize != mSize) {			
			throw new IllegalArgumentException("Incompatible matrices");
		}
	}

	public OctMatrix add(OctMatrix other) {
		return elementwiseOperation(other, OctValue::add);
	}
	
	public static OctMatrix min(OctMatrix a, OctMatrix b) {
		return a.elementwiseOperation(b, OctValue::min);
	}
	
	public static OctMatrix max(OctMatrix a, OctMatrix b) {
		return a.elementwiseOperation(b, OctValue::max);
	}

	// TODO document: Different matrices may represent the same octagon.
	public boolean isEqualTo(OctMatrix other) {
		if (this == other) {
			return true;
		}
		return elementwiseRelation(other, (x, y) -> x.compareTo(y) == 0);
	}

	public boolean isEqualToPermutation(OctMatrix permutation, int[] mapThisVarIndexToPermVarIndex) {
		for (int bRow = 0; bRow < variables(); ++bRow) {
			// compare only block lower triangular part (upper part is coherent)
			for (int bCol = 0; bCol <= bRow; ++bCol) {
				int permBRow = mapThisVarIndexToPermVarIndex[bRow];
				int permBCol = mapThisVarIndexToPermVarIndex[bCol];
				if (!isBlockEqualTo(bRow, bCol, permutation, permBRow, permBCol)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isBlockEqualTo(int bRow, int bCol, OctMatrix other, int otherBRow, int otherBCol) {
		bRow *= 2;
		bCol *= 2;
		otherBRow *= 2;
		otherBCol *= 2;
		for (int j = 0; j < 2; ++j) {
			for (int i = 0; i < 2; ++i) {
				if (get(bRow + i, bCol + j).compareTo((other.get(otherBRow + i, otherBCol + j))) != 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	// note: "not less-equal than" does not necessarily mean "greater than"
	public boolean isLessEqualThan(OctMatrix other) {
		return elementwiseRelation(other, (x, y) -> x.compareTo(y) <= 0);		
	}
	
	public OctMatrix strongClosure() {
		return strongClosure(sDefaultShortestPathClosure);
	}

	public OctMatrix strongClosureNaiv() {
		return strongClosure(OctMatrix::shortestPathClosureInPlaceNaiv);
	}
	
	public OctMatrix strongClosureApron() {
		return strongClosure(OctMatrix::shortestPathClosureInPlaceApron);
	}
	
	public OctMatrix strongClosureFullSparse() {
		return strongClosure(OctMatrix::shortestPathClosureInPlaceFullSparse);
	}
	
	public OctMatrix strongClosureSparse() {
		return strongClosure(OctMatrix::shortestPathClosureInPlaceSparse);
	}
	
	public OctMatrix strongClosurePrimitiveSparse() {
		return strongClosure(OctMatrix::shortestPathClosureInPlacePrimitiveSparse);
	}
	
	public OctMatrix strongClosure(Consumer<OctMatrix> shortestPathClosureAlgorithm) {
		if (mStrongClosure != null) {
			return mStrongClosure;
		}
		OctMatrix sc = copy();
		shortestPathClosureAlgorithm.accept(sc);
		sc.strengtheningInPlace();
		mStrongClosure = sc.mStrongClosure = sc;
		sc.mTightClosure = mTightClosure;
		return sc;
	}
	
	public OctMatrix tightClosure() {
		return tightClosure(sDefaultShortestPathClosure);
	}
	
	public OctMatrix tightClosure(Consumer<OctMatrix> shortestPathClosureAlgorithm) {
		if (mTightClosure != null) {
			return mTightClosure;
		}
		OctMatrix tc;
		if (mStrongClosure != null) {
			tc = mStrongClosure.copy();
		} else {
			tc = copy();
			shortestPathClosureAlgorithm.accept(tc);			
		}
		tc.tighteningInPlace();
		mTightClosure = tc.mTightClosure = tc;
		tc.mStrongClosure = mStrongClosure;
		return tc;
	}

	private void shortestPathClosureInPlaceNaiv() {
		minimizeMainDiagonal();
		for (int k = 0; k < mSize; ++k) {
			for (int i = 0; i < mSize; ++i) {
				OctValue ik = get(i, k);
				for (int j = 0; j < mSize; ++j) {
					OctValue indirectRoute = ik.add(get(k, j));
					if (get(i, j).compareTo(indirectRoute) > 0) {
						set(i, j, indirectRoute);
					}
				}
			}
		}
	}
	
	private void shortestPathClosureInPlaceFullSparse() {
		minimizeMainDiagonal();
		List<Integer> ck = null; // indices of finite elements in columns k and k^1
		List<Integer> rk = null; // indices of finite elements in rows k and k^1
		for (int k = 0; k < mSize; ++k) {
			ck = indexFiniteElementsInColumn(k);
			rk = indexFiniteElementsInRow(k);
			for (int i : ck) {
				OctValue ik = get(i, k);
				for (int j : rk) {
					OctValue kj = get(k, j);
					OctValue indirectRoute = ik.add(kj);
					if (get(i, j).compareTo(indirectRoute) > 0) {
						set(i, j, indirectRoute);
					}
				}
			}
		}
	}

	// incoming edges of k
	private List<Integer> indexFiniteElementsInColumn(int k) {
		List<Integer> index = new ArrayList<Integer>(mSize);
		for (int i = 0; i < mSize; ++i) {
			if (!get(i, k).isInfinity()) {
				index.add(i);
			}
		}
		return index;
	}
	
	// outgoing edges of k
	private List<Integer> indexFiniteElementsInRow(int k) {
		List<Integer> index = new ArrayList<Integer>(mSize);
		for (int j = 0; j < mSize; ++j) {
			if (!get(k, j).isInfinity()) {
				index.add(j);
			}
		}
		return index;
	}
	
	private void shortestPathClosureInPlaceSparse() {
		minimizeMainDiagonal();
		List<Integer> ck = null; // indices of finite elements in columns k and k^1
		List<Integer> rk = null; // indices of finite elements in rows k and k^1
		for (int k = 0; k < mSize; ++k) {
			int kk = k ^ 1;
			if (k < kk) { // k is even => entered new 2x2 block
				ck = indexFiniteElementsInBlockColumn(k);
				rk = indexFiniteElementsInBlockRow(k);
			}
			for (int i : ck) {
				OctValue ik = get(i, k);
				OctValue ikk = get(i, kk);
				int maxCol = i | 1;
				for (int j : rk) {
					if (j > maxCol) {
						break;
					}
					OctValue kj = get(k, j);
					OctValue kkj = get(kk, j);
					OctValue indirectRoute = OctValue.min(ik.add(kj), ikk.add(kkj));
					if (get(i, j).compareTo(indirectRoute) > 0) {
						set(i, j, indirectRoute);
					}
				}
			}
		}
	}
	
	private List<Integer> indexFiniteElementsInBlockColumn(int k) {
		List<Integer> index = new ArrayList<Integer>(mSize);
		int kk = k ^ 1;
		for (int i = 0; i < mSize; ++i) {
			if (!get(i, k).isInfinity() || !get(i, kk).isInfinity()) {
				index.add(i);
			}
		}
		return index;
	}
	
	private List<Integer> indexFiniteElementsInBlockRow(int k) {
		List<Integer> index = new ArrayList<Integer>(mSize);
		int kk = k ^ 1;
		for (int j = 0; j < mSize; ++j) {
			if (!get(k, j).isInfinity() || !get(kk, j).isInfinity()) {
				index.add(j);
			}
		}
		return index;
	}
	
	private void shortestPathClosureInPlacePrimitiveSparse() {
		minimizeMainDiagonal();
		int[] ck = null; // indices of finite elements in columns k and k^1
		int[] rk = null; // indices of finite elements in rows k and k^1
		for (int k = 0; k < mSize; ++k) {
			int kk = k ^ 1;
			if (k < kk) { // k is even => entered new 2x2 block
				ck = primitiveIndexFiniteElementsInBlockColumn(k);
				rk = primitiveIndexFiniteElementsInBlockRow(k);
			}
			for (int _i = 1; _i <= ck[0]; ++_i) {
				int i = ck[_i];
				OctValue ik = get(i, k);
				OctValue ikk = get(i, kk);
				int maxCol = i | 1;
				for (int _j = 1; _j <= rk[0]; ++_j) {
					int j = rk[_j];
					if (j > maxCol) {
						break;
					}
					OctValue kj = get(k, j);
					OctValue kkj = get(kk, j);
					OctValue indirectRoute = OctValue.min(ik.add(kj), ikk.add(kkj));
					if (get(i, j).compareTo(indirectRoute) > 0) {
						set(i, j, indirectRoute);
					}
				}
			}
		}
	}
	
	private int[] primitiveIndexFiniteElementsInBlockColumn(int k) {
		int[] index = new int[mSize + 1];
		int c = 0;
		int kk = k ^ 1;
		for (int i = 0; i < mSize; ++i) {
			if (!get(i, k).isInfinity() || !get(i, kk).isInfinity()) {
				index[++c] = i;
			}
		}
		index[0] = c;
		return index;
	}
	
	private int[] primitiveIndexFiniteElementsInBlockRow(int k) {
		int[] index = new int[mSize + 1];
		int c = 0;
		int kk = k ^ 1;
		for (int j = 0; j < mSize; ++j) {
			if (!get(k, j).isInfinity() || !get(kk, j).isInfinity()) {
				index[++c] = j;
			}
		}
		index[0] = c;
		return index;
	}
	

	private void shortestPathClosureInPlaceApron() {
		minimizeMainDiagonal();
		for (int k = 0; k < mSize; ++k) {
			for (int i = 0; i < mSize; ++i) {
				OctValue ik = get(i, k);
				OctValue ikk = get(i, k^1);
				int maxCol = i | 1;
				for (int j = 0; j <= maxCol; ++j) {
					OctValue kj = get(k, j);
					OctValue kkj = get(k^1, j);
					OctValue indirectRoute = OctValue.min(ik.add(kj), ikk.add(kkj));
					if (get(i, j).compareTo(indirectRoute) > 0) {
						set(i, j, indirectRoute);
					}
				}
			}
		}
	}

	private void strengtheningInPlace() {
		// blocks on the diagonal (upper, lower bounds) won't change by strengthening
		// => iterate only 2x2 blocks that are _strictly_ below the main diagonal
		for (int i = 2; i < mSize; ++i) {
			OctValue ib = get(i, i^1).half();
			int maxCol = (i - 2) | 1;
			for (int j = 0; j <= maxCol; ++j) {
				OctValue jb = get(j^1, j).half();
				OctValue b = ib.add(jb);
				if (get(i, j).compareTo(b) > 0) {
					set(i, j, b);
				}
			}
		}		
	}

	private void tighteningInPlace() {
		for (int i = 0; i < mSize; ++i) {
			OctValue ib = get(i, i^1).half().floor();
			int maxCol = i | 1;
			for (int j = 0; j <= maxCol; ++j) {
				OctValue jb = get(j^1, j).half().floor();
				OctValue b = ib.add(jb);
				if (get(i, j).compareTo(b) > 0) {
					set(i, j, b);
				}
			}
		}		
	}

	/**
	 * Checks for negative self loops in the graph represented by this adjacency matrix.
	 * Self loops are represented by this matrix' diagonal elements.
	 * <p>
	 * <b>m</b> is strongly closed and has a negative self loop <=> <b>m</b> is empty in <b>R</b><br>
	 * <b>m</b> is tightly closed and has a negative self loop <=> <b>m</b> is empty in <b>Z</b>
	 * 
	 * @return a negative self loop exists
	 */
	public boolean hasNegativeSelfLoop() {
		for (int i = 0; i < mSize; ++i) {
			if (get(i, i).compareTo(OctValue.ZERO) < 0) {
				return true;
			}
		}
		return false;
	}

	public OctMatrix widenSimple(OctMatrix n) {
		return elementwiseOperation(n, (mij, nij) -> mij.compareTo(nij) >= 0 ? mij : OctValue.INFINITY);
	}

	// TODO document: stabilization depends on user-given threshold (stabilizes for threshold < inf)
	public OctMatrix widenExponential(OctMatrix n, OctValue threshold) {
		final OctValue epsilon = new OctValue(1);
		final OctValue minusTwoEpsilon = epsilon.add(epsilon).negate();
		final OctValue oneHalfEpsilon = epsilon.half();
		return elementwiseOperation(n, (mij, nij) -> {
			if (mij.compareTo(nij) >= 0) {
				return mij;
			}
			if (nij.compareTo(threshold) > 0) {
				return OctValue.INFINITY;
			}
			OctValue result;
			if (nij.compareTo(minusTwoEpsilon) <= 0) {
				result = nij.half(); // there is no -inf => will always converge towards 0
			} else if (nij.compareTo(OctValue.ZERO) <= 0) {
				result = OctValue.ZERO;
			} else if (nij.compareTo(oneHalfEpsilon) <= 0) {
				result = epsilon;
			} else {
				result = nij.add(nij); // 2 * nij
			}
			return OctValue.min(result, threshold);
		});
//		OctValue result = nij;
//		if (result.compareTo(threshold) > 0) {
//			return OctValue.INFINITY;
//		}
//		int resultComp = result.compareTo(OctValue.ZERO);
//		if (resultComp > 0) {
//			result = OctValue.max(nij.add(nij), OctValue.ONE);
//		} else if (resultComp < 0) {
//			result = nij.half(); // there is no -inf => will always converge towards 0
//			result = result.compareTo(OctValue.MINUS_ONE) > 0 ? OctValue.ZERO : result;
//		} else {
//			result = nij; // == 0
//		}
//		return result;
	}

	// TODO document: stabilization depends on user-given limit (limit has to be inf after finite number of widenings)
	// TODO: use list of literals instead of one fixed limit
	public OctMatrix widenLimit(OctMatrix n, OctValue limit) {
		return elementwiseOperation(n, (mij, nij) -> mij.compareTo(nij) >= 0 ? mij : OctValue.max(nij, limit));
	}

	public OctMatrix addVariables(int count) {
		if (count < 0 ) {
			throw new IllegalArgumentException("Cannot add " + count + " variables.");
		} else if (count == 0) {
			return this;
		}
		OctMatrix n = new OctMatrix(mSize + (2 * count));
		System.arraycopy(mElements, 0, n.mElements, 0, mElements.length);
		Arrays.fill(n.mElements, this.mElements.length, n.mElements.length, OctValue.INFINITY);
		// cached closures are of different size and cannot be (directly) reused
		return n;
	}
	
	public OctMatrix removeVariable(int v) {
		return removeVariables(Collections.singleton(v));
	}
	
	public OctMatrix removeVariables(Set<Integer> vars) {
		if (areVariablesLast(vars)) {
			return removeLastVariables(vars.size()); // note: sets cannot contain duplicates
		} else {
			return removeArbitraryVariables(vars);
		}
	}

	private boolean areVariablesLast(Set<Integer> vars) {
		List<Integer> varsDescending = new ArrayList<>(vars);
		Collections.sort(varsDescending);
		int vPrev = variables();
		for (int v : varsDescending) {
			if (v < 0 || v >= variables()) {
				throw new IllegalArgumentException("Variable " + v + " does not exist.");
			} else if (v + 1 == vPrev) {				
				vPrev = v;
			} else {
				return false;
			}
		}
		return true;
	}

	public OctMatrix removeLastVariables(int count) {
		if (count > variables()) {
			throw new IllegalArgumentException("Cannot remove more variables than exist.");
		}
		OctMatrix n = new OctMatrix(mSize - (2 * count));
		System.arraycopy(mElements, 0, n.mElements, 0, n.mElements.length);
		// cached closures are of different size and cannot be (directly) reused
		return n;
	}
	
	public OctMatrix removeArbitraryVariables(Set<Integer> vars) {
		OctMatrix n = new OctMatrix(mSize - (2 * vars.size())); // note: sets cannot contain duplicates
		int in = 0;
		for (int i = 0; i < mSize; ++i) {
			if (vars.contains(i / 2)) {
				// 2x2 block row shall be removed => continue in next 2x2 block row
				++i;
				continue;
			}
			int maxCol = i | 1;
			for (int j = 0; j <= maxCol; ++j) {
				if (vars.contains(j / 2)) {
					// 2x2 block column shall be removed => continue in next 2x2 block
					++j;
					continue;
				}
				n.mElements[in++] = get(i, j);
			}
		}
		// cached closures are of different size and cannot be (directly) reused
		return n;
	}

	// TODO note that information is lost. Strong Closure on this and source in advance can reduce loss.
	// TODO source must be different from target (= this)
	protected void copySelection(OctMatrix source, BidirectionalMap<Integer, Integer> mapSourceVarToTargetVar) {
		if (source.mElements == mElements) {
			for (Map.Entry<Integer, Integer> entry : mapSourceVarToTargetVar.entrySet()) {
				if (!entry.getKey().equals(entry.getValue())) {
					throw new UnsupportedOperationException("Cannot overwrite in place");
				}
			}
			return;
		}
		BidirectionalMap<Integer, Integer> mapTargetVarToSourceVar = mapSourceVarToTargetVar.inverse();
		for (Map.Entry<Integer, Integer> entry : mapSourceVarToTargetVar.entrySet()) {
			int sourceVar = entry.getKey();
			int targetVar = entry.getValue();
			for (int targetOther = 0; targetOther < variables(); ++targetOther) {
				// copy only columns. Rows are copied by coherence
				Integer sourceOther = mapTargetVarToSourceVar.get(targetOther);
				if (sourceOther == null) {
					setBlock(targetOther, targetVar, OctValue.INFINITY);
//					setBlock(targetVar, targetOther, OctValue.INFINITY);
				} else {
					copyBlock(targetOther, targetVar, /* := */ source, sourceOther, sourceVar); // column
//					copyBlock(targetVar, targetOther, /* := */ source, sourceVar, sourceOther); // row
				}
			}
		}
	}

	// TODO document: selectedSourceVars should not contain duplicates
	// TODO document: iteration order matters
	public OctMatrix appendSelection(OctMatrix source, Collection<Integer> selectedSourceVars) {
		OctMatrix m = this.addVariables(selectedSourceVars.size());
		BidirectionalMap<Integer, Integer> mapSourceVarToTargetVar = new BidirectionalMap<>();
		for (Integer s : selectedSourceVars) {
			Integer prevValue = mapSourceVarToTargetVar.put(s, mapSourceVarToTargetVar.size() + variables());
			assert prevValue == null : "selection contained duplicate " + s;
		}
		m.copySelection(source, mapSourceVarToTargetVar);
		return m;
	}
	
	protected void copyBlock(int targetBRow, int targetBCol, OctMatrix source, int sourceBRow, int sourceBCol) {
		targetBRow *= 2;
		targetBCol *= 2;
		sourceBRow *= 2;
		sourceBCol *= 2;
		for (int j = 0; j < 2; ++j) {
			for (int i = 0; i < 2; ++i) {
				set(targetBRow + i, targetBCol + j, source.get(sourceBRow + i, sourceBCol + j));
			}
		}
	}

	protected void setBlock(int bRow, int bCol, OctValue v) {
		bRow *= 2;
		bCol *= 2;
		for (int j = 0; j < 2; ++j) {
			for (int i = 0; i < 2; ++i) {
				set(bRow + i, bCol + j, v);
			}
		}
	}
	
	public Term getTerm(Script script, List<Term> vars) {
		Term acc = script.term("true");
		for (int rowBlock = 0; rowBlock < variables(); ++rowBlock) {
			Term rowVar = vars.get(rowBlock);
			for (int colBlock = 0; colBlock <= rowBlock; ++colBlock) {
				Term colVar = vars.get(colBlock);
				Term blockTerm = getBlockTerm(script, rowBlock, colBlock, rowVar, colVar);
				acc = Util.and(script, acc, blockTerm);
			}
		}
		return acc;
	}

	private Term getBlockTerm(Script script, int rowBlock, int colBlock, Term rowVar, Term colVar) {
		Term acc = script.term("true");
		// 0 = plus, 1 = minus
		for (int i = 0; i < 2; ++i) { // row offset
			for (int j = 0; j < 2; ++j) { // col offset
				OctValue value = get(rowBlock + i, colBlock + j);
				if (!value.isInfinity()) {
					Term vj = j == 0 ? colVar : script.term("-", colVar);
					Term vi = i == 0 ? rowVar : script.term("-", rowVar);
					Term constraint = script.term("<=", script.term("-", vj, vi), script.decimal(value.getValue()));
					acc = Util.and(script, acc, constraint);
				}
			}
		}
		return acc;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < mSize; ++row) {
			String delimiter = "";
			for (int col = 0; col < mSize; ++col) {
				sb.append(delimiter);
				sb.append(get(row, col));
				delimiter = "\t";
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
}