package com.ecil.bluetooth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import org.greenrobot.eventbus.EventBus;

@SuppressLint("HandlerLeak") public class BluetoothConnect extends MainActivity implements OnClickListener,
        OnItemClickListener
{
    private static final UUID 	MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ListView list;
    Button btnScan;
    BluetoothAdapter bAdapter;
    BroadcastReceiver receiver;
    IntentFilter filter;
    private ArrayAdapter<String> listAdapter;
    ArrayList<BluetoothDevice> devices;
    public static String device_address, deviceName;
    public static BluetoothDevice selectedDevice;
    private static final int 	REQUEST_ENABLE_BT 	= 0;
    protected static final int 	SUCCESS_CONNECT 	= 0;
    protected static final int 	SEND_INSTREAM		= 1;
    protected static final int 	SEND_OUTSTREAM 		= 2;
    protected static final int  CONNECTION_ERROR_GENERAL = 3;
    protected static final int  MESSAGE_COMMUNICATION=4;
    protected static final int  RECONNECT			= 5;
    protected static final int  CONNECTION_ERROR_DEVICE	= 6;
    private static int 			DEVICE_FOUND 		= 0;
    //private String sDeviceAddress;

    int BLUETOOTH_ADD_LENGTH = 17;
    InputStream instm;
    OutputStream outstm;
    private boolean REGISTER = false;
    boolean deviceselected = false;
    BluetoothDevice device1;
    BluetoothSocket mBTSocket   = null;
    MainActivity m;
    PaintView pv;
    CreateReport cr;
    Display	d;
    int iScreenWidth=0;
    boolean bFileRead = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);
        Log.i(TAG, "entering OnCreate of Bluetooth \\n");
        //initialization of GUI
        list=(ListView) findViewById(R.id.list);
        btnScan=(Button) findViewById(R.id.btnscan);
        listAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,0);
        devices = new ArrayList<BluetoothDevice>();
        list.setAdapter(listAdapter);
        list.setOnItemClickListener(this);
        btnScan.setOnClickListener(this);
        bAdapter=BluetoothAdapter.getDefaultAdapter();
        checkBluetoothStatus();

        if(bFileRead==false)
            bFileRead = read_bluetooth_address();	//this will read the file and save address in "device_address" else will be blank.
        Log.i(TAG, "Done with OnCreate of Bluetooth \\n");
    }

    //function to check initial Bluetooth status
    public void checkBluetoothStatus()
    {
        try
        {
            if(bAdapter==null)
                Toast.makeText(getApplicationContext(), "NO BLUETOOTH DEVICE DETECTED!", Toast.LENGTH_SHORT).show();

            if(bAdapter.isEnabled())
            {
                System.out.println("checkBluetoothStatus");
                doDiscovery();
                init();
            }
            else
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        catch(Exception e)
        {
            System.out.println("bt status>"+e.getMessage());
        }
    }

    private void init()
    {
        try
        {
            filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
            receiver=new BroadcastReceiver()
            {
                @SuppressWarnings("static-access")
                @Override
                public void onReceive(Context arg0, Intent intent)
                {
                    String action=intent.getAction();
                    String deviceNameRecieved;

                    //read_bluetooth_address();//Moved to onCreate
                    if(BluetoothDevice.ACTION_FOUND.equals(action))
                    {
                        setProgressBarVisibility(false);

                        device1 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        deviceNameRecieved = device1.getName();
                        if(listAdapter.isEmpty())
                        {
                            System.out.println("empty");
                            //Populate only those devices which are TeleECG devices
                            if(deviceNameRecieved!=null && deviceNameRecieved.startsWith("TeleECG"))
                            {
                                //if device is found then flag DEVICE_FOUND is set 1
                                DEVICE_FOUND = 1;
                                deviceName = deviceNameRecieved;
                              //  if(device_address.equals("") || device_address.equals(null))// there is no entry in the bluetooth file, so add the device to list.
                                {
                                    listAdapter.add(deviceName);
                                }
                                if(device1.getAddress().toString().equals(device_address))
                                {
                                    bAdapter.cancelDiscovery();
                                    ConnectThread connect=new ConnectThread(device1);
                                    connect.start();
                                }
                                devices.add(device1);
                            }
                            else
                            {
                                //do nothing.. continue with search
                            }
                        }
                        else
                        {
                            for(int a=0;a<list.getCount();a++)
                            {
                                //if not exist in list then add in list
                                if(deviceNameRecieved!=null&&deviceNameRecieved.startsWith("TeleECG") && !deviceNameRecieved.equals(list.getItemAtPosition(a).toString()))//Make sure only one TeleECG machine is in at a time. If two devices are present their name has to be different.
                                {
                                    deviceName = deviceNameRecieved;
                                    //if(device1.getAddress().toString().equals(device_address) && a==0)	//a=0 in list is the last element added... List is Last IN First Out
                                    if(device1.getAddress().toString().equals(device_address) )	//a=0 in list is the last element added... List is Last IN First Out
                                    {
                                        bAdapter.cancelDiscovery();
                                        ConnectThread connect=new ConnectThread(device1);
                                        connect.start();
                                    }
                                    else
                                    {
                                        listAdapter.add(deviceName);
                                    }
                                    devices.add(device1);
                                    System.out.println("add device");
                                    break;
                                }
                            }
                        }
                        //devices.add(device1);
                        setTitleColor(Color.BLACK);
                        setTitle("Select TeleECG Device...");
                    }
                    else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
                    {


                        setProgressBarVisibility(true);
                    }
                    else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                    {
                        EventBus.getDefault().post(new MessageEvent("","Discovering TeleECG Devices..",MessageEvent.PROGRESS_DIALOG_STOP));

                        setProgressBarVisibility(false);
                        setProgressBarIndeterminateVisibility(false);
                        setTitleColor(Color.BLACK);
                        setTitle("select Device");
                        if (DEVICE_FOUND == 0)
                        {
                            setTitle("No TeleECG Devices Found.");
                            //listAdapter.add("No TeleECG Devices Found. Check If the Device is Off.");
                            Toast.makeText(getApplicationContext(), "No TeleECG Devices Found. Check If the Device is Off.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
                    {
                        //new added
                        setProgressBarVisibility(false);
                        if (bAdapter.getState() == bAdapter.STATE_OFF)
                        {
                            Toast.makeText(getApplicationContext(), "Bluetooth connectivity problem", Toast.LENGTH_LONG).show();// changed to getApplication context to maintain uniformity in display
                            EventBus.getDefault().post(new MessageEvent(MessageEvent.SHOW_HOME_SCREEN));
/*                            Intent enableBtIntent = new Intent(
                                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent,
                                    REQUEST_ENABLE_BT);
                            doDiscovery();
                            init();*/
                        }
                    }
                }
            };
            registerReceiver(receiver, filter);

            filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            registerReceiver(receiver, filter);

            filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);

            filter=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(receiver, filter);

            REGISTER = true;

        } catch (Exception e)
        {
            System.out.println("error in bluetooth init"+e.getMessage());
            Toast.makeText(getApplicationContext(), "Bluetooth init error!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (REGISTER) {
            unregisterReceiver(receiver);
            REGISTER = false;
        }
    }


    private boolean read_bluetooth_address()
    {
        File f = new File(settingsFolder,"bluetooth_address.bin");
        InputStream fin = null;
        char[] array = new char[100];
        int i = 0;

        try
        {
            Log.i(TAG, "Entering read_bluetooth_address() \\n");
            device_address = "";
            if (!f.exists())
            {
                System.out.println("No previous Bluetooth connetion to TELE-ECG device found!");
                //Toast.makeText(getApplicationContext(), "No previous Bluetooth coonetion to TELE-ECG device found!", Toast.LENGTH_SHORT).show();
                return true;
            }
            else
            {
                fin = new FileInputStream(f);
                while (fin.available() > 0)
                {
                    array[i] = (char) fin.read();
                    i++;
                }
                if(i>0)
                {
                    device_address = String.valueOf(array, 0, BLUETOOTH_ADD_LENGTH);
                    System.out.println("device_address read="+device_address);
                }
                fin.close();
            }
            Log.i(TAG, "device_address found: " + device_address + "\\n");
        } catch (IOException ex) {
            System.out.println("func read_bluetooth_address ="+ex.getMessage());
        }
        return true;
    }


    private void doDiscovery()
    {
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitleColor(Color.BLACK);
        setTitle("Scanning Bluetooth Devices...");

        // If we're already discovering, stop it
        if (bAdapter.isDiscovering()) {
            bAdapter.cancelDiscovery();
        }
        EventBus.getDefault().post(new MessageEvent("","Discovering TeleECG Devices..",MessageEvent.PROGRESS_DIALOG_START));
        System.out.println("doDiscovery");
        // Request discover from BluetoothAdapter
        bAdapter.startDiscovery();
        Toast.makeText(getApplicationContext(), "Discovering TeleECG...", Toast.LENGTH_LONG).show();// changed to getApplication context to maintain uniformity in display
        //Toast.makeText(this, "Discovering TeleECG...",Toast.LENGTH_LONG).show();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set device discovery
                    doDiscovery();
                    init();
                    System.out.println("REQUEST_ENABLE_BT");

                } else {
                    // User did not enable Bluetooth or an error occurred
                    System.out.println("bt should be on");
                    Toast.makeText(this, "BLUETOOTH SHOULD BE ENABLED",
                            Toast.LENGTH_LONG).show();
                    //System.exit(0);
                  //  this.finish();
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.SHOW_HOME_SCREEN));
                }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inf=getMenuInflater();
        inf.inflate(R.menu.main, menu);
        //get the positions of menu item
        prev_menu=menu.getItem(0);
        start_menu=menu.getItem(1);
        stop_menu=menu.getItem(2);
        next_menu=menu.getItem(3);
        mode_menu=menu.getItem(4);
        filter_menu=menu.getItem(5);
        gain_menu=menu.getItem(6);
        gen_report_menu=menu.getItem(7);
        acq_mode=menu.getItem(8);
        add_patient_info=menu.getItem(9);
        load_menu=menu.getItem(10);
        send_menu=menu.getItem(11);
        back_to_main_menu=menu.getItem(12);
        new_patient=menu.getItem(13);
        prev_lead=menu.getItem(14);
        next_lead=menu.getItem(15);
        repeat_acq=menu.getItem(16);

        //change visibility of menu item
        filter_menu.setVisible(true);
        stop_menu.setVisible(false);
        mode_menu.setVisible(true);
        gen_report_menu.setVisible(false);
        send_menu.setVisible(false);
        gain_menu.setVisible(true);
        load_menu.setVisible(false);
        add_patient_info.setVisible(false);
        prev_menu.setVisible(false);
        next_menu.setVisible(false);
        new_patient.setVisible(false);
        next_lead.setVisible(false);
        prev_lead.setVisible(false);
        repeat_acq.setVisible(false);

        //hides the menu during connectivity
        if(mState=="HIDE_MENU")
        {
            for(int j=0;j<menu.size();j++)
                menu.getItem(j).setVisible(false);

            back_to_main_menu.setVisible(true);
        }
        if(mState=="UNHINDE_MENU")
        {
            //demo_menu.setVisible(false);
        }

        if(load_existing_file==true)
            send_menu.setVisible(false);
        else if(load_existing_file==false && send_report==true)
            send_menu.setVisible(true);
        if(no_of_lead_to_display==12)
            acq_mode.setVisible(false);

        MainActivity.read_filter_gain();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3)
    {
        System.out.println("on item click");
        connect_device(arg2);
    }

    //this function is used for pairing with the device
    public void connect_device(int arg2)
    {
        EventBus.getDefault().post(new MessageEvent("","Connecting TeleECG Device..",MessageEvent.PROGRESS_DIALOG_START));
        deviceselected=true;
        if(bAdapter.isDiscovering()) {
            bAdapter.cancelDiscovery();
        }
        try
        {
            selectedDevice=devices.get(arg2);
            if (!BluetoothAdapter.checkBluetoothAddress(devices.get(arg2).getAddress()))
            {
                System.out.println("BT not valid");
                Toast.makeText(getApplicationContext(), "Not a valid Bluetooth Address..Select another bluetooth device OR Configure again!!",Toast.LENGTH_SHORT).show();
            }
            else
            {
                System.out.println("selectedDevice.getName()" + selectedDevice.getName());
                System.out.println("bAdapter.getBondedDevices()" + bAdapter.getBondedDevices());
                if(bAdapter.getBondedDevices().contains(selectedDevice.getName()))//device_address))//device is already paired, directly start the thread.
                {
                    System.out.println("paired device available, directly connect to device");
                    ConnectThread connect=new ConnectThread(selectedDevice);
                    connect.start();
                }
                else
                {
                    System.out.println("call pair device");
                    pairDevice(selectedDevice);
                }
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public final Handler Bt_mHandler=new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case SUCCESS_CONNECT:
                    try
                    {
                        EventBus.getDefault().post(new MessageEvent("","Successfully Connected ",MessageEvent.PROGRESS_DIALOG_STOP));
                        System.out.println("Device Address to Store: " + device_address);
                        store_bluetooth_address(device_address);
                        Toast.makeText(getApplicationContext(), "Successfully Connected to " + deviceName, Toast.LENGTH_SHORT).show();
                        System.out.println("SUCCESS_CONNECT");
                        ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
                        connectedThread.start();
                    }
                    catch (Exception e1) {
                        Toast.makeText(getApplicationContext(), "Error after connecting to Device through bluetooth", Toast.LENGTH_SHORT).show();
                        EventBus.getDefault().post(new MessageEvent("","Error  Connecting ",MessageEvent.PROGRESS_DIALOG_STOP));
                        System.out.println("Error161=" + e1.getMessage());
                    }
                    break;
                case SEND_INSTREAM:
                    instrm=(InputStream)msg.obj;
                    System.out.println("SEND_INSTREAM="+instrm.toString());
                    break;
                case SEND_OUTSTREAM:
                    outstrm=(OutputStream)msg.obj;
                    System.out.println("SEND_OUTSTREAM="+outstrm.toString());
                    mState="UNHINDE_MENU";
                    //supportInvalidateOptionsMenu();//AK: for APK 8 and below
                    invalidateOptionsMenu(); //currently set to 11
                    initialization_paintView();//call the method from main activity for creating canvas
                    start_menu.setVisible(true);
                    send_menu.setVisible(false);
                    break;
                case CONNECTION_ERROR_DEVICE:
                 //   Toast.makeText(getApplicationContext(), "Bluetooth connection to TeleECG device failed.", Toast.LENGTH_SHORT).show();
                    break;
                case CONNECTION_ERROR_GENERAL:
                    Toast.makeText(getApplicationContext(), "Selected Device is not a TeleECG device.", Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_COMMUNICATION:
                    AlertDialog.Builder build2 = new AlertDialog.Builder(
                            BluetoothConnect.this);
                    build2.setTitle("Communication Error");
                    build2.setMessage("Communication Error! \n Try Again!");
                    build2.setCancelable(false);
                    build2.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int arg1) {
                                    System.exit(0);
                                }
                            });
                    AlertDialog alert2 = build2.create();
                    alert2.show();
                    break;
            }
        }
    };

    //displays the pairing window for connectivity
    public void pairDevice(BluetoothDevice device)
    {
        try
        {
            bAdapter.cancelDiscovery();
            //Paring the selected device
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            device_address=device.getAddress().toString();
            System.out.println("pairing device");
            ConnectThread connect=new ConnectThread(device);
            connect.start();
        } catch (Exception e)
        {
            System.out.println("ERROR PAIR DEVICE "+e.getMessage());
        }
    }

    //store the mac address of the device on pairing for first time and then connects automatically when found for next time
    private void store_bluetooth_address(String bt_address) {
        File f = new File(settingsFolder,"bluetooth_address.bin");
        OutputStream osw = null;
        try
        {
            if (!f.exists()) {
                f.createNewFile();
            }
            osw = new FileOutputStream(f);
            osw.write(bt_address.getBytes());
            System.out.println("Bytes Written:" + bt_address.getBytes().length);
            osw.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    // unpair the device
    public void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            System.out.println("Eroor6>" + e.getMessage());
        }
    }
    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        public ConnectThread(BluetoothDevice device)
        {
            // Use a temporary object that is later assigned to mmSocket, because mmSocket is final
            BluetoothSocket tmp = null;
            try
            {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);

                System.out.println("ConnectThread");
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), "Socket error: Constructor error", Toast.LENGTH_SHORT).show();
                System.out.println("ERR!="+e.getMessage());
            }
            mmSocket = tmp;
        }

        public void run()
        {
            // Cancel discovery because it will slow down the connection
            EventBus.getDefault().post(new MessageEvent("","Connecting TeleECG Device..",MessageEvent.PROGRESS_DIALOG_START));
            System.out.println("entered run thread, before cancelDiscovery");
            bAdapter.cancelDiscovery();
            try
            {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                System.out.println("run ConnectThread");
                EventBus.getDefault().post(new MessageEvent("","Connecting TeleECG Device..",MessageEvent.TOAST_MESSAGE));
                mmSocket.connect();
            }
            catch (IOException connectException)
            {
//                Toast.makeText(getApplicationContext(), "Socket error: run() IOException", Toast.LENGTH_SHORT).show();
                System.out.println("IO Exception in run()");
                // Unable to connect; close the socket and get out
                try
                {
                    mmSocket.close();
                    this.cancel();	//Thread is canceled/closed along with the socket... RKJ
                    System.out.println("not a TeleECG device run()");
                    Message msg1 = Bt_mHandler.obtainMessage(CONNECTION_ERROR_DEVICE);
                    Bt_mHandler.sendMessage(msg1);
                }
                catch (Exception closeException)
                {
            //        Toast.makeText(getApplicationContext(), "Socket error: run() socket close", Toast.LENGTH_SHORT).show();
                    System.out.println("Socket close error in run ERR3="+closeException.getMessage());
                    Message msg2 = Bt_mHandler.obtainMessage(CONNECTION_ERROR_GENERAL);
                    Bt_mHandler.sendMessage(msg2);
                }

                return;
            }
            // Do work to manage the connection (in a separate thread)
            Message msg= Bt_mHandler.obtainMessage(SUCCESS_CONNECT,mmSocket);
            Bt_mHandler.sendMessage(msg);
        }
        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try
            {
                mmSocket.close();
         //       this.cancel();		//Thread is canceled/closed along with the socket... RKJ
            } catch (IOException e) {
            }
        }
    }

    //used for scanning the devices on button click(scan button)
    @Override
    public void onClick(View v)
    {
        try {
            listAdapter.clear();
            devices.clear();
            doDiscovery();
        } catch (Exception e) {
            System.out.println("error in on click"+e.getMessage());
        }
    }

    protected void onDestroy()
    {
        // Make sure we're not doing discovery anymore
        if (bAdapter != null)
        {
            bAdapter.cancelDiscovery();
            // Unregister broadcast listeners
            if (REGISTER)
                this.unregisterReceiver(receiver);
        }
        Toast.makeText(this, "Quitting App", Toast.LENGTH_LONG).show();
        super.onDestroy();
        System.exit(0);
    }

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                System.out.println("ConnectedThread");
            } catch (Exception e) {
                System.out.println("Exception in ConnectedThread: + "+e.getMessage());
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            // Keep listening to the InputStream until an exception occurs
            try
            {
                Message msg1= Bt_mHandler.obtainMessage(SEND_INSTREAM,mmInStream);
                Bt_mHandler.sendMessage(msg1);
                System.out.println("run ConnectedThread");
                Message msg2= Bt_mHandler.obtainMessage(SEND_OUTSTREAM,mmOutStream);
                Bt_mHandler.sendMessage(msg2);
            } catch (Exception e){
                Message m1=Bt_mHandler.obtainMessage(MESSAGE_COMMUNICATION);
                Bt_mHandler.sendMessage(m1);
                System.out.println("Error in connected thread="+e.getMessage());
            }
        }
    }
}
