package at.vl.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Projectile;
import at.vl.ecs.components.RigidBody;
import at.vl.util.JsonHelper;

public class ProjectileAnimationSystem extends IteratingSystem {
    private ComponentMapper<Animator> animatorMapper;

    private final Animation<TextureRegion> PROJECTILE_ANIMATION;

    public ProjectileAnimationSystem() {
        super(Aspect.all(Projectile.class, Collider.class, RigidBody.class));

        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("player/Bullet.atlas"));
        Array<TextureAtlas.AtlasRegion> frames = atlas.getRegions();
        PROJECTILE_ANIMATION = new Animation<>(1f / JsonHelper.getConfigValue().getFloat("ProjectileAnimationSpeed"), frames, Animation.PlayMode.LOOP);
    }

    @Override
    protected void process(int entityId) {
        Animator animator = animatorMapper.get(entityId);

        animator.stateTime += world.getDelta();
        animator.currentFrame = PROJECTILE_ANIMATION.getKeyFrame(animator.stateTime, true);
    }
}
