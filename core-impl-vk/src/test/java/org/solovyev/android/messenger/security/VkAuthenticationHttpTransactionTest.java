package org.solovyev.android.messenger.security;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solovyev.android.http.AndroidHttpUtils;
import org.solovyev.android.Captcha;
import org.solovyev.android.messenger.vk.VkConfigurationImpl;
import org.solovyev.android.messenger.vk.VkMessengerApplication;
import org.solovyev.android.messenger.api.CommonApiError;
import org.solovyev.android.messenger.vk.http.VkErrorType;
import org.solovyev.android.messenger.vk.http.VkResponseErrorException;
import org.solovyev.android.messenger.vk.secutiry.VkAuthenticationHttpTransaction;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 10:34 PM
 */
public class VkAuthenticationHttpTransactionTest {

    @Before
    public void setUp() throws Exception {
        VkConfigurationImpl.getInstance().setClientId(VkMessengerApplication.CLIENT_ID);
        VkConfigurationImpl.getInstance().setClientSecret(VkMessengerApplication.CLIENT_SECRET);
    }

    @Test
    public void testErrorResult() throws Exception {
        try {
            AndroidHttpUtils.execute(new VkAuthenticationHttpTransaction("test", "test"));
            Assert.fail();
        } catch (VkResponseErrorException e) {
            final CommonApiError vkError = e.getApiError();
            // just to be sure that we've got known error
            final VkErrorType vkErrorType = VkErrorType.valueOf(vkError.getErrorId());

            switch (vkErrorType) {
                case invalid_client:
                    break;
                case need_captcha:
                    final Captcha captcha = vkError.getCaptcha();
                    Assert.assertNotNull(captcha);
                    Assert.assertNotNull(captcha.getCaptchaSid());
                    Assert.assertNotNull(captcha.getCaptchaImage());
                    break;
            }
        }
    }
}