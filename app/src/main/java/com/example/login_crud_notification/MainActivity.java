package com.example.login_crud_notification;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText email, pass;
    private Button btnReg, btnLogin;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static final String CHANNEL_ID = "user_auth_notifications";
    private static final int NOTIFICATION_ID = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.editText1);
        pass = findViewById(R.id.editText2);
        btnReg = findViewById(R.id.button2);
        btnLogin = findViewById(R.id.buttonLogin);
        progressDialog = new ProgressDialog(this);

        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();

        createNotificationChannel();

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userRegistration();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });
    }

    private void userRegistration() {
        String enteredEmail = email.getText().toString().trim();
        String enteredPassword = pass.getText().toString().trim();

        if (TextUtils.isEmpty(enteredEmail)) {
            showNotification("Registration Failed", "Please enter email");
            return;
        }
        if (TextUtils.isEmpty(enteredPassword)) {
            showNotification("Registration Failed", "Please enter password");
            return;
        }

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(enteredEmail, enteredPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            showNotification("Registration Successful", "You have successfully registered");
                        } else {
                            showNotification("Registration Failed", "Error while registering");
                        }
                    }
                });
    }

    private void userLogin() {
        String enteredEmail = email.getText().toString().trim();
        String enteredPassword = pass.getText().toString().trim();

        if (TextUtils.isEmpty(enteredEmail)) {
            showNotification("Login Failed", "Please enter email");
            return;
        }
        if (TextUtils.isEmpty(enteredPassword)) {
            showNotification("Login Failed", "Please enter password");
            return;
        }

        progressDialog.setMessage("Logging in Please Wait...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            showNotification("Login Successful", "Welcome back!");
                            Intent sqlite=new Intent(MainActivity.this,sqlite.class);
                            startActivity(sqlite);
                        } else {
                            showNotification("Login Failed", "Authentication failed. Please try again.");
                        }
                    }
                });
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.kakashi)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "User Authentication Notifications";
            String description = "Notifications for user authentication status";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
