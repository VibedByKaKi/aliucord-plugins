version = "1.0.0"
description = "Copy a contiguous range of chat messages via Copy from here / Copy through here."

aliucord {
    changelog.set(
        """
        # 1.0.0
        * Initial release: range copy from the message long-press sheet
        """.trimIndent(),
    )

    // Publish zip to the builds branch / updater when CI deploys
    deploy.set(true)
}
