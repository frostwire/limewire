package com.limewire.translations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

final class Config {

  private final static Config instance = new Config();
  public static Config getInstance() {return instance;}

  private final Properties props = new Properties();

  private final String file = ".translations";
  
  Config() {
    try {
      File f = new File(file);
      if (f.exists()) {
        FileInputStream in = new FileInputStream(f);
        props.load(in);
        in.close();
      }
    } catch (Exception e) {e.printStackTrace();}
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        public void run() {
          try {
            File f = new File(file);
            FileOutputStream out = new FileOutputStream(f);
            props.store(out,"properties");
            out.close();
          } catch (Exception e) {e.printStackTrace();}
        }
      }));
  }

  String get(String key) {
    return props.getProperty(key);
  }

  void set(String key, String val) {
    props.setProperty(key,val);
  }
  
}