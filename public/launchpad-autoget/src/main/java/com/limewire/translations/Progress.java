package com.limewire.translations;

/**
 * Represents something that shows progress.
 */
interface Progress {

  void update(String msg, int inc);
  void inc();
  void reset();
  void setMax(int val);
  void setTitle(String title);
  void close();

  public final static Progress DEFAULT = new Progress() {
      public void update(String msg, int inc) {
        System.out.println("PROGRESS: " + inc + ":" + msg);
      }
      public void inc() {}
      public void reset() {}
      public void setMax(int val) {}
      public void setTitle(String title) {}
      public void close() {}
    };
}