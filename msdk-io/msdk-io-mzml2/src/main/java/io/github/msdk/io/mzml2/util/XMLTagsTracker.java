package io.github.msdk.io.mzml2.util;

import io.github.msdk.MSDKRuntimeException;

public class XMLTagsTracker {

  private String path;

  public XMLTagsTracker() {
    path = "";
  }

  public String enteredTag(String tag) {
    path += "/" + tag;
    return path;
  }

  public String exitedTag(String tag) {
    String exitTag = path.substring(path.lastIndexOf('/') + 1, path.length());
    if (!tag.equals(exitTag))
      throw (new MSDKRuntimeException(
          "Cannot exit tag " + tag + "\nLast tag entered was " + exitTag));
    path = path.substring(0, path.lastIndexOf('/'));
    return path;
  }

  public String getPath() {
    return path;
  }

  public boolean isInside(String tag) {
    String exitTag = path.substring(path.lastIndexOf('/') + 1, path.length());
    if (tag.equals(exitTag) || path.contains("/" + tag + "/")) {
      return true;
    }
    return false;
  }

}
