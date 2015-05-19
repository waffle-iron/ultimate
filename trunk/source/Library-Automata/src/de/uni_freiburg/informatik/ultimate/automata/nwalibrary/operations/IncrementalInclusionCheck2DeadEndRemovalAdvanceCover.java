package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.OperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonSimple;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedRun;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWord;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.IncrementalInclusionCheck2DeadEndRemoval.NodeData;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.incremental_inclusion.AbstractIncrementalInclusionCheck;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.incremental_inclusion.InclusionViaDifference;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.Transitionlet;
import de.uni_freiburg.informatik.ultimate.core.services.IUltimateServiceProvider;

/**
 * 
 * This is an implementation of incremental inclusion check based on the Bn baseline Algorithm.<br/>
 * We use InclusionViaDIfference to check its correctness.
 * 
 * @author jefferyyjhsu@iis.sinica.edu.tw
 *
 * @param <LETTER>
 * @param <STATE>
 */

public class IncrementalInclusionCheck2DeadEndRemovalAdvanceCover<LETTER,STATE> extends AbstractIncrementalInclusionCheck<LETTER,STATE> implements IOperation<LETTER, STATE>  {
	public int counter_run = 0, counter_total_nodes = 0 ;
	private INestedWordAutomatonSimple<LETTER, STATE> local_m_A;
	private List<INestedWordAutomatonSimple<LETTER, STATE>> local_m_B;
	private List<INestedWordAutomatonSimple<LETTER,STATE>> local_m_B2;
	private StateFactory<STATE> localStateFactory;
	private IUltimateServiceProvider localServiceProvider;
	public PseudoAutomata workingAutomata;
	public int nodeNumberBeforeDelete = 0;
	class PseudoAutomata{
		public INestedWordAutomatonSimple<LETTER,STATE> associatedAutomata;
		public PseudoAutomata prvPAutomata;
		public Set<LETTER> letter;
		public HashSet<NodeData> allNodes;
		public LinkedList<NodeData>errorNodes,currentTree;
		public LinkedList<NodeData> completeTree,coveredNodes,ACCNodes;
		public HashSet<NodeData> initialNodes;
		public PseudoAutomata(INestedWordAutomatonSimple<LETTER,STATE> a) throws OperationCanceledException{
			associatedAutomata = a;
			prvPAutomata = null;
			letter = a.getAlphabet();
			allNodes = new HashSet<NodeData>();
			errorNodes = new LinkedList<NodeData>();
			completeTree = new LinkedList<NodeData>();
			coveredNodes = new LinkedList<NodeData>();
			ACCNodes = new LinkedList<NodeData>();
			currentTree = expand(true,true);
			initialNodes = new HashSet<NodeData>(currentTree);
			do{
				if(cover(true)){
					break;
				}
				currentTree = expand(true,false);
			}while(true);
		}
		
		public PseudoAutomata(PseudoAutomata preAutomata,INestedWordAutomatonSimple<LETTER,STATE> bn) throws OperationCanceledException{
			associatedAutomata = bn;
			prvPAutomata = preAutomata;
			allNodes = new HashSet<NodeData>();
			errorNodes = new LinkedList<NodeData>();
			completeTree = new LinkedList<NodeData>();
			coveredNodes = new LinkedList<NodeData>();
			ACCNodes = new LinkedList<NodeData>();
			letter = prvPAutomata.getAlphabet();
			if(!letter.equals(bn.getAlphabet())){
				m_Logger.info("Alphabet inconsistent");
				return;
			}
			if(!prvPAutomata.ACCNodes.isEmpty()){
				prvPAutomata.finishACCover();
			}
			prvPAutomata.deadendRemove();
			currentTree = expand(false,true);
			initialNodes = new HashSet<NodeData>(currentTree);
			do{
				calculateAcceptingStates();
				if(cover(true)){
					break;
				}
				currentTree = expand(false,false);
			}while(true);
		}
				
