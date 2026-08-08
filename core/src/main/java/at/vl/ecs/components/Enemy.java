package at.vl.ecs.components;

import java.util.HashMap;
import java.util.Map;

import com.artemis.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import at.vl.ecs.State;

public class Enemy extends Component {
    public Map<State, Animation<TextureRegion>> animations = new HashMap<>();
    public Map<State, TextureRegion> textures = new HashMap<>();

    // Stats
    public int currentHealth;
    public int maxHealth = 1;

    public float maxSpeed;

    public float detectionRadius;
    public float attackRange;

    public boolean hasDealtDamage = false;
    public float knockbackTimer = 0f;
    public float knockbackDuration;
    public float knockbackStrength;
}
