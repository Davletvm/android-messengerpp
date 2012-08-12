package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 12:56 AM
 */
public interface Realm {

    // realm's identifier. Must be unique for all existed realms
    @NotNull
    String getId();

    @NotNull
    RealmConnection createRealmConnection(@NotNull Context context);

    /*
    **********************************************************************
    *
    *                           Realm Services
    *
    **********************************************************************
    */
    @NotNull
    RealmUserService getRealmUserService();

    @NotNull
    RealmChatService getRealmChatService();

    @NotNull
    RealmAuthService getRealmAuthService();
}