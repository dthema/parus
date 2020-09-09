package com.example.parus.ui.communication.see;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.parus.R;
import com.example.parus.databinding.ActivitySeeBinding;
import com.example.parus.viewmodels.SeeViewModel;

public class SeeActivity extends AppCompatActivity {

    private static final String TAG = SeeActivity.class.getSimpleName();
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private final String[] types = {"Распознавание текста", "Распознавание объекта"};
    private SeeViewModel seeViewModel;
    private ActivitySeeBinding binding;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_see);
        seeViewModel = new ViewModelProvider(this).get(SeeViewModel.class);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Смотреть");
        }
        binding.openSeeCamera.setOnClickListener(v -> startCameraActivity());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item2, types);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        binding.seeTypeSpinner.setAdapter(adapter);
        binding.seeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    binding.seeWarning.setVisibility(View.VISIBLE);
                else
                    binding.seeWarning.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // запуск с главной страницы
        if (getIntent().getIntExtra("fastAction", 0) == 1) {
            binding.seeTypeSpinner.setSelection(0);
            binding.openSeeCamera.callOnClick();
        }
        if (getIntent().getIntExtra("fastAction", 0) == 2) {
            binding.seeTypeSpinner.setSelection(1);
            binding.openSeeCamera.callOnClick();
        }

    }

    private void startCameraActivity() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            } else {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Bundle extras = data.getExtras();
            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                if (extras != null) {
                    Bitmap bitmap = (Bitmap) extras.get("data");
                    if (bitmap != null) {
                        switch (binding.seeTypeSpinner.getSelectedItemPosition()) {
                            case 0:
                                detectText(bitmap);
                                break;
                            case 1:
                                binding.seeText.setText("Результат переводится...");
                                binding.seeText.setTextSize(40);
                                detectObject(bitmap);
                                break;
                        }
                    }
                }
            }
        } else if (getIntent().getIntExtra("fastAction", 0) != 0) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

    private void detectText(Bitmap bitmap) {
        seeViewModel.detectText(bitmap).observe(this, pair -> {
            String text = pair.first;
            Float textSize = pair.second;
            if (textSize == null || text == null)
                return;
            if (textSize == 0f) {
                Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                binding.seeText.setText("");
            } else {
                binding.seeText.setText(text);
                binding.seeText.setTextSize(textSize);
            }
        });
    }

    private void detectObject(Bitmap bitmap) {
        seeViewModel.detectObject(bitmap).observe(this, pair -> {
            String text = pair.first;
            Float textSize = pair.second;
            if (textSize == null || text == null)
                return;
            if (textSize == 0f) {
                Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                binding.seeText.setText("");
            } else {
                binding.seeText.setText(text);
                binding.seeText.setTextSize(textSize);
            }
        });
    }

}


