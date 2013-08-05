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
package org.catrobat.catroid.test.content.bricks;

import java.util.ArrayList;
import java.util.List;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.UserScript;
import org.catrobat.catroid.content.bricks.ChangeXByNBrick;
import org.catrobat.catroid.content.bricks.LegoNxtMotorStopBrick;
import org.catrobat.catroid.content.bricks.LegoNxtMotorStopBrick.Motor;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.content.bricks.UserBrickUIComponent;
import org.catrobat.catroid.content.bricks.UserBrickUIDataArray;
import org.catrobat.catroid.content.bricks.UserScriptDefinitionBrick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.FormulaElement;
import org.catrobat.catroid.formulaeditor.FormulaElement.ElementType;
import org.catrobat.catroid.test.utils.Reflection;
import org.catrobat.catroid.test.utils.Reflection.ParameterList;

import android.test.AndroidTestCase;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class UserBrickTest extends AndroidTestCase {
	private Sprite sprite;
	private Project project;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sprite = new Sprite("testSprite");
		Reflection.invokeMethod(sprite, "init");

		project = new Project(null, "testProject");

		project.addSprite(sprite);
		ProjectManager.getInstance().setProject(project);
		ProjectManager.getInstance().setCurrentSprite(sprite);
	}

	public void testSpriteInit() {

		ArrayList<Script> array = (ArrayList<Script>) Reflection.getPrivateField(sprite, "userBricks");

		assertTrue("the sprite should have zero user bricks after being created and initialized.", array.size() == 0);

		Reflection.invokeMethod(sprite, "getUserBrickListAtLeastOneBrick", new ParameterList("Example", "Variable 1"));

		array = (ArrayList<Script>) Reflection.getPrivateField(sprite, "userBricks");

		assertTrue("the sprite should have one user brick after getUserBrickList()", array.size() == 1);

	}

	public void testSpriteHasOneUserBrickAfterAddingAUserBrick() {
		UserBrick brick = new UserBrick(sprite, 0);
		brick.addUIText("test0");
		brick.addUIVariable("test1");

		Script userScript = addUserBrickToSpriteAndGetUserScript(brick, sprite);

		userScript.addBrick(new ChangeXByNBrick(sprite, 1));

		ArrayList<Script> array = (ArrayList<Script>) Reflection.getPrivateField(sprite, "userBricks");

		assertTrue("the sprite should have one user brick after we added a user brick to it, has " + array.size(),
				array.size() == 1);
	}

	public void testSpriteMovedCorrectly() {
		int moveValue = 6;

		UserBrick brick = new UserBrick(sprite, 0);
		brick.addUIText("test0");
		brick.addUIVariable("test1");

		Script userScript = addUserBrickToSpriteAndGetUserScript(brick, sprite);

		userScript.addBrick(new ChangeXByNBrick(sprite, moveValue));

		SequenceAction sequence = new SequenceAction();
		brick.addActionToSequence(sequence);

		float x = sprite.look.getXInUserInterfaceDimensionUnit();
		float y = sprite.look.getYInUserInterfaceDimensionUnit();

		assertEquals("Unexpected initial sprite x position: " + x, 0f, x);
		assertEquals("Unexpected initial sprite y position: " + y, 0f, y);

		sequence.act(1f);

		x = sprite.look.getXInUserInterfaceDimensionUnit();
		y = sprite.look.getYInUserInterfaceDimensionUnit();

		assertEquals("Unexpected initial sprite x position: ", (float) moveValue,
				sprite.look.getXInUserInterfaceDimensionUnit());
		assertEquals("Unexpected initial sprite y position: ", 0f, sprite.look.getYInUserInterfaceDimensionUnit());
	}

	public void testSpriteMovedCorrectlyWithNestedBricks() {
		Integer moveValue = 6;

		UserBrick outerBrick = new UserBrick(sprite, 0);
		outerBrick.addUIText("test2");
		outerBrick.addUIVariable("outerBrickVariable");

		outerBrick.updateUIComponents(null);
		List<Formula> formulaList = outerBrick.getFormulas();

		assertEquals("formulaList.size() after outerBrick.updateUIComponents()" + formulaList.size(), 1,
				formulaList.size());

		for (Formula formula : formulaList) {
			formula.setRoot(new FormulaElement(ElementType.NUMBER, moveValue.toString(), null));

			assertEquals("outerBrick.formula.interpretDouble: ", (float) moveValue, formula.interpretFloat(sprite));
		}

		UserBrick innerBrick = new UserBrick(sprite, 1);
		innerBrick.addUIText("test0");
		innerBrick.addUIVariable("innerBrickVariable");

		Script innerScript = addUserBrickToSpriteAndGetUserScript(innerBrick, sprite);

		Formula innerFormula = new Formula(new FormulaElement(ElementType.USER_VARIABLE, "innerBrickVariable", null));

		innerScript.addBrick(new ChangeXByNBrick(sprite, innerFormula));

		innerBrick.updateUIComponents(null);

		formulaList = innerBrick.getFormulas();

		assertEquals("formulaList.size() after innerBrick.updateUIComponents()" + formulaList.size(), 1,
				formulaList.size());

		for (Formula formula : formulaList) {
			formula.setRoot(new FormulaElement(ElementType.USER_VARIABLE, "outerBrickVariable", null));
		}

		Script outerScript = addUserBrickToSpriteAndGetUserScript(outerBrick, sprite);
		outerScript.addBrick(innerBrick);

		StartScript startScript = new StartScript(sprite);
		sprite.addScript(startScript);
		startScript.addBrick(outerBrick);

		SequenceAction sequence = new SequenceAction();
		startScript.run(sequence);

		float x = sprite.look.getXInUserInterfaceDimensionUnit();
		float y = sprite.look.getYInUserInterfaceDimensionUnit();

		assertEquals("Unexpected initial sprite x position: ", 0f, x);
		assertEquals("Unexpected initial sprite y position: ", 0f, y);

		sequence.act(1f);

		x = sprite.look.getXInUserInterfaceDimensionUnit();
		y = sprite.look.getYInUserInterfaceDimensionUnit();

		assertEquals("Unexpected initial sprite x position: ", (float) moveValue,
				sprite.look.getXInUserInterfaceDimensionUnit());
		assertEquals("Unexpected initial sprite y position: ", 0f, sprite.look.getYInUserInterfaceDimensionUnit());
	}

	public void testGetRequiredResources() {
		int moveValue = 6;

		UserBrick brick = new UserBrick(sprite, 0);

		assertEquals("brick.getRequiredResources(): ", brick.NO_RESOURCES, brick.getRequiredResources());

		Script userScript = addUserBrickToSpriteAndGetUserScript(brick, sprite);

		LegoNxtMotorStopBrick legoBrick = new LegoNxtMotorStopBrick(sprite, Motor.MOTOR_A);

		userScript.addBrick(legoBrick);

		assertNotSame("legoBrick.getRequiredResources(): ", brick.NO_RESOURCES, legoBrick.getRequiredResources());

		assertEquals("brick.getRequiredResources(): ", legoBrick.getRequiredResources(), brick.getRequiredResources());

	}

	public void testBrickCloneWithFormula() {
		UserBrick brick = new UserBrick(sprite, 0);
		brick.addUIText("test0");
		brick.addUIVariable("test1");

		UserBrick cloneBrick = brick.clone();
		UserBrickUIDataArray array = (UserBrickUIDataArray) Reflection.getPrivateField(brick, "uiData");
		UserBrickUIDataArray clonedArray = (UserBrickUIDataArray) Reflection.getPrivateField(cloneBrick, "uiData");
		assertTrue("The cloned brick has a different uiDataArray than the original brick", array == clonedArray);

		UserScriptDefinitionBrick definition = (UserScriptDefinitionBrick) Reflection.getPrivateField(brick,
				"definitionBrick");
		UserScriptDefinitionBrick clonedDef = (UserScriptDefinitionBrick) Reflection.getPrivateField(cloneBrick,
				"definitionBrick");
		assertTrue("The cloned brick has a different UserScriptDefinitionBrick than the original brick",
				definition == clonedDef);

		UserScript userScript = (UserScript) Reflection.getPrivateField(definition, "userScript");
		UserScript clonedUserScript = (UserScript) Reflection.getPrivateField(clonedDef, "userScript");
		assertTrue("The cloned brick has a different UserScriptDefinitionBrick than the original brick",
				userScript == clonedUserScript);

		ArrayList<UserBrickUIComponent> componentArray = (ArrayList<UserBrickUIComponent>) Reflection.getPrivateField(
				brick, "uiComponents");
		ArrayList<UserBrickUIComponent> clonedComponentArray = (ArrayList<UserBrickUIComponent>) Reflection
				.getPrivateField(cloneBrick, "uiComponents");
		assertTrue("The cloned brick has a different uiDataArray than the original brick",
				componentArray != clonedComponentArray);
	}

	private Script addUserBrickToSpriteAndGetUserScript(UserBrick userBrick, Sprite sprite) {
		UserScriptDefinitionBrick definitionBrick = (UserScriptDefinitionBrick) Reflection.getPrivateField(userBrick,
				"definitionBrick");
		return definitionBrick.initScript(sprite);
	}
}
