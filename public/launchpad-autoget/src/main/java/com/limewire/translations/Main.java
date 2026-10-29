package com.limewire.translations;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The main entry point for the program to download the translation files.  This has the following options:
 * <pre>
 *  -help                         prints help
 *  -email &lt;String&gt;         set the email to which the message from the translation people goes
 *  -passwordEmail &lt;String&gt; password for the email above
 *  -username &lt;String&gt;      username for launchpad.net
 *  -password &lt;String&gt;      password for launchpad.net
 *  -server &lt;String&gt;        mail server to use; defaults to cyrus.limewire.com
 *  -group &lt;String&gt;         group on launchpad.net -- i.e. the limewire in 
 *                                https://translations.launchpad.net/limewire/trunk/+pots/limewire/ja
 *                                this allows other folks to use it; defaults to limewire
 * -gui                           uses the GUI version
 * </pre>
 */
final class Main {
	
	private boolean done = false;
	private LoginInfo loginInfo;
	
	private String email;
	private String passwordEmail;
	private String username;
	private String password;
	private String server;
	private String group;
	private boolean gui = false;
  
  /** Remember when we sent the message so we don't get an old file. */
  private Date startTime;

	public static void main(String[] args) {
		new Main().realMain(args);
	}
	
	/**
	 * Entry point from the command line.
	 * 
	 * @param args command line arguments
	 */
  void realMain(String[] args) {
  	
  	if (args != null) {
	  	for (int i=0; i<args.length;) {
	  		String arg = args[i++];
	  		if (arg.equals("-help")) {
	  			printHelp();
	  			return;
	  		} else if (arg.equals("-email")) {
	  			email = demand("email", args, i++);
	  		} else if (arg.equals("-passwordEmail")) {
	  			passwordEmail = demand("passwordEmail", args, i++);
	  		} else if (arg.equals("-username")) {
	  			username = demand("username", args, i++);
	  		} else if (arg.equals("-password")) {
	  			password = demand("password", args, i++);
	  		} else if (arg.equals("-server")) {
	  			server = demand("server", args, i++);
	  		} else if (arg.equals("-group")) {
	  			group = demand("group", args, i++);
	  		} else if (arg.equals("-gui")) {
	  			gui = true;
	  		} else {
	  			System.err.println("Illegal option " + arg);
	  		}

	  	}
    }
  	
  	go();
  }
  
  /**
   * Entry point after adding options -- e.g. when using ant.
   */
  public void go() {
  	
    Pages.newInstance(group);
    
    // Set the value for when we sent the message
    // set time back by 60 seconds, since request is run from a different script now
    // before
    startTime = new Date(System.currentTimeMillis() - 60 * 1000);

    // First get the log in info
  	if (gui) {
  		loginInfo = getLoginInfo();
  		if (loginInfo == null) return;
  	}
    
    // We'll let these be null but warn
    if (group == null) {
      group = "limewire";
      warn("using default group '" + group + "'");
    }
    if (server == null) {
      server = "cyrus.limewire.com";
      warn("using default server '" + server + "'");
    }
    
    // Check for nulls
    nullCheck("email -- your email login", email);
    nullCheck("passwordEmail -- the password to your email", passwordEmail);
    nullCheck("username -- your username for launchpad", username);
    nullCheck("password -- your password for launchpad", password);    
    
    // Show a quit window
    if (gui) showQuitWindow();
    
    // launchpad login is completely broken after changes to launchpad
    
    // Login
//    final String userAgent = UserAgents.randomUserAgent();
//    InputStreamAndCookie inAndC = null;
//    try {
//			inAndC = Util.login(this, username, password, userAgent);
//		} catch (IOException e) {
//			Util.handle(this,e,ErrorCode.LOGIN);
//		}
//		if (inAndC == null) {
//			fail("Couldn't log in");
//			return;
//		} else {
//			// uncomment for debugging
//			// new StreamConsumer(inAndC.getInputStream(), "login.html");//.start();
//		}
//
//		// Click download
//    Download download = new Download(inAndC, userAgent);
//    try {
//    	download.submit();
//    	// new StreamConsumer(stream, "download.html");//start();
//    } catch (IOException e) {
//    	Util.handle(this, e,ErrorCode.DOWNLOAD);
//    }
    
		// Wait for mail
		for (int tries=0; !done; tries++) {
			note("trying for the " + Util.cardinal2ordinal(tries) + " time...");
			if (checkForMailSafely()) break;
			try {
				Thread.sleep(5 * 60 * 1000);
			} catch (InterruptedException e) {
				Util.handle(this, e, ErrorCode.CHECKING_FOR_MAIL);
			}
		}
		
		// done
		note("done");
		if (gui) System.exit(0);
  }
  
