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
package com.jme3.examples.jme3ai.ai;

import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.Path;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.cinematic.MotionPath;
import com.jme3.examples.jme3ai.controls.AnimationControl;
import com.jme3.examples.jme3ai.controls.PCControl;
import com.jme3.examples.jme3ai.enums.EnumPosType;
import com.jme3.examples.jme3ai.interfaces.DataKey;
import com.jme3.examples.jme3ai.interfaces.ListenerKey;
import com.jme3.examples.jme3ai.interfaces.Pickable;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Spline;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements NavigationControl but actually extends Pathfinder which
 * uses the NavMesh, you can replace it with any Pathfinding system.
 *
 * @author adapted by mitm from the MonkeyZone project written by normenhansen.
 */
public class NavigationControl extends NavMeshPathfinder implements Control,
        JmeCloneable, Pickable {
    private static final Logger LOG = Logger.getLogger(NavigationControl.class.
            getName());
    private final ScheduledExecutorService executor;
    private PCControl pcControl;
    private Spatial spatial;
    private boolean pathfinding;
    private Vector3f wayPosition;
    private final boolean debug;
    private MotionPath motionPath;
    private boolean showPath;
    private final SimpleApplication app;
    private Vector3f target;

    public NavigationControl(NavMesh navMesh, Application app, boolean debug) {
        super(navMesh); //sets the NavMesh for this control
        this.app = (SimpleApplication) app;
        this.debug = debug;
        if (debug) {
            motionPath = new MotionPath();
            motionPath.setPathSplineType(Spline.SplineType.Linear);
        }
        executor = Executors.newScheduledThreadPool(1);
        startPathFinder();
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        try {
            NavigationControl c = (NavigationControl) clone();
            c.spatial = null; // to keep setSpatial() from throwing an exception
            c.setSpatial(spatial);
            return c;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Can't clone control for spatial", e);
        }
    }

    @Override
    public Object jmeClone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Can't clone control for spatial", e);
        }
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        this.spatial = cloner.clone(spatial);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        if (this.spatial != null && spatial != null && spatial != this.spatial) {
            throw new IllegalStateException(
                    "This control has already been added to a Spatial");
        }
        this.spatial = spatial;
        if (spatial == null) {
            shutdownAndAwaitTermination(executor);
            pcControl = null;
        } else {
            pcControl = spatial.getControl(PCControl.class);
            if (pcControl == null) {
                throw new IllegalStateException(
                        "Cannot add NavigationControl to spatial without PCControl!");
            }
        }
    }

    //standard shutdown process for executor
    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(6, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(6, TimeUnit.SECONDS)) {
                    LOG.log(Level.SEVERE, "Pool did not terminate {0}", pool);
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void update(float tpf) {
        if (getWayPosition() != null) {
            Vector3f spatialPosition = spatial.getWorldTranslation();
            Vector2f aiPosition = new Vector2f(spatialPosition.x,
                    spatialPosition.z);
            Vector2f waypoint2D = new Vector2f(getWayPosition().x,
                    getWayPosition().z);
            float distance = aiPosition.distance(waypoint2D);
            //move char between waypoints until waypoint reached then set null
            if (distance > .25f) {
                Vector2f direction = waypoint2D.subtract(aiPosition);
                direction.mult(tpf);
                pcControl.setViewDirection(new Vector3f(direction.x, 0,
                        direction.y).normalize());
                pcControl.onAction(ListenerKey.MOVE_FORWARD, true, 1);
            } else {
                setWayPosition(null);
            }
        } else if (!isPathfinding() && getNextWaypoint() != null
                && !isAtGoalWaypoint()) {
            if (showPath) {
                showPath();
                showPath = false;
            }
            //advance to next waypoint 
            goToNextWaypoint();
            setWayPosition(new Vector3f(getWaypointPosition()));

            //set spatial physical position
            if (getPosType() == EnumPosType.POS_STANDING.positionType()) {
                setPositionType(EnumPosType.POS_RUNNING.positionType());
                stopFeetPlaying();
                stopTorsoPlaying();
            }
        } else {
            //waypoint null so stop moving and set spatials physical position
            if (getPosType() == EnumPosType.POS_RUNNING.positionType()) {
                setPositionType(EnumPosType.POS_STANDING.positionType());
                stopFeetPlaying();
                stopTorsoPlaying();
            }
            pcControl.onAction(ListenerKey.MOVE_FORWARD, false, 1);
        }
    }

    @Override
    public void render(RenderManager rm, ViewPort vp) {

    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //Computes a path using the A* algorithm. Every 1/2 second checks target
    //for processing. Path will remain untill a new path is generated.
    private void startPathFinder() {
        executor.scheduleWithFixedDelay(() -> {
            if (target != null) {
                clearPath();
                setWayPosition(null);
                pathfinding = true;
                //setPosition must be set before computePath is called.
                setPosition(spatial.getWorldTranslation());
                //*The first waypoint on any path is the one you set with 
                //`setPosition()`.
                //*The last waypoint on any path is always the `target` Vector3f.
                //computePath() adds one waypoint to the cell *nearest* to the 
                //target only if you are not in the goalCell (the cell target is in), 
                //and if there is a cell between first and last waypoint, 
                //and if there is no direct line of sight. 
                //*If inside the goalCell when a new target is selected, 
                //computePath() will do a direct line of sight placement of 
                //target. This means there will only be 2 waypoints set, 
                //`setPosition()` and `target`.
                //*If the `target` is outside the `NavMesh`, your endpoint will 
                //be also.
                //warpInside(target) moves endpoint within the navMesh always.
                warpInside(target);
                System.out.println("Target " + target);
                boolean success;
                //compute the path
                success = computePath(target);
                System.out.println("SUCCESS = " + success);
                if (success) {
                    //clear target if successful
                    target = null;
                    if (debug) {
                        //display motion path
                        showPath = true;
                    }
                }
                pathfinding = false;
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * @return the pathfinding
     */
    public boolean isPathfinding() {
        return pathfinding;
    }

    /**
     * @return the wayPosition
     */
    public Vector3f getWayPosition() {
        return wayPosition;
    }

    /**
     * @param wayPosition the wayPosition to set
     */
    public void setWayPosition(Vector3f wayPosition) {
        this.wayPosition = wayPosition;
    }

    //looks at UserData for the physical position of a spatial.
    private int getPosType() {
        return (int) spatial.getUserData(DataKey.POSITION_TYPE);
    }

    //Sets the physical posType of a spatial.
    private void setPositionType(int posType) {
        spatial.setUserData(DataKey.POSITION_TYPE, posType);
    }

    //Stops the torso channel if playing an animation.
    private void stopTorsoPlaying() {
        spatial.getControl(AnimationControl.class).getTorsoChannel().setTime(
                spatial.getControl(AnimationControl.class).getTorsoChannel().
                        getAnimMaxTime());
    }

    //Stops the feet channel if playing an animation.
    private void stopFeetPlaying() {
        spatial.getControl(AnimationControl.class).getFeetChannel().setTime(
                spatial.getControl(AnimationControl.class).getFeetChannel().
                        getAnimMaxTime());
    }

    //Displays a motion path showing each waypoint. Stays in scene until another 
    //path is set.
    private void showPath() {
        if (motionPath.getNbWayPoints() > 0) {
            motionPath.clearWayPoints();
            motionPath.disableDebugShape();
        }

        for (Path.Waypoint wp : getPath().getWaypoints()) {
            motionPath.addWayPoint(wp.getPosition());
        }
        motionPath.enableDebugShape(app.getAssetManager(), app.getRootNode());
    }

    /**
     * @param target the target to set
     */
    @Override
    public void setTarget(Vector3f target) {
        this.target = target;
    }

}
