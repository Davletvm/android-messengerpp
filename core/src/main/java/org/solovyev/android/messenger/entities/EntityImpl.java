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

package org.solovyev.android.messenger.entities;

import android.os.Parcel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.realms.Realms;
import org.solovyev.common.JCloneable;
import org.solovyev.common.JObject;

import static org.solovyev.android.messenger.accounts.AccountService.NO_ACCOUNT_ID;

public class EntityImpl extends JObject implements JCloneable<EntityImpl>, MutableEntity {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	public static final Creator<Entity> CREATOR = new Creator<Entity>() {
		@Override
		public Entity createFromParcel(@Nonnull Parcel in) {
			return fromParcel(in);
		}

		@Override
		public Entity[] newArray(int size) {
			return new Entity[size];
		}
	};

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	@Nonnull
	private String accountId;

	@Nullable
	private String realmId;

	@Nonnull
	private String accountEntityId;

	@Nullable
	private String appAccountEntityId;

	@Nonnull
	private String entityId;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	EntityImpl(@Nonnull String accountId, @Nonnull String accountEntityId, @Nonnull String entityId) {
		this.accountId = accountId;
		this.accountEntityId = accountEntityId;
		this.entityId = entityId;
	}

	private EntityImpl(@Nonnull String accountId,
					   @Nullable String realmId,
					   @Nonnull String accountEntityId,
					   @Nonnull String entityId,
					   @Nullable String appAccountEntityId) {
		this.accountId = accountId;
		this.realmId = realmId;
		this.accountEntityId = accountEntityId;
		this.entityId = entityId;
		this.appAccountEntityId = appAccountEntityId;
	}

	@Nonnull
	private static Entity fromParcel(@Nonnull Parcel in) {
		return new EntityImpl(in.readString(), in.readString(), in.readString(), in.readString(), in.readString());
	}

	@Nonnull
	public String getEntityId() {
		return entityId;
	}

	@Nonnull
	public String getAccountId() {
		return this.accountId;
	}

	@Nonnull
	@Override
	public String getRealmId() {
		if (this.realmId == null) {
			final int index = accountId.indexOf(Realms.DELIMITER_REALM);
			if (index >= 0) {
				this.realmId = entityId.substring(0, index);
			} else {
				throw new IllegalArgumentException("No realm id is stored in accountId!");
			}

		}
		return this.realmId;
	}

	@Nonnull
	public String getAccountEntityId() {
		return this.accountEntityId;
	}

	@Override
	public boolean isAccountEntityIdSet() {
		return !NO_ACCOUNT_ID.equals(accountEntityId);
	}

	@Nonnull
	@Override
	public String getAppAccountEntityId() {
		if (appAccountEntityId == null) {
			final int index = entityId.indexOf(Entities.DELIMITER);
			if (index >= 0) {
				appAccountEntityId = entityId.substring(index + 1);
			} else {
				throw new IllegalArgumentException("No realm is stored in entityId!");
			}
		}

		return appAccountEntityId;
	}

	@Nonnull
	@Override
	public EntityImpl clone() {
		return (EntityImpl) super.clone();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EntityImpl)) return false;

		EntityImpl that = (EntityImpl) o;

		if (!entityId.equals(that.entityId)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return entityId.hashCode();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@Nonnull Parcel out, int flags) {
		out.writeString(accountId);
		out.writeString(realmId);
		out.writeString(accountEntityId);
		out.writeString(entityId);
		out.writeString(appAccountEntityId);
	}

	@Override
	public String toString() {
		return "Entity{id=" + entityId + "}";
	}
}
