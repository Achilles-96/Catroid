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

package org.catrobat.catroid.test.livewallpaper.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.badlogic.gdx.utils.Select;

import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.livewallpaper.postprocessing.BloomAttributeContainer;
import org.catrobat.catroid.livewallpaper.postprocessing.CrtMonitorAttributeContainer;
import org.catrobat.catroid.livewallpaper.postprocessing.CurvatureAttributeContainer;
import org.catrobat.catroid.livewallpaper.postprocessing.PostProcessingEffectAttributContainer;
import org.catrobat.catroid.livewallpaper.postprocessing.PostProcessingEffectsEnum;
import org.catrobat.catroid.livewallpaper.postprocessing.VignetteAttributeContainer;
import org.catrobat.catroid.livewallpaper.ui.SelectBloomEffectActivity;
import org.catrobat.catroid.livewallpaper.ui.SelectCrtMonitorEffectActivity;
import org.catrobat.catroid.livewallpaper.ui.SelectCurvatureEffectActivity;
import org.catrobat.catroid.livewallpaper.ui.SelectVignetteEffectActivity;

import java.util.Map;

public class TestUtils {

	//Bloom
	public static float BASE_INT = 10.0f;
	public static float BASE_SAT = 11.0f;
	public static float BLOOM_INT = 12.0f;
	public static float BLOOM_SAT = 13.0f;
	public static float BLOOM_THRESHOLD = 14.0f;
	public static boolean BLOOM_IS_ENABLED = true;

	//Vignette
	public static float INTENSITY_FACTOR = 40.0f;
	public static boolean VIGNETTE_IS_ENABLED = true;

	//Curvature
	public static float DISTORTION_FACTOR = 50.0f;
	public static boolean CURVATURE_IS_ENABLED = true;

	//Crt-Monitor
	public static float CHROMATIC_DISPERSION_RC_FACTOR = 60.0f;
	public static float CHROMATIC_DISPERSION_BY_FACTOR = 65.0f;
	public static boolean CRTMONITOR_IS_ENABLED = true;

	
	public static Project createEmptyProjectWithoutSettingIt(Context context, String projectName) {
		if (StorageHandler.getInstance().projectExists(projectName)) {
			throw new IllegalArgumentException("Project with name '" + projectName + "' already exists!");
		}
		Project emptyProject = new Project(context, projectName);
		emptyProject.setDeviceData(context);
		StorageHandler.getInstance().saveProject(emptyProject);
		//ProjectManager.getInstance().setProject(emptyProject);

		return emptyProject;
	}
	
	public static void restartActivity(Activity myActivity)
	{
		Intent myIntent = new Intent(myActivity, myActivity.getClass()); 
		myActivity.finish();
		myIntent.setAction(Intent.ACTION_MAIN); 
		myIntent.addCategory(Intent.CATEGORY_LAUNCHER); 
		myIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY); 
		myActivity.startActivity(myIntent); 	
	}

	public static void initializePostProcessingGUISForTest(Map<PostProcessingEffectsEnum,PostProcessingEffectAttributContainer> map)
	{
		BloomAttributeContainer bloom = new BloomAttributeContainer();
		bloom.setBaseInt(BASE_INT / SelectBloomEffectActivity.BASE_INT_FACTOR);
		bloom.setBaseSat(BASE_SAT / SelectBloomEffectActivity.BASE_SAT_FACTOR);
		bloom.setBloomSat(BLOOM_SAT / SelectBloomEffectActivity.BLOOM_SAT_FACTOR);
		bloom.setBloomInt(BLOOM_INT / SelectBloomEffectActivity.BLOOM_INT_FACTOR);
		bloom.setThreshold(BLOOM_THRESHOLD / SelectBloomEffectActivity.BLOOM_THRESHOLD_FACTOR);
		bloom.setEnabled(BLOOM_IS_ENABLED);
		map.put(PostProcessingEffectsEnum.BLOOM, bloom);


		VignetteAttributeContainer vignette = new VignetteAttributeContainer();
		vignette.setIntensity(INTENSITY_FACTOR / SelectVignetteEffectActivity.INTENSITY_FACTOR);
		vignette.setEnabled(VIGNETTE_IS_ENABLED);
		map.put(PostProcessingEffectsEnum.VIGNETTE, vignette);


		CurvatureAttributeContainer curvature = new CurvatureAttributeContainer();
		curvature.setDistortion(DISTORTION_FACTOR / SelectCurvatureEffectActivity.DISTORTION_FACTOR);
		curvature.setEnabled(CURVATURE_IS_ENABLED);
		map.put(PostProcessingEffectsEnum.CURVATURE, curvature);


		CrtMonitorAttributeContainer crtMonitor = new CrtMonitorAttributeContainer();
		crtMonitor.setChromaticDispersionRC(CHROMATIC_DISPERSION_RC_FACTOR / SelectCrtMonitorEffectActivity.CHROMATIC_DISPERSION_RC_FACTOR);
		crtMonitor.setChromaticDispersionBY(CHROMATIC_DISPERSION_BY_FACTOR / SelectCrtMonitorEffectActivity.CHROMATIC_DISPERSION_BY_FACTOR);
		crtMonitor.setEnabled(CRTMONITOR_IS_ENABLED);
		map.put(PostProcessingEffectsEnum.CRTMONITOR, crtMonitor);
	}

	public static void setBloomEffectDisabled(Map<PostProcessingEffectsEnum,PostProcessingEffectAttributContainer> map){
		BloomAttributeContainer bloom = new BloomAttributeContainer();
		bloom.setEnabled(false);
		map.put(PostProcessingEffectsEnum.BLOOM, bloom);
	}
}
