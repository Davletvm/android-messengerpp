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

package org.solovyev.android.messenger.realms.vk.users;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountSyncData;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.users.*;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

import static org.solovyev.android.properties.Properties.newProperty;

class JsonUser {

	@Nullable
	private String uid;

	@Nullable
	private String first_name;

	@Nullable
	private String last_name;

	@Nullable
	private String nickname;

	@Nullable
	private Integer sex;

	@Nullable
	private Integer online;

	@Nullable
	private String bdate;

	@Nullable
	private Integer city;

	@Nullable
	private Integer country;

	@Nullable
	private String timezone;

	@Nullable
	private String photo;

	@Nullable
	private String photo_medium;

	@Nullable
	private String photo_big;

	@Nullable
	private String photo_rec;

	@Nonnull
	public User toUser(@Nonnull Account account) throws IllegalJsonException {
		if (uid == null) {
			throw new IllegalJsonException();
		}

		final MutableUser user = Users.newUser(account.newUserEntity(uid), Collections.<AProperty>emptyList());

		if (first_name != null) {
			user.setFirstName(first_name);
		}

		if (last_name != null) {
			user.setLastName(last_name);
		}

		final MutableAProperties properties = user.getProperties();
		if (nickname != null) {
			properties.setProperty(User.PROPERTY_NICKNAME, nickname);
		}

		final String gender = getGender();
		if (gender != null) {
			properties.setProperty(newProperty(User.PROPERTY_SEX, gender));
		}

		final Boolean online = getOnline();
		if (online != null) {
			user.setOnline(online);
		}
		if (bdate != null) {
			properties.setProperty("bdate", bdate);
		}

		properties.setProperty("cityId", String.valueOf(city));
		properties.setProperty("countryId", String.valueOf(country));
		if (photo != null) {
			properties.setProperty("photo", photo);
		}

		if (photo_medium != null) {
			properties.setProperty("photoMedium", photo_medium);
		}

		if (photo_big != null) {
			properties.setProperty("photoBig", photo_big);
		}

		if (photo_rec != null) {
			properties.setProperty("photoRec", photo_rec);
		}

		return user;
	}

	@Nullable
	private Boolean getOnline() {
		if (online == null) {
			return null;
		} else if (online.equals(0)) {
			return Boolean.FALSE;
		} else if (online.equals(1)) {
			return Boolean.TRUE;
		} else {
			return null;
		}
	}

	@Nullable
	private String getGender() {
		if (sex == null) {
			return null;
		} else if (sex.equals(1)) {
			return Gender.female.name();
		} else if (sex.equals(2)) {
			return Gender.male.name();
		} else {
			return null;
		}
	}

}
