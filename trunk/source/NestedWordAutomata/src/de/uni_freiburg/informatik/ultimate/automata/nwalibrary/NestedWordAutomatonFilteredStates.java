package de.uni_freiburg.informatik.ultimate.automata.nwalibrary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.AtsDefinitionPrinter;
import de.uni_freiburg.informatik.ultimate.automata.IRun;
import de.uni_freiburg.informatik.ultimate.automata.OperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.Word;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.reachableStatesAutomaton.NestedWordAutomatonReachableStates;

public class NestedWordAutomatonFilteredStates<LETTER, STATE> implements
		INestedWordAutomatonOldApi<LETTER, STATE>, INestedWordAutomaton<LETTER, STATE>, IDoubleDeckerAutomaton<LETTER, STATE> {
	INestedWordAutomatonOldApi<LETTER, STATE> m_Nwa;
	Predicate<STATE> m_RemainingStates;
	Predicate<STATE> m_RemainingInitials;
	private HashSet<STATE> m_Initials;
	private HashSet<STATE> m_States;
	
	NestedWordAutomatonFilteredStates(INestedWordAutomatonOldApi<LETTER, STATE> automaton, 
			Predicate<STATE> remainingStates, Predicate<STATE> remainingInitials) {
		m_Nwa = automaton;
		m_RemainingStates = remainingStates;
		m_RemainingInitials = remainingInitials;
	}
	
	public NestedWordAutomatonFilteredStates(NestedWordAutomatonReachableStates<LETTER, STATE> automaton) {
		m_Nwa = automaton;
		m_RemainingStates = new NoDeadEnd(automaton);
		m_RemainingInitials = new InitialAfterDeadEndRemoval(automaton);
//		m_RemainingStates = new TruePredicate();
//		m_RemainingInitials = new TruePredicate();

	}
	
	public Set<STATE> getDownStates(STATE up) {
		if (m_Nwa instanceof NestedWordAutomatonReachableStates) {
			NestedWordAutomatonReachableStates<LETTER, STATE> rsa = (NestedWordAutomatonReachableStates<LETTER, STATE>) m_Nwa;
			return rsa.getDownStatesAfterDeadEndRemoval(up);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean accepts(Word<LETTER> word) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return getStates().size();
	}

	@Override
	public Set<LETTER> getAlphabet() {
		return m_Nwa.getAlphabet();
	}

	@Override
	public String sizeInformation() {
		return "sizeInformation of NestedWordAutomatonFilteredStates";
	}

	@Override
	public Set<LETTER> getInternalAlphabet() {
		return m_Nwa.getInternalAlphabet();
	}

	@Override
	public Set<LETTER> getCallAlphabet() {
		return m_Nwa.getCallAlphabet();
	}

	@Override
	public Set<LETTER> getReturnAlphabet() {
		return m_Nwa.getReturnAlphabet();
	}

	@Override
	public StateFactory<STATE> getStateFactory() {
		return m_Nwa.getStateFactory();
	}

	@Override
	public Collection<STATE> getStates() {
		m_States = new HashSet<STATE>();
		for (STATE state : m_Nwa.getStates()) {
			if(m_RemainingStates.evaluate(state)) {
				m_States.add(state);
			}
		}
		return m_States;
	}

	@Override
	public Collection<STATE> getInitialStates() {
		m_Initials = new HashSet<STATE>();
		for (STATE state : m_Nwa.getInitialStates()) {
			if(m_RemainingInitials.evaluate(state)) {
				m_Initials.add(state);
			}
		}
		return m_Initials;
	}

	@Override
	public Collection<STATE> getFinalStates() {
		//TODO only correct when removing dead ends
		return m_Nwa.getFinalStates();
	}

	@Override
	public boolean isInitial(STATE state) {
		return m_RemainingInitials.evaluate(state);
	}

	@Override
	public boolean isFinal(STATE state) {
		return m_Nwa.isFinal(state);
	}

	@Override
	public STATE getEmptyStackState() {
		return m_Nwa.getEmptyStackState();
	}

	@Override
	public Set<LETTER> lettersInternal(STATE state) {
		Set<LETTER> letters = new HashSet<LETTER>();
		for (OutgoingInternalTransition<LETTER, STATE> outTrans : internalSuccessors(state)) {
			letters.add(outTrans.getLetter());
		}
		return letters;
	}

	@Override
	public Set<LETTER> lettersCall(STATE state) {
		Set<LETTER> letters = new HashSet<LETTER>();
		for (OutgoingCallTransition<LETTER, STATE> outTrans : callSuccessors(state)) {
			letters.add(outTrans.getLetter());
		}
		return letters;
	}

	@Override
	public Set<LETTER> lettersReturn(STATE state) {
		Set<LETTER> letters = new HashSet<LETTER>();
		for (OutgoingReturnTransition<LETTER, STATE> outTrans : returnSuccessors(state)) {
			letters.add(outTrans.getLetter());
		}
		return letters;
	}

	@Override
	public Set<LETTER> lettersInternalIncoming(STATE state) {
		Set<LETTER> letters = new HashSet<LETTER>();
		for (IncomingInternalTransition<LETTER, STATE> outTrans : internalPredecessors(state)) {
			letters.add(outTrans.getLetter());
		}
		return letters;
	}

	@Override
	public Set<LETTER> lettersCallIncoming(STATE state) {
		Set<LETTER> letters = new HashSet<LETTER>();
		for (IncomingCallTransition<LETTER, STATE> outTrans : callPredecessors(state)) {
			letters.add(outTrans.getLetter());
		}
		return letters;
	}

	@Override
	public Set<LETTER> lettersReturnIncoming(STATE state) {
		Set<LETTER> letters = new HashSet<LETTER>();
		for (IncomingReturnTransition<LETTER, STATE> outTrans : returnPredecessors(state)) {
			letters.add(outTrans.getLetter());
		}
		return letters;
	}

	@Override
	public Set<LETTER> lettersReturnSummary(STATE state) {
		Set<LETTER> letters = new HashSet<LETTER>();
		for (SummaryReturnTransition<LETTER, STATE> outTrans : returnSummarySuccessor(state)) {
			letters.add(outTrans.getLetter());
		}
		return letters;
	}

	@Override
	public Iterable<STATE> succInternal(final STATE state, final LETTER letter) {
		return new FilteredIterable<STATE>(m_Nwa.succInternal(state, letter), m_RemainingStates);
	}

	@Override
	public Iterable<STATE> succCall(STATE state, LETTER letter) {
		return new FilteredIterable<STATE>(m_Nwa.succCall(state, letter), m_RemainingStates);
	}

	@Override
	public Iterable<STATE> hierPred(STATE state, LETTER letter) {
		return new FilteredIterable<STATE>(m_Nwa.hierPred(state, letter), m_RemainingStates);
	}

	@Override
	public Iterable<STATE> succReturn(STATE state, STATE hier, LETTER letter) {
		return new FilteredIterable<STATE>(m_Nwa.succReturn(state, hier, letter), m_RemainingStates);
	}

	@Override
	public Iterable<STATE> predInternal(STATE state, LETTER letter) {
		return new FilteredIterable<STATE>(m_Nwa.predInternal(state, letter), m_RemainingStates);
	}

	@Override
	public Iterable<STATE> predCall(STATE state, LETTER letter) {
		return new FilteredIterable<STATE>(m_Nwa.predCall(state, letter), m_RemainingStates);
	}

	@Override
	public Iterable<STATE> predReturnLin(STATE state, LETTER letter, STATE hier) {
		return new FilteredIterable<STATE>(m_Nwa.predReturnLin(state, letter, hier), m_RemainingStates);
	}

	@Override
	public Iterable<STATE> predReturnHier(STATE state, LETTER letter) {
		return new FilteredIterable<STATE>(m_Nwa.predReturnHier(state, letter), m_RemainingStates);
	}

	@Override
	public boolean finalIsTrap() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDeterministic() {
		return false;
	}

	@Override
	public boolean isTotal() {
		throw new UnsupportedOperationException();
	}



	@Override
	public Iterable<IncomingInternalTransition<LETTER, STATE>> internalPredecessors(LETTER letter, STATE succ) {
		Predicate<IncomingInternalTransition<LETTER, STATE>> predicate = new Predicate<IncomingInternalTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(IncomingInternalTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getPred());
			}
		};
		return new FilteredIterable<IncomingInternalTransition<LETTER, STATE>>(m_Nwa.internalPredecessors(letter,succ), predicate);
	}

	@Override
	public Iterable<IncomingInternalTransition<LETTER, STATE>> internalPredecessors(STATE succ) {
		Predicate<IncomingInternalTransition<LETTER, STATE>> predicate = new Predicate<IncomingInternalTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(IncomingInternalTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getPred());
			}
		};
		return new FilteredIterable<IncomingInternalTransition<LETTER, STATE>>(m_Nwa.internalPredecessors(succ), predicate);
	}

	@Override
	public Iterable<IncomingCallTransition<LETTER, STATE>> callPredecessors(LETTER letter, STATE succ) {
		Predicate<IncomingCallTransition<LETTER, STATE>> predicate = new Predicate<IncomingCallTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(IncomingCallTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getPred());
			}
		};
		return new FilteredIterable<IncomingCallTransition<LETTER, STATE>>(m_Nwa.callPredecessors(letter,succ), predicate);
	}

	@Override
	public Iterable<IncomingCallTransition<LETTER, STATE>> callPredecessors(STATE succ) {
		Predicate<IncomingCallTransition<LETTER, STATE>> predicate = new Predicate<IncomingCallTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(IncomingCallTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getPred());
			}
		};
		return new FilteredIterable<IncomingCallTransition<LETTER, STATE>>(m_Nwa.callPredecessors(succ), predicate);
	}

	@Override
	public Iterable<OutgoingInternalTransition<LETTER, STATE>> internalSuccessors(STATE state, LETTER letter) {
		Predicate<OutgoingInternalTransition<LETTER, STATE>> predicate = new Predicate<OutgoingInternalTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(OutgoingInternalTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getSucc());
			}
		};
		return new FilteredIterable<OutgoingInternalTransition<LETTER, STATE>>(m_Nwa.internalSuccessors(state,letter), predicate);
	}

	@Override
	public Iterable<OutgoingInternalTransition<LETTER, STATE>> internalSuccessors(STATE state) {
		Predicate<OutgoingInternalTransition<LETTER, STATE>> predicate = new Predicate<OutgoingInternalTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(OutgoingInternalTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getSucc());
			}
		};
		return new FilteredIterable<OutgoingInternalTransition<LETTER, STATE>>(m_Nwa.internalSuccessors(state), predicate);
	}

	@Override
	public Iterable<OutgoingCallTransition<LETTER, STATE>> callSuccessors(STATE state, LETTER letter) {
		Predicate<OutgoingCallTransition<LETTER, STATE>> predicate = new Predicate<OutgoingCallTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(OutgoingCallTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getSucc());
			}
		};
		return new FilteredIterable<OutgoingCallTransition<LETTER, STATE>>(m_Nwa.callSuccessors(state,letter), predicate);
	}

	@Override
	public Iterable<OutgoingCallTransition<LETTER, STATE>> callSuccessors(STATE state) {
		Predicate<OutgoingCallTransition<LETTER, STATE>> predicate = new Predicate<OutgoingCallTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(OutgoingCallTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getSucc());
			}
		};
		return new FilteredIterable<OutgoingCallTransition<LETTER, STATE>>(m_Nwa.callSuccessors(state), predicate);
	}

	@Override
	public Iterable<IncomingReturnTransition<LETTER, STATE>> returnPredecessors(STATE hier, LETTER letter, STATE succ) {
		Predicate<IncomingReturnTransition<LETTER, STATE>> predicate = new Predicate<IncomingReturnTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(IncomingReturnTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getLinPred());
			}
		};
		return new FilteredIterable<IncomingReturnTransition<LETTER, STATE>>(m_Nwa.returnPredecessors(hier, letter, succ), predicate);
	}

	@Override
	public Iterable<IncomingReturnTransition<LETTER, STATE>> returnPredecessors(LETTER letter, STATE succ) {
		Predicate<IncomingReturnTransition<LETTER, STATE>> predicate = new Predicate<IncomingReturnTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(IncomingReturnTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getLinPred()) && m_RemainingStates.evaluate(trans.getHierPred());
			}
		};
		return new FilteredIterable<IncomingReturnTransition<LETTER, STATE>>(m_Nwa.returnPredecessors(letter, succ), predicate);
	}

	@Override
	public Iterable<IncomingReturnTransition<LETTER, STATE>> returnPredecessors(STATE succ) {
		Predicate<IncomingReturnTransition<LETTER, STATE>> predicate = new Predicate<IncomingReturnTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(IncomingReturnTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getLinPred()) && m_RemainingStates.evaluate(trans.getHierPred());
			}
		};
		return new FilteredIterable<IncomingReturnTransition<LETTER, STATE>>(m_Nwa.returnPredecessors(succ), predicate);
	}

	@Override
	public Iterable<OutgoingReturnTransition<LETTER, STATE>> returnSucccessors(STATE state, STATE hier, LETTER letter) {
		Predicate<OutgoingReturnTransition<LETTER, STATE>> predicate = new Predicate<OutgoingReturnTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(OutgoingReturnTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getSucc());
			}
		};
		return new FilteredIterable<OutgoingReturnTransition<LETTER, STATE>>(m_Nwa.returnSucccessors(state,hier,letter), predicate);
	}

	@Override
	public Iterable<OutgoingReturnTransition<LETTER, STATE>> returnSuccessors(STATE state, LETTER letter) {
		Predicate<OutgoingReturnTransition<LETTER, STATE>> predicate = new Predicate<OutgoingReturnTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(OutgoingReturnTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getHierPred()) && m_RemainingStates.evaluate(trans.getSucc());
			}
		};
		return new FilteredIterable<OutgoingReturnTransition<LETTER, STATE>>(m_Nwa.returnSuccessors(state, letter), predicate);
	}

	@Override
	public Iterable<OutgoingReturnTransition<LETTER, STATE>> returnSuccessors(STATE state) {
		Predicate<OutgoingReturnTransition<LETTER, STATE>> predicate = new Predicate<OutgoingReturnTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(OutgoingReturnTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getHierPred()) && m_RemainingStates.evaluate(trans.getSucc());
			}
		};
		return new FilteredIterable<OutgoingReturnTransition<LETTER, STATE>>(m_Nwa.returnSuccessors(state), predicate);
	}

	@Override
	public Iterable<OutgoingReturnTransition<LETTER, STATE>> returnSuccessorsGivenHier(STATE state, STATE hier) {
		Predicate<OutgoingReturnTransition<LETTER, STATE>> predicate = new Predicate<OutgoingReturnTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(OutgoingReturnTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getSucc());
			}
		};
		return new FilteredIterable<OutgoingReturnTransition<LETTER, STATE>>(m_Nwa.returnSuccessorsGivenHier(state,hier), predicate);
	}
	
	@Override
	public Iterable<SummaryReturnTransition<LETTER, STATE>> returnSummarySuccessor(LETTER letter, STATE hier) {
		Predicate<SummaryReturnTransition<LETTER, STATE>> predicate = new Predicate<SummaryReturnTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(SummaryReturnTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getSucc()) && m_RemainingStates.evaluate(trans.getLinPred());
			}
		};
		return new FilteredIterable<SummaryReturnTransition<LETTER, STATE>>(m_Nwa.returnSummarySuccessor(letter, hier), predicate);
	}
	
	@Override
	public Iterable<SummaryReturnTransition<LETTER, STATE>> returnSummarySuccessor(STATE hier) {
		Predicate<SummaryReturnTransition<LETTER, STATE>> predicate = new Predicate<SummaryReturnTransition<LETTER,STATE>>() {
			@Override
			public boolean evaluate(SummaryReturnTransition<LETTER, STATE> trans) {
				return m_RemainingStates.evaluate(trans.getSucc()) && m_RemainingStates.evaluate(trans.getLinPred());
			}
		};
		return new FilteredIterable<SummaryReturnTransition<LETTER, STATE>>(m_Nwa.returnSummarySuccessor(hier), predicate);
	}
	
	@Override
	public String toString() {
		return (new AtsDefinitionPrinter<String,String>("nwa", this)).getDefinitionAsString();
	}
	
	
	
	public interface Predicate<T> {
		public boolean evaluate(T elem);
	}
	
	public class TruePredicate implements Predicate<STATE> {
		@Override
		public boolean evaluate(STATE state) {
			return true;
		}
	}
	
	public class NoDeadEnd implements Predicate<STATE> {
		final NestedWordAutomatonReachableStates<LETTER, STATE> m_Nwa;
		NoDeadEnd(NestedWordAutomatonReachableStates<LETTER, STATE> nwa) {
			m_Nwa = nwa;
		}
		@Override
		public boolean evaluate(STATE state) {
			return !m_Nwa.isDeadEnd(state);
		}
	}
	
	public class InitialAfterDeadEndRemoval implements Predicate<STATE> {
		final NestedWordAutomatonReachableStates<LETTER, STATE> m_Nwa;
		InitialAfterDeadEndRemoval(NestedWordAutomatonReachableStates<LETTER, STATE> nwa) {
			m_Nwa = nwa;
		}
		@Override
		public boolean evaluate(STATE state) {
			return m_Nwa.isInitialAfterDeadEndRemoval(state);
		}
	}


	
	
	
	public class FilteredIterable<T> implements Iterable<T> {
		final Iterable<T> m_Iterable;
		final Predicate<T> m_Predicate;
		
		public FilteredIterable(Iterable<T> iterable, Predicate<T> predicate) {
			m_Iterable = iterable;
			m_Predicate = predicate;
		}
		
		@Override
		public Iterator<T> iterator() {
			return new Iterator<T>() {
				final Iterator<T> m_Iterator;
				T m_next = null;
				{
					m_Iterator = m_Iterable.iterator();
					if (m_Iterator.hasNext()) {
						getNextThatSatisfiesPredicate();
					}
				}
				private void getNextThatSatisfiesPredicate() {
					if (m_Iterator.hasNext()) {
						m_next = m_Iterator.next();
						while (m_next != null && !m_Predicate.evaluate(m_next)) {
							if (m_Iterator.hasNext()) {
								m_next = m_Iterator.next();
							} else {
								m_next = null;
							}
						}
					} else {
						m_next = null;
					}
				}

				@Override
				public boolean hasNext() {
					return m_next != null;
				}

				@Override
				public T next() {
					T result = m_next;
					getNextThatSatisfiesPredicate();
					return result;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

			};
		}

	}





	@Override
	public boolean isDoubleDecker(STATE up, STATE down) {
		return ((NestedWordAutomatonReachableStates<LETTER, STATE>) m_Nwa).isDownStateAfterDeadEndRemoval(up, down);
	}
}
