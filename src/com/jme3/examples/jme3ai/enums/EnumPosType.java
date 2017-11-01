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
package com.jme3.examples.jme3ai.enums;

/**
 * A physical position with speed settings.
 * 
 * @author mitm
 */
public enum EnumPosType {

    POS_DEAD(0, 0.0f),
    POS_MORTAL(1, 0.0f),
    POS_INCAP(2, 0.0f),
    POS_STUNNED(3, 0.0f),
    POS_SLEEPING(4, 0.0f),
    POS_RESTING(5, 0.0f),
    POS_SITTING(6, 0.0f),
    POS_FIGHTING(7, 0.0f),
    POS_STANDING(8, 0.0f),
    POS_SWIMMING(9, 1.0f),
    POS_WALKING(10, 1.50f),
    POS_RUNNING(11, 3.0f);

    private final float speed;
    private final int position;

    EnumPosType(int position, float speed) {
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
    public int positionType() {
        return position;
    }

}
