package cz.absolutno.sifry.common.widget;

import android.view.animation.AnimationUtils;

final class BottomBarFlinger {

    private static final float SPEED = 1E-6f;
    private static final float SPEED2 = 1E-2f;
    private static final float EPSILON = 0.01f;
    private static final float MAX_DX = 0.3f;

    private boolean finished;
    private boolean dragged;
    private float velX;
    private float curX;
    private long lastT;
    private boolean gotoMode;

    private final float minX;
    private final float maxX;
    private float lastX;

    public BottomBarFlinger(float minX, float maxX, float curX) {
        this.minX = minX;
        this.maxX = maxX;
        this.curX = curX;
        lastX = curX;
        velX = 0;
        finished = true;
        dragged = false;
        gotoMode = false;
    }

    public boolean isFinished() {
        return finished;
    }

    public void fling(float velX) {
        this.velX = velX;
        finished = false;
        dragged = false;
        if (curX < minX) {
            lastX = minX;
            gotoMode = true;
        } else if (curX > maxX) {
            lastX = maxX;
            gotoMode = true;
        } else
            gotoMode = false;
        lastT = AnimationUtils.currentAnimationTimeMillis();
        bound();
    }

    public void drag(float dx) {
        this.curX += dx;
        this.velX = 0;
        finished = false;
        dragged = true;
        gotoMode = false;
    }

    public void goTo(int which) {
        this.velX = 0;
        lastX = which;
        gotoMode = true;
        finished = false;
        dragged = false;
        lastT = AnimationUtils.currentAnimationTimeMillis();
    }

    private void bound() {
        if (Math.abs(curX - lastX) < EPSILON) {
            curX = lastX;
            finished = true;
        }
        if (Math.abs(curX - lastX) > 1 - EPSILON && !gotoMode) {
            lastX += Math.signum(curX - lastX);
            if (lastX >= minX && lastX <= maxX) {
                curX = lastX;
                finished = true;
            } else
                lastX -= Math.signum(curX - lastX);
        }
        if (curX < minX) {
            lastX = minX;
            gotoMode = true;
        } else if (curX > maxX) {
            lastX = maxX;
            gotoMode = true;
        }
    }

    public float compute() {
        long time = AnimationUtils.currentAnimationTimeMillis();
        if (finished || dragged)
            return curX;

        int dt = (int) (time - lastT);
        if (gotoMode) {
            float px = curX - lastX;
            velX = -(px + Math.signum(px) * EPSILON / 2) * SPEED2;
            if (Math.abs(velX * dt) > MAX_DX)
                velX = Math.signum(velX) * MAX_DX / dt;
            curX += velX * dt;
        } else {
            float px = curX - (float) Math.floor(curX);
            velX += (2 * px - 1) * (1 - velX * velX / ((px + EPSILON) * (1 + EPSILON - px))) * SPEED * dt;
            float ps = Math.signum(curX - lastX);
            curX += velX * dt;
            if (Math.signum(curX - lastX) != ps)
                curX = lastX;
        }
        lastT = time;
        bound();
        //android.util.Log.d(gotoMode?"goto":"comp", Float.toString(curX));
        return curX;
    }
}
