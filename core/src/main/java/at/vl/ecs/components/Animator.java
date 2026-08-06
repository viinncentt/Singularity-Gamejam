package at.vl.ecs.components;

import com.artemis.Component;

import at.vl.ecs.State;

public class Animator extends Component {
    public State currentState;
    public float stateTime;
}
