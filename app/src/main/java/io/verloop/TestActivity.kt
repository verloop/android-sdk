package io.verloop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import io.verloop.sdk.LiveChatUrlClickListener
import io.verloop.sdk.Verloop
import io.verloop.sdk.VerloopConfig

class TestActivity : AppCompatActivity() {
    private val TAG: String = "TestActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)

        val closeBtn1 = findViewById<Button>(R.id.close_chat_1)
        val closeBtn2 = findViewById<Button>(R.id.close_chat_2)

        val editClientId1 = findViewById<EditText>(R.id.editClientId1)
        val editClientId2 = findViewById<EditText>(R.id.editClientId2)

        val editUserId1 = findViewById<EditText>(R.id.editUserId1)
        val editUserId2 = findViewById<EditText>(R.id.editUserId2)

        val editRecipeId1 = findViewById<EditText>(R.id.editRecipeId1)
        val editRecipeId2 = findViewById<EditText>(R.id.editRecipeId2)

        var verloopConfig: VerloopConfig? = null
        var verloop: Verloop? = null

        var verloopConfig2: VerloopConfig? = null
        var verloop2: Verloop? = null

        button1.setOnClickListener {
            val clientId = editClientId1.text?.toString()
            val userId = editUserId1.text?.toString()
            val recipeId = editRecipeId1.text?.toString()
            verloopConfig =
                VerloopConfig.Builder().clientId(clientId).userId(userId!!).isStaging(false).build()

            verloopConfig?.userId = userId
            verloopConfig?.recipeId = recipeId

            verloopConfig?.setUrlClickListener(object : LiveChatUrlClickListener {
                override fun urlClicked(url: String?) {
                    Log.i(TAG, "Chat 01 url clicked")
                }
            })
            verloop = Verloop(this, verloopConfig!!)
            verloop?.showChat()
        }

        button2.setOnClickListener {
            val clientId = editClientId2.text?.toString()
            val userId = editUserId2.text?.toString()
            val recipeId = editRecipeId2.text?.toString()
            verloopConfig2 =
                VerloopConfig.Builder().clientId(clientId).userId(userId!!).isStaging(false).build()
            verloopConfig2?.userId = userId
            verloopConfig2?.recipeId = recipeId

            verloopConfig2?.setUrlClickListener(object : LiveChatUrlClickListener {
                override fun urlClicked(url: String?) {
                    Log.i(TAG, "Chat 02 url clicked")
                }
            })
            verloop2 = Verloop(this, verloopConfig2!!)
            verloop2?.showChat()
        }

        closeBtn1.setOnClickListener(View.OnClickListener {
            Log.d(TAG,"logging out")
            verloop?.logout()
        })

        closeBtn2.setOnClickListener(View.OnClickListener {
            verloop2?.logout()
        })
    }
}