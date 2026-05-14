package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public final class UiFont {
  private static final List<String> PREFERRED_FAMILIES = Arrays.asList(
      "Microsoft YaHei UI",
      "Microsoft YaHei",
      "微软雅黑",
      "Noto Sans CJK SC",
      "Source Han Sans SC",
      "PingFang SC",
      "SimHei",
      Font.SANS_SERIF,
      Font.DIALOG);

  private static volatile String cachedFamily;

  private UiFont() {
  }

  public static Font font(int style, int size) {
    return new Font(resolveFamily(), style, size);
  }

  public static Font font(Font base) {
    if (base == null) {
      return font(Font.PLAIN, 12);
    }
    return font(base.getStyle(), base.getSize());
  }

  public static void installDefaults() {
    Font baseFont = font(Font.PLAIN, 12);
    String[] keys = {
        "Button.font",
        "ToggleButton.font",
        "Label.font",
        "TextField.font",
        "PasswordField.font",
        "ComboBox.font",
        "CheckBox.font",
        "RadioButton.font",
        "ToolTip.font",
        "OptionPane.font",
        "TitledBorder.font",
        "Menu.font",
        "MenuItem.font",
        "PopupMenu.font"
    };
    for (String key : keys) {
      UIManager.put(key, baseFont);
    }
  }

  private static String resolveFamily() {
    String family = cachedFamily;
    if (family != null) {
      return family;
    }
    synchronized (UiFont.class) {
      if (cachedFamily != null) {
        return cachedFamily;
      }
      List<String> available = Arrays.asList(
          GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
      for (String preferred : PREFERRED_FAMILIES) {
        for (String candidate : available) {
          if (candidate.equalsIgnoreCase(preferred)) {
            cachedFamily = candidate;
            return candidate;
          }
        }
      }
      cachedFamily = Font.DIALOG;
      return cachedFamily;
    }
  }
}