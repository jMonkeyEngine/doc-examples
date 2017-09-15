/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.examples.jme3ai.ai;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.terrain.Terrain;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import jme3tools.optimize.GeometryBatchFactory;
import com.jme3.examples.jme3ai.interfaces.DataKey;
import com.jme3.export.binary.BinaryExporter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.critterai.nmgen.IntermediateData;

/**
 * Configuartion file for generating a NavMesh. Can save, load and export a 
 * NavMesh.
 * 
 * @author sploreg
 * @author mitm
 */
public class NavMeshState extends BaseAppState {

    private static final Logger LOG = Logger.getLogger(NavMeshState.class.
            getName());
    private SimpleApplication app;
    private Mesh navMesh;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        //comment out generateNavMesh() and uncomment everything after it to
        //load a exported NavMesh.
        generateNavMesh();
//        Geometry loadNavMesh = loadNavMesh(DataKey.NAVMESH);
//        showGeometry(loadNavMesh, ColorRGBA.Green);
//        saveNavMesh(loadNavMesh.getMesh());
    }

    @Override
    protected void cleanup(Application app) {
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
    }

    //onEnable()/onDisable() can be used for managing things that should 
    //only exist while the state is enabled. Prime examples would be scene 
    //graph attachment or input listener attachment.
    @Override
    protected void onEnable() {
        //Called when the state is fully enabled, ie: is attached and 
        //isEnabled() is true or when the setEnabled() status changes after the 
        //state is attached.
    }

    @Override
    protected void onDisable() {
        //Called when the state was previously enabled but is now disabled 
        //either because setEnabled(false) was called or the state is being 
        //cleaned up.
    }

    @Override
    public void update(float tpf) {
        //TODO: implement behavior during runtime
    }

    private void generateNavMesh() {
        NavMeshGenerator generator = new NavMeshGenerator();
        //The width and depth resolution used when sampling the source geometry. 
        //outdoors = agentRadius/2, indoors = agentRadius/3, cellSize = 
        //agentRadius for very small cells. 
        //Constraints > 0 , default=1
        generator.setCellSize(.25f);
        //The height resolution used when sampling the source geometry.
        //minTraversableHeight, maxTraversableStep, and contourMaxDeviation 
        //will need to be greater than the value of cellHeight in order to 
        //function correctly. maxTraversableStep is especially susceptible to 
        //impact from the value of cellHeight.
        //cellSize/2
        //Constraints > 0, default=1.5
        generator.setCellHeight(.125f);
        //Represents the minimum floor to ceiling height that will still allow 
        //the floor area to be considered traversable.
        //minTraversableHeight should be at least two times the value of 
        //cellHeight in order to get good results. Max spatial height.
        //Constraints > 0, default=7.5
        generator.setMinTraversableHeight(2f);
        //Represents the maximum ledge height that is considered to still be 
        //traversable.
        //maxTraversableStep should be greater than two times cellHeight.
        //Constraints >= 0, default=1
        generator.setMaxTraversableStep(0.3f);
        //The maximum slope that is considered traversable. (In degrees.)
        //Constraints>= 0, default=48
        generator.setMaxTraversableSlope(50.0f);
        //Indicates whether ledges should be considered un-walkable.
        //Constraints None, default=false
        generator.setClipLedges(false);
        //Represents the closest any part of a mesh can get to an obstruction in 
        //the source geometry.
        //traversableAreaBorderSize value must be greater than the cellSize to 
        //have an effect. Radius of the spatial.
        //Constraints >= 0, default=1.2
        generator.setTraversableAreaBorderSize(0.6f);
        //The amount of smoothing to be performed when generating the distance 
        //field used for deriving regions.
        //Constraints >= 0, default=2
        generator.setSmoothingThreshold(0);
        //Applies extra algorithms to help prevent malformed regions from 
        //forming.
        //Constraints None, default=true
        generator.setUseConservativeExpansion(true);
        //The minimum region size for unconnected (island) regions.
        //Constraints > 0, default=3
        generator.setMinUnconnectedRegionSize(8);
        //Any regions smaller than this size will, if possible, be merged with 
        //larger regions.
        //Constraints >= 0, default=10
        generator.setMergeRegionSize(20);
        //The maximum length of polygon edges that represent the border of 
        //meshes.
        //setTraversableAreaBorderSize * 8
        //Constraints >= 0, default=0
        generator.setMaxEdgeLength(4.0f);
        //The maximum distance the edges of meshes may deviate from the source 
        //geometry.
        //1.1 to 1.5 for best results.
        //Constraints >= 0 , default=2.4
        generator.setEdgeMaxDeviation(1.3f);
        //The maximum number of vertices per polygon for polygons generated 
        //during the voxel to polygon conversion process.
        //Constraints >= 3, default=6
        generator.setMaxVertsPerPoly(6);
        //Sets the sampling distance to use when matching the detail mesh to the 
        //surface of the original geometry.
        //Constraints >= 0, default=25
        generator.setContourSampleDistance(5.0f);
        //The maximum distance the surface of the detail mesh may deviate from 
        //the surface of the original geometry.
        //Constraints >= 0, default=25
        generator.setContourMaxDeviation(5.0f);
        //Time allowed before generation process times out in miliseconds.
        //default=10000
        generator.setTimeout(40000);
        
        //the data object to use for storing data related to building the 
        //navigation mesh.
        IntermediateData data = new IntermediateData();
        generator.setIntermediateData(null);

        Mesh mesh = new Mesh();
        GeometryBatchFactory.mergeGeometries(findGeometries(app.getRootNode(),
                new LinkedList<>(), generator), mesh);

        //uncomment to show mesh
//        Geometry meshGeom = new Geometry("MeshGeometry");
//        meshGeom.setMesh(mesh);
//        showGeometry(meshGeom, ColorRGBA.Yellow);
        
        navMesh = generator.optimize(mesh);
        Geometry geom = new Geometry(DataKey.NAVMESH);
        geom.setMesh(navMesh);
        geom.setModelBound(new BoundingBox());
        //display the mesh
        showGeometry(geom, ColorRGBA.Green);
        //saves navMesh to rootNode
        saveNavMesh(navMesh);
        //save the navmesh to user.home for loading 
        exportNavMesh(geom, DataKey.NAVMESH);
    }

    //Gathers all geometries in supplied node into supplied List. Uses 
    //NavMeshGenerator to merge found Terrain meshes into one geometry prior to 
    //adding. Scales and sets translation of merged geometry.
    private List<Geometry> findGeometries(Node node, List<Geometry> geoms,
            NavMeshGenerator generator) {
        for (Iterator<Spatial> it = node.getChildren().iterator(); it.hasNext();) {
            Spatial spatial = it.next();

            if (spatial instanceof Geometry) {
                geoms.add((Geometry) spatial);
            } else if (spatial instanceof Node) {
                if (spatial instanceof Terrain) {
                    Mesh merged = generator.terrain2mesh((Terrain) spatial);
                    Geometry g = new Geometry("mergedTerrain");
                    g.setMesh(merged);
                    g.setLocalScale(spatial.getLocalScale());
                    g.setLocalTranslation(spatial.getLocalTranslation());
                    geoms.add(g);
                } else {
                    findGeometries((Node) spatial, geoms, generator);
                }
            }
        }
        return geoms;
    }

    //NavMesh saves as UserData on the scene's root node
    private void saveNavMesh(Mesh navMesh) {
        app.getRootNode().setUserData(DataKey.NAVMESH, navMesh);
    }

    /**
     * Returns the navMesh if it is in memory, otherwise looks for it in 
     * rootNode UserData.
     *
     * @return the navMesh
     */
    public Mesh getNavMesh() {
        if (navMesh == null) {
            navMesh = findNavMesh();
        }
        return navMesh;
    }

    //looks at userData for navMesh
    private Mesh findNavMesh() {
        Mesh mesh = app.getRootNode().getUserData(DataKey.NAVMESH);
        if (mesh == null) {
            mesh = ((Geometry) app.getRootNode().getChild(DataKey.NAVMESH)).getMesh();
        }
        return mesh;
    }

    //Displays the NavMesh for debugging.
    private void showGeometry(Geometry geom, ColorRGBA color) {
        if (geom.getMaterial() == null) {
            Material mat = new Material(getApplication()
                    .getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            mat.getAdditionalRenderState().setWireframe(true);
            geom.setCullHint(CullHint.Never);
            geom.setMaterial(mat);
        }

        Spatial previous = app.getRootNode().getChild(geom.getName());
        if (previous != null) {
            previous.removeFromParent();
        }
        app.getRootNode().attachChild(geom);
    }

    //Exports the NavMesh to user.home so you can load a saved NavMesh
    private void exportNavMesh(Geometry geom, String fileName) {
        String userHome = System.getProperty("user.home");
        BinaryExporter exporter = BinaryExporter.getInstance();
        File file = new File(userHome + "/NavMesh/" + fileName + ".j3o");
        try {
            exporter.save(geom, file);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error: Failed to save NavMesh!", ex);
        }
    }

    //Loads a saved NavMesh
    private Geometry loadNavMesh(String fileName) {
        String userHome = System.getProperty("user.home");
        app.getAssetManager().registerLocator(userHome, FileLocator.class);
        Geometry geom = (Geometry) app.getAssetManager().loadModel("NavMesh/"
                + fileName + ".j3o");
        return geom;
    }
}
