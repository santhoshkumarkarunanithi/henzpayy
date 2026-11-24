package com.henzpay.alerts

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class PayNotificationService : NotificationListenerService() {
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this) { if (it == TextToSpeech.SUCCESS) { tts?.language = Locale("en", "IN"); ttsReady = true } }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val source = PAY_APPS[sbn.packageName] ?: return
        val ex = sbn.notification.extras
        val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (ex.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: ex.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        val p = PaymentParser.parse(source, title, text, sbn.postTime) ?: return
        if (!PaymentStore.add(this, p)) return
        if (ttsReady && PaymentStore.voice(this)) {
            val amt = if (p.amount % 1.0 == 0.0) p.amount.toLong().toString() else p.amount.toString()
            tts?.speak("$source. $amt rupees received", TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    override fun onDestroy() { tts?.shutdown(); super.onDestroy() }
}
