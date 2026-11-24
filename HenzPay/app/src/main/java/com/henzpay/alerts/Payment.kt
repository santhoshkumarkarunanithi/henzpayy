package com.henzpay.alerts

import org.json.JSONArray
import org.json.JSONObject

data class Payment(val source: String, val amount: Double, val sender: String, val time: Long)

/** Apps we listen to. Edit this map to add more (package name -> display name). */
val PAY_APPS = mapOf(
    "com.phonepe.app" to "PhonePe",
    "com.google.android.apps.nbu.paisa.user" to "Google Pay",
    "net.one97.paytm" to "Paytm",
    "in.org.npci.upiapp" to "BHIM"
)

object PaymentParser {
    private val amountRx = Regex("""(?:₹|Rs\.?|INR)\s?([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
    private val receivedRx = Regex("""received|paid you|sent you|credited|has paid""", RegexOption.IGNORE_CASE)
    private val sentRx = Regex("""you paid|you sent|debited|paid to|sent to""", RegexOption.IGNORE_CASE)

    fun parse(source: String, title: String, text: String, time: Long): Payment? {
        val all = "$title $text"
        if (sentRx.containsMatchIn(all) && !receivedRx.containsMatchIn(all)) return null
        if (!receivedRx.containsMatchIn(all)) return null
        val amt = amountRx.find(all)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: return null
        val sender = Regex("""from\s+([A-Za-z .]+)""", RegexOption.IGNORE_CASE)
            .find(all)?.groupValues?.get(1)?.trim()
            ?: Regex("""^([A-Za-z .]+?)\s+(?:paid|sent)""", RegexOption.IGNORE_CASE).find(all)?.groupValues?.get(1)?.trim()
            ?: "Customer"
        return Payment(source, amt, sender, time)
    }
}

object PaymentStore {
    private const val PREF = "henzpay"
    val listeners = mutableListOf<() -> Unit>()

    fun all(c: android.content.Context): List<Payment> {
        val arr = JSONArray(c.getSharedPreferences(PREF, 0).getString("list", "[]"))
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            Payment(o.getString("s"), o.getDouble("a"), o.getString("n"), o.getLong("t"))
        }
    }

    /** Returns false if it is a duplicate (same app, amount, within 20s). */
    @Synchronized fun add(c: android.content.Context, p: Payment): Boolean {
        val cur = all(c)
        if (cur.any { it.source == p.source && it.amount == p.amount && kotlin.math.abs(it.time - p.time) < 20_000 }) return false
        val arr = JSONArray()
        (listOf(p) + cur).take(500).forEach {
            arr.put(JSONObject().put("s", it.source).put("a", it.amount).put("n", it.sender).put("t", it.time))
        }
        c.getSharedPreferences(PREF, 0).edit().putString("list", arr.toString()).apply()
        listeners.toList().forEach { it() }
        return true
    }

    fun voice(c: android.content.Context) = c.getSharedPreferences(PREF, 0).getBoolean("voice", true)
    fun setVoice(c: android.content.Context, v: Boolean) = c.getSharedPreferences(PREF, 0).edit().putBoolean("voice", v).apply()
}
