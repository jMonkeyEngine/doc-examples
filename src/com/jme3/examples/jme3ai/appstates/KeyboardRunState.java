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
        app.getInputManager().addMapping(ListenerKey.PAUSE, new KeyTrigger(
                KeyInput.KEY_F1));
        app.getInputManager().addMapping(ListenerKey.PICK,
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        //map ChaseCamera rotation keys, must be called after PCState getHead()
        //because setToggleRotationTrigger clears the triggers before adding new
        //KEY_RIGHT -> clockwise
        app.getInputManager().addMapping(CameraInput.CHASECAM_MOVERIGHT,
                new KeyTrigger(KeyInput.KEY_RIGHT));
        //KEY_LEFT <- counter clockwise
        app.getInputManager().addMapping(CameraInput.CHASECAM_MOVELEFT,
                new KeyTrigger(KeyInput.KEY_LEFT));
        app.getInputManager().addMapping(CameraInput.CHASECAM_DOWN,
                new KeyTrigger(KeyInput.KEY_DOWN));
        app.getInputManager().addMapping(CameraInput.CHASECAM_UP,
                new KeyTrigger(KeyInput.KEY_UP));
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
