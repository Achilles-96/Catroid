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
package org.catrobat.catroid.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.content.bricks.UserBrickUIData;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.formulaeditor.UserVariablesContainer;
import org.catrobat.catroid.ui.DragAndDropBrickLayoutListener;
import org.catrobat.catroid.ui.DragNDropBrickLayout;
import org.catrobat.catroid.ui.ScriptActivity;
import org.catrobat.catroid.ui.dialogs.UserBrickEditElementDialog;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

public class UserBrickDataEditorFragment extends SherlockFragment implements OnKeyListener,
		DragAndDropBrickLayoutListener, UserBrickEditElementDialog.DialogListener {

	public static final String BRICK_DATA_EDITOR_FRAGMENT_TAG = "brick_data_editor_fragment";
	private static final String BRICK_BUNDLE_ARGUMENT = "current_brick";
	private Context context;
	private UserBrick currentBrick;
	private int indexOfCurrentlyEditedElement;
	private LinearLayout editorBrickSpace;
	private View brickView;

	private View fragmentView;

	//blah

	public UserBrickDataEditorFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.brick_data_editor_title));

		currentBrick = (UserBrick) getArguments().getSerializable(BRICK_BUNDLE_ARGUMENT);
	}

	public static void showFragment(View view, UserBrick brick) {
		SherlockFragmentActivity activity = null;
		activity = (SherlockFragmentActivity) view.getContext();

		UserBrickDataEditorFragment dataEditorFragment = (UserBrickDataEditorFragment) activity
				.getSupportFragmentManager().findFragmentByTag(BRICK_DATA_EDITOR_FRAGMENT_TAG);

		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();

		fragTransaction.addToBackStack(null);

		if (dataEditorFragment == null) {
			dataEditorFragment = new UserBrickDataEditorFragment();
			Bundle bundle = new Bundle();
			bundle.putSerializable(BRICK_BUNDLE_ARGUMENT, brick);
			dataEditorFragment.setArguments(bundle);

			fragTransaction.add(R.id.script_fragment_container, dataEditorFragment, BRICK_DATA_EDITOR_FRAGMENT_TAG);
			fragTransaction.hide(fragmentManager.findFragmentByTag(ScriptFragment.TAG));
			fragTransaction.show(dataEditorFragment);
			activity.findViewById(R.id.bottom_bar).setVisibility(View.GONE);

		} else if (dataEditorFragment.isHidden()) {
			dataEditorFragment.updateBrickView();
			fragTransaction.hide(fragmentManager.findFragmentByTag(ScriptFragment.TAG));
			fragTransaction.show(dataEditorFragment);
			activity.findViewById(R.id.bottom_bar).setVisibility(View.GONE);
		} else {
			// ??
		}
		fragTransaction.commit();
	}

	private void onUserDismiss() {
		SherlockFragmentActivity activity = getSherlockActivity();

		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		fragmentManager.popBackStack();

		if (activity instanceof ScriptActivity) {
			((ScriptActivity) activity).setupActionBar();
		} else {
			Log.e("userbricks",
					"UserBrickDataEditor.onUserDismiss() called when the parent activity is not a UserBrickScriptActivity!\n"
							+ "This should never happen, afaik. I don't know how to correctly reset the action bar...");
		}

		activity.findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
		activity.findViewById(R.id.bottom_bar_separator).setVisibility(View.VISIBLE);
		activity.findViewById(R.id.button_play).setVisibility(View.VISIBLE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentView = inflater.inflate(R.layout.fragment_brick_data_editor, container, false);
		fragmentView.setFocusableInTouchMode(true);
		fragmentView.requestFocus();

		context = getActivity();
		brickView = View.inflate(context, R.layout.brick_user_editable, null);

		updateBrickView();

		editorBrickSpace = (LinearLayout) fragmentView.findViewById(R.id.brick_data_editor_brick_space);

		editorBrickSpace.addView(brickView);

		ListView buttonList = (ListView) fragmentView.findViewById(R.id.button_list);

		buttonList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Resources res = getResources();

				String[] actions = res.getStringArray(R.array.data_editor_buttons);

				String action = actions[position];
				if (action.equals(res.getString(R.string.add_text))) {
					addTextDialog();
				}
				if (action.equals(res.getString(R.string.add_variable))) {
					addVariableDialog();
				}
				if (action.equals(res.getString(R.string.close))) {
					onUserDismiss();
				}

			}
		});

		return fragmentView;
	}

	public void addTextDialog() {
		int indexOfNewText = currentBrick.addUIText("");

		editElementDialog(indexOfNewText, "", false, R.string.add_text, R.string.add_text);
		indexOfCurrentlyEditedElement = indexOfNewText;

	}

	public void addVariableDialog() {
		int indexOfNewText = currentBrick.addUIVariable("");
		editElementDialog(indexOfNewText, "", false, R.string.add_variable, R.string.add_variable);
		indexOfCurrentlyEditedElement = indexOfNewText;
	}

	public void editElementDialog(int id, CharSequence text, boolean editMode, int title, int defaultText) {
		UserVariablesContainer variablesContainer = null;
		variablesContainer = ProjectManager.getInstance().getCurrentProject().getUserVariables();

		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();
		List<UserVariable> spriteVars = variablesContainer.getOrCreateVariableListForSprite(currentSprite);
		List<UserVariable> globalVars = variablesContainer.getProjectVariables();

		ArrayList<String> takenVariables = new ArrayList<String>();
		int i = 0;
		for (UserBrickUIData d : currentBrick.uiData) {
			if (i != id && d.isVariable) {
				takenVariables.add(d.name);
			}
			i++;
		}
		for (UserVariable v : spriteVars) {
			takenVariables.add(v.getName());
		}
		for (UserVariable v : globalVars) {
			takenVariables.add(v.getName());
		}

		UserBrickEditElementDialog dialog = new UserBrickEditElementDialog();
		dialog.addDialogListener(this);
		dialog.show(((SherlockFragmentActivity) getActivity()).getSupportFragmentManager(),
				UserBrickEditElementDialog.DIALOG_FRAGMENT_TAG);
		UserBrickEditElementDialog.setTakenVariables(takenVariables);
		UserBrickEditElementDialog.setTitle(title);
		UserBrickEditElementDialog.setText(text);
		UserBrickEditElementDialog.setHintText(defaultText);
		UserBrickEditElementDialog.setEditMode(editMode);
	}

	@Override
	public void onFinishDialog(CharSequence text, boolean editMode) {
		Log.d("FOREST", "onFinishDialog()");
		UserBrickUIData d = currentBrick.uiData.get(indexOfCurrentlyEditedElement);
		if (d != null) {
			String emptyString = ("").toString();
			if (text != null) {
				String oldString = d.name;
				String newString = text.toString();
				currentBrick.renameUIVariable(oldString, newString, getActivity());
			} else if (d.name.toString().equals(emptyString)) {
				currentBrick.uiData.remove(d);
			}
		}
		updateBrickView();
	}

	@Override
	public void reorder(int from, int to) {

		currentBrick.reorderUIData(from, to);
		updateBrickView();
	}

	@Override
	public void click(int id) {
		Log.d("FOREST", "click()");
		UserBrickUIData d = currentBrick.uiData.get(id);
		if (d != null) {
			Log.d("FOREST", "d != null");
			int title = d.isVariable ? R.string.edit_variable : R.string.edit_text;
			int defaultText = d.isVariable ? R.string.edit_variable : R.string.edit_text;
			editElementDialog(id, d.name, true, title, defaultText);
			indexOfCurrentlyEditedElement = id;
		}
	}

	private void deleteButtonClicked(View theView) {
		DragNDropBrickLayout layout = (DragNDropBrickLayout) brickView.findViewById(R.id.brick_user_flow_layout);
		int found = -1;
		for (int i = 0; i < layout.getChildCount(); i++) {
			if (layout.getChildAt(i) == theView) {
				found = i;
			}
		}
		if (found > -1) {
			currentBrick.removeDataAt(found);
			updateBrickView();
		} else {
			Log.d("FOREST", "UserBrickDataEditorFragment.deleteButtonClicked() Unable to find view to remove!! ");
		}
	}

	public void updateBrickView() {
		Context context = brickView.getContext();

		DragNDropBrickLayout layout = (DragNDropBrickLayout) brickView.findViewById(R.id.brick_user_flow_layout);
		layout.setListener(this);

		if (layout.getChildCount() > 0) {
			layout.removeAllViews();
		}

		for (UserBrickUIData d : currentBrick.uiData) {
			View dataView = null;
			if (d.isVariable) {
				dataView = View.inflate(context, R.layout.brick_user_data_variable, null);
			} else {
				dataView = View.inflate(context, R.layout.brick_user_data_text, null);
			}
			TextView textView = (TextView) dataView.findViewById(R.id.text_view);

			textView.setText(d.name);
			Button button = (Button) dataView.findViewById(R.id.button);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					deleteButtonClicked((View) view.getParent());
				}
			});

			layout.addView(dataView);
		}

		//if(onTouchListener)
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		for (int index = 0; index < menu.size(); index++) {
			menu.getItem(index).setVisible(false);
		}

		getSherlockActivity().getSupportActionBar().setNavigationMode(
				com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_STANDARD);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.brick_data_editor_title));

		super.onPrepareOptionsMenu(menu);
	}

	private void showToast(int ressourceId) {
		Toast.makeText(context, getString(ressourceId), Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				onUserDismiss();
				return true;
		}
		return false;
	}

}