  private boolean checkForMailSafely() {
    try {
      return checkForMail();
    } catch (Exception e) {
      Util.handle(this,e,ErrorCode.CHECKING_FOR_MAIL);
    }
    return false; // won't happen
  }

  private void warn(String msg) {
    System.err.println(msg);
  }
  
  /**
   * Throws a {@link NullPointerException} if <code>arg</code> is <code>null</code>.
   * 
   * @param name name of <code>arg</code>
   * @param arg the value of the argument
   */
  private void nullCheck(String name, String arg) {
    if (arg == null) {
      throw new NullPointerException(name + " must not be null");
    }
  }
  
  /**
	 * Ensures that there's another String in <code>args</code> that doesn't
	 * start with a <code>-</code> and returns that String.
	 * 
	 * @param cmd
	 *          command to use for error messages
	 * @param args
	 *          arguments
	 * @param i
	 *          index of the argument to return
	 * @return the String at <code>args[i]</code> or throws an exception
	 */
	private String demand(String cmd, String[] args, int i) {
		if (i >= args.length) {
			throw new IllegalArgumentException(cmd + " requires an argument");
		}
		final String arg = args[i];
		if (arg.startsWith("-")) {
			throw new IllegalArgumentException(cmd + " requires an argument");
		}
		return arg;
	}

	private void printHelp() {
		System.err.println("Usage: java " + getClass().getName() + " [options] where the options are:");
		System.err.println(" -help                        prints help");
		System.err.println(" -email <String>              set the email to which the message from the translation people goes");
		System.err.println(" -passwordEmail <String>      password for the email above");
		System.err.println(" -username <String>           username for launchpad.net");
		System.err.println(" -password <String>           password for the email above");
		System.err.println(" -server <String>             mail server to use; defaults to cyrus.limewire.com");
		System.err.println(" -group <String>              group on launchpad.net -- i.e. the limewire in ");
    System.err.println("                              this allows other folks to use it; defaults to limewire");
    System.err.println(" -gui                          uses the GUI version");
	}

	/**
	 * Looks the latest messages and then returns <code>true</code> if one is
	 * found from <code>rosetta@launchpad.net</code> with a file endingi n
	 * <code>tar.gz</code>.
	 * 
	 * @return <code>true</code> if one is found from
	 *         <code>rosetta@launchpad.net</code> with a file endingi n
	 *         <code>tar.gz</code>.
	 */
	private boolean checkForMail() throws Exception {
		Progress p;
		if (gui) {
			p = Util.open("searching mail for a message...");
		} else {
			p = new TextProgress();
		}
    Store store = null;
    Folder folder = null;
    try {
      Properties props = System.getProperties();
      Session session = Session.getDefaultInstance(props, null);
      store = session.getStore("imaps");
      store.connect(server == null || Util.isEmpty(server) ? "cyrus.limewire.com" : server, 
      		          email, 
      		          passwordEmail);
      folder = store.getDefaultFolder();
      if (folder == null) throw new Exception("No default folder");
      folder = folder.getFolder("INBOX");
      if (folder == null) throw new Exception("No imap INBOX");
      folder.open(Folder.READ_ONLY);
      Message[] msgs = folder.getMessages();
      p.setMax(msgs.length);
      p.update("found " + msgs.length + " message" + (msgs.length==1 ? "" : "s"),0);
      for (int i=msgs.length-1, N=100, j=0; j<N && i>=0; i--, j++, p.inc()) {
      	p.update(j+1 + "/" + msgs.length, j+1);
        Message m = msgs[i];
        Address[] a = m.getFrom();
        if (a.length > 1) continue;
        String addr = String.valueOf(a[0]);
        if (addr.indexOf("rosetta@launchpad.net") == -1) continue;
        //
        // Skipping messages before we sent them
        //
        if (m.getReceivedDate().before(startTime)) continue;
        try {
        	String file = analyze(m);
        	if (file != null) {
        		p.update("Downloading " + file + "...", i+1);
        		download(file, p);
        		return true;
        	}
        } catch (Exception e) {e.printStackTrace();}
      } 
    } catch (Exception e) {handle(e);}
    p.close();
    return false;
	}

