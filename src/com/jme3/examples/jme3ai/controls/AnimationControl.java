/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import com.jme3.examples.jme3ai.enums.EnumPosition;
import com.jme3.examples.jme3ai.interfaces.AnimInput;
import com.jme3.examples.jme3ai.interfaces.DataKey;

/**
 * Implements all animations of a spatial by reading the spatials physical 
 * position. Spatial must have AnimControl to use this control.
 *
 * @author mitm
 */
public class AnimationControl extends AbstractControl {

    private AnimChannel feetChannel, torsoChannel;
    private AnimControl animControl;
    private static final Logger LOG = Logger.getLogger(AnimationControl.class.
            getName());
    private int position;

    public AnimationControl() {

    }

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

        position = getPosition();
        for (EnumPosition pos : EnumPosition.values()) {
            if (pos.position() == position) {
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
    public AnimationControl jmeClone() {
        try {
            return (AnimationControl) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Clone Not Supported", ex);
        }

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

    //Checks spatial physical position whenver an animation ends. Sets animation 
    //based off that position.
    private class AnimationEventListener implements AnimEventListener {

        @Override
        public void onAnimCycleDone(AnimControl control, AnimChannel channel,
                String animName) {
            //position is set by MovementControl after game start
            position = getPosition();

            for (EnumPosition pos : EnumPosition.values()) {
                if (pos.position() == position) {
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
     * @return spatials physical position
     */
    private int getPosition() {
        return (int) spatial.getUserData(DataKey.POSITION);
    }
}
