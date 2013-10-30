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
package org.catrobat.catroid.livewallpaper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.io.SoundManager;

import java.util.List;

@SuppressLint("NewApi")
public class LiveWallpaperSettings extends PreferenceActivity {

	static Context context;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}

	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.livewallpapersettings);
			handleAboutPocketCodePreference();
			handleAboutThisWallpaperPreference();
			handleCreateWallpapers();
			handleSelectProgramDialog();
			handleAllowSoundsCheckBox();

		}

		private void handleSelectProgramDialog() {
			Preference pref = findPreference(getResources().getString(R.string.lwp_select_program));

			pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					SelectProgramDialog selectProgramDialog = new SelectProgramDialog(context);
					selectProgramDialog.show();
					return false;
				}
			});

		}

		private void handleAllowSoundsCheckBox() {
			final CheckBoxPreference allowSounds = (CheckBoxPreference) findPreference(getResources().getString(
					R.string.lwp_allow_sounds));

			allowSounds.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
					Editor editor = sharedPreferences.edit();

					if (newValue.toString().equals("true")) {
						SoundManager.getInstance().soundDisabledByLwp = false;
						allowSounds.setChecked(true);
						editor.putBoolean(Constants.PREF_SOUND_DISABLED, false);

					} else {
						SoundManager.getInstance().soundDisabledByLwp = true;
						allowSounds.setChecked(false);
						editor.putBoolean(Constants.PREF_SOUND_DISABLED, true);

					}
					editor.commit();
					return false;
				}
			});

		}

		private void handleAboutThisWallpaperPreference() {
			Preference pref = findPreference(getResources().getString(R.string.about_this_wallpaper));

			pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					AboutWallpaperDialog aboutWallpaperDialog = new AboutWallpaperDialog(context);
					aboutWallpaperDialog.show();
					return false;
				}
			});

		}

		private void handleAboutPocketCodePreference() {
			Preference licence = findPreference(getResources().getString(R.string.main_menu_about_pocketcode));

			licence.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {

					AboutPocketCodeDialog aboutPocketCodeDialog = new AboutPocketCodeDialog(context);
					aboutPocketCodeDialog.show();
					return false;
				}
			});

		}

		private void handleCreateWallpapers() {

			Preference licence = findPreference(getResources().getString(R.string.create_programs));

			licence.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent pocketCodeIntent = new Intent("android.intent.action.MAIN");
					pocketCodeIntent.setComponent(new ComponentName(Constants.POCKET_CODE_PACKAGE_NAME,
							Constants.POCKET_CODE_INTENT_ACTIVITY_NAME));
					boolean isInstalled = checkIfPocketCodeInstalled(pocketCodeIntent);
					if (isInstalled) {

						pocketCodeIntent.addCategory("android.intent.category.LAUNCHER");
						startActivity(pocketCodeIntent);
					}

					return false;
				}
			});

		}

		private boolean checkIfPocketCodeInstalled(Intent pocketCodeIntent) {
			final Activity activity = getActivity();
			List<ResolveInfo> packageList = activity.getPackageManager().queryIntentActivities(pocketCodeIntent,
					PackageManager.MATCH_DEFAULT_ONLY);

			if (packageList.size() <= 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage(R.string.pocket_code_not_installed).setCancelable(false)
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {

								Intent downloadPocketPaintIntent = new Intent(Intent.ACTION_VIEW, Uri
										.parse(Constants.POCKET_CODE_DOWNLOAD_LINK));
								activity.startActivity(downloadPocketPaintIntent);
							}
						}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
				return false;
			} else {
				return true;
			}

		}

	}
}
