package com.example.gestorpro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.gestorpro.database.Producto;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = "ScanActivity";
    public static final String EXTRA_RETURN_RESULT = "return_result";
    public static final String EXTRA_SCANNED_CODE = "scanned_code";
    
    private static final int PERMISSION_REQUEST_CAMERA = 1001;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private String userUid, userName;
    private FirebaseFirestore db;
    private boolean returnResultMode = false;

    private LinearLayout viewInstructions, viewNotFound, viewFound;
    private TextView tvScannedCodeNotFound, tvFoundCode, tvFoundPrice, tvFoundQty, tvFoundProductNameHeader;
    private View btnGoToRegister, btnAdminProduct, scanLine;
    private ImageView btnBack;

    private boolean isScanningPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        userUid = getIntent().getStringExtra("USER_UID");
        userName = getIntent().getStringExtra("USER_NAME");
        returnResultMode = getIntent().getBooleanExtra(EXTRA_RETURN_RESULT, false);
        
        db = FirebaseFirestore.getInstance();

        initViews();
        startScanLineAnimation();

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        previewView = findViewById(R.id.previewView);
        viewInstructions = findViewById(R.id.view_instructions);
        viewNotFound = findViewById(R.id.view_not_found);
        viewFound = findViewById(R.id.view_found);
        scanLine = findViewById(R.id.scan_line);

        tvScannedCodeNotFound = findViewById(R.id.tvScannedCodeNotFound);
        tvFoundCode = findViewById(R.id.tvFoundCode);
        tvFoundPrice = findViewById(R.id.tvFoundPrice);
        tvFoundQty = findViewById(R.id.tvFoundQty);
        tvFoundProductNameHeader = findViewById(R.id.tvFoundProductNameHeader);
        
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnAdminProduct = findViewById(R.id.btnAdminProduct);
        btnBack = findViewById(R.id.btnBack);

        viewInstructions.setOnClickListener(v -> resetScannerUI());
    }

    private void startScanLineAnimation() {
        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.95f
        );
        animation.setDuration(2000);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        scanLine.startAnimation(animation);
    }

    private void vibrateFeedback() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(50);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "No se pudo vibrar: " + e.getMessage());
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error al iniciar cámara", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        BarcodeScanner scanner = BarcodeScanning.getClient();

        imageAnalysis.setAnalyzer(cameraExecutor, image -> {
            if (isScanningPaused) {
                image.close();
                return;
            }
            processImageProxy(scanner, image);
        });

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Error binding use cases", e);
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(BarcodeScanner scanner, androidx.camera.core.ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null && !isScanningPaused) {
                            isScanningPaused = true;
                            runOnUiThread(() -> {
                                vibrateFeedback();
                                if (returnResultMode) {
                                    returnScannedResult(rawValue);
                                } else {
                                    processScannedCode(rawValue);
                                }
                            });
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void returnScannedResult(String code) {
        Intent data = new Intent();
        data.putExtra(EXTRA_SCANNED_CODE, code);
        setResult(RESULT_OK, data);
        finish();
    }

    private void processScannedCode(String code) {
        // Búsqueda global (sin filtrar por usuario_creador_id)
        db.collection("productos")
                .whereEqualTo("codigo_barras", code)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        Producto producto = doc.toObject(Producto.class);
                        producto.setId(doc.getId());
                        showProductFound(producto);
                    } else {
                        showProductNotFound(code);
                    }
                })
                .addOnFailureListener(e -> {
                    isScanningPaused = false;
                    Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show();
                });
    }

    private void showProductNotFound(String code) {
        viewInstructions.setVisibility(View.GONE);
        viewFound.setVisibility(View.GONE);
        viewNotFound.setVisibility(View.VISIBLE);
        tvScannedCodeNotFound.setText("CÓDIGO: " + code);
        
        btnGoToRegister.setOnClickListener(v -> {
            ProductoDialogFragment dialog = ProductoDialogFragment.newInstance(userUid, userName, null, code);
            dialog.show(getSupportFragmentManager(), "registrar_producto");
        });
    }

    private void showProductFound(Producto producto) {
        viewInstructions.setVisibility(View.GONE);
        viewNotFound.setVisibility(View.GONE);
        viewFound.setVisibility(View.VISIBLE);
        
        tvFoundProductNameHeader.setText(producto.getNombre());
        tvFoundCode.setText(producto.getCodigo_barras());
        tvFoundQty.setText(String.valueOf(producto.getStock_actual()));
        tvFoundPrice.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecio()));

        btnAdminProduct.setOnClickListener(v -> {
            ProductoDialogFragment dialog = ProductoDialogFragment.newInstance(userUid, userName, producto);
            dialog.show(getSupportFragmentManager(), "editar_producto");
        });
    }

    private void resetScannerUI() {
        viewNotFound.setVisibility(View.GONE);
        viewFound.setVisibility(View.GONE);
        viewInstructions.setVisibility(View.VISIBLE);
        isScanningPaused = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetScannerUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
