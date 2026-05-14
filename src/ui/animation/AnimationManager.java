package ui.animation;

import model.Position;
import java.util.*;

/**
 * 统一的动画管理器
 * 管理所有游戏内的动画效果：消除、连线、缩放、渐隐等
 */
public class AnimationManager {

    // 动画类型
    public enum AnimationType {
        FADE_OUT, // 渐隐
        SCALE, // 缩放
        LINE_DRAW, // 连线绘制
        DROP // 掉落
    }

    // 单个动画的状态
    private static class AnimationState {
        AnimationType type;
        long startTime;
        long duration;
        Object data; // 存储动画相关数据

        AnimationState(AnimationType type, long duration, Object data) {
            this.type = type;
            this.duration = duration;
            this.data = data;
            this.startTime = System.currentTimeMillis();
        }

        float getProgress() {
            long elapsed = System.currentTimeMillis() - startTime;
            return Math.min(1f, elapsed / (float) duration);
        }

        boolean isFinished() {
            return getProgress() >= 1f;
        }
    }

    private Map<String, AnimationState> activeAnimations = new HashMap<>();
    private Set<String> finishedAnimations = new HashSet<>();

    /**
     * 启动消除动画
     */
    public void startFadeOutAnimation(Position pos, long duration) {
        String key = "fadeOut_" + pos.getRow() + "_" + pos.getCol();
        activeAnimations.put(key, new AnimationState(AnimationType.FADE_OUT, duration, pos));
    }

    /**
     * 启动缩放动画
     */
    public void startScaleAnimation(Position pos, long duration) {
        String key = "scale_" + pos.getRow() + "_" + pos.getCol();
        activeAnimations.put(key, new AnimationState(AnimationType.SCALE, duration, pos));
    }

    /**
     * 启动连线绘制动画
     */
    public void startLineDrawAnimation(long duration) {
        activeAnimations.put("lineDraw", new AnimationState(AnimationType.LINE_DRAW, duration, null));
    }

    /**
     * 启动掉落动画
     */
    public void startDropAnimation(long duration) {
        activeAnimations.put("drop", new AnimationState(AnimationType.DROP, duration, null));
    }

    /**
     * 获取棋子消除动画进度 [0, 1]
     */
    public float getFadeOutProgress(Position pos) {
        String key = "fadeOut_" + pos.getRow() + "_" + pos.getCol();
        AnimationState state = activeAnimations.get(key);
        return state != null ? state.getProgress() : 0f;
    }

    /**
     * 检查棋子是否正在消除动画中
     */
    public boolean isFadingOut(Position pos) {
        String key = "fadeOut_" + pos.getRow() + "_" + pos.getCol();
        AnimationState state = activeAnimations.get(key);
        return state != null && !state.isFinished();
    }

    /**
     * 获取缩放动画的缩放因子 [0, 1]
     */
    public float getScaleFactor(Position pos) {
        String key = "scale_" + pos.getRow() + "_" + pos.getCol();
        AnimationState state = activeAnimations.get(key);
        if (state == null)
            return 1f;

        float progress = state.getProgress();
        // 先放大再缩小的效果
        if (progress < 0.5f) {
            return 1f + progress * 0.2f; // 放大到 1.1x
        } else {
            return 1.1f - (progress - 0.5f) * 0.2f; // 缩回到 1.0x
        }
    }

    /**
     * 获取选中棋子的缩放因子
     */
    public float getSelectedScaleFactor(Position pos) {
        String key = "scale_" + pos.getRow() + "_" + pos.getCol();
        AnimationState state = activeAnimations.get(key);
        return state != null ? getScaleFactor(pos) : 1f;
    }

    /**
     * 获取连线绘制动画进度
     */
    public float getLineDrawProgress() {
        AnimationState state = activeAnimations.get("lineDraw");
        return state != null ? state.getProgress() : 1f;
    }

    /**
     * 获取掉落动画进度
     */
    public float getDropProgress() {
        AnimationState state = activeAnimations.get("drop");
        return state != null ? state.getProgress() : 1f;
    }

    /**
     * 更新所有动画状态
     */
    public void update() {
        finishedAnimations.clear();

        Iterator<Map.Entry<String, AnimationState>> iterator = activeAnimations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AnimationState> entry = iterator.next();
            if (entry.getValue().isFinished()) {
                finishedAnimations.add(entry.getKey());
                iterator.remove();
            }
        }
    }

    /**
     * 检查是否有动画完成
     */
    public boolean hasAnimationFinished(String key) {
        return finishedAnimations.contains(key);
    }

    /**
     * 检查是否有任何活动的动画
     */
    public boolean hasActiveAnimation() {
        return !activeAnimations.isEmpty();
    }

    /**
     * 清除所有动画
     */
    public void clearAll() {
        activeAnimations.clear();
        finishedAnimations.clear();
    }
}
