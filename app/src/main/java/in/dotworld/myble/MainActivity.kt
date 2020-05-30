package `in`.dotworld.myble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import pub.devrel.easypermissions.EasyPermissions
@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    lateinit var context: Context
    var mbluetoothadapter=BluetoothAdapter.getDefaultAdapter()
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context=this
        var mbluetoothadapter=BluetoothAdapter.getDefaultAdapter()
        if(mbluetoothadapter!=null && !mbluetoothadapter.isEnabled){
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),1)
        }

        val perms= arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if(!EasyPermissions.hasPermissions(this,*perms)){
            EasyPermissions.requestPermissions(
                this,
                "app needs permissions/nphone state/nlocation/nstorage",
                0,
                *perms
            )
        }
        if(mbluetoothadapter != null){
            val blescanner=mbluetoothadapter.bluetoothLeScanner
            val callback=LocalScanCallback(mbluetoothadapter,applicationContext)
            blescanner.startScan(callback)
        }
    }

    override fun onRestart() {
        super.onRestart()
        if(mbluetoothadapter!=null && !mbluetoothadapter.isEnabled){
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),1)
        }
    }
}
