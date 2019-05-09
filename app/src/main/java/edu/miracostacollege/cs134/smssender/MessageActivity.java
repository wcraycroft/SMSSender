package edu.miracostacollege.cs134.smssender;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.miracostacollege.cs134.smssender.model.Contact;
import edu.miracostacollege.cs134.smssender.model.ContactsAdapter;
import edu.miracostacollege.cs134.smssender.model.DBHelper;

public class MessageActivity extends AppCompatActivity {

    private ArrayList<Contact> contactsList;
    private ContactsAdapter contactsAdapter;
    private DBHelper db;
    private ListView contactsListView;
    private EditText messageEditText;
    private Button sendTextMessageButton;

    public static final int REQUEST_CONTACT = 101;
    public static final int REQUEST_SMS = 102;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        db = new DBHelper(this);
        contactsList = db.getAllContacts();
        contactsAdapter = new ContactsAdapter(this, R.layout.contact_list_item, contactsList);
        contactsListView = findViewById(R.id.contactsListView);
        contactsListView.setAdapter(contactsAdapter);

        messageEditText = findViewById(R.id.messageEditText);
        sendTextMessageButton = findViewById(R.id.sendTextMessageButton);
    }

    public void addContacts(View view) {
        // TODO: Start an activity for intent to pick a contact from the device.
        // Implicit intent to open Android contacts
        Intent contactsIntent = new Intent (Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

        // Determine the result you expect back from the activity
        startActivityForResult(contactsIntent, REQUEST_CONTACT);
    }


    // TODO: Overload (create) the onActivityResult() method, get the contactData,
    // TODO: resolve the content and create a new Contact object from the name and phone number.
    // TODO: Add the new contact to the database and the contactsAdapter.

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check to see if request matches REQUEST_CONTACT (101)
        if (requestCode == REQUEST_CONTACT)
        {
            if (resultCode == RESULT_OK)
            {
                Uri contactData = data.getData();
                Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                // Check to see if the cursor has any results (any contacts)
                if (cursor.moveToFirst())
                {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Contact newContact = new Contact(name, number);
                    // Add to Database
                    db.addContact(newContact);
                    // Add to list
                    contactsAdapter.add(newContact);

                }
            }
        }
    }

    public void deleteContact(View view) {
        // TODO: Delete the selected contact from the database and remove the contact from the contactsAdapter.
        Contact removingContact = (Contact) view.getTag();
        contactsAdapter.remove(removingContact);
        db.deleteContact(removingContact.getId());
    }

    public void sendTextMessage(View view) {

        // TODO: Get the default SmsManager, then send a text message to each of the contacts in the list.
        // TODO: Be sure to check for permissions to SEND_SMS and request permissions if necessary.

        SmsManager manager;

        // Check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            // Ask user for permission
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SEND_SMS}, REQUEST_SMS);
        }
        else
        {
            String message = messageEditText.getText().toString();
            manager = SmsManager.getDefault();
            String phoneNumber;
            for (Contact c : contactsList)
            {
                phoneNumber = c.getPhone();
                manager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(this, "Text message send to: " + c.getName(), Toast.LENGTH_LONG).show();
            }

        }
    }
}
