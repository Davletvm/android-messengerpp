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

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.inject.Inject;

import org.solovyev.android.db.AbstractDbQuery;
import org.solovyev.android.db.AbstractSQLiteHelper;
import org.solovyev.android.db.Dao;
import org.solovyev.android.db.ListMapper;
import org.solovyev.android.db.SqliteDao;
import org.solovyev.android.db.SqliteDaoEntityMapper;
import org.solovyev.common.Converter;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import com.google.gson.Gson;
import com.google.inject.Singleton;

import static java.util.Collections.emptyList;
import static org.solovyev.android.db.AndroidDbUtils.doDbQuery;
import static org.solovyev.android.messenger.App.getExceptionHandler;
import static org.solovyev.android.messenger.App.getSecurityService;

@Singleton
public class SqliteAccountDao extends AbstractSQLiteHelper implements AccountDao {

	@Nonnull
	private Dao<Account> dao;

	@Nonnull
	private AccountDaoMapper daoMapper;

	@Inject
	public SqliteAccountDao(@Nonnull Application context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
	}

	@Override
	public void init() {
		daoMapper = new AccountDaoMapper(getSecurityService().getSecretKey());
		dao = new SqliteDao<Account>("accounts", "id", daoMapper, getContext(), getSqliteOpenHelper());
	}

	@Override
	public long create(@Nonnull Account account) throws AccountRuntimeException {
		return dao.create(account);
	}

	@Nullable
	@Override
	public Account read(@Nonnull String accountId) {
		return dao.read(accountId);
	}

	@Override
	public void deleteById(@Nonnull String accountId) {
		dao.deleteById(accountId);
	}

	@Nonnull
	@Override
	public Collection<Account> readAll() {
		try {
			return dao.readAll();
		} catch (AccountRuntimeException e) {
			getExceptionHandler().handleException(e);
			return emptyList();
		}
	}

	@Nonnull
	@Override
	public Collection<String> readAllIds() {
		return dao.readAllIds();
	}

	@Override
	public void deleteAll() {
		dao.deleteAll();
	}

	@Override
	public long update(@Nonnull Account account) throws AccountRuntimeException {
		return dao.update(account);
	}

	@Override
	public void delete(@Nonnull Account account) {
		dao.delete(account);
	}

	@Nonnull
	@Override
	public Collection<Account> loadAccountsInState(@Nonnull AccountState state) {
		try {
			return doDbQuery(getSqliteOpenHelper(), new LoadAccount(getContext(), state, getSqliteOpenHelper()));
		} catch (AccountRuntimeException e) {
			getExceptionHandler().handleException(e);
			return emptyList();
		}
	}

    /*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	private static class AccountDaoMapper implements SqliteDaoEntityMapper<Account> {

		@Nullable
		private final SecretKey secret;

		@Nonnull
		private final Converter<Cursor, Account> cursorMapper;

		private AccountDaoMapper(@Nullable SecretKey secret) {
			this.secret = secret;
			this.cursorMapper = new AccountMapper(secret);
		}

		@Nonnull
		@Override
		public ContentValues toContentValues(@Nonnull Account account) throws AccountRuntimeException {
			final ContentValues values = new ContentValues();

			values.put("id", account.getId());
			values.put("realm_id", account.getRealm().getId());
			values.put("user_id", account.getUser().getEntity().getEntityId());

			final AccountConfiguration configuration;

			try {
				final Cipherer<AccountConfiguration, AccountConfiguration> cipherer = account.getRealm().getCipherer();
				if (cipherer != null && secret != null) {
					configuration = cipherer.encrypt(secret, account.getConfiguration());
				} else {
					configuration = account.getConfiguration();
				}
				values.put("configuration", new Gson().toJson(configuration));
			} catch (CiphererException e) {
				throw new AccountRuntimeException(account.getId(), e);
			}

			values.put("state", account.getState().name());

			return values;
		}

		@Nonnull
		@Override
		public Converter<Cursor, Account> getCursorMapper() {
			return cursorMapper;
		}
	}

	private class LoadAccount extends AbstractDbQuery<Collection<Account>> {

		@Nonnull
		private final AccountState state;

		protected LoadAccount(@Nonnull Context context,
							  @Nonnull AccountState state,
							  @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.state = state;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			return db.query("accounts", null, "state = ?", new String[]{state.name()}, null, null, null);
		}

		@Nonnull
		@Override
		public Collection<Account> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<Account>(daoMapper.getCursorMapper()).convert(cursor);
		}
	}
}
