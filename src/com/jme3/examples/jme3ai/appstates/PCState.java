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

import com.jme3.ai.navmesh.NavMesh;
import com.jme3.examples.jme3ai.controls.PCControl;
import com.jme3.examples.jme3ai.controls.AnimationControl;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.examples.jme3ai.ai.NavigationControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.examples.jme3ai.enums.EnumPosition;
import com.jme3.examples.jme3ai.interfaces.DataKey;
import com.jme3.examples.jme3ai.interfaces.ListenerKey;
import com.jme3.examples.jme3ai.interfaces.Pickable;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;

/**
 * Creates the character for the AI example project.
 *
 * @author mitm
 */
public class PCState extends BaseAppState {

    private SimpleApplication app;
    private Node charNode;
    private Geometry mark;
    private ClickedListener actionListener;
    private Pickable picked;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        actionListener = new ClickedListener();
        initChar();
        initMark();
    }

    private void initChar() {
        Node spatial = (Node) getApplication().getAssetManager().loadModel(
                "Models/Sinbad/Sinbad.mesh.xml");
        BoundingBox bounds = (BoundingBox) spatial.getWorldBound();
        //scale spatial
        spatial.setLocalScale(1.8f / (bounds.getYExtent() * 2));
        bounds = (BoundingBox) spatial.getWorldBound();
        spatial.setLocalTranslation(0, bounds.getYExtent()
                - bounds.getCenter().y, 0);
        //spatial origin is at center so need to offset by using Node
        charNode = new Node("charNode");
        charNode.setLocalTranslation(new Vector3f(0, bounds.getYExtent(), 0));
        //attach ChaseCamera i.e. head.
        charNode.attachChild(getHead(bounds));
        charNode.attachChild(spatial);
        //set starting position of spatial
        charNode.setUserData(DataKey.POSITION, EnumPosition.POS_STANDING.
                position());
        //control that moves spatial
        charNode.addControl(new PCControl(.6f, 1.8f, 80f));
        //control for animations
        charNode.addControl(new AnimationControl());
        //load NavMesh geometry saved to assets folder
        Geometry navGeom = (Geometry) getApplication().getAssetManager().
                loadModel("Scenes/NavMesh/NavMesh.j3o");
        NavigationControl navControl = new NavigationControl(new NavMesh(
                navGeom.getMesh()), getApplication(), true);
        charNode.addControl(navControl);
        //NavigationControl implements Pickable Interface 
        picked = navControl;
    }

    @Override
    protected void cleanup(Application app) {
        this.app.getRootNode().detachChild(charNode);
        charNode.detachAllChildren();
        charNode.removeControl(PCControl.class);
        charNode.removeControl(NavigationControl.class);
        charNode.removeControl(AnimationControl.class);
    }

    //onEnable()/onDisable() can be used for managing things that should 
    //only exist while the state is enabled. Prime examples would be scene 
    //graph attachment or input listener attachment.
    @Override
    protected void onEnable() {
        getInputManager().addListener(actionListener, ListenerKey.PICK);
        //add PCControl to PhysicsSpace
        getPhysicsSpace().add(charNode);
        //add char to game
        app.getRootNode().attachChild(charNode);
    }

    @Override
    protected void onDisable() {
        getInputManager().removeListener(actionListener);
        //remove PCControl from PhysicsSpace
        getPhysicsSpace().remove(charNode);
        //remove char from game
        app.getRootNode().detachChild(charNode);
    }

    @Override
    public void update(float tpf) {
        //TODO: implement behavior during runtime
    }

    /**
     * A red ball that marks the last spot that was "hit" by the "shot".
     */
    protected void initMark() {
        Sphere sphere = new Sphere(30, 30, 0.2f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(getApplication().getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
    }

    //Create the 3rd person view.
    private Node getHead(BoundingBox bounds) {
        Node head = new Node("headNode");
        //ofset head node using spatial bounds to position head level
        head.setLocalTranslation(0, bounds.getYExtent() * 2, 0);
        //use offset head node as pickManager for cam to follow
        ChaseCamera chaseCam = new ChaseCamera(app.getCamera(), head,
                getInputManager());
        //Set arrow keys to rotate view, sets CHASECAM_TOGGLEROTATE, you map the 
        //triggers globaly after calling setToggleRotationTrigger, see KeyBoardRunState.
        //Uses default mouse scrolling to zoom.
        chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(
                MouseInput.AXIS_WHEEL),
                new KeyTrigger(KeyInput.KEY_LEFT),
                new KeyTrigger(KeyInput.KEY_RIGHT),
                new KeyTrigger(KeyInput.KEY_UP),
                new KeyTrigger(KeyInput.KEY_DOWN));
        //duplicate blender rotation
        chaseCam.setInvertVerticalAxis(true);
        //disable so camera stays same distance from head when moving
        chaseCam.setSmoothMotion(false);
        //never hide cursor if used for picking
        chaseCam.setHideCursorOnRotate(false);
        //set camera to face spatial on start
        chaseCam.setDefaultHorizontalRotation(FastMath.PI / 2);
        chaseCam.setRotationSpeed(4f);
        chaseCam.setMinDistance(bounds.getYExtent() * 2);
        chaseCam.setDefaultDistance(bounds.getYExtent() * 10);
        chaseCam.setMaxDistance(bounds.getYExtent() * 15);
        //prevent camera rotation below head
        chaseCam.setDownRotateOnCloseViewOnly(false);
        return head;
    }

    private class ClickedListener implements ActionListener {

        @Override
        public void onAction(String name, boolean isPressed, float tpf) {

            if (name.equals(ListenerKey.PICK) && !isPressed) {
                CollisionResults results = new CollisionResults();
                Vector2f click2d = getInputManager().getCursorPosition().clone();
                Vector3f click3d = app.getCamera().getWorldCoordinates(click2d,
                        0f).clone();
                Vector3f dir = app.getCamera().getWorldCoordinates(
                        click2d, 1f).subtractLocal(click3d).normalizeLocal();
                Ray ray = new Ray(click3d, dir);
                app.getRootNode().collideWith(ray, results);

                for (int i = 0; i < results.size(); i++) {
                    // For each hit, we know distance, impact point, name of geometry.
                    float dist = results.getCollision(i).getDistance();
                    Vector3f pt = results.getCollision(i).getContactPoint();
                    String hit = results.getCollision(i).getGeometry().getName();
                    System.out.println("* Collision #" + i);
                    System.out.println(
                            "  You shot " + hit
                            + " at " + pt
                            + ", " + dist + " wu away.");
                }

                if (results.size() > 0) {
                    // The closest collision point is what was truly hit:
                    CollisionResult closest = results.getClosestCollision();
                    // Let's interact - we mark the hit with a red dot.
                    mark.setLocalTranslation(closest.getContactPoint());
                    app.getRootNode().attachChild(mark);
                    picked.setTarget(closest.getContactPoint());
                    System.out.println("  Closest Contact " + closest.
                            getContactPoint());
                } else {
                    // No hits? Then remove the red mark.
                    app.getRootNode().detachChild(mark);
                }
            }
        }
    }

    //get the physics space
    private PhysicsSpace getPhysicsSpace() {
        return getState(BulletAppState.class).getPhysicsSpace();
    }

    //get the input manager
    private InputManager getInputManager() {
        return getApplication().getInputManager();
    }

}
