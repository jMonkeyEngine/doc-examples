/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Spline;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jme3.examples.jme3ai.enums.EnumPosition;
import com.jme3.examples.jme3ai.interfaces.DataKey;
import com.jme3.examples.jme3ai.interfaces.ListenerKey;

/**
 * @literal
 * MovementControl implements pathfinding. When target != null, navi will
 * calculate a newpath.
 *
 * controlUpdate continuously checks the path for two or more waypoints. If path
 * contains enough waypoints, moves the path position to the next wayPoint, sets
 * wayPosition and spatials physical position.
 *
 * If wayPosition != null, calculates the distance from current spatial world
 * position to the wayPosition. If distance > value, sets the viewDirection of
 * PCControl by subtracting spatial world position from wayPosition, then sets
 * PCControl forward variable to true. If distance < value, nulls wayPosition.
 *
 * If wayPosition = null, sets spatials physical position and PCControl forward 
 * variableto false.
 *
 * @author mitm
 */
public class MovementControl extends AbstractControl {

    private static final Logger LOG = Logger.getLogger(MovementControl.class.
            getName());
    private ScheduledExecutorService executor;
    private Vector3f target;
    private boolean pathfinding;
    private Vector3f wayPosition;
    private NavMeshPathfinder navi;
    private final SimpleApplication app;
    private final boolean debug;
    private MotionPath motionPath;
    private boolean showPath;

    /**
     * Constructor for MovementControl. Grabs a Mesh from memory or rootNode, 
     * creates a NavMesh from that Mesh, then starts the NavMeshPathFinder 
     * executor service using the NavMesh.
     * 
     * @param app reference to Application
     * @param debug show motion path for debugging
     */
    public MovementControl(Application app, Boolean debug) {
        this.app = (SimpleApplication) app;

        if (debug) {
            motionPath = new MotionPath();
            motionPath.setPathSplineType(Spline.SplineType.Linear);
        }
        this.debug = debug;
        this.wayPosition = null;
        this.target = null;
        Mesh mesh = getMesh();
        NavMesh navMesh = new NavMesh(mesh);
        executor = Executors.newScheduledThreadPool(1);
        startPathFinder(navMesh);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial == null) {
            shutdownAndAwaitTermination(executor);
        }
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        Vector3f spatialPosition = spatial.getWorldTranslation();

        if (getWayPosition() != null) {
            Vector2f aiPosition = new Vector2f(spatialPosition.x,
                    spatialPosition.z);
            Vector2f waypoint2D = new Vector2f(getWayPosition().x,
                    getWayPosition().z);
            float distance = aiPosition.distance(waypoint2D);

            //move char between waypoints untill waypoint reached then set null
            if (distance > 0.25f) {
                Vector2f direction = waypoint2D.subtract(aiPosition);
                direction.mult(tpf);
                getPCControl().setViewDirection(new Vector3f(direction.x, 0,
                        direction.y).normalize());
                getPCControl().onAction(ListenerKey.MOVE_FORWARD, true, 1);
            } else {
                setWayPosition(null);
            }
        } else if (!isPathfinding() && getNavi().getNextWaypoint() != null
                && !getNavi().isAtGoalWaypoint()) {
            //called from the update loop
            if (showPath) {
                showPath();
                showPath = false;
            }

            //advance to next waypoint 
            getNavi().goToNextWaypoint();
            setWayPosition(new Vector3f(getNavi().getWaypointPosition()));

            //set spatial physical position
            if (getPosition() == EnumPosition.POS_STANDING.position()) {
                setPosition(EnumPosition.POS_RUNNING.position());
                stopFeetPlaying();
                stopTorsoPlaying();
            }
        } else {
            //waypoint null so stop moving and set spatials physical position
            if (getPosition() == EnumPosition.POS_RUNNING.position()) {
                setPosition(EnumPosition.POS_STANDING.position());
                stopFeetPlaying();
                stopTorsoPlaying();
            }
            getPCControl().onAction(ListenerKey.MOVE_FORWARD, false, 1);
        }
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        MovementControl control = new MovementControl(app, false);
        control.setSpatial(spatial);
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        //TODO: load properties of this Control, e.g.
        //this.value = in.readFloat("name", defaultValue);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
        //TODO: save properties of this Control, e.g.
        //out.write(this.value, "name", defaultValue);
    }
    
    /**
     * Sets the desired goal of the path.
     * @param target the end point of the path
     */
    public void setTarget(Vector3f target) {
        this.target = target;
    }
    
    /**
     * 
     * @return whether or not pathfinder is busy
     */
    public boolean isPathfinding() {
        return pathfinding;
    }
    
    /**
     * @return the NavMeshPathFinder
     */
    public NavMeshPathfinder getNavi() {
        return navi;
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
    
    /**
     * @return the PCControl
     */
    public PCControl getPCControl() {
        return spatial.getControl(PCControl.class);
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
    
    //Displays a motion path showing each waypoint. Stays in scene until another 
    //path is set.
    private void showPath() {
        if (motionPath.getNbWayPoints() > 0) {
            motionPath.clearWayPoints();
            motionPath.disableDebugShape();
        }

        for (Path.Waypoint wp : getNavi().getPath().getWaypoints()) {
            motionPath.addWayPoint(wp.getPosition());
        }
        motionPath.enableDebugShape(app.getAssetManager(), app.getRootNode());
    }

    //Computes a path using the A* algorithm. Every 1/2 second checks target
    //for processing. Path will remain untill a new path is generated.
    private void startPathFinder(NavMesh navMesh) {
        navi = new NavMeshPathfinder(navMesh);
        executor.scheduleWithFixedDelay(() -> {
            if (target != null) {
                pathfinding = true;
                //setPosition must be set before computePath is called.
                navi.setPosition(getSpatial().getWorldTranslation());
                //computePath() adds the target to the end of the path.
                //computePath() adds one endpoint to the cell nearest target 
                //only if you are not in the goalCell.
                //If inside goalCell, computePath() will do a direct line of 
                //sight placement of target. 
                //This all means endpoint is always outside the navMesh when 
                //target is outside navMesh. 
                //warpInside(target) moves endpoint within the navMesh always.
                //Look at forcePointToCellColumn(Vector2f point) in Cell.java 
                //inside the Jme3AI Library and Modify 
                //PathDirection = PathDirection.mult(0.9f) 
                //to decrease endpoint distance to a cell edge with 1 = edge.
                navi.warpInside(target);
                System.out.println("Target " + target);
                boolean success;
                //comput the path
                success = navi.computePath(target);
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

    //getNavMesh() looks first for the Mesh in memory, and if not found, looks 
    //for it in rootNode UserData.
    private Mesh getMesh() {
        return app.getStateManager().getState(NavMeshState.class).getNavMesh();
    }

    //looks at UserData for the physical position of a spatial.
    private int getPosition() {
        return (int) spatial.getUserData(DataKey.POSITION);
    }

    //Sets the physical position of a spatial.
    private void setPosition(int position) {
        spatial.setUserData(DataKey.POSITION, position);
    }
    
}
