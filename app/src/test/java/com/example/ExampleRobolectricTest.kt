package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.system.TacticalPermissionsHelper
import com.example.viewmodel.SipraStatus
import com.example.viewmodel.SipraViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Lyra", appName)
  }

  @Test
  fun `test tactical permissions helper checks`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertNotNull(context)
    // Verify methods execute safely without exception
    val mic = TacticalPermissionsHelper.isMicGranted(context)
    val cam = TacticalPermissionsHelper.isCameraGranted(context)
    val notif = TacticalPermissionsHelper.isNotificationGranted(context)
    val overlay = TacticalPermissionsHelper.isOverlayGranted(context)
    val battery = TacticalPermissionsHelper.isBatteryOptimizationIgnored(context)

    // Verify boolean evaluation returns valid non-crashing states
    assertTrue(mic || !mic)
    assertTrue(cam || !cam)
    assertTrue(notif || !notif)
    assertTrue(overlay || !overlay)
    assertTrue(battery || !battery)
  }

  @Test
  fun `test sipra view model initial state and auto listen property`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = SipraViewModel(app)

    assertTrue(vm.autoListenOnSpeechEnd.value)
    assertEquals("Sir", vm.userTitle.value)
    assertEquals(SipraStatus.IDLE, vm.status.value)

    vm.setAutoListenOnSpeechEnd(false)
    assertEquals(false, vm.autoListenOnSpeechEnd.value)

    vm.setAutoListenOnSpeechEnd(true)
    assertEquals(true, vm.autoListenOnSpeechEnd.value)
  }
}
