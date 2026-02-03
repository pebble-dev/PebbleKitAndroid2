package io.rebble.pebblekit2.client.ui

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import io.rebble.pebblekit.client.ui.R
import io.rebble.pebblekit2.client.PebbleAndroidAppPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Display either a prompt dialog for the user to grant us connecting to the currently installed Pebble app or an error message
 * if there is no Pebble app installed.
 *
 * This dialog will automatically set [PebbleAndroidAppPicker.getCurrentlySelectedApp] to the current app
 * when granted or set it to *null* when denied. Value will be set before the call to the [onDismiss] is made.
 */
@Composable
public fun PebbleAppPermissionDialog(
    pebbleAndroidAppPicker: PebbleAndroidAppPicker,
    /**
     * Called when the dialog is dismissed by the user, either when granting, denying or backing out.
     */
    onDismiss: () -> Unit,
    title: @Composable () -> Unit = { Text(stringResource(R.string.pebble_connection_permission_title)) },
    /**
     * First paragraph of the dialog text, asking user whether to grant the permission
     */
    introductionText: @Composable ColumnScope.(appName: String) -> Unit = { DefaultIntroductionText(it) },
    /**
     * Second paragraph of the dialog text, warning user why granting this to the malicious app might be bad.
     */
    rationaleText: @Composable ColumnScope.() -> Unit = {},
    /**
     * Text shown if there are no Pebble apps installed
     */
    noAppsInstalledText: @Composable () -> Unit = { Text(stringResource(R.string.no_pebble_app_text)) },
    buttonGrantText: @Composable RowScope.() -> Unit = { Text(stringResource(R.string.grant)) },
    buttonDenyText: @Composable RowScope.() -> Unit = { Text(stringResource(R.string.deny)) },
    buttonOkText: @Composable RowScope.() -> Unit = { Text(stringResource(R.string.ok)) },
) {
    val context = LocalContext.current
    var showNoAppsInstalled by rememberSaveable { mutableStateOf(false) }
    var permissionDialogState by rememberSaveable { mutableStateOf<DialogState?>(null) }
    var icon by remember { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(pebbleAndroidAppPicker) {
        @Suppress("InjectDispatcher") // Fine in this case
        withContext(Dispatchers.Default) {
            val eligibleApps = pebbleAndroidAppPicker.getAllEligibleApps()
            if (eligibleApps.isEmpty()) {
                showNoAppsInstalled = true
                return@withContext
            }

            val pkg = eligibleApps.first()

            val packageManager = context.packageManager
            val appName = try {
                context.packageManager.getApplicationInfo(pkg, 0).loadLabel(context.packageManager).toString()
            } catch (ignored: PackageManager.NameNotFoundException) {
                pkg
            }

            val mainActivityLabel = try {
                val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
                launchIntent?.let {
                    packageManager.queryIntentActivities(launchIntent, 0)
                        .firstOrNull()
                        ?.loadLabel(packageManager)
                }
            } catch (ignored: PackageManager.NameNotFoundException) {
                null
            }

            val finalLabel = if (mainActivityLabel != null && mainActivityLabel != appName) {
                "$mainActivityLabel ($appName)"
            } else {
                appName
            }

            permissionDialogState = DialogState(pkg, finalLabel)
            icon = try {
                packageManager.getApplicationIcon(pkg)
            } catch (ignored: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val currentPermissionDialogState = permissionDialogState
    if (currentPermissionDialogState != null) {
        PermissionDialog(
            onDismiss,
            title,
            introductionText,
            currentPermissionDialogState,
            rationaleText,
            icon,
            coroutineScope,
            pebbleAndroidAppPicker,
            buttonGrantText,
            buttonDenyText
        )
    } else if (showNoAppsInstalled) {
        NoAppsInstalledDialog(onDismiss, title, noAppsInstalledText, buttonOkText)
    }
}

@Composable
private fun PermissionDialog(
    onDismiss: () -> Unit,
    title: @Composable (() -> Unit),
    introductionText: @Composable (ColumnScope.(String) -> Unit),
    currentPermissionDialogState: DialogState,
    rationaleText: @Composable (ColumnScope.() -> Unit),
    icon: Drawable?,
    coroutineScope: CoroutineScope,
    pebbleAndroidAppPicker: PebbleAndroidAppPicker,
    buttonGrant: @Composable (RowScope.() -> Unit),
    buttonDeny: @Composable (RowScope.() -> Unit),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { title() },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    introductionText(currentPermissionDialogState.appTitle)
                    rationaleText()
                }

                Box(
                    Modifier
                        .padding(16.dp)
                        .size(32.dp),
                    propagateMinConstraints = true
                ) {
                    icon?.let {
                        Image(rememberDrawablePainter(it), contentDescription = null)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        pebbleAndroidAppPicker.selectApp(currentPermissionDialogState.appPackage)
                        onDismiss()
                    }
                },
                content = buttonGrant
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        pebbleAndroidAppPicker.selectApp(null)
                        onDismiss()
                    }
                },
                content = buttonDeny
            )
        }
    )
}

@Composable
private fun NoAppsInstalledDialog(
    onDismiss: () -> Unit,
    title: @Composable (() -> Unit),
    noAppsInstalledText: @Composable (() -> Unit),
    buttonOk: @Composable (RowScope.() -> Unit),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { title() },
        text = {
            noAppsInstalledText()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                },
                content = buttonOk
            )
        },
    )
}

@Composable
private fun DefaultIntroductionText(appName: String) {
    val string = buildAnnotatedString {
        append(stringResource(R.string.pebble_connection_permission_body_prefix))

        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(appName)
        }

        append(stringResource(R.string.pebble_connection_permission_body_suffix))
    }

    Text(string)
}

private data class DialogState(val appPackage: String, val appTitle: String)
