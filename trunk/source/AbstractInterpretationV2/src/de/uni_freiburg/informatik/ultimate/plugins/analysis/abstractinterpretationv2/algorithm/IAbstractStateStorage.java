package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm;

import java.util.Collection;

import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.IAbstractState;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 *
 * @param <ACTION>
 * @param <VARDECL>
 */
public interface IAbstractStateStorage<ACTION, VARDECL> {

	Collection<IAbstractState<ACTION, VARDECL>> getAbstractPreStates(ACTION transition);

	Collection<IAbstractState<ACTION, VARDECL>> getAbstractPostStates(ACTION transition);

	IAbstractState<ACTION, VARDECL> getCurrentAbstractPreState(ACTION transition);

	IAbstractState<ACTION, VARDECL> getCurrentAbstractPostState(ACTION transition);

	void addAbstractPreState(ACTION transition, IAbstractState<ACTION, VARDECL> state);

	void addAbstractPostState(ACTION transition, IAbstractState<ACTION, VARDECL> state);
	
	IAbstractState<ACTION, VARDECL> setPostStateIsFixpoint(ACTION transition, IAbstractState<ACTION, VARDECL> state,
			boolean value);

	IAbstractState<ACTION, VARDECL> mergePostStates(ACTION transition);
	
	IAbstractStateStorage<ACTION, VARDECL> createStorage();

}