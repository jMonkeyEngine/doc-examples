/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.examples.jme3ai.enums;

/**
 * A physical position with speed settings.
 * 
 * @author mitm
 */
public enum EnumPosition {

    POS_DEAD(0, 0.0f),
    POS_MORTAL(1, 0.0f),
    POS_INCAP(2, 0.0f),
    POS_STUNNED(3, 0.0f),
    POS_SLEEPING(4, 0.0f),
    POS_RESTING(5, 0.0f),
    POS_SITTING(6, 0.0f),
    POS_FIGHTING(7, 0.0f),
    POS_STANDING(8, 0.0f),
    POS_SWIMMING(9, 1.5f),
    POS_WALKING(10, 3.0f),
    POS_RUNNING(11, 6.0f);

    private final float speed;
    private final int position;

    EnumPosition(int position, float speed) {
        this.speed = speed;
        this.position = position;
    }

    /**
     * @return the speed
     */
    public float speed() {
        return speed;
    }

    /**
     * @return the position
     */
    public int position() {
        return position;
    }

}
