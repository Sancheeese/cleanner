package com.sancheeese.cleanner.app

import android.app.Application
import org.junit.Assert.assertNotNull
import org.junit.Test

class CleannerViewModelConstructorTest {
    @Test
    fun exposesApplicationOnlyConstructorForAndroidViewModelFactory() {
        val constructor = CleannerViewModel::class.java.getConstructor(Application::class.java)

        assertNotNull(constructor)
    }
}
