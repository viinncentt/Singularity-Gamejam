package at.vl.player;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import at.vl.Main;
import at.vl.ecs.components.Animator;

import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Facing;
import at.vl.ecs.components.Player;
import at.vl.ecs.components.RigidBody;
import at.vl.util.JsonHelper;

public class PlayerAnimationSystem extends IteratingSystem {
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<RigidBody> rigidBodyMapper;
    private ComponentMapper<Animator> animatorMapper;
    private ComponentMapper<Facing> facingMapper;

    private Collider collider;
    private RigidBody rb;
    private Facing facing;

    private final TextureRegion IDLE_ANIMATION;
    private final Animation<TextureRegion> WALKING_ANIMATION;
    private final TextureRegion JUMP_ANIMATION;
    private final TextureRegion FALL_ANIMATION;
    private final TextureRegion LAND_ANIMATION;
    private final Animation<TextureRegion> SUCKING_ANIMATION;
    private final Animation<TextureRegion> SHOOTING_ANIMATION;

    public PlayerAnimationSystem() {
        super(Aspect.all(Player.class, Collider.class, RigidBody.class, Animator.class, Facing.class));

        // Animations
        // Idle
        Texture temp = new Texture(Gdx.files.internal("player/PlayerIdle.png"));
        IDLE_ANIMATION = new TextureRegion(temp);

        // Walking
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("player/Walking.atlas"));
        Array<TextureAtlas.AtlasRegion> frames = atlas.getRegions();
        WALKING_ANIMATION = new Animation<>(1f / JsonHelper.getConfigValue().getFloat("PlayerWalkingAnimationSpeed") , frames, Animation.PlayMode.LOOP);

        // Jump
        temp = new Texture(Gdx.files.internal("player/PlayerJump.png"));
        JUMP_ANIMATION = new TextureRegion(temp);

        // Fall
        temp = new Texture(Gdx.files.internal("player/PlayerFall.png"));
        FALL_ANIMATION = new TextureRegion(temp);

        // Land
        temp = new Texture(Gdx.files.internal("player/PlayerLand.png"));
        LAND_ANIMATION = new TextureRegion(temp);

        // Sucking
        atlas = new TextureAtlas(Gdx.files.internal("player/Sucking.atlas"));
        frames = atlas.getRegions();
        SUCKING_ANIMATION = new Animation<>(1f / JsonHelper.getConfigValue().getFloat("PlayerSuckingAnimationSpeed") , frames, Animation.PlayMode.LOOP);

        // Shooting
        atlas = new TextureAtlas(Gdx.files.internal("player/Shooting.atlas"));
        frames = atlas.getRegions();
        SHOOTING_ANIMATION = new Animation<>(1f / JsonHelper.getConfigValue().getFloat("PlayerShootingAnimationSpeed") , frames, Animation.PlayMode.LOOP);
    }

    @Override
    protected void process(int entityId) {
        collider = colliderMapper.get(entityId);
        rb = rigidBodyMapper.get(entityId);
        facing = facingMapper.get(entityId);
        Animator animator = animatorMapper.get(entityId);

        animator.stateTime += world.getDelta();

        switch(animator.currentState) {
            case IDLE:
                animator.currentFrame = IDLE_ANIMATION;
                break;

            case WALKING:
                animator.currentFrame = WALKING_ANIMATION.getKeyFrame(animator.stateTime, true);
                break;

            case JUMPING:
                animator.currentFrame = JUMP_ANIMATION;
                break;

            case FALLING:
                animator.currentFrame = FALL_ANIMATION;
                break;

            case LANDING:
                animator.currentFrame = LAND_ANIMATION;
                break;

            case SUCKING:
                animator.currentFrame = SUCKING_ANIMATION.getKeyFrame(animator.stateTime, true);
                break;

            case SHOOTING:
                animator.currentFrame = SHOOTING_ANIMATION.getKeyFrame(animator.stateTime, true);
                break;

            default:
                break;
        }

        flip(animator.currentFrame, facing.lookingRight);
    }

    private void flip(TextureRegion frame, boolean lookingRight) {
        boolean shouldBeFlipped = !lookingRight;
        if (frame.isFlipX() != shouldBeFlipped) {
            frame.flip(true, false);
        }
    }
}
