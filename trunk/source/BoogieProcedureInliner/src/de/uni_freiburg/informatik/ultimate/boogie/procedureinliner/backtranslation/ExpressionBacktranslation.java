package de.uni_freiburg.informatik.ultimate.boogie.procedureinliner.backtranslation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.boogie.procedureinliner.VarMapKey;
import de.uni_freiburg.informatik.ultimate.boogie.procedureinliner.VarMapValue;
import de.uni_freiburg.informatik.ultimate.model.IType;
import de.uni_freiburg.informatik.ultimate.model.ModelUtils;
import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieTransformer;
import de.uni_freiburg.informatik.ultimate.model.boogie.DeclarationInformation;
import de.uni_freiburg.informatik.ultimate.model.boogie.DeclarationInformation.StorageClass;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.UnaryExpression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.UnaryExpression.Operator;
import de.uni_freiburg.informatik.ultimate.model.location.ILocation;

import static de.uni_freiburg.informatik.ultimate.model.boogie.DeclarationInformation.StorageClass.*;

/**
 * Part of @link {@link InlinerBacktranslator}.
 * 
 * Still a work in progress, therefore no final comments.
 * 
 * @author schaetzc@informatik.uni-freiburg.de
 */
public class ExpressionBacktranslation extends BoogieTransformer {

	private Map<VarMapValue, VarMapKey> mReverseVarMap = new HashMap<>();

	private Set<String> mActiveProcedures = Collections.emptySet();
	
	private boolean mProcessedExprWasActive = false;
	
	public void reverseAndAddMapping(Map<VarMapKey, VarMapValue> map) {
		for (Map.Entry<VarMapKey, VarMapValue> entry : map.entrySet()) {
			VarMapValue key = entry.getValue();
			VarMapKey newValue = entry.getKey();
			VarMapKey oldValue = mReverseVarMap.put(key, newValue);
			if (oldValue != null && !oldValue.equals(newValue)) {
				if (oldValue.getVarId().equals(oldValue.getVarId())) {
					DeclarationInformation combinedDeclInfo = combineDeclInfo(oldValue.getDeclInfo(), newValue.getDeclInfo());
					VarMapKey combinedValue = new VarMapKey(oldValue.getVarId(), combinedDeclInfo);
					mReverseVarMap.put(key, combinedValue);
				} else {
					throw new AssertionError("Ambiguous backtranslation mapping. Different variable names.");
				}
			}
		}
	}
	
	private DeclarationInformation combineDeclInfo(DeclarationInformation oldDI, DeclarationInformation newDI) {
		String oldProc = oldDI.getProcedure();
		String newProc = newDI.getProcedure();
		if (oldProc != null && oldProc.equals(newProc) || oldProc == null && newProc == null) {
			StorageClass oldSC = oldDI.getStorageClass();
			StorageClass newSC = newDI.getStorageClass();
			if (oldSC == IMPLEMENTATION_INPARAM && newSC == PROC_FUNC_INPARAM
					|| newSC == IMPLEMENTATION_INPARAM && oldSC == PROC_FUNC_INPARAM) {
				return new DeclarationInformation(IMPLEMENTATION_INPARAM, oldProc);
			} else if (oldSC == IMPLEMENTATION_OUTPARAM && newSC == PROC_FUNC_OUTPARAM
					|| newSC == IMPLEMENTATION_OUTPARAM && oldSC == PROC_FUNC_OUTPARAM) {
				return new DeclarationInformation(IMPLEMENTATION_OUTPARAM, oldProc);
			} else {
				throw new AssertionError("Ambiguous translation mapping. DeclarationInformations cannot be merged: "
						+ oldDI + ", " + newDI);
			}
		} else {
			throw new AssertionError("Ambiguous translation mapping. Different procedure in DeclarationInformation: "
					+ oldDI + ", " + newDI);
		}
	}

	/**
	 * Sets the set of procedures, whose variables are considered to be active.
	 * <p>
	 * A variable is active, if it isn't out of scope (like a local variable of procedure F,
	 * when the callstack doesn't contain F).
	 * Global variables and unknown variables (such without a mapping) are always considered to be active.
	 * <p>
	 * Calling this method will reset the intern <i>active</i> flag, used by {@link #processedExprWasActive()}.
	 * 
	 * @param activeProcedures {@link CallReinserter#unreturnedInlinedProcedures()}
	 */
	public void setInlinedActiveProcedures(Set<String> activeProcedures) {
		mActiveProcedures = activeProcedures;
		mProcessedExprWasActive = false;
	}

