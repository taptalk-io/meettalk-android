package io.taptalk.meettalk.helper

import android.net.Uri
import android.os.Build
import android.telecom.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.taptalk.meettalk.BuildConfig
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CALLER_NAME
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CALLER_NUMBER
import io.taptalk.meettalk.manager.MeetTalkCallManager

@RequiresApi(Build.VERSION_CODES.M)
class MeetTalkConnectionService : ConnectionService() {

    val TAG: String = MeetTalkConnectionService::class.java.simpleName

//    companion object {
        var connection: MeetTalkCallConnection? = null
//    }

    override fun onCreateIncomingConnection(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?): Connection {
        MeetTalkCallManager.callState = MeetTalkCallManager.Companion.CallState.RINGING
        val extras = request!!.extras
        val callerName = extras.getString(CALLER_NAME, "")
        val callerNumber = extras.getString(CALLER_NUMBER, "")
        connection = MeetTalkCallConnection.newInstance()
        connection?.setCallerDisplayName(callerName, TelecomManager.PRESENTATION_ALLOWED)
//        connection?.setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        connection?.setAddress(Uri.parse(callerNumber), TelecomManager.PRESENTATION_ALLOWED)
        connection?.setInitializing()
        connection?.setActive()
        if (BuildConfig.DEBUG) {
            Log.e(">>>> $TAG", "onCreateIncomingConnection: callerName: $callerName, address: ${request.address}, request: ${request.toString()}")
        }
        return connection!!
    }

    override fun onCreateIncomingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
        if (BuildConfig.DEBUG) {
            Log.e(">>>> $TAG", "onCreateIncomingFailed: ${request.toString()}")
        }
        Toast.makeText(applicationContext,"onCreateIncomingConnectionFailed",Toast.LENGTH_LONG).show()
        MeetTalkCallManager.clearPendingIncomingCall()
    }

    override fun onCreateOutgoingConnection(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?): Connection {
        MeetTalkCallManager.callState = MeetTalkCallManager.Companion.CallState.RINGING
        val extras = request!!.extras
        val callerName = extras.getString(CALLER_NAME, "")
        connection = MeetTalkCallConnection.newInstance()
        connection?.setCallerDisplayName(callerName, TelecomManager.PRESENTATION_ALLOWED)
        connection?.setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        connection?.setInitializing()
        connection?.setActive()
        if (BuildConfig.DEBUG) {
            Log.e(">>>> $TAG", "onCreateOutgoingConnection: callerName: $callerName, address: ${request.address}, request: ${request.toString()}")
        }
        return connection!!
    }

    override fun onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
        if (BuildConfig.DEBUG) {
            Log.e(">>>> $TAG", "onCreateOutgoingConnectionFailed: ${request.toString()}")
        }
        Toast.makeText(applicationContext,"onCreateOutgoingConnectionFailed",Toast.LENGTH_LONG).show();
    }
}
