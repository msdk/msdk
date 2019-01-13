package io.github.msdk.isotopes.tracing.data;

import java.util.ArrayList;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
@SuppressWarnings("serial")
public class FragmentList extends ArrayList<Fragment> {

  public FragmentList() {

  }

  public FragmentList(Fragment... fragments) {
    for (Fragment fragment : fragments) {
      this.add(fragment);
    }
  }

  public FragmentList copy() {
    FragmentList fragments = new FragmentList();
    for (Fragment fragment : this) {
      fragments.add(fragment.copy());
    }
    return fragments;
  }

}
