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

package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.view.IconGenerator;
import org.solovyev.android.security.base64.ABase64StringDecoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class XmppRealmIconService implements RealmIconService {

	@Nonnull
	private final Context context;

	@Nonnull
	private final IconGenerator iconGenerator;

	public XmppRealmIconService(@Nonnull Context context) {
		this.context = context;
		this.iconGenerator = App.getIconGenerator();
	}

	@Override
	public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
		final BitmapDrawable drawable = getUserIcon(user);
		if (drawable != null) {
			imageView.setImageDrawable(drawable);
		} else {
			imageView.setImageDrawable(iconGenerator.getIcon(user));
		}
	}

	@Override
	public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
		setUserIcon(user, imageView);
	}

	@Override
	public void fetchUsersIcons(@Nonnull List<User> users) {
		// everything is already fetched
	}

	@Override
	public void setUsersIcon(@Nonnull List<User> users, @Nonnull ImageView imageView) {
		imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_users_red));
	}

	@Nullable
	private BitmapDrawable getUserIcon(@Nonnull User user) {
		BitmapDrawable result = null;

		final String userIconBase64 = user.getPropertyValueByName(XmppRealm.USER_PROPERTY_AVATAR_BASE64);
		if (userIconBase64 != null) {
			try {
				final byte[] userIconBytes = ABase64StringDecoder.getInstance().convert(userIconBase64);
				result = new BitmapDrawable(BitmapFactory.decodeByteArray(userIconBytes, 0, userIconBytes.length));
			} catch (IllegalArgumentException e) {
				Log.e("XmppRealm", e.getMessage(), e);
			}
		}

		return result;
	}
}
