package at.vl.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import at.vl.ecs.State;
import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Enemy;
import at.vl.ecs.components.Facing;

public class EnemyAnimationSystem extends IteratingSystem  {
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<Enemy> enemyMapper;
    private ComponentMapper<Animator> animatorMapper;
    private ComponentMapper<Facing> facingMapper;


    public EnemyAnimationSystem() {
        super(Aspect.all(Enemy.class, Animator.class));
    }

    @Override
    protected void process(int entityId) {
        Enemy enemy = enemyMapper.get(entityId);
        if(enemy.animations.get(State.WALKING) == null) return;
        if(enemy.textures.get(State.IDLE) == null) return;
        Collider collider = colliderMapper.get(entityId);
        Facing facing = facingMapper.get(entityId);
        Animator animator = animatorMapper.get(entityId);

        animator.stateTime += world.getDelta();

        switch(animator.currentState) {
            case IDLE:
                animator.currentFrame = enemy.textures.get(State.IDLE);
                break;

            case WALKING:
                animator.currentFrame = enemy.animations.get(State.WALKING).getKeyFrame(animator.stateTime, true);
                break;

            default:
                animator.currentFrame = enemy.textures.get(State.IDLE);
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
