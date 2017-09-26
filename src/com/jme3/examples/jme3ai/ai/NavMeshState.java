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
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
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
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final NavMesh navMesh = new NavMesh();
    private NavMeshGenerator generator = new NavMeshGenerator();

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        //generate NavMesh
        startGenerator();
        createNavMesh();
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

    private void startGenerator() {
        generator = new NavMeshGenerator();
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
        //Constraints >= 0, default=48
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
        generator.setIntermediateData(data);
    }

    /**
     * creates the nav mesh for the loaded level
     */
    public void createNavMesh() {

        Mesh mesh = new Mesh();
        GeometryBatchFactory.mergeGeometries(findGeometries(app.getRootNode(),
                new LinkedList<>(), generator), mesh);

        //uncomment to show mesh
//        Geometry meshGeom = new Geometry("MeshGeometry");
//        meshGeom.setMesh(mesh);
//        showGeometry(meshGeom, ColorRGBA.Yellow);
//        saveNavMesh(meshGeom);
        
        Mesh optiMesh = generator.optimize(mesh);
        navMesh.loadFromMesh(optiMesh);

        Geometry geom = new Geometry(DataKey.NAVMESH);
        geom.setMesh(optiMesh);
        //display the mesh
        showGeometry(geom, ColorRGBA.Green);
        //save the navmesh to Scenes/NavMesh for loading 
        exportNavMesh(geom, DataKey.NAVMESH);
        //save geom to rootNode if you wish
        saveNavMesh(geom);
    }
    
    private void saveNavMesh(Geometry geom) {
        Spatial previous = app.getRootNode().getChild(geom.getName());
        if (previous != null) {
            previous.removeFromParent();
        }
        app.getRootNode().attachChild(geom);
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

    //Displays the NavMesh for debugging.
    private void showGeometry(Geometry geom, ColorRGBA color) {
        Material mat;
        if (geom.getMaterial() == null) {
            mat = new Material(getApplication()
                    .getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            geom.setMaterial(mat);
        } else {
            mat = geom.getMaterial();
        }
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setWireframe(true);
        geom.setCullHint(CullHint.Never);
    }

    //Exports the NavMesh to user.home so you can load a saved NavMesh
    private void exportNavMesh(Geometry geom, String fileName) {
        String sep = System.getProperty("file.separator");
        Path path = Paths.get("assets" + sep + "Scenes" + sep + "NavMesh");
        BinaryExporter exporter = BinaryExporter.getInstance();
        File file = new File(path + sep + fileName + ".j3o");
        try {
            exporter.save(geom, file);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error: Failed to save NavMesh!", ex);
        }
    }

    /**
     * Get the NavMesh.
     * @return the navMesh
     */
    public NavMesh getNavMesh() {
        return navMesh;
    }
}
