package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.OperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonSimple;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedRun;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWord;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.IncrementalInclusionCheck2DeadEndRemovalAdvanceCover_2Stacks.PseudoAutomata;
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

public class IncrementalInclusionCheck2DeadEndRemovalAdvanceCover_2Stacks_multipleCounterExamplesAtOnce<LETTER,STATE> extends AbstractIncrementalInclusionCheck<LETTER,STATE> implements IOperation<LETTER, STATE>  {
	public int counter_run = 0, counter_total_nodes = 0 ;
	private INestedWordAutomatonSimple<LETTER, STATE> local_m_A;
	private INestedWordAutomatonSimple<LETTER,STATE> current_m_B;
	private LinkedList<INestedWordAutomatonSimple<LETTER,STATE>> union_m_Bs;
	private List<INestedWordAutomatonSimple<LETTER, STATE>> local_m_B;
	private List<INestedWordAutomatonSimple<LETTER,STATE>> local_m_B2;
	private StateFactory<STATE> localStateFactory;
	private IUltimateServiceProvider localServiceProvider;
	private int counterExampleFlag;
	public PseudoAutomata workingAutomata;
	public LinkedList<PseudoAutomata> prvPAutomaton;
	public int nodeNumberBeforeDelete = 0;
	public int totalNodes = 0, totalAACNodes = 0, totalCoveredNodes = 0,totalUniqueNodes = 0;
	private boolean m_acc;
	
	class NfaUnion implements IOperation<LETTER, STATE> {
		INestedWordAutomatonSimple<LETTER,STATE> orgin,target;
		NestedWordAutomaton<LETTER,STATE> result;
		Collection<STATE> state1,state2;
		Collection<LETTER> letter1,letter2,newLetterSet;
		private final IUltimateServiceProvider m_Services;
		public StateFactory<STATE> stateFactory;
		private Logger s_Logger;
		public NfaUnion(IUltimateServiceProvider services,StateFactory<STATE> sf,INestedWordAutomatonSimple<LETTER,STATE> in1,INestedWordAutomatonSimple<LETTER,STATE> in2){
			m_Services=services;
			stateFactory =sf;
			//s_Logger = (Logger) m_Services.getLoggingService();
			s_Logger = m_Logger;
			s_Logger.info(startMessage());
			orgin = in1;
			target = in2;
			letter1=orgin.getInternalAlphabet();
			letter2=target.getInternalAlphabet();
			newLetterSet = new HashSet<LETTER>();
			newLetterSet.addAll(letter1);
			newLetterSet.addAll(letter2);
			union();
			s_Logger.info(exitMessage());
		}
		public void union(){
			result = new NestedWordAutomaton<LETTER, STATE>(m_Services,(Set<LETTER>)newLetterSet,null,null,stateFactory);
			HashSet<STATE> currentStates = new HashSet<STATE>();
			HashSet<STATE> nextRoundStates = new HashSet<STATE>();
			HashSet<STATE> finishedStates = new HashSet<STATE>();
			currentStates.addAll((Collection<? extends STATE>) orgin.getInitialStates());
			finishedStates.addAll(currentStates);
			do{
				for(STATE a:currentStates){
					result.addState(orgin.isInitial(a), orgin.isFinal(a), a);
					for(OutgoingInternalTransition<LETTER, STATE> tran : orgin.internalSuccessors(a)){
						result.addInternalTransition(a, tran.getLetter(), tran.getSucc());
						if(!finishedStates.contains(tran.getSucc())){
							nextRoundStates.add( tran.getSucc());
							finishedStates.add(tran.getSucc());
						}
					}
				}
				if(nextRoundStates.isEmpty()){
					break;
				}else{
					currentStates.clear();
					currentStates.addAll(nextRoundStates);
					nextRoundStates.clear();
				}
			}while(true);
			finishedStates.clear();
			currentStates.clear();
			nextRoundStates.clear();
			currentStates.addAll((Collection<? extends STATE>) target.getInitialStates());
			finishedStates.addAll(currentStates);
			do{
				for(STATE a:currentStates){
					result.addState(target.isInitial(a), target.isFinal(a), a);
					for(OutgoingInternalTransition<LETTER, STATE> tran : target.internalSuccessors(a)){
						result.addInternalTransition(a, tran.getLetter(), tran.getSucc());
						if(!finishedStates.contains(tran.getSucc())){
							nextRoundStates.add( tran.getSucc());
							finishedStates.add(tran.getSucc());
						}
					}
				}
				if(nextRoundStates.isEmpty()){
					break;
				}else{
					currentStates.clear();
					currentStates.addAll(nextRoundStates);
					nextRoundStates.clear();
				}
			}while(true);
			/*for(STATE a:state1){
				result.addState(orgin.getInitialStates().contains(a), orgin.getFinalStates().contains(a), a);
			}
			for(STATE a:state2){
				result.addState(target.getInitialStates().contains(a), target.getFinalStates().contains(a), a);
			}
			for(STATE x:state1){
				for(OutgoingInternalTransition<LETTER, STATE> y:orgin.internalSuccessors(x)){
					result.addInternalTransition(x, y.getLetter(), y.getSucc());
				}
			}
			for(STATE x:state2){
				for(OutgoingInternalTransition<LETTER, STATE> y:target.internalSuccessors(x)){
					result.addInternalTransition(x, y.getLetter(), y.getSucc());
				}
			}*/
		}
		@Override
		public String operationName() {
			return "NfaUnion";
		}
		
