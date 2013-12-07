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

package org.solovyev.android.messenger;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import org.solovyev.android.TimeLoggingExecutor;
import org.solovyev.android.db.CommonSQLiteOpenHelper;
import org.solovyev.android.db.SQLiteOpenHelperConfiguration;
import org.solovyev.android.http.CachingImageLoader;
import org.solovyev.android.messenger.accounts.AccountDao;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.DefaultAccountService;
import org.solovyev.android.messenger.accounts.SqliteAccountDao;
import org.solovyev.android.messenger.accounts.connection.AccountConnections;
import org.solovyev.android.messenger.accounts.connection.AccountConnectionsService;
import org.solovyev.android.messenger.accounts.connection.DefaultAccountConnections;
import org.solovyev.android.messenger.accounts.connection.DefaultAccountConnectionsService;
import org.solovyev.android.messenger.chats.ChatDao;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.DefaultChatService;
import org.solovyev.android.messenger.chats.SqliteChatDao;
import org.solovyev.android.messenger.messages.DefaultMessageService;
import org.solovyev.android.messenger.messages.MessageDao;
import org.solovyev.android.messenger.messages.MessageService;
import org.solovyev.android.messenger.messages.SqliteMessageDao;
import org.solovyev.android.messenger.notifications.DefaultNotificationService;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.realms.DefaultRealmService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.realms.vk.registration.DummyRegistrationService;
import org.solovyev.android.messenger.registration.RegistrationService;
import org.solovyev.android.messenger.security.DefaultSecurityService;
import org.solovyev.android.messenger.security.MessengerSecurityService;
import org.solovyev.android.messenger.sync.DefaultSyncService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.DefaultUserService;
import org.solovyev.android.messenger.users.SqliteUserDao;
import org.solovyev.android.messenger.users.UserDao;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.android.network.NetworkStateServiceImpl;
import org.solovyev.tasks.TaskService;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

import static org.solovyev.tasks.Tasks.newTaskService;

public class MessengerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Executor.class).to(TimeLoggingExecutor.class);

		final Background background = new Background();
		bind(Background.class).toInstance(background);
		bind(TaskService.class).toInstance(newTaskService(background.getExecutor()));

		bind(MessengerSecurityService.class).to(DefaultSecurityService.class);
		bind(MessengerListeners.class).to(DefaultMessengerListeners.class);
		bind(ExceptionHandler.class).to(DefaultExceptionHandler.class);
		bind(NotificationService.class).to(DefaultNotificationService.class);
		bind(SQLiteOpenHelperConfiguration.class).to(DbConfiguration.class);
		bind(android.database.sqlite.SQLiteOpenHelper.class).to(SQLiteOpenHelper.class);

		bind(RealmService.class).to(DefaultRealmService.class);
		bind(AccountConnections.class).to(DefaultAccountConnections.class);
		bind(AccountService.class).to(DefaultAccountService.class);
		bind(AccountDao.class).to(SqliteAccountDao.class);

		bind(AccountConnectionsService.class).to(DefaultAccountConnectionsService.class);
		bind(Configuration.class).to(DefaultConfiguration.class);
		bind(org.solovyev.android.http.ImageLoader.class).to(ImageLoader.class);
		bind(NetworkStateService.class).to(NetworkStateServiceImpl.class).in(Scopes.SINGLETON);

		bind(UserDao.class).to(SqliteUserDao.class);
		bind(UserService.class).to(DefaultUserService.class);

		bind(ChatDao.class).to(SqliteChatDao.class);
		bind(ChatService.class).to(DefaultChatService.class);

		bind(MessageDao.class).to(SqliteMessageDao.class);
		bind(MessageService.class).to(DefaultMessageService.class);

		bind(SyncService.class).to(DefaultSyncService.class);
		bind(RegistrationService.class).to(DummyRegistrationService.class);

		bind(MultiPaneManager.class).to(DefaultMultiPaneManager.class);
	}

	@Singleton
	public static class SQLiteOpenHelper extends CommonSQLiteOpenHelper {

		@Inject
		public SQLiteOpenHelper(@Nonnull Application context,
								@Nonnull SQLiteOpenHelperConfiguration configuration) {
			super(context, configuration);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			db.execSQL("PRAGMA foreign_keys=ON");
		}
	}

	@Singleton
	public static class ImageLoader extends CachingImageLoader {

		@Inject
		public ImageLoader(@Nonnull Application context) {
			super(context, "messenger", App.getUiHandler());
		}
	}
}
