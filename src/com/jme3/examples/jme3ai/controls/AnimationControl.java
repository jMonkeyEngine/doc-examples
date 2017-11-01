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

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jme3.examples.jme3ai.enums.EnumPosType;
import com.jme3.examples.jme3ai.interfaces.AnimInput;
import com.jme3.examples.jme3ai.interfaces.DataKey;

/**
 * Implements all animations of a spatial by reading the spatials physical 
 posType. Spatial must have AnimControl to use this control.
 *
 * @author mitm
 */
public class AnimationControl extends AbstractControl {

    private AnimChannel feetChannel, torsoChannel;
    private AnimControl animControl;
    private static final Logger LOG = Logger.getLogger(AnimationControl.class.
            getName());
    private int posType;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        
        if (spatial == null) {
            return;
        }

        spatial.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node node) {
                if (node.getControl(AnimControl.class) != null) {
                    animControl = node.getControl(AnimControl.class);
                    animControl.addListener(new AnimationEventListener());
                    feetChannel = animControl.createChannel();
                    torsoChannel = animControl.createChannel();
                }
            }
        });

        if (animControl == null) {
            LOG.log(Level.SEVERE, "No AnimControl {0}", spatial);
            throw new RuntimeException();
        }

        posType = getPosType();
        for (EnumPosType pos : EnumPosType.values()) {
            if (pos.positionType() == posType) {
                switch (pos) {
                    case POS_RUNNING:
                        feetChannel.setAnim(AnimInput.RUN_BASE);
                        feetChannel.setLoopMode(LoopMode.DontLoop);
                        torsoChannel.setAnim(AnimInput.RUN_TOP);
                        torsoChannel.setLoopMode(LoopMode.Loop);
                        //channel.setSpeed(1f);
                        break;
                    default:
                        feetChannel.setAnim(AnimInput.IDLE_BASE);
                        feetChannel.setLoopMode(LoopMode.DontLoop);
                        torsoChannel.setAnim(AnimInput.IDLE_TOP);
                        torsoChannel.setLoopMode(LoopMode.Loop);
                        //channel.setSpeed(1f);
                        break;
                }
            }
        }

    }

    @Override
    protected void controlUpdate(float tpf) {

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        AnimationControl control = new AnimationControl();
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

    //Checks spatial physical posType whenver an animation ends. Sets animation 
    //based off that posType.
    private class AnimationEventListener implements AnimEventListener {

        @Override
        public void onAnimCycleDone(AnimControl control, AnimChannel channel,
                String animName) {
            //posType is set by NavigationControl after game start
            posType = getPosType();

            for (EnumPosType pos : EnumPosType.values()) {
                if (pos.positionType() == posType) {
                    switch (pos) {
                        case POS_RUNNING:
                            if (channel.equals(feetChannel)) {
                                channel.setAnim(AnimInput.RUN_BASE);
                                channel.setLoopMode(LoopMode.Loop);
                            }
                            if (channel.equals(torsoChannel)) {
                                channel.setAnim(AnimInput.RUN_TOP);
                                channel.setLoopMode(LoopMode.Loop);
                            }
                            break;
                        default:
                            if (channel.equals(feetChannel)) {
                                feetChannel.setAnim(AnimInput.IDLE_BASE);
                                feetChannel.setLoopMode(LoopMode.Loop);
                            }
                            if (channel.equals(torsoChannel)) {
                                torsoChannel.setAnim(AnimInput.IDLE_TOP);
                                torsoChannel.setLoopMode(LoopMode.Loop);
                            }
                            break;
                    }
                }
            }
        }

        @Override
        public void onAnimChange(AnimControl control, AnimChannel channel,
                String animName) {

        }
    }

    /**
     * @return the feetChannel
     */
    public AnimChannel getFeetChannel() {
        return feetChannel;
    }

    /**
     * @return the torsoChannel
     */
    public AnimChannel getTorsoChannel() {
        return torsoChannel;
    }

    /**
     *
     * @return spatials physical posType
     */
    private int getPosType() {
        return (int) spatial.getUserData(DataKey.POSITION_TYPE);
    }
}
