package com.erdatamedia.estimator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TakePictureActivity extends AppCompatActivity {
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS
            = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private TextView infoTv, distanceTv;
    private Executor executor = Executors.newSingleThreadExecutor();

    private String deviceName = null;
    private String deviceAddress;
    private static Handler handler;
    private static BluetoothSocket mmSocket;
    private static ConnectedThread connectedThread;
    private static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1;
    // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2;
    // used in bluetooth handler to identify message update

    private Context context = TakePictureActivity.this;

    private ImageView siluet;
    private int a = 0;
    private int b = 0;

    private int containerW = 1280;
    private int containerH = 960;

    private int jarakRef = 2;   // meter
    private int boxW = 300;     // pixel
    private int boxH = 200;     // pixel
    private int refW = 70;      // cm
    private int refH = 45;      // cm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_take_picture);

        previewView = findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        infoTv = findViewById(R.id.info);
        distanceTv = findViewById(R.id.distance);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindImageAnalysis(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        siluet = findViewById(R.id.siluet);

        findViewById(R.id.bluetooth).setOnClickListener(v -> {
            Intent intent = new Intent(TakePictureActivity.this, SelectDeviceActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.choose_siluet).setOnClickListener(v -> {
            String[] colors = {"Sapi Bali", "Sapi Madura", "Sapi PO"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pilih siluet");
            builder.setItems(colors, (dialog, which) -> {
                if (which == 0) {
                    siluet.setImageDrawable(getResources().getDrawable(R.drawable.siluet_bali));
                } else if (which == 1) {
                    siluet.setImageDrawable(getResources().getDrawable(R.drawable.siluet_madura));
                } else {
                    siluet.setImageDrawable(getResources().getDrawable(R.drawable.siluet_po));
                }
            });
            builder.show();
        });
        findViewById(R.id.flip).setOnClickListener(v -> {
            if (siluet.getScaleX() == 1) siluet.setScaleX(-1);
            else siluet.setScaleX(1);
        });

        ImageView buttonConnect = findViewById(R.id.bluetooth);

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            Toast.makeText(this, "Menyambungkan ...", Toast.LENGTH_SHORT).show();
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress, context);
            createConnectThread.start();
        }

//        String acuan = jarakRef + "m\nwidth:" + siluet.getLayoutParams().width + "\nheigth:" + siluet.getLayoutParams().height;
        String acuan = "siluet width:" + siluet.getLayoutParams().width +
                "\nsiluet heigth:" + siluet.getLayoutParams().height;
        infoTv.setText(acuan);

        siluet.post(() -> {
            a = siluet.getWidth();
            b = siluet.getHeight();
//            String acuan1 = jarakRef + "m\nwidth:" + siluet.getWidth() + "\nheigth:" + siluet.getHeight();
            String acuan1 = "siluet width:" + siluet.getWidth() +
                    "\nsiluet heigth:" + siluet.getHeight();
            infoTv.setText(acuan1);
        });

        SeekBar seekbar = findViewById(R.id.seek);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                float scale = progress / 100;
                float scale = progress;
                int calcA = (int) (scale * 10);
                int calcB = (int) (scale * 10);

                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(a + calcA, b + calcB);
                layout.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                siluet.setLayoutParams(layout);

//                String acuan = jarakRef + "m\nwidth:" + siluet.getLayoutParams().width + "\nheigth:" + siluet.getLayoutParams().height;
                String acuan = "siluet width:" + siluet.getLayoutParams().width +
                        "\nsiluet heigth:" + siluet.getLayoutParams().height;
                infoTv.setText(String.valueOf(acuan));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /* Second most important piece of Code. GUI Handler */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                Toast.makeText(TakePictureActivity.this,
                                        "Berhasil Tersambung", Toast.LENGTH_SHORT).show();
                                buttonConnect.setEnabled(true);
                                break;
                            case -1:
                                Toast.makeText(TakePictureActivity.this,
                                        "Gagal Tersambung", Toast.LENGTH_SHORT).show();
                                buttonConnect.setEnabled(true);
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        // Read message from Arduino
                        String jarakReal = !msg.obj.toString().equals("") ? msg.obj.toString() : "0";
                        double a = (Double.parseDouble(jarakReal) - 300) * 0.026 + 10;
                        String arduinoMsg = a + " px " + jarakReal + " cm";
                        distanceTv.setText(arduinoMsg);
                }
            }
        };

        screening();
    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1000, 1000))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
//                textView.setText(Integer.toString(orientation));
            }
        };
        orientationEventListener.enable();
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());


        ImageCapture.Builder builder = new ImageCapture.Builder();
        ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        findViewById(R.id.capture).setOnClickListener(v -> {
            if (allPermissionsGranted()) {
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg");
                ImageCapture.OutputFileOptions outputFileOptions =
                        new ImageCapture.OutputFileOptions.Builder(file).build();
                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        new Handler(Looper.getMainLooper())
                                .post(() -> startActivity(
                                        new Intent(TakePictureActivity.this,
                                                ProcessActivity.class)));
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException error) {
                        error.printStackTrace();
//                        loading.dismiss();
                        Toast.makeText(TakePictureActivity.this,
                                "Something error " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();

                    }
                });
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        });

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector,
                imageAnalysis, preview, imageCapture);

    }

    private void screening() {
        TextView screenTv = findViewById(R.id.screen);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        String size = "screen width:" + width + "\nscreen height:" + height;
        screenTv.setText(size);
    }

    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null) createConnectThread.cancel();
        super.onBackPressed();
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
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
                Toast.makeText(context, "Bluetooth Terputus", Toast.LENGTH_SHORT).show();
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


    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

}