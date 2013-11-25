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

package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import com.google.common.base.Function;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountRuntimeException;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.realms.xmpp.XmppAccountUserService.logUserPresence;

class XmppRosterListener implements RosterListener {

	@Nonnull
	private static final String TAG = "M++/XmppRosterListener";

	@Nonnull
	private final XmppAccount account;

	XmppRosterListener(@Nonnull XmppAccount account) {
		this.account = account;
	}

	@Override
	public void entriesAdded(@Nonnull Collection<String> contactIds) {
		Log.d(TAG, "entriesAdded() called");
		final AccountUserService aus = account.getAccountUserService();
		final List<User> contacts;
		try {
			contacts = newArrayList(transform(contactIds, new Function<String, User>() {
				@Override
				public User apply(@Nullable String contactId) {
					assert contactId != null;
					// we need to request new user entity because user id should be prepared properly
					final Entity entity = account.newUserEntity(contactId);
					try {
						return aus.getUserById(entity.getAccountEntityId());
					} catch (AccountException e) {
						throw new AccountRuntimeException(e);
					}
				}
			}));

			// we cannot allow delete because we don't know if user is really deleted on remote server - we only know that his presence was changed
			getUserService().mergeContacts(account, contacts, false, true);

		} catch (AccountRuntimeException e) {
			App.getExceptionHandler().handleException(new AccountException(e));
		}
	}

	@Override
	public void entriesUpdated(@Nonnull Collection<String> contactIds) {
		Log.d(TAG, "entriesUpdated() called");
	}

	@Override
	public void entriesDeleted(@Nonnull Collection<String> contactIds) {
		Log.d(TAG, "entriesDeleted() called");
	}

	@Override
	public void presenceChanged(@Nonnull final Presence presence) {
		final String accountUserId = presence.getFrom();

		final User contact = getUserService().getUserById(account.newUserEntity(accountUserId));
		final boolean online = presence.isAvailable();
		logUserPresence("XmppRosterListener", account, online, contact.getLogin());
		getUserService().onContactPresenceChanged(account.getUser(), contact, online);
	}

	@Nonnull
	private UserService getUserService() {
		return App.getUserService();
	}
}
