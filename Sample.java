package com.bbcsolution.smartagentsms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Sample extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

//        Intent i = new Intent(android.content.Intent.ACTION_VIEW);
//        i.putExtra("address", "5556; 5558; 5560");
//        // here i can send message to emulator 5556,5558,5560
//        // you can change in real device
//        i.putExtra("sms_body", "Hello my friends!");
//        i.setType("vnd.android-dir/mms-sms");
//        startActivity(i);
    }
}