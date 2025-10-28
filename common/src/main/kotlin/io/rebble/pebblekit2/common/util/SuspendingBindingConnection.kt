package io.rebble.pebblekit2.common.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * Wrapper around [ServiceConnection] that allows easy access of the async connection via a suspending [getOrConnect].
 *
 * Call [close] to close the connection.
 */
public class SuspendingBindingConnection<T>(
    private val context: Context,
    private val intentFactory: suspend () -> Intent?,
    private val bind: (IBinder) -> T,
    private val bindingFlags: Int = Context.BIND_AUTO_CREATE,
) : AutoCloseable {
    private val service: MutableStateFlow<Status<T>> = MutableStateFlow(Status.Disconnected)

    /**
     * If the service is already connected, this returns immediately. Otherwise, it suspends until
     * the connection is finished. If connection attempt fails, it returns null.
     */
    public suspend fun getOrConnect(): T? {
        val initialValue = service.value
        if (initialValue is Status.Connected) {
            return initialValue.binder
        }

        val shouldConnect = service.compareAndSet(Status.Disconnected, Status.Connecting)
        if (shouldConnect) {
            val intent = intentFactory()
            if (intent == null) {
                service.value = Status.Disconnected
                return null
            }

            context.bindService(intent, connection, bindingFlags)
        }

        return withTimeoutOrNull(BINDING_TIMEOUT) {
            service.filterIsInstance<Status.Connected<T>>().first().binder
        }
    }

    override fun close() {
        context.unbindService(connection)
        service.value = Status.Disconnected
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
            service.value = Status.Connected(bind(binder))
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service.value = Status.Connecting
        }

        override fun onBindingDied(name: ComponentName) {
            close()
        }

        override fun onNullBinding(name: ComponentName?) {
            service.value = Status.Connected(null)
        }
    }

    public sealed class Status<in T> {
        public data object Disconnected : Status<Any?>()
        public data object Connecting : Status<Any?>()
        public data class Connected<T>(val binder: T?) : Status<T>()
    }
}

private val BINDING_TIMEOUT = 10.seconds
