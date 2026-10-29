package com.limewire.translations;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Centers components on the screen.
 */
final class CenterHelper {
	
	private CenterHelper() {}

  public final static void center(Component f) {
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (int)((d.getWidth()  - f.getWidth())  / 2);
    int y = (int)((d.getHeight() - f.getHeight()) / 2);
    f.setLocation(x, y);
  }
}
