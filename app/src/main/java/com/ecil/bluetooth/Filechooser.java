package com.ecil.bluetooth;

import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListAdapter;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

public class Filechooser extends Activity {

    // Stores names of traversed directories
    ArrayList<String> str = new ArrayList<String>();
    // Check if the first level of the directory structure is the one showing
    private Item[] fileList;
    private File path = new File(Environment.getExternalStorageDirectory() + "/TELE-ECG", "TELE-ECG Reports");
    private String chosenFile;
    private static final int DIALOG_LOAD_FILE = 1000;
    public static File sel;
    public static boolean file_loaded = false;
    public AlertDialog.Builder builder;
    Dialog dialog = null;
    int iBackPress = 0;
    ListAdapter adapter;
    public boolean BACK = false;
    private String[] mFileList;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Patient Info...");
        loadFileList();
        if (mFileList == null || mFileList.length == 0) {
            showAlert("No Reports Available", "Report");
        } else {
            showDialog(DIALOG_LOAD_FILE);
        }
    }

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        try {
            int counter = 0;
            if (path.exists()) {
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        File sel = new File(dir, filename);
                        return sel.getName().endsWith(".dat") || sel.isDirectory();
                    }
                };
                mFileList = path.list(filter);
                fileList = new Item[counter];
                for (int j = 0; j < mFileList.length; j++) {
                    if (mFileList[j].endsWith(".dat") == true) {
                        fileList[counter] = new Item(mFileList[j]);
                        counter++;
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "path does not exist..\nplease create new file first..", Toast.LENGTH_SHORT).show();
                mFileList = new String[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Item {
        public String file;

        public Item(String file) {
            this.file = file;
        }

        @Override
        public String toString() {
            return file;
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        switch (id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose .dat file to load data.");
                if (mFileList == null) {
                    dialog = builder.create();
                    return dialog;
                }

                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    @SuppressWarnings("deprecation")
                    public void onClick(DialogInterface dialog, int which) {
                        chosenFile = mFileList[which];
                        sel = new File(path + "/" + chosenFile);
                        if (sel.isDirectory()) {
                            // Adds chosen directory to list
                            str.add(chosenFile);
                            fileList = null;
                            path = new File(sel + "");
                            loadFileList();
                            removeDialog(DIALOG_LOAD_FILE);
                            showDialog(DIALOG_LOAD_FILE);
                        } else {
                            if (sel.getName().endsWith(".dat") == false) {
                                Toast.makeText(getApplicationContext(), "Select .dat file", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            } else {
                                // Perform action with file picked
                                iBackPress = 1;
                                new loadingFilesTask().execute();
                            }
                        }
                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }

    //Read data from selected file and display on screen
    private void read_data(File sel) {
        try {
            file_loaded = true;
            RandomAccessFile raf = new RandomAccessFile(sel, "rw");
            raf.readUTF();//title
            PatientInfo.sdf1 = raf.readUTF();//date
            PatientInfo.schno = raf.readUTF();//chssno
            PatientInfo.sname = raf.readUTF();//name
            PatientInfo.sdob = raf.readUTF();//dob//
            PatientInfo.sage = raf.readUTF();//age//
            PatientInfo.sgen = raf.readUTF();//gende
            PatientInfo.sht = raf.readUTF();//height
            PatientInfo.swt = raf.readUTF();//weight
            PatientInfo.smedi = raf.readUTF();//medi
            PatientInfo.sBP = raf.readUTF();//bp
            PatientInfo.sComments = raf.readUTF();//bp
            MainActivity.iGain = raf.readInt();
            ;//gain
            PaintView.filter_state = raf.readInt();//filter
            raf.seek(1023);

            for (int j = 0; j < 12; j++) {
                for (int k = 0; k < PaintView.total_samples; k++) {
                    PaintView.raw_data[j][k] = (short) raf.readInt();
                    PaintView.Gen_report[j][k] = PaintView.raw_data[j][k];
                }
            }
            raf.close();//SNB: CONFIRM
            PaintView.START_GUI = true;
            MainActivity.refresh_data = true;
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            Filechooser.this.finish();

            //calls patient info class if any changes in the patient details to be done
            //   Intent i = new Intent(getApplicationContext(), PatientInfo.class);
            //   startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in Noo of text lines: " + e.getMessage());
        }
    }

    //exit the application on pressing of back button
    @Override
    public void onBackPressed() {
        BACK = true;
        Filechooser.this.finish();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    protected class loadingFilesTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(Filechooser.this);
            dialog.setMessage("Loading File");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            read_data(sel);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            } catch (Exception e) {

            }
            EventBus.getDefault().post(new ActivityCallBackEvents(ActivityCallBackEvents.FINISH_PATIENTINFO_ACTIVITY));
        }
    }

    public void showAlert(String msg, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Filechooser.this);
        builder.setMessage(msg);
        builder.setTitle(title);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                //   EventBus.getDefault().post(new MessageEvent(MessageEvent.SHOW_HOME_SCREEN));
                dialogInterface.dismiss();
                Filechooser.this.finish();

            }
        });
        builder.create().show();

    }

}

