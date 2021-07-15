package com.themedimension.ivoryshop.android.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import com.themedimension.ivoryshop.android.R;

import java.util.Calendar;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

    }

    /*
      Method that creates and shows the date picker dialog.
       */
    public void showCalendar(Context context) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        final int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(context, android.R.style.Theme_Material_Light_Dialog, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
               BaseActivity.this.onDateSet(year, month, dayOfMonth);
            }
        }, year, month, day);
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        datePicker.show();
    }

    public String cardExpiryDate(int year, int month) {
        String expiryDate = "";
        month = month + 1;
        String monthString = String.valueOf(month);
        if (month < 10) {
            monthString = "0" + monthString;
        }
        expiryDate = monthString + "/" + year;
        return expiryDate;
    }

    public abstract void onDateSet(int year, int month, int dayOfMonth);

}
