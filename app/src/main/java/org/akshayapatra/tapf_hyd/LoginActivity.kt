package org.akshayapatra.tapf_hyd

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase


class LoginActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        progressDialog = ProgressDialog(this)
        progressDialog?.setTitle("Loading")
        progressDialog?.isIndeterminate = true

        mAuth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {

        if (currentUser != null) {
            if (currentUser.displayName.isNullOrBlank()) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Enter Your Name")

                val input = EditText(this)
                input.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                input.hint = "Your Name"
                builder.setView(input)

                builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    progressDialog?.show()
                    val name = input.text.toString()
                    val database = FirebaseDatabase.getInstance()
                    val users = database.getReference("users")
                    users.child(currentUser.uid).setValue(name)
                            .addOnCompleteListener {
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build()
                                currentUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener {
                                            startActivity(Intent(this, QueryActivity::class.java))
                                        }
                                progressDialog?.hide()
                            }
                })
                builder.setCancelable(false);

                builder.show()
            } else {
                startActivity(Intent(this, QueryActivity::class.java))
            }
        }
    }

    fun login(view: View) {
        progressDialog?.show()
        val emailTextView: TextView = findViewById(R.id.email)
        val email = emailTextView.text.toString()
        val passwordTextView: TextView = findViewById(R.id.password)
        val password = passwordTextView.text.toString()
        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth!!.currentUser
                        updateUI(user)
                    } else {
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                    progressDialog?.hide()
                }
    }
}
