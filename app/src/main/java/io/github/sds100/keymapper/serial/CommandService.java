package io.github.sds100.keymapper.serial;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.Toast;

import io.github.sds100.keymapper.usbserial.driver.UsbSerialDriver;
import io.github.sds100.keymapper.usbserial.driver.UsbSerialPort;
import io.github.sds100.keymapper.usbserial.driver.UsbSerialProber;

import io.github.sds100.keymapper.BuildConfig;

public class CommandService extends IntentService implements ServiceConnection, SerialListener {
    private enum Connected {False, Pending, True}

    private int deviceId, portNum, baudRate;
    private String newline = "\r";
    private String command;
    public static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    private SerialSocket socket;
    private SerialService service;
    private boolean initialStart = true;
    private BroadcastReceiver broadcastReceiver;
    private Connected connected = Connected.False;


    public static final String PARAM_COMMAND = "cmd";
    public static final String PARAM_OUT_MSG = "omsg";

    private boolean firstCommand = true;

    public CommandService() {
        super("CommandService");
        Log.d("service", "constructor");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(INTENT_ACTION_GRANT_USB)) {
                    Boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    connect(granted);
                }
            }
        };
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        Log.d("service", "onServiceConnected");
        connect();
//        send("PO");
//        disconnect();
//        if(initialStart && isResumed()) {
//            initialStart = false;
//            getActivity().runOnUiThread(this::connect);
//        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("service", "onHandle received");
        command = intent.getStringExtra(PARAM_COMMAND);
        deviceId = 0;
        portNum = 0;
        baudRate = 9600;

        Log.d("service", "connect");

        if (service != null) {
//            service.attach(this);
//            send(command);
//            if(!firstCommand) {
                if (connected == Connected.True) {
                    if (command == null) {
                        Log.d("service", "null cmd");

                        send("PF");
                    } else {
                        Log.d("service", "cmd " + command);
                        send(command);
                    }
                    Log.d("service", "sent");
                }
//            }
        } else {
            getApplicationContext().bindService(new Intent(getApplicationContext(), SerialService.class), this, Context.BIND_AUTO_CREATE);
//            getApplicationContext().startService(new Intent(getApplicationContext(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
        }


    }

    private void connect() {
        connect(null);
    }

    private void connect(Boolean permissionGranted) {
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        for (UsbDevice v : usbManager.getDeviceList().values()) {
//            if (v.getDeviceId() == deviceId)
            device = v;
        }
        if (device == null) {
            status("connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if (driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if (driver == null) {
            status("connection failed: no driver for device");
            return;
        }
        if (driver.getPorts().size() < portNum) {
            status("connection failed: not enough ports at device");
            return;
        }
        UsbSerialPort usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if (usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if (usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                status("connection failed: permission denied");
            else
                status("connection failed: open failed");
            return;
        }

        connected = Connected.Pending;
        Log.d("service", "Connected.Pending");
        try {
            socket = new SerialSocket();
            service.connect(this, "Connected");
            Log.d("service", "service.connect");

            socket.connect(getApplicationContext(), service, usbConnection, usbSerialPort, baudRate);
            Log.d("service", "socket.connect");
            // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
            // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
            onSerialConnect();
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        if (service != null) {
            service.disconnect();
        } else {
            Log.d("service", "service is null");
        }
        if (socket != null) {
            socket.disconnect();
        } else {
            Log.d("service", "socket is null");
        }
        socket = null;
    }

    private void send(String str) {
        if (connected != Connected.True) {
            Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
//            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            receiveText.append(spn);
            byte[] data = (str + newline).getBytes();
            socket.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
//        receiveText.append(new String(data));
        String received = new String(data);
        Log.d("service receive", received);
//        if (received.equals("\n")) {
//            disconnect();
//            getApplicationContext().stopService(new Intent(getApplicationContext(), SerialService.class));
//            stopSelf();
//        }
    }

    void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
//        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Log.d("service status", str);
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
        Log.d("service", "connected");
        if (firstCommand) {
            if (command == null) {
                Log.d("service", "null cmd");

                send("PF");
            } else {
                Log.d("service", "cmd " + command);
                send(command);
            }
            Log.d("service", "sent");
            firstCommand = false;
        }
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
//        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
//        disconnect();
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getApplicationContext().stopService(new Intent(getApplicationContext(), SerialService.class));
        super.onDestroy();
    }
}

