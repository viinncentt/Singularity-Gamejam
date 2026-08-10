package at.vl.ecs.components;

import com.artemis.Component;

public class Player extends Component {
    public Player() {}

    public int currentHealth;
    public int maxHealth;
    public boolean readyToRespawn;
    public boolean dying;
    public boolean hurting;

    public boolean filled = false;
}
