/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.examples.jme3ai.controls;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.examples.jme3ai.enums.EnumPosition;
import com.jme3.examples.jme3ai.interfaces.DataKey;
import com.jme3.examples.jme3ai.interfaces.ListenerKey;

/**
 * Controls the spatials movement. Speed is derived from EnumPosition.
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
        Vector3f modelForwardDir = spatial.getWorldRotation().mult(Vector3f.UNIT_Z);  
        walkDirection.set(0, 0, 0);
        if (forward) {
            position = getPosition();
            for (EnumPosition pos : EnumPosition.values()) {
                if (pos.position() == position) {
                    switch (pos) {
                        case POS_RUNNING:
                            moveSpeed = EnumPosition.POS_RUNNING.speed();
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
        if (name.equals(ListenerKey.JUMP)) {
            jump();
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
    private int getPosition() {
        return (int) spatial.getUserData(DataKey.POSITION);
    }

}
