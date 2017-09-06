package com.jme3.examples.jme3ai;

import com.jme3.examples.jme3ai.appstates.TerrainState;
import com.jme3.examples.jme3ai.ai.NavMeshState;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.renderer.RenderManager;
import com.jme3.examples.jme3ai.appstates.KeyboardRunState;
import com.jme3.examples.jme3ai.appstates.PCState;

/**
 * This is the Jme3AI Class of your Game. You should only do initialization
 * here. Move your Logic into AppStates or Controls
 *
 * @author mitm
 */
public class Jme3AI extends SimpleApplication {

    public Jme3AI() {
        super(new StatsAppState(), new DebugKeysAppState(), new TerrainState(),
                new NavMeshState(), new PCState(), new KeyboardRunState());
    }

    public static void main(String[] args) {
        Jme3AI app = new Jme3AI();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //create PhysicsSpace
        stateManager.attach(new BulletAppState());
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

}
