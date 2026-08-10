package at.vl.systems;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import at.vl.ecs.State;
import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Enemy;
import at.vl.ecs.components.Facing;
import at.vl.ecs.components.IgnoreGravity;
import at.vl.ecs.components.Player;
import at.vl.ecs.components.RigidBody;
import at.vl.util.JsonHelper;

public class SpawningSystem extends BaseSystem {
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<RigidBody> rigidBodyMapper;

    public void spawnPlayer(float x, float y) {
        Entity entity = world.createEntity();
        EntityEdit edit = entity.edit();

        Collider collider = edit.create(Collider.class);
        collider.rect.set(x, y, 0.65f, 0.9f);

        RigidBody rigidBody = edit.create(RigidBody.class);
        rigidBody.knockbackDuration = JsonHelper.getConfigValue().getFloat("PlayerKnockbackDuration");

        // Player
        Player player = edit.create(Player.class);
        player.maxHealth = 2;
        player.currentHealth = player.maxHealth;

        // Animator
        Animator animator = edit.create(Animator.class);
        animator.currentState = State.IDLE;

        // Facing
        Facing facing = edit.create(Facing.class);
        facing.lookingRight = true;

    }

    public void spawnEnemy(float x, float y, float width, float height) {
        Entity entity = world.createEntity();
        EntityEdit edit = entity.edit();

        Collider collider = edit.create(Collider.class);
        collider.rect.set(x, y, width, height);

        RigidBody rigidbody = edit.create(RigidBody.class);
        rigidbody.velocity.set(0f, 0f);

        // Animator
        Animator animator = edit.create(Animator.class);
        animator.currentState = State.IDLE;

        // Facing
        Facing facing = edit.create(Facing.class);
        facing.lookingRight = true;

        // Enemy
        Enemy enemy = edit.create(Enemy.class);
        enemy.maxHealth = 3;
        enemy.currentHealth = enemy.maxHealth;
    }

    public void spawnUndefinedMass(float x, float y, UndefinedMassType type) {
        Entity entity = world.createEntity();
        EntityEdit edit = entity.edit();

        Collider collider = edit.create(Collider.class);
        collider.rect.set(x, y, 1f, 1f);

        RigidBody rigidbody = edit.create(RigidBody.class);
        rigidbody.velocity.set(0f, 0f);
        rigidbody.knockbackDuration = 0.25f;

        // Animator
        Animator animator = edit.create(Animator.class);
        animator.currentState = State.IDLE;

        // Facing
        Facing facing = edit.create(Facing.class);
        facing.lookingRight = true;

        // Ignore Gravity
        switch (type) {
            case NORMAL:
                IgnoreGravity ignoreGravity = edit.create(IgnoreGravity.class);

                // Enemy
                Enemy enemy = edit.create(Enemy.class);
                enemy.maxHealth = 3;
                enemy.currentHealth = enemy.maxHealth;
                enemy.maxSpeed = JsonHelper.getConfigValue().getFloat("UndefinedMassSpeed");

                enemy.detectionRadius = JsonHelper.getConfigValue().getFloat("UndefinedMassDetectionRadius");
                enemy.attackRange = 1f;

                enemy.knockbackStrength = JsonHelper.getConfigValue().getFloat("UndefinedMassKnockbackStrength");

                TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("enemies/enemy1/Walking.atlas"));
                Array<TextureAtlas.AtlasRegion> frames = atlas.getRegions();
                Animation WALKING_ANIMATION = new Animation<>(1f / JsonHelper.getConfigValue().getFloat("UndefinedMassAnimationSpeed") , frames, Animation.PlayMode.LOOP);
                enemy.animations.put(State.WALKING, WALKING_ANIMATION);

                Texture temp = new Texture(Gdx.files.internal("enemies/enemy1/Idle.png"));
                enemy.textures.put(State.IDLE, new TextureRegion(temp));
                break;
            case RED:
                IgnoreGravity ignoreGravity2 = edit.create(IgnoreGravity.class);

                // Enemy
                Enemy enemy2 = edit.create(Enemy.class);
                enemy2.maxHealth = 3;
                enemy2.currentHealth = enemy2.maxHealth;
                enemy2.maxSpeed = JsonHelper.getConfigValue().getFloat("RedUndefinedMassSpeed");

                enemy2.detectionRadius = JsonHelper.getConfigValue().getFloat("RedUndefinedMassDetectionRadius");
                enemy2.attackRange = 1f;

                enemy2.knockbackStrength = JsonHelper.getConfigValue().getFloat("RedUndefinedMassKnockbackStrength");

                TextureAtlas atlas2 = new TextureAtlas(Gdx.files.internal("enemies/enemy2/Walking.atlas"));
                Array<TextureAtlas.AtlasRegion> frames2 = atlas2.getRegions();
                Animation WALKING_ANIMATION2 = new Animation<>(1f / JsonHelper.getConfigValue().getFloat("UndefinedMassAnimationSpeed") , frames2, Animation.PlayMode.LOOP);
                enemy2.animations.put(State.WALKING, WALKING_ANIMATION2);

                Texture temp2 = new Texture(Gdx.files.internal("enemies/enemy2/Idle.png"));
                enemy2.textures.put(State.IDLE, new TextureRegion(temp2));
                break;
            case BLUE:
                IgnoreGravity ignoreGravity3 = edit.create(IgnoreGravity.class);

                // Enemys
                Enemy enemy3 = edit.create(Enemy.class);
                enemy3.maxHealth = 3;
                enemy3.currentHealth = enemy3.maxHealth;
                enemy3.maxSpeed = JsonHelper.getConfigValue().getFloat("BlueUndefinedMassSpeed");

                enemy3.detectionRadius = JsonHelper.getConfigValue().getFloat("BlueUndefinedMassDetectionRadius");
                enemy3.attackRange = 1f;

                enemy3.knockbackStrength = JsonHelper.getConfigValue().getFloat("BlueUndefinedMassKnockbackStrength");
                enemy3.shootRange = JsonHelper.getConfigValue().getFloat("BlueUndefinedMassShootingRange");

                enemy3.projectileSpeed = JsonHelper.getConfigValue().getFloat("BlueUndefinedMassProjectileSpeed");
                enemy3.shootCooldown = JsonHelper.getConfigValue().getFloat("BlueUndefinedMassShootingCooldown");
                enemy3.maxProjectiles = JsonHelper.getConfigValue().getInt("BlueUndefinedMassProjectileAmount");

                TextureAtlas atlas3 = new TextureAtlas(Gdx.files.internal("enemies/enemy3/Walking.atlas"));
                Array<TextureAtlas.AtlasRegion> frames3 = atlas3.getRegions();
                Animation WALKING_ANIMATION3 = new Animation<>(1f / JsonHelper.getConfigValue().getFloat("UndefinedMassAnimationSpeed") , frames3, Animation.PlayMode.LOOP);
                enemy3.animations.put(State.WALKING, WALKING_ANIMATION3);

                Texture temp3 = new Texture(Gdx.files.internal("enemies/enemy3/Idle.png"));
                enemy3.textures.put(State.IDLE, new TextureRegion(temp3));
                break;
            default:
                break;
        }

    }

    public void spawnGround(float x, float y, float width, float height) {
        Entity entity = world.createEntity();
        EntityEdit edit = entity.edit();

        Collider collider = edit.create(Collider.class);
        collider.rect.set(x, y, width, height);

    }

    @Override
    protected void processSystem() {

    }
}
