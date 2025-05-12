package com.app.yajari.utils
import android.content.Context
import com.app.yajari.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

open class TimeAgo(protected var context: Context) {

    fun timeAgo(date: Date): String {
        return timeAgo(date.time)
    }

    fun timeAgo(millis: Long): String {
        val diff = Date().time - millis

        val r = context.resources

        val prefix = r.getString(R.string.time_ago_prefix)
        val suffix = r.getString(R.string.time_ago_suffix)

        val seconds = (kotlin.math.abs(diff) / 1000).toDouble()
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val years = days / 365

        val words: String = when {
            seconds < 45 -> String.format(
                r.getString(R.string.time_ago_seconds),
                seconds.roundToInt()
            )
            seconds < 90 -> String.format(r.getString(R.string.time_ago_minute), 1)
            minutes < 45 -> String.format(
                r.getString(R.string.time_ago_minutes),
                minutes.roundToInt()
            )
            minutes < 90 -> String.format(r.getString(R.string.time_ago_hour), 1)
            hours < 24 -> String.format(r.getString(R.string.time_ago_hours), hours.roundToInt())
            hours < 42 -> String.format(r.getString(R.string.time_ago_day), 1)
            days < 30 -> String.format(r.getString(R.string.time_ago_days), days.roundToInt())
            days < 45 -> String.format(r.getString(R.string.time_ago_month), 1)
            days < 365 -> String.format(
                r.getString(R.string.time_ago_months),
                (days / 30).roundToInt()
            )
            years < 1.5 -> String.format(r.getString(R.string.time_ago_year), 1)
            else -> r.getString(R.string.time_ago_years, years.roundToInt())
        }

        val sb = StringBuilder()

        if (prefix.isNotEmpty()) {
            sb.append(prefix).append(" ")
        }

        sb.append(words)

        if (suffix.isNotEmpty() && words != "just now") {
            sb.append(" ").append(suffix)
        }

        return sb.toString().trim { it <= ' ' }
    }

    fun chatTime(sendTimeMillis: Long): String {

        val cal = Calendar.getInstance().apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
        }
        val todayStartMillis = cal.timeInMillis

        cal.add(Calendar.DATE, -1)
        val yesterdayStartMillis = cal.timeInMillis

        return when {
            sendTimeMillis > todayStartMillis -> {
                // Sent it within today time
                //SimpleDateFormat("hh:mm a", Locale.getDefault()).format(sendTimeMillis)
                "Today"
            }
            sendTimeMillis in (yesterdayStartMillis + 1) until todayStartMillis -> {
                // Sent it yesterday
                "Yesterday"
            }
            else -> {
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(sendTimeMillis)
            }
        }

    }
}

