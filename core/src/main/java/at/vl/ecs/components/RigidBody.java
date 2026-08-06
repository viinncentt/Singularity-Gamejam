package at.vl.ecs.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

public class RigidBody extends Component {
    public Vector2 velocity = new Vector2();
    public boolean movedX;
    public boolean movedY;
    public boolean grounded;
    public boolean fasterGravity;
}
