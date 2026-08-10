package at.vl.ecs.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.artemis.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

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
    public float shootRange = 5;

    public float shootTimer = 0f;      // counts down; fires when <= 0
    public float shootCooldown = 0.5f;   // seconds between shots — tune via config like your other fields
    public float projectileSpeed = 5f;
    public float projectileMaxDistance = 15f;
    public float projectileWidth = 0.3f;
    public float projectileHeight = 0.3f;

    public int maxProjectiles = 0;

    public boolean hasDealtDamage = false;
    public float knockbackStrength;

    public float lastDirection = 1f;
    public float lastDirectionY = 1f;

}
