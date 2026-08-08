package fr.outadoc.justchatting.utils.presentation

import android.content.Context
import android.hardware.input.InputManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.InputDevice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberHasPointingDevice(): Boolean {
    val context = LocalContext.current
    val inputManager =
        remember(context) {
            context.getSystemService(Context.INPUT_SERVICE) as InputManager
        }

    var hasPointingDevice by remember(inputManager) {
        mutableStateOf(inputManager.hasPointingDevice())
    }

    DisposableEffect(inputManager) {
        val listener =
            object : InputManager.InputDeviceListener {
                override fun onInputDeviceAdded(deviceId: Int) = update()

                override fun onInputDeviceRemoved(deviceId: Int) = update()

                override fun onInputDeviceChanged(deviceId: Int) = update()

                fun update() {
                    hasPointingDevice = inputManager.hasPointingDevice()
                }
            }

        inputManager.registerInputDeviceListener(listener, Handler(Looper.getMainLooper()))
        listener.update()

        onDispose {
            inputManager.unregisterInputDeviceListener(listener)
        }
    }

    return hasPointingDevice
}

private fun InputManager.hasPointingDevice(): Boolean =
    inputDeviceIds.any { id ->
        val device = getInputDevice(id)
        device != null &&
            !device.isVirtual &&
            // Built-in components (e.g. Samsung's "sec_touchpad" node backing S Pen air actions)
            // report MOUSE/TOUCHPAD sources too, so only external devices count as an actual
            // pointing device the user attached.
            device.isKnownExternal() &&
            (
                device.supportsSource(InputDevice.SOURCE_MOUSE) ||
                    device.supportsSource(InputDevice.SOURCE_STYLUS) ||
                    device.supportsSource(InputDevice.SOURCE_TOUCHPAD)
            )
    }

// InputDevice.isExternal() requires API 29; treat it as unknown (and therefore excluded)
// on older versions, since we can't reliably tell built-in pointer-capable nodes apart
// from actually attached devices there.
private fun InputDevice.isKnownExternal(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isExternal
