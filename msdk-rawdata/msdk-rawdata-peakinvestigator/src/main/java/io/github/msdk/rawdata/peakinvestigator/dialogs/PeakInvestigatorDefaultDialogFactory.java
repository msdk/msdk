/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.rawdata.peakinvestigator.dialogs;

import com.veritomyx.actions.InitAction;
import com.veritomyx.actions.PiVersionsAction;

import io.github.msdk.rawdata.peakinvestigator.dialogs.PeakInvestigatorDialog.Status;

/**
 * A dialog factory that returns dialogs with sensible defaults. Specifically,
 * the following behavior is used:
 * 
 * <ul>
 * <li>{@code show()} always returns {@code Status.ACCEPT} unless there aren't
 * sufficient funds during INIT.</li>
 * <li>The version is the last used, if available; otherwise, the current
 * version.</li>
 * <li>The most cost-effective response time objective (i.e. RTO-24) is
 * used.</li>
 * </ul>
 *
 */
public class PeakInvestigatorDefaultDialogFactory implements PeakInvestigatorDialogFactory {

	private final static String DEFAULT_RTO = "RTO-24";

	@Override
	public PeakInvestigatorOptionsDialog getOptionsDialog(PiVersionsAction action, final int dataStart,
			final int dataEnd) {

		final String version = action.getLastUsedVersion().isEmpty() ? action.getCurrentVersion()
				: action.getLastUsedVersion();

		return new PeakInvestigatorOptionsDialog() {
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

	@Override
	public PeakInvestigatorInitDialog getInitDialog(InitAction action) {
		final Status status = action.getMaxPotentialCost(DEFAULT_RTO) <= action.getFunds() ? Status.ACCEPT
				: Status.CANCEL;
		return new PeakInvestigatorInitDialog() {

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
