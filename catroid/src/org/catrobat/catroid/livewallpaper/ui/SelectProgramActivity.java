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
package org.catrobat.catroid.livewallpaper.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.catrobat.catroid.R;
import org.catrobat.catroid.livewallpaper.ColorPickerDialog;
import org.catrobat.catroid.ui.dialogs.NewProjectDialog;

public class SelectProgramActivity extends BaseActivity implements ColorPickerDialog.OnColorChangedListener {

	public static final String ACTION_PROJECT_LIST_INIT = "org.catrobat.catroid.livewallpaper.PROJECT_LIST_INIT";

	private SelectProgramFragment selectProgramFragment;
	private int tintingColor = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_projects_lwp);
		setUpActionBar();

		selectProgramFragment = (SelectProgramFragment) getSupportFragmentManager().findFragmentById(
				R.id.fragment_projects_list_lwp);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			sendBroadcast(new Intent(ACTION_PROJECT_LIST_INIT));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_selectprogram_lwp, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.delete: {
				selectProgramFragment.startDeleteActionMode();
				break;
			}
			case R.id.about: {
				AboutPocketCodeDialog aboutPocketCodeDialog = new AboutPocketCodeDialog(this);
				aboutPocketCodeDialog.show();
				break;
			}
			case R.id.lwp_new: {
				NewProjectDialog dialog = new NewProjectDialog();
				dialog.show(getSupportFragmentManager(), NewProjectDialog.DIALOG_FRAGMENT_TAG);
				break;
			}
			case R.id.lwp_tinting: {
				tinting();
				break;
			}
			case R.id.lwp_disable_tinting: {
				disableTinting();
				break;
			}
			case R.id.lwp_disable_effect: {
				disableEffects();
				break;
			}
			case R.id.lwp_effects:
			{
				goToSelectPostProcessingEffects();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	public void goToSelectPostProcessingEffects()
	{
		Intent intent = new Intent(this, SelectPostProcessingEffectActivity.class);
		//String message = editText.getText().toString();
		//intent.putExtra(EXTRA_MESSAGE, message);
		startActivity(intent);
	}

	public void disableTinting() {
		selectProgramFragment.disableTinting();
	}

	public void tinting() {
		Paint mPaint = new Paint();
		ColorPickerDialog dialog = new ColorPickerDialog(SelectProgramActivity.this, SelectProgramActivity.this,
				mPaint.getColor());
		dialog.show();
	}

	public void disableEffects() {
		selectProgramFragment.disableEffects();
	}

	private void setUpActionBar() {
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(R.string.lwp_select_program);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(false);
	}

	public SelectProgramFragment getSelectProgramFragment() {
		return selectProgramFragment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.catrobat.catroid.livewallpaper.ColorPickerDialog.OnColorChangedListener#colorChanged(int)
	 */
	@Override
	public void colorChanged(int color) {
		selectProgramFragment.tinting(color);
	}

}
