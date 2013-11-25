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

package org.solovyev.android.messenger.realms.sms;

import android.database.Cursor;
import android.provider.ContactsContract;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class SmsUserEmailMapper implements Converter<Cursor, User> {

	static final String[] COLUMNS = new String[]{
			ContactsContract.CommonDataKinds.Email.CONTACT_ID,
			ContactsContract.CommonDataKinds.Email.DATA,
			ContactsContract.Contacts.DISPLAY_NAME};

	private SmsAccount realm;

	public SmsUserEmailMapper(@Nonnull SmsAccount realm) {
		this.realm = realm;
	}

	@Nonnull
	@Override
	public User convert(@Nonnull Cursor cursor) {
		final String userId = cursor.getString(0);
		final String email = cursor.getString(1);

		final List<AProperty> properties = new ArrayList<AProperty>();
		Users.tryParseNameProperties(properties, cursor.getString(2));
		properties.add(Properties.newProperty(User.PROPERTY_EMAIL, email));
		return Users.newUser(realm.getId(), userId, properties);
	}
}
