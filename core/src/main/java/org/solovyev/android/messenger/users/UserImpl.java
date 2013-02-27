package org.solovyev.android.messenger.users;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmEntityImpl;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;
import org.solovyev.common.JObject;
import org.solovyev.common.text.Strings;

import javax.annotation.Nullable;
import java.util.*;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:30 PM
 */
public class UserImpl extends JObject implements User {

    @NotNull
    private String login;

    @NotNull
    private RealmEntity realmEntity;

    @NotNull
    private UserSyncData userSyncData;

    @NotNull
    private List<AProperty> properties = new ArrayList<AProperty>();

    @NotNull
    private Map<String, String> propertiesMap = new HashMap<String, String>();

    private UserImpl() {
    }

    @NotNull
    public static User newInstance(@NotNull String reamId,
                                   @NotNull String realmUserId,
                                   @NotNull UserSyncData userSyncData,
                                   @NotNull List<AProperty> properties) {
        final RealmEntity realmEntity = RealmEntityImpl.newInstance(reamId, realmUserId);
        return newInstance(realmEntity, userSyncData, properties);
    }

    @NotNull
    public static User newInstance(@NotNull RealmEntity realmEntity,
                                   @NotNull UserSyncData userSyncData,
                                   @NotNull List<AProperty> properties) {
        final UserImpl result = new UserImpl();

        result.realmEntity = realmEntity;
        result.login = realmEntity.getRealmEntityId();
        result.userSyncData = userSyncData;
        result.properties.addAll(properties);

        for (AProperty property : result.properties) {
            result.propertiesMap.put(property.getName(), property.getValue());
        }

        return result;
    }

    @NotNull
    public static User newFakeInstance(@NotNull RealmEntity realmUser) {
        return newInstance(realmUser, UserSyncDataImpl.newNeverSyncedInstance(), Collections.<AProperty>emptyList());
    }

    @NotNull
    public static User newFakeInstance(@NotNull String userId) {
        return newFakeInstance(RealmEntityImpl.fromEntityId(userId));
    }

    @NotNull
    public String getLogin() {
        return login;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        final StringBuilder result = new StringBuilder();

        final String firstName = getPropertyValueByName("firstName");
        final String lastName = getPropertyValueByName("lastName");

        result.append(firstName);
        if (!Strings.isEmpty(firstName) && !Strings.isEmpty(lastName)) {
            result.append(" ");
        }
        result.append(lastName);

        return result.toString();
    }

    @Override
    public Gender getGender() {
        final String result = getPropertyValueByName("sex");
        return result == null ? null : Gender.valueOf(result);
    }

    @Override
    public boolean isOnline() {
        return Boolean.valueOf(getPropertyValueByName(PROPERTY_ONLINE));
    }

    @Override
    @NotNull
    public UserSyncData getUserSyncData() {
        return userSyncData;
    }

    @NotNull
    @Override
    public User updateChatsSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updateChatsSyncDate();
        return clone;
    }

    @NotNull
    @Override
    public User updatePropertiesSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updatePropertiesSyncDate();
        return clone;
    }

    @NotNull
    @Override
    public User updateContactsSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updateContactsSyncDate();
        return clone;
    }

    @NotNull
    @Override
    public User updateUserIconsSyncDate() {
        final UserImpl clone = this.clone();
        clone.userSyncData = clone.userSyncData.updateUserIconsSyncDate();
        return clone;
    }

    @Override
    @NotNull
    public List<AProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @NotNull
    @Override
    public RealmEntity getRealmUser() {
        return this.realmEntity;
    }

    @Override
    public String getPropertyValueByName(@NotNull String name) {
        return this.propertiesMap.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserImpl)) return false;

        final UserImpl that = (UserImpl) o;

        if (!realmEntity.equals(that.realmEntity)) return false;

        return true;
    }


    @Override
    public int hashCode() {
        return realmEntity.hashCode();
    }

    @Override
    public String toString() {
        return "UserImpl{" +
                "id=" + realmEntity.getEntityId() +
                '}';
    }

    @NotNull
    @Override
    public UserImpl clone() {
        final UserImpl clone = (UserImpl) super.clone();

        clone.realmEntity = realmEntity.clone();

        return clone;
    }

    @NotNull
    @Override
    public User cloneWithNewStatus(boolean online) {
        final UserImpl clone = clone();

        Iterables.removeIf(clone.properties, new Predicate<AProperty>() {
            @Override
            public boolean apply(@Nullable AProperty property) {
                return property != null && property.getName().equals(PROPERTY_ONLINE);
            }
        });
        clone.properties.add(APropertyImpl.newInstance(PROPERTY_ONLINE, Boolean.toString(online)));
        clone.propertiesMap.put(PROPERTY_ONLINE, Boolean.toString(online));

        return clone;
    }
}
