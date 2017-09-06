/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.examples.jme3ai.appstates;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.examples.jme3ai.interfaces.ListenerKey;
import com.jme3.input.CameraInput;

/**
 * Keyboard mappings.
 * 
 * @author mitm
 */
public class KeyboardRunState extends BaseAppState {
    
    @Override
    protected void initialize(Application app) { 
        app.getInputManager().addMapping(ListenerKey.PAUSE, new KeyTrigger(KeyInput.KEY_F1));
        app.getInputManager().addMapping(ListenerKey.JUMP, new KeyTrigger(KeyInput.KEY_SPACE));
        app.getInputManager().addMapping(ListenerKey.PICK, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        //map ChaseCamera rotation keys, must be called after PCState getHead()
        //because setToggleRotationTrigger clears the triggers before adding new
        //KEY_RIGHT -> clockwise
        app.getInputManager().addMapping(CameraInput.CHASECAM_MOVERIGHT, new KeyTrigger(KeyInput.KEY_RIGHT));
        //KEY_LEFT <- counter clockwise
        app.getInputManager().addMapping(CameraInput.CHASECAM_MOVELEFT, new KeyTrigger(KeyInput.KEY_LEFT));
        app.getInputManager().addMapping(CameraInput.CHASECAM_DOWN, new KeyTrigger(KeyInput.KEY_DOWN));
        app.getInputManager().addMapping(CameraInput.CHASECAM_UP, new KeyTrigger(KeyInput.KEY_UP));
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
    
}
