package com.sancheeese.cleanner.core.rules

import com.sancheeese.cleanner.core.model.AppOwner
import com.sancheeese.cleanner.core.model.PrivacyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderRuleRegistryTest {
    private val registry = FolderRuleRegistry.default()

    @Test
    fun matchesWeChatChatImageFolderWithPurposeExplanation() {
        val profile = registry.match(
            app = AppOwner.WeChat,
            path = "/storage/emulated/0/Android/media/com.tencent.mm/MicroMsg/image2/ab/cd"
        )

        assertEquals("微信聊天图片目录", profile.displayName)
        assertTrue(profile.purpose.contains("聊天"))
        assertTrue(profile.purpose.contains("图片"))
        assertEquals(PrivacyLevel.Sensitive, profile.privacyLevel)
    }

    @Test
    fun unknownFolderKeepsPathContextButDoesNotPretendToKnowPurpose() {
        val profile = registry.match(
            app = AppOwner.QQ,
            path = "/storage/emulated/0/tencent/QQfile_recv/custom"
        )

        assertEquals("QQ 接收文件目录", profile.displayName)
        assertTrue(profile.cleaningHint.contains("确认"))
    }
}
