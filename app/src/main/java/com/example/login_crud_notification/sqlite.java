package com.example.login_crud_notification;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class sqlite extends AppCompatActivity {

    private EditText editTextCharityName, editTextAddress, editTextPhone, editTextMessage;
    private Button btnSave, btnRead, btnUpdate, btnDelete, btnSendMessage, btnSendGroupMessage;
    private MultiAutoCompleteTextView multiAutoCompleteTextView;
    private SQLiteDatabase database;
    private ArrayList<String> phoneNumbers;
    private ArrayAdapter<String> adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charity_info);

        // Initialize views
        editTextCharityName = findViewById(R.id.editTextCharityName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextMessage = findViewById(R.id.editTextMessage);
        btnSave = findViewById(R.id.btnSave);
        btnRead = findViewById(R.id.btnRead);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        multiAutoCompleteTextView = findViewById(R.id.multiAutoCompleteTextView);
        btnSendGroupMessage = findViewById(R.id.btnSendGroupMessage);

        // Create or open the SQLite database
        database = openOrCreateDatabase("CharityDB", MODE_PRIVATE, null);

        // Create table if not exists
        database.execSQL("CREATE TABLE IF NOT EXISTS CharityTable (id INTEGER PRIMARY KEY AUTOINCREMENT, charity_name TEXT, address TEXT, phone_no TEXT)");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCharityInfo();
                loadPhoneNumbers();
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readCharityInfo();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCharityAddress();
                loadPhoneNumbers();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCharityInfo();
                loadPhoneNumbers();
            }
        });

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        btnSendGroupMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGroupMessage();
            }
        });

        phoneNumbers = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, phoneNumbers);
        multiAutoCompleteTextView.setAdapter(adapter);
        multiAutoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        // Load phone numbers initially
        loadPhoneNumbers();
    }

    private void saveCharityInfo() {
        String charityName = editTextCharityName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (charityName.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        } else {
            ContentValues values = new ContentValues();
            values.put("charity_name", charityName);
            values.put("address", address);
            values.put("phone_no", phone);

            long result = database.insert("CharityTable", null, values);
            if (result != -1) {
                Toast.makeText(this, "Charity information saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save charity information", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("Range")
    private void readCharityInfo() {
        Cursor cursor = database.rawQuery("SELECT * FROM CharityTable", null);
        if (cursor.moveToFirst()) {
            StringBuilder resultBuilder = new StringBuilder();
            do {
                String name = cursor.getString(cursor.getColumnIndex("charity_name"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String phone = cursor.getString(cursor.getColumnIndex("phone_no"));
                resultBuilder.append("Charity Name: ").append(name).append("\nAddress: ").append(address).append("\nPhone no: ").append(phone).append("\n\n");
            } while (cursor.moveToNext());

            // Show data in a dialog
            showDialog(resultBuilder.toString());
        } else {
            showDialog("No charities found in the database");
        }
        cursor.close();
    }

    private void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Charity Information");
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void updateCharityAddress() {
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put("address", address);

        int rowsAffected = database.update("CharityTable", values, "phone_no=?", new String[]{phone});
        if (rowsAffected > 0) {
            Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update address", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteCharityInfo() {
        String phone = editTextPhone.getText().toString().trim();

        int rowsDeleted = database.delete("CharityTable", "phone_no=?", new String[]{phone});
        if (rowsDeleted > 0) {
            Toast.makeText(this, "Charity information deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to delete charity information", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("Range")
    private void loadPhoneNumbers() {
        phoneNumbers.clear();
        Cursor cursor = database.rawQuery("SELECT phone_no FROM CharityTable", null);
        if (cursor.moveToFirst()) {
            do {
                phoneNumbers.add(cursor.getString(cursor.getColumnIndex("phone_no")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void sendMessage() {
        String phone = editTextPhone.getText().toString().trim();
        String message = editTextMessage.getText().toString().trim();

        if (phone.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please enter both phone number and message", Toast.LENGTH_SHORT).show();
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, message, null, null);
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendGroupMessage() {
        String message = editTextMessage.getText().toString().trim();
        Editable selectedContacts = multiAutoCompleteTextView.getText();
        String[] phones = selectedContacts.toString().split(",\\s*");

        if (phones.length > 0 && !message.isEmpty()) {
            for (String phone : phones) {
                Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + phone + "&text=" + message);
                Intent sendIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(sendIntent);
            }
        } else {
            Toast.makeText(this, "Please select at least one contact and enter a message", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database connection when the activity is destroyed
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
}
