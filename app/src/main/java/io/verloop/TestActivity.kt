package io.verloop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import io.verloop.sdk.LiveChatUrlClickListener
import io.verloop.sdk.Verloop
import io.verloop.sdk.VerloopConfig
import io.verloop.sdk.VerloopException
import com.google.firebase.messaging.FirebaseMessaging
import android.widget.EditText
import android.view.LayoutInflater
import android.view.View
import org.json.JSONObject

class TestActivity : AppCompatActivity() {

    private val TAG: String = "TestActivity"

    var verloop: Verloop? = null
    var verloop2: Verloop? = null

    var clientId: String? = null
    var userId: String? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null && intent.extras != null) {
            val verloopData = intent.extras?.get("verloop")
            if (verloopData != null) {
                val obj = JSONObject(verloopData.toString())
                if (obj.has("client_id")) clientId = obj.getString("client_id")
                if (obj.has("userId")) userId = obj.getString("userId")
            }
            if (clientId === null) clientId = intent.extras?.get("clientId") as String?
            if (userId === null) userId = intent.extras?.get("userId") as String?
            if (clientId != null) {
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
        val checkOverrideUrlClick = findViewById<CheckBox>(R.id.checkOverrideUrlClick)

        val btnAdd = findViewById<Button>(R.id.buttonAdd)
        val containerCustomFields = findViewById<LinearLayout>(R.id.containerCustomFields)
        val editCustomField = findViewById<LinearLayout>(R.id.editCustomField)

        val allCustomKeys: ArrayList<EditText> = ArrayList()
        val allCustomValues: ArrayList<EditText> = ArrayList()
        val allCustomScopes: ArrayList<CheckBox> = ArrayList()
        allCustomKeys.add(editCustomField.findViewById(R.id.editCustomKey))
        allCustomValues.add(editCustomField.findViewById(R.id.editCustomValue))
        allCustomScopes.add(editCustomField.findViewById(R.id.checkBoxRoom))

        btnAdd.setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.setMargins(8, 0, 8, 0)
            val field: View = inflater.inflate(R.layout.edit_text_view, null, false)
            field.layoutParams = params
            containerCustomFields.addView(field)
            allCustomKeys.add(field.findViewById(R.id.editCustomKey))
            allCustomValues.add(field.findViewById(R.id.editCustomValue))
            allCustomScopes.add(field.findViewById(R.id.checkBoxRoom))
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isComplete) {
                fcmToken = it.result.toString()
                fcmToken?.let { it1 -> Log.i("FCM Token", it1) }
            }
        }

        btnStartChat1.setOnClickListener {
            try {
                var customFields: ArrayList<VerloopConfig.CustomField> = ArrayList()
                for (i in 0 until allCustomKeys.size - 1) {
                    val key = allCustomKeys[i].text.toString()
                    val value = allCustomValues[i].text.toString()
                    val scope =
                        if (allCustomScopes[i].isChecked) VerloopConfig.Scope.ROOM else VerloopConfig.Scope.USER
                    val customField = VerloopConfig.CustomField(key, value, scope)
                    customFields.add(customField)
                }

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
                        .fields(customFields)
                        .isStaging(checkBoxIsStaging.isChecked).build()

                verloopConfig?.setUrlClickListener(object : LiveChatUrlClickListener {
                    override fun urlClicked(url: String?) {
                        Toast.makeText(applicationContext, "Chat 1: $url", Toast.LENGTH_SHORT)
                            .show()
                        val i = Intent(this@TestActivity, ProductDetailsActivity::class.java)
                        i.putExtra("config", verloopConfig)
                        startActivity(i)
                    }
                }, checkOverrideUrlClick.isChecked)
                verloop = Verloop(this, verloopConfig!!)
                verloop?.showChat()
            } catch (e: VerloopException) {
                Log.e(TAG, e.message.toString())
                Toast.makeText(this@TestActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        btnStartChat2.setOnClickListener {
            try {
                var customFields: ArrayList<VerloopConfig.CustomField> = ArrayList()
                for (i in 0 until allCustomKeys.size - 1) {
                    val key = allCustomKeys[i].text.toString()
                    val value = allCustomValues[i].text.toString()
                    val scope =
                        if (allCustomScopes[i].isChecked) VerloopConfig.Scope.ROOM else VerloopConfig.Scope.USER
                    val customField = VerloopConfig.CustomField(key, value, scope)
                    customFields.add(customField)
                }

                verloopConfig2 =
                    VerloopConfig.Builder()
                        .clientId(clientId2.text?.toString())
                        .userId(userId2.text?.toString())
                        .fields(customFields)
                        .isStaging(false).build()

                verloopConfig2?.setUrlClickListener(object : LiveChatUrlClickListener {
                    override fun urlClicked(url: String?) {
                        Toast.makeText(applicationContext, "Chat 2: $url", Toast.LENGTH_SHORT)
                            .show()
                        val i = Intent(this@TestActivity, ProductDetailsActivity::class.java)
                        i.putExtra("config", verloopConfig)
                        startActivity(i)
                    }
                }, checkOverrideUrlClick.isChecked)
                verloop2 = Verloop(this, verloopConfig2!!)
                verloop2?.showChat()
            } catch (e: VerloopException) {
                Log.e(TAG, e.message.toString())
                Toast.makeText(this@TestActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        btnClose1.setOnClickListener {
            verloop?.logout()
        }

        btnClose2.setOnClickListener {
            verloop2?.logout()
        }
    }
}