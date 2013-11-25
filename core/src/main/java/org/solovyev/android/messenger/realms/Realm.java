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

import android.content.Context;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.users.BaseEditUserFragment;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.security.Cipherer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface Realm<C extends AccountConfiguration> extends Identifiable {

	@Nonnull
	String FAKE_REALM_ID = "fake";

	/**
	 * Method returns realm definition's identifier. Must be unique for all existed realm difinitions.
	 * Realm definition id must contain only alpha-numeric symbols in lower case: [a-z][0-9]
	 *
	 * @return realm definition id in application
	 */
	@Nonnull
	String getId();

	/**
	 * @return android string resource id for realm's name
	 */
	int getNameResId();

	/**
	 * @return android drawable resource id for realm's icon
	 */
	int getIconResId();

	/**
	 * Method does initial setup for realm definition.
	 * NOTE: this method must be called on application start, e.g. in {@link android.app.Application#onCreate()} method
	 *
	 * @param context application's context
	 */
	void init(@Nonnull Context context);

	boolean isEnabled();

	@Nonnull
	Class<? extends BaseAccountConfigurationFragment> getConfigurationFragmentClass();

	@Nonnull
	Account<C> newAccount(@Nonnull String accountId, @Nonnull User user, @Nonnull C configuration, @Nonnull AccountState state, @Nonnull AccountSyncData syncData);

	@Nonnull
	Class<? extends C> getConfigurationClass();

	@Nonnull
	AccountBuilder newAccountBuilder(@Nonnull C configuration, @Nullable Account editedAccount);

	/**
	 * Returns list of translated user properties where property name = title, property value = value
	 *
	 * @param user user which properties will be returned
	 * @return list of translated user properties
	 */
	@Nonnull
	List<AProperty> getUserDisplayProperties(@Nonnull User user, @Nonnull Context context);

	/**
	 * @return true if account would notify us about delivery status of a message
	 */
	boolean shouldWaitForDeliveryReport();

	@Nonnull
	RealmIconService getRealmIconService();

	/**
	 * @return cipherer to be used while saving {@link AccountConfiguration} in persistence storage
	 */
	@Nullable
	Cipherer<C, C> getCipherer();

	boolean handleException(@Nonnull Throwable e, @Nonnull Account account);

	boolean canCreateUsers();

	boolean canEditUsers();

	@Nullable
	Class<? extends BaseEditUserFragment> getCreateUserFragmentClass();

	/*
	**********************************************************************
    *
    *                           EQUALS/HASHCODE
    *
    **********************************************************************
    */

	boolean equals(@Nullable Object o);

	int hashCode();
}
