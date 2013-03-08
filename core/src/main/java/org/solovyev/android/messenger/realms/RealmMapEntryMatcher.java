package org.solovyev.android.messenger.realms;

import com.google.common.base.Predicate;
import javax.annotation.Nonnull;

import java.util.Map;

/**
* User: serso
* Date: 2/28/13
* Time: 9:00 PM
*/
public class RealmMapEntryMatcher implements Predicate<Map.Entry<RealmEntity, ?>> {

    @Nonnull
    private final String realmId;

    private RealmMapEntryMatcher(@Nonnull String realmId) {
        this.realmId = realmId;
    }

    @Nonnull
    public static RealmMapEntryMatcher forRealm(@Nonnull String realmId) {
        return new RealmMapEntryMatcher(realmId);
    }

    @Override
    public boolean apply(@javax.annotation.Nullable Map.Entry<RealmEntity, ?> entry) {
        return entry != null && entry.getKey().getRealmId().equals(realmId);
    }
}