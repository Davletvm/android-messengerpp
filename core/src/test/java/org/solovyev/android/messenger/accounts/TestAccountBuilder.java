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

package org.solovyev.android.messenger.accounts;


import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

public class TestAccountBuilder extends AbstractAccountBuilder<TestAccount, TestAccountConfiguration> {

	public TestAccountBuilder(@Nonnull Realm realm, @Nonnull TestAccountConfiguration configuration, @Nullable TestAccount editedAccount) {
		super(realm, configuration, editedAccount);
	}

	@Nonnull
	@Override
	protected MutableUser getAccountUser(@Nonnull String accountId) {
		final TestAccountConfiguration configuration = getConfiguration();
		String accountEntityId = "test_user";

		final Integer accountUserId = configuration.getAccountUserId();
		if(accountUserId != null) {
			accountEntityId += "_" + accountUserId;
		}
		final MutableUser user = newEmptyUser(newEntity(accountId, accountEntityId));
		user.setOnline(true);
		return user;
	}

	@Nonnull
	@Override
	protected TestAccount newAccount(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state, @Nonnull AccountSyncData syncData) {
		return new TestAccount(id, getRealm(), user, getConfiguration(), state);
	}

	@Override
	public void connect() throws ConnectionException {

	}

	@Override
	public void disconnect() throws ConnectionException {

	}

	@Override
	public void loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {

	}
}
