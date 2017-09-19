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

package io.github.msdk.spectra.peakinvestigator.providers;

import com.veritomyx.actions.InitAction;
import com.veritomyx.actions.PiVersionsAction;

import io.github.msdk.spectra.peakinvestigator.providers.PeakInvestigatorProvider.Status;

/**
 * A dialog factory that returns providers with sensible defaults. Specifically, the following
 * behavior is used:
 *
 * <ul>
 * <li>{@code show()} always returns {@code Status.ACCEPT} unless there aren't sufficient funds
 * during INIT.</li>
 * <li>Project information is obtained from System properties, which can be set via -D flags to the
 * JVM:
 * <ul>
 * <li>{@code peakinvestigator.username}</li>
 * <li>{@code peakinvestigator.password}</li>
 * <li>{@code peakinvestigator.project}</li>
 * </ul>
 * <li>The version is the last used, if available; otherwise, the current version.</li>
 * <li>The most cost-effective response time objective (i.e. RTO-24) is used.</li>
 * </ul>
 */
public class PeakInvestigatorDefaultProviderFactory implements PeakInvestigatorProviderFactory {

  private final static String DEFAULT_RTO = "RTO-24";

  /** {@inheritDoc} */
  @Override
  public PeakInvestigatorProjectProvider createProjectProvider() {
    return new PeakInvestigatorProjectProvider() {

      @Override
      public Status show() {
        if (System.getProperty("peakinvestigator.username") == null
            || System.getProperty("peakinvestigator.password") == null
            || System.getProperty("peakinvestigator.project") == null) {
          return Status.CANCEL;
        }

        return Status.ACCEPT;
      }

      @Override
      public String getUsername() {
        return System.getProperty("peakinvestigator.username");
      }

      @Override
      public String getPassword() {
        return System.getProperty("peakinvestigator.password");
      }

      @Override
      public int getProjectId() {
        return Integer.parseInt(System.getProperty("peakinvestigator.project"));
      }
    };
  }

  /** {@inheritDoc} */
  @Override
  public PeakInvestigatorOptionsProvider createOptionsProvider(PiVersionsAction action,
      final int dataStart, final int dataEnd) {

    final String version = action.getLastUsedVersion().isEmpty() ? action.getCurrentVersion()
        : action.getLastUsedVersion();

    return new PeakInvestigatorOptionsProvider() {
      @Override
      public Status show() {
        return Status.ACCEPT;
      }

      @Override
      public String getVersion() {
        return version;
      }

      @Override
      public int getStartMass() {
        return dataStart;
      }

      @Override
      public int getEndMass() {
        return dataEnd;
      }

    };
  }

  /** {@inheritDoc} */
  @Override
  public PeakInvestigatorInitProvider createInitProvider(InitAction action) {
    final Status status = action.getMaxPotentialCost(DEFAULT_RTO) <= action.getFunds()
        ? Status.ACCEPT : Status.CANCEL;
    return new PeakInvestigatorInitProvider() {

      @Override
      public Status show() {
        return status;
      }

      @Override
      public String getRto() {
        return DEFAULT_RTO;
      }
    };
  }

}
