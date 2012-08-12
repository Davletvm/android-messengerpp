package org.solovyev.android.messenger.users;

import android.content.Context;
import android.widget.ImageView;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:12 PM
 */
public interface UserService extends UserEventContainer {

    // initial initialization: will be called once on application start
    void init();

    // NOTE: finding user by id always return user object, if real user cannot be found via API (e.g. user was removed) service must return dummy user object
    @NotNull
    User getUserById(@NotNull String userId, @NotNull Context context);

    @NotNull
    List<User> getUserContacts(@NotNull String userId, @NotNull Context context);

    @NotNull
    List<Chat> getUserChats(@NotNull String userId, @NotNull Context context);

    @NotNull
    Chat getPrivateChat(@NotNull String userId, @NotNull String secondUserId, @NotNull Context context);

    @NotNull
    List<User> getOnlineUserContacts(@NotNull String userId, @NotNull Context context);

    void updateUser(@NotNull User user, @NotNull Context context);

    void setUserIcon(@NotNull ImageView imageView, @NotNull User user, @NotNull Context context);

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

    void syncUserProperties(@NotNull String userId, @NotNull Context context);

    @NotNull
    List<User> syncUserContacts(@NotNull String userId, @NotNull Context context);

    @NotNull
    List<Chat> syncUserChats(@NotNull String userId, @NotNull Context context);

    void mergeUserChats(@NotNull String userId, @NotNull List<? extends ApiChat> apiChats, @NotNull Context context);

    void checkOnlineUseContacts(@NotNull String userId, @NotNull Context context);

    void fetchUserIcons(@NotNull User user, @NotNull Context context);
}