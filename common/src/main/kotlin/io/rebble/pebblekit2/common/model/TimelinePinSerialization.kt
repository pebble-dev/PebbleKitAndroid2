package io.rebble.pebblekit2.common.model

import android.os.Bundle
import co.touchlab.kermit.Logger
import kotlin.time.Duration
import kotlin.time.Instant

public fun TimelinePin.Companion.fromBundle(bundle: Bundle): TimelinePin {
    val reminderCount = bundle.getInt(KEY_REMINDERS_COUNT, 0)
    val reminders = (0 until reminderCount).map { i ->
        TimelineReminder.fromBundle(bundle.getBundle("$KEY_REMINDER_PREFIX$i") ?: Bundle())
    }
    return TimelinePin(
        id = bundle.getString(KEY_ID) ?: error("missing id"),
        startTime = Instant.parse(bundle.getString(KEY_START_TIME) ?: error("missing start time")),
        duration = bundle.getString(KEY_DURATION)?.let { Duration.parse(it) },
        layout = TimelineLayout.fromBundle(bundle),
        reminders = reminders,
    )
}

public fun TimelineReminder.Companion.fromBundle(bundle: Bundle): TimelineReminder {
    return TimelineReminder(
        time = Instant.parse(bundle.getString(KEY_REMINDER_TIME) ?: error("missing reminder time")),
        layout = TimelineLayout.fromBundle(bundle),
    )
}

@Suppress("CyclomaticComplexMethod") // Lots of fields, just a big copy paste
public fun TimelinePin.toBundle(): Bundle {
    return Bundle().apply {
        putString(KEY_ID, id)
        putString(KEY_START_TIME, startTime.toString())
        duration?.let { putString(KEY_DURATION, it.toIsoString()) }
        putLayoutFields(layout)
        putInt(KEY_REMINDERS_COUNT, reminders.size)
        reminders.forEachIndexed { i, reminder ->
            putBundle("$KEY_REMINDER_PREFIX$i", reminder.toBundle())
        }
    }
}

public fun TimelineReminder.toBundle(): Bundle {
    return Bundle().apply {
        putString(KEY_REMINDER_TIME, time.toString())
        putLayoutFields(layout)
    }
}

private fun Bundle.putLayoutFields(layout: TimelineLayout) {
    putString(KEY_LAYOUT_TYPE, layout.type.code)
    layout.title?.let { putString(KEY_LAYOUT_TITLE, it) }
    layout.subtitle?.let { putString(KEY_LAYOUT_SUBTITLE, it) }
    layout.body?.let { putString(KEY_LAYOUT_BODY, it) }
    layout.tinyIcon?.let { putString(KEY_LAYOUT_TINY_ICON, it) }
    layout.smallIcon?.let { putString(KEY_LAYOUT_SMALL_ICON, it) }
    layout.largeIcon?.let { putString(KEY_LAYOUT_LARGE_ICON, it) }
    layout.primaryColor?.let { putString(KEY_LAYOUT_PRIMARY_COLOR, it) }
    layout.secondaryColor?.let { putString(KEY_LAYOUT_SECONDARY_COLOR, it) }
    layout.backgroundColor?.let { putString(KEY_LAYOUT_BACKGROUND_COLOR, it) }
    layout.headings?.let { putStringArray(KEY_LAYOUT_HEADINGS, it.toTypedArray()) }
    layout.paragraphs?.let { putStringArray(KEY_LAYOUT_PARAGRAPHS, it.toTypedArray()) }
    layout.lastUpdated?.let { putString(KEY_LAYOUT_LAST_UPDATED, it.toString()) }
}

private fun TimelineLayout.Companion.fromBundle(bundle: Bundle): TimelineLayout {
    val typeCode = bundle.getString(KEY_LAYOUT_TYPE).orEmpty()
    val type = TimelineLayoutType.entries.firstOrNull { it.code == typeCode }
        ?: run {
            Logger.withTag("PebbleKit")
                .e { "Got unknown layout type '$typeCode' while decoding TimelinePin" }
            TimelineLayoutType.GENERIC_PIN
        }
    return TimelineLayout(
        type = type,
        title = bundle.getString(KEY_LAYOUT_TITLE),
        subtitle = bundle.getString(KEY_LAYOUT_SUBTITLE),
        body = bundle.getString(KEY_LAYOUT_BODY),
        tinyIcon = bundle.getString(KEY_LAYOUT_TINY_ICON),
        smallIcon = bundle.getString(KEY_LAYOUT_SMALL_ICON),
        largeIcon = bundle.getString(KEY_LAYOUT_LARGE_ICON),
        primaryColor = bundle.getString(KEY_LAYOUT_PRIMARY_COLOR),
        secondaryColor = bundle.getString(KEY_LAYOUT_SECONDARY_COLOR),
        backgroundColor = bundle.getString(KEY_LAYOUT_BACKGROUND_COLOR),
        headings = bundle.getStringArray(KEY_LAYOUT_HEADINGS)?.toList(),
        paragraphs = bundle.getStringArray(KEY_LAYOUT_PARAGRAPHS)?.toList(),
        lastUpdated = bundle.getString(KEY_LAYOUT_LAST_UPDATED)?.let { Instant.parse(it) },
    )
}

private const val KEY_ID = "ID"
private const val KEY_START_TIME = "START_TIME"
private const val KEY_DURATION = "DURATION"
private const val KEY_LAYOUT_TYPE = "LAYOUT_TYPE"
private const val KEY_LAYOUT_TITLE = "LAYOUT_TITLE"
private const val KEY_LAYOUT_SUBTITLE = "LAYOUT_SUBTITLE"
private const val KEY_LAYOUT_BODY = "LAYOUT_BODY"
private const val KEY_LAYOUT_TINY_ICON = "LAYOUT_TINY_ICON"
private const val KEY_LAYOUT_SMALL_ICON = "LAYOUT_SMALL_ICON"
private const val KEY_LAYOUT_LARGE_ICON = "LAYOUT_LARGE_ICON"
private const val KEY_LAYOUT_PRIMARY_COLOR = "LAYOUT_PRIMARY_COLOR"
private const val KEY_LAYOUT_SECONDARY_COLOR = "LAYOUT_SECONDARY_COLOR"
private const val KEY_LAYOUT_BACKGROUND_COLOR = "LAYOUT_BACKGROUND_COLOR"
private const val KEY_LAYOUT_HEADINGS = "LAYOUT_HEADINGS"
private const val KEY_LAYOUT_PARAGRAPHS = "LAYOUT_PARAGRAPHS"
private const val KEY_LAYOUT_LAST_UPDATED = "LAYOUT_LAST_UPDATED"
private const val KEY_REMINDERS_COUNT = "REMINDERS_COUNT"
private const val KEY_REMINDER_PREFIX = "REMINDER_"
private const val KEY_REMINDER_TIME = "REMINDER_TIME"
