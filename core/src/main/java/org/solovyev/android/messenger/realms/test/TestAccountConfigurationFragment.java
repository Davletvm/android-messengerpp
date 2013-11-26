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

package org.solovyev.android.messenger.realms.test;

import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TestAccountConfigurationFragment extends BaseAccountConfigurationFragment<TestAccount> {

	@Inject
	@Nonnull
	private TestRealm realm;

	public TestAccountConfigurationFragment() {
		super(0);
	}

	@Nullable
	@Override
	public AccountConfiguration validateData() {
		return new TestAccountConfiguration("test", 42);
	}

	@Nonnull
	@Override
	public Realm getRealm() {
		return realm;
	}
}
