package ui.animation;

/**
 * 动画工具类
 * 提供各种缓动函数和动画计算方法
 */
public class AnimationUtils {

    /**
     * 线性插值
     */
    public static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    /**
     * 缓进缓出（平滑）- 用于一般动画
     */
    public static float easeInOutQuad(float progress) {
        return progress < 0.5f
                ? 2 * progress * progress
                : 1 - (float) Math.pow(-2 * progress + 2, 2) / 2;
    }

    /**
     * 缓进 - 开始较慢
     */
    public static float easeInQuad(float progress) {
        return progress * progress;
    }

    /**
     * 缓出 - 结束较慢
     */
    public static float easeOutQuad(float progress) {
        return 1 - (1 - progress) * (1 - progress);
    }

    /**
     * 弹性缓出 - 有反弹效果
     */
    public static float easeOutElastic(float progress) {
        if (progress == 0)
            return 0;
        if (progress == 1)
            return 1;

        float c5 = (2 * (float) Math.PI) / 4.5f;
        return (float) Math.pow(2, -10 * progress) * (float) Math.sin((progress * 10 - 0.75f) * c5) + 1;
    }

    /**
     * 背弹缓出 - 有返回效果
     */
    public static float easeOutBack(float progress) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(progress - 1, 3) + c1 * (float) Math.pow(progress - 1, 2);
    }

    /**
     * 计算消除动画的透明度（从1到0）
     */
    public static float calculateFadeAlpha(float progress) {
        // 先保持不透明，然后逐渐消失
        return easeOutQuad(1 - progress);
    }

    /**
     * 计算消除动画的缩放（从1到0）
     */
    public static float calculateFadeScale(float progress) {
        // 边消失边缩小
        return 1 - progress * 0.3f;
    }

    /**
     * 计算连线绘制的部分比例
     */
    public static float calculateLineDrawRatio(float progress, int segmentIndex, int totalSegments) {
        // 让连线逐段显示，而不是全部一起显示
        float segmentStart = (float) segmentIndex / totalSegments;
        float segmentEnd = (float) (segmentIndex + 1) / totalSegments;

        if (progress < segmentStart)
            return 0f;
        if (progress >= segmentEnd)
            return 1f;

        return (progress - segmentStart) / (segmentEnd - segmentStart);
    }

    /**
     * 计算选中棋子的缩放因子（跳跃效果）
     */
    public static float calculateSelectionScale(float progress) {
        // 快速弹跳效果
        return 1 + easeOutElastic(progress) * 0.15f;
    }
}
