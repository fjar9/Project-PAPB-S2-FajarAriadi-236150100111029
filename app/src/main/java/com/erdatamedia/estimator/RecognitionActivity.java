package com.erdatamedia.estimator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.erdatamedia.estimator.fragment.BluetoothFragment;
import com.erdatamedia.estimator.fragment.CapturedFragment;
import com.erdatamedia.estimator.model.DeviceInfoModel;
import com.erdatamedia.estimator.tools.ObjectDetectorClass;
import com.erdatamedia.estimator.tools.Tools;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RecognitionActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "RecognitionActivity";
    private Dialog loading;

    private LinearLayout controlLyt;
    private ImageView flipBtn, bluetoothBtn, captureBtn;
    private Button capturedBtn, saveBtn, delBtn;
    private ImageView preview;
    private TextView info, distanceTv;
    private SeekBar seekBar;

    private Mat mRgba = new Mat();
    private Mat mGray = new Mat();
    private Mat recognized = new Mat();
    private CameraBridgeViewBase mOpenCvCameraView;
    private ObjectDetectorClass objectDetectorClass;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCv Is loaded");
                    mOpenCvCameraView.enableView();
                }
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private static final int DIALOG_QUEST_CODE = 1000;
    private static final int DIALOG_QUEST_CODE_2 = 2000;

    public RecognitionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    private String SIDE = "";
    private double cm = 0.026458333333333d;
    private double distance = 300;
    private double capaturedDistance = 300;
    private double multiplier = 10d;
    private double result = 0;
    private int foto = 0;
    private Activity activity = RecognitionActivity.this;

    private Executor executor = Executors.newSingleThreadExecutor();
    private String deviceName = null;
    private String deviceAddress;
    private static Handler handler;
    private static BluetoothSocket mmSocket;
    private static ConnectedThread connectedThread;
    private static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;
    private Integer param = 10;

    private BluetoothAdapter bluetoothAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_recognition);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        loading = loadingDialog();

        SIDE = getIntent().getStringExtra("SIDE");
        param = getIntent().getIntExtra("PARAM", 10);

        controlLyt = findViewById(R.id.control);
        preview = findViewById(R.id.preview);
        info = findViewById(R.id.info);
        distanceTv = findViewById(R.id.distance);
        seekBar = findViewById(R.id.seek);
        flipBtn = findViewById(R.id.flip);
        bluetoothBtn = findViewById(R.id.bluetooth);
        captureBtn = findViewById(R.id.capture);
        capturedBtn = findViewById(R.id.captured);
        saveBtn = findViewById(R.id.save);
        delBtn = findViewById(R.id.delete);
        mOpenCvCameraView = findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        if (SIDE.equals("SIDE")) flipBtn.setVisibility(View.VISIBLE);

//        mOpenCvCameraView.setMaxFrameSize(1,1);
        try {
            // input size is 300 for this model
            objectDetectorClass = new ObjectDetectorClass(getAssets(),
                    "ssd_mobilenet.tflite", "labelmap.txt", 300, SIDE);
            Log.d("MainActivity", "Model is successfully loaded");
        } catch (IOException e) {
            Log.d("MainActivity", "Getting some error");
            e.printStackTrace();
        }

        captureBtn.setOnClickListener(view -> capture());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                multiplier = param + 0.026 * (progress);