		@Override
		public String startMessage() {
			return "Start " + operationName();
		}
		
		@Override
		public String exitMessage() {
			return "Exit " + operationName();
		}
		@Override
		public NestedWordAutomaton<LETTER,STATE> getResult()throws OperationCanceledException {
			return result;
		}
		public boolean checkResult(StateFactory<STATE> stateFactory)
				throws OperationCanceledException {
			return true;
		}

	}

	class PseudoAutomata{
		public INestedWordAutomatonSimple<LETTER,STATE> associatedAutomata;
		public PseudoAutomata prvPAutomata;
		public Set<LETTER> letter;
		public HashSet<NodeData> allNodes;
		public LinkedList<NodeData>errorNodes,errorNodes2,currentTree,stack2;
		//public LinkedList<NodeData> completeTree,coveredNodes,ACCNodes;
		public LinkedList<NodeData> coveredNodes;
		public HashMap<NodeData, LinkedList<NodeData>> completeTree,ACCNodes;
		public HashSet<NodeData> initialNodes;
		public boolean testing_newAcceptingState = false;
		public PseudoAutomata(INestedWordAutomatonSimple<LETTER,STATE> a) throws OperationCanceledException{
			associatedAutomata = a;
			prvPAutomata = null;
			letter = a.getAlphabet();
			allNodes = new HashSet<NodeData>();
			errorNodes = new LinkedList<NodeData>();
			errorNodes2 = new LinkedList<NodeData>();
			stack2 = new LinkedList<NodeData>();
			completeTree = new HashMap<NodeData, LinkedList<NodeData>>();
			coveredNodes = new LinkedList<NodeData>();
			ACCNodes = new HashMap<NodeData, LinkedList<NodeData>>();
			currentTree = expand(true,true);
			initialNodes = new HashSet<NodeData>(currentTree);
			do{
				if(cover(m_acc)){
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
			errorNodes2 = new LinkedList<NodeData>();
			stack2 = new LinkedList<NodeData>();
			completeTree = new HashMap<NodeData, LinkedList<NodeData>>();
			coveredNodes = new LinkedList<NodeData>();
			ACCNodes = new HashMap<NodeData, LinkedList<NodeData>>();
			letter = prvPAutomata.getAlphabet();
			if(!letter.equals(bn.getAlphabet())){
				m_Logger.info("Alphabet inconsistent");
				return;
			}
			prvPAutomata.deadendRemove();
			HashMap<NodeData,LinkedList<NodeData>> finishTheseACCFirst = new HashMap<NodeData,LinkedList<NodeData>>(); 
			for(NodeData node :prvPAutomata.ACCNodes.keySet()){
				for(NodeData node2 : prvPAutomata.ACCNodes.get(node)){
					if(node2.keep&&!node2.parentNode.keep_Hard){
						//finishTheseACCFirst2.add(node2);
						if(!finishTheseACCFirst.keySet().contains(node2.parentNode)){
							finishTheseACCFirst.put(node2.parentNode, new LinkedList<NodeData>());
						}
						finishTheseACCFirst.get(node2.parentNode).add(node2);
					}
				}
			}
			for(NodeData parentNode : finishTheseACCFirst.keySet()){
				prvPAutomata.finishACCover2(parentNode,finishTheseACCFirst.get(parentNode));
			}
			if(!finishTheseACCFirst.isEmpty()){
				prvPAutomata.deadendRemove();
			}
			currentTree = expand(false,true);
			initialNodes = new HashSet<NodeData>(currentTree);
			do{
				calculateAcceptingStates();
				if(cover(m_acc)){
					if(!stack2.isEmpty()){
						HashSet<NodeData> prvNodesToBeFinish = new  HashSet<NodeData>();
						for(NodeData stack2Nodes :stack2){
							prvNodesToBeFinish.add(stack2Nodes.aState);
						}
						preAutomata.finishACCover(prvNodesToBeFinish);
						currentTree.clear();
						currentTree.addAll(stack2);
						do{
							currentTree = expand(false,false);
							calculateAcceptingStates();
							if(cover(m_acc)){
								break;
							}
						}while(true);
					}
					break;
				}
				currentTree = expand(false,false);
			}while(true);
//			deadendRemove();
//			//HashSet<NodeData> finishTheseACCFirst2 = new HashSet<NodeData>();
//			HashMap<NodeData,LinkedList<NodeData>> finishTheseACCFirst = new HashMap<NodeData,LinkedList<NodeData>>(); 
//			for(NodeData node :ACCNodes.keySet()){
//				for(NodeData node2 : ACCNodes.get(node)){
//					if(node2.keep&&!node2.parentNode.keep_Hard){
//						//finishTheseACCFirst2.add(node2);
//						if(!finishTheseACCFirst.keySet().contains(node2.parentNode)){
//							finishTheseACCFirst.put(node2.parentNode, new LinkedList<NodeData>());
//						}
//						finishTheseACCFirst.get(node2.parentNode).add(node2);
//					}
//				}
//			}
//			//finishACCover(finishTheseACCFirst2);
//			for(NodeData parentNode : finishTheseACCFirst.keySet()){
//				finishACCover2(parentNode,finishTheseACCFirst.get(parentNode));
//			}
			//deadendRemove();
			//記得改回比較快的位置(有下一個iteration再做deadend remove) (還要改finishACC)
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
						totalNodes ++;
						if(associatedAutomata.isFinal(initStateA)){
							tempNodeData.accepting = true;
							errorNodes.add(tempNodeData);
							errorNodes2.add(tempNodeData);
							testing_newAcceptingState = true;
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
						if(preNode.coveredBy == null){
							assert preNode.bStates.size()==1;
							for(STATE s : preNode.bStates){
								for(OutgoingInternalTransition<LETTER, STATE> ATransition : associatedAutomata.internalSuccessors(s)){
									tempNodeData = new NodeData();
									totalNodes ++;
									if(associatedAutomata.isFinal(ATransition.getSucc())){
										tempNodeData.accepting = true;
										errorNodes.add(tempNodeData);
										errorNodes2.add(tempNodeData);
										testing_newAcceptingState = true;
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
						if(initNode.keep == true){
							tempNodeData = new NodeData();
							totalNodes ++;
							tempNodeData.parentNode = null;
							tempNodeData.aState = initNode;
							tempNodeData.correspondingAState = initNode.correspondingAState;
							tempNodeData.bStates = (HashSet<STATE>) bStates.clone();
							tempNodeData.hash = BHash;
							tempNodeData.word = new NestedRun<LETTER,STATE>(tempNodeData.correspondingAState);
							newNodes.add(tempNodeData);		
							allNodes.add(tempNodeData);
						}
					}
				}else{
					for(NodeData preNode : currentTree){
						if(preNode.coveredBy == null){
							for(Transition tran : prvPAutomata.internalSuccessors(preNode.aState)){
								if(tran.getSucc().keep == true){
									tempNodeData = new NodeData();
									totalNodes ++;
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
				}
			}
			return newNodes;
		}
		
		public void calculateAcceptingStates() throws OperationCanceledException{
			if (!m_Services.getProgressMonitorService().continueProcessing()) {
                throw new OperationCanceledException(this.getClass());
			}
			for(NodeData currentNodeSet1:currentTree){
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
						errorNodes2.add(currentNodeSet1);
						testing_newAcceptingState = true;
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
			//NodeData currentNodeSet1 = null,potenialACCCandidate = null;
			NodeData potenialACCCandidate = null;
			LinkedList<NodeData> toBeDeleteed = new LinkedList<NodeData>();
			//int i,j;
			//for(i=0;i<currentTree.size();i++){
				//currentNodeSet1 = currentTree.get(i);
			for(NodeData currentNodeSet1 : currentTree){
				containsAllbnState = false;
				potenialACCCandidate = null;
				if(completeTree.containsKey(currentNodeSet1.aState)){
					for(NodeData completeNodeSet:completeTree.get(currentNodeSet1.aState)){
						if(completeNodeSet.hash==(currentNodeSet1.hash&completeNodeSet.hash)&&(currentNodeSet1.bStates.size() >= completeNodeSet.bStates.size())){
							if(currentNodeSet1.bStates.containsAll(completeNodeSet.bStates)){
								if(currentNodeSet1.bStates.size() == completeNodeSet.bStates.size()){
									containsAllbnState = true;
									totalCoveredNodes++;
									currentNodeSet1.coveredBy = completeNodeSet;
									currentNodeSet1.identicalCover = true;
									completeNodeSet.covering.add(currentNodeSet1);
									if(currentNodeSet1.parentNode!=null){
										currentNodeSet1.parentNode.outgoingTransition.add(new Transition(currentNodeSet1.word.getSymbol(currentNodeSet1.word.getLength()-2),completeNodeSet));	
									}
									coveredNodes.add(currentNodeSet1);
									//toBeDeleteed.add(currentNodeSet1);
									break;
								}else{
									if(acc){
										currentNodeSet1.ACCPotentialCoverBy.add(completeNodeSet);
										if(potenialACCCandidate == null || potenialACCCandidate.bStates.size()>completeNodeSet.bStates.size()){
											potenialACCCandidate = completeNodeSet;
										}
									}
								}
							}
						}
					}
				}
				if(acc &&!containsAllbnState){
					if(ACCNodes.containsKey(currentNodeSet1.aState)){
						for(NodeData completeNodeSet:ACCNodes.get(currentNodeSet1.aState)){
							if(completeNodeSet.hash==(currentNodeSet1.hash&completeNodeSet.hash)&&(currentNodeSet1.bStates.size() == completeNodeSet.bStates.size())){
								if(currentNodeSet1.bStates.containsAll(completeNodeSet.bStates)){
									containsAllbnState = true;
									totalCoveredNodes++;
									currentNodeSet1.coveredBy = completeNodeSet;
									currentNodeSet1.identicalCover = true;
									completeNodeSet.covering.add(currentNodeSet1);
									if(currentNodeSet1.parentNode!=null){
										currentNodeSet1.parentNode.outgoingTransition.add(new Transition(currentNodeSet1.word.getSymbol(currentNodeSet1.word.getLength()-2),completeNodeSet));	
									}
									coveredNodes.add(currentNodeSet1);
									//toBeDeleteed.add(currentNodeSet1);
									break;
								}
							}
						}
					}
				}
				if(acc && !containsAllbnState){
					for(NodeData currentNodeSet2 : currentTree){	
						if(currentNodeSet1!=currentNodeSet2&&(currentNodeSet2.coveredBy == null)&&(currentNodeSet1.aState == currentNodeSet2.aState)&&(currentNodeSet2.hash==(currentNodeSet1.hash&currentNodeSet2.hash)&&(currentNodeSet1.bStates.size() > currentNodeSet2.bStates.size()))){
							if(currentNodeSet1.bStates.containsAll(currentNodeSet2.bStates)){
								currentNodeSet1.ACCPotentialCoverBy.add(currentNodeSet2);
								if( potenialACCCandidate == null || potenialACCCandidate.bStates.size()>currentNodeSet2.bStates.size()){
									potenialACCCandidate = currentNodeSet2;
								}
							}
						}
					}
					if(currentNodeSet1.aState!=null&&currentNodeSet1.aState.coveredBy!=null){
						for(NodeData ACCANodes : currentNodeSet1.aState.ACCPotentialCoverBy){
							if(completeTree.containsKey(ACCANodes)){
								for(NodeData completeNodeSet:completeTree.get(ACCANodes)){
									if(completeNodeSet.hash==(currentNodeSet1.hash&completeNodeSet.hash)&&(currentNodeSet1.bStates.size() >= completeNodeSet.bStates.size())){
										if(currentNodeSet1.bStates.containsAll(completeNodeSet.bStates)){
											currentNodeSet1.ACCPotentialCoverBy.add(completeNodeSet);
											if(acc == true &&( potenialACCCandidate == null || potenialACCCandidate.bStates.size()>completeNodeSet.bStates.size())){
												potenialACCCandidate = completeNodeSet;
											}
										}
									}
								}
							}
							for(NodeData currentNodeSet2 : currentTree){	
								if(currentNodeSet1!=currentNodeSet2&&(currentNodeSet2.coveredBy == null)&&(ACCANodes == currentNodeSet2.aState)&&(currentNodeSet2.hash==(currentNodeSet1.hash&currentNodeSet2.hash)&&(currentNodeSet1.bStates.size() >= currentNodeSet2.bStates.size()))){
									if(currentNodeSet1.bStates.containsAll(currentNodeSet2.bStates)){
										currentNodeSet1.ACCPotentialCoverBy.add(currentNodeSet2);
										if( potenialACCCandidate == null || potenialACCCandidate.bStates.size()>currentNodeSet2.bStates.size()){
											potenialACCCandidate = currentNodeSet2;
										}
									}
								}
							}							
						}
					}
				}
				if(!containsAllbnState){
					if(potenialACCCandidate == null || acc == false){
						newNodeInCompleteTree = true;
						if(!completeTree.containsKey(currentNodeSet1.aState)){
							completeTree.put(currentNodeSet1.aState, new LinkedList<NodeData>());
						}
						completeTree.get(currentNodeSet1.aState).addFirst(currentNodeSet1);
						totalUniqueNodes++;
						if(currentNodeSet1.parentNode!=null){
							currentNodeSet1.parentNode.outgoingTransition.add(new Transition(currentNodeSet1.word.getSymbol(currentNodeSet1.word.getLength()-2),currentNodeSet1));
						}
						if(currentNodeSet1.aState!=null&&currentNodeSet1.aState.coveredBy!=null){
							stack2.add(currentNodeSet1);
							toBeDeleteed.add(currentNodeSet1);
						}
					}else{
						totalAACNodes++;
						currentNodeSet1.coveredBy = potenialACCCandidate;
						currentNodeSet1.identicalCover = false;
						potenialACCCandidate.covering.add(currentNodeSet1);
						if(currentNodeSet1.parentNode!=null){
							currentNodeSet1.parentNode.outgoingTransition.add(new Transition(currentNodeSet1.word.getSymbol(currentNodeSet1.word.getLength()-2),currentNodeSet1));
						}
						if(!ACCNodes.containsKey(currentNodeSet1.aState)){
							ACCNodes.put(currentNodeSet1.aState, new LinkedList<NodeData>());
						}
						ACCNodes.get(currentNodeSet1.aState).addFirst(currentNodeSet1);
						//toBeDeleteed.add(currentNodeSet1);
					}
				}
			}
			currentTree.removeAll(toBeDeleteed);
			return !newNodeInCompleteTree;
		}
		
		public void finishACCover(HashSet<NodeData> nodes) throws OperationCanceledException{
			if (!m_Services.getProgressMonitorService().continueProcessing()) {
                throw new OperationCanceledException(this.getClass());
			}
			HashSet<NodeData> nodesToBeFinishedFirst = new HashSet<NodeData>();
			currentTree.clear();
			for(NodeData key : nodes){
				ACCNodes.get(key.aState).remove(key);
				if(!completeTree.containsKey(key.aState)){
					completeTree.put(key.aState, new LinkedList<NodeData>());
				}
				completeTree.get(key.aState).add(key);
				totalAACNodes--;
				totalUniqueNodes++;
				key.coveredBy.covering.remove(key);
				key.coveredBy = null;
				if(key.aState!=null&&key.aState.coveredBy!=null){
					nodesToBeFinishedFirst.add(key.aState);
				}
			}
			currentTree.addAll(nodes);
			if(!nodesToBeFinishedFirst.isEmpty()&&prvPAutomata !=null){
				prvPAutomata.finishACCover(nodesToBeFinishedFirst);
			}
			do{
				if(prvPAutomata == null){
					currentTree = expand(true,false);
					if(cover(false)){
						deadendRemove();
						break;
					}
				}else{
					currentTree = expand(false,false);
					calculateAcceptingStates();
					if(cover(false)){
						deadendRemove();
						break;
					}
				}
			}while(true);
		}
		
		public void finishACCover2(NodeData parentNode,LinkedList<NodeData> nodes) throws OperationCanceledException{
			if (!m_Services.getProgressMonitorService().continueProcessing()) {
                throw new OperationCanceledException(this.getClass());
			}
			HashSet<NodeData> nodesToBeFinishedFirst = new HashSet<NodeData>();
			for(NodeData key : nodes){
				testing_newAcceptingState = false;
				currentTree.clear();
				currentTree.add(key);
				ACCNodes.get(key.aState).remove(key);
				if(!completeTree.containsKey(key.aState)){http://www.amazon.com/
					completeTree.put(key.aState, new LinkedList<NodeData>());
				}
				completeTree.get(key.aState).add(key);
				totalAACNodes--;
				totalUniqueNodes++;
				key.coveredBy.covering.remove(key);
				key.coveredBy = null;
				if(key.aState!=null&&key.aState.coveredBy!=null){
					nodesToBeFinishedFirst.add(key.aState);
					prvPAutomata.finishACCover(nodesToBeFinishedFirst);
				}
				do{
					if(prvPAutomata == null){
						currentTree = expand(true,false);
						if(cover(false)){
							//deadendRemove();
							break;
						}
					}else{
						currentTree = expand(false,false);
						calculateAcceptingStates();
						if(cover(false)){
							//deadendRemove();
							break;
						}
					}
				}while(true);
				if(testing_newAcceptingState == true){
					break;
				}
			}
		}
		
		//private HashSet<NodeData> toBeKeepedNodes;
		
		public void deadendRemove(){
			//toBeKeepedNodes = new HashSet<NodeData>();
			//HashSet<NodeData> toBeDeletedNodes = new HashSet<NodeData>(allNodes);
			int i=0;
			for(NodeData node :completeTree.keySet()){
				for(NodeData node2 : completeTree.get(node)){
					if(node2.keep){
						i++;
					}
				}
			}
			for(NodeData node :ACCNodes.keySet()){
				for(NodeData node2 : ACCNodes.get(node)){
					if(node2.keep){
						i++;
					}
				}
			}
			m_Logger.info("Nodes before: "+i);
			for(NodeData nodes : allNodes){
				nodes.keep = false;
				nodes.keep_Hard = false;
			}
			for(NodeData errorNode : errorNodes2){
				deadEndRemoveWalker(errorNode,true);
			}
			i=0;
			for(NodeData node :completeTree.keySet()){
				for(NodeData node2 : completeTree.get(node)){
					if(node2.keep){
						i++;
					}
				}
			}
			for(NodeData node :ACCNodes.keySet()){
				for(NodeData node2 : ACCNodes.get(node)){
					if(node2.keep){
						i++;
					}
				}
			}
			m_Logger.info("Nodes After: "+i);
/*			toBeDeletedNodes.removeAll(toBeKeepedNodes);
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
						nodeToBeDelete.parentNode.outgoingTransition.remove(removeTran);
					}
				}else{
					if(nodeToBeDelete.parentNode!=null){
						for(Transition tran: nodeToBeDelete.parentNode.outgoingTransition){
							if(tran.getSucc() == nodeToBeDelete){
								removeTran  = tran;
								break;
							}
						}
						nodeToBeDelete.parentNode.outgoingTransition.remove(removeTran);
					}
				}
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
			allNodes.removeAll(toBeDeletedNodes);*/
		}
		private void deadEndRemoveWalker(NodeData node,boolean hard){
			assert node !=null;
			if(!node.keep){
				node.keep_Hard = hard;
				node.keep = true;
				//toBeKeepedNodes.add(node);
				for(NodeData coveringNode : node.covering){
					if(coveringNode.identicalCover){
						deadEndRemoveWalker(coveringNode,hard);
					}else{
						deadEndRemoveWalker(coveringNode,false);
					}
				}
				if(node.parentNode != null){
					deadEndRemoveWalker(node.parentNode,hard);
				}
			}else{
				if(hard&&!node.keep_Hard){
					node.keep_Hard = hard;
					for(NodeData coveringNode : node.covering){
						if(coveringNode.identicalCover){
							deadEndRemoveWalker(coveringNode,hard);
						}
					}
					if(node.parentNode != null){
						deadEndRemoveWalker(node.parentNode,hard);
					}
				}
			}
		}
		
		public Set<LETTER> getAlphabet(){
			return letter;
		}
		
		public Set<NodeData> getInitialStates(){
			return initialNodes;
		}
		
		public LinkedList<Transition> internalSuccessors(NodeData node) throws OperationCanceledException{
			/*if(node.coveredBy!=null){
				HashSet<NodeData> n = new HashSet<NodeData>(); 
				n.add(node);
				finishACCover(n);
			}*/
			return node.outgoingTransition;
		}
		public LinkedList<Transition> internalSuccessors(NodeData node, LETTER let) throws OperationCanceledException{
			/*if(node.coveredBy!=null){
				HashSet<NodeData> n = new HashSet<NodeData>(); 
				n.add(node);
				finishACCover(n);
			}*/
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
		public boolean keep,keep_Hard;
		public int hash;
		public NodeData coveredBy = null;
		public HashSet<NodeData> ACCPotentialCoverBy;
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
			keep = true;
			keep_Hard = false;
			identicalCover = false;
			DeadEndsRemoved = false;
			bStates = new HashSet<STATE>();
			word = null;
			covering = new HashSet<NodeData>();
			ACCPotentialCoverBy = new HashSet<NodeData>();
			outgoingTransition = new LinkedList<Transition>();
			hash = 0;
			accepting = false;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IncrementalInclusionCheck2DeadEndRemovalAdvanceCover_2Stacks_multipleCounterExamplesAtOnce(IUltimateServiceProvider services, StateFactory<STATE> sf,
			INestedWordAutomatonSimple<LETTER, STATE> a, List<INestedWordAutomatonSimple<LETTER,STATE>> b,boolean acc) throws AutomataLibraryException{
		super(services,a);
		IncrementalInclusionCheck2DeadEndRemovalAdvanceCover_2Stacks_multipleCounterExamplesAtOnce.abortIfContainsCallOrReturn(a);
		counterExampleFlag = 0;
		m_acc = acc;
		localServiceProvider = services;
		localStateFactory = sf;
		m_Logger.info(startMessage());
		local_m_A =  a;
		local_m_B = new ArrayList<INestedWordAutomatonSimple<LETTER, STATE>>();
		local_m_B2 = new ArrayList<INestedWordAutomatonSimple<LETTER, STATE>>();
		union_m_Bs = new LinkedList<INestedWordAutomatonSimple<LETTER, STATE>>();
		current_m_B = null;
		prvPAutomaton = new LinkedList<PseudoAutomata>();
		workingAutomata = new PseudoAutomata(local_m_A);
		prvPAutomaton.add(workingAutomata);
		for(INestedWordAutomatonSimple<LETTER,STATE> bn : b){
			try {
				super.addSubtrahend(bn);
			} catch (AutomataLibraryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			local_m_B.add(bn);
			local_m_B2.add(bn);
			if(current_m_B == null){
				current_m_B = bn;
			}else{
				current_m_B = new NfaUnion(localServiceProvider,localStateFactory,current_m_B,bn).getResult();
			}
		}
		if(!getResult()){
			workingAutomata = new PseudoAutomata(workingAutomata,current_m_B);
			prvPAutomaton.add(workingAutomata);
		}
		union_m_Bs.add(current_m_B);
		current_m_B = null;
		m_Logger.info(exitMessage());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IncrementalInclusionCheck2DeadEndRemovalAdvanceCover_2Stacks_multipleCounterExamplesAtOnce(IUltimateServiceProvider services, StateFactory<STATE> sf,
			INestedWordAutomatonSimple<LETTER, STATE> a, List<INestedWordAutomatonSimple<LETTER,STATE>> b) throws AutomataLibraryException{
		super(services,a);
		IncrementalInclusionCheck2DeadEndRemovalAdvanceCover_2Stacks_multipleCounterExamplesAtOnce.abortIfContainsCallOrReturn(a);
		counterExampleFlag = 0;
		m_acc = true;
		localServiceProvider = services;
		localStateFactory = sf;
		m_Logger.info(startMessage());
		local_m_A =  a;
		local_m_B = new ArrayList<INestedWordAutomatonSimple<LETTER, STATE>>();
		local_m_B2 = new ArrayList<INestedWordAutomatonSimple<LETTER, STATE>>();
		union_m_Bs = new LinkedList<INestedWordAutomatonSimple<LETTER, STATE>>();
		current_m_B = null;
		prvPAutomaton = new LinkedList<PseudoAutomata>();
		workingAutomata = new PseudoAutomata(local_m_A);
		prvPAutomaton.add(workingAutomata);
		for(INestedWordAutomatonSimple<LETTER,STATE> bn : b){
			try {
				super.addSubtrahend(bn);
			} catch (AutomataLibraryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(current_m_B == null){
				current_m_B = bn;
			}else{
				current_m_B = new NfaUnion(localServiceProvider,localStateFactory,current_m_B,bn).getResult();
			}
			local_m_B.add(bn);
			local_m_B2.add(bn);
			
		}
		if(!getResult()&&current_m_B!=null){
			workingAutomata = new PseudoAutomata(workingAutomata,current_m_B);
			prvPAutomaton.add(workingAutomata);
			union_m_Bs.add(current_m_B);
			current_m_B = null;
		}
		m_Logger.info(exitMessage());
	}
	
	@Override
	public void addSubtrahend(INestedWordAutomatonSimple<LETTER, STATE> nwa) throws AutomataLibraryException {
		super.addSubtrahend(nwa);
		local_m_B.add(nwa);
		local_m_B2.add(nwa);
		if(current_m_B == null){
			current_m_B = nwa;
		}else{
			current_m_B = new NfaUnion(localServiceProvider,localStateFactory,current_m_B,nwa).getResult();
		}
		if(workingAutomata.errorNodes.peekFirst()==null || counterExampleFlag==2){
			m_Logger.info(startMessage());
			counterExampleFlag = 0;
			workingAutomata = new PseudoAutomata(workingAutomata,current_m_B);
			prvPAutomaton.add(workingAutomata);
			union_m_Bs.add(current_m_B);
			current_m_B = null;
			m_Logger.info(exitMessage());
		}
	}
	
	public NestedRun<LETTER,STATE> getCounterexample(){
		if(workingAutomata.errorNodes.peekFirst()!=null){
			if(counterExampleFlag==0){
				counterExampleFlag++;
				return workingAutomata.errorNodes.poll().word;
			}else{
				counterExampleFlag++;
				return workingAutomata.errorNodes.pollLast().word;
			}
		}else{
			return null;
		}
		
	}
	@Override
	public String operationName() {
		return "IncrementalInclusionCheck2DeadEndRemovalAdvanceCover_2Stacks_multipleCounterExamplesAtOnce";
	}
	@Override
	public String startMessage() {
		return "Start " + operationName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String exitMessage() {
		/*int i = 0, j = 0;
		for(INestedWordAutomatonSimple<LETTER, STATE> automata:local_m_B){
			try {
				if(!(new IsDeterministic<LETTER, STATE>(localServiceProvider,automata)).getResult()){
					i++;
				}
			} catch (AutomataLibraryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		if(!getResult()){
			m_Logger.info("counterExample: "+workingAutomata.errorNodes.peekFirst().word.getWord().toString());
		}
		m_Logger.info("Total: "+ totalNodes+" node(s)");
		m_Logger.info("Total ACC: "+ totalAACNodes+" node(s)");
		m_Logger.info("Total IC: "+ totalCoveredNodes+" node(s)");
		m_Logger.info("Total Unique: "+ totalUniqueNodes+" node(s)");
		m_Logger.info("Total Iterations: "+ prvPAutomaton.size()+" iterations");
		m_Logger.info("Total B Automaton: "+ local_m_B2.size()+" ");
/*		m_Logger.info("Total B Automaton: "+local_m_B.size());
		m_Logger.info("Total non-Deterministic B Automaton: "+i);
		i = 0;		
		HashSet<STATE> visitedStates,currentStates,nextStates;
		for(INestedWordAutomatonSimple<LETTER, STATE> automata:local_m_B){
			try {
				if(!(new IsDeterministic<LETTER, STATE>(localServiceProvider,automata)).getResult()){
					visitedStates = new HashSet<STATE>();
					currentStates = new HashSet<STATE>();
					nextStates = new HashSet<STATE>();
					nextStates.addAll((Collection<? extends STATE>) automata.getInitialStates());
					do{
						currentStates.clear();
						currentStates.addAll(nextStates);
						nextStates.clear();
						for(STATE state:currentStates){
							if(!visitedStates.contains(state)){
								i++;
								visitedStates.add(state);
								if(!stateDeterministicCheck(automata,state)){
									j++;
								}
								for(OutgoingInternalTransition<LETTER, STATE> tran : automata.internalSuccessors(state)){
									nextStates.add(tran.getSucc());
								}
							}
						}
					}while(!nextStates.isEmpty());
				}
			} catch (AutomataLibraryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		if(i!=0){
			m_Logger.info("Total States: "+i);
			m_Logger.info("Total non-Deterministic States:"+j);
			m_Logger.info("non-Determinism: "+((j*1.0)/(i*1.0)));
		}*/
		return "Exit " + operationName();
	}
	
	private boolean stateDeterministicCheck(INestedWordAutomatonSimple<LETTER, STATE> automata, STATE state){
		HashSet<LETTER> visitedLetters = new HashSet<LETTER>();
		for(OutgoingInternalTransition<LETTER, STATE> tran:automata.internalSuccessors(state)){
			if(visitedLetters.contains(tran.getLetter())){
				return false;
			}else{
				visitedLetters.add(tran.getLetter());
			}
		}
		return true;
	}
	
	public Boolean getResult(){
		if(workingAutomata.errorNodes.peekFirst()==null){
			return true;
		}else{
			return false;
		}
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