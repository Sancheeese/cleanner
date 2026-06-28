package com.sancheeese.cleanner.core.rules

import com.sancheeese.cleanner.core.model.AppOwner
import com.sancheeese.cleanner.core.model.Confidence
import com.sancheeese.cleanner.core.model.FolderProfile
import com.sancheeese.cleanner.core.model.PrivacyLevel

class FolderRuleRegistry(private val rules: List<FolderRule>) {
    fun match(app: AppOwner, path: String): FolderProfile {
        val normalizedPath = path.replace('\\', '/').lowercase()
        return rules.firstOrNull { rule ->
            rule.app == app && rule.pathKeywords.all { normalizedPath.contains(it) }
        }?.profile ?: unknownProfile(app, path)
    }

    private fun unknownProfile(app: AppOwner, path: String): FolderProfile {
        return FolderProfile(
            app = app,
            pathPattern = path,
            displayName = "${app.displayName} 未识别目录",
            purpose = "当前版本还不能确定这个目录的具体用途，只能根据文件类型和大小辅助判断。",
            privacyLevel = PrivacyLevel.Unknown,
            cleaningHint = "建议逐个查看后再清理，不要批量默认删除。",
            confidence = Confidence.Low
        )
    }

    companion object {
        fun default(): FolderRuleRegistry {
            return FolderRuleRegistry(
                listOf(
                    FolderRule(
                        app = AppOwner.WeChat,
                        pathKeywords = listOf("micromsg", "image2"),
                        profile = FolderProfile(
                            app = AppOwner.WeChat,
                            pathPattern = "*/MicroMsg/*/image2/*",
                            displayName = "微信聊天图片目录",
                            purpose = "通常存储微信聊天中收发、缓存或生成的图片内容，账号目录一般是随机哈希名称。",
                            privacyLevel = PrivacyLevel.Sensitive,
                            cleaningHint = "图片可能包含聊天内容，建议查看后手动选择清理。",
                            confidence = Confidence.High
                        )
                    ),
                    FolderRule(
                        app = AppOwner.WeChat,
                        pathKeywords = listOf("micromsg", "video"),
                        profile = FolderProfile(
                            app = AppOwner.WeChat,
                            pathPattern = "*/MicroMsg/*video*/*",
                            displayName = "微信聊天视频目录",
                            purpose = "通常存储微信聊天中收发、拍摄或缓存的视频文件。",
                            privacyLevel = PrivacyLevel.Sensitive,
                            cleaningHint = "视频通常较大，建议预览或确认来源后清理。",
                            confidence = Confidence.Medium
                        )
                    ),
                    FolderRule(
                        app = AppOwner.WeChat,
                        pathKeywords = listOf("micromsg", "voice"),
                        profile = FolderProfile(
                            app = AppOwner.WeChat,
                            pathPattern = "*/MicroMsg/*voice*/*",
                            displayName = "微信语音目录",
                            purpose = "通常存储微信聊天语音、语音消息缓存或相关索引文件。",
                            privacyLevel = PrivacyLevel.Sensitive,
                            cleaningHint = "语音可能包含聊天隐私，建议确认后再清理。",
                            confidence = Confidence.Medium
                        )
                    ),
                    FolderRule(
                        app = AppOwner.WeChat,
                        pathKeywords = listOf("micromsg", "attachment"),
                        profile = FolderProfile(
                            app = AppOwner.WeChat,
                            pathPattern = "*/MicroMsg/*attachment*/*",
                            displayName = "微信附件目录",
                            purpose = "通常存储微信聊天中接收或打开过的文档、压缩包等附件。",
                            privacyLevel = PrivacyLevel.Sensitive,
                            cleaningHint = "这里可能有你主动保存或仍要使用的文件，建议确认内容后清理。",
                            confidence = Confidence.Medium
                        )
                    ),
                    FolderRule(
                        app = AppOwner.WeChat,
                        pathKeywords = listOf("micromsg", "download"),
                        profile = FolderProfile(
                            app = AppOwner.WeChat,
                            pathPattern = "*/MicroMsg/*download*/*",
                            displayName = "微信下载目录",
                            purpose = "通常存储微信内下载、打开或转存的文件。",
                            privacyLevel = PrivacyLevel.Sensitive,
                            cleaningHint = "下载文件可能仍有用，建议确认后清理。",
                            confidence = Confidence.Medium
                        )
                    ),
                    FolderRule(
                        app = AppOwner.WeChat,
                        pathKeywords = listOf("micromsg", "cache"),
                        profile = FolderProfile(
                            app = AppOwner.WeChat,
                            pathPattern = "*/MicroMsg/*cache*/*",
                            displayName = "微信缓存目录",
                            purpose = "通常存储微信运行时生成的临时缓存、缩略图或中间文件。",
                            privacyLevel = PrivacyLevel.Normal,
                            cleaningHint = "缓存类文件一般可清理，但清理后微信可能重新生成。",
                            confidence = Confidence.Medium
                        )
                    ),
                    FolderRule(
                        app = AppOwner.WeChat,
                        pathKeywords = listOf("pictures", "weixin"),
                        profile = FolderProfile(
                            app = AppOwner.WeChat,
                            pathPattern = "*/Pictures/WeiXin/*",
                            displayName = "微信保存到相册",
                            purpose = "通常存储从微信保存到系统相册的图片或视频。",
                            privacyLevel = PrivacyLevel.Sensitive,
                            cleaningHint = "这些通常是用户主动保存的内容，默认建议先查看再决定。",
                            confidence = Confidence.High
                        )
                    ),
                    FolderRule(
                        app = AppOwner.QQ,
                        pathKeywords = listOf("qqfile_recv"),
                        profile = FolderProfile(
                            app = AppOwner.QQ,
                            pathPattern = "*/QQfile_recv/*",
                            displayName = "QQ 接收文件目录",
                            purpose = "通常存储 QQ 聊天、群聊或传输助手接收到的文件。",
                            privacyLevel = PrivacyLevel.Sensitive,
                            cleaningHint = "这里可能有用户主动保存的文件，建议确认内容后清理。",
                            confidence = Confidence.High
                        )
                    ),
                    FolderRule(
                        app = AppOwner.QQ,
                        pathKeywords = listOf("mobileqq", "photo"),
                        profile = FolderProfile(
                            app = AppOwner.QQ,
                            pathPattern = "*/MobileQQ/photo/*",
                            displayName = "QQ 图片目录",
                            purpose = "通常存储 QQ 聊天图片、表情图片或图片缓存。",
                            privacyLevel = PrivacyLevel.Sensitive,
                            cleaningHint = "图片可能包含聊天内容，建议查看后手动选择清理。",
                            confidence = Confidence.Medium
                        )
                    ),
                    FolderRule(
                        app = AppOwner.QQ,
                        pathKeywords = listOf("qq_images"),
                        profile = FolderProfile(
                            app = AppOwner.QQ,
                            pathPattern = "*/tencent/qq_images/*",
                            displayName = "QQ 图片缓存目录",
                            purpose = "通常存储 QQ 产生或保存的图片缓存。",
                            privacyLevel = PrivacyLevel.Sensitive,
                            cleaningHint = "建议查看图片内容后再清理。",
                            confidence = Confidence.Medium
                        )
                    ),
                    FolderRule(
                        app = AppOwner.QQ,
                        pathKeywords = listOf("tencent", "audio"),
                        profile = FolderProfile(
                            app = AppOwner.QQ,
                            pathPattern = "*/tencent/audio/*",
                            displayName = "QQ 语音目录",
                            purpose = "通常存储 QQ 语音消息、音频缓存或相关临时文件。",
                            privacyLevel = PrivacyLevel.Sensitive,
                            cleaningHint = "语音可能包含聊天隐私，建议确认后再清理。",
                            confidence = Confidence.Medium
                        )
                    ),
                    FolderRule(
                        app = AppOwner.QQ,
                        pathKeywords = listOf("mobileqq", "thumb"),
                        profile = FolderProfile(
                            app = AppOwner.QQ,
                            pathPattern = "*/MobileQQ/*thumb*/*",
                            displayName = "QQ 缩略图目录",
                            purpose = "通常存储 QQ 图片或视频预览用的缩略图。",
                            privacyLevel = PrivacyLevel.Normal,
                            cleaningHint = "缩略图通常可以清理，应用需要时会重新生成。",
                            confidence = Confidence.High
                        )
                    ),
                    FolderRule(
                        app = AppOwner.QQ,
                        pathKeywords = listOf("msflogs"),
                        profile = FolderProfile(
                            app = AppOwner.QQ,
                            pathPattern = "*/tencent/msflogs/*",
                            displayName = "QQ 运行日志目录",
                            purpose = "通常存储 QQ 或腾讯基础组件生成的运行日志、诊断日志。",
                            privacyLevel = PrivacyLevel.Normal,
                            cleaningHint = "旧日志通常可以清理。",
                            confidence = Confidence.High
                        )
                    ),
                    FolderRule(
                        app = AppOwner.QQ,
                        pathKeywords = listOf("mobileqq", "log"),
                        profile = FolderProfile(
                            app = AppOwner.QQ,
                            pathPattern = "*/MobileQQ/*log*/*",
                            displayName = "QQ 日志目录",
                            purpose = "通常存储 QQ 运行日志或诊断文件。",
                            privacyLevel = PrivacyLevel.Normal,
                            cleaningHint = "旧日志通常可以清理。",
                            confidence = Confidence.Medium
                        )
                    ),
                    FolderRule(
                        app = AppOwner.QQ,
                        pathKeywords = listOf("mobileqq", "data"),
                        profile = FolderProfile(
                            app = AppOwner.QQ,
                            pathPattern = "*/MobileQQ/data/*",
                            displayName = "QQ 数据目录",
                            purpose = "通常存储 QQ 运行数据、缓存索引或功能模块数据，具体用途需要结合文件类型判断。",
                            privacyLevel = PrivacyLevel.Unknown,
                            cleaningHint = "不要默认批量清理，建议只选择明确可清理的缓存、缩略图、日志或安装包。",
                            confidence = Confidence.Medium
                        )
                    ),
                    FolderRule(
                        app = AppOwner.QQ,
                        pathKeywords = listOf("pictures", "qq"),
                        profile = FolderProfile(
                            app = AppOwner.QQ,
                            pathPattern = "*/Pictures/QQ/*",
                            displayName = "QQ 保存到相册",
                            purpose = "通常存储从 QQ 保存到系统相册的图片或视频。",
                            privacyLevel = PrivacyLevel.Sensitive,
                            cleaningHint = "这些通常是用户主动保存的内容，默认建议先查看再决定。",
                            confidence = Confidence.High
                        )
                    )
                )
            )
        }
    }
}

data class FolderRule(
    val app: AppOwner,
    val pathKeywords: List<String>,
    val profile: FolderProfile
)
