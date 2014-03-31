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

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.db.*;
import org.solovyev.android.db.properties.PropertyByIdDbQuery;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.db.StringIdMapper;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Iterables.getFirst;
import static org.solovyev.android.db.AndroidDbUtils.*;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.messages.MessageState.removed;


@Singleton
public class SqliteMessageDao extends AbstractSQLiteHelper implements MessageDao {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private ChatService chatService;

	@Inject
	@Nonnull
	private UserService userService;

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	@Nonnull
	private final Dao<Message> dao;

	@Nonnull
	private final MessageMapper mapper = new MessageMapper(SqliteMessageDao.this);

	@Inject
	public SqliteMessageDao(@Nonnull Application context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
		this.dao = new SqliteDao<Message>("messages", "id", new MessageDaoMapper(), context, sqliteOpenHelper);
	}

	@Nonnull
	@Override
	public List<String> readMessageIds(@Nonnull String chatId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadMessageIdsByChatId(getContext(), chatId, getSqliteOpenHelper()));
	}

	@Override
	public long create(@Nonnull Message message) {
		final long result = dao.create(message);
		if (result != DbExec.SQL_ERROR) {
			doDbExec(getSqliteOpenHelper(), new InsertProperties(message));
		}
		return result;
	}

	@Nullable
	@Override
	public Message read(@Nonnull String messageId) {
		return dao.read(messageId);
	}

	@Nonnull
	@Override
	public Collection<Message> readAll() {
		return dao.readAll();
	}

	@Nonnull
	@Override
	public Collection<String> readAllIds() {
		return dao.readAllIds();
	}

	@Override
	public long update(@Nonnull Message message) {
		final long rows = dao.update(message);
		if (rows > 0) {
			// message exists => can remove/insert properties
			doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new DeleteProperties(message), new InsertProperties(message)));
		}
		return rows;
	}

	@Override
	public void delete(@Nonnull Message message) {
		dao.delete(message);
	}

	@Override
	public void deleteById(@Nonnull String id) {
		dao.deleteById(id);
	}

	@Nonnull
	@Override
	public List<Message> readMessages(@Nonnull String chatId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadMessages(getContext(), chatId, getSqliteOpenHelper()));
	}

	@Nonnull
	@Override
	public String getOldestMessageForChat(@Nonnull String chatId) {
		return doDbQuery(getSqliteOpenHelper(), new OldestMessageLoader(getContext(), getSqliteOpenHelper(), chatId));
	}

	@Nullable
	@Override
	public Message readLastMessage(@Nonnull String chatId) {
		final String lastMessageId = doDbQuery(getSqliteOpenHelper(), new LastMessageLoader(getContext(), getSqliteOpenHelper(), chatId));
		if (!Strings.isEmpty(lastMessageId)) {
			final List<Message> messages = doDbQuery(getSqliteOpenHelper(), new LoadMessage(getContext(), lastMessageId, getSqliteOpenHelper()));
			return getFirst(messages, null);
		} else {
			return null;
		}
	}

	@Override
	public int getUnreadMessagesCount() {
		return doDbQuery(getSqliteOpenHelper(), new UnreadMessagesCountLoader(getContext(), getSqliteOpenHelper()));
	}

	@Override
	public boolean changeReadStatus(@Nonnull String messageId, boolean read) {
		final Long rows = doDbExec(getSqliteOpenHelper(), new ReadStatusUpdater(messageId, read));
		return rows != 0;
	}

	@Override
	public boolean changeMessageState(@Nonnull String messageId, @Nonnull MessageState state) {
		final Long rows = doDbExec(getSqliteOpenHelper(), new StateUpdater(messageId, state));
		return rows != 0;
	}

	@Override
	public void deleteAll() {
		doDbExec(getSqliteOpenHelper(), DeleteAllRowsDbExec.newInstance("messages"));
	}

	@Nonnull
	@Override
	public List<AProperty> readPropertiesById(@Nonnull String messageId) {
		return doDbQuery(getSqliteOpenHelper(), new LoadPropertiesDbQuery(messageId, getContext(), getSqliteOpenHelper()));
	}

	@Nullable
	@Override
	public Message readSameMessage(@Nonnull String body, @Nonnull DateTime sendTime, @Nonnull Entity author, @Nonnull Entity recipient) {
		return getFirst(doDbQuery(getSqliteOpenHelper(), new LoadSameMessage(body, sendTime, author, recipient)), null);
	}

	@Nonnull
	@Override
	public MessagesMergeDaoResult mergeMessages(@Nonnull String chatId, @Nonnull Collection<? extends Message> messages) {
		final MessagesMergeDaoResult result = new MessagesMergeDaoResult();

		final Chat chat = getChatService().getChatById(newEntityFromEntityId(chatId));

		if (chat != null) {
			for (Message message : messages) {
				final Message messageFromDb = read(message.getId());
				if (messageFromDb == null) {
					result.addAddedMessage(message);
				} else {
					final Message mergedMessage = messageFromDb.merge(message);
					result.addUpdatedMessage(mergedMessage);
					final boolean wasRead = messageFromDb.isRead();
					final boolean nowRead = mergedMessage.isRead();
					if (!wasRead && nowRead) {
						result.addReadMessage(mergedMessage);
					}
				}
			}

			final List<DbExec> execs = new ArrayList<DbExec>();

			for (Message updatedMessage : result.getUpdatedObjects()) {
				execs.add(new UpdateMessage(updatedMessage));
				execs.add(new DeleteProperties(updatedMessage));
				execs.add(new InsertProperties(updatedMessage));
			}

			for (Message addedMessage : result.getAddedObjects()) {
				execs.add(new InsertMessage(addedMessage));
				execs.add(new InsertProperties(addedMessage));
			}

			doDbExecs(getSqliteOpenHelper(), execs);
		}

		return result;
	}

	@Nonnull
	private ChatService getChatService() {
		return this.chatService;
	}

	public void setChatService(@Nonnull ChatService chatService) {
		this.chatService = chatService;
	}

	public void setUserService(@Nonnull UserService userService) {
		this.userService = userService;
	}

	/*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	private static class LoadMessageIdsByChatId extends AbstractDbQuery<List<String>> {

		@Nonnull
		private final String chatId;

		private LoadMessageIdsByChatId(@Nonnull Context context, @Nonnull String chatId, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.chatId = chatId;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("messages", null, "chat_id = ?", new String[]{chatId}, null, null, null);
		}

		@Nonnull
		@Override
		public List<String> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<String>(StringIdMapper.getInstance()).convert(cursor);
		}
	}

	public static final class InsertMessage extends AbstractObjectDbExec<Message> {

		public InsertMessage(@Nullable Message message) {
			super(message);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Message message = getNotNullObject();

			final ContentValues values = toContentValues(message);

			return db.insert("messages", null, values);
		}
	}

	private static final class UpdateMessage extends AbstractObjectDbExec<Message> {

		private UpdateMessage(@Nonnull Message message) {
			super(message);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Message message = getNotNullObject();

			final ContentValues values = toContentValues(message);

			return db.update("messages", values, "id = ?", new String[]{String.valueOf(message.getEntity().getEntityId())});
		}
	}

	private class MessageDaoMapper implements SqliteDaoEntityMapper<Message> {

		@Nonnull
		@Override
		public ContentValues toContentValues(@Nonnull Message message) {
			return SqliteMessageDao.toContentValues(message);
		}

		@Nonnull
		@Override
		public Converter<Cursor, Message> getCursorMapper() {
			return mapper;
		}
	}


	private final class LoadMessages extends AbstractDbQuery<List<Message>> {

		@Nonnull
		private final String chatId;

		private LoadMessages(@Nonnull Context context,
							 @Nonnull String chatId,
							 @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.chatId = chatId;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("messages", null, "chat_id = ? and state <> ?", new String[]{chatId, removed.name()}, null, null, null);
		}

		@Nonnull
		@Override
		public List<Message> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<Message>(mapper).convert(cursor);
		}
	}

	private final class LoadMessage extends AbstractDbQuery<List<Message>> {

		@Nonnull
		private final String messageId;

		private LoadMessage(@Nonnull Context context,
							@Nonnull String messageId,
							@Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.messageId = messageId;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("messages", null, "id = ? ", new String[]{String.valueOf(messageId)}, null, null, null);
		}

		@Nonnull
		@Override
		public List<Message> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<Message>(mapper).convert(cursor);
		}
	}

	private static class OldestMessageLoader extends AbstractDbQuery<String> {

		@Nonnull
		private String chatId;

		protected OldestMessageLoader(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper, @Nonnull String chatId) {
			super(context, sqliteOpenHelper);
			this.chatId = chatId;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.rawQuery("select id from messages where chat_id = ? and state <> ? order by send_time asc", new String[]{chatId, removed.name()});
		}

		@Nonnull
		@Override
		public String retrieveData(@Nonnull Cursor cursor) {
			if (cursor.moveToFirst()) {
				return cursor.getString(0);
			} else {
				return "";
			}
		}
	}

	private static class LastMessageLoader extends AbstractDbQuery<String> {

		@Nonnull
		private String chatId;

		protected LastMessageLoader(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper, @Nonnull String chatId) {
			super(context, sqliteOpenHelper);
			this.chatId = chatId;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.rawQuery("select id from messages where chat_id = ? and state <> ? order by send_time desc", new String[]{chatId, removed.name()});
		}

		@Nonnull
		@Override
		public String retrieveData(@Nonnull Cursor cursor) {
			if (cursor.moveToFirst()) {
				return cursor.getString(0);
			} else {
				return "";
			}
		}
	}

	@Nonnull
	private static ContentValues toContentValues(@Nonnull Message message) {
		final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

		final ContentValues values = new ContentValues();

		final Entity entity = message.getEntity();
		values.put("id", entity.getEntityId());
		values.put("account_id", entity.getAccountId());
		values.put("account_message_id", entity.getAccountEntityId());

		values.put("chat_id", message.getChat().getEntityId());
		values.put("author_id", message.getAuthor().getEntityId());
		final Entity recipient = message.getRecipient();
		values.put("recipient_id", recipient == null ? null : recipient.getEntityId());
		values.put("send_date", dateTimeFormatter.print(message.getSendDate()));
		values.put("send_time", message.getSendDate().getMillis());
		values.put("title", message.getTitle());
		values.put("body", message.getBody());
		values.put("read", message.isRead() ? 1 : 0);
		values.put("state", message.getState().name());
		return values;
	}

	private static class UnreadMessagesCountLoader extends AbstractDbQuery<Integer> {

		private UnreadMessagesCountLoader(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.rawQuery("select count(*) from messages where read = 0 and and state <> ?", new String[]{removed.name()});
		}

		@Nonnull
		@Override
		public Integer retrieveData(@Nonnull Cursor cursor) {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0);
			} else {
				return 0;
			}
		}
	}

	private static class ReadStatusUpdater implements DbExec {

		@Nonnull
		private final String messageId;

		private final boolean read;

		private ReadStatusUpdater(@Nonnull String messageId, boolean read) {
			this.messageId = messageId;
			this.read = read;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final ContentValues values = new ContentValues();
			final int newReadValue = read ? 1 : 0;
			values.put("read", newReadValue);
			return db.update("messages", values, "id = ? and read <> ?", new String[]{messageId, String.valueOf(newReadValue)});
		}
	}

	private static class StateUpdater implements DbExec {

		@Nonnull
		private final String messageId;

		@Nonnull
		private final MessageState state;

		private StateUpdater(@Nonnull String messageId, @Nonnull MessageState state) {
			this.messageId = messageId;
			this.state = state;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final ContentValues values = new ContentValues();
			values.put("state", state.name());
			return db.update("messages", values, "id = ?", new String[]{messageId});
		}
	}

	private static final class LoadPropertiesDbQuery extends PropertyByIdDbQuery {

		public LoadPropertiesDbQuery(@Nonnull String messageId, @Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper, "message_properties", "message_id", messageId);
		}
	}

	private static final class DeleteProperties extends AbstractObjectDbExec<Message> {

		private DeleteProperties(@Nonnull Message message) {
			super(message);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Message message = getNotNullObject();

			return db.delete("message_properties", "message_id = ?", new String[]{String.valueOf(message.getEntity().getEntityId())});
		}
	}

	private static final class InsertProperties extends AbstractObjectDbExec<Message> {

		private InsertProperties(@Nonnull Message message) {
			super(message);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			long result = 0;

			final Message message = getNotNullObject();

			for (AProperty property : message.getProperties().getPropertiesCollection()) {
				final ContentValues values = new ContentValues();
				final String value = property.getValue();
				if (value != null) {
					values.put("message_id", message.getEntity().getEntityId());
					values.put("property_name", property.getName());
					values.put("property_value", value);
					final long id = db.insert("message_properties", null, values);
					if (id == DbExec.SQL_ERROR) {
						result = DbExec.SQL_ERROR;
					}
				}
			}

			return result;
		}
	}

	private class LoadSameMessage implements DbQuery<List<Message>> {

		@Nonnull
		private final String body;

		@Nonnull
		private final DateTime sendTime;

		@Nonnull
		private final Entity author;

		@Nonnull
		private final Entity recipient;

		public LoadSameMessage(@Nonnull String body, @Nonnull DateTime sendTime, @Nonnull Entity author, @Nonnull Entity recipient) {
			this.body = body;
			this.sendTime = sendTime;
			this.author = author;
			this.recipient = recipient;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			final String sendTime = String.valueOf(this.sendTime.getMillis());
			return db.query("messages", null, "body = ? and author_id = ? and recipient_id = ? and abs(send_time - ?) < 60000", new String[]{body, author.getEntityId(), recipient.getEntityId(), sendTime}, null, null, null);
		}

		@Nonnull
		@Override
		public List<Message> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<Message>(mapper).convert(cursor);
		}
	}
}
