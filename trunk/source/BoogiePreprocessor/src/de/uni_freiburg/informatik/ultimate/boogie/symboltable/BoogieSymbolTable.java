package de.uni_freiburg.informatik.ultimate.boogie.symboltable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.model.IType;
import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieVisitor;
import de.uni_freiburg.informatik.ultimate.model.boogie.DeclarationInformation.StorageClass;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Body;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Declaration;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.FunctionDeclaration;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Procedure;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VarList;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VariableDeclaration;

/**
 * BoogieSymbolTable is a symbol table for all scopes of a Boogie program.
 * 
 * It is still work in progress, so there are no final comments.
 * 
 * @author dietsch
 * 
 */
public class BoogieSymbolTable {

	private Map<StorageClass, Map<String, Map<String, Declaration>>> mSymbolTable;

	public BoogieSymbolTable() {
		mSymbolTable = new LinkedHashMap<>();
	}

	/**
	 * Add a procedure or function declaration to the symbol map. The symbol map
	 * will decide if this is an implementation, procedure, or function and
	 * store it accordingly.
	 * 
	 * @param symbolName
	 * @param decl
	 */
	protected void addProcedureOrFunction(String symbolName, Procedure decl) {
		Map<String, Declaration> procMap = getProcedureMap(decl);
		assert !procMap.containsKey(symbolName);
		procMap.put(symbolName, decl);
	}

	protected void addProcedureOrFunction(String symbolName, FunctionDeclaration decl) {
		addSymbol(StorageClass.PROC_FUNC, null, symbolName, decl);
	}

	protected void addInParams(String procedureOrFunctionName, String paramName, Procedure decl) {
		if (isImplementation(decl)) {
			addSymbol(StorageClass.IMPLEMENTATION_INPARAM, procedureOrFunctionName, paramName, decl);
		} else {
			addSymbol(StorageClass.PROC_FUNC_INPARAM, procedureOrFunctionName, paramName, decl);
		}
	}

	protected void addInParams(String procedureOrFunctionName, String paramName, FunctionDeclaration decl) {
		addSymbol(StorageClass.PROC_FUNC_INPARAM, procedureOrFunctionName, paramName, decl);
	}

	protected void addOutParams(String procedureOrFunctionName, String paramName, Procedure decl) {
		if (isImplementation(decl)) {
			addSymbol(StorageClass.IMPLEMENTATION_OUTPARAM, procedureOrFunctionName, paramName, decl);
		} else {
			addSymbol(StorageClass.PROC_FUNC_OUTPARAM, procedureOrFunctionName, paramName, decl);
		}
	}

	protected void addOutParams(String procedureOrFunctionName, String paramName, FunctionDeclaration decl) {
		addSymbol(StorageClass.PROC_FUNC_OUTPARAM, procedureOrFunctionName, paramName, decl);
	}

	protected void addLocalVariable(String procedureName, String variableName, Declaration decl) {
		addSymbol(StorageClass.LOCAL, procedureName, variableName, decl);
	}

	protected void addGlobalVariable(String variableName, Declaration decl) {
		addSymbol(StorageClass.GLOBAL, null, variableName, decl);
	}

	private Map<String, Declaration> getProcedureMap(Procedure decl) {
		if (isImplementation(decl)) {
			return getMap(StorageClass.IMPLEMENTATION, StorageClass.IMPLEMENTATION.toString());
		} else {
			return getMap(StorageClass.PROC_FUNC, StorageClass.PROC_FUNC.toString());
		}
	}

	private boolean isImplementation(Procedure decl) {
		return decl.getSpecification() == null;
	}

	private void addSymbol(StorageClass sc, String scopeName, String symbolName, Declaration decl) {
		if (scopeName == null) {
			scopeName = sc.toString();
		}
		AssertIsEmpty(sc, scopeName, symbolName);
		getMap(sc, scopeName).put(symbolName, decl);
	}

	private void AssertIsEmpty(StorageClass sc, String scopeName, String symbolName) {
		assert (!getMap(sc, scopeName).containsKey(symbolName));
	}

