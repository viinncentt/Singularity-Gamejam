package at.vl.ecs.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import at.vl.ecs.State;

public class Animator extends Component {
    public State currentState;
    public TextureRegion currentFrame;
    public float stateTime;
    public TextureRegion effectsFrame;
    public float dyingStateTime;
}
