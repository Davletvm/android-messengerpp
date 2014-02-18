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

package org.solovyev.android.messenger.realms.whatsapp;

import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

public final class WhatsAppAccountConfiguration extends JObject implements AccountConfiguration {

	@Nonnull
	@Override
	public WhatsAppAccountConfiguration clone() {
		return (WhatsAppAccountConfiguration) super.clone();
	}

	@Override
	public boolean isSameAccount(AccountConfiguration c) {
		return c instanceof WhatsAppAccountConfiguration;
	}

	@Override
	public boolean isSameCredentials(AccountConfiguration c) {
		return isSameAccount(c);
	}

	@Override
	public boolean isSame(AccountConfiguration c) {
		return isSameCredentials(c);
	}

	@Override
	public void applySystemData(AccountConfiguration oldConfiguration) {
	}
}
