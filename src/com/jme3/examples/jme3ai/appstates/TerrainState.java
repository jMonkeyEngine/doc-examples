/*
 * Copyright (c) 2017, jMonkeyEngine All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors may 
 *   be used to endorse or promote products derived from this software without 
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.examples.jme3ai.appstates;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

/**
 * Creates the floors and ramps for the AI example project.
 * 
 * @author mitm
 */
public class TerrainState extends BaseAppState {

    private static final Quaternion YAW090 = new Quaternion().fromAngleAxis(
            FastMath.PI / 2, new Vector3f(0, 1, 0));
    private static final Quaternion ROLL135 = new Quaternion().fromAngleAxis(
            FastMath.PI * 135 / 180, new Vector3f(0, 0, 1));
    private static final Quaternion PITCH045 = new Quaternion().fromAngleAxis(
            FastMath.PI / 4, new Vector3f(1, 0, 0));
    private SimpleApplication app;
    private Geometry lowerLevel, midLevel, upperLevel, ramp, ramp2, ramp3;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        initFloor();
        initLight();
    }

    @Override
    protected void cleanup(Application app) {
        lowerLevel.removeControl(RigidBodyControl.class);
        ramp.removeControl(RigidBodyControl.class);
        midLevel.removeControl(RigidBodyControl.class);
        ramp2.removeControl(RigidBodyControl.class);
        upperLevel.removeControl(RigidBodyControl.class);
        ramp3.removeControl(RigidBodyControl.class);
    }

    //onEnable()/onDisable() can be used for managing things that should 
    //only exist while the state is enabled. Prime examples would be scene 
    //graph attachment or input listener attachment.
    @Override
    protected void onEnable() {
        getPhysicsSpace().add(lowerLevel);
        app.getRootNode().attachChild(lowerLevel);
        getPhysicsSpace().add(midLevel);
        app.getRootNode().attachChild(midLevel);
        getPhysicsSpace().add(upperLevel);
        app.getRootNode().attachChild(upperLevel);
        getPhysicsSpace().add(ramp);
        app.getRootNode().attachChild(ramp);
        getPhysicsSpace().add(ramp2);
        app.getRootNode().attachChild(ramp2);
        getPhysicsSpace().add(ramp3);
        app.getRootNode().attachChild(ramp3);
    }

    @Override
    protected void onDisable() {
        getPhysicsSpace().remove(lowerLevel);
        app.getRootNode().detachChild(lowerLevel);
        getPhysicsSpace().remove(midLevel);
        app.getRootNode().detachChild(midLevel);
        getPhysicsSpace().remove(upperLevel);
        app.getRootNode().detachChild(upperLevel);
        getPhysicsSpace().remove(ramp);
        app.getRootNode().detachChild(ramp);
        getPhysicsSpace().remove(ramp2);
        app.getRootNode().detachChild(ramp2);
        getPhysicsSpace().remove(ramp3);
        app.getRootNode().detachChild(ramp3);
    }

    @Override
    public void update(float tpf) {
        //TODO: implement behavior during runtime
    }

    private void initFloor() {
        //all floors, ramps share same material, Box, collision shape
        Box floorBox = new Box(10f, 0.1f, 5f);
        floorBox.scaleTextureCoordinates(new Vector2f(3, 6));
        Material mat = new Material(getApplication().getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Textures/Terrain/Pond/Pond.jpg");
        key.setGenerateMips(true);
        Texture tex = getApplication().getAssetManager().loadTexture(key);
        tex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("ColorMap", tex);

        lowerLevel = new Geometry("lowerLevel", floorBox);
        lowerLevel.setMaterial(mat);
        lowerLevel.setLocalTranslation(0, -.1f, 0);
        BoxCollisionShape boxCollisionShape = new BoxCollisionShape(
                new Vector3f(10f, 0.1f, 5f));
        lowerLevel.addControl(new RigidBodyControl(boxCollisionShape, 0));

        midLevel = new Geometry("midLevel", floorBox);
        midLevel.setMaterial(mat);
        midLevel.setLocalTranslation(0, 1f, -11);
        midLevel.addControl(new RigidBodyControl(boxCollisionShape, 0));

        upperLevel = new Geometry("upperLevel", floorBox);
        upperLevel.setMaterial(mat);
        upperLevel.setLocalRotation(YAW090);
        upperLevel.setLocalTranslation(-10, 2.3f, -5);
        upperLevel.addControl(new RigidBodyControl(boxCollisionShape, 0));

        Box rampBox = new Box(2f, 0.1f, 1f);
        ramp = new Geometry("ramp", rampBox);
        ramp.setMaterial(mat);
        ramp.setLocalRotation(PITCH045);
        ramp.setLocalTranslation(0, .5f, -5.5f);
        BoxCollisionShape boxCollisionShape1 = new BoxCollisionShape(new Vector3f(2f, 0.1f, 1f));
        ramp.addControl(new RigidBodyControl(boxCollisionShape1, 0));

        ramp2 = new Geometry("ramp", rampBox);
        ramp2.setMaterial(mat);
        ramp2.setLocalRotation(ROLL135);
        ramp2.setLocalTranslation(-3.5f, 1f, -3f);
        ramp2.addControl(new RigidBodyControl(boxCollisionShape1, 0));

        ramp3 = new Geometry("ramp", rampBox);
        ramp3.setMaterial(mat);
        ramp3.setLocalRotation(ROLL135);
        ramp3.setLocalTranslation(-3.5f, 1f, -13f);
        ramp3.addControl(new RigidBodyControl(boxCollisionShape1, 0));
    }

    private void initLight() {
        /**
         * A white, directional light source
         */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        app.getRootNode().addLight(sun);

        /**
         * A white ambient light source.
         */
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        app.getRootNode().addLight(ambient);
    }

    /**
     * @return the PhysicsSpace
     */
    private PhysicsSpace getPhysicsSpace() {
        return getState(BulletAppState.class).getPhysicsSpace();
    }

}
