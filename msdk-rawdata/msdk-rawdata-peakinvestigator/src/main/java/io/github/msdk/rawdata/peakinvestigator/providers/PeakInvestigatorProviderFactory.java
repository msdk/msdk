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

package io.github.msdk.rawdata.peakinvestigator.providers;

import com.veritomyx.actions.InitAction;
import com.veritomyx.actions.PiVersionsAction;

public interface PeakInvestigatorProviderFactory {
	PeakInvestigatorOptionsProvider createOptionsProvider(PiVersionsAction action, int dataStart, int dataEnd);
	PeakInvestigatorInitProvider createInitProvider(InitAction action);
}
