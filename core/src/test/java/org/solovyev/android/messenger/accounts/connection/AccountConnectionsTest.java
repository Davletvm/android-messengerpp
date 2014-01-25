/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.solovyev.android.messenger.Background;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountsTest;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricTestRunner.class)
public class AccountConnectionsTest {

	@Nonnull
	private DefaultAccountConnections connections;

	@Nonnull
	private Account account;

	@Nonnull
	private AccountConnection connection;

	@Nonnull
	private final Executor executor = new Executor() {
		@Override
		public void execute(@Nonnull Runnable command) {
			command.run();
		}
	};
	;

	@Before
	public void setUp() throws Exception {
		connections = new DefaultAccountConnections(application);
		connections.setExecutor(executor);
		final Background background = new Background();
		background.setHighPriorityExecutor(executor);
		background.setLowPriorityExecutor(executor);
		connections.setBackground(background);
		account = AccountsTest.newMockAccountWithStaticConnection();
		connection = AccountsTest.prepareStaticConnectionForAccount(account);
	}

	@Test
	public void testShouldStartConnectionIfNetworkConnectionExists() throws Exception {
		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(1)).start();
	}

	@Test
	public void testShouldNotStartConnectionIfNetworkConnectionDoesntExist() throws Exception {
		connections.startConnectionsFor(Arrays.asList(account), false);
		verify(connection, times(0)).start();
	}

	@Test
	public void testShouldNotStartConnectionIfConnectionIsRunning() throws Exception {
		when(connection.isStopped()).thenReturn(false);
		connections.startConnectionsFor(Arrays.asList(account), false);
		verify(connection, times(0)).start();
	}

	@Test
	public void testShouldReuseConnectionIfExists() throws Exception {
		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(1)).start();
		verify(account, times(1)).newConnection(any(Context.class));

		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(account, times(1)).newConnection(any(Context.class));
	}

	@Test
	public void testShouldRestartConnectionIfExistsAndStopped() throws Exception {
		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(1)).start();
		verify(account, times(1)).newConnection(any(Context.class));
		connection.stop();

		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(2)).start();
	}

	@Test
	public void testShouldNotRestartConnectionIfExistsAndNotStopped() throws Exception {
		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(1)).start();
		verify(account, times(1)).newConnection(any(Context.class));

		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(1)).start();
	}

	@Test
	public void testShouldNotRestartConnectionIfExistsAndStoppedButNoInternet() throws Exception {
		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(1)).start();
		verify(account, times(1)).newConnection(any(Context.class));
		connection.stop();

		connections.startConnectionsFor(Arrays.asList(account), false);
		verify(connection, times(1)).start();
	}

	@Test
	public void testShouldStopAllConnections() throws Exception {
		final Connections c = new Connections(10);

		this.connections.startConnectionsFor(c.accounts, true);
		for (int i = 0; i < c.count; i++) {
			final AccountConnection connection = c.getConnection(i);
			verify(connection, times(1)).start();
			assertFalse(connection.isStopped());
		}

		for (int i = 0; i < c.count / 2; i++) {
			c.getConnection(i).stop();
		}

		this.connections.tryStopAll();
		c.assertAllStopped();
	}

	@Test
	public void testShouldStopInternetDependantConnections() throws Exception {
		final Connections c = new Connections(10);

		int runningUpTo = c.count / 2;
		for (int i = 0; i < runningUpTo; i++) {
			when(c.getConnection(i).isInternetConnectionRequired()).thenReturn(false);
		}

		connections.startConnectionsFor(c.accounts, true);
		c.assertAllRunning();

		connections.onNoInternetConnection();
		c.assertRunningUpTo(runningUpTo);
		c.assertStoppedFrom(runningUpTo);
	}

	@Test
	public void testShouldStopConnectionsFromAccount() throws Exception {
		final Connections c = new Connections(10);

		connections.startConnectionsFor(c.accounts, true);
		c.assertAllRunning();

		int stoppedUpTo = c.count / 2;
		tryStopAndCheck(c, stoppedUpTo);

		// check if stop called only once
		tryStopAndCheck(c, stoppedUpTo);
	}

	private void tryStopAndCheck(Connections c, int stoppedUpTo) {
		for (int i = 0; i < stoppedUpTo; i++) {
			connections.tryStopFor(c.getAccount(i));
		}
		c.assertStoppedUpTo(stoppedUpTo);
		c.assertRunningFrom(stoppedUpTo);
	}

	@Test
	public void testShouldStartAllStoppedConnections() throws Exception {
		final Connections c = new Connections(10);
		connections.startConnectionsFor(c.accounts, true);
		connections.onNoInternetConnection();
		c.assertAllStopped();

		connections.tryStartAll(true);
		c.assertAllRunning();
	}

	@Test
	public void testShouldNotStartNotStoppedConnections() throws Exception {
		final Connections c = new Connections(10);
		connections.startConnectionsFor(c.accounts, true);

		int stoppedUpTo = c.count / 2;
		for (int i = 0; i < stoppedUpTo; i++) {
			c.getConnection(i).stop();
		}

		connections.tryStartAll(true);
		c.assertAllRunning();
		for (int i = 0; i < stoppedUpTo; i++) {
			verify(c.getConnection(i), times(2)).start();
		}

		for (int i = stoppedUpTo; i < c.count; i++) {
			verify(c.getConnection(i), times(1)).start();
		}
	}

	@Test
	public void testShouldRemoveConnectionForAccount() throws Exception {
		final Connections c = new Connections(10);
		connections.startConnectionsFor(c.accounts, true);

		connections.removeConnectionFor(c.getAccount(0));
		c.assertRunningFrom(1);
		c.assertStoppedUpTo(1);

		// restart should not restart removed connections
		connections.tryStopAll();
		connections.tryStartAll(true);

		c.assertRunningFrom(1);
		c.assertStoppedUpTo(1);
	}

	@Test
	public void testShouldUpdateConnectionForAccount() throws Exception {
		final Connections c = new Connections(10);
		connections.startConnectionsFor(c.accounts, true);

		connections.restartConnectionForChangedAccount(c.getAccount(0), true);
		c.assertAllRunning();

		verify(c.getConnection(0), times(1)).stop();
		for (int i = 1; i < c.count; i++) {
			verify(c.getConnection(i), times(0)).stop();
		}

		verify(c.getConnection(0), times(2)).start();
		for (int i = 1; i < c.count; i++) {
			verify(c.getConnection(i), times(1)).start();
		}
	}

	private static final class Connections {
		@Nonnull
		private final List<Account> accounts = new ArrayList<Account>();

		@Nonnull
		private final List<AccountConnection> connections = new ArrayList<AccountConnection>();

		private final int count;

		private Connections(int count) {
			this.count = count;
			for (int i = 0; i < count; i++) {
				final Account account = AccountsTest.newMockAccountWithStaticConnection();
				accounts.add(account);
				connections.add(account.newConnection(application));
			}
		}

		@Nonnull
		public AccountConnection getConnection(int i) {
			return this.connections.get(i);
		}

		@Nonnull
		public Account getAccount(int i) {
			return this.accounts.get(i);
		}

		public void assertAllStopped() {
			assertStoppedUpTo(this.count);
		}

		public void assertStoppedFrom(int from) {
			for (int i = from; i < this.count; i++) {
				final AccountConnection connection = connections.get(i);
				verify(connection, times(1)).stop();
				assertTrue(connection.isStopped());
			}
		}


		public void assertStoppedUpTo(int upTo) {
			for (int i = 0; i < upTo; i++) {
				final AccountConnection connection = connections.get(i);
				verify(connection, atLeastOnce()).stop();
				assertTrue(connection.isStopped());
			}
		}

		public void assertAllRunning() {
			assertRunningUpTo(this.count);
		}

		public void assertRunningFrom(int from) {
			for (int i = from; i < this.count; i++) {
				final AccountConnection connection = connections.get(i);
				assertFalse(connection.isStopped());
			}
		}

		public void assertRunningUpTo(int upTo) {
			for (int i = 0; i < upTo; i++) {
				final AccountConnection connection = connections.get(i);
				assertFalse(connection.isStopped());
			}
		}
	}
}