		@SuppressWarnings("unchecked")
		public  LinkedList<NodeData> expand(boolean iteration1, boolean init) throws OperationCanceledException{
			if (!m_Services.getProgressMonitorService().continueProcessing()) {
                throw new OperationCanceledException(this.getClass());
			}
			LinkedList<NodeData> newNodes = new LinkedList<NodeData>();
			NodeData tempNodeData;
			if(iteration1){
				if(init){
					for(STATE initStateA : associatedAutomata.getInitialStates()){
						tempNodeData = new NodeData();
						if(associatedAutomata.isFinal(initStateA)){
							tempNodeData.accepting = true;
							errorNodes.add(tempNodeData);
						}else{
							tempNodeData.accepting = false;
						}
						tempNodeData.parentNode = null;
						tempNodeData.aState = null;
						tempNodeData.bStates.add(initStateA);
						tempNodeData.correspondingAState = initStateA;
						tempNodeData.hash = initStateA.hashCode();
						tempNodeData.word = new NestedRun<LETTER,STATE>(initStateA);
						newNodes.add(tempNodeData);		
						allNodes.add(tempNodeData);
					}
				}else{
					for(NodeData preNode : currentTree){
						assert preNode.bStates.size()==1;
						for(STATE s : preNode.bStates){
							for(OutgoingInternalTransition<LETTER, STATE> ATransition : associatedAutomata.internalSuccessors(s)){
								tempNodeData = new NodeData();
								if(associatedAutomata.isFinal(ATransition.getSucc())){
									tempNodeData.accepting = true;
									errorNodes.add(tempNodeData);
								}else{
									tempNodeData.accepting = false;
								}
								tempNodeData.parentNode = preNode;
								tempNodeData.aState = null;
								tempNodeData.correspondingAState = ATransition.getSucc();
								tempNodeData.bStates.add(ATransition.getSucc());
								tempNodeData.hash = ATransition.getSucc().hashCode();
								ArrayList<STATE> newStateSequence = (ArrayList<STATE>) preNode.word.getStateSequence().clone();
								newStateSequence.add(ATransition.getSucc());
								tempNodeData.word = new NestedRun<LETTER,STATE>(preNode.word.getWord().concatenate(new NestedWord<LETTER>(ATransition.getLetter(),-2)),newStateSequence);
								newNodes.add(tempNodeData);		
								allNodes.add(tempNodeData);
							}
						}
					}
				}
			}else{
				if(init){
					HashSet<STATE> bStates = new HashSet<STATE>();
					int BHash = 0;
					for(STATE BState : associatedAutomata.getInitialStates()){
						bStates.add(BState);
						BHash = BHash | BState.hashCode();
					}
					for(NodeData initNode : prvPAutomata.initialNodes){
						tempNodeData = new NodeData();
						tempNodeData.parentNode = null;
						tempNodeData.aState = initNode;
						tempNodeData.correspondingAState = initNode.correspondingAState;
						tempNodeData.bStates = (HashSet<STATE>) bStates.clone();
						tempNodeData.hash = BHash;
						tempNodeData.word = new NestedRun<LETTER,STATE>(tempNodeData.correspondingAState);
						newNodes.add(tempNodeData);		
						allNodes.add(tempNodeData);
					}
				}else{
					for(NodeData preNode : currentTree){
						for(Transition tran : prvPAutomata.internalSuccessors(preNode.aState)){
							tempNodeData = new NodeData();
							tempNodeData.parentNode = preNode;
							tempNodeData.aState = tran.getSucc();
							tempNodeData.correspondingAState = tran.getSucc().correspondingAState;
							for(STATE preBState : preNode.bStates){
								for(OutgoingInternalTransition<LETTER, STATE> BTransition :associatedAutomata.internalSuccessors(preBState,tran.getLetter())){
									tempNodeData.bStates.add(BTransition.getSucc());
									tempNodeData.hash = tempNodeData.hash | BTransition.getSucc().hashCode();
								}
							}
							ArrayList<STATE> newStateSequence = (ArrayList<STATE>) preNode.word.getStateSequence().clone();
							newStateSequence.add(tempNodeData.correspondingAState);
							tempNodeData.word = new NestedRun<LETTER,STATE>(preNode.word.getWord().concatenate(new NestedWord<LETTER>(tran.getLetter(),-2)),newStateSequence);
							newNodes.add(tempNodeData);		
							allNodes.add(tempNodeData);
						}
					}
				}
			}
			return newNodes;
		}
		
