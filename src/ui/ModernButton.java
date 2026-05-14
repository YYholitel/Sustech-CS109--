package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 现代化蓝色按键组件，具有蓝色渐变、动画、闪光和高光效果
 * 特性：
 * - 蓝色渐变背景
 * - 悬停时明亮效果
 * - 点击时按压效果和高光加深
 * - 平滑的动画过渡
 * - 闪光和氛围效果
 */
public class ModernButton extends JButton {
  private float hoverProgress = 0f; // 悬停动画进度 0-1
  private float pressProgress = 0f; // 按压动画进度 0-1
  private float glowProgress = 0f; // 闪光动画进度 0-1
  private boolean isHovered = false;
  private boolean isPressed = false;
  private Timer animationTimer;
  private Timer glowTimer;

  // 蓝色配色方案
  private static final Color PRIMARY_BLUE = new Color(41, 128, 185); // 主蓝色
  private static final Color DARK_BLUE = new Color(25, 77, 120); // 深蓝色
  private static final Color LIGHT_BLUE = new Color(52, 152, 219); // 亮蓝色
  private static final Color GLOW_COLOR = new Color(74, 165, 235, 80); // 闪光颜色（透明）
  private static final Color HIGHLIGHT = new Color(255, 255, 255, 120); // 高光

  public ModernButton(String text) {
    super(text);
    setOpaque(false);
    setContentAreaFilled(false);
    setBorderPainted(false);
    setFocusPainted(false);
    setFont(UiFont.font(Font.BOLD, 16));
    setForeground(Color.WHITE);
    setCursor(new Cursor(Cursor.HAND_CURSOR));

    // 动画定时器 - 悬停和按压效果
    animationTimer = new Timer(16, e -> {
      float targetHover = isHovered ? 1f : 0f;
      float targetPress = isPressed ? 1f : 0f;

      // 平滑过渡
      hoverProgress += (targetHover - hoverProgress) * 0.2f;
      pressProgress += (targetPress - pressProgress) * 0.25f;

      if (Math.abs(hoverProgress - targetHover) < 0.01f) {
        hoverProgress = targetHover;
      }
      if (Math.abs(pressProgress - targetPress) < 0.01f) {
        pressProgress = targetPress;
      }

      repaint();

      if (hoverProgress == targetHover && pressProgress == targetPress) {
        animationTimer.stop();
      }
    });

    // 闪光动画定时器 - 循环闪光效果
    glowTimer = new Timer(1500, e -> {
      glowProgress = 0f;
      startGlowAnimation();
    });

    // 鼠标监听
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        isHovered = true;
        animationTimer.restart();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        isHovered = false;
        animationTimer.restart();
      }

