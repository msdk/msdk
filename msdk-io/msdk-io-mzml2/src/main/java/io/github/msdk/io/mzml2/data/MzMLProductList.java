/*
 * (C) Copyright 2015-2016 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.msdk.io.mzml2.data;

import java.util.ArrayList;

public class MzMLProductList {

  private ArrayList<MzMLProduct> products;

  public MzMLProductList() {
    this.products = new ArrayList<>();
  }

  public ArrayList<MzMLProduct> getProducts() {
    return products;
  }

  public void addProduct(MzMLProduct product) {
    products.add(product);
  }

}