//                distance = 300 + progress;
//                String aa = (300 + progress) + " cm";
//                distanceTv.setText(aa);
                objectDetectorClass.THICKNESS = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        saveBtn.setOnClickListener(v -> {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + SIDE),
                    SIDE + "_" + foto + "_" + capaturedDistance + "_" + result + ".jpg");

            BitmapDrawable drawable = (BitmapDrawable) preview.getDrawable();
            Tools.createFile(file, drawable.getBitmap());

            foto++;
            String totalFoto = foto + " foto";
            capturedBtn.setText(totalFoto);
            preview.setVisibility(View.GONE);
            controlLyt.setVisibility(View.GONE);
            mOpenCvCameraView.enableView();
            captureBtn.setOnClickListener(view -> capture());
        });

        delBtn.setOnClickListener(v -> {
            preview.setVisibility(View.GONE);
            controlLyt.setVisibility(View.GONE);
            mOpenCvCameraView.enableView();
            captureBtn.setOnClickListener(view -> capture());
        });


        File[] files = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + SIDE + "/")
                .listFiles();
        foto = files != null ? files.length : 0;
        String c = foto + " foto";
        capturedBtn.setText(c);
        capturedBtn.setOnClickListener(v -> {
            mOpenCvCameraView.disableView();
            showDialogCaptured();
        });

        bluetoothBtn.setOnClickListener(v -> showDialogBluetooth());

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                Toast.makeText(activity, "Berhasil Tersambung",
                                        Toast.LENGTH_SHORT).show();
                                bluetoothBtn.setEnabled(true);
                                loading.dismiss();
                                break;
                            case -1:
                                Toast.makeText(activity, "Gagal Tersambung",
                                        Toast.LENGTH_SHORT).show();
                                bluetoothBtn.setEnabled(true);
                                loading.dismiss();
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        // Read message from Arduino
                        String jarakReal = !msg.obj.toString().equals("") ? msg.obj.toString() : "0";
                        multiplier = 300 * 0.026 + param;
                        String arduinoMsg = jarakReal + " cm";
                        distance = Double.parseDouble(jarakReal);
                        distanceTv.setText(arduinoMsg);
                }
            }
        };

        flipBtn.setOnClickListener(v -> {
            if (objectDetectorClass.H.equals("RIGHT")) objectDetectorClass.H = "LEFT";
            else objectDetectorClass.H = "RIGHT";

            flipBtn.setRotation(180);
        });
    }

    private void capture() {
        int halfCols = recognized.cols() / 2;
        int halfRows = recognized.rows() / 2;

        Bitmap dstBitmap = Bitmap.createBitmap(
                recognized.cols() > 0 ? recognized.cols() : mRgba.cols(),
                recognized.rows() > 0 ? recognized.rows() : mRgba.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(recognized, dstBitmap);

        preview.setImageBitmap(dstBitmap);

        result = (objectDetectorClass.result * cm * multiplier);
        String string = "hasil : " + result;
        info.setText(string);

        preview.setVisibility(View.VISIBLE);
        controlLyt.setVisibility(View.VISIBLE);
        mOpenCvCameraView.disableView();
        capaturedDistance = distance;
        captureBtn.setOnClickListener(view -> {
        });
    }

    private void showDialogCaptured() {
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + SIDE) + "/";
        File[] files = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + SIDE + "/")
                .listFiles();
        List<String> list = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                list.add(file.getName());
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        CapturedFragment newFragment = new CapturedFragment(activity, path, list);
        newFragment.setRequestCode(DIALOG_QUEST_CODE);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
        newFragment.setOnCallbackResult((requestCode, msg) -> {
            if (requestCode == DIALOG_QUEST_CODE) {
                File[] f = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + SIDE + "/")
                        .listFiles();
                foto = f != null ? f.length : 0;
                String c = foto + " foto";
                capturedBtn.setText(c);
                mOpenCvCameraView.enableView();
            }
        });
    }

    private void showDialogBluetooth() {
        mOpenCvCameraView.disableView();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<DeviceInfoModel> deviceList = new ArrayList<>();
        for (BluetoothDevice device : pairedDevices) {
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress();
            deviceList.add(new DeviceInfoModel(deviceName, deviceHardwareAddress));
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        BluetoothFragment newFragment = new BluetoothFragment(activity, deviceList);
        newFragment.setRequestCode(DIALOG_QUEST_CODE_2);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
        newFragment.setOnCallbackResult((requestCode, msg) -> {
            if (requestCode == DIALOG_QUEST_CODE_2) {
                if (!msg.equals("")) {
                    deviceAddress = msg;
                    loading.show();
                    Toast.makeText(activity, "Menyambungkan ...", Toast.LENGTH_SHORT).show();
                    bluetoothBtn.setEnabled(false);

                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress, activity);
                    createConnectThread.start();
                }
                mOpenCvCameraView.enableView();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            //if load success
            Log.d(TAG, "Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            //if not loaded
            Log.d(TAG, "Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
        if (createConnectThread != null) createConnectThread.cancel();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame cvCameraViewFrame) {
        mRgba = cvCameraViewFrame.rgba();
        mGray = cvCameraViewFrame.gray();

        recognized = objectDetectorClass.recognizeImage(mRgba);
        return recognized;
    }

    @Override
    public void onCameraViewStarted(int i, int i2) {
        mRgba = new Mat(i2, i, CvType.CV_8UC4);
        mGray = new Mat(i2, i, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Dialog loadingDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setDimAmount(0f);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static class CreateConnectThread extends Thread {

        private String TAG = "TAG";
        private Context context;

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address, Context context) {
            this.context = context;
            /* Use a temporary object that is later assigned to mmSocket
            because mmSocket is final. */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /* Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID); */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.d("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.d("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.d(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
//                Toast.makeText(context, "Bluetooth Terputus", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.d(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /* Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler. */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n') {
                        readMessage = new String(buffer, 0, bytes);
                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error", "Unable to send message", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}