	private Map<String, Declaration> getMap(StorageClass sc, String scopeName) {
		scopeName = checkScopeName(sc, scopeName);

		switch (sc) {
		case IMPLEMENTATION:
		case PROC_FUNC:
		case GLOBAL:
		case QUANTIFIED:
			if (!mSymbolTable.containsKey(sc)) {
				Map<String, Map<String, Declaration>> outer = new LinkedHashMap<String, Map<String, Declaration>>();
				Map<String, Declaration> inner = new LinkedHashMap<String, Declaration>();
				outer.put(scopeName, inner);
				mSymbolTable.put(sc, outer);
			}
			return mSymbolTable.get(sc).get(scopeName);
		case PROC_FUNC_INPARAM:
		case PROC_FUNC_OUTPARAM:
		case IMPLEMENTATION_INPARAM:
		case IMPLEMENTATION_OUTPARAM:
		case LOCAL:
			if (!mSymbolTable.containsKey(sc)) {
				Map<String, Map<String, Declaration>> outer = new LinkedHashMap<String, Map<String, Declaration>>();
				Map<String, Declaration> inner = new LinkedHashMap<String, Declaration>();
				outer.put(scopeName, inner);
				mSymbolTable.put(sc, outer);
			}
			Map<String, Map<String, Declaration>> scopeMap = mSymbolTable.get(sc);
			if (!scopeMap.containsKey(scopeName)) {
				scopeMap.put(scopeName, new LinkedHashMap<String, Declaration>());
			}
			return scopeMap.get(scopeName);
		default:
			throw new UnsupportedOperationException(String.format("Extend this method for the new scope %s", sc));
		}
	}

	private Collection<String> getSymbolNames(StorageClass scope, String scopeName) {
		scopeName = checkScopeName(scope, scopeName);

		if (!mSymbolTable.containsKey(scope)) {
			return new ArrayList<String>();
		}
		Map<String, Declaration> decls = getMap(scope, scopeName);
		if (decls == null) {
			return new ArrayList<String>();
		}
		ArrayList<String> rtr = new ArrayList<>();
		rtr.addAll(decls.keySet());
		return rtr;
	}

	private String checkScopeName(StorageClass scope, String scopeName) {
		switch (scope) {
		case IMPLEMENTATION:
		case GLOBAL:
		case PROC_FUNC:
			return scope.toString();
		default:
			break;
		}
		if (scopeName == null) {
			throw new IllegalArgumentException("scopeName must be non-null");
		}
		return scopeName;
	}

	/**
	 * Returns a list of declarations for the name of a function or procedure.
	 * <ul>
	 * <li>The list is emtpy if the symbol is not in the program</li>
	 * <li>The list contains one function declaration if the symbol is for a
	 * function</li>
	 * <li>For procedures, the list may contain up to two procedure
	 * declarations: One for the implementation, one for the specification.</li>
	 * </ul>
	 * 
	 * @param symbolname
	 * @return
	 */
	public List<Declaration> getFunctionOrProcedureDeclaration(String symbolname) {
		final StorageClass[] procedures = new StorageClass[] { StorageClass.IMPLEMENTATION, StorageClass.PROC_FUNC };
		ArrayList<Declaration> rtr = new ArrayList<>();
		for (StorageClass sc : procedures) {
			Declaration decl = getDeclaration(symbolname, sc, null);
			if (decl != null) {
				rtr.add(decl);
			}
		}
		return rtr;
	}

	/**
	 * 
	 * @param symbolname
	 * @param scope
	 * @param scopeName
	 * @return
	 */
	public Declaration getDeclaration(String symbolname, StorageClass scope, String scopeName) {
		return getMap(scope, scopeName).get(symbolname);
	}

	public Declaration getDeclaration(IdentifierExpression exp) {
		return getDeclaration(exp.getIdentifier(), exp.getDeclarationInformation().getStorageClass(), exp
				.getDeclarationInformation().getProcedure());
	}

	public IType getTypeForVariableSymbol(String symbolname, StorageClass scope, String scopeName) {
		Declaration decl = getDeclaration(symbolname, scope, scopeName);
		if (decl == null) {
			return null;
		}
		return findType(decl, symbolname);
	}

