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
            path = "/storage/emulated/0/tencent/MicroMsg/da2bd3309eaed93a52de219ae9e0e801/image2/ab/cd"
        )

        assertEquals("微信聊天图片目录", profile.displayName)
        assertTrue(profile.purpose.contains("聊天"))
        assertTrue(profile.purpose.contains("图片"))
        assertEquals(PrivacyLevel.Sensitive, profile.privacyLevel)
    }

    @Test
    fun matchesWeChatGalleryFolderAsUserSavedContent() {
        val profile = registry.match(
            app = AppOwner.WeChat,
            path = "/storage/emulated/0/Pictures/WeiXin"
        )

        assertEquals("微信保存到相册", profile.displayName)
        assertTrue(profile.cleaningHint.contains("主动保存"))
        assertEquals(PrivacyLevel.Sensitive, profile.privacyLevel)
    }

    @Test
    fun matchesQqReceivedFilesFolderWithConfirmationHint() {
        val profile = registry.match(
            app = AppOwner.QQ,
            path = "/storage/emulated/0/tencent/QQfile_recv/custom"
        )

        assertEquals("QQ 接收文件目录", profile.displayName)
        assertTrue(profile.cleaningHint.contains("确认"))
    }

    @Test
    fun matchesQqThumbFolderAsNormalCleanablePreviewData() {
        val profile = registry.match(
            app = AppOwner.QQ,
            path = "/storage/emulated/0/tencent/MobileQQ/thumb2/abc"
        )

        assertEquals("QQ 缩略图目录", profile.displayName)
        assertTrue(profile.cleaningHint.contains("可以清理"))
        assertEquals(PrivacyLevel.Normal, profile.privacyLevel)
    }

    @Test
    fun matchesQqMsfLogsFolderAsRuntimeLogs() {
        val profile = registry.match(
            app = AppOwner.QQ,
            path = "/storage/emulated/0/tencent/msflogs/com/tencent/mobileqq"
        )

        assertEquals("QQ 运行日志目录", profile.displayName)
        assertTrue(profile.purpose.contains("运行日志"))
        assertEquals(PrivacyLevel.Normal, profile.privacyLevel)
    }
}
