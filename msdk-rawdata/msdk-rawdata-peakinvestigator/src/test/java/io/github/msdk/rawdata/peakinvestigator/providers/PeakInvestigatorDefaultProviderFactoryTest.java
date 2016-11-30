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

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.equalTo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.veritomyx.actions.InitAction;
import com.veritomyx.actions.PiVersionsAction;

import io.github.msdk.rawdata.peakinvestigator.providers.PeakInvestigatorDefaultProviderFactory;
import io.github.msdk.rawdata.peakinvestigator.providers.PeakInvestigatorProvider;
import io.github.msdk.rawdata.peakinvestigator.providers.PeakInvestigatorProviderFactory;
import io.github.msdk.rawdata.peakinvestigator.providers.PeakInvestigatorInitProvider;
import io.github.msdk.rawdata.peakinvestigator.providers.PeakInvestigatorOptionsProvider;

public class PeakInvestigatorDefaultProviderFactoryTest {

	private final PeakInvestigatorProviderFactory factory = new PeakInvestigatorDefaultProviderFactory();
	private final int DATA_START = 100;
	private final int DATA_END = 2000;

	@Test
	public void testVersionNoLastUsed() {
		PiVersionsAction action = mock(PiVersionsAction.class);
		when(action.getLastUsedVersion()).thenReturn("");
		when(action.getCurrentVersion()).thenReturn("1.3");

		PeakInvestigatorOptionsProvider provider = factory.createOptionsProvider(action, DATA_START, DATA_END);
		assertThat(provider.show(), equalTo(PeakInvestigatorProvider.Status.ACCEPT));
		assertThat(provider.getVersion(), equalTo("1.3"));
		assertThat(provider.getStartMass(), equalTo(DATA_START));
		assertThat(provider.getEndMass(), equalTo(DATA_END));
	}

	@Test
	public void testVersionWithLastUsed() {
		PiVersionsAction action = mock(PiVersionsAction.class);
		when(action.getLastUsedVersion()).thenReturn("1.3");
		when(action.getCurrentVersion()).thenReturn("2.0");

		PeakInvestigatorOptionsProvider provider = factory.createOptionsProvider(action, DATA_START, DATA_END);
		assertThat(provider.show(), equalTo(PeakInvestigatorProvider.Status.ACCEPT));
		assertThat(provider.getVersion(), equalTo("1.3"));
		assertThat(provider.getStartMass(), equalTo(DATA_START));
		assertThat(provider.getEndMass(), equalTo(DATA_END));
	}

	@Test
	public void testInitWithSufficientFunds() {
		InitAction action = mock(InitAction.class);
		when(action.getFunds()).thenReturn(100.0);
		when(action.getMaxPotentialCost("RTO-24")).thenReturn(50.0);

		PeakInvestigatorInitProvider provider = factory.createInitProvider(action);
		assertThat(provider.show(), equalTo(PeakInvestigatorProvider.Status.ACCEPT));
		assertThat(provider.getRto(), equalTo("RTO-24"));
	}

	@Test
	public void testInitWithoutSufficientFunds() {
		InitAction action = mock(InitAction.class);
		when(action.getFunds()).thenReturn(10.0);
		when(action.getMaxPotentialCost("RTO-24")).thenReturn(50.0);

		PeakInvestigatorInitProvider provider = factory.createInitProvider(action);
		assertThat(provider.show(), equalTo(PeakInvestigatorProvider.Status.CANCEL));
	}
}
