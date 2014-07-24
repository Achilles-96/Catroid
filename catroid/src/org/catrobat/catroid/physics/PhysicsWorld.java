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
package org.catrobat.catroid.physics;

import android.util.Log;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxNativesLoader;

import org.catrobat.catroid.content.Look;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.InterpretationException;
import org.catrobat.catroid.physics.shapebuilder.PhysicsShapeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PhysicsWorld {
	static {
		GdxNativesLoader.load();
	}

	private static final String TAG = PhysicsWorld.class.getSimpleName();

	// CATEGORY
	public static final short NOCOLLISION_MASK = 0x0000;
	public static final short CATEGORY_BOUNDARYBOX = 0x0002;
	public static final short CATEGORY_PHYSICSOBJECT = 0x0004;

	// COLLISION_MODE
	public static final short MASK_BOUNDARYBOX = CATEGORY_PHYSICSOBJECT; // collides with physics_objects
	public static final short MASK_PHYSICSOBJECT = ~CATEGORY_BOUNDARYBOX; // collides with everything but not with the boundarybox
	public static final short MASK_TOBOUNCE = CATEGORY_BOUNDARYBOX; // collide with the boundarybox
	public static final short MASK_NOCOLLISION = 0; // collides with NOBODY

	public static final float RATIO = 40.0f;
	public static final int VELOCITY_ITERATIONS = 8;
	public static final int POSITION_ITERATIONS = 3;

	public static final Vector2 DEFAULT_GRAVITY = new Vector2(0.0f, -10.0f);
	public static final boolean IGNORE_SLEEPING_OBJECTS = false;

	public static final int STABILIZING_STEPS = 6;

	private final World world = new World(PhysicsWorld.DEFAULT_GRAVITY, PhysicsWorld.IGNORE_SLEEPING_OBJECTS);
	private final Map<Sprite, PhysicsObject> physicsObjects = new HashMap<Sprite, PhysicsObject>();
	private final ArrayList<Sprite> toBounce = new ArrayList<Sprite>();
	private Box2DDebugRenderer renderer;
	private int stabilizingSteCounter = 0;
	private PhysicsBoundaryBox box;

	private PhysicsShapeBuilder physicsShapeBuilder = new PhysicsShapeBuilder();

	public PhysicsWorld(int width, int height) {
		box = new PhysicsBoundaryBox(world);
		box.create(width, height);
		world.setContactListener(new PhysicsCollision(this));
	}

	public void setBounceOnce(Sprite sprite) {
		if (physicsObjects.containsKey(sprite)) {
			PhysicsObject physicsObject = physicsObjects.get(sprite);
			physicsObject.setIfOnEdgeBounce(true, sprite);
			toBounce.add(sprite);
			//Log.d(TAG, "setBounceOnce: TRUE");
		} else {
			//Log.d(TAG, "setBounceOnce: SPRITE NOT KNOWN");
		}
	}

	public void step(float deltaTime) {
		if (stabilizingSteCounter < STABILIZING_STEPS) {
			stabilizingSteCounter++;
		} else {
			try {
				world.step(deltaTime, PhysicsWorld.VELOCITY_ITERATIONS, PhysicsWorld.POSITION_ITERATIONS);
			} catch (Exception exception) {
				Log.e(TAG, Log.getStackTraceString(exception));
			}
		}
	}

	public void render(Matrix4 perspectiveMatrix) {
		if (renderer == null) {
			renderer = new Box2DDebugRenderer(PhysicsDebugSettings.Render.RENDER_BODIES,
					PhysicsDebugSettings.Render.RENDER_JOINTS, PhysicsDebugSettings.Render.RENDER_AABB,
					PhysicsDebugSettings.Render.RENDER_INACTIVE_BODIES, PhysicsDebugSettings.Render.RENDER_VELOCITIES);
		}
		renderer.render(world, perspectiveMatrix.scl(PhysicsWorld.RATIO));
	}

	public void setGravity(float x, float y) {
		world.setGravity(new Vector2(x, y));
	}

	public void changeLook(PhysicsObject physicsObject, Look look) {
		physicsObject.setShape(physicsShapeBuilder.getShape(look.getLookData(),
				look.getSizeInUserInterfaceDimensionUnit() / 100f));
	}

	public void changeLook(Sprite sprite) {
		PhysicsObject physicsObject = getPhysicsObject(sprite);
		physicsObject.setShape(physicsShapeBuilder.getShape(sprite.look.getLookData(),
				sprite.look.getSizeInUserInterfaceDimensionUnit() / 100f));

		try {
			physicsObject.setX(new Formula(-sprite.look.getX()).interpretFloat(sprite));
			physicsObject.setY(new Formula(sprite.look.getY()).interpretFloat(sprite));
		}catch(InterpretationException interpretationException){

		}
		physicsObject.setDirection(sprite.look.getDirectionInUserInterfaceDimensionUnit());
	}

	public PhysicsObject getPhysicsObject(Sprite sprite) {
		if (sprite == null) {
			throw new NullPointerException();
		}

		if (physicsObjects.containsKey(sprite)) {
			return physicsObjects.get(sprite);
		}

		PhysicsObject physicsObject = createPhysicsObject(sprite);
		physicsObjects.put(sprite, physicsObject);

		return physicsObject;
	}

	private PhysicsObject createPhysicsObject(Sprite sprite) {
		BodyDef bodyDef = new BodyDef();
		return new PhysicsObject(world.createBody(bodyDef), sprite);
	}

	public void bounced(Sprite sprite) {
		if (physicsObjects.containsKey(sprite)) {
			PhysicsObject physicsObject = physicsObjects.get(sprite);
			physicsObject.setIfOnEdgeBounce(false, sprite);
			//Log.d(TAG, "Bounced");
			if (toBounce.remove(sprite)) {
				//Log.d(TAG, "SPRITE REMOVED");
			}
		} else {
			//Log.d(TAG, "Bounced: BUT SPRITE NOT KNOWN");
		}

	}
}