	/**
	 * Downloads the <code>url</code> specified, updating <code>p</code>.
	 * 
	 * @param url url to download
	 * @param p   progress thing to update
	 */
	private void download(String url, Progress p) {
		p.update("downloading " + url, 0);
		int ilastSlash = url.lastIndexOf('/');
		if (ilastSlash == -1) ilastSlash = url.lastIndexOf('\\');
		String name = ilastSlash == -1 ? url : url.substring(ilastSlash+1);
		File fileToSave = new File(name);
		if (gui) {
			JFileChooser fc = new JFileChooser("Please select a directory to save " + url) {
				
				private static final long serialVersionUID = -3156189604634266014L;
	
				public boolean isDirectorySelectionEnabled() {
					return false;
				}
			};
			 
			fc.setSelectedFile(fileToSave);
			fc.showSaveDialog(null);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileToSave = fc.getSelectedFile();
		} else {
			fileToSave = new File(new File("."), name);
		}
		note("saving to " + fileToSave.getAbsolutePath());
		try {
			Util.wget(url, fileToSave, p);
		} catch (Exception e) {
			Util.handle(this, e, ErrorCode.SAVING_TO(fileToSave.getAbsolutePath()));
		}
	}

	/**
	 * Looks a single email and checks whether it has a file that ends in <code>tar.gz</code> the returns that URL or <code>null</code> if not found.
	 * 
	 * @param m the message
	 * @return a URL ending in <code>tar.gz</code> or <code>null</code> if not found.
	 * @throws Exception
	 */
	private String analyze(Message m) throws Exception {
    //
    // Get the text part of the message, because it's easier
    //
		Object content = m.getContent();
		if (content instanceof String) {
			for (StringTokenizer st = new StringTokenizer((String)content,"\n"); st.hasMoreTokens();) {
				String line = st.nextToken();
				String res = check(line);
				if (res != null) return res;
      }
		} else if (content instanceof MimeMultipart) {
	    MimeMultipart mmp = (MimeMultipart)m.getContent();
	    for (int i=0; i<mmp.getCount(); i++) {
	      BodyPart bp = mmp.getBodyPart(i);
	      if (bp.getContentType().indexOf("TEXT/PLAIN") == -1) continue;
	      BufferedReader r = new BufferedReader(new InputStreamReader(bp.getInputStream()));
	      String line;
	      while ((line = r.readLine()) != null) {
					String res = check(line);
					if (res != null) return res;
	      }
	    }
		}
		//
		// Didn't find it
		//
		return null;
	}

	/**
	 * Returns a file ending in <code>tar.gz</code> or <code>null</code> if not found.
	 * 
	 * @param line String to inspect
	 * @return a file ending in <code>tar.gz</code> or <code>null</code> if not found
	 */
	private String check(String line) {
    line = line.trim();
    if (line.length() == 0) return null;
    if (line.indexOf("tar.gz") != -1) return line;
    return null;
	}

