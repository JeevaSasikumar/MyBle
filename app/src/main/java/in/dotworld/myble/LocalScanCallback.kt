package `in`.dotworld.myble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.O)
class LocalScanCallback(private val mbluetoothadapter:BluetoothAdapter,context: Context):ScanCallback() {
    private val context: Context = context
    fun onCreate(obj:JSONObject){
        context.cacheDir.deleteRecursively()
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val clientid = tm.imei
        val client = MqttAndroidClient(context.applicationContext, "tcp://broker.hivemq.com:1883", clientid)
        val options = MqttConnectOptions()
        val token = client.connect(options)
        token.actionCallback=object : IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Log.d("publish - ","connected")
                val message=MqttMessage(obj.toString().toByteArray())
                message.qos=0
                message.isRetained=false
                val topic="dotworld/beacons"
                client.publish(topic, message)
                Log.d("publish - ","published")
                token.actionCallback= object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.d("publish - ","disconnected")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.d("publish - ","not disconnected")
                    }
                }
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Log.d("publish - ","not connected")
            }
        }
    }

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)

        if ((result.device.name != null) && result.device.name.startsWith("April")) {
            Log.d("DeviceName", "DeviceName - ${result.device.name}")
            Log.d("DeviceName", "LocalScanCallback started")
            val t=Thread {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val clientid = tm.imei
                val obj: JSONObject = JSONObject()
                obj.put("IMEI",clientid)
                obj.put("DeviceName", result.device.name)
                obj.put("DeviceAddress", result.device.address)
                obj.put("DeviceRssi", result.rssi)
                val dat=Date()
                val formatter=SimpleDateFormat("HH:mm:ss")
                val date=formatter.format(dat)
                obj.put("DeviceTime", date)
                val secondformatter=SimpleDateFormat("s")
                val sec=(secondformatter.format(dat)).toInt()
                if(sec%5==0){
                    onCreate(obj)
                }
                Log.d("April","beat")
            }
            t.start()
        }
    }
}