	@Override
	public Expression processExpression(Expression expr) {
		if (isDisguisedStruct(expr)) {
			return processDisguisedStruct((IdentifierExpression) expr);
		} else if (expr instanceof IdentifierExpression) {
			IdentifierExpression idExpr = (IdentifierExpression) expr;
			ILocation location = idExpr.getLocation();
			IType type = idExpr.getType();
			VarMapKey mapping = mReverseVarMap.get(
					new VarMapValue(idExpr.getIdentifier(), idExpr.getDeclarationInformation()));
			if (mapping == null) {
				mProcessedExprWasActive = true;
				return expr;
			}
			DeclarationInformation translatedDeclInfo = mapping.getDeclInfo();
			String translatedId = mapping.getVarId();
			Expression newExpr = new IdentifierExpression(location, type, translatedId, translatedDeclInfo);
			ModelUtils.mergeAnnotations(expr, newExpr);
			if (mapping.getInOldExprOfProc() != null) {
				newExpr = new UnaryExpression(location, type, Operator.OLD, idExpr);
			}
			if (translatedDeclInfo.getStorageClass() == GLOBAL
					|| translatedDeclInfo.getStorageClass() == QUANTIFIED
					|| mActiveProcedures.contains(translatedDeclInfo.getProcedure())) {
				mProcessedExprWasActive = true;
			}
			return newExpr;
		} else {
			return super.processExpression(expr);			
		}
	}

	/**
	 * Recognizes IdentifierExpressions, which actually should be StructAcessExpressions.
	 * <p>
	 * This method is a workaround, since one would expect structs instead of simple variables.
	 * Eventually the preprocessor's backtranslation misses them.
	 * 
	 * @param expr Expression
	 * @return The Expression is an IdentifierExpression, which was created by the preprocessor
	 *         to replace a StructAccessExpression.
	 */
	private boolean isDisguisedStruct(Expression expr) {
		if (expr instanceof IdentifierExpression) {
			IdentifierExpression idExpr = (IdentifierExpression) expr;
			return idExpr.getIdentifier().contains("!");
		}
		return false;
	}

	/**
	 * Backtranslates an IdentifierExpression, which actually should be an StructAcessExpression.
	 * <p>
	 * This method is a workaround, since one would expect structs instead of simple variables.
	 * Eventually the preprocessor's backtranslation misses them.
	 * 
	 * @param disguisedStruct IdentifierExpression from the preprocessor, which replaced a struct.
	 * @return Backtranslated struct as an IdentifierExpression.
	 */
	private IdentifierExpression processDisguisedStruct(IdentifierExpression disguisedStruct) {
		String[] idParts = disguisedStruct.getIdentifier().split("!", 2);
		assert idParts.length == 2 : "IdentifierExpression was no disguised struct: " + disguisedStruct;
		IdentifierExpression struct = new IdentifierExpression(
				disguisedStruct.getLocation(), null, idParts[0], disguisedStruct.getDeclarationInformation());
		IdentifierExpression newStruct = (IdentifierExpression) processExpression(struct);		
		return new IdentifierExpression(newStruct.getLocation(), disguisedStruct.getType(),
				newStruct.getIdentifier() + "!" + idParts[1], newStruct.getDeclarationInformation());
	} 
	
	/**
	 * Determines whether to keep processed variables in the ProgramState of an ProgramExecution or not.
	 * <p>
	 * A variable is active, if it isn't out of scope (like a local variable of procedure F,
	 * when the callstack doesn't contain F).
	 * Global variables and unknown variables (such without a mapping) are always considered to be active.
	 * <p>
	 * The returned value determines, that at least one of all processed variables was active.
	 * It is reseted with each call {@link #setInlinedActiveProcedures(Set)}.
	 * 
	 * @return One of the processed expressions (since the last call of {@link #setInlinedActiveProcedures(Set)})
	 *         was active.
	 */
	public boolean processedExprWasActive() {
		return mProcessedExprWasActive;
	}

}