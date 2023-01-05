package com.dei.app.arduinofirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class LoginActivity extends AppCompatActivity {

    private Button LoginButton;
    private EditText Email;
    private EditText Password;
    private AppCompatCheckBox checkbox;
    private TextView Registration;
    private TextView Title;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListner;

    private ProgressDialog loadingBar;

    private FirebaseUser currentUser;

    private int btntype = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        LoginButton = (Button) findViewById(R.id.login);
        Email = (EditText) findViewById(R.id.email);
        Password = (EditText) findViewById(R.id.password);
        checkbox = (AppCompatCheckBox) findViewById(R.id.checkbox);
        loadingBar = new ProgressDialog(this);
        Registration = (TextView) findViewById(R.id.register_text);
        Registration.setPaintFlags(Registration.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        Title = (TextView) findViewById(R.id.status_title);

        Registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Registration.setVisibility(View.INVISIBLE);
                LoginButton.setText("Register");
                Title.setText("Registration");
                btntype = 1;
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    // show password
                    Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    // hide password
                    Password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        firebaseAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if(currentUser != null)
                {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                if (btntype == 0) {
                    String email = Email.getText().toString();
                    String password = Password.getText().toString();

                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(LoginActivity.this, "Please write your Email...", Toast.LENGTH_SHORT).show();
                    }

                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(LoginActivity.this, "Please write your Password...", Toast.LENGTH_SHORT).show();
                    } else {
                        loadingBar.setTitle("Logging In");
                        loadingBar.setMessage("Please wait while we check your credentials.");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Sign In , Successful...", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();

                                    loadingBar.dismiss();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Error Occurred, while Signing In... ", Toast.LENGTH_SHORT).show();

                                    loadingBar.dismiss();
                                }
                            }
                        });
                    }
                } else {

                    final String email = Email.getText().toString();
                    String password = Password.getText().toString();

                    if(TextUtils.isEmpty(email))
                    {
                        Toast.makeText(LoginActivity.this, "Please write your Email...", Toast.LENGTH_SHORT).show();
                    }

                    if(TextUtils.isEmpty(password))
                    {
                        Toast.makeText(LoginActivity.this, "Please write your Password...", Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        loadingBar.setTitle("Registering User");
                        loadingBar.setMessage("Please wait while we create your account !");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                if(task.isSuccessful())
                                {

                                    Intent intent = new Intent(LoginActivity.this, DetailsActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    finish();

                                    loadingBar.dismiss();
                                }
                                else
                                {
                                    Toast.makeText(LoginActivity.this, "Please Try Again. Error Occurred, while registering... ", Toast.LENGTH_SHORT).show();

                                    loadingBar.dismiss();
                                }
                            }
                        });
                    }
                }
            }
        });

    }
}
