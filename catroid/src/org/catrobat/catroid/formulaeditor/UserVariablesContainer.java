/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.formulaeditor;

import android.content.Context;
import android.util.SparseArray;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.ui.adapter.UserVariableAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserVariablesContainer implements Serializable {
	private static final long serialVersionUID = 1L;

	@XStreamAlias("programVariableList")
	private List<UserVariable> projectVariables;
	@XStreamAlias("objectVariableList")
	private Map<Sprite, List<UserVariable>> spriteVariables;
	@XStreamAlias("userBrickVariableList")
	private SparseArray<List<UserVariable>> userBrickVariables;

	private int nextUserBrickId = 0;
	private int currentUserBrickId = 0;

	public UserVariablesContainer() {
		projectVariables = new ArrayList<UserVariable>();
		spriteVariables = new HashMap<Sprite, List<UserVariable>>();
		userBrickVariables = new SparseArray<List<UserVariable>>();
	}

	public UserVariableAdapter createUserVariableAdapter(Context context, int userBrickId, Sprite sprite) {
		List<UserVariable> userBrickVariables = null;
		if (userBrickId == -1) {
			userBrickVariables = new LinkedList<UserVariable>();
		} else {
			userBrickVariables = getOrCreateVariableListForUserBrick(userBrickId);
		}
		List<UserVariable> spriteVariables = getOrCreateVariableListForSprite(sprite);
		return new UserVariableAdapter(context, userBrickVariables, spriteVariables, projectVariables);
	}

	public UserVariable getUserVariable(String userVariableName, Sprite sprite) {
		UserVariable var;
		var = findUserVariable(userVariableName, getOrCreateVariableListForSprite(sprite));
		if (var == null) {
			var = findUserVariable(userVariableName, projectVariables);
		}
		return var;
	}

	public List<UserVariable> getProjectVariables() {
		return projectVariables;
	}

	public void setCurrentUserBrickBeingEvaluated(int userBrickId) {
		currentUserBrickId = userBrickId;
	}

	public int getCurrentUserBrickBeingEvaluated() {
		return currentUserBrickId;
	}

	public UserVariable addUserBrickUserVariableToUserBrick(int userBrickId, String userVariableName) {
		List<UserVariable> varList = getOrCreateVariableListForUserBrick(userBrickId);
		UserVariable userVariableToAdd = new UserVariable(userVariableName, varList);
		userVariableToAdd.setValue(0);
		varList.add(userVariableToAdd);
		return userVariableToAdd;
	}

	public UserVariable addSpriteUserVariable(String userVariableName) {
		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();
		return addSpriteUserVariableToSprite(currentSprite, userVariableName);
	}

	public UserVariable addSpriteUserVariableToSprite(Sprite sprite, String userVariableName) {
		List<UserVariable> varList = getOrCreateVariableListForSprite(sprite);
		UserVariable userVariableToAdd = new UserVariable(userVariableName, varList);
		varList.add(userVariableToAdd);
		return userVariableToAdd;
	}

	public UserVariable addProjectUserVariable(String userVariableName) {
		UserVariable userVariableToAdd = new UserVariable(userVariableName, projectVariables);
		projectVariables.add(userVariableToAdd);
		return userVariableToAdd;
	}

	/**
	 * This function deletes the user variable with userVariableName in the current context.
	 * 
	 * The current context consists of all global variables, the sprite variables for the current sprite,
	 * and the user brick variables for the current user brick.
	 */
	public void deleteUserVariableByName(String userVariableName) {
		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();
		UserBrick currentUserBrick = ProjectManager.getInstance().getCurrentUserBrick();
		int userBrickId = -1;
		if (currentUserBrick != null) {
			userBrickId = currentUserBrick.getDefinitionBrick().getUserBrickId();
		}
		UserVariable variableToDelete = getUserVariable(userVariableName, userBrickId, currentSprite);
		if (variableToDelete != null) {
			List<UserVariable> context = variableToDelete.getContext();
			context.remove(variableToDelete);
		}
	}

	public void deleteUserVariableFromUserBrick(int userBrickId, String userVariableName) {
		List<UserVariable> context = userBrickVariables.get(userBrickId);
		UserVariable variableToDelete = findUserVariable(userVariableName, context);
		if (variableToDelete != null) {
			context.remove(variableToDelete);
		}

	}

	public List<UserVariable> getOrCreateVariableListForUserBrick(int userBrickId) {
		List<UserVariable> variables = userBrickVariables.get(userBrickId);
		if (variables == null) {
			variables = new ArrayList<UserVariable>();
			userBrickVariables.put(userBrickId, variables);
		}
		return variables;
	}

	public void cleanVariableListForUserBrick(int userBrickId) {
		List<UserVariable> vars = userBrickVariables.get(userBrickId);
		if (vars != null) {
			vars.clear();
		}
		userBrickVariables.remove(userBrickId);
	}

	public List<UserVariable> getOrCreateVariableListForSprite(Sprite sprite) {
		List<UserVariable> variables = spriteVariables.get(sprite);
		if (variables == null) {
			variables = new ArrayList<UserVariable>();
			spriteVariables.put(sprite, variables);
		}
		return variables;
	}

	public List<UserVariable> createVariableListForCopySprite(Sprite sprite) {
		return spriteVariables.get(sprite);
	}

	public void cleanVariableListForSprite(Sprite sprite) {
		List<UserVariable> vars = spriteVariables.get(sprite);
		if (vars != null) {
			vars.clear();
		}
		spriteVariables.remove(sprite);
	}

	public int getAndIncrementUserBrickId() {
		return nextUserBrickId++;
	}

	/**
	 * This function finds the user variable with userVariableName in the current context.
	 * 
	 * The current context consists of all global variables, the sprite variables for the current sprite,
	 * and the user brick variables for the current user brick.
	 */
	public UserVariable getUserVariable(String name, int userBrickId, Sprite currentSprite) {

		UserVariable variableToReturn;
		List<UserVariable> spriteVariables = getOrCreateVariableListForSprite(currentSprite);
		variableToReturn = findUserVariable(name, spriteVariables);
		if (variableToReturn != null) {
			return variableToReturn;
		}

		if (userBrickId != -1) {
			List<UserVariable> userBrickVariables = getOrCreateVariableListForUserBrick(userBrickId);
			variableToReturn = findUserVariable(name, userBrickVariables);
			if (variableToReturn != null) {
				return variableToReturn;
			}
		}

		variableToReturn = findUserVariable(name, projectVariables);
		if (variableToReturn != null) {
			return variableToReturn;
		}
		return null;
	}

	public UserVariable findUserVariable(String name, List<UserVariable> variables) {
		if (variables == null) {
			return null;
		}
		for (UserVariable variable : variables) {
			if (variable.getName().equals(name)) {
				return variable;
			}
		}
		return null;
	}

	public void resetAllUserVariables() {

		resetUserVariables(projectVariables);

		Iterator<Sprite> spriteIterator = spriteVariables.keySet().iterator();
		while (spriteIterator.hasNext()) {
			Sprite currentSprite = spriteIterator.next();
			resetUserVariables(spriteVariables.get(currentSprite));
		}
		for (int i = 0; i < userBrickVariables.size(); i++) {
			int key = userBrickVariables.keyAt(i);
			resetUserVariables(userBrickVariables.get(key));
		}
	}

	private void resetUserVariables(List<UserVariable> userVariableList) {
		for (UserVariable userVariable : userVariableList) {
			userVariable.setValue(0);
		}
	}
}
