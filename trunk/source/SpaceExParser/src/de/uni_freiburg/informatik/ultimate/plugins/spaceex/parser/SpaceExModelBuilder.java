package de.uni_freiburg.informatik.ultimate.plugins.spaceex.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.plugins.spaceex.ast.SpaceExRootNode;
import de.uni_freiburg.informatik.ultimate.plugins.spaceex.parser.generated.ComponentType;
import de.uni_freiburg.informatik.ultimate.plugins.spaceex.parser.generated.Sspaceex;

/**
 * Constructs SpaceEx Ultimate model representation.
 * @author greitsch@informatik.uni-freiburg.de
 *
 */
public class SpaceExModelBuilder {

	private Map<String, ComponentType> mComponents;
	private Map<String, ComponentType> mSystems;
	
	private SpaceExRootNode mModel;
	
	public SpaceExModelBuilder(Sspaceex root) {
		mComponents = new HashMap<String, ComponentType>();
		mSystems = new HashMap<String, ComponentType>();
	
		mModel = new SpaceExRootNode(root.getMath(), root.getVersion());
		
		parseComponents(root.getComponent());
	}
	
	public SpaceExRootNode getModel() {
		return mModel;
	}

	private void parseComponents(List<ComponentType> components) {
		for (ComponentType c : components) {
			if (c.getBind().size() == 0) {
				addComponent(c);
			}
			else {
				addSystem(c);
			}
		}
	}
	
	private void addComponent(ComponentType component) {
		mModel.addComponent(component);

	}
	
	private void addSystem(ComponentType system) {
		mModel.addSystem(system);
		//mSystems.put(system.getId(), system);
	}
}