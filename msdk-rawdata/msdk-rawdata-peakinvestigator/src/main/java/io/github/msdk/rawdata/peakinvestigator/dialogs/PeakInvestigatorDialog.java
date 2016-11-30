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

/**
 * Base interface of all dialogs used to present information or query for user
 * input.
 *
 */
public interface PeakInvestigatorDialog {

	public enum Status {
		ACCEPT, CANCEL
	}

	/**
	 * Show the dialog to the user.
	 * 
	 * @return Status indicating whether a particular query has been accepted or
	 *         canceled.
	 */
	Status show();
}
