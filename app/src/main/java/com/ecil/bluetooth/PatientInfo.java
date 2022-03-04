package com.ecil.bluetooth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.R.color;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class PatientInfo extends MainActivity implements android.view.View.OnClickListener {
    Button save, clear, cancel;
    public static EditText name, age, chno, medi, BP, dob, txtComments;
    public static Spinner gender, ht, wt;
    public static ArrayAdapter<String> dataAdapter_gen;
    public static ArrayAdapter<Number> dataAdapter_ht, dataAdapter_wt;
    public static TextView date;
    public static String sname, sage, sgen, schno, date1, smedi, sBP, sht, swt, sdob, sComments;
    public static String sdf, sdf1, acq_date;
    public static boolean data_entered = false;
    boolean data_edited = false;
    public static int gen_position, ht_position, wt_position;
    PaintView paintV;
    String pos;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        data_entered = false;
        data_edited = false;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        //setting background and text color for title
//        EventBus.getDefault().register(PatientInfo.this);
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = findViewById(actionBarTitleId);
            if (title != null)
                title.setTextColor(Color.RED);
        }
        ActionBar ab = getActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(color.white));

        setContentView(R.layout.activity_generate_report_linear);
        setTitleColor(Color.BLACK);
        setTitle("Patient Details");
        //initialization of GUI of patient details
        save = findViewById(R.id.btnSave);
        save.setOnClickListener(this);
        clear = findViewById(R.id.btnClear);
        clear.setOnClickListener(this);
        cancel = findViewById(R.id.btnCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.gen_report_menu.setVisible(true);
                //PatientInfo.this.finish();
                EventBus.getDefault().post(new MessageEvent(MessageEvent.SHOW_HOME_SCREEN));
            }
        });
        chno = (EditText) findViewById(R.id.chss_no);
        dob = (EditText) findViewById(R.id.DOB);
        name = (EditText) findViewById(R.id.txtName);
        age = (EditText) findViewById(R.id.txtAge);
        date = (TextView) findViewById(R.id.date);
        medi = (EditText) findViewById(R.id.txtMedication);
        txtComments = findViewById(R.id.txtComments);
        BP = (EditText) findViewById(R.id.txtBP);
        gender = (Spinner) findViewById(R.id.spinner_gender);//drop down for gender
        ht = (Spinner) findViewById(R.id.spinner_height);//drop down for height
        wt = (Spinner) findViewById(R.id.spinner_weight);//drop down for weight


        //adding gender to list
        List<String> list = new ArrayList<String>();
        list.add("Male");
        list.add("Female");
        list.add("Transgender");
        dataAdapter_gen = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter_gen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(dataAdapter_gen);

        //adding height to dropdown list
        List<Number> list_ht = new ArrayList<Number>();
        for (int i = 1; i <= 255; i++)
            list_ht.add(i);

        dataAdapter_ht = new ArrayAdapter<Number>(this, android.R.layout.simple_spinner_item, list_ht);
        dataAdapter_ht.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ht.setAdapter(dataAdapter_ht);
        ht.setSelection(149);

        //adding weight to dropdown list
        List<Number> list_wt = new ArrayList<Number>();
        for (int i = 1; i <= 255; i++)
            list_wt.add(i);

        dataAdapter_wt = new ArrayAdapter<Number>(this, android.R.layout.simple_spinner_item, list_wt);
        dataAdapter_wt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wt.setAdapter(dataAdapter_wt);
        wt.setSelection(74);

        //date

        //date.setText(sdf);
        if (Filechooser.file_loaded == false) {
            sdf1 = sdf;
        }
        //       cancel.setEnabled(false);//disables cancel button
        //   }
        set_data();
        Filechooser.file_loaded=false;
        name.addTextChangedListener(tw);
        age.addTextChangedListener(tw);
        chno.addTextChangedListener(tw);
        medi.addTextChangedListener(tw);
        BP.addTextChangedListener(tw);
        //dob.addTextChangedListener();
        new DateInputMask(dob);
        txtComments.addTextChangedListener(tw);

    }

    TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            data_edited = true;
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        Toast.makeText(PatientInfo.this,"Resuming ..",Toast.LENGTH_SHORT).show();
        PatientInfo.this.finish();
        Intent main = new Intent(getApplicationContext(), MainActivity.class);
        //   main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(main);
        //EventBus.getDefault().post(new MessageEvent(MessageEvent.SHOW_HOME_SCREEN));
    }

    @Override
    protected void onStop() {
        super.onStop();
        //saves selected gain in menu option
    }

    public static void clearVariables() {
        //   sname= sage=sgen=schno= date1= smedi= sBP= sht= swt= sdob= sComments=  sdf1= "";
        //sdf

        sname = sage = sgen = schno = date1 = smedi = sBP = sht = swt = sdob = sComments = sdf = sdf1 = acq_date = "";
    }

    public static void setDateTime(){
        Date now = new Date();
        sdf = new SimpleDateFormat("yyyy_MM_dd HH-mm").format(now);
        acq_date = new SimpleDateFormat("dd/MM/yyyy  HH-mm").format(now);

    }

    private void set_data() {
        try {
            //display text on text box
            date.setText(sdf1.trim());
            name.setText(sname.trim());
            chno.setText(schno.trim());
            dob.setText(sdob.trim());
            age.setText(sage.trim());

            //adding gender
            if (sgen.contains("Female"))
                gender.setSelection(1);
            else if (sgen.contains("Male"))
                gender.setSelection(0);
            else if (sgen.contains("Transgender"))
                gender.setSelection(2);

            //set the previous selected height
            for (int i = 1; i <= 255; i++) {
                pos = String.valueOf(i);
                if (sht.equals(pos)) {
                    ht.setSelection(i - 1);
                }
            }

            //set the previous selected weight
            for (int i = 1; i <= 255; i++) {
                pos = String.valueOf(i);
                if (swt.equals(pos)) {
                    wt.setSelection(i - 1);
                }
            }
            medi.setText(smedi.trim());
            BP.setText(sBP.trim());
            txtComments.setText(sComments.trim());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("set data.." + e.getMessage());
        }
    }


    public void onClick(View v) {
        if (v.getId() == R.id.btnSave) {
            data_entered = true;
            sdf = date.getText().toString();
            sname = name.getText().toString().trim();
            schno = chno.getText().toString().trim();
            sdob = dob.getText().toString().trim();
            sage = age.getText().toString().trim();
            sgen = gender.getSelectedItem().toString().trim();
            sht = ht.getSelectedItem().toString().trim();
            swt = wt.getSelectedItem().toString().trim();
            smedi = medi.getText().toString().trim();
            sBP = BP.getText().toString().trim();
            sComments = txtComments.getText().toString().trim();
            //validation for GUI ?? SNB: Its commented.. CAn I delete?
			/*if(schno.isEmpty())
					chno.setError("Enter Id number");
			else if(sname.isEmpty())
					name.setError("Enter name");
			else if( sage.isEmpty())
					age.setError("Enter age");
			else if(Integer.parseInt(sage)>100)
					age.setError("Age should be less than 100..");
			else if(smedi.isEmpty())
					medi.setError("Enter medication");
			else if(sBP.isEmpty())
					BP.setError("Enter Blood Pressure");
			else if(sdob.isEmpty())
					dob.setError("enter date of birth");
			else
			{*/
            //calls the constructor of paintView
         //   if (data_edited) {
                MainActivity.gen_report_menu.setVisible(true);

                if(sdf==null || sdf.trim().isEmpty()){
                    setDateTime();
                    if(sdf==null || sdf.trim().isEmpty()){
                        sdf = new SimpleDateFormat("yyyy_MM_dd HH-mm").format( new Date());
                    }
                }

                paintV = new PaintView(sdf, sname, schno, sdob, sage, sgen, sht, swt, smedi, sBP, sComments, PatientInfo.this);
                EventBus.getDefault().post(new CallBackEvents(CallBackEvents.GENERATE_REPORT));
         //   } else {
         //       data_entered = false;
         //       PatientInfo.this.finish();
         //   }
            //PatientInfo.this.finish();
/*            Intent returnIntent = new Intent();
            setResult(ACTIVITY_PATIENT_INFO_RESULT_SAVE,returnIntent);
            Log.d("ACTIVITY_PATIENT_INFO","Result : "+ACTIVITY_PATIENT_INFO_RESULT_SAVE);
            PatientInfo.this.finish();*/
            //}
            //  Toast.makeText(getApplicationContext(), "Data saved", Toast.LENGTH_SHORT).show();
        }
        if (v.getId() == R.id.btnClear)
            Clear_alert();
        //if(v.getId()==R.id.btnCancel)
        //PatientInfo.this.finish();
    }

    //clear data and sets default data
    public void Clear_alert() {
        AlertDialog.Builder build = new AlertDialog.Builder(PatientInfo.this);
        build.setMessage("Do you want to clear the entered details?");
        build.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                name.setText("");
                age.setText("");
                gender.setSelection(0);
                medi.setText("");
                BP.setText("");
                chno.setText("");
                dob.setText("");
                ht.setSelection(149);
                wt.setSelection(74);
                txtComments.setText("");
            }
        });

        build.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();
            }
        });
        AlertDialog alert = build.create();
        alert.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void comCallBackEvents(ActivityCallBackEvents event) {
        switch (event.getEventType()) {
            case CallBackEvents.FINISH_PATIENTINFO_ACTIVITY:
                PatientInfo.this.finish();
                break;
        }
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("PatientInfoSaved", ACTIVITY_PATIENT_INFO_RESULT_SAVE);
        setResult(RESULT_OK, returnIntent);
        Log.d("ACTIVITY_PATIENT_INFO", "Result : " + ACTIVITY_PATIENT_INFO_RESULT_SAVE);
        super.finish();
    }


}
