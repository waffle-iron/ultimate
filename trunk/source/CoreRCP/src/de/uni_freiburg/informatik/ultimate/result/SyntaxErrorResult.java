package de.uni_freiburg.informatik.ultimate.result;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.model.IElement;
import de.uni_freiburg.informatik.ultimate.model.ITranslator;
import de.uni_freiburg.informatik.ultimate.model.location.ILocation;

/**
 * Class to describe one of the following results
 * <ul>
 * <li> we have detected a syntax error 
 * <li> we have detected a type error (e.g. an int value was assigned to a
 *  Boolean variable)
 * <li> we have detected syntax which is not (yet) supported by out tool or not
 * supported in a specific setting (e.g. input is program that uses arrays but
 * solver setting uses logic that does not support arrays) 
 * </ul>
 * @author heizmann@informatik.uni-freiburg.de
 *
 */
public class SyntaxErrorResult extends AbstractResult implements IResultWithLocation {
	
	private String m_LongDescription;
	private final ILocation m_Location;

	/**
	 * @param location
	 * @param syntaxErrorType
	 */
	@Deprecated
	public SyntaxErrorResult(String plugin, 
			ILocation location) {
		super(plugin);
		m_Location = location;
	}
	
	public SyntaxErrorResult(String plugin, 
			ILocation location,
			String longDescription) {
		super(plugin);
		m_Location = location;
		m_LongDescription = longDescription;
	}

	@Override
	public String getShortDescription() {
		return "Incorrect Syntax";
	}

	@Override
	public String getLongDescription() {
		return m_LongDescription;
	}

	@Deprecated
	public void setLongDescription(String longDescription) {
		m_LongDescription = longDescription;
	}
	
	@Override
	public ILocation getLocation() {
		return m_Location;
	}

	
}