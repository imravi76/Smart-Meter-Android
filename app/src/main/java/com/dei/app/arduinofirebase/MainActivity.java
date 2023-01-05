package com.dei.app.arduinofirebase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    ConstraintLayout rootLayout;
    boolean isDark = false;
    private FloatingActionButton night;
    private ImageView bgimage;
    private TextView Dash;

    private CircleImageView userImage;

    private TextView Name, temp, humid, consum, bill, led1, led2;
    private ToggleButton Led1, Led2;
    private CardView prediction, temp_card, humid_card, bill_card;
    private DatabaseReference mDatabase;
    private DatabaseReference lightDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        night = findViewById(R.id.night);
        rootLayout = findViewById(R.id.root_layout);
        bgimage = findViewById(R.id.imageView);
        Dash = findViewById(R.id.dash);
        userImage = findViewById(R.id.user_image);
        Name = findViewById(R.id.user_name);

        prediction = findViewById(R.id.consumption_card);
        temp_card = findViewById(R.id.temp_card);
        humid_card = findViewById(R.id.humid_card);
        bill_card = findViewById(R.id.bill_card);

        prediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CardActivity.class);
                startActivity(intent);
            }
        });

        temp = findViewById(R.id.temp);
        humid = findViewById(R.id.humid);
        consum = findViewById(R.id.consumption);
        bill = findViewById(R.id.bill);
        led1 = findViewById(R.id.led1_text);
        led2 = findViewById(R.id.led2_text);

        lightDatabase = FirebaseDatabase.getInstance().getReference();
        Led1 = findViewById(R.id.led1);
        Led1.setChecked(false);
        Led2 = findViewById(R.id.led2);
        Led2.setChecked(false);

        Led1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {
                    // The toggle is enabled
                    lightDatabase.child("led1").setValue(1);
                } else {
                    // The toggle is disabled
                    lightDatabase.child("led1").setValue(0);
                }
            }
        });

        Led2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {
                    // The toggle is enabled
                    lightDatabase.child("led2").setValue(1);
                } else {
                    // The toggle is disabled
                    lightDatabase.child("led2").setValue(0);
                }
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });

        night.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDark = !isDark ;
                if (isDark) {

                    rootLayout.setBackgroundColor(getResources().getColor(R.color.black));
                    Dash.setTextColor(getResources().getColor(R.color.white));
                    led1.setTextColor(getResources().getColor(R.color.white));
                    led2.setTextColor(getResources().getColor(R.color.white));
                    bgimage.setVisibility(View.GONE);
                    night.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_day));

                }
                else {
                    rootLayout.setBackgroundColor(getResources().getColor(R.color.white));
                    Dash.setTextColor(getResources().getColor(R.color.black));
                    led1.setTextColor(getResources().getColor(R.color.black));
                    led2.setTextColor(getResources().getColor(R.color.black));
                    bgimage.setVisibility(View.VISIBLE);
                    night.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_night));
                }

                saveThemeStatePref(isDark);
            }
        });

        isDark = getThemeStatePref();
        if(isDark) {
            // dark theme is on
            rootLayout.setBackgroundColor(getResources().getColor(R.color.black));
            Dash.setTextColor(getResources().getColor(R.color.white));
            led1.setTextColor(getResources().getColor(R.color.white));
            led2.setTextColor(getResources().getColor(R.color.white));
            bgimage.setVisibility(View.GONE);
            night.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_day));

        }
        else
        {
            // light theme is on
            rootLayout.setBackgroundColor(getResources().getColor(R.color.white));
            Dash.setTextColor(getResources().getColor(R.color.black));
            led1.setTextColor(getResources().getColor(R.color.black));
            led2.setTextColor(getResources().getColor(R.color.black));
            bgimage.setVisibility(View.VISIBLE);
            night.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_night));

        }

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String Temp = dataSnapshot.child("Temperature").getValue().toString();
                String Humid = dataSnapshot.child("Humidity").getValue().toString();
                String Consum = dataSnapshot.child("Consumption").getValue().toString();
                String next = dataSnapshot.child("Tomorrow's Prediction").getValue().toString();
                String Bill = dataSnapshot.child("Bill_Amount").getValue().toString();

                temp.setText("Temperature \n" +Temp+"°C");
                humid.setText("Humidity \n" +Humid+"%");
                consum.setText("Consumption \n" +Consum+"Wh");
                bill.setText("Bill \n₹" +Bill);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (mCurrentUser != null) {

            mDatabase.child("Users").child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("image_thumb").getValue().toString();

                    Name.setText("Hi, " + name);
                    if (!image.equals("default")) {

                        Picasso.get().load(image).placeholder(R.drawable.profile_round).into(userImage);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void saveThemeStatePref(boolean isDark) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPref",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isDark",isDark);
        editor.commit();
    }

    private boolean getThemeStatePref () {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPref",MODE_PRIVATE);
        boolean isDark = pref.getBoolean("isDark",false) ;
        return isDark;

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            sendToLogin();

        }

    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, SplashActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

}
