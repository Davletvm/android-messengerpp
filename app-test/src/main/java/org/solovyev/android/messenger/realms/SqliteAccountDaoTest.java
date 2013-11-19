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

package org.solovyev.android.messenger.realms;

import com.google.inject.Inject;
import junit.framework.Assert;
import org.solovyev.android.messenger.BaseInstrumentationTest;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.AccountDao;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.realms.sms.SmsRealm;
import org.solovyev.android.messenger.realms.vk.VkAccountConfiguration;
import org.solovyev.android.messenger.realms.vk.VkRealm;
import org.solovyev.android.messenger.realms.xmpp.CustomXmppRealm;
import org.solovyev.android.messenger.realms.xmpp.XmppAccountConfiguration;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.messenger.users.Users;

import java.util.Collection;

import static com.google.common.collect.Iterables.getFirst;
import static java.util.Arrays.asList;
import static org.solovyev.android.messenger.realms.Realms.makeAccountId;

public class SqliteAccountDaoTest extends BaseInstrumentationTest {

	@Inject
	private UserService userService;

	@Inject
	private AccountDao dao;

	@Inject
	private VkRealm vkRealm;

	@Inject
	private CustomXmppRealm xmppRealm;

	@Inject
	private SmsRealm smsRealm;

	public void setUp() throws Exception {
		super.setUp();
		getDao().deleteAll();
	}

	public void testVkAccountShouldBeSaved() throws Exception {
		Collection<Account> accounts = getDao().readAll();
		Assert.assertTrue(accounts.isEmpty());

		final VkAccountConfiguration expectedConfiguration = new VkAccountConfiguration("login", "password");
		expectedConfiguration.setAccessParameters("token", "user_id");
		final Account expectedAccount = vkRealm.newAccount("test~01", Users.newEmptyUser(Entities.newEntity("test~01", "user01")), expectedConfiguration, AccountState.enabled);
		getDao().create(expectedAccount);

		accounts = getDao().readAll();
		assertTrue(accounts.size() == 1);
		final Account<VkAccountConfiguration> actualAccount = getFirst(accounts, null);
		assertNotNull(actualAccount);
		assertTrue(expectedAccount.same(actualAccount));
		assertTrue(actualAccount.getConfiguration().isSame(expectedConfiguration));
		assertEquals("login", actualAccount.getConfiguration().getLogin());

		// password should be cleared as we have access token instead
		assertEquals("", actualAccount.getConfiguration().getPassword());
		assertEquals("token", actualAccount.getConfiguration().getAccessToken());
		assertEquals("user_id", actualAccount.getConfiguration().getUserId());

		getDao().deleteById(expectedAccount.getId());

		accounts = getDao().readAll();
		assertTrue(accounts.isEmpty());
	}

	public void testConcreteRealms() throws Exception {
		int index = 0;
		for (Realm realm : asList(vkRealm, xmppRealm, smsRealm)) {
			final AccountConfiguration configuration = (AccountConfiguration) realm.getConfigurationClass().newInstance();
			if (realm == vkRealm) {
				((VkAccountConfiguration) configuration).setAccessParameters("test", "test");
			} else if (realm == xmppRealm) {
				((XmppAccountConfiguration) configuration).setPassword("test");
			}
			final String accountId = makeAccountId(realm.getId(), index);
			final Account account = realm.newAccount(accountId, Users.newEmptyUser(Entities.newEntity(accountId, String.valueOf(index))), configuration, AccountState.enabled);
			getDao().create(account);
		}

		final Collection<Account> accounts = getDao().readAll();
		assertTrue(accounts.size() == 3);
	}

	private AccountDao getDao() {
		return new UserSavingAccountDao(userService, dao);
	}

	public void tearDown() throws Exception {
		getDao().deleteAll();
		super.tearDown();
	}
}
