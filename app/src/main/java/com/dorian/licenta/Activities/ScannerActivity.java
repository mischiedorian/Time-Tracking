package com.dorian.licenta.Activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import com.dorian.licenta.NetworkAvailable;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.dorian.licenta.Product.Product;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScannerActivity extends AppCompatActivity {

    private SurfaceView cameraView;
    private Button takePhoto;
    private CameraSource cameraSource;
    private final int reqCameraPermissionID = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        cameraView = (SurfaceView) findViewById(R.id.surfaceView);
        takePhoto = (Button) findViewById(R.id.btnPhoto);

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(ScannerActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    reqCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {
                        takePhoto.setOnClickListener(v -> {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < items.size(); ++i) {
                                TextBlock item = items.valueAt(i);
                                stringBuilder.append(item.getValue());
                                stringBuilder.append("\n");
                            }
                            obtineProduse(stringBuilder.toString());
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case reqCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    public void obtineProduse(String result) {
        SharedPreferences sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
        int idUser = sharedPreferences.getInt("idUser", 0);
        String[] vector = result.split("\n");

        String product = null;
        int quantity = 0;

        for (int i = 0; i < vector.length; i++) {
            try {
                if (vector[i].split(" ")[1].equals("x") || vector[i].split(" ")[1].equals("X")) {
                    product = vector[i - 1];
                    quantity = Integer.parseInt(vector[i].split(" ")[0].split(",")[0]);
                }
            } catch (Exception e) {

            }
        }

        if (product != null && quantity != 0 && idUser != 0) {
            Log.wtf("produs", product + " - " + quantity);
            postProduct(new Product(0, product, quantity, idUser));
        } else {
            Log.wtf("produs", product + " - " + quantity + " - " + idUser);
            Toast.makeText(getApplicationContext(),
                    R.string.msgErrorScanner,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void postProduct(Product product) {
        try {
            RestServices.Factory.getIstance().getProductsAfterName(product.getName()).enqueue(new Callback<List<Product>>() {
                @Override
                public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                    int semafor = 0;
                    for (Product product : response.body()) {
                        if (product.getIdLocatie() == 0) {
                            semafor = product.getId();
                        }
                    }
                    if (semafor != 0) {
                        product.setQuantity(product.getQuantity() + product.getQuantity());
                        RestServices.Factory.getIstance().modifyProduct(semafor, product).enqueue(new Callback<Product>() {
                            @Override
                            public void onResponse(Call<Product> call, Response<Product> response) {
                            }

                            @Override
                            public void onFailure(Call<Product> call, Throwable t) {
                                if (t.getMessage().contains("Failed to connect to /192.168.")) {
                                    Log.i("onResponseUser", "Server down!");
                                    Toast.makeText(getApplicationContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        post(product);
                    }
                }

                @Override
                public void onFailure(Call<List<Product>> call, Throwable t) {
                    post(product);
                }
            });
            if (!NetworkAvailable.isNetworkAvailable(this)) {
                Toast.makeText(getApplicationContext(), R.string.networkMsg, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.scannerOkMsg, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.msgServerDown, Toast.LENGTH_SHORT).show();
        }
    }

    private void post(Product product) {
        RestServices.Factory.getIstance().postProduct(product).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                if (t.getMessage().contains("Failed to connect to /192.168.")) {
                    Log.i("onResponseUser", "Server down!");
                    Toast.makeText(getApplicationContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}