      @Override
      public void mousePressed(MouseEvent e) {
        isPressed = true;
        animationTimer.restart();
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        isPressed = false;
        animationTimer.restart();
      }
    });

    // 启动闪光动画
    startGlowAnimation();
  }

  @Override
  public void setFont(Font font) {
    if (font == null) {
      super.setFont(UiFont.font(Font.BOLD, 16));
      return;
    }
    super.setFont(UiFont.font(font.getStyle(), font.getSize()));
  }

  private void startGlowAnimation() {
    glowTimer.start();
    Timer glowSequence = new Timer(50, e -> {
      glowProgress += 0.05f;
      if (glowProgress >= 1f) {
        glowProgress = 1f;
        ((Timer) e.getSource()).stop();
      }
      repaint();
    });
    glowSequence.start();
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    int width = getWidth();
    int height = getHeight();
    int arcRadius = 12;

    // 计算颜色变化
    float hoverFactor = hoverProgress * 0.3f; // 悬停亮度增加30%
    float pressFactor = pressProgress * 0.4f; // 按压深度增加40%

    // 基础颜色混合
    Color topColor = new Color(
        (int) (PRIMARY_BLUE.getRed() * (1 + hoverFactor) * (1 - pressFactor * 0.5f)),
        (int) (PRIMARY_BLUE.getGreen() * (1 + hoverFactor) * (1 - pressFactor * 0.5f)),
        (int) (PRIMARY_BLUE.getBlue() * (1 + hoverFactor) * (1 - pressFactor * 0.5f)));

    Color bottomColor = new Color(
        (int) (DARK_BLUE.getRed() * (1 + hoverFactor * 0.7f) * (1 - pressFactor * 0.7f)),
        (int) (DARK_BLUE.getGreen() * (1 + hoverFactor * 0.7f) * (1 - pressFactor * 0.7f)),
        (int) (DARK_BLUE.getBlue() * (1 + hoverFactor * 0.7f) * (1 - pressFactor * 0.7f)));

    // 绘制阴影 - 按压时减少
    drawShadow(g2d, width, height, arcRadius, 1f - pressProgress * 0.7f);

    // 绘制主体渐变
    GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, height, bottomColor);
    g2d.setPaint(gradient);
    g2d.fillRoundRect(0, 0, width, height, arcRadius, arcRadius);

    // 绘制顶部高光 - 按压时更明显
    drawToplight(g2d, width, height, arcRadius, 0.3f + pressProgress * 0.2f);

    // 绘制闪光氛围
    drawGlow(g2d, width, height, arcRadius);

    // 绘制内部高光条纹（按压时加深）
    drawInnerHighlight(g2d, width, height, arcRadius, pressProgress);

    // 绘制边框
    drawBorder(g2d, width, height, arcRadius);

    // 绘制文字
    super.paintComponent(g);
  }

  private void drawShadow(Graphics2D g2d, int width, int height, int arcRadius, float opacity) {
    g2d.setColor(new Color(0, 0, 0, (int) (40 * opacity)));
    g2d.setStroke(new BasicStroke(3));
    RoundRectangle2D shadow = new RoundRectangle2D.Float(2, height - 4, width - 4, 4, 2, 2);
    g2d.fill(shadow);
  }

  private void drawToplight(Graphics2D g2d, int width, int height, int arcRadius, float intensity) {
    Paint oldPaint = g2d.getPaint();
    Color highlightColor = new Color(255, 255, 255, (int) (80 * intensity));
    GradientPaint toplight = new GradientPaint(
        0, 0, highlightColor,
        0, height / 3, new Color(255, 255, 255, 0));
    g2d.setPaint(toplight);
    g2d.fillRoundRect(0, 0, width, height / 3, arcRadius, arcRadius);
    g2d.setPaint(oldPaint);
  }

  private void drawGlow(Graphics2D g2d, int width, int height, int arcRadius) {
    // 动态闪光效果
    float glowAlpha = (float) Math.sin(glowProgress * Math.PI) * 0.6f;
    if (isHovered) {
      glowAlpha += 0.2f;
    }

    g2d.setColor(new Color(74, 165, 235, (int) (100 * glowAlpha)));
    g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2d.drawRoundRect(1, 1, width - 2, height - 2, arcRadius, arcRadius);

    // 外部辉光
    g2d.setColor(new Color(52, 152, 219, (int) (30 * glowAlpha)));
    g2d.drawRoundRect(-2, -2, width + 4, height + 4, arcRadius + 4, arcRadius + 4);
  }

  private void drawInnerHighlight(Graphics2D g2d, int width, int height, int arcRadius, float pressIntensity) {
    // 按压时的内部高光加深
    int highlightHeight = (int) (height * 0.3f);
    GradientPaint innerGradient = new GradientPaint(
        0, height / 2, new Color(255, 255, 255, (int) (60 * pressIntensity)),
        0, height, new Color(0, 0, 0, (int) (40 * pressIntensity)));
    g2d.setPaint(innerGradient);
    g2d.fillRoundRect(0, height / 2, width, height / 2, arcRadius, arcRadius);
  }

  private void drawBorder(Graphics2D g2d, int width, int height, int arcRadius) {
    g2d.setColor(new Color(25, 77, 120, 180));
    g2d.setStroke(new BasicStroke(1.5f));
    g2d.drawRoundRect(0, 0, width - 1, height - 1, arcRadius, arcRadius);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(150, 50);
  }
}
