package com.eas.cards2;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.content.*;
import android.content.SharedPreferences;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.budiyev.android.codescanner.*;
import com.budiyev.android.codescanner.CodeScannerView;
import com.getkeepsafe.taptargetview.*;
import com.google.zxing.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import jp.wasabeef.picasso.transformations.*;
import org.json.*;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ScannerActivity extends AppCompatActivity {
	
	private CodeScanner mCodeScanner;
	
	private LinearLayout parent;
	private CodeScannerView scanner_view;
	
	private SharedPreferences shared;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.scanner);
        final View root = findViewById(R.id.parent);

		final int pL = root.getPaddingLeft();
		final int pT = root.getPaddingTop();
		final int pR = root.getPaddingRight();
		final int pB = root.getPaddingBottom();

		ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
			Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(
				pL + bars.left,
				pT + bars.top,
				pR + bars.right,
				pB + bars.bottom
			);
			return insets;
		});

		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		parent = findViewById(R.id.parent);
		scanner_view = findViewById(R.id.scanner_view);
		shared = getSharedPreferences("saveData", Activity.MODE_PRIVATE);
	}
	
	private void initializeLogic() {
		CodeScannerView scannerView = findViewById(R.id.scanner_view);
		mCodeScanner = new CodeScanner(this, scannerView);
		mCodeScanner.setDecodeCallback(new DecodeCallback() {
			@Override public void onDecoded(@NonNull final Result result) { runOnUiThread(new Runnable() {
					@Override
					public void run() {
						shared.edit().putString("code", result.getText()).commit();
						shared.edit().putString("type", result.getBarcodeFormat().toString()).commit();
						finish();
					} }
				
				); }
			
		}
		
		);
		mCodeScanner.startPreview();
	}
	
}