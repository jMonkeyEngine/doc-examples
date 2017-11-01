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
package com.jme3.examples.jme3ai.controls;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.examples.jme3ai.enums.EnumPosType;
import com.jme3.examples.jme3ai.interfaces.DataKey;
import com.jme3.examples.jme3ai.interfaces.ListenerKey;

/**
 * Controls the spatials movement. Speed is derived from EnumPosType.
 * 
 * @author mitm
 */
public class PCControl extends BetterCharacterControl implements ActionListener {

    private boolean forward;
    private float moveSpeed;
    private int position;

    public PCControl(float radius, float height, float mass) {
        super(radius, height, mass);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        this.moveSpeed = 0;  
        walkDirection.set(0, 0, 0);
        if (forward) {
            Vector3f modelForwardDir = spatial.getWorldRotation().mult(Vector3f.UNIT_Z);
            position = getPositionType();
            for (EnumPosType pos : EnumPosType.values()) {
                if (pos.positionType() == position) {
                    switch (pos) {
                        case POS_RUNNING:
                            moveSpeed = EnumPosType.POS_RUNNING.speed();
                            break;                            
                        default:
                            moveSpeed = 0f;
                            break;
                    }
                }
            }
            walkDirection.addLocal(modelForwardDir.mult(moveSpeed));
        }
        setWalkDirection(walkDirection);
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(ListenerKey.MOVE_FORWARD)) {
            forward = isPressed;
        }
    }
    
    //need to overide because we extended BetterCharacterControl
    @Override
    public PCControl cloneForSpatial(Spatial spatial) {
        try {
            PCControl control = (PCControl) super.clone();
            control.setSpatial(spatial); 
            return control;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Clone Not Supported", ex);
        }
    }
    
    //need to override because we extended BetterCharacterControl
    @Override
    public PCControl jmeClone() {
        try {
            return (PCControl) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Clone Not Supported", ex);
        }
    }
    
    //gets the physical position of spatial
    private int getPositionType() {
        return (int) spatial.getUserData(DataKey.POSITION_TYPE);
    }

}
