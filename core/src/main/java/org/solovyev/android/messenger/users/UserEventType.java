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

package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public enum UserEventType {
	added,
	changed,

	contacts_added {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof List;
		}
	},
	// data == id of removed contact for current user
	contact_removed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof String;
		}
	},

	chat_added,
	chats_added {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof List;
		}
	},
	// data == id of removed chat for current user
	chat_removed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof String;
		}
	},

	contacts_changed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof List;
		}
	},

	/**
	 * Fires when contacts presence has changed,
	 * Data: list of contacts for whom presence have been changed
	 */
	contacts_presence_changed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof List;
		}
	},

	// Number of unread messages in private chat has changed
	unread_messages_count_changed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Integer;
		}
	};

	@Nonnull
	public final UserEvent newEvent(@Nonnull User user) {
		return newEvent(user, null);
	}

	@Nonnull
	public final UserEvent newEvent(@Nonnull User user, @Nullable Object data) {
		checkData(data);
		return new UserEvent(user, this, data);
	}

	protected void checkData(@Nullable Object data) {
		assert data == null;
	}
}
