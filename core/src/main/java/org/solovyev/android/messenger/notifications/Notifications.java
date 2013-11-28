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

package org.solovyev.android.messenger.notifications;

import android.content.Intent;
import org.acra.ACRA;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.core.R;
import org.solovyev.common.msg.MessageLevel;
import org.solovyev.common.msg.MessageType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.provider.Settings.ACTION_WIRELESS_SETTINGS;
import static org.solovyev.android.Activities.startActivity;

public final class Notifications {


	private Notifications() {
		throw new AssertionError();
	}

	public static final Notification NO_INTERNET_NOTIFICATION = Notification.newInstance(R.string.mpp_notification_network_problem, MessageType.warning).solvedBy(new NoInternetConnectionSolution());
	public static final Notification ACCOUNT_NOT_SUPPORTED_NOTIFICATION = Notification.newInstance(R.string.mpp_notification_account_unsupported_exception, MessageType.error);

	@Nonnull
	public static Notification newInvalidResponseNotification() {
		return Notification.newInstance(R.string.mpp_notification_invalid_response, MessageType.error);
	}

	@Nonnull
	public static Notification newUndefinedErrorNotification() {
		return Notification.newInstance(R.string.mpp_notification_undefined_error, MessageType.error);
	}

	@Nonnull
	public static Notification newAccountErrorNotification() {
		return Notification.newInstance(R.string.mpp_notification_account_exception, MessageType.error);
	}

	@Nonnull
	public static Notification newAccountConnectionErrorNotification() {
		return Notification.newInstance(R.string.mpp_notification_account_connection_exception, MessageType.error);
	}

	@Nonnull
	public static Notification newNotification(int messageResId, @Nonnull MessageLevel messageLevel, @Nullable Object... params) {
		return Notification.newInstance(messageResId, messageLevel, params);
	}

	@Nonnull
	public static NotificationSolution newOpenAccountConfSolution(@Nonnull Account account) {
		return new OpenAccountSolution(account);
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER CLASSES
	*
	**********************************************************************
	*/

	static final class NotifyDeveloperSolution implements NotificationSolution {

		@Nonnull
		public static final NotifyDeveloperSolution instance = new NotifyDeveloperSolution();

		private NotifyDeveloperSolution() {
		}

		@Nonnull
		static NotifyDeveloperSolution getInstance() {
			return instance;
		}

		@Override
		public void solve(@Nonnull Notification notification) {
			final Throwable cause = notification.getCause();
			if (cause != null) {
				ACRA.getErrorReporter().handleException(cause);
			}
			notification.dismiss();
		}
	}

	private static final class NoInternetConnectionSolution implements NotificationSolution {

		@Override
		public void solve(@Nonnull Notification notification) {
			startActivity(new Intent(ACTION_WIRELESS_SETTINGS), App.getApplication());
		}
	}

	private static class OpenAccountSolution implements NotificationSolution {
		@Nonnull
		private final Account account;

		public OpenAccountSolution(@Nonnull Account account) {
			this.account = account;
		}

		@Override
		public void solve(@Nonnull Notification notification) {
			// todo serso: open configuration for specified realm
			notification.dismiss();
		}
	}
}