		public void calculateAcceptingStates() throws OperationCanceledException{
			if (!m_Services.getProgressMonitorService().continueProcessing()) {
                throw new OperationCanceledException(this.getClass());
			}
			NodeData currentNodeSet1 = null;
			int i;
			for(i=0;i<currentTree.size();i++){
				currentNodeSet1 = currentTree.get(i);
				if(currentNodeSet1.aState.accepting){
					currentNodeSet1.accepting = true;
					for(STATE state : currentNodeSet1.bStates){
						if(associatedAutomata.isFinal(state)){
							currentNodeSet1.accepting = false;
							break;
						}
					}
					if(currentNodeSet1.accepting == true){
						errorNodes.add(currentNodeSet1);
					}
				}else{
					currentNodeSet1.accepting = false;
				}
			}
		}
		
		public boolean cover(boolean acc) throws OperationCanceledException{
			if (!m_Services.getProgressMonitorService().continueProcessing()) {
                throw new OperationCanceledException(this.getClass());
			}
			//cover() will need to write appropriate outgoing transition for previous nodes
			boolean newNodeInCompleteTree = false;
			boolean containsAllbnState = false;
			NodeData currentNodeSet1 = null,potenialACCCandidate = null;
			LinkedList<NodeData> toBeDeleteed = new LinkedList<NodeData>();
			int i,j;
			for(i=0;i<currentTree.size();i++){
				currentNodeSet1 = currentTree.get(i);
				containsAllbnState = false;
				potenialACCCandidate = null;
				if(completeTree.size()!=0){
					for(NodeData completeNodeSet:completeTree){
						if((currentNodeSet1.aState == completeNodeSet.aState)&&completeNodeSet.hash==(currentNodeSet1.hash&completeNodeSet.hash)&&(currentNodeSet1.bStates.size() >= completeNodeSet.bStates.size())){
							if(!currentNodeSet1.bStates.equals(completeNodeSet.bStates)){
								containsAllbnState = false;
								if(potenialACCCandidate == null || potenialACCCandidate.bStates.size()>completeNodeSet.bStates.size()){
									if(currentNodeSet1.bStates.containsAll(completeNodeSet.bStates)){
										potenialACCCandidate = completeNodeSet;
									}
								}
							}
							else{
								containsAllbnState = true;
								currentNodeSet1.coveredBy = completeNodeSet;
								currentNodeSet1.identicalCover = true;
								completeNodeSet.covering.add(currentNodeSet1);
								if(currentNodeSet1.parentNode!=null){
									currentNodeSet1.parentNode.outgoingTransition.add(new Transition(currentNodeSet1.word.getSymbol(currentNodeSet1.word.getLength()-2),completeNodeSet));	
								}
								coveredNodes.add(currentNodeSet1);
								toBeDeleteed.add(currentNodeSet1);
								break;
							}
						}
					}
				}else{
					containsAllbnState = false;
				}
				if(!containsAllbnState){
					for(j = 0;j<currentTree.size();j++){
						NodeData currentNodeSet2 = currentTree.get(j);
						if(currentNodeSet1!=currentNodeSet2&&!(currentNodeSet2.coveredBy == null)&&(currentNodeSet1.aState == currentNodeSet2.aState)&&(currentNodeSet2.hash==(currentNodeSet1.hash&currentNodeSet2.hash)&&(currentNodeSet1.bStates.size() >= currentNodeSet2.bStates.size()))){
							if(!currentNodeSet1.bStates.equals(currentNodeSet2.bStates)){
								containsAllbnState = false;
								if(potenialACCCandidate == null || potenialACCCandidate.bStates.size()>currentNodeSet2.bStates.size()){
									if(currentNodeSet1.bStates.containsAll(currentNodeSet2.bStates)){
										potenialACCCandidate = currentNodeSet2;
									}
								}
							}
							else{
								containsAllbnState = true;
								currentNodeSet1.coveredBy = currentNodeSet2;
								currentNodeSet1.identicalCover = true;
								currentNodeSet2.covering.add(currentNodeSet1);
								if(currentNodeSet1.parentNode!=null){
									currentNodeSet1.parentNode.outgoingTransition.add(new Transition(currentNodeSet1.word.getSymbol(currentNodeSet1.word.getLength()-2),currentNodeSet2));
								}
								coveredNodes.add(currentNodeSet1);
								toBeDeleteed.add(currentNodeSet1);
								break;
							}
						}
					}
				}
				if(!containsAllbnState){
					if(potenialACCCandidate == null || acc == false){
						newNodeInCompleteTree = true;
						completeTree.add(currentNodeSet1);
						if(currentNodeSet1.parentNode!=null){
							currentNodeSet1.parentNode.outgoingTransition.add(new Transition(currentNodeSet1.word.getSymbol(currentNodeSet1.word.getLength()-2),currentNodeSet1));
						}
					}else{
						currentNodeSet1.coveredBy = potenialACCCandidate;
						currentNodeSet1.identicalCover = false;
						potenialACCCandidate.covering.add(currentNodeSet1);
						if(currentNodeSet1.parentNode!=null){
							currentNodeSet1.parentNode.outgoingTransition.add(new Transition(currentNodeSet1.word.getSymbol(currentNodeSet1.word.getLength()-2),currentNodeSet1));
						}
						ACCNodes.add(currentNodeSet1);
						toBeDeleteed.add(currentNodeSet1);
					}
				}
			}
			currentTree.removeAll(toBeDeleteed);
			return !newNodeInCompleteTree;
		}
		
