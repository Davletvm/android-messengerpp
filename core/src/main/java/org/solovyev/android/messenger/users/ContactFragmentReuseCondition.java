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

import android.support.v4.app.Fragment;
import org.solovyev.android.fragments.AbstractFragmentReuseCondition;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;

public final class ContactFragmentReuseCondition extends AbstractFragmentReuseCondition<ContactFragment> {

	@Nonnull
	private final String contactId;

	public ContactFragmentReuseCondition(@Nonnull String contactId) {
		super(ContactFragment.class);
		this.contactId = contactId;
	}

	@Nonnull
	public static JPredicate<Fragment> forContact(@Nonnull Entity contact) {
		return new ContactFragmentReuseCondition(contact.getEntityId());
	}

	@Override
	protected boolean canReuseFragment(@Nonnull ContactFragment fragment) {
		final User user = fragment.getUser();
		return user != null && contactId.equals(user.getId());
	}
}
