package com.limewire.translations;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Pops up and allows the user to enter information for logging in.
 */
final class LoginInfoFrame {
  
  private String username;
  private String password;
  private String email;
  private String passwordEmail;
  private String server;
  private String group;
  private boolean isChecked;
  private boolean isCancelled;

  private final JTextField usernameField;
  private final JTextField passwordField;
  private final JTextField emailField;
  private final JTextField passwordEmailField;
  private final JTextField serverField;
  private final JTextField groupField;
  private final JPanel mainPanel;
  private final JCheckBox check;
  
  public String getUsername()  {return username;}
  public String getPassword()  {return password;}
  public String getEmail()  {return email;}
  public String getPasswordEmail()  {return passwordEmail;}
  public String getServer()  {return !Util.isEmpty(server) ? server : "cyrus.limewire.com";}
  public String getGroup()  {return !Util.isEmpty(group) ? group : "limewire";}
  public boolean isChecked()   {return isChecked;}
  public boolean isCancelled() {return isCancelled;}

  public void setUsername(String username) {usernameField.setText(this.username = username);}
  public void setPassword(String password) {passwordField.setText(this.password = password);}
  public void setEmail(String email) {emailField.setText(this.email = email);}
  public void setPasswordEmail(String passwordEmail) {passwordEmailField.setText(this.passwordEmail = passwordEmail);}
  public void setServer(String server) {serverField.setText(this.server = server);}
  public void setGroup(String group) {groupField.setText(this.group = group);}

  void addAdditionalButton(AbstractAction b) {
    JPanel row = newPanel();
    row.add(new JButton(b));
    mainPanel.add(row);    
  }
  
  private JPanel newPanel() {
    JPanel p = new JPanel();
    p.setLayout(new FlowLayout(FlowLayout.LEFT));
    return p;
  }
    
  public LoginInfoFrame(String username, String password, String email, String passwordEmail, String server, String group) {

    this.username 			= username;
    this.password 			= password;
    this.email 					= email;
    this.passwordEmail 	= passwordEmail;
    this.server 				= server;
    this.group 					= group;

    JPanel p = newPanel();
    p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
    
    JPanel row;
    
    row = newPanel();
    row.add(new JLabel("Please enter your launchpad.net and email info"));
    p.add(row);

    row = newPanel();
    row.setLayout(new GridLayout(6,6));

    row.add(new JLabel("Username for launchpad.net"));
    row.add(usernameField = new JTextField(username==null?"":username,20));
    
    row.add(new JLabel("Password for launchpad.net"));
    row.add(passwordField = new JPasswordField(password==null ? "" : password,20));

    row.add(new JLabel("Email"));
    row.add(emailField = new JTextField(email==null?"":email,20));
    
    row.add(new JLabel("Password for email"));
    row.add(passwordEmailField = new JPasswordField(passwordEmail==null ? "" : passwordEmail,20));

    row.add(new JLabel("Mail server"));
    row.add(serverField = new JTextField(server==null?"cyrus.limewire.com":server,20));
    
    row.add(new JLabel("Group (e.g. limewire)"));
    row.add(groupField = new JTextField(group==null ? "limewire" : group,20));

    p.add(row);
    
    row = newPanel();
    row.add(new JLabel("Remember"));
    check = new JCheckBox();
    if (!Util.isEmpty(username) || !Util.isEmpty(password) ||
        !Util.isEmpty(email) || !Util.isEmpty(passwordEmail)) {
      check.setSelected(true);
    }
    row.add(check);
    p.add(row);

   this.mainPanel = p;
  }
  
  public void show() {
    
    isCancelled = false;

    int res = JOptionPane.showConfirmDialog(null, mainPanel, "Login", 
                                            JOptionPane.OK_CANCEL_OPTION,
                                            JOptionPane.QUESTION_MESSAGE,
                                            null);
    isChecked = check.isSelected();
    if (res == JOptionPane.OK_OPTION) {
      username = usernameField.getText().trim();
      password = passwordField.getText().trim();
      email = emailField.getText().trim();
      passwordEmail = passwordEmailField.getText().trim();
      server = serverField.getText().trim();
      group = groupField.getText().trim();
    } else {
      isCancelled = true;
    }
  }
  
  public boolean isReady() {
    return !Util.isEmpty(getUsername()) && !Util.isEmpty(getPassword())
      &&   !Util.isEmpty(getEmail()) && !Util.isEmpty(getPasswordEmail())
      &&   !Util.isEmpty(getGroup());
  }
  
}