		@SuppressWarnings("unchecked")
		public void finishACCover() throws OperationCanceledException{
			if (!m_Services.getProgressMonitorService().continueProcessing()) {
                throw new OperationCanceledException(this.getClass());
			}
			assert !ACCNodes.isEmpty();
			assert currentTree.isEmpty();
			currentTree.addAll(ACCNodes);
			ACCNodes.clear();
			for(NodeData node : currentTree){
				node.coveredBy.covering.remove(node);
				node.coveredBy = null;
			}
			do{
				if(prvPAutomata == null){
					if(cover(false)){
						break;
					}
					currentTree = expand(true,false);
				}else{
					if(cover(false)){
						break;
					}
					currentTree = expand(false,false);
					calculateAcceptingStates();
				}
			}while(true);
		}
		
		private HashSet<NodeData> toBeKeepedNodes;
		public void deadendRemove(){
			assert ACCNodes.isEmpty();
			assert currentTree.isEmpty();
			toBeKeepedNodes = new HashSet<NodeData>();
			HashSet<NodeData> toBeDeletedNodes = new HashSet<NodeData>(allNodes);
			for(NodeData nodes : allNodes){
				nodes.keep = false;
			}
			for(NodeData errorNode : errorNodes){
				deadEndRemoveWalker(errorNode);
			}
			toBeDeletedNodes.removeAll(toBeKeepedNodes);
			for(NodeData nodeToBeDelete : toBeDeletedNodes){
				Transition removeTran = null;
				if(nodeToBeDelete.identicalCover){
					if(nodeToBeDelete.parentNode!=null){
						for(Transition tran: nodeToBeDelete.parentNode.outgoingTransition){
							if(tran.getSucc() == nodeToBeDelete.coveredBy){
								removeTran  = tran;
								break;
							}
						}
					}
				}else{
					if(nodeToBeDelete.parentNode!=null){
						for(Transition tran: nodeToBeDelete.parentNode.outgoingTransition){
							if(tran.getSucc() == nodeToBeDelete){
								removeTran  = tran;
								break;
							}
						}
					}
				}
				nodeToBeDelete.parentNode.outgoingTransition.remove(removeTran);
				if(completeTree.contains(nodeToBeDelete)){
					completeTree.remove(nodeToBeDelete);
				}else{
					if(coveredNodes.contains(nodeToBeDelete)){
						coveredNodes.remove(nodeToBeDelete);
					}
				}
				if(nodeToBeDelete.parentNode!=null){
					nodeToBeDelete.parentNode.DeadEndsRemoved = true;
				}
			}
			allNodes.removeAll(toBeDeletedNodes);
		}
		private void deadEndRemoveWalker(NodeData node){
			assert node !=null;
			if(!node.keep){
				node.keep = true;
				toBeKeepedNodes.add(node);
				for(NodeData coveringNode : node.covering){
					deadEndRemoveWalker(coveringNode);
				}
				if(node.parentNode != null){
					deadEndRemoveWalker(node.parentNode);
				}
			}
		}
		
