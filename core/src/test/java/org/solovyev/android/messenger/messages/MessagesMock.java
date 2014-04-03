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

package org.solovyev.android.messenger.messages;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.messages.MessageState.created;
import static org.solovyev.android.properties.Properties.newProperties;

public class MessagesMock {

	@Nonnull
	private static final AtomicInteger counter = new AtomicInteger();

	@Nonnull
	public static Message newMockMessage() {
		return newMockMessage(DateTime.now());
	}

	@Nonnull
	public static Message newMockMessage(@Nonnull DateTime sendDate) {
		final Entity from = mock(Entity.class);
		final Entity to = mock(Entity.class);

		return newMockMessage(sendDate, from, to, "test", "test:test");
	}

	@Nonnull
	public static Message newMockMessage(@Nonnull DateTime sendDate,
										 @Nonnull Entity from,
										 @Nonnull Entity to,
										 @Nonnull Account account,
										 @Nonnull String chatId) {
		return newMockMessage(sendDate, from, to, account.getId(), chatId);
	}

	@Nonnull
	public static Message newMockMessage(@Nonnull DateTime sendDate,
										 @Nonnull Entity from,
										 @Nonnull Entity to,
										 @Nonnull String accountId,
										 @Nonnull String chatId) {
		final Message message = mock(Message.class);

		final String id = getMessageId();
		when(message.getEntity()).thenReturn(newEntity(accountId, id));
		when(message.getId()).thenReturn(String.valueOf(id));
		when(message.getBody()).thenReturn("body_" + id);
		when(message.getTitle()).thenReturn("title_" + id);

		when(message.getAuthor()).thenReturn(from);

		when(message.getRecipient()).thenReturn(to);
		when(message.getState()).thenReturn(created);

		when(message.getSendDate()).thenReturn(sendDate);
		final DateTime localDateTime = sendDate.toDateTime(DateTimeZone.forTimeZone(TimeZone.getDefault()));
		when(message.getLocalSendDateTime()).thenReturn(localDateTime);
		when(message.getLocalSendDate()).thenReturn(localDateTime.toLocalDate());
		final MutableAProperties properties = newProperties(Collections.<AProperty>emptyList());
		properties.setProperty("property_1", "test");
		properties.setProperty("property_2", "42");
		when(message.getProperties()).thenReturn(properties);
		when(message.getChat()).thenReturn(newEntityFromEntityId(chatId));
		return message;
	}

	static String getMessageId() {
		return String.valueOf(counter.getAndIncrement());
	}
}
