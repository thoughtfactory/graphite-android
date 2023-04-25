package com.syncodec.graphite.service

import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import com.syncodec.graphite.service.syncService.DropboxService
import com.syncodec.graphite.service.syncService.SyncerService
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible


@RunWith(AndroidJUnit4::class)
class DropboxServiceTest {
	@Test
	fun useAppContext() {
		val appContext = InstrumentationRegistry.getInstrumentation().targetContext
		Assert.assertEquals("com.syncodec.graphite", appContext.packageName)
	}

	@JvmField
	@Rule
	val serviceTestRule : ServiceTestRule = ServiceTestRule()
	private var service : DropboxService? = null

	@Test
	@Throws(TimeoutException::class)
	fun testWithBoundService() {
		// Create the service Intent.
		val serviceIntent = Intent(getApplicationContext(), DropboxService::class.java)

		val binder : IBinder = serviceTestRule.bindService(serviceIntent)
		service = (binder as DropboxService.DropboxServiceBinder).service

		assert(service != null)
		assert(service!!.syncStatus.value == SyncerService.Companion.SyncStatus.Init)
	}

	@Test
	fun test_dropboxConnection() {
		assert(service != null)
		service!!.let { service ->
			assert(service.syncStatus.value == SyncerService.Companion.SyncStatus.Init)

			val syncCoroutine = service.getPrivateProperty("syncCoroutine") as CoroutineScope?
			val reSyncCoroutine = service.getPrivateProperty("reSyncCoroutine") as CoroutineScope?

			service.onClickSyncNow()
		}
	}

	private inline fun <reified T> T.invokePrivateFunction(name: String, vararg args: Any?): Any? =
		T::class
			.declaredMemberFunctions
			.firstOrNull { it.name == name }
			?.apply { isAccessible = true }
			?.call(this, *args)

	private inline fun <reified T> T.getPrivateProperty(name: String): Any? =
		T::class
			.declaredMembers
			.firstOrNull { it.name == name }
			?.apply { isAccessible = true }
			?.call(this)
}
