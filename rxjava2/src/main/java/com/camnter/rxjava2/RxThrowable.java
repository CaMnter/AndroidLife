package com.camnter.rxjava2;

import android.support.annotation.NonNull;

/**
 * @author CaMnter
 */

public class RxThrowable extends Throwable {

  private int responseCode;

  @NonNull
  private final String responseMessage;

  public RxThrowable(@NonNull final String responseMessage) {
    super();
    this.responseMessage = responseMessage;
  }

  public RxThrowable(final int responseCode, @NonNull final String responseMessage) {
    super();
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  @NonNull
  public String getResponseMessage() {
    return this.responseMessage;
  }

}
