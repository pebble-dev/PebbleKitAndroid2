package io.rebble.pebblekit2.common.model

import kotlin.time.Duration
import kotlin.time.Instant

public data class TimelinePin(
    /**
     * Developer-implemented identifier for this pin event.
     */
    val id: String,

    /**
     * The start time of the event the pin represents, such as the beginning of a meeting.
     * See [Pin Time Limitations](https://developer.repebble.com/guides/pebble-timeline/timeline-public/#pin-time-limitations)
     * for information on the acceptable time range.
     */
    val startTime: Instant,

    /**
     * The duration of the event the pin represents, in minutes.
     */
    val duration: Duration? = null,

    /**
     * Description of the values to populate the layout when the user views the pin.
     */
    val layout: TimelineLayout,

    /**
     * Optional reminders that buzz the watch before this pin's start time.
     * Each reminder is inserted into BlobDatabase.Reminder and linked to this pin.
     */
    val reminders: List<TimelineReminder> = emptyList(),
) {
    public companion object
}

/**
 * A reminder that fires before a [TimelinePin] and buzzes the watch.
 *
 * Use [TimelineLayoutType.GENERIC_REMINDER] for the layout type, and
 * `system://images/NOTIFICATION_REMINDER` as the icon.
 *
 * @param time absolute time at which the reminder fires (e.g. pin.startTime - 15.minutes)
 * @param layout layout shown on the watch when the reminder fires
 */
public data class TimelineReminder(
    val time: Instant,
    val layout: TimelineLayout,
) {
    public companion object
}

public data class TimelineLayout(
    /**
     * The type of layout the pin will use.
     * See [Pin Layouts](https://developer.repebble.com/guides/pebble-timeline/pin-structure/#pin-layouts)
     * for a list of available types.
     */
    val type: TimelineLayoutType,

    /**
     * The title of the pin when viewed.
     */
    val title: String? = null,

    /**
     * Shorter subtitle for details.
     */
    val subtitle: String? = null,

    /**
     * The body text of the pin. Maximum of 512 characters.
     */
    val body: String? = null,

    /**
     * URI of the pin's tiny icon.
     *
     * See [The list of icons](https://developer.repebble.com/guides/pebble-timeline/pin-structure/#pin-icons).
     */
    val tinyIcon: String? = null,

    /**
     * URI of the pin's small icon.
     *
     * See [The list of icons](https://developer.repebble.com/guides/pebble-timeline/pin-structure/#pin-icons).
     */
    val smallIcon: String? = null,

    /**
     * URI of the pin's large icon.
     *
     * See [The list of icons](https://developer.repebble.com/guides/pebble-timeline/pin-structure/#pin-icons).
     */
    val largeIcon: String? = null,

    /**
     * Six-digit color hexadecimal string or case-insensitive SDK constant (e.g.: "665566" or "mintgreen"),
     * describing the primary text color.
     */
    val primaryColor: String? = null,

    /**
     * Similar to [primaryColor], except applies to the layout's secondary-colored elements.
     */
    val secondaryColor: String? = null,

    /**
     * Similar to [primaryColor], except applies to the layout's background color.
     */
    val backgroundColor: String? = null,

    /**
     * List of section headings in this layout. The list must be less than 128 characters in length,
     * including the underlying delimiters (one byte) between each item.
     * Longer items will be truncated with an ellipsis ('...').
     */
    val headings: List<String>? = null,

    /**
     * List of paragraphs in this layout. Must equal the number of [headings].
     * The list must be less than 1024 characters in length, including the underlying delimiters (one byte)
     * between each item. Longer items will be truncated with an ellipsis ('...').
     */
    val paragraphs: List<String>? = null,

    /**
     * Timestamp of when the pin’s data (e.g: weather forecast or sports score) was last updated.
     */
    val lastUpdated: Instant? = null,
) {
    public companion object
}

public enum class TimelineLayoutType(public val code: String) {
    GENERIC_PIN("genericPin"),
    CALENDAR_PIN("calendarPin"),
    GENERIC_REMINDER("genericReminder"),
    GENERIC_NOTIFICATION("genericNotification"),
    COMMN_NOTIFICATION("commNotification"),
    WEATHER_PIN("weatherPin"),
    SPORTS_PIN("sportsPin"),
    ;

    public companion object
}