	private void showQuitWindow() {
		final JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.X_AXIS));
		f.getContentPane().add(new JButton(new AbstractAction("Quit") {
			private static final long serialVersionUID = 7339066668372380232L;

			public void actionPerformed(ActionEvent arg0) {
				done = true;
				System.exit(0);
			}
		}));
		f.getContentPane().add(new JButton(new AbstractAction("Check for mail") {
			private static final long serialVersionUID = 2638374802092394185L;

			public void actionPerformed(ActionEvent arg0) {
				checkForMailSafely();
			}
		}));
		f.pack();
		f.setVisible(true);
	}

	private void fail(String string) {
		JOptionPane.showMessageDialog(null, "Error", string, JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	/**
	 * Returns log in information from the user after prompting her for it or
	 * <code>null</code> if it was cancelled.
	 * 
	 * @return log in information from the user after prompting her for it or
	 *         <code>null</code> if it was cancelled
	 */
	private LoginInfo getLoginInfo() {
    note("getting login info...");
    
    username      = Config.getInstance().get(wrap(Props.USERNAME));
    password      = Config.getInstance().get(wrap(Props.PASSWORD));
    email         = Config.getInstance().get(wrap(Props.EMAIL));
    passwordEmail = Config.getInstance().get(wrap(Props.PASSWORD_EMAIL));
    server        = Config.getInstance().get(wrap(Props.SERVER));
    group         = Config.getInstance().get(wrap(Props.GROUP));

    if (!Util.isProperty("debug")) {
      LoginInfoFrame login = null;
      boolean save = false;
      do {
        if (login == null) login = new LoginInfoFrame(username,password,email,passwordEmail,server,group);
        login.show();
        if (login.isCancelled()) return null;
        username = login.getUsername();
        password = login.getPassword();
        email = login.getEmail();
        passwordEmail = login.getPasswordEmail();
        server = login.getServer();
        save = login.isChecked();
        group = login.getGroup();
      } while (Util.isEmpty(username) || Util.isEmpty(email) || Util.isEmpty(passwordEmail) ||
      		     Util.isEmpty(password) || Util.isEmpty(group) 
      		     );
      if (save) {
        Config.getInstance().set(wrap(Props.USERNAME),			username);
        Config.getInstance().set(wrap(Props.PASSWORD),			password);
        Config.getInstance().set(wrap(Props.EMAIL),					email);
        Config.getInstance().set(wrap(Props.PASSWORD_EMAIL),passwordEmail);
        Config.getInstance().set(wrap(Props.SERVER),				server);
        Config.getInstance().set(wrap(Props.GROUP),					group);
      }
    }
    return new LoginInfo(username,password,email,passwordEmail,server,group);
  }
	
  private void handle(Throwable t) {
    t.printStackTrace();
  }

  private void note(Object msg) {
    Util.note(this,msg);
  }

  private String wrap(String prop) {
    return getClass().getName() + ":" + prop;
  }
  
  /*
	 * These are for the ant interface, and that's why they are not private and
	 * all the way down here.
	 */

	boolean isDone() {
		return done;
	}

	void setDone(boolean done) {
		this.done = done;
	}

	String getEmail() {
		return email;
	}

	void setEmail(String email) {
		this.email = email;
	}

	String getPasswordEmail() {
		return passwordEmail;
	}

	void setPasswordEmail(String passwordEmail) {
		this.passwordEmail = passwordEmail;
	}

	String getUsername() {
		return username;
	}

	void setUsername(String username) {
		this.username = username;
	}

	String getPassword() {
		return password;
	}

	void setPassword(String password) {
		this.password = password;
	}

	String getServer() {
		return server;
	}

	void setServer(String server) {
		this.server = server;
	}

	String getGroup() {
		return group;
	}

	void setGroup(String group) {
		this.group = group;
	}

	boolean isGui() {
		return gui;
	}

	void setGui(boolean gui) {
		this.gui = gui;
	}
}
