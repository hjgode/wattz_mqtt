package dubrowgn.wattz

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Formatter
import java.util.Locale

//import org.eclipse.paho.android.service.MqttAndroidClient;
class MyMQTT(var context: Context) {
    var mqtt_server: String = "tcp://192.168.0.40:1883"
    var mqtt_clientid: String = "android"
    var LOG_TAG: String = "### mqtt battmon MyMqtt"

    var mqtt_topic: String = "android/battery"
    private var mqttClient: MqttClient? = null
    private var options: MqttConnectOptions? = null
    var notificationChannelIdD: String = "MQTT"

    init {
        var deviceStr = Settings.Global.getString(context.getContentResolver(), "device_name")
        deviceStr = deviceStr.replace(" ", "_")
        deviceStr = deviceStr.replace("(", "")
        deviceStr = deviceStr.replace(")", "")
        mqtt_topic = mqtt_topic + "/" + deviceStr
        Log.d(LOG_TAG, "mqtt_topic: " + mqtt_topic)
        try {
            mqttClient = MqttClient(mqtt_server, mqtt_clientid, null)
            options = MqttConnectOptions()
            options?.setCleanSession(true)
            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(LOG_TAG, "Connection lost", cause)
                }

                @Throws(Exception::class)
                override fun messageArrived(topic: String?, message: MqttMessage) {
                    Log.d(LOG_TAG, "Message arrived: " + String(message.getPayload()))
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    Log.d(LOG_TAG, "Message delivered: " + token.getMessageId())
                }
            })
        } catch (e: MqttException) {
            Log.e(LOG_TAG, "Error initializing MQTT client", e)
        }
    }

    private fun getMessageJson(level: Double?, charging: String?, timestamp: String?): String {
        val sb = StringBuilder()

        // {
        //  "level": 55,
        //  "status": "discharging",
        //  "datetime": "17.07.2024 19:38"
        //}
        val formatter = Formatter(sb, Locale.US)
        // Explicit argument indices may be used to re-order output.
        formatter.format(
            "{\"level\": %1\$d,\"status\":\"%2\$s\",\"datetime\":\"%3\$s\"}",
            level,
            charging,
            timestamp
        )
        return sb.toString()
    }

    fun doPublish(level: Double?, charging: Boolean): Boolean {
        var bRes = true
        val clientId = this.mqtt_clientid
        var topic: String? = ""
        val date = Date()
        try {
            val FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMAN)
            val formattedDate = FORMATTER.format(date)
            //MqttAndroidClient client = new MqttAndroidClient(this.context, this.mqtt_server, clientId);
            mqttClient!!.connect()
            if (mqttClient!!.isConnected()) {
                val chargingTxt: String?
                if (charging) chargingTxt = "charging"
                else chargingTxt = "discharging"
                val json = getMessageJson(level, chargingTxt, formattedDate)
                val message = MqttMessage(json.toByteArray())

                //MqttMessage message = new MqttMessage(String.valueOf(level).getBytes());
                message.setRetained(true)
                topic = mqtt_topic //+"/battery";
                mqttClient?.publish(topic, message)
                //                Log.d(LOG_TAG, "published: "+topic+":"+String.valueOf(level));
                Log.d(LOG_TAG, "published: " + topic + ": " + json)
                /*
                message = new MqttMessage(chargingTxt.getBytes());
                message.setRetained(true);
                topic=mqtt_topic+"/charging";
                mqttClient.publish(topic, message);
                Log.d(LOG_TAG, "published: "+topic+":"+chargingTxt);

                topic=mqtt_topic+"/timestamp";
                mqttClient.publish(topic, new MqttMessage(formattedDate.getBytes()));
                message.setRetained(true);
                Log.d(LOG_TAG, "published: "+topic+":"+formattedDate);
*/
                mqttClient?.disconnect()
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.message!!)
            bRes = false
        }
        return bRes
    }


    private fun createNotification(): Notification {
        createChannel()
        val mainActivityIntent = Intent(context, MainActivity::class.java)
        var pendingIntentFlag = 0
        pendingIntentFlag = PendingIntent.FLAG_IMMUTABLE
        val mainActivityPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainActivityIntent,
            pendingIntentFlag
        )
        val notification = NotificationCompat.Builder(context, notificationChannelIdD)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentInfo("MQTT publish done")
            .setContentIntent(mainActivityPendingIntent)
            .setAutoCancel(true)
            .build()

        return notification
    }

    private fun createChannel() {
        // Create a Notification channel
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        val name: CharSequence = "MQTT channel"
        val description = "MQTT notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            notificationChannelIdD,
            "DoWork Worker",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.setDescription(description)
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        val notificationManager = ContextCompat.getSystemService<NotificationManager?>(
            context,
            NotificationManager::class.java
        )
        if (notificationManager != null) notificationManager.createNotificationChannel(channel)
        else {
            Log.d(LOG_TAG, "notificationManager ist NULL")
        }
    }
}
