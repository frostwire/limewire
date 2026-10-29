package com.limewire.translations;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * A graphical {@ link Progress} instance.
 */
final class ProgressPanel extends JPanel implements Progress {

	private static final long serialVersionUID = -4238271726544393394L;
	
	private final JProgressBar pb = new JProgressBar();
  private final JLabel lab = new JLabel("Idle...");
  private final JFrame parent;

  ProgressPanel(JFrame parent) {
    this.parent = parent;
    add(lab);
    add(pb);
    setPreferredSize(new Dimension(500,100));
  }

  ProgressPanel() { this(null); }

  public void close() {
    if (parent != null) {
      parent.setVisible(false);
      parent.dispose();
    }
  }
  
  public void update(String msg, int inc) {
    pb.setString(msg);
    lab.setText(msg);
    Util.note(this, msg);
    pb.setValue(pb.getValue()+inc);
    invalidate();
    repaint();  
  }
  
  public void inc() {
    pb.setValue(pb.getValue()+1);
  }
  
  public void reset() {
    pb.setValue(0);
  }
  
  public void setMax(int max) {
    reset();
    pb.setMaximum(max);
  }

  public static ProgressPanel open(String title) {
    JFrame f = new JFrame(title);
    final ProgressPanel res = new ProgressPanel(f);
    f.getContentPane().add(res);
    f.pack();
    CenterHelper.center(f);
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    return res;
  }

	public void setTitle(String string) {
		parent.setTitle(string);
	}  
}