		public Set<LETTER> getAlphabet(){
			return letter;
		}
		
		public Set<NodeData> getInitialStates(){
			return initialNodes;
		}
		
		public LinkedList<Transition> internalSuccessors(NodeData node){
			return node.outgoingTransition;
		}
		public LinkedList<Transition> internalSuccessors(NodeData node, LETTER let){
			LinkedList<Transition> result = new LinkedList<Transition>();
			for(Transition tran : node.outgoingTransition){
				if(tran.getLetter().equals(let)){
					result.add(tran);
				}
			}
			return result;
		}
	}
	class Transition implements Transitionlet<LETTER,STATE>{
		private LETTER letter;
		private NodeData succ;
		public Transition(LETTER let,NodeData node){
			letter = let;
			succ = node;
		}
		public LETTER getLetter(){
			return letter;
		}
		public NodeData getSucc(){
			return succ;
		}
	}
	class NodeData{
		public boolean DeadEndsRemoved;
		public boolean keep;
		public int hash;
		public NodeData coveredBy = null;
		public boolean accepting;
		public NodeData parentNode;
		public boolean identicalCover;
		public HashSet<NodeData> covering;
		public NodeData aState;
		public STATE correspondingAState;
		public HashSet<STATE> bStates;
		public LinkedList<Transition> outgoingTransition; 
		public NestedRun<LETTER,STATE> word;
		public NodeData(){
			identicalCover = false;
			DeadEndsRemoved = false;
			bStates = new HashSet<STATE>();
			word = null;
			covering = new HashSet<NodeData>();
			outgoingTransition = new LinkedList<Transition>();
			hash = 0;
			accepting = false;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IncrementalInclusionCheck2DeadEndRemovalAdvanceCover(IUltimateServiceProvider services, StateFactory<STATE> sf,
			INestedWordAutomatonSimple<LETTER, STATE> a, List<INestedWordAutomatonSimple<LETTER,STATE>> b) throws AutomataLibraryException{
		super(services,a);
		IncrementalInclusionCheck2DeadEndRemovalAdvanceCover.abortIfContainsCallOrReturn(a);
		localServiceProvider = services;
		localStateFactory = sf;
		m_Logger.info(startMessage());
		local_m_A =  a;
		local_m_B = new ArrayList<INestedWordAutomatonSimple<LETTER, STATE>>();
		local_m_B2 = new ArrayList<INestedWordAutomatonSimple<LETTER, STATE>>();
		workingAutomata = new PseudoAutomata(local_m_A);
		for(INestedWordAutomatonSimple<LETTER,STATE> bn : b){
			try {
				super.addSubtrahend(bn);
				if(!getResult()){
					workingAutomata = new PseudoAutomata(workingAutomata,bn);
				}
			} catch (AutomataLibraryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			local_m_B.add(bn);
			local_m_B2.add(bn);
			
		}
		m_Logger.info(exitMessage());
	}

	@Override
	public void addSubtrahend(INestedWordAutomatonSimple<LETTER, STATE> nwa) throws AutomataLibraryException {
		m_Logger.info(startMessage());
		super.addSubtrahend(nwa);
		local_m_B.add(nwa);
		local_m_B2.add(nwa);
		if(!getResult()){
			workingAutomata = new PseudoAutomata(workingAutomata,nwa);
		}
		m_Logger.info(exitMessage());
	}
	
	public NestedRun<LETTER,STATE> getCounterexample(){
		if(workingAutomata.errorNodes.peekFirst()!=null){
			return workingAutomata.errorNodes.peekFirst().word;
		}else{
			return null;
		}
		
	}
	@Override
	public String operationName() {
		return "IncrementalInclusionCheck2DeadEndRemovalAdvanceCoverDeadEndRemoval";
	}
	@Override
	public String startMessage() {
		return "Start " + operationName();
	}
	
	@Override
	public String exitMessage() {
		return "Exit " + operationName();
	}
	
	public Boolean getResult(){
		return getCounterexample() == null;
	}
	@Override
	public boolean checkResult(StateFactory<STATE> stateFactory)
			throws AutomataLibraryException {
		boolean checkResult;
		if(getCounterexample() != null){
			checkResult = compareInclusionCheckResult(localServiceProvider, 
					localStateFactory, local_m_A, local_m_B2, getCounterexample());
		}
		else{
			checkResult = compareInclusionCheckResult(localServiceProvider, 
					localStateFactory, local_m_A, local_m_B2, null);
		}
		return checkResult;

	}
	
	/**
	 * Compare the result of an inclusion check with an inclusion check based
	 * on a emptiness/difference operations.
	 * The NestedRun ctrEx is the result of an inclusion check whose inputs
	 * are an automaton <b>a</b> and and a list of automata b.
	 * If the language of <b>a</b> is included in the union of all languages of the
	 * automata b then ctrEx is null, otherwise ctrEx is a run of <b>a</b> that
	 * is a counterexample to the inclusion.
	 * This method returns true if the difference-based inclusion check comes
	 * up with the same result, i.e., if it find a counterexample if ctrEx is
	 * non-null and if it does not find a counterexample if ctrEX is null.
	 * Note that if inclusion does not hold, there may be several 
	 * counterexamples. Dies method does NOT require that both methods return
	 * exactly the same counterexample. 
	 */
	public static <LETTER, STATE> boolean compareInclusionCheckResult(
			IUltimateServiceProvider services, 
			StateFactory<STATE> stateFactory, 
			INestedWordAutomatonSimple<LETTER, STATE> a, 
			List<INestedWordAutomatonSimple<LETTER, STATE>> b, 
			NestedRun<LETTER,STATE> ctrEx) throws AutomataLibraryException {
		InclusionViaDifference<LETTER, STATE> ivd = 
				new InclusionViaDifference<LETTER, STATE>(services, stateFactory, a);
		// add all b automata
		for (INestedWordAutomatonSimple<LETTER, STATE> bi : b) {
			ivd.addSubtrahend(bi);
		}
		// obtain counterexample, counterexample is null if inclusion holds
		NestedRun<LETTER, STATE> ivdCounterexample = ivd.getCounterexample();
		// return true iff both counterexamples are null or both counterexamples
		// are non-null.
		boolean result;
		if (ivdCounterexample == null) {
			if (ctrEx == null) {
				result = true;
			} else {
				result = false;
			}
		} else {
			if (ctrEx == null) {
				result = false;
			} else {
				result = true;
			}
		}
		return result;
	}

	public static <LETTER, STATE> void abortIfContainsCallOrReturn(INestedWordAutomatonSimple<LETTER, STATE> a) {
		if (!a.getCallAlphabet().isEmpty() || !a.getReturnAlphabet().isEmpty()) {
			throw new UnsupportedOperationException("Operation does not support call or return");
		}
	}
}