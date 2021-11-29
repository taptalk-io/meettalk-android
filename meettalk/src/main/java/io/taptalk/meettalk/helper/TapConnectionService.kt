package io.taptalk.meettalk.helper

import android.net.Uri
import android.os.Build
import android.telecom.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CALLER_NAME
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CALLER_NUMBER
import io.taptalk.meettalk.manager.TapCallManager

@RequiresApi(Build.VERSION_CODES.M)
class TapConnectionService : ConnectionService() {

    val TAG: String = TapConnectionService::class.java.simpleName

//    companion object {
        var connection: TapCallConnection? = null
//    }

    override fun onCreateIncomingConnection(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?): Connection {
        TapCallManager.callState = TapCallManager.Companion.CallState.RINGING
        val extras = request!!.extras
        val callerName = extras.getString(CALLER_NAME, "")
        val callerNumber = extras.getString(CALLER_NUMBER, "")
        connection = TapCallConnection.newInstance()
        connection?.setCallerDisplayName(callerName, TelecomManager.PRESENTATION_ALLOWED)
//        connection?.setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        connection?.setAddress(Uri.parse(callerNumber), TelecomManager.PRESENTATION_ALLOWED)
        connection?.setInitializing()
        connection?.setActive()
        Log.e(">>>> $TAG", "onCreateIncomingConnection: callerName: $callerName, address: ${request.address}, request: ${request.toString()}")
        return connection!!
    }

    override fun onCreateIncomingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
        Log.e(">>>> $TAG", "onCreateIncomingFailed: ${request.toString()}")
        Toast.makeText(applicationContext,"onCreateIncomingConnectionFailed",Toast.LENGTH_LONG).show()
        TapCallManager.clearPendingIncomingCall()
    }

    override fun onCreateOutgoingConnection(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?): Connection {
        TapCallManager.callState = TapCallManager.Companion.CallState.RINGING
        val extras = request!!.extras
        val callerName = extras.getString(CALLER_NAME, "")
        connection = TapCallConnection.newInstance()
        connection?.setCallerDisplayName(callerName, TelecomManager.PRESENTATION_ALLOWED)
        connection?.setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        connection?.setInitializing()
        connection?.setActive()
        Log.e(">>>> $TAG", "onCreateOutgoingConnection: callerName: $callerName, address: ${request.address}, request: ${request.toString()}")
        return connection!!
    }

    override fun onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
        Log.e(">>>> $TAG", "onCreateOutgoingConnectionFailed: ${request.toString()}")
        Toast.makeText(applicationContext,"onCreateOutgoingConnectionFailed",Toast.LENGTH_LONG).show();
    }
}
