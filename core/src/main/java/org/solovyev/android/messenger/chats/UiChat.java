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

package org.solovyev.android.messenger.chats;

import com.google.common.base.Predicate;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.collect.Iterables.any;
import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.App.getChatService;

/**
 * Chat for UI, contains additional parameters like user, last message to be shown on UI
 */
public final class UiChat implements Identifiable, EntityAware {

	@Nonnull
	private User user;

	@Nonnull
	private Chat chat;

	private int unreadMessagesCount;

	@Nullable
	private Account account;

	@Nullable
	private Message lastMessage;

	// precached display name in order to calculate it before shown (e.g. for sorting)
	@Nonnull
	private String displayName;

	private boolean online;

	private UiChat(@Nonnull User user, @Nonnull Chat chat, @Nullable Account account, @Nullable Message lastMessage, int unreadMessagesCount, @Nonnull String displayName, boolean online) {
		this.user = user;
		this.chat = chat;
		this.account = account;
		this.lastMessage = lastMessage;
		this.unreadMessagesCount = unreadMessagesCount;
		this.displayName = displayName;
		this.online = online;
	}

	@Nonnull
	static UiChat newUiChat(@Nonnull User user, @Nonnull Chat chat, @Nullable Account account, @Nullable Message lastMessage, int unreadMessagesCount, @Nonnull String displayName, boolean online) {
		return new UiChat(user, chat, account, lastMessage, unreadMessagesCount, displayName, online);
	}

	@Nonnull
	static UiChat loadUiChat(@Nonnull User user, @Nonnull Chat chat, @Nullable Account account) {
		final Message lastMessage = getLastMessage(chat);
		final int unreadMessagesCount = getUnreadMessagesCount(chat);
		final String displayName = Chats.getDisplayName(chat, lastMessage, unreadMessagesCount);
		final boolean online = isParticipantsOnline(user, chat);

		return new UiChat(user, chat, account, lastMessage, unreadMessagesCount, displayName, online);
	}

	@Nonnull
	static UiChat loadUiChat(@Nonnull User user, @Nonnull Chat chat) {
		final Account account = getAccountService().getAccountByEntity(user.getEntity());
		return loadUiChat(user, chat, account);
	}

	@Nonnull
	static UiChat loadUiChat(@Nonnull Chat chat) {
		final Account account = getAccountService().getAccountByEntity(chat.getEntity());
		return loadUiChat(account.getUser(), chat, account);
	}

	@Nonnull
	public static UiChat newEmptyUiChat(@Nonnull User user, @Nonnull Chat chat) {
		return newUiChat(user, chat, null, null, 0, "", false);
	}

	@Nullable
	private static Message getLastMessage(Chat chat) {
		return getChatService().getLastMessage(chat.getEntity());
	}

	private static int getUnreadMessagesCount(@Nonnull Chat chat) {
		return getChatService().getUnreadMessagesCount(chat.getEntity());
	}

	private static boolean isParticipantsOnline(@Nonnull User user, @Nonnull Chat chat) {
		final Iterable<User> participants = getChatService().getParticipantsExcept(chat.getEntity(), user.getEntity());

		return any(participants, new Predicate<User>() {
			@Override
			public boolean apply(User participant) {
				return participant.isOnline();
			}
		});
	}

	@Nonnull
	public User getUser() {
		return user;
	}

	@Nonnull
	public Chat getChat() {
		return chat;
	}

	@Nullable
	public Message getLastMessage() {
		return lastMessage;
	}

	public boolean isOnline() {
		return online;
	}

	@Nonnull
	public Account getAccount() {
		if (account == null) {
			account = App.getAccountService().getAccountByEntity(chat.getEntity());
		}
		return account;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final UiChat that = (UiChat) o;

		if (!chat.equals(that.chat)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return chat.hashCode();
	}

	public int getUnreadMessagesCount() {
		return unreadMessagesCount;
	}

	@Nonnull
	@Override
	public String getId() {
		return this.chat.getId();
	}

	@Nonnull
	String getDisplayName() {
		return displayName;
	}


	public void setChat(@Nonnull Chat chat) {
		this.chat = chat;
	}

	public void setLastMessage(@Nullable Message lastMessage) {
		this.lastMessage = lastMessage;
	}

	public void setUnreadMessagesCount(int unreadMessagesCount) {
		this.unreadMessagesCount = unreadMessagesCount;
	}

	public boolean setOnline(boolean online) {
		if (this.online != online) {
			this.online = online;
			return true;
		}

		return false;
	}

	public boolean updateOnlineStatus() {
		return setOnline(isParticipantsOnline(this.user, this.chat));
	}

	@Nonnull
	@Override
	public Entity getEntity() {
		return getChat().getEntity();
	}
}
