package io.verloop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import io.verloop.sdk.LiveChatUrlClickListener
import io.verloop.sdk.Verloop
import io.verloop.sdk.VerloopConfig
import io.verloop.sdk.VerloopException
import com.google.firebase.messaging.FirebaseMessaging


class TestActivity : AppCompatActivity() {

    private val TAG: String = "TestActivity"

    var verloop: Verloop? = null
    var verloop2: Verloop? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && intent.extras != null) {
            val clientId = intent.extras?.get("clientId")
            val userId = intent.extras?.get("userId")
            Log.i(TAG, "clientId: $clientId, userId: $userId")
            if (clientId != null && userId != null) {
                verloop?.showChat()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        if (intent.extras != null) {
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!!.getString(key)
                Log.d(TAG, "Key: $key Value: $value")
            }
        }
        var verloopConfig: VerloopConfig? = null

        var verloopConfig2: VerloopConfig? = null

        var fcmToken: String? = null

        val btnStartChat1 = findViewById<Button>(R.id.button1)
        val btnStartChat2 = findViewById<Button>(R.id.button2)

        val btnClose1 = findViewById<Button>(R.id.close_chat_1)
        val btnClose2 = findViewById<Button>(R.id.close_chat_2)

        val clientId1 = findViewById<EditText>(R.id.editClientId1)
        val clientId2 = findViewById<EditText>(R.id.editClientId2)

        val userId1 = findViewById<EditText>(R.id.editUserId1)
        val userId2 = findViewById<EditText>(R.id.editUserId2)

        val recipeId1 = findViewById<EditText>(R.id.editRecipeId1)
        val recipeId2 = findViewById<EditText>(R.id.editRecipeId2)

        val name1 = findViewById<EditText>(R.id.editUserName1)
        val name2 = findViewById<EditText>(R.id.editUserName2)

        val email1 = findViewById<EditText>(R.id.editUserEmail1)
        val email2 = findViewById<EditText>(R.id.editUserEmail2)

        val phone1 = findViewById<EditText>(R.id.editUserPhone1)
        val phone2 = findViewById<EditText>(R.id.editUserPhone2)

        val department1 = findViewById<EditText>(R.id.editDepartment1)
        val department2 = findViewById<EditText>(R.id.editDepartment2)

        val checkBoxIsStaging = findViewById<CheckBox>(R.id.checkBoxStaging)
        val checkBoxRegisterFCMToken = findViewById<CheckBox>(R.id.checkBoxFCM)

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isComplete) {
                fcmToken = it.result.toString()
                fcmToken?.let { it1 -> Log.i("FCM Token", it1) }
            }
        }

        btnStartChat1.setOnClickListener {
            try {
                verloopConfig =
                    VerloopConfig.Builder()
                        .clientId(clientId1.text?.toString())
                        .userId(userId1.text?.toString())
                        .recipeId(recipeId1.text?.toString())
                        .userName(name1.text?.toString())
                        .userEmail(email1.text?.toString())
                        .userPhone(phone1.text?.toString())
                        .department(department1.text?.toString())
                        .fcmToken(if (checkBoxRegisterFCMToken.isChecked) fcmToken else null)
                        .isStaging(checkBoxIsStaging.isChecked).build()

                verloopConfig?.setUrlClickListener(object : LiveChatUrlClickListener {
                    override fun urlClicked(url: String?) {
                        Toast.makeText(applicationContext, "Chat 1: $url", Toast.LENGTH_SHORT)
                            .show()
                        val i = Intent(this@TestActivity, ProductDetailsActivity::class.java)
                        i.putExtra("config", verloopConfig)
                        startActivity(i)
                    }
                }, false)
                verloop = Verloop(this, verloopConfig!!)
                verloop?.showChat()
            } catch (e: VerloopException) {
                Log.e(TAG, e.message.toString())
            }
        }

        btnStartChat2.setOnClickListener {
            verloopConfig2 =
                VerloopConfig.Builder()
                    .clientId(clientId2.text?.toString())
                    .userId(userId2.text?.toString())
                    .isStaging(false).build()

            verloopConfig2?.setUrlClickListener(object : LiveChatUrlClickListener {
                override fun urlClicked(url: String?) {
                    Toast.makeText(applicationContext, "Chat 2: $url", Toast.LENGTH_SHORT).show()
                    val i = Intent(this@TestActivity, ProductDetailsActivity::class.java)
                    i.putExtra("config", verloopConfig)
                    startActivity(i)
                }
            }, true)
            verloop2 = Verloop(this, verloopConfig2!!)
            verloop2?.showChat()
        }

        btnClose1.setOnClickListener {
            verloop?.logout()
        }

        btnClose2.setOnClickListener {
            verloop2?.logout()
        }
    }
}