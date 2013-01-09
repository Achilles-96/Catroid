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
package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.physics.PhysicObject;
import org.catrobat.catroid.physics.PhysicObjectBrick;
import org.catrobat.catroid.ui.ScriptTabActivity;
import org.catrobat.catroid.ui.dialogs.BrickTextDialog;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;

public class SetVelocityBrick implements PhysicObjectBrick, OnClickListener {
	private static final long serialVersionUID = 1L;

	private PhysicObject physicObject;
	private Sprite sprite;
	private Vector2 velocity;

	private transient View view;

	public SetVelocityBrick() {
	}

	public SetVelocityBrick(Sprite sprite, Vector2 velocity) {
		this.sprite = sprite;
		this.velocity = new Vector2(velocity);
	}

	@Override
	public int getRequiredResources() {
		return NO_RESOURCES;
	}

	@Override
	public void execute() {
		physicObject.setVelocity(velocity);
	}

	@Override
	public void setPhysicObject(PhysicObject physicObject) {
		this.physicObject = physicObject;
	}

	@Override
	public Sprite getSprite() {
		return this.sprite;
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter adapter) {
		view = View.inflate(context, R.layout.brick_set_velocity, null);

		TextView textX = (TextView) view.findViewById(R.id.brick_set_velocity_prototype_text_view_x);
		EditText editX = (EditText) view.findViewById(R.id.brick_set_velocity_edit_text_x);
		editX.setText(String.valueOf(velocity.x));

		textX.setVisibility(View.GONE);
		editX.setVisibility(View.VISIBLE);
		editX.setOnClickListener(this);

		TextView textY = (TextView) view.findViewById(R.id.brick_set_velocity_prototype_text_view_y);
		EditText editY = (EditText) view.findViewById(R.id.brick_set_velocity_edit_text_y);
		editY.setText(String.valueOf(velocity.y));

		textY.setVisibility(View.GONE);
		editY.setVisibility(View.VISIBLE);
		editY.setOnClickListener(this);

		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.brick_set_velocity, null);
		return view;
	}

	@Override
	public Brick clone() {
		return new SetVelocityBrick(sprite, velocity);
	}

	@Override
	public void onClick(final View view) {
		ScriptTabActivity activity = (ScriptTabActivity) view.getContext();

		BrickTextDialog editDialog = new BrickTextDialog() {
			@Override
			protected void initialize() {
				if (view.getId() == R.id.brick_set_velocity_edit_text_x) {
					input.setText(String.valueOf(velocity.x));
				} else if (view.getId() == R.id.brick_set_velocity_edit_text_y) {
					input.setText(String.valueOf(velocity.y));
				}
				input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
				input.setSelectAllOnFocus(true);
			}

			@Override
			protected boolean handleOkButton() {
				try {
					if (view.getId() == R.id.brick_set_velocity_edit_text_x) {
						velocity.x = Float.parseFloat(input.getText().toString());
					} else if (view.getId() == R.id.brick_set_velocity_edit_text_y) {
						velocity.y = Float.parseFloat(input.getText().toString());
					}
				} catch (NumberFormatException exception) {
					Toast.makeText(getActivity(), R.string.error_no_number_entered, Toast.LENGTH_SHORT).show();
				}

				return true;
			}
		};

		editDialog.show(activity.getSupportFragmentManager(), "dialog_set_velocity_brick");
	}
}