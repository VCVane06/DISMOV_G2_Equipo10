package com.example.gestorpro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
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

import com.example.gestorpro.database.AppDatabase;
import com.example.gestorpro.database.Producto;
import com.example.gestorpro.database.ProductoDao;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 1001;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private int usuarioId;
    private ProductoDao productoDao;
    private AppDatabase db;

    // UI Result Views
    private LinearLayout viewInstructions, viewNotFound, viewFound;
    private TextView tvScannedCodeNotFound, tvFoundCode, tvFoundName, tvFoundQty, tvFoundPrice, tvFoundProductNameHeader;
    private TextView btnGoToRegister, btnAdminProduct;
    private ImageView btnBack, ivFoundProductImage;

    private boolean isScanningPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        usuarioId = getIntent().getIntExtra("USER_ID", -1);
        db = AppDatabase.getInstance(this);
        productoDao = db.productoDao();

        previewView = findViewById(R.id.previewView);
        viewInstructions = findViewById(R.id.view_instructions);
        viewNotFound = findViewById(R.id.view_not_found);
        viewFound = findViewById(R.id.view_found);

        tvScannedCodeNotFound = findViewById(R.id.tvScannedCodeNotFound);
        tvFoundCode = findViewById(R.id.tvFoundCode);
        tvFoundName = findViewById(R.id.tvFoundName);
        tvFoundQty = findViewById(R.id.tvFoundQty);
        tvFoundPrice = findViewById(R.id.tvFoundPrice);
        tvFoundProductNameHeader = findViewById(R.id.tvFoundProductNameHeader);
        
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnAdminProduct = findViewById(R.id.btnAdminProduct);
        btnBack = findViewById(R.id.btnBack);
        ivFoundProductImage = findViewById(R.id.ivFoundProductImage);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }

        btnBack.setOnClickListener(v -> finish());
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
                Toast.makeText(this, "Error al iniciar cámara", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

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
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Toast.makeText(this, "Fallo al vincular casos de uso", Toast.LENGTH_SHORT).show();
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
                            processScannedCode(rawValue);
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void processScannedCode(String code) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Producto producto = productoDao.getByCodigo(code, usuarioId);
            runOnUiThread(() -> {
                viewInstructions.setVisibility(View.GONE);
                if (producto != null) {
                    showProductFound(producto);
                } else {
                    showProductNotFound(code);
                }
            });
        });
    }

    private void showProductNotFound(String code) {
        viewFound.setVisibility(View.GONE);
        viewNotFound.setVisibility(View.VISIBLE);
        tvScannedCodeNotFound.setText(code);
        
        btnGoToRegister.setOnClickListener(v -> {
            ProductoDialogFragment dialog = ProductoDialogFragment.newInstance(usuarioId, null, code);
            dialog.setOnProductoGuardadoListener(nuevoProducto -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    db.productoDao().insert(nuevoProducto);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Producto registrado: " + nuevoProducto.getNombre(), Toast.LENGTH_SHORT).show();
                        showProductFound(nuevoProducto); // Mostrarlo una vez registrado
                    });
                });
            });
            dialog.show(getSupportFragmentManager(), "registrar_producto");
        });
    }

    private void showProductFound(Producto producto) {
        viewNotFound.setVisibility(View.GONE);
        viewFound.setVisibility(View.VISIBLE);
        
        tvFoundProductNameHeader.setText(producto.getNombre());
        tvFoundCode.setText(producto.getCodigoBarras());
        tvFoundName.setText(producto.getNombre());
        tvFoundQty.setText(String.format(Locale.getDefault(), "%d/%d", producto.getCantidadActual(), producto.getCantidadMinima()));
        tvFoundPrice.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecioUnitario()));

        btnAdminProduct.setOnClickListener(v -> {
            ProductoDialogFragment dialog = ProductoDialogFragment.newInstance(usuarioId, producto);
            dialog.setOnProductoGuardadoListener(actualizado -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    db.productoDao().update(actualizado);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                        showProductFound(actualizado);
                    });
                });
            });
            dialog.show(getSupportFragmentManager(), "editar_producto");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isScanningPaused = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