	private IType findType(Declaration decl, String symbolname) {
		if (decl instanceof VariableDeclaration) {
			for (VarList vl : ((VariableDeclaration) decl).getVariables()) {
				for (String s : vl.getIdentifiers()) {
					if (s.equals(symbolname)) {
						return vl.getType().getBoogieType();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Produces a really long string describing the content of the symbol table.
	 * 
	 * @return A string representation of the symbol table.
	 */
	public String prettyPrintSymbolTable() {

		StringBuilder globals = new StringBuilder();

		// global variables
		Map<String, Declaration> globalsMap = getMap(StorageClass.GLOBAL, null);
		for (String s : globalsMap.keySet()) {
			globals.append(" * ").append(s).append(" : ")
					.append(getTypeForVariableSymbol(s, StorageClass.GLOBAL, null)).append("\n");
		}

		HashSet<String> functionSymbols = new HashSet<>();
		functionSymbols.addAll(getSymbolNames(StorageClass.IMPLEMENTATION, null));
		functionSymbols.addAll(getSymbolNames(StorageClass.PROC_FUNC, null));

		StringBuilder functions = new StringBuilder();
		StringBuilder procedures = new StringBuilder();
		StringBuilder implementations = new StringBuilder();

		// functions and procedures, inlined with local definitions
		for (String functionSymbol : functionSymbols) {
			// get the declaration(s) for the function or procedure symbol
			List<Declaration> decls = getFunctionOrProcedureDeclaration(functionSymbol);

			for (Declaration decl : decls) {
				// check what kind of symbol it is
				if (decl instanceof FunctionDeclaration) {
					functions.append(" * ").append(functionSymbol).append(" := ").append(decl).append("\n");
					// add the local variable declarations
					appendLocals(functions, functionSymbol);
				} else {
					Procedure proc = (Procedure) decl;
					if (isImplementation(proc)) {
						// implementations.append(" * ").append(functionSymbol).append(" := ").append(decl).append("\n");
						implementations.append(" * ").append(prettyPrintProcedureSignature(decl)).append("\n");
						appendLocals(implementations, functionSymbol);
					} else {
						// procedures.append(" * ").append(functionSymbol).append(" := ").append(decl).append("\n");
						procedures.append(" * ").append(prettyPrintProcedureSignature(decl)).append("\n");
						if (decls.size() == 1) {
							// only print locals if there is no implementation
							// defined (do not print locals 2 times)
							appendLocals(procedures, functionSymbol);
						}
					}
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		if (globals.length() > 0) {
			sb.append("Globals\n");
			sb.append(globals);
			sb.append("\n");
		}

		if (procedures.length() > 0) {
			sb.append("Procedures\n");
			sb.append(procedures);
			sb.append("\n");
		}

		if (implementations.length() > 0) {
			sb.append("Implementations\n");
			sb.append(implementations);
			sb.append("\n");
		}

		if (functions.length() > 0) {
			sb.append("Functions\n");
			sb.append(functions);
			sb.append("\n");
		}

		return sb.toString();

	}

	private void appendLocals(StringBuilder builder, String currentFunctionSymbol) {
		Collection<String> localSymbols = getSymbolNames(StorageClass.LOCAL, currentFunctionSymbol);
		if (localSymbols.size() == 0) {
			return;
		}
		for (String symbol : localSymbols) {
			IType type = getTypeForVariableSymbol(symbol, StorageClass.LOCAL, currentFunctionSymbol);
			builder.append("  * ").append(symbol).append(" : ").append(type).append("\n");
		}
	}

	private String prettyPrintProcedureSignature(Declaration decl) {
		PrettyPrinter signatureBuilder = new PrettyPrinter();
		try {
			return signatureBuilder.process(decl).toString();
		} catch (Throwable e) {
			e.printStackTrace();
			return "";
		}
	}

	private class PrettyPrinter extends BoogieVisitor {
		private StringBuilder sb;

		public StringBuilder process(Declaration node) {
			sb = new StringBuilder();
			processDeclaration(node);
			// replace the superfluous " returns " at the end (from
			// processVarLists)
			sb.replace(sb.length() - 9, sb.length(), "");
			return sb;
		}

		@Override
		protected void visit(Procedure decl) {
			sb.append(decl.getIdentifier());
		}

		@Override
		protected void visit(FunctionDeclaration decl) {
			sb.append(decl.getIdentifier());
		}

		@Override
		protected VarList[] processVarLists(VarList[] vls) {
			sb.append("(");
			VarList[] rtr = super.processVarLists(vls);
			if (vls.length > 0) {
				// replace the superfluous ", "
				sb.replace(sb.length() - 2, sb.length(), "");
			}
			sb.append(") returns ");
			return rtr;
		}

		@Override
		protected VarList processVarList(VarList vl) {
			String[] identifiers = vl.getIdentifiers();
			if (identifiers.length > 0) {
				for (String name : identifiers) {
					sb.append(name).append(" : ").append(vl.getType().getBoogieType()).append(", ");
				}
			}
			return super.processVarList(vl);
		}

		// prevent traversing the whole ast with the following overrides
		@Override
		protected Body processBody(Body body) {
			return body;
		}

		@Override
		protected Expression processExpression(Expression expr) {
			return expr;
		}
	}
}
