package io.github.msdk.io.mzml2.util;

import java.util.Stack;

import io.github.msdk.MSDKRuntimeException;

public class XMLTagsTracker {

  private Stack<String> tagStack;

  public XMLTagsTracker() {
    tagStack = new Stack<>();
  }

  public void enteredTag(String tag) {
    tagStack.push(tag);
  }

  public void exitedTag(String tag) {
    if (!tagStack.peek().equals(tag))
      throw (new MSDKRuntimeException(
          "Cannot exit tag " + tag + "\nLast tag entered was " + tagStack.peek()));
    tagStack.pop();
  }

  public boolean isInside(String tag) {
    if (tagStack.contains(tag)) {
      return true;
    }
    return false;
  }

}
