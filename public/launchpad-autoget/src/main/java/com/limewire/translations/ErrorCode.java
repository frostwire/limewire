package com.limewire.translations;

/**
 * An error with a message and <code>int</code>. Can't compare instances with
 * <code>==</code>, but you can with {@link #equals(Object)}.
 */
final class ErrorCode implements Comparable {
  
  /*
   * Instances
   */
  final static ErrorCode DOWNLOAD = new ErrorCode("Trying to download", 2);
  final static ErrorCode LOGIN = new ErrorCode("Trying to login", 3);
  final static ErrorCode CHECKING_FOR_MAIL = new ErrorCode("Checking for mail", 4);
  final static ErrorCode SAVING_TO(String absolutePath) {
    return new ErrorCode("Saving to " + absolutePath, 5);
  }

  private final String msg;
  private final int code;
  
  private ErrorCode(String msg, int code) {
    this.msg = msg;
    this.code = code;
  }
  
  final String getMsg() { 
    return msg;
  }
  
  final int getCode() {
    return code;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof ErrorCode)) return false;
    ErrorCode that = (ErrorCode)o;
    return this.getCode() == that.getCode(); 
  }
  
  public int compareTo(Object o) {
    if (!(o instanceof ErrorCode)) return -1;
    ErrorCode that = (ErrorCode)o;
    return this.getCode() - that.getCode();
  }
}
