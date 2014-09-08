/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2014 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.catroid.physics.content.bricks;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.ui.adapter.BrickAdapter;

import java.util.List;

public class SeparatorBrick implements Brick {
	@Override
	public List<SequenceAction> addActionToSequence(Sprite sprite, SequenceAction sequence) {
		return null;
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite) {
		return null;
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter adapter) {
		return null;
	}

	@Override
	public View getPrototypeView(Context context) {
		return View.inflate(context, R.layout.brick_physics, null);
	}

	@Override
	public Brick clone() throws CloneNotSupportedException {
		return null;
	}

	@Override
	public int getRequiredResources() {
		return 0;
	}

	@Override
	public void setCheckboxVisibility(int visibility) {
	}

	@Override
	public int getAlphaValue() {
		return 0;
	}

	@Override
	public void setBrickAdapter(BrickAdapter adapter) {

	}

	@Override
	public CheckBox getCheckBox() {
		return null;
	}

	@Override
	public boolean isChecked() {
		return false;
	}

	@Override
	public void setCheckedBoolean(boolean newValue) {

	}

	@Override
	public void setCheckboxView(int id) {

	}

	@Override
	public View getViewWithAlpha(int alphaValue) {
		return null;
	}

	@Override
	public void setAnimationState(boolean animationState) {
	}
}
