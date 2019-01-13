/*
 * (C) Copyright 2015-2017 by MSDK Development Team
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
package io.github.msdk.isotopes.tracing.data.constants;

/**
 * If a set of fragments exhibits a natural isotope incorporation we refer to this incorporation as
 * NATURAL. If all fragments in a set contain the same amount of tracers we refer to this
 * incorporation as an MARKED one. If some are marked and some not, we have a mixed set.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public enum IncorporationType {

  NATURAL(), MARKED(), MIXED();

}
