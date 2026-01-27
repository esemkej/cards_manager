package com.eas.cards;

import android.animation.*;
import android.animation.ObjectAnimator;
import android.app.*;
import android.app.Activity;
import android.content.*;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import android.os.Bundle;
import android.text.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.webkit.*;
import android.widget.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
import com.budiyev.android.codescanner.*;
import com.getkeepsafe.taptargetview.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.*;
import java.io.*;
import java.io.InputStream;
import java.text.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.*;
import org.json.*;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.common.BitMatrix;
import android.graphics.drawable.shapes.RoundRectShape;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.text.method.KeyListener;
import android.view.inputmethod.InputMethodManager;

public class MainActivity extends AppCompatActivity {
	
	private FloatingActionButton _fab;
	private HashMap<String, Object> cards = new HashMap<>();
	private String cardSaveName = "";
	private String cardSaveType = "";
	private String cardSaveCode = "";
	private TextView code_txt;
	private HashMap<String, Object> colors = new HashMap<>();
	private String selectedGradStyle = "";
	private boolean favorite = false;
	private static final int REQ_CAMERA = 1000;
	private static final String PREF_SORT_TYPE = "pref_sort_type";
	private static final String PREF_SORT_ORDER = "pref_sort_order";
	private static final String PREF_SORT_FILTER = "pref_sort_filter";
	private HashMap<String, Object> settings = new HashMap<>();
	private double progress = 0;
	private boolean folder = false;
	private boolean inFolder = false;
	private String folderPath = "";
	private final ArrayList<String> folderIdStack = new ArrayList<>();
	private final ArrayList<String> folderNameStack = new ArrayList<>();
	private boolean delFolder = false;
	private static final int TAG_BASE_TEXT_PX = 0x7f0a0123;
	private double textLevel = 0;
	private boolean debug = false;
	private ActivityResultLauncher<Intent> imagePickerLauncher;
	private OnImagePicked pendingPicked;
	private OnCancelled pendingCancelled;
	private boolean pickInProgress = false;
	private boolean scanImage = false;
	private ViewGroup tapTargetRoot;
	private String backup = "";
	private static final int REQ_EXPORT_JSON = 6101;
	private static final int REQ_IMPORT_JSON = 6102;
	private boolean editable = false;
	
	private ArrayList<HashMap<String, Object>> cards_list = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> cards_list_all = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> colors_list = new ArrayList<>();
	
	private LinearLayout parent;
	private LinearLayout filter_parent;
	private SwipeRefreshLayout srefresh;
	private LinearLayout no_items_lay;
	private LinearLayout filter_bar;
	private LinearLayout search_bar;
	private LinearLayout settings_bar;
	private ImageView filter_img;
	private EditText search_txt;
	private ImageView settings_img;
	private RecyclerView cards_rec;
    private RecyclerView colors_rec;
    private Cards_recAdapter cardsAdapter;
    private Colors_recAdapter colorsAdapter;
	private TextView no_items_txt;
	
	private SharedPreferences card_prefs;
	private com.google.android.material.bottomsheet.BottomSheetDialog bottomShii;
	private Intent i = new Intent();
	private AlertDialog d;
	private ObjectAnimator o = new ObjectAnimator();
	private PopupWindow p;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
        final View root = findViewById(R.id._coordinator);
        final View parent = findViewById(R.id.parent);
        final View fab = findViewById(R.id._fab);

        final int pL = parent.getPaddingLeft();
        final int pT = parent.getPaddingTop();
        final int pR = parent.getPaddingRight();
        final int pB = parent.getPaddingBottom();

        final ViewGroup.MarginLayoutParams fabLp =
                (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        final int mL = fabLp.leftMargin;
        final int mT = fabLp.topMargin;
        final int mR = fabLp.rightMargin;
        final int mB = fabLp.bottomMargin;

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            parent.setPadding(
                    pL + bars.left,
                    pT + bars.top,
                    pR + bars.right,
                    pB + bars.bottom
            );

            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
            lp.leftMargin   = mL + bars.left;
            lp.topMargin    = mT + bars.top;
            lp.rightMargin  = mR + bars.right;
            lp.bottomMargin = mB + bars.bottom;
            fab.setLayoutParams(lp);

            return insets;
        });

        initialize(_savedInstanceState);
        initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		_fab = findViewById(R.id._fab);
		parent = findViewById(R.id.parent);
		filter_parent = findViewById(R.id.filter_parent);
		srefresh = findViewById(R.id.srefresh);
		no_items_lay = findViewById(R.id.no_items_lay);
		filter_bar = findViewById(R.id.filter_bar);
		search_bar = findViewById(R.id.search_bar);
		settings_bar = findViewById(R.id.settings_bar);
		filter_img = findViewById(R.id.filter_img);
		search_txt = findViewById(R.id.search_txt);
		settings_img = findViewById(R.id.settings_img);
		cards_rec = findViewById(R.id.cards_rec);
		no_items_txt = findViewById(R.id.no_items_txt);
		card_prefs = getSharedPreferences("saveData", Activity.MODE_PRIVATE);
		
		srefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				applySortFilter(
				search_txt.getText().toString(),
				loadSortTypeId(),
				loadOrderId(),
				loadFilterId()
				);
			}
		});
		
		filter_bar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				bottomShii = new com.google.android.material.bottomsheet.BottomSheetDialog(MainActivity.this);
				View bottomShiiV;
				bottomShiiV = getLayoutInflater().inflate(R.layout.filters_dialog,null );
				bottomShii.setContentView(bottomShiiV);
				bottomShii.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
				final LinearLayout div1 = (LinearLayout) bottomShiiV.findViewById(R.id.div1);
				final LinearLayout div2 = (LinearLayout) bottomShiiV.findViewById(R.id.div2);
				final RadioGroup sort_types = (RadioGroup) bottomShiiV.findViewById(R.id.sort_types);
				final RadioGroup orders = (RadioGroup) bottomShiiV.findViewById(R.id.orders);
				final RadioGroup filters = (RadioGroup) bottomShiiV.findViewById(R.id.filters);
				final TextView sort_by_txt = (TextView) bottomShiiV.findViewById(R.id.sort_by_txt);
				final RadioButton by_name = (RadioButton) bottomShiiV.findViewById(R.id.by_name);
				final RadioButton by_date_created = (RadioButton) bottomShiiV.findViewById(R.id.by_date_created);
				final RadioButton by_use_count = (RadioButton) bottomShiiV.findViewById(R.id.by_use_count);
				final RadioButton ascending = (RadioButton) bottomShiiV.findViewById(R.id.ascending);
				final RadioButton descending = (RadioButton) bottomShiiV.findViewById(R.id.descending);
				final RadioButton all = (RadioButton) bottomShiiV.findViewById(R.id.all);
				final RadioButton favorites = (RadioButton) bottomShiiV.findViewById(R.id.favorites);
				float scale = textScaleFromLevel((int) textLevel);
				applyTextScale(sort_by_txt, scale);
				RadioButton[] views = new RadioButton[] { by_name, by_date_created, by_use_count, ascending, descending, all, favorites};
				for (RadioButton tv : views) {
					if (tv != null) applyTextScale(tv, scale);
				}
				div1.setClickable(true);
				final float div1_rTL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 360, getResources().getDisplayMetrics());
				final float div1_rTR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 360, getResources().getDisplayMetrics());
				final float div1_rBR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 360, getResources().getDisplayMetrics());
				final float div1_rBL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 360, getResources().getDisplayMetrics());
				final int div1_strokePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 0, getResources().getDisplayMetrics());
				final GradientDrawable div1_bg = new GradientDrawable();
				div1_bg.setColor(0xFFBDBDBD);
				div1_bg.setCornerRadii(new float[]{div1_rTL,div1_rTL,div1_rTR,div1_rTR,div1_rBR,div1_rBR,div1_rBL,div1_rBL});
				div1_bg.setStroke(div1_strokePx, Color.TRANSPARENT);
				div1.setBackground(div1_bg);
				div2.setClickable(true);
				final float div2_rTL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 360, getResources().getDisplayMetrics());
				final float div2_rTR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 360, getResources().getDisplayMetrics());
				final float div2_rBR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 360, getResources().getDisplayMetrics());
				final float div2_rBL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 360, getResources().getDisplayMetrics());
				final int div2_strokePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 0, getResources().getDisplayMetrics());
				final GradientDrawable div2_bg = new GradientDrawable();
				div2_bg.setColor(0xFF212121);
				div2_bg.setCornerRadii(new float[]{div2_rTL,div2_rTL,div2_rTR,div2_rTR,div2_rBR,div2_rBR,div2_rBL,div2_rBL});
				div2_bg.setStroke(div2_strokePx, Color.TRANSPARENT);
				div2.setBackground(div2_bg);
				sort_types.check(loadSortTypeId());
				orders.check(loadOrderId());
				filters.check(loadFilterId());
				setupSortFilterListeners(sort_types, orders, filters);
				bottomShii.setCancelable(true);
				bottomShii.show();
			}
		});
		
		settings_bar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				bottomShii = new com.google.android.material.bottomsheet.BottomSheetDialog(MainActivity.this);
				View bottomShiiV;
				bottomShiiV = getLayoutInflater().inflate(R.layout.settings_dialog,null );
				bottomShii.setContentView(bottomShiiV);
				bottomShii.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
				final TextView visual_txt = (TextView) bottomShiiV.findViewById(R.id.visual_txt);
				final TextView grid_amount_txt = (TextView) bottomShiiV.findViewById(R.id.grid_amount_txt);
				final TextView text_size_txt = (TextView) bottomShiiV.findViewById(R.id.text_size_txt);
				final TextView ux_txt = (TextView) bottomShiiV.findViewById(R.id.ux_txt);
				final TextView default_scan_txt = (TextView) bottomShiiV.findViewById(R.id.default_scan_txt);
				final TextView scan_tip_txt = (TextView) bottomShiiV.findViewById(R.id.scan_tip_txt);
				final TextView mode_switch_txt = (TextView) bottomShiiV.findViewById(R.id.mode_switch_txt);
				final TextView show_tutorial_txt = (TextView) bottomShiiV.findViewById(R.id.show_tutorial_txt);
				final TextView data_txt = (TextView) bottomShiiV.findViewById(R.id.data_txt);
				final TextView import_btn = (TextView) bottomShiiV.findViewById(R.id.import_btn);
				final TextView export_btn = (TextView) bottomShiiV.findViewById(R.id.export_btn);
				final SeekBar grid_amount_sbar = (SeekBar) bottomShiiV.findViewById(R.id.grid_amount_sbar);
				final SeekBar text_size_sbar = (SeekBar) bottomShiiV.findViewById(R.id.text_size_sbar);
				final Switch mode_switch = (Switch) bottomShiiV.findViewById(R.id.mode_switch);
				final CheckBox show_tutorial = (CheckBox) bottomShiiV.findViewById(R.id.show_tutorial);
				final LinearLayout div1 = (LinearLayout) bottomShiiV.findViewById(R.id.div1);
				final LinearLayout div2 = (LinearLayout) bottomShiiV.findViewById(R.id.div2);
				final LinearLayout div3 = (LinearLayout) bottomShiiV.findViewById(R.id.div3);
				final LinearLayout div4 = (LinearLayout) bottomShiiV.findViewById(R.id.div4);
				final LinearLayout show_tutorial_lay = (LinearLayout) bottomShiiV.findViewById(R.id.show_tutorial_lay);
				bottomShii.setCancelable(true);
				div1.setClickable(true);
				
				final float div1_rTL = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div1_rTR = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div1_rBR = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div1_rBL = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				div1.setBackground(new ShapeDrawable(new RoundRectShape(
				new float[]{
					div1_rTL, div1_rTL,
					div1_rTR, div1_rTR,
					div1_rBR, div1_rBR,
					div1_rBL, div1_rBL
				},
				null,
				null
				)) {{
						getPaint().setColor(0xFFBDBDBD);
					}});
				div2.setClickable(true);
				
				final float div2_rTL = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div2_rTR = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div2_rBR = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div2_rBL = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				div2.setBackground(new ShapeDrawable(new RoundRectShape(
				new float[]{
					div2_rTL, div2_rTL,
					div2_rTR, div2_rTR,
					div2_rBR, div2_rBR,
					div2_rBL, div2_rBL
				},
				null,
				null
				)) {{
						getPaint().setColor(0xFF212121);
					}});
				div3.setClickable(true);
				
				final float div3_rTL = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div3_rTR = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div3_rBR = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div3_rBL = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				div3.setBackground(new ShapeDrawable(new RoundRectShape(
				new float[]{
					div3_rTL, div3_rTL,
					div3_rTR, div3_rTR,
					div3_rBR, div3_rBR,
					div3_rBL, div3_rBL
				},
				null,
				null
				)) {{
						getPaint().setColor(0xFFBDBDBD);
					}});
				div4.setClickable(true);
				
				final float div4_rTL = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div4_rTR = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div4_rBR = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				final float div4_rBL = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				(float) 360,
				getResources().getDisplayMetrics()
				);
				
				div4.setBackground(new ShapeDrawable(new RoundRectShape(
				new float[]{
					div4_rTL, div4_rTL,
					div4_rTR, div4_rTR,
					div4_rBR, div4_rBR,
					div4_rBL, div4_rBL
				},
				null,
				null
				)) {{
						getPaint().setColor(0xFF212121);
					}});
				import_btn.setClickable(true);
				import_btn.setBackground(new RippleDrawable(
				new ColorStateList(
				new int[][]{new int[]{}},
				new int[]{0xFFD2B6DC}
				),
				new GradientDrawable() {
					public GradientDrawable getIns(int a, int b, int c, int d) {
						this.setCornerRadius(a);
						this.setStroke(b, c);
						this.setColor(d);
						return this;
					}
				}.getIns((int)12, (int)2, 0xFF212121, 0xFFFFFFFF), 
				null
				));
				
				export_btn.setClickable(true);
				export_btn.setBackground(new RippleDrawable(
				new ColorStateList(
				new int[][]{new int[]{}},
				new int[]{0xFFD2B6DC}
				),
				new GradientDrawable() {
					public GradientDrawable getIns(int a, int b, int c, int d) {
						this.setCornerRadius(a);
						this.setStroke(b, c);
						this.setColor(d);
						return this;
					}
				}.getIns((int)12, (int)2, 0xFF212121, 0xFFFFFFFF), 
				null
				));
				
				show_tutorial_lay.post(new Runnable() {
					@Override
					public void run() {
						int available = show_tutorial_lay.getWidth();
						
						int textWidth = show_tutorial_txt.getMeasuredWidth();
						int checkWidth = show_tutorial.getMeasuredWidth();
						
						int needed = textWidth + checkWidth
						+ show_tutorial_lay.getPaddingLeft()
						+ show_tutorial_lay.getPaddingRight();
						
						if (needed > available) {
							show_tutorial_lay.setOrientation(LinearLayout.VERTICAL);
							show_tutorial.setPadding(8, 4, 8, 8);
						} else {
							show_tutorial_lay.setOrientation(LinearLayout.HORIZONTAL);
						}
					}
				});
				try{
					progress = (double)settings.get("grid_amount");
					textLevel = (double)settings.get("text_level");
					scanImage = (boolean)settings.get("scan_image");
				}catch(Exception e){
					progress = 2;
					textLevel = 3;
					scanImage = false;
				}
				float scale = textScaleFromLevel((int) textLevel);
				View[] views = new View[] {
					visual_txt,
					grid_amount_txt,
					text_size_txt,
					ux_txt,
					default_scan_txt,
					scan_tip_txt,
					mode_switch,
					mode_switch_txt,
					show_tutorial_txt,
					data_txt,
					import_btn,
					export_btn
				};
				
				for (View v : views) {
					if (v instanceof TextView) {
						applyTextScale((TextView) v, scale);
					}
				}
				grid_amount_txt.setText(getString(R.string.grid_amount).concat(" ".concat(String.valueOf((long)(progress)))));
				text_size_txt.setText(getString(R.string.text_size).concat(" ".concat(String.valueOf((long)(textLevel)))));
				grid_amount_sbar.setProgress((int) progress);
				text_size_sbar.setProgress((int) textLevel);
				mode_switch.setChecked(scanImage);
				grid_amount_sbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar _param1, int _param2, boolean _param3) {
						final int _progressValue = _param2;
						grid_amount_txt.setText(getString(R.string.grid_amount).concat(" ".concat(String.valueOf((long)(_progressValue)))));
						progress = _progressValue;
					} 
					@Override
					public void onStartTrackingTouch(SeekBar _param1) {
						
					}
					
					@Override
					public void onStopTrackingTouch(SeekBar _param2) {
						
					}
				});
				text_size_sbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar _param1, int _param2, boolean _param3) {
						final int _progressValue = _param2;
						text_size_txt.setText(getString(R.string.text_size).concat(" ".concat(String.valueOf((long)(_progressValue)))));
						textLevel = _progressValue;
						float scale = textScaleFromLevel((int) textLevel);
						View[] views = new View[] {
							visual_txt,
							grid_amount_txt,
							text_size_txt,
							ux_txt,
							default_scan_txt,
							scan_tip_txt,
							mode_switch,
							mode_switch_txt,
							show_tutorial_txt,
							data_txt,
							import_btn,
							export_btn
						};
						
						for (View v : views) {
							if (v instanceof TextView) {
								applyTextScale((TextView) v, scale);
							}
						}
					} 
					@Override
					public void onStartTrackingTouch(SeekBar _param1) {
						
					}
					
					@Override
					public void onStopTrackingTouch(SeekBar _param2) {
						
					}
				});
				mode_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
					scanImage = isChecked;
				});
				show_tutorial.setOnCheckedChangeListener((buttonView, isChecked) -> {
					settings.put("main_tutorial", isChecked);
					settings.put("colors_tutorial", isChecked);
					settings.put("settings_tutorial", isChecked);
				});
				import_btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View _view) {
						try {
							if (p != null) p.dismiss();
						} catch (Exception p_e) {}
						
						LayoutInflater p_li = getLayoutInflater();
						View p_pv = p_li.inflate(R.layout.mode_popup, null);
						
						p = new PopupWindow(
						p_pv,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						true
						);
						
						p.setOutsideTouchable(true);
						p.setFocusable(true);
						p.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
						final LinearLayout camera_lay = (LinearLayout) p_pv.findViewById(R.id.camera_lay);
						final LinearLayout image_lay = (LinearLayout) p_pv.findViewById(R.id.image_lay);
						final TextView camera_scanner_txt = (TextView) p_pv.findViewById(R.id.camera_scanner_txt);
						final TextView scan_from_image_txt = (TextView) p_pv.findViewById(R.id.scan_from_image_txt);
						final ImageView camera_img = (ImageView) p_pv.findViewById(R.id.camera_img);
						final ImageView scan_img = (ImageView) p_pv.findViewById(R.id.scan_img);
						applyTextScale(camera_scanner_txt, textScaleFromLevel((int) textLevel));
						applyTextScale(scan_from_image_txt, textScaleFromLevel((int) textLevel));
						camera_lay.setClickable(true);
						camera_lay.setBackground(new RippleDrawable(
						new ColorStateList(
						new int[][]{new int[]{}},
						new int[]{0xFFD2B6DC}
						),
						new GradientDrawable() {
							public GradientDrawable getIns(int a, int b, int c, int d) {
								this.setCornerRadius(a);
								this.setStroke(b, c);
								this.setColor(d);
								return this;
							}
						}.getIns((int)12, (int)2, 0xFF212121, 0xFFFFFFFF), 
						null
						));
						
						image_lay.setClickable(true);
						image_lay.setBackground(new RippleDrawable(
						new ColorStateList(
						new int[][]{new int[]{}},
						new int[]{0xFFD2B6DC}
						),
						new GradientDrawable() {
							public GradientDrawable getIns(int a, int b, int c, int d) {
								this.setCornerRadius(a);
								this.setStroke(b, c);
								this.setColor(d);
								return this;
							}
						}.getIns((int)12, (int)2, 0xFF212121, 0xFFFFFFFF), 
						null
						));
						
						camera_scanner_txt.setText(getString(R.string.from_text));
						scan_from_image_txt.setText(getString(R.string.from_file));
						camera_img.setImageResource(R.drawable.ic_text);
						scan_img.setImageResource(R.drawable.ic_file);
						final Runnable p_dismissAnim = new Runnable() {
							@Override
							public void run() {
								p_pv.animate()
								.alpha(0f)
								.scaleX(0.96f)
								.scaleY(0.96f)
								.setDuration(120)
								.setInterpolator(new android.view.animation.AccelerateInterpolator())
								.withEndAction(new Runnable() {
									@Override
									public void run() {
										try { p.dismiss(); } catch (Exception p_e) {}
									}
								})
								.start();
							}
						};
						
						p_pv.setOnTouchListener(new View.OnTouchListener() {
							@Override
							public boolean onTouch(View p_v, android.view.MotionEvent p_event) {
								if (p_event.getAction() == android.view.MotionEvent.ACTION_OUTSIDE) {
									p_dismissAnim.run();
									return true;
								}
								return false;
							}
						});
						
						p_pv.measure(
						View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
						View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
						);
						int p_popupW = p_pv.getMeasuredWidth();
						int p_popupH = p_pv.getMeasuredHeight();
						
						int[] p_loc = new int[2];
						import_btn.getLocationOnScreen(p_loc);
						int p_anchorX = p_loc[0];
						int p_anchorY = p_loc[1];
						
						android.util.DisplayMetrics p_dm = getResources().getDisplayMetrics();
						int p_screenW = p_dm.widthPixels;
						int p_screenH = p_dm.heightPixels;
						
						int p_x = p_anchorX + import_btn.getWidth() - p_popupW;
						
						int p_yBelow = p_anchorY + import_btn.getHeight();
						int p_yAbove = p_anchorY - p_popupH;
						
						if (p_x < 0) p_x = 0;
						if (p_x + p_popupW > p_screenW)
						p_x = Math.max(0, p_screenW - p_popupW);
						
						int p_y;
						if (p_yBelow + p_popupH <= p_screenH) {
							p_y = p_yBelow;
						} else if (p_yAbove >= 0) {
							p_y = p_yAbove;
						} else {
							p_y = Math.max(0, p_screenH - p_popupH);
						}
						camera_lay.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								p_dismissAnim.run();
								d = new AlertDialog.Builder(MainActivity.this).create();
								LayoutInflater dLI = getLayoutInflater();
								View dCV = (View) dLI.inflate(R.layout.import_dialog, null);
								d.setView(dCV);
								final TextView message_txt = (TextView)
								dCV.findViewById(R.id.message_txt);
								final TextView positive_txt = (TextView)
								dCV.findViewById(R.id.positive_txt);
								final TextView negative_txt = (TextView)
								dCV.findViewById(R.id.negative_txt);
								final EditText import_txt = (EditText)
								dCV.findViewById(R.id.import_txt);
								final LinearLayout dialog_parent = (LinearLayout)
								dCV.findViewById(R.id.parent);
								final LinearLayout buttons_bar = (LinearLayout)
								dCV.findViewById(R.id.buttons_bar);
								float scale = textScaleFromLevel((int) textLevel);
								applyTextScale(import_txt, scale);
								TextView[] views = new TextView[] { message_txt, positive_txt, negative_txt };
								for (TextView tv : views) {
									if (tv != null) applyTextScale(tv, scale);
								}
								positive_txt.setClickable(true);
								positive_txt.setBackground(new RippleDrawable(
								new ColorStateList(
								new int[][]{new int[]{}},
								new int[]{0xFFF2EAF5}
								),
								new GradientDrawable() {
									public GradientDrawable getIns(int a, int b, int c, int d) {
										this.setCornerRadius(a);
										this.setStroke(b, c);
										this.setColor(d);
										return this;
									}
								}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFD2B6DC), 
								null
								));
								
								negative_txt.setClickable(true);
								negative_txt.setBackground(new RippleDrawable(
								new ColorStateList(
								new int[][]{new int[]{}},
								new int[]{0xFFF2EAF5}
								),
								new GradientDrawable() {
									public GradientDrawable getIns(int a, int b, int c, int d) {
										this.setCornerRadius(a);
										this.setStroke(b, c);
										this.setColor(d);
										return this;
									}
								}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFD2B6DC), 
								null
								));
								
								dialog_parent.setClickable(true);
								final float dialog_parent_rTL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
								final float dialog_parent_rTR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
								final float dialog_parent_rBR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
								final float dialog_parent_rBL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
								final int dialog_parent_strokePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 2, getResources().getDisplayMetrics());
								final GradientDrawable dialog_parent_bg = new GradientDrawable();
								dialog_parent_bg.setColor(0xFFFFFFFF);
								dialog_parent_bg.setCornerRadii(new float[]{dialog_parent_rTL,dialog_parent_rTL,dialog_parent_rTR,dialog_parent_rTR,dialog_parent_rBR,dialog_parent_rBR,dialog_parent_rBL,dialog_parent_rBL});
								dialog_parent_bg.setStroke(dialog_parent_strokePx, 0xFF424242);
								dialog_parent.setBackground(dialog_parent_bg);
								buttons_bar.setClickable(true);
								
								final float buttons_bar_rTL = TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP,
								(float) 0,
								getResources().getDisplayMetrics()
								);
								
								final float buttons_bar_rTR = TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP,
								(float) 0,
								getResources().getDisplayMetrics()
								);
								
								final float buttons_bar_rBR = TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP,
								(float) 12,
								getResources().getDisplayMetrics()
								);
								
								final float buttons_bar_rBL = TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP,
								(float) 12,
								getResources().getDisplayMetrics()
								);
								
								buttons_bar.setBackground(new ShapeDrawable(new RoundRectShape(
								new float[]{
									buttons_bar_rTL, buttons_bar_rTL,
									buttons_bar_rTR, buttons_bar_rTR,
									buttons_bar_rBR, buttons_bar_rBR,
									buttons_bar_rBL, buttons_bar_rBL
								},
								null,
								null
								)) {{
										getPaint().setColor(0xFFD2B6DC);
									}});
								d.setCancelable(true);
								d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
								message_txt.setText(getString(R.string.import_warning));
								positive_txt.setText(getString(R.string.import_positive));
								negative_txt.setText(getString(R.string.cancel));
								positive_txt.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View _view) {
										backup = new Gson().toJson(cards_list_all);
										try{
											card_prefs.edit().putString("cards", import_txt.getText().toString()).commit();
											ArrayList<HashMap<String, Object>> tmp = new ArrayList<>();
											
											if (card_prefs.contains("cards")) {
												tmp = new Gson().fromJson(
												card_prefs.getString("cards", ""),
												new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType()
												);
												if (tmp == null) tmp = new ArrayList<>();
											}
											
											tmp = sanitizeTree(tmp);
											
											folderIdStack.clear();
											folderNameStack.clear();
											inFolder = false;
											folderPath = "";
											
											cards_list.clear();
											cards_list_all.clear();
											cards_list.addAll(tmp);
											cards_list_all.addAll(tmp);
											
											if (cards_rec.getAdapter() == null) {
												cardsAdapter = new Cards_recAdapter(cards_list);
												cards_rec.setAdapter(cardsAdapter);
											} else {
												cardsAdapter.notifyDataSetChanged();
											}
											SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.import_success));
										}catch(Exception e){
											card_prefs.edit().putString("cards", backup).commit();
											ArrayList<HashMap<String, Object>> tmp = new ArrayList<>();
											
											if (card_prefs.contains("cards")) {
												tmp = new Gson().fromJson(
												card_prefs.getString("cards", ""),
												new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType()
												);
												if (tmp == null) tmp = new ArrayList<>();
											}
											
											tmp = sanitizeTree(tmp);
											
											folderIdStack.clear();
											folderNameStack.clear();
											inFolder = false;
											folderPath = "";
											
											cards_list.clear();
											cards_list_all.clear();
											cards_list.addAll(tmp);
											cards_list_all.addAll(tmp);
											
											if (cards_rec.getAdapter() == null) {
												cardsAdapter = new Cards_recAdapter(cards_list);
												cards_rec.setAdapter(cardsAdapter);
											} else {
												cardsAdapter.notifyDataSetChanged();
											}
											SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.import_fail).concat(" ".concat(e.getMessage())));
										}
										applySortFilter(
										search_txt.getText().toString(),
										loadSortTypeId(),
										loadOrderId(),
										loadFilterId()
										);
										d.dismiss();
									}
								});
								negative_txt.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View _view) {
										d.dismiss();
									}
								});
								d.show();
							}
						});
						image_lay.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								p_dismissAnim.run();
								i.setAction(Intent.ACTION_OPEN_DOCUMENT);
								i.addCategory(Intent.CATEGORY_OPENABLE);
								i.setType("application/json");
								i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/*", "*/*"});
								startActivityForResult(i, REQ_IMPORT_JSON);
							}
						});
						p_pv.setAlpha(0f);
						p_pv.setScaleX(0.94f);
						p_pv.setScaleY(0.94f);
						
						p.showAtLocation(
						import_btn,
						android.view.Gravity.TOP | android.view.Gravity.START,
						p_x,
						p_y
						);
						
						p_pv.setPivotX(p_pv.getMeasuredWidth());
						p_pv.setPivotY(0f);
						
						p_pv.animate()
						.alpha(1f)
						.scaleX(1f)
						.scaleY(1f)
						.setDuration(140)
						.setInterpolator(new android.view.animation.DecelerateInterpolator())
						.start();
					}
				});
				export_btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View _view) {
						try {
							if (p != null) p.dismiss();
						} catch (Exception p_e) {}
						
						LayoutInflater p_li = getLayoutInflater();
						View p_pv = p_li.inflate(R.layout.mode_popup, null);
						
						p = new PopupWindow(
						p_pv,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						true
						);
						
						p.setOutsideTouchable(true);
						p.setFocusable(true);
						p.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
						final LinearLayout camera_lay = (LinearLayout) p_pv.findViewById(R.id.camera_lay);
						final LinearLayout image_lay = (LinearLayout) p_pv.findViewById(R.id.image_lay);
						final TextView camera_scanner_txt = (TextView) p_pv.findViewById(R.id.camera_scanner_txt);
						final TextView scan_from_image_txt = (TextView) p_pv.findViewById(R.id.scan_from_image_txt);
						final ImageView camera_img = (ImageView) p_pv.findViewById(R.id.camera_img);
						final ImageView scan_img = (ImageView) p_pv.findViewById(R.id.scan_img);
						applyTextScale(camera_scanner_txt, textScaleFromLevel((int) textLevel));
						applyTextScale(scan_from_image_txt, textScaleFromLevel((int) textLevel));
						camera_lay.setClickable(true);
						camera_lay.setBackground(new RippleDrawable(
						new ColorStateList(
						new int[][]{new int[]{}},
						new int[]{0xFFD2B6DC}
						),
						new GradientDrawable() {
							public GradientDrawable getIns(int a, int b, int c, int d) {
								this.setCornerRadius(a);
								this.setStroke(b, c);
								this.setColor(d);
								return this;
							}
						}.getIns((int)12, (int)2, 0xFF212121, 0xFFFFFFFF), 
						null
						));
						
						image_lay.setClickable(true);
						image_lay.setBackground(new RippleDrawable(
						new ColorStateList(
						new int[][]{new int[]{}},
						new int[]{0xFFD2B6DC}
						),
						new GradientDrawable() {
							public GradientDrawable getIns(int a, int b, int c, int d) {
								this.setCornerRadius(a);
								this.setStroke(b, c);
								this.setColor(d);
								return this;
							}
						}.getIns((int)12, (int)2, 0xFF212121, 0xFFFFFFFF), 
						null
						));
						
						camera_scanner_txt.setText(getString(R.string.copy_as_text));
						scan_from_image_txt.setText(getString(R.string.save_as_file));
						camera_img.setImageResource(R.drawable.ic_copy);
						scan_img.setImageResource(R.drawable.ic_to_file);
						final Runnable p_dismissAnim = new Runnable() {
							@Override
							public void run() {
								p_pv.animate()
								.alpha(0f)
								.scaleX(0.96f)
								.scaleY(0.96f)
								.setDuration(120)
								.setInterpolator(new android.view.animation.AccelerateInterpolator())
								.withEndAction(new Runnable() {
									@Override
									public void run() {
										try { p.dismiss(); } catch (Exception p_e) {}
									}
								})
								.start();
							}
						};
						
						p_pv.setOnTouchListener(new View.OnTouchListener() {
							@Override
							public boolean onTouch(View p_v, android.view.MotionEvent p_event) {
								if (p_event.getAction() == android.view.MotionEvent.ACTION_OUTSIDE) {
									p_dismissAnim.run();
									return true;
								}
								return false;
							}
						});
						
						p_pv.measure(
						View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
						View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
						);
						int p_popupW = p_pv.getMeasuredWidth();
						int p_popupH = p_pv.getMeasuredHeight();
						
						int[] p_loc = new int[2];
						export_btn.getLocationOnScreen(p_loc);
						int p_anchorX = p_loc[0];
						int p_anchorY = p_loc[1];
						
						android.util.DisplayMetrics p_dm = getResources().getDisplayMetrics();
						int p_screenW = p_dm.widthPixels;
						int p_screenH = p_dm.heightPixels;
						
						int p_x = p_anchorX + export_btn.getWidth() - p_popupW;
						
						int p_yBelow = p_anchorY + export_btn.getHeight();
						int p_yAbove = p_anchorY - p_popupH;
						
						if (p_x < 0) p_x = 0;
						if (p_x + p_popupW > p_screenW)
						p_x = Math.max(0, p_screenW - p_popupW);
						
						int p_y;
						if (p_yBelow + p_popupH <= p_screenH) {
							p_y = p_yBelow;
						} else if (p_yAbove >= 0) {
							p_y = p_yAbove;
						} else {
							p_y = Math.max(0, p_screenH - p_popupH);
						}
						camera_lay.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", new Gson().toJson(cards_list_all)));
								SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.text_copied));
								p_dismissAnim.run();
							}
						});
						image_lay.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								p_dismissAnim.run();
								String fileName = "cards_backup_" + System.currentTimeMillis() + ".json";
								i.setAction(Intent.ACTION_CREATE_DOCUMENT);
								i.addCategory(Intent.CATEGORY_OPENABLE);
								i.setType("application/json");
								i.putExtra(Intent.EXTRA_TITLE, fileName);
								startActivityForResult(i, REQ_EXPORT_JSON);
							}
						});
						p_pv.setAlpha(0f);
						p_pv.setScaleX(0.94f);
						p_pv.setScaleY(0.94f);
						
						p.showAtLocation(
						export_btn,
						android.view.Gravity.TOP | android.view.Gravity.START,
						p_x,
						p_y
						);
						
						p_pv.setPivotX(p_pv.getMeasuredWidth());
						p_pv.setPivotY(0f);
						
						p_pv.animate()
						.alpha(1f)
						.scaleX(1f)
						.scaleY(1f)
						.setDuration(140)
						.setInterpolator(new android.view.animation.DecelerateInterpolator())
						.start();
					}
				});
				bottomShii.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						applyTextScale(search_txt, textScaleFromLevel((int) textLevel));
						applyTextScale(no_items_txt, textScaleFromLevel((int) textLevel));
						search_bar.post(new Runnable() {
							@Override
							public void run() {
								int searchH = search_bar.getHeight();
								
								int innerH = searchH
								- search_bar.getPaddingTop()
								- search_bar.getPaddingBottom();
								
								if (innerH < 0) innerH = 0;
								
								ViewGroup.LayoutParams flp = filter_img.getLayoutParams();
								flp.width = innerH;
								flp.height = innerH;
								filter_img.setLayoutParams(flp);
								
								ViewGroup.LayoutParams slp = settings_img.getLayoutParams();
								slp.width = innerH;
								slp.height = innerH;
								settings_img.setLayoutParams(slp);
								
								ViewGroup.LayoutParams fblp = filter_bar.getLayoutParams();
								fblp.height = searchH;
								ViewGroup.LayoutParams sblp = settings_bar.getLayoutParams();
								sblp.height = searchH;
								filter_bar.setLayoutParams(fblp);
								settings_bar.setLayoutParams(sblp);
								
							}
						});
						cardsAdapter.notifyDataSetChanged();
						settings.put("grid_amount", (double)(progress));
						settings.put("text_level", (double)(textLevel));
						settings.put("scan_image", scanImage);
						card_prefs.edit().putString("settings", new Gson().toJson(settings)).commit();
						cards_rec.removeItemDecorationAt(0);
						final StaggeredGridLayoutManager cards_rec_layoutManager =
						new StaggeredGridLayoutManager((int) progress, StaggeredGridLayoutManager.VERTICAL);
						
						cards_rec_layoutManager.setGapStrategy(
						StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
						);
						cards_rec.setLayoutManager(cards_rec_layoutManager);
						
						final Context cards_rec_ctx = cards_rec.getContext();
						
						final int cards_rec_hSpacingPx = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 8,
						cards_rec_ctx.getResources().getDisplayMetrics()
						);
						
						final int cards_rec_vSpacingPx = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 2,
						cards_rec_ctx.getResources().getDisplayMetrics()
						);
						
						cards_rec.addItemDecoration(new RecyclerView.ItemDecoration() {
							@Override
							public void getItemOffsets(
							Rect outRect,
							View view,
							RecyclerView parent,
							RecyclerView.State state
							) {
								
								int cards_rec_position = parent.getChildAdapterPosition(view);
								if (cards_rec_position == RecyclerView.NO_POSITION) return;
								
								RecyclerView.LayoutManager cards_rec_lm = parent.getLayoutManager();
								if (!(cards_rec_lm instanceof StaggeredGridLayoutManager)) return;
								
								StaggeredGridLayoutManager.LayoutParams cards_rec_lp =
								(StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
								
								int cards_rec_spanCount =
								((StaggeredGridLayoutManager) cards_rec_lm).getSpanCount();
								int cards_rec_spanIndex = cards_rec_lp.getSpanIndex();
								
								outRect.left  =
								(cards_rec_spanIndex * cards_rec_hSpacingPx) / cards_rec_spanCount;
								outRect.right =
								cards_rec_hSpacingPx
								- ((cards_rec_spanIndex + 1) * cards_rec_hSpacingPx) / cards_rec_spanCount;
								
								if (cards_rec_position >= cards_rec_spanCount) {
									outRect.top = cards_rec_vSpacingPx;
								}
							}
						});
					}
				});
				bottomShii.show();
				if ((boolean)settings.get("settings_tutorial")) {
					View import_btn_targetView = bottomShii.findViewById(R.id.import_btn);
					
					TapTarget import_btn_tapTarget = TapTarget.forView(import_btn_targetView, getString(R.string.import_title), getString(R.string.import_desc))
					.outerCircleColorInt(0xFFD2B6DC)
					.targetCircleColorInt(0xFFC3A2CF)
					.titleTextColorInt(0xFFFFFFFF)
					.descriptionTextColorInt(0xFFFFFFFF)
					.descriptionTextAlpha(1f)
					.cancelable(true)
					.transparentTarget(true)
					.drawShadow(true)
					.id(1);
					
					TapTargetView.showFor(bottomShii, import_btn_tapTarget, new TapTargetView.Listener() {
						@Override
						public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
							super.onTargetDismissed(view, userInitiated);
							View export_btn_targetView = bottomShii.findViewById(R.id.export_btn);
							
							TapTarget export_btn_tapTarget = TapTarget.forView(export_btn_targetView, getString(R.string.export_title), getString(R.string.export_desc))
							.outerCircleColorInt(0xFFD2B6DC)
							.targetCircleColorInt(0xFFC3A2CF)
							.titleTextColorInt(0xFFFFFFFF)
							.descriptionTextColorInt(0xFFFFFFFF)
							.descriptionTextAlpha(1f)
							.cancelable(true)
							.transparentTarget(true)
							.drawShadow(true)
							.id(1);
							
							TapTargetView.showFor(bottomShii, export_btn_tapTarget, new TapTargetView.Listener() {
								@Override
								public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
									super.onTargetDismissed(view, userInitiated);
									settings.put("settings_tutorial", false);
									card_prefs.edit().putString("settings", new Gson().toJson(settings)).commit();
								}
							});
						}
					});
				}
			}
		});
		
		search_txt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				final String _charSeq = _param1.toString();
				String q = _charSeq.toString();
				
				applySortFilter(
				q,
				loadSortTypeId(),
				loadOrderId(),
				loadFilterId()
				);
			}
			
			@Override
			public void beforeTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				
			}
			
			@Override
			public void afterTextChanged(Editable _param1) {
				
			}
		});
		
		_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				cardSaveName = "";
				cardSaveType = "";
				cardSaveCode = "";
				selectedGradStyle = "lr";
				favorite = false;
				folder = false;
				bottomShii = new com.google.android.material.bottomsheet.BottomSheetDialog(MainActivity.this);
				View bottomShiiV;
				bottomShiiV = getLayoutInflater().inflate(R.layout.add_new_dialog,null );
				bottomShii.setContentView(bottomShiiV);
				bottomShii.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
				final TextInputEditText card_name_txt = (TextInputEditText) bottomShiiV.findViewById(R.id.card_name_txt);
				final TextView scan_btn = (TextView) bottomShiiV.findViewById(R.id.scan_btn);
				final TextView save_btn = (TextView) bottomShiiV.findViewById(R.id.save_btn);
				final TextView folder_txt = (TextView) bottomShiiV.findViewById(R.id.folder_txt);
				final RecyclerView colors_rec = (RecyclerView) bottomShiiV.findViewById(R.id.colors_rec);
				final ImageView fav_btn = (ImageView) bottomShiiV.findViewById(R.id.fav_btn);
				final ImageView folder_btn = (ImageView) bottomShiiV.findViewById(R.id.folder_btn);
				code_txt = bottomShiiV.findViewById(R.id.code_txt);
				float scale = textScaleFromLevel((int) textLevel);
				applyTextScale(card_name_txt, scale);
				applyTextScale(scan_btn, scale);
				applyTextScale(save_btn, scale);
				applyTextScale(folder_txt, scale);
				applyTextScale(code_txt, scale);
				bottomShii.setCancelable(true);
				if (inFolder) {
					folder_txt.setText(getString(R.string.folder).concat(" ".concat(folderPath)));
				} else {
					folder_txt.setVisibility(View.GONE);
				}
				scan_btn.setClickable(true);
				scan_btn.setBackground(new RippleDrawable(
				new ColorStateList(
				new int[][]{new int[]{}},
				new int[]{0xFFD2B6DC}
				),
				new GradientDrawable() {
					public GradientDrawable getIns(int a, int b, int c, int d) {
						this.setCornerRadius(a);
						this.setStroke(b, c);
						this.setColor(d);
						return this;
					}
				}.getIns((int)12, (int)2, 0xFF212121, 0xFFFFFFFF), 
				null
				));
				
				save_btn.setClickable(true);
				save_btn.setBackground(new RippleDrawable(
				new ColorStateList(
				new int[][]{new int[]{}},
				new int[]{0xFFD2B6DC}
				),
				new GradientDrawable() {
					public GradientDrawable getIns(int a, int b, int c, int d) {
						this.setCornerRadius(a);
						this.setStroke(b, c);
						this.setColor(d);
						return this;
					}
				}.getIns((int)12, (int)2, 0xFF212121, 0xFFFFFFFF), 
				null
				));
				
				scan_btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View _view) {
						if (scanImage) {
							pickImage(new OnImagePicked() {
								@Override
								public void onPicked(Uri uri) {
									new Thread(new Runnable() {
										@Override
										public void run() {
											try {
												android.graphics.Bitmap bitmap = loadBitmapFromUri(uri, 2048);
												
												com.google.zxing.Result result = decodeWithZxing(bitmap);
												
												final String text = result.getText();
												final String type = result.getBarcodeFormat().toString();
												
												runOnUiThread(new Runnable() {
													@Override
													public void run() {
														code_txt.setText(getString(R.string.card_code).concat(" ".concat(text)));
														cardSaveCode = text;
														cardSaveType = type;
													}
												});
												
											} catch (final Exception e) {
												runOnUiThread(new Runnable() {
													@Override
													public void run() {
														SketchwareUtil.showMessage(getApplicationContext(), e.toString());
													}
												});
											}
										}
									}).start();
								}
							}, new OnCancelled() {
								@Override
								public void onCancelled() {
									
								}
							});
						} else {
							openScannerOrRequestPermission();
						}
					}
				});
				scan_btn.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View _view) {
						
						LayoutInflater li = getLayoutInflater();
						View pv = li.inflate(R.layout.mode_popup, null);
						
						final LinearLayout camera_lay = (LinearLayout) pv.findViewById(R.id.camera_lay);
						final LinearLayout image_lay = (LinearLayout) pv.findViewById(R.id.image_lay);
						final TextView camera_scanner_txt = (TextView) pv.findViewById(R.id.camera_scanner_txt);
						final TextView scan_from_image_txt = (TextView) pv.findViewById(R.id.scan_from_image_txt);
						
						final PopupWindow pw = new PopupWindow(
						pv,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						true
						);
						
						pw.setOutsideTouchable(true);
						pw.setFocusable(true);
						pw.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
						
						final Runnable dismissAnim = new Runnable() {
							@Override
							public void run() {
								pv.animate()
								.alpha(0f)
								.scaleX(0.96f)
								.scaleY(0.96f)
								.setDuration(120)
								.setInterpolator(new android.view.animation.AccelerateInterpolator())
								.withEndAction(new Runnable() {
									@Override
									public void run() {
										try { pw.dismiss(); } catch (Exception e) {}
									}
								})
								.start();
							}
						};
						
						pv.setOnTouchListener(new View.OnTouchListener() {
							@Override
							public boolean onTouch(View v, android.view.MotionEvent event) {
								if (event.getAction() == android.view.MotionEvent.ACTION_OUTSIDE) {
									dismissAnim.run();
									return true;
								}
								return false;
							}
						});
						camera_lay.setClickable(true);
						camera_lay.setBackground(new RippleDrawable(
						new ColorStateList(
						new int[][]{new int[]{}},
						new int[]{0xFFD2B6DC}
						),
						new GradientDrawable() {
							public GradientDrawable getIns(int a, int b, int c, int d) {
								this.setCornerRadius(a);
								this.setStroke(b, c);
								this.setColor(d);
								return this;
							}
						}.getIns((int)12, (int)2, 0xFF212121, 0xFFFFFFFF), 
						null
						));
						
						image_lay.setClickable(true);
						image_lay.setBackground(new RippleDrawable(
						new ColorStateList(
						new int[][]{new int[]{}},
						new int[]{0xFFD2B6DC}
						),
						new GradientDrawable() {
							public GradientDrawable getIns(int a, int b, int c, int d) {
								this.setCornerRadius(a);
								this.setStroke(b, c);
								this.setColor(d);
								return this;
							}
						}.getIns((int)12, (int)2, 0xFF212121, 0xFFFFFFFF), 
						null
						));
						
						applyTextScale(camera_scanner_txt, textScaleFromLevel((int) textLevel));
						applyTextScale(scan_from_image_txt, textScaleFromLevel((int) textLevel));
						camera_lay.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _v) {
								dismissAnim.run();
								openScannerOrRequestPermission();
							}
						});
						
						image_lay.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _v) {
								dismissAnim.run();
								pickImage(new OnImagePicked() {
									@Override
									public void onPicked(Uri uri) {
										new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													android.graphics.Bitmap bitmap = loadBitmapFromUri(uri, 2048);
													
													com.google.zxing.Result result = decodeWithZxing(bitmap);
													
													final String text = result.getText();
													final String type = result.getBarcodeFormat().toString();
													
													runOnUiThread(new Runnable() {
														@Override
														public void run() {
															code_txt.setText(getString(R.string.card_code).concat(" ".concat(text)));
															cardSaveCode = text;
															cardSaveType = type;
														}
													});
													
												} catch (final Exception e) {
													runOnUiThread(new Runnable() {
														@Override
														public void run() {
															SketchwareUtil.showMessage(getApplicationContext(), e.toString());
														}
													});
												}
											}
										}).start();
									}
								}, new OnCancelled() {
									@Override
									public void onCancelled() {
										
									}
								});
							}
						});
						
						pv.measure(
						View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
						View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
						);
						int popupW = pv.getMeasuredWidth();
						int popupH = pv.getMeasuredHeight();
						
						int[] loc = new int[2];
						scan_btn.getLocationOnScreen(loc);
						int anchorX = loc[0];
						int anchorY = loc[1];
						
						android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
						int screenW = dm.widthPixels;
						int screenH = dm.heightPixels;
						
						int x = anchorX + scan_btn.getWidth() - popupW;
						
						int yBelow = anchorY + scan_btn.getHeight();
						int yAbove = anchorY - popupH;
						
						if (x < 0) x = 0;
						if (x + popupW > screenW) x = Math.max(0, screenW - popupW);
						
						int y;
						if (yBelow + popupH <= screenH) {
							y = yBelow;
						} else if (yAbove >= 0) {
							y = yAbove;
						} else {
							y = Math.max(0, screenH - popupH);
						}
						
						pv.setAlpha(0f);
						pv.setScaleX(0.94f);
						pv.setScaleY(0.94f);
						
						pw.showAtLocation(scan_btn, android.view.Gravity.TOP | android.view.Gravity.START, x, y);
						
						pv.setPivotX(pv.getMeasuredWidth());
						pv.setPivotY(0f);
						
						pv.animate()
						.alpha(1f)
						.scaleX(1f)
						.scaleY(1f)
						.setDuration(140)
						.setInterpolator(new android.view.animation.DecelerateInterpolator())
						.start();
						return true;
					}
				});
				save_btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View _view) {
						cardSaveName = card_name_txt.getText().toString();
						if ((cardSaveName.isEmpty() || (cardSaveType.isEmpty() || cardSaveCode.isEmpty())) && (!folder && !debug)) {
							SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.empty_err));
						} else {
							ArrayList<Integer> picked = new ArrayList<>();
							for (HashMap<String, Object> m : colors_list) {
								Object v = m.get("color");
								if (!(v instanceof Integer)) continue;
								picked.add((Integer) v);
							}
							cards = new HashMap<>();
							cards.put("name", cardSaveName);
							if (folder) {
								cards.put("folder", true);
								cards.put("data", new ArrayList<HashMap<String,Object>>());
							} else {
								if (debug) {
									cards.put("type", "debug_type");
									cards.put("code", "debug_code");
								} else {
									cards.put("type", cardSaveType);
									cards.put("code", cardSaveCode);
								}
								cards.put("folder", false);
							}
							cards.put("grad_style", selectedGradStyle);
							cards.put("favorite", favorite);
							cards.put("used", (int)(0));
							cards.put("colors", new Gson().toJson(picked));
							long newId = card_prefs.getLong("lastId", -1) + 1;
							cards.put("id", String.valueOf(newId));
							card_prefs.edit().putLong("lastId", newId).commit();
							cards_list.add(cards);
							if (inFolder) {
								ArrayList<HashMap<String, Object>> masterContainer =
								resolveContainerList(cards_list_all, folderIdStack);
								masterContainer.add(cards);
							} else {
								cards_list_all.add(cards);
							}
							card_prefs.edit().putString("cards", new Gson().toJson(cards_list_all)).commit();
							applySortFilter(
							search_txt.getText().toString(),
							loadSortTypeId(),
							loadOrderId(),
							loadFilterId()
							);
							bottomShii.dismiss();
						}
					}
				});
				fav_btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View _view) {
						toggleFavorite(fav_btn, true, new ArrayList<HashMap<String, Object>>(), 0);
					}
				});
				folder_btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View _view) {
						if (folder_btn.isSelected()) {
							folder = false;
							folder_btn.setSelected(false);
							o.cancel();
							o.setTarget(code_txt);
							o.setPropertyName("alpha");
							o.setFloatValues((float)(0), (float)(1));
							o.setDuration((int)(250));
							o.setInterpolator(new LinearInterpolator());
							o.start();
							scan_btn.setClickable(true);
							scan_btn.setBackground(new RippleDrawable(
							new ColorStateList(
							new int[][]{new int[]{}},
							new int[]{0xFFD2B6DC}
							),
							new GradientDrawable() {
								public GradientDrawable getIns(int a, int b, int c, int d) {
									this.setCornerRadius(a);
									this.setStroke(b, c);
									this.setColor(d);
									return this;
								}
							}.getIns((int)12, (int)2, 0xFF424242, 0xFFFFFFFF), 
							null
							));
							
						} else {
							folder = true;
							folder_btn.setSelected(true);
							folder_btn.animate().cancel();
							folder_btn.setScaleX(1f);
							folder_btn.setScaleY(1f);
							
							folder_btn.animate()
							.scaleX(1.18f)
							.scaleY(1.18f)
							.setDuration(90)
							.setInterpolator(new android.view.animation.OvershootInterpolator())
							.withEndAction(new Runnable() {
								@Override
								public void run() {
									folder_btn.animate()
									.scaleX(1f)
									.scaleY(1f)
									.setDuration(120)
									.setInterpolator(new android.view.animation.DecelerateInterpolator())
									.start();
								}
							})
							.start();
							o.cancel();
							o.setTarget(code_txt);
							o.setPropertyName("alpha");
							o.setFloatValues((float)(1), (float)(0));
							o.setDuration((int)(250));
							o.setInterpolator(new LinearInterpolator());
							o.start();
							scan_btn.setClickable(false);
							scan_btn.setBackground(new RippleDrawable(
							new ColorStateList(
							new int[][]{new int[]{}},
							new int[]{Color.TRANSPARENT}
							),
							new GradientDrawable() {
								public GradientDrawable getIns(int a, int b, int c, int d) {
									this.setCornerRadius(a);
									this.setStroke(b, c);
									this.setColor(d);
									return this;
								}
							}.getIns((int)12, (int)2, 0xFF212121, 0xFF9E9E9E), 
							null
							));
						}
					}
				});
				colors_rec.setLayoutManager(
				new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false)
				);
				final int spacingPx = (int) (8 * getResources().getDisplayMetrics().density);
				colors_rec.addItemDecoration(new RecyclerView.ItemDecoration() {
					@Override
					public void getItemOffsets(
					Rect outRect,
					View view,
					RecyclerView parent,
					RecyclerView.State state
					) {
						int position = parent.getChildAdapterPosition(view);
						if (position == RecyclerView.NO_POSITION) return;
						
						if (position < parent.getAdapter().getItemCount() - 1) {
							outRect.right = spacingPx;
						}
					}
				});
				colors_list.clear();
				colors = new HashMap<>();
				colors.put("color", (int)(0xFF008DCD));
				colors_list.add(colors);
				colors = new HashMap<>();
				colors.put("color", "plus");
				colors_list.add(colors);
				colors = new HashMap<>();
				colors.put("color", "settings");
				colors_list.add(colors);
				colorsAdapter = new Colors_recAdapter(colors_list);
				colors_rec.setAdapter(colorsAdapter);
				bottomShii.show();
				tapTargetRoot = (ViewGroup) bottomShii.getWindow().getDecorView();
				if ((boolean)settings.get("colors_tutorial")) {
					colors_rec.post(new Runnable() {
						@Override
						public void run() {
							showColorsTutorialStep(bottomShii, colors_rec, 0);
						}
					});
				}
			}
		});
	}
	
	private void initializeLogic() {
		debug = true;
		if (card_prefs.contains("settings")) {
			settings = new Gson().fromJson(card_prefs.getString("settings", ""), new TypeToken<HashMap<String, Object>>(){}.getType());
			final StaggeredGridLayoutManager cards_rec_layoutManager =
			new StaggeredGridLayoutManager((int) (double)settings.get("grid_amount"), StaggeredGridLayoutManager.VERTICAL);
			
			cards_rec_layoutManager.setGapStrategy(
			StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
			);
			cards_rec.setLayoutManager(cards_rec_layoutManager);
			
			final Context cards_rec_ctx = cards_rec.getContext();
			
			final int cards_rec_hSpacingPx = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			(float) 8,
			cards_rec_ctx.getResources().getDisplayMetrics()
			);
			
			final int cards_rec_vSpacingPx = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			(float) 2,
			cards_rec_ctx.getResources().getDisplayMetrics()
			);
			
			cards_rec.addItemDecoration(new RecyclerView.ItemDecoration() {
				@Override
				public void getItemOffsets(
				Rect outRect,
				View view,
				RecyclerView parent,
				RecyclerView.State state
				) {
					
					int cards_rec_position = parent.getChildAdapterPosition(view);
					if (cards_rec_position == RecyclerView.NO_POSITION) return;
					
					RecyclerView.LayoutManager cards_rec_lm = parent.getLayoutManager();
					if (!(cards_rec_lm instanceof StaggeredGridLayoutManager)) return;
					
					StaggeredGridLayoutManager.LayoutParams cards_rec_lp =
					(StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
					
					int cards_rec_spanCount =
					((StaggeredGridLayoutManager) cards_rec_lm).getSpanCount();
					int cards_rec_spanIndex = cards_rec_lp.getSpanIndex();
					
					outRect.left  =
					(cards_rec_spanIndex * cards_rec_hSpacingPx) / cards_rec_spanCount;
					outRect.right =
					cards_rec_hSpacingPx
					- ((cards_rec_spanIndex + 1) * cards_rec_hSpacingPx) / cards_rec_spanCount;
					
					if (cards_rec_position >= cards_rec_spanCount) {
						outRect.top = cards_rec_vSpacingPx;
					}
				}
			});
		} else {
			settings = new HashMap<>();
			settings.put("grid_amount", (double)(2));
			settings.put("text_level", (double)(3));
			settings.put("scan_image", false);
			settings.put("main_tutorial", true);
			settings.put("colors_tutorial", true);
			settings.put("settings_tutorial", true);
			card_prefs.edit().putString("settings", new Gson().toJson(settings)).commit();
			final StaggeredGridLayoutManager cards_rec_layoutManager =
			new StaggeredGridLayoutManager((int) 2, StaggeredGridLayoutManager.VERTICAL);
			
			cards_rec_layoutManager.setGapStrategy(
			StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
			);
			cards_rec.setLayoutManager(cards_rec_layoutManager);
			
			final Context cards_rec_ctx = cards_rec.getContext();
			
			final int cards_rec_hSpacingPx = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			(float) 8,
			cards_rec_ctx.getResources().getDisplayMetrics()
			);
			
			final int cards_rec_vSpacingPx = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			(float) 2,
			cards_rec_ctx.getResources().getDisplayMetrics()
			);
			
			cards_rec.addItemDecoration(new RecyclerView.ItemDecoration() {
				@Override
				public void getItemOffsets(
				Rect outRect,
				View view,
				RecyclerView parent,
				RecyclerView.State state
				) {
					
					int cards_rec_position = parent.getChildAdapterPosition(view);
					if (cards_rec_position == RecyclerView.NO_POSITION) return;
					
					RecyclerView.LayoutManager cards_rec_lm = parent.getLayoutManager();
					if (!(cards_rec_lm instanceof StaggeredGridLayoutManager)) return;
					
					StaggeredGridLayoutManager.LayoutParams cards_rec_lp =
					(StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
					
					int cards_rec_spanCount =
					((StaggeredGridLayoutManager) cards_rec_lm).getSpanCount();
					int cards_rec_spanIndex = cards_rec_lp.getSpanIndex();
					
					outRect.left  =
					(cards_rec_spanIndex * cards_rec_hSpacingPx) / cards_rec_spanCount;
					outRect.right =
					cards_rec_hSpacingPx
					- ((cards_rec_spanIndex + 1) * cards_rec_hSpacingPx) / cards_rec_spanCount;
					
					if (cards_rec_position >= cards_rec_spanCount) {
						outRect.top = cards_rec_vSpacingPx;
					}
				}
			});
		}
		try{
			textLevel = (double)settings.get("text_level");
			scanImage = (boolean)settings.get("scan_image");
		}catch(Exception e){
			textLevel = 3;
			scanImage = false;
		}
		applyTextScale(search_txt, textScaleFromLevel((int) textLevel));
		search_bar.post(new Runnable() {
			@Override
			public void run() {
				int searchH = search_bar.getHeight();
				
				int innerH = searchH
				- search_bar.getPaddingTop()
				- search_bar.getPaddingBottom();
				
				if (innerH < 0) innerH = 0;
				
				ViewGroup.LayoutParams flp = filter_img.getLayoutParams();
				flp.width = innerH;
				flp.height = innerH;
				filter_img.setLayoutParams(flp);
				
				ViewGroup.LayoutParams slp = settings_img.getLayoutParams();
				slp.width = innerH;
				slp.height = innerH;
				settings_img.setLayoutParams(slp);
				
				ViewGroup.LayoutParams fblp = filter_bar.getLayoutParams();
				fblp.height = searchH;
				ViewGroup.LayoutParams sblp = settings_bar.getLayoutParams();
				sblp.height = searchH;
				filter_bar.setLayoutParams(fblp);
				settings_bar.setLayoutParams(sblp);
				
			}
		});
		filter_bar.setClickable(true);
		filter_bar.setBackground(new RippleDrawable(
		new ColorStateList(
		new int[][]{new int[]{}},
		new int[]{0xFFD2B6DC}
		),
		new GradientDrawable() {
			public GradientDrawable getIns(int a, int b, int c, int d) {
				this.setCornerRadius(a);
				this.setStroke(b, c);
				this.setColor(d);
				return this;
			}
		}.getIns((int)12, (int)2, 0xFF424242, 0xFFFFFFFF), 
		null
		));
		
		settings_bar.setClickable(true);
		settings_bar.setBackground(new RippleDrawable(
		new ColorStateList(
		new int[][]{new int[]{}},
		new int[]{0xFFD2B6DC}
		),
		new GradientDrawable() {
			public GradientDrawable getIns(int a, int b, int c, int d) {
				this.setCornerRadius(a);
				this.setStroke(b, c);
				this.setColor(d);
				return this;
			}
		}.getIns((int)12, (int)2, 0xFF424242, 0xFFFFFFFF), 
		null
		));
		
		search_bar.setClickable(true);
		search_bar.setBackground(new RippleDrawable(
		new ColorStateList(
		new int[][]{new int[]{}},
		new int[]{0xFFD2B6DC}
		),
		new GradientDrawable() {
			public GradientDrawable getIns(int a, int b, int c, int d) {
				this.setCornerRadius(a);
				this.setStroke(b, c);
				this.setColor(d);
				return this;
			}
		}.getIns((int)12, (int)2, 0xFF424242, 0xFFFFFFFF), 
		null
		));
		
		_refreshList();
		applySortFilter(
		search_txt.getText().toString(),
		loadSortTypeId(),
		loadOrderId(),
		loadFilterId()
		);
		initImagePicker();
		if ((boolean)settings.get("main_tutorial")) {
			View _fab_targetView = findViewById(R.id._fab);
			
			TapTarget _fab_tapTarget = TapTarget.forView(_fab_targetView, getString(R.string.fab_title), getString(R.string.fab_desc))
			.outerCircleColorInt(0xFFD2B6DC)
			.targetCircleColorInt(0xFFC3A2CF)
			.titleTextColorInt(0xFFFFFFFF)
			.descriptionTextColorInt(0xFFFFFFFF)
			.descriptionTextAlpha(1f)
			.cancelable(true)
			.transparentTarget(true)
			.drawShadow(true)
			.id(1);
			TapTargetView.showFor(MainActivity.this, _fab_tapTarget, new TapTargetView.Listener() {
				@Override
				public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
					super.onTargetDismissed(view, userInitiated);
					View filter_bar_targetView = findViewById(R.id.filter_bar);
					
					TapTarget filter_bar_tapTarget = TapTarget.forView(filter_bar_targetView, getString(R.string.filter_title), getString(R.string.filter_desc))
					.outerCircleColorInt(0xFFD2B6DC)
					.targetCircleColorInt(0xFFC3A2CF)
					.titleTextColorInt(0xFFFFFFFF)
					.descriptionTextColorInt(0xFFFFFFFF)
					.descriptionTextAlpha(1f)
					.cancelable(true)
					.transparentTarget(true)
					.drawShadow(true)
					.id(1);
					TapTargetView.showFor(MainActivity.this, filter_bar_tapTarget, new TapTargetView.Listener() {
						@Override
						public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
							super.onTargetDismissed(view, userInitiated);
							View search_bar_targetView = findViewById(R.id.search_bar);
							
							TapTarget search_bar_tapTarget = TapTarget.forView(search_bar_targetView, getString(R.string.search_title), getString(R.string.search_desc))
							.outerCircleColorInt(0xFFD2B6DC)
							.targetCircleColorInt(0xFFC3A2CF)
							.titleTextColorInt(0xFFFFFFFF)
							.descriptionTextColorInt(0xFFFFFFFF)
							.descriptionTextAlpha(1f)
							.cancelable(true)
							.transparentTarget(true)
							.drawShadow(true)
							.id(1);
							TapTargetView.showFor(MainActivity.this, search_bar_tapTarget, new TapTargetView.Listener() {
								@Override
								public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
									super.onTargetDismissed(view, userInitiated);
									View settings_bar_targetView = findViewById(R.id.settings_bar);
									
									TapTarget settings_bar_tapTarget = TapTarget.forView(settings_bar_targetView, getString(R.string.settings_title), getString(R.string.settings_desc))
									.outerCircleColorInt(0xFFD2B6DC)
									.targetCircleColorInt(0xFFC3A2CF)
									.titleTextColorInt(0xFFFFFFFF)
									.descriptionTextColorInt(0xFFFFFFFF)
									.descriptionTextAlpha(1f)
									.cancelable(true)
									.transparentTarget(true)
									.drawShadow(true)
									.id(1);
									TapTargetView.showFor(MainActivity.this, settings_bar_tapTarget, new TapTargetView.Listener() {
										@Override
										public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
											super.onTargetDismissed(view, userInitiated);
											settings.put("main_tutorial", false);
											card_prefs.edit().putString("settings", new Gson().toJson(settings)).commit();
										}
									});
								}
							});
						}
					});
				}
			});
		}
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
		if (_resultCode != Activity.RESULT_OK || _data == null) return;
		
		Uri uri = _data.getData();
		if (uri == null) return;
		
		if (_requestCode == REQ_EXPORT_JSON) {
			exportCardsToUri(uri);
		} else if (_requestCode == REQ_IMPORT_JSON) {
			importCardsFromUri(uri);
		}
		switch (_requestCode) {
			
			default:
			break;
		}
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		if (card_prefs.contains("code") && card_prefs.contains("type")) {
			if (bottomShii != null && bottomShii.isShowing() && code_txt != null) {
				code_txt.setText(getString(R.string.card_code).concat(" ".concat(card_prefs.getString("code", ""))));
				cardSaveCode = card_prefs.getString("code", "");
				cardSaveType = card_prefs.getString("type", "");
				card_prefs.edit().remove("code").commit();
				card_prefs.edit().remove("type").commit();
			} else {
				SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.failed_scan));
				card_prefs.edit().remove("code").commit();
				card_prefs.edit().remove("type").commit();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		
		if (bottomShii != null && bottomShii.isShowing()) {
			bottomShii.dismiss();
			return;
		}
		
		if (d != null && d.isShowing()) {
			d.dismiss();
			return;
		}
		
		if (inFolder && !folderIdStack.isEmpty()) {
			
			folderIdStack.remove(folderIdStack.size() - 1);
			folderNameStack.remove(folderNameStack.size() - 1);
			
			if (folderIdStack.isEmpty()) {
				inFolder = false;
				folderPath = "";
			} else {
				inFolder = true;
				folderPath = joinWithSlash(folderNameStack);
			}
			
			applySortFilter(
			search_txt.getText().toString(),
			loadSortTypeId(),
			loadOrderId(),
			loadFilterId()
			);
			return;
		}
		
		super.onBackPressed();
	}
	public void _refreshList() {
		ArrayList<HashMap<String, Object>> tmp = new ArrayList<>();
		
		if (card_prefs.contains("cards")) {
			tmp = new Gson().fromJson(
			card_prefs.getString("cards", ""),
			new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType()
			);
			if (tmp == null) tmp = new ArrayList<>();
		}
		
		tmp = sanitizeTree(tmp);
		
		folderIdStack.clear();
		folderNameStack.clear();
		inFolder = false;
		folderPath = "";
		
		cards_list_all.clear();
		cards_list_all.addAll(tmp);
		
		if (cards_rec.getAdapter() == null) {
			cardsAdapter = new Cards_recAdapter(cards_list);
			cards_rec.setAdapter(cardsAdapter);
		} else {
			cardsAdapter.notifyDataSetChanged();
		}
	}
	private void importCardsFromUri(Uri uri) {
		String prev = new Gson().toJson(cards_list_all);
		
		InputStream is = null;
		try {
			is = getContentResolver().openInputStream(uri);
			if (is == null) throw new Exception("openInputStream returned null");
			
			java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int r;
			while ((r = is.read(buf)) != -1) bos.write(buf, 0, r);
			
			String importedJson = bos.toString("UTF-8");
			
			card_prefs.edit().putString("cards", importedJson).commit();
			
			ArrayList<HashMap<String, Object>> tmp = new Gson().fromJson(
			importedJson,
			new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType()
			);
			if (tmp == null) tmp = new ArrayList<>();
			
			tmp = sanitizeTree(tmp);
			
			folderIdStack.clear();
			folderNameStack.clear();
			inFolder = false;
			folderPath = "";
			
			cards_list.clear();
			cards_list_all.clear();
			cards_list.addAll(tmp);
			cards_list_all.addAll(tmp);
			
			if (cards_rec.getAdapter() == null) {
				cardsAdapter = new Cards_recAdapter(cards_list);
				cards_rec.setAdapter(cardsAdapter);
			} else {
				cardsAdapter.notifyDataSetChanged();
			}
			
			SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.import_success));
		} catch (Exception e) {
			try {
				card_prefs.edit().putString("cards", prev).commit();
			} catch (Exception ignored) {}
			
			try {
				ArrayList<HashMap<String, Object>> tmp = new Gson().fromJson(
				prev,
				new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType()
				);
				if (tmp == null) tmp = new ArrayList<>();
				
				tmp = sanitizeTree(tmp);
				
				folderIdStack.clear();
				folderNameStack.clear();
				inFolder = false;
				folderPath = "";
				
				cards_list.clear();
				cards_list_all.clear();
				cards_list.addAll(tmp);
				cards_list_all.addAll(tmp);
				
				if (cards_rec.getAdapter() == null) {
					cardsAdapter = new Cards_recAdapter(cards_list);
					cards_rec.setAdapter(cardsAdapter);
				} else {
					cardsAdapter.notifyDataSetChanged();
				}
			} catch (Exception ignored2) {}
			
			SketchwareUtil.showMessage(
			getApplicationContext(),
			getString(R.string.import_fail).concat(" ").concat(String.valueOf(e.getMessage()))
			);
		} finally {
			try { if (is != null) is.close(); } catch (Exception ignored) {}
		}
	}
	private void exportCardsToUri(Uri uri) {
		String json = new Gson().toJson(cards_list_all);
		
		OutputStream os = null;
		try {
			os = getContentResolver().openOutputStream(uri, "wt");
			if (os == null) throw new Exception("openOutputStream returned null");
			
			byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			os.write(bytes);
			os.flush();
			
			SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.export_success));
		} catch (Exception e) {
			SketchwareUtil.showMessage(
			getApplicationContext(),
			getString(R.string.export_fail).concat(" ").concat(String.valueOf(e.getMessage()))
			);
		} finally {
			try { if (os != null) os.close(); } catch (Exception ignored) {}
		}
	}
	private void showColorsTutorialStep(
	final com.google.android.material.bottomsheet.BottomSheetDialog bottomShii,
	final RecyclerView colors_rec,
	final int pos
	) {
		RecyclerView.ViewHolder vh = colors_rec.findViewHolderForAdapterPosition(pos);
		
		if (vh == null) {
			colors_rec.post(new Runnable() {
				@Override
				public void run() {
					showColorsTutorialStep(bottomShii, colors_rec, pos);
				}
			});
			return;
		}
		
		View target = vh.itemView.findViewById(R.id.parent);
		if (target == null) target = vh.itemView;
		
		int titleRes = getColorsTutTitleRes(pos);
		int descRes  = getColorsTutDescRes(pos);
		
		TapTarget tt = TapTarget.forView(target, getString(titleRes), getString(descRes))
		.outerCircleColorInt(0xFFD2B6DC)
		.targetCircleColorInt(0xFFC3A2CF)
		.titleTextColorInt(0xFFFFFFFF)
		.descriptionTextColorInt(0xFFFFFFFF)
		.descriptionTextAlpha(1f)
		.cancelable(true)
		.transparentTarget(true)
		.drawShadow(true)
		.id(200 + pos);
		
		TapTargetView.showFor(bottomShii, tt, new TapTargetView.Listener() {
			@Override
			public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
				super.onTargetDismissed(view, userInitiated);
				
				int next = pos + 1;
				if (next <= 2) {
					showColorsTutorialStep(bottomShii, colors_rec, next);
				} else {
					settings.put("colors_tutorial", false);
					card_prefs.edit()
					.putString("settings", new Gson().toJson(settings))
					.commit();
				}
			}
		});
	}
	private int getColorsTutTitleRes(int pos) {
		if (pos == 0) return R.string.edit_title;
		if (pos == 1) return R.string.add_title;
		return R.string.grads_title;
	}
	private int getColorsTutDescRes(int pos) {
		if (pos == 0) return R.string.edit_desc;
		if (pos == 1) return R.string.add_desc;
		return R.string.grads_desc;
	}
	private android.graphics.Bitmap loadBitmapFromUri(android.net.Uri uri, int maxSize) throws Exception {
		android.graphics.BitmapFactory.Options opts = new android.graphics.BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		
		java.io.InputStream is = getContentResolver().openInputStream(uri);
		android.graphics.BitmapFactory.decodeStream(is, null, opts);
		if (is != null) is.close();
		
		int w = opts.outWidth;
		int h = opts.outHeight;
		int inSampleSize = 1;
		
		while (w / inSampleSize > maxSize || h / inSampleSize > maxSize) {
			inSampleSize *= 2;
		}
		
		android.graphics.BitmapFactory.Options opts2 = new android.graphics.BitmapFactory.Options();
		opts2.inSampleSize = inSampleSize;
		
		is = getContentResolver().openInputStream(uri);
		android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(is, null, opts2);
		if (is != null) is.close();
		
		if (bmp == null) throw new Exception("Bitmap decode returned null");
		return bmp;
	}
	private com.google.zxing.Result decodeWithZxing(android.graphics.Bitmap bitmap) throws Exception {
		com.google.zxing.Result res;
		
		res = tryDecodeOnce(bitmap);
		if (res != null) return res;
		
		android.graphics.Bitmap b90 = rotateBitmap(bitmap, 90);
		res = tryDecodeOnce(b90);
		if (res != null) return res;
		
		android.graphics.Bitmap b180 = rotateBitmap(bitmap, 180);
		res = tryDecodeOnce(b180);
		if (res != null) return res;
		
		android.graphics.Bitmap b270 = rotateBitmap(bitmap, 270);
		res = tryDecodeOnce(b270);
		if (res != null) return res;
		
		throw com.google.zxing.NotFoundException.getNotFoundInstance();
	}
	private com.google.zxing.Result tryDecodeOnce(android.graphics.Bitmap bitmap) {
		try {
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			
			int[] pixels = new int[w * h];
			bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
			
			com.google.zxing.RGBLuminanceSource source =
			new com.google.zxing.RGBLuminanceSource(w, h, pixels);
			
			java.util.Map<com.google.zxing.DecodeHintType, Object> hints =
			new java.util.EnumMap<com.google.zxing.DecodeHintType, Object>(com.google.zxing.DecodeHintType.class);
			hints.put(com.google.zxing.DecodeHintType.TRY_HARDER, Boolean.TRUE);
			
			com.google.zxing.MultiFormatReader reader = new com.google.zxing.MultiFormatReader();
			reader.setHints(hints);
			
			com.google.zxing.BinaryBitmap bb =
			new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source));
			
			return reader.decodeWithState(bb);
		} catch (Exception e) {
			return null;
		}
	}
	private android.graphics.Bitmap rotateBitmap(android.graphics.Bitmap src, int degrees) {
		android.graphics.Matrix m = new android.graphics.Matrix();
		m.postRotate(degrees);
		return android.graphics.Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
	}
	public interface OnImagePicked {
		void onPicked(Uri uri);
	}
	public interface OnCancelled {
		void onCancelled();
	}
	private void initImagePicker() {
		imagePickerLauncher = registerForActivityResult(
		new ActivityResultContracts.StartActivityForResult(),
		new ActivityResultCallback<ActivityResult>() {
			@Override
			public void onActivityResult(ActivityResult result) {
				pickInProgress = false;
				OnImagePicked picked = pendingPicked;
				OnCancelled cancelled = pendingCancelled;
				pendingPicked = null;
				pendingCancelled = null;
				
				if (result == null
				|| result.getResultCode() != Activity.RESULT_OK
				|| result.getData() == null) {
					if (cancelled != null) cancelled.onCancelled();
					return;
				}
				
				Uri uri = result.getData().getData();
				if (uri == null) {
					if (cancelled != null) cancelled.onCancelled();
					return;
				}
				
				if (picked != null) picked.onPicked(uri);
			}
		}
		);
	}
	private void pickImage(OnImagePicked onPicked, OnCancelled onCancelled) {
		if (pickInProgress) return;
		pickInProgress = true;
		
		pendingPicked = onPicked;
		pendingCancelled = onCancelled;
		
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		
		imagePickerLauncher.launch(intent);
	}
	private static float textScaleFromLevel(int level) {
		int clamped = Math.max(1, Math.min(5, level));
		int delta = clamped - 3;
		return (float) Math.pow(1.12, delta);
	}
	private static void applyTextScale(TextView tv, float scale) {
		Object tag = tv.getTag(TAG_BASE_TEXT_PX);
		float basePx;
		if (tag instanceof Float) {
			basePx = (Float) tag;
		} else {
			basePx = tv.getTextSize();
			tv.setTag(TAG_BASE_TEXT_PX, basePx);
		}
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, basePx * scale);
	}
	@androidx.annotation.ColorInt
	private static int pickForegroundForGradient(java.util.List<Integer> bgColors) {
		if (bgColors == null || bgColors.isEmpty()) return 0xFFFFFFFF;
		
		final int WHITE = 0xFFFFFFFF;
		final int BLACK = 0xFF212121;
		
		final double MIN_CONTRAST_FOR_WHITE = 3.0;
		
		double worstContrastWithWhite = Double.POSITIVE_INFINITY;
		
		for (int c : bgColors) {
			int opaque = compositeOverWhite(c);
			worstContrastWithWhite = Math.min(
			worstContrastWithWhite,
			contrastRatio(opaque, WHITE)
			);
		}
		
		return (worstContrastWithWhite >= MIN_CONTRAST_FOR_WHITE)
		? WHITE
		: BLACK;
	}
	
	private static int compositeOverWhite(int c) {
		int a = (c >>> 24) & 0xFF;
		if (a >= 255) return c;
		
		int r = (c >>> 16) & 0xFF;
		int g = (c >>>  8) & 0xFF;
		int b = (c       ) & 0xFF;
		
		int outR = (r * a + 255 * (255 - a)) / 255;
		int outG = (g * a + 255 * (255 - a)) / 255;
		int outB = (b * a + 255 * (255 - a)) / 255;
		
		return 0xFF000000 | (outR << 16) | (outG << 8) | outB;
	}
	
	private static double contrastRatio(int c1, int c2) {
		double l1 = relativeLuminance(c1);
		double l2 = relativeLuminance(c2);
		
		double lighter = Math.max(l1, l2);
		double darker  = Math.min(l1, l2);
		
		return (lighter + 0.05) / (darker + 0.05);
	}
	private static double relativeLuminance(int c) {
		double r = srgbToLinear(((c >>> 16) & 0xFF) / 255.0);
		double g = srgbToLinear(((c >>>  8) & 0xFF) / 255.0);
		double b = srgbToLinear(((c       ) & 0xFF) / 255.0);
		
		return 0.2126 * r + 0.7152 * g + 0.0722 * b;
	}
	private static double srgbToLinear(double x) {
		if (x <= 0.04045) return x / 12.92;
		return Math.pow((x + 0.055) / 1.055, 2.4);
	}
	private boolean removeByIdInList(ArrayList<HashMap<String, Object>> list, String id) {
		if (list == null) return false;
		
		for (int i = 0; i < list.size(); i++) {
			HashMap<String, Object> m = list.get(i);
			if (m == null) continue;
			
			Object v = m.get("id");
			if (v != null && id.equals(String.valueOf(v))) {
				list.remove(i);
				return true;
			}
		}
		return false;
	}
	private boolean removeByIdRecursive(ArrayList<HashMap<String, Object>> list, String id) {
		if (list == null) return false;
		
		for (int i = 0; i < list.size(); i++) {
			HashMap<String, Object> m = list.get(i);
			if (m == null) continue;
			
			Object v = m.get("id");
			if (v != null && id.equals(String.valueOf(v))) {
				list.remove(i);
				return true;
			}
			
			if (getBool(m, "folder", false)) {
				ArrayList<HashMap<String, Object>> child = normalizeListOfMaps(m.get("data"));
				m.put("data", child);
				
				if (removeByIdRecursive(child, id)) return true;
			}
		}
		return false;
	}
	private ArrayList<HashMap<String, Object>> sanitizeTree(Object rootObj) {
		ArrayList<HashMap<String, Object>> list = normalizeListOfMaps(rootObj);
		
		for (int i = 0; i < list.size(); i++) {
			HashMap<String, Object> item = list.get(i);
			if (item == null) continue;
			
			if (getBool(item, "folder", false)) {
				ArrayList<HashMap<String, Object>> child = sanitizeTree(item.get("data"));
				item.put("data", child);
			}
		}
		
		return list;
	}
	private boolean folderHasFavorite(HashMap<String, Object> folder) {
		if (folder == null) return false;
		
		if (getBool(folder, "favorite", false)) return true;
		
		ArrayList<HashMap<String, Object>> child = normalizeListOfMaps(folder.get("data"));
		
		for (int i = 0; i < child.size(); i++) {
			HashMap<String, Object> c = child.get(i);
			if (c == null) continue;
			
			if (getBool(c, "folder", false)) {
				if (folderHasFavorite(c)) return true;
			} else {
				if (getBool(c, "favorite", false)) return true;
			}
		}
		
		return false;
	}
	@SuppressWarnings("unchecked")
	private ArrayList<HashMap<String, Object>> normalizeListOfMaps(Object obj) {
		ArrayList<HashMap<String, Object>> out = new ArrayList<>();
		if (obj == null) return out;
		
		if (obj instanceof ArrayList) {
			ArrayList list = (ArrayList) obj;
			for (Object el : list) {
				if (el instanceof HashMap) {
					out.add((HashMap<String, Object>) el);
				} else if (el instanceof Map) {
					out.add(new HashMap<String, Object>((Map) el)); // LinkedTreeMap -> HashMap
				}
			}
			return out;
		}
		
		if (obj instanceof Map) {
			out.add(new HashMap<String, Object>((Map) obj));
			return out;
		}
		
		if (obj instanceof String) {
			String s = (String) obj;
			if ("{}".equals(s) || "[]".equals(s) || s.trim().isEmpty()) return out;
		}
		
		return out;
	}
	private HashMap<String, Object> findFolderById(ArrayList<HashMap<String, Object>> list, String folderId) {
		for (int i = 0; i < list.size(); i++) {
			HashMap<String, Object> m = list.get(i);
			if (m == null) continue;
			
			Object folderObj = m.get("folder");
			boolean isFolder = false;
			if (folderObj instanceof Boolean) isFolder = (Boolean) folderObj;
			else if (folderObj != null) isFolder = "true".equalsIgnoreCase(String.valueOf(folderObj));
			if (!isFolder) continue;
			
			Object idObj = m.get("id");
			String id = (idObj == null) ? null : String.valueOf(idObj);
			if (folderId.equals(id)) return m;
		}
		return null;
	}
	private ArrayList<HashMap<String, Object>> resolveContainerList(
	ArrayList<HashMap<String, Object>> root,
	ArrayList<String> folderIdStack
	) {
		ArrayList<HashMap<String, Object>> curList = root;
		
		for (int i = 0; i < folderIdStack.size(); i++) {
			String targetFolderId = folderIdStack.get(i);
			
			HashMap<String, Object> folderMap = findFolderById(curList, targetFolderId);
			if (folderMap == null) {
				// stack doesn't match data -> safest fallback
				return root;
			}
			
			Object dataObj = folderMap.get("data");
			ArrayList<HashMap<String, Object>> child = normalizeListOfMaps(dataObj);
			
			folderMap.put("data", child);
			
			curList = child;
		}
		
		return curList;
	}
	private String joinWithSlash(ArrayList<String> parts) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.size(); i++) {
			if (i > 0) sb.append("/");
			sb.append(parts.get(i));
		}
		return sb.toString();
	}
	private void setupSortFilterListeners(final RadioGroup sort_types,
	final RadioGroup orders,
	final RadioGroup filters) {
		
		RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				
				int sortTypeId = sort_types.getCheckedRadioButtonId();
				int orderId = orders.getCheckedRadioButtonId();
				int filterId = filters.getCheckedRadioButtonId();
				
				if (sortTypeId == -1) sortTypeId = R.id.by_name;
				if (orderId == -1) orderId = R.id.ascending;
				if (filterId == -1) filterId = R.id.all;
				
				saveSortPrefs(sortTypeId, orderId, filterId);
				
				applySortFilter(
				search_txt.getText().toString(),
				sortTypeId,
				orderId,
				filterId
				);
			}
		};
		
		sort_types.setOnCheckedChangeListener(listener);
		orders.setOnCheckedChangeListener(listener);
		filters.setOnCheckedChangeListener(listener);
	}
	private int loadSortTypeId() {
		return card_prefs.getInt(PREF_SORT_TYPE, R.id.by_name);
	}
	private int loadOrderId() {
		return card_prefs.getInt(PREF_SORT_ORDER, R.id.ascending);
	}
	private int loadFilterId() {
		return card_prefs.getInt(PREF_SORT_FILTER, R.id.all);
	}
	private void saveSortPrefs(int sortTypeId, int orderId, int filterId) {
		card_prefs.edit()
		.putInt(PREF_SORT_TYPE, sortTypeId)
		.putInt(PREF_SORT_ORDER, orderId)
		.putInt(PREF_SORT_FILTER, filterId)
		.apply();
	}
	private void applySortFilter(String searchQuery,
	int sortTypeCheckedId,
	int orderCheckedId,
	int filterCheckedId) {
		
		String q = (searchQuery == null) ? "" : searchQuery.toLowerCase().trim();
		
		boolean onlyFav = (filterCheckedId == R.id.favorites);
		boolean ascending = (orderCheckedId == R.id.ascending);
		
		ArrayList<HashMap<String, Object>> source;
		
		if (inFolder && !folderIdStack.isEmpty()) {
			source = resolveContainerList(cards_list_all, folderIdStack);
		} else {
			source = cards_list_all;
		}
		
		cards_list.clear();
		
		for (int i = 0; i < source.size(); i++) {
			HashMap<String, Object> card = source.get(i);
			
			if (onlyFav) {
				if (getBool(card, "folder", false)) {
					if (!folderHasFavorite(card)) continue;
				} else {
					if (!getBool(card, "favorite", false)) continue;
				}
			}
			
			if (q.length() > 0) {
				String name = getStr(card, "name").toLowerCase();
				if (!name.contains(q)) continue;
			}
			
			cards_list.add(card);
		}
		
		java.util.Collections.sort(cards_list, new java.util.Comparator<HashMap<String, Object>>() {
			@Override
			public int compare(HashMap<String, Object> a, HashMap<String, Object> b) {
				
				int res = 0;
				
				if (sortTypeCheckedId == R.id.by_name) {
					
					String an = getStr(a, "name");
					String bn = getStr(b, "name");
					res = an.compareToIgnoreCase(bn);
					
				} else if (sortTypeCheckedId == R.id.by_date_created) {
					
					long ai = getLong(a, "id", 0);
					long bi = getLong(b, "id", 0);
					res = (ai < bi) ? -1 : (ai > bi ? 1 : 0);
					
				} else if (sortTypeCheckedId == R.id.by_use_count) {
					
					int au = getInt(a, "used", 0);
					int bu = getInt(b, "used", 0);
					res = (au < bu) ? -1 : (au > bu ? 1 : 0);
				}
				
				if (res == 0) {
					long ai = getLong(a, "id", 0);
					long bi = getLong(b, "id", 0);
					
					if (sortTypeCheckedId == R.id.by_use_count) {
						res = (ai < bi) ? 1 : (ai > bi ? -1 : 0);
					} else {
						res = (ai < bi) ? -1 : (ai > bi ? 1 : 0);
					}
				}
				
				if (sortTypeCheckedId == R.id.by_use_count) {
					return ascending ? -res : res;
				}
				
				return ascending ? res : -res;
			}
		});
		
		cardsAdapter.notifyDataSetChanged();
		srefresh.setRefreshing(false);
		applyTextScale(no_items_txt, textScaleFromLevel((int) textLevel));
		
		if (cards_list_all.size() == 0) {
			srefresh.setVisibility(View.GONE);
			no_items_lay.setVisibility(View.VISIBLE);
		} else {
			srefresh.setVisibility(View.VISIBLE);
			no_items_lay.setVisibility(View.GONE);
		}
	}
	private String getStr(HashMap<String, Object> m, String k) {
		Object v = m.get(k);
		return v == null ? "" : String.valueOf(v);
	}
	private long getLong(HashMap<String, Object> m, String k, long def) {
		Object v = m.get(k);
		if (v == null) return def;
		try {
			if (v instanceof Number) return ((Number) v).longValue();
			return Long.parseLong(String.valueOf(v));
		} catch (Exception e) {
			return def;
		}
	}
	private int getInt(HashMap<String, Object> m, String k, int def) {
		Object v = m.get(k);
		if (v == null) return def;
		try {
			if (v instanceof Number) return ((Number) v).intValue();
			return Integer.parseInt(String.valueOf(v));
		} catch (Exception e) {
			return def;
		}
	}
	private boolean getBool(HashMap<String, Object> m, String k, boolean def) {
		Object v = m.get(k);
		if (v == null) return def;
		try {
			if (v instanceof Boolean) return (Boolean) v;
			return Boolean.parseBoolean(String.valueOf(v));
		} catch (Exception e) {
			return def;
		}
	}
	private void openScannerOrRequestPermission() {
		i.setAction(null);
		i.setData(null);
		i.setType(null);
		i.replaceExtras((Bundle) null);
		
		if (Build.VERSION.SDK_INT < 23) {
			i.setClass(getApplicationContext(), ScannerActivity.class);
			startActivity(i);
			return;
		}
		
		if (checkSelfPermission(android.Manifest.permission.CAMERA)
		== android.content.pm.PackageManager.PERMISSION_GRANTED) {
			i.setClass(getApplicationContext(), ScannerActivity.class);
			startActivity(i);
		} else {
			requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQ_CAMERA);
		}
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
		if (requestCode == REQ_CAMERA) {
			if (grantResults.length > 0
			&& grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
				i.setClass(getApplicationContext(), ScannerActivity.class);
				startActivity(i);
				
			} else {
				boolean canAskAgain = true;
				
				if (Build.VERSION.SDK_INT >= 23) {
					canAskAgain = shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA);
				}
				
				if (canAskAgain) {
					SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.camera_perm));
				} else {
					SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.camera_fail));
					
					Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					intent.setData(android.net.Uri.fromParts("package", getPackageName(), null));
					startActivity(intent);
				}
			}
		}
	}
	private void toggleFavorite(View heartView, boolean isNew, ArrayList<HashMap<String, Object>> _data, int pos) {
		boolean currentlyFavorite = false;
		if (isNew) {
			currentlyFavorite = heartView.isSelected();
		} else {
			currentlyFavorite = (boolean) _data.get(pos).get("favorite");
		}
		boolean newFavorite = !currentlyFavorite;
		heartView.setSelected(newFavorite);
		
		if (newFavorite) {
			heartView.animate().cancel();
			heartView.setScaleX(1f);
			heartView.setScaleY(1f);
			
			heartView.animate()
			.scaleX(1.18f)
			.scaleY(1.18f)
			.setDuration(90)
			.setInterpolator(new android.view.animation.OvershootInterpolator())
			.withEndAction(new Runnable() {
				@Override
				public void run() {
					heartView.animate()
					.scaleX(1f)
					.scaleY(1f)
					.setDuration(120)
					.setInterpolator(new android.view.animation.DecelerateInterpolator())
					.start();
				}
			})
			.start();
		}
		if (isNew) {
			if (newFavorite) {
				favorite = true;
			} else {
				favorite = false;
			}
		} else {
			_data.get(pos).put("favorite", newFavorite);
		}
	}
	private GradientDrawable.Orientation _gradOrientationFromStyle(String style) {
		if (style == null) return GradientDrawable.Orientation.LEFT_RIGHT;
		
		if (style.equals("lr")) return GradientDrawable.Orientation.LEFT_RIGHT;
		if (style.equals("rl")) return GradientDrawable.Orientation.RIGHT_LEFT;
		if (style.equals("tb")) return GradientDrawable.Orientation.TOP_BOTTOM;
		if (style.equals("bt")) return GradientDrawable.Orientation.BOTTOM_TOP;
		if (style.equals("tl_br")) return GradientDrawable.Orientation.TL_BR;
		if (style.equals("tr_bl")) return GradientDrawable.Orientation.TR_BL;
		if (style.equals("bl_tr")) return GradientDrawable.Orientation.BL_TR;
		if (style.equals("br_tl")) return GradientDrawable.Orientation.BR_TL;
		
		return GradientDrawable.Orientation.LEFT_RIGHT;
	}
	public class Colors_recAdapter extends RecyclerView.Adapter<Colors_recAdapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Colors_recAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.colors_recycler, parent, false);
			return new ViewHolder(v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, final int _position) {
			View _view = _holder.itemView;
			final LinearLayout color = _view.findViewById(R.id.color);
			final LinearLayout plus_lay = _view.findViewById(R.id.plus_lay);
			final LinearLayout parent = _view.findViewById(R.id.parent);
			final ImageView plus_img = _view.findViewById(R.id.plus_img);
			if (_data.get((int)(_position)).get("color").toString().equals("plus")) {
				parent.setClickable(true);
				parent.setBackground(new RippleDrawable(
				new ColorStateList(
				new int[][]{new int[]{}},
				new int[]{0xFFD2B6DC}
				),
				new GradientDrawable() {
					public GradientDrawable getIns(int a, int b, int c, int d) {
						this.setCornerRadius(a);
						this.setStroke(b, c);
						this.setColor(d);
						return this;
					}
				}.getIns((int)360, (int)2, 0xFF424242, 0xFFFFFFFF), 
				null
				));
				
				plus_lay.setVisibility(View.VISIBLE);
				color.setVisibility(View.GONE);
				plus_img.setImageResource(R.drawable.ic_add_grey);
			} else {
				if (_data.get((int)(_position)).get("color").toString().equals("settings")) {
					parent.setClickable(true);
					parent.setBackground(new RippleDrawable(
					new ColorStateList(
					new int[][]{new int[]{}},
					new int[]{0xFFD2B6DC}
					),
					new GradientDrawable() {
						public GradientDrawable getIns(int a, int b, int c, int d) {
							this.setCornerRadius(a);
							this.setStroke(b, c);
							this.setColor(d);
							return this;
						}
					}.getIns((int)360, (int)2, 0xFF424242, 0xFFFFFFFF), 
					null
					));
					
					plus_lay.setVisibility(View.VISIBLE);
					color.setVisibility(View.GONE);
					plus_img.setImageResource(R.drawable.ic_settings);
				} else {
					parent.setClickable(true);
					parent.setBackground(new RippleDrawable(
					new ColorStateList(
					new int[][]{new int[]{}},
					new int[]{0xFFD2B6DC}
					),
					new GradientDrawable() {
						public GradientDrawable getIns(int a, int b, int c, int d) {
							this.setCornerRadius(a);
							this.setStroke(b, c);
							this.setColor(d);
							return this;
						}
					}.getIns((int)360, (int)2, 0xFF424242, (int)(_data.get((int)(_position)).get("color"))), 
					null
					));
					color.setVisibility(View.VISIBLE);
					plus_lay.setVisibility(View.GONE);
				}
			}
			parent.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					int pos = _holder.getBindingAdapterPosition();
					if (pos == RecyclerView.NO_POSITION) return;
					boolean isNew = "plus".equals(_data.get(pos).get("color").toString());
					if (_data.get((int)(pos)).get("color").toString().equals("settings")) {
						selectedGradStyle = "lr";
						d = new AlertDialog.Builder(MainActivity.this).create();
						LayoutInflater dLI = getLayoutInflater();
						View dCV = (View) dLI.inflate(R.layout.radio_dialog, null);
						d.setView(dCV);
						final RadioGroup grad_styles = (RadioGroup)
						dCV.findViewById(R.id.grad_styles);
						final TextView title_txt = (TextView)
						dCV.findViewById(R.id.title_txt);
						final TextView save_txt = (TextView)
						dCV.findViewById(R.id.save_txt);
						final TextView close_txt = (TextView)
						dCV.findViewById(R.id.close_txt);
						final LinearLayout dialog_parent = (LinearLayout)
						dCV.findViewById(R.id.parent);
						final LinearLayout button_bar = (LinearLayout)
						dCV.findViewById(R.id.button_bar);
						final RadioButton leftright = (RadioButton)
						dCV.findViewById(R.id.leftright);
						final RadioButton rightleft = (RadioButton)
						dCV.findViewById(R.id.rightleft);
						final RadioButton topbottom = (RadioButton)
						dCV.findViewById(R.id.topbottom);
						final RadioButton bottomtop = (RadioButton)
						dCV.findViewById(R.id.bottomtop);
						final RadioButton tl_br = (RadioButton)
						dCV.findViewById(R.id.tl_br);
						final RadioButton tr_bl = (RadioButton)
						dCV.findViewById(R.id.tr_bl);
						final RadioButton bl_tr = (RadioButton)
						dCV.findViewById(R.id.bl_tr);
						final RadioButton br_tl = (RadioButton)
						dCV.findViewById(R.id.br_tl);
						float scale = textScaleFromLevel((int) textLevel);
						TextView[] views = new TextView[] { title_txt, save_txt, close_txt };
						for (TextView tv : views) {
							if (tv != null) applyTextScale(tv, scale);
						}
						RadioButton[] buttons = new RadioButton[] { leftright, rightleft, topbottom, bottomtop, tl_br, tr_bl, bl_tr, br_tl };
						for (RadioButton rb : buttons) {
							if (rb != null) applyTextScale(rb, scale);
						}
						save_txt.setClickable(true);
						save_txt.setBackground(new RippleDrawable(
						new ColorStateList(
						new int[][]{new int[]{}},
						new int[]{0xFFF2EAF5}
						),
						new GradientDrawable() {
							public GradientDrawable getIns(int a, int b, int c, int d) {
								this.setCornerRadius(a);
								this.setStroke(b, c);
								this.setColor(d);
								return this;
							}
						}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFD2B6DC), 
						null
						));
						
						close_txt.setClickable(true);
						close_txt.setBackground(new RippleDrawable(
						new ColorStateList(
						new int[][]{new int[]{}},
						new int[]{0xFFF2EAF5}
						),
						new GradientDrawable() {
							public GradientDrawable getIns(int a, int b, int c, int d) {
								this.setCornerRadius(a);
								this.setStroke(b, c);
								this.setColor(d);
								return this;
							}
						}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFD2B6DC), 
						null
						));
						
						dialog_parent.setClickable(true);
						final float dialog_parent_rTL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final float dialog_parent_rTR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final float dialog_parent_rBR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final float dialog_parent_rBL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final int dialog_parent_strokePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 2, getResources().getDisplayMetrics());
						final GradientDrawable dialog_parent_bg = new GradientDrawable();
						dialog_parent_bg.setColor(0xFFFFFFFF);
						dialog_parent_bg.setCornerRadii(new float[]{dialog_parent_rTL,dialog_parent_rTL,dialog_parent_rTR,dialog_parent_rTR,dialog_parent_rBR,dialog_parent_rBR,dialog_parent_rBL,dialog_parent_rBL});
						dialog_parent_bg.setStroke(dialog_parent_strokePx, 0xFF424242);
						dialog_parent.setBackground(dialog_parent_bg);
						button_bar.setClickable(true);
						
						final float button_bar_rTL = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 0,
						getResources().getDisplayMetrics()
						);
						
						final float button_bar_rTR = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 0,
						getResources().getDisplayMetrics()
						);
						
						final float button_bar_rBR = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 12,
						getResources().getDisplayMetrics()
						);
						
						final float button_bar_rBL = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 12,
						getResources().getDisplayMetrics()
						);
						
						button_bar.setBackground(new ShapeDrawable(new RoundRectShape(
						new float[]{
							button_bar_rTL, button_bar_rTL,
							button_bar_rTR, button_bar_rTR,
							button_bar_rBR, button_bar_rBR,
							button_bar_rBL, button_bar_rBL
						},
						null,
						null
						)) {{
								getPaint().setColor(0xFFD2B6DC);
							}});
						d.setCancelable(true);
						d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
						grad_styles.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(RadioGroup group, int id) {
								if (id == R.id.leftright) {
									selectedGradStyle = "lr";
								} else if (id == R.id.rightleft) {
									selectedGradStyle = "rl";
								} else if (id == R.id.topbottom) {
									selectedGradStyle = "tb";
								} else if (id == R.id.bottomtop) {
									selectedGradStyle = "bt";
								} else if (id == R.id.tl_br) {
									selectedGradStyle = "tl_br";
								} else if (id == R.id.tr_bl) {
									selectedGradStyle = "tr_bl";
								} else if (id == R.id.bl_tr) {
									selectedGradStyle = "bl_tr";
								} else if (id == R.id.br_tl) {
									selectedGradStyle = "br_tl";
								} else {
									selectedGradStyle = "lr";
								}
							}
						});
						save_txt.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								d.dismiss();
							}
						});
						close_txt.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								d.dismiss();
							}
						});
						d.show();
					} else {
						d = new AlertDialog.Builder(MainActivity.this).create();
						LayoutInflater dLI = getLayoutInflater();
						View dCV = (View) dLI.inflate(R.layout.color_picker_dialog, null);
						d.setView(dCV);
						final HsvColorPickerView color_picker = (HsvColorPickerView)
						dCV.findViewById(R.id.color_picker);
						final LinearLayout dialog_parent = (LinearLayout)
						dCV.findViewById(R.id.parent);
						final LinearLayout button_bar = (LinearLayout)
						dCV.findViewById(R.id.button_bar);
						final TextView add_txt = (TextView)
						dCV.findViewById(R.id.add_txt);
						final TextView close_txt = (TextView)
						dCV.findViewById(R.id.close_txt);
						applyTextScale(add_txt, textScaleFromLevel((int) textLevel));
						applyTextScale(close_txt, textScaleFromLevel((int) textLevel));
						if (!isNew) {
							add_txt.setText(getString(R.string.change));
						}
						add_txt.setClickable(true);
						add_txt.setBackground(new RippleDrawable(
						new ColorStateList(
						new int[][]{new int[]{}},
						new int[]{0xFFF2EAF5}
						),
						new GradientDrawable() {
							public GradientDrawable getIns(int a, int b, int c, int d) {
								this.setCornerRadius(a);
								this.setStroke(b, c);
								this.setColor(d);
								return this;
							}
						}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFD2B6DC), 
						null
						));
						
						close_txt.setClickable(true);
						close_txt.setBackground(new RippleDrawable(
						new ColorStateList(
						new int[][]{new int[]{}},
						new int[]{0xFFF2EAF5}
						),
						new GradientDrawable() {
							public GradientDrawable getIns(int a, int b, int c, int d) {
								this.setCornerRadius(a);
								this.setStroke(b, c);
								this.setColor(d);
								return this;
							}
						}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFD2B6DC), 
						null
						));
						
						dialog_parent.setClickable(true);
						final float dialog_parent_rTL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final float dialog_parent_rTR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final float dialog_parent_rBR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final float dialog_parent_rBL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final int dialog_parent_strokePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 2, getResources().getDisplayMetrics());
						final GradientDrawable dialog_parent_bg = new GradientDrawable();
						dialog_parent_bg.setColor(0xFFFFFFFF);
						dialog_parent_bg.setCornerRadii(new float[]{dialog_parent_rTL,dialog_parent_rTL,dialog_parent_rTR,dialog_parent_rTR,dialog_parent_rBR,dialog_parent_rBR,dialog_parent_rBL,dialog_parent_rBL});
						dialog_parent_bg.setStroke(dialog_parent_strokePx, 0xFF424242);
						dialog_parent.setBackground(dialog_parent_bg);
						button_bar.setClickable(true);
						
						final float button_bar_rTL = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 0,
						getResources().getDisplayMetrics()
						);
						
						final float button_bar_rTR = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 0,
						getResources().getDisplayMetrics()
						);
						
						final float button_bar_rBR = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 12,
						getResources().getDisplayMetrics()
						);
						
						final float button_bar_rBL = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 12,
						getResources().getDisplayMetrics()
						);
						
						button_bar.setBackground(new ShapeDrawable(new RoundRectShape(
						new float[]{
							button_bar_rTL, button_bar_rTL,
							button_bar_rTR, button_bar_rTR,
							button_bar_rBR, button_bar_rBR,
							button_bar_rBL, button_bar_rBL
						},
						null,
						null
						)) {{
								getPaint().setColor(0xFFD2B6DC);
							}});
						d.setCancelable(true);
						d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
						add_txt.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								if (isNew) {
									colors = new HashMap<>();
									colors.put("color", (int)(color_picker.getColor()));
									int insertPos = Math.max(0, _data.size() - 2);
									_data.add(insertPos, colors);
									colorsAdapter.notifyItemInserted(insertPos);
								} else {
									if (pos < 0 || pos >= _data.size()) return;
									
									_data.get(pos).put("color", (int) color_picker.getColor());
									colorsAdapter.notifyItemChanged(pos);
								}
								d.dismiss();
							}
						});
						close_txt.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								d.dismiss();
							}
						});
						d.show();
					}
				}
			});
		}
		
		@Override
		public int getItemCount() {
			return _data.size();
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder {
			public ViewHolder(View v) {
				super(v);
			}
		}
	}
	
	public class Cards_recAdapter extends RecyclerView.Adapter<Cards_recAdapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Cards_recAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getLayoutInflater();
			View _v = _inflater.inflate(R.layout.cards_recycler, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new ViewHolder(_v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, final int _position) {
			View _view = _holder.itemView;
			
			final FrameLayout parent = _view.findViewById(R.id.parent);
			final View gradient = _view.findViewById(R.id.gradient);
			final ImageView folder = _view.findViewById(R.id.folder);
			final TextView card_name = _view.findViewById(R.id.card_name);
			
			Object nameObj = _data.get(_position).get("name");
			card_name.setText(nameObj == null ? "" : String.valueOf(nameObj));
			ArrayList<Integer> picked = new ArrayList<>();
			try {
				Object raw = _data.get(_position).get("colors");
				if (raw != null) {
					String s = String.valueOf(raw);
					ArrayList<Integer> tmp = new Gson().fromJson(
					s,
					new com.google.gson.reflect.TypeToken<ArrayList<Integer>>(){}.getType()
					);
					if (tmp != null) picked.addAll(tmp);
				}
			} catch (Exception ignore) { }
			
			GradientDrawable.Orientation ori = GradientDrawable.Orientation.LEFT_RIGHT;
			try {
				Object st = _data.get(_position).get("grad_style");
				String style = (st == null) ? null : String.valueOf(st);
				ori = _gradOrientationFromStyle(style);
			} catch (Exception ignore) { }
			
			final int strokePx = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 2, _view.getResources().getDisplayMetrics()
			);
			final float radiusPx = TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 12, _view.getResources().getDisplayMetrics()
			);
			
			GradientDrawable content = new GradientDrawable();
			content.setShape(GradientDrawable.RECTANGLE);
			content.setCornerRadius(radiusPx);
			content.setStroke(strokePx, 0xFF212121);
			
			if (picked.size() <= 0) {
				content.setColor(0xFFFFFFFF);
			} else if (picked.size() == 1) {
				content.setColor(picked.get(0));
			} else {
				int[] arr = new int[picked.size()];
				for (int i = 0; i < picked.size(); i++) arr[i] = picked.get(i);
				
				content.setOrientation(ori);
				content.setColors(arr);
			}
			
			parent.setClickable(true);
			gradient.setBackground(new RippleDrawable(
			new ColorStateList(
			new int[][]{ new int[]{} },
			new int[]{ 0xFFD2B6DC }
			),
			content,
			null
			));
			card_name.setTextColor(pickForegroundForGradient(picked));
			applyTextScale(card_name, textScaleFromLevel((int) textLevel));
			parent.post(new Runnable() {
				@Override
				public void run() {
					int w = parent.getWidth();
					ViewGroup.LayoutParams parent_layoutParams = parent.getLayoutParams();
					
					parent_layoutParams.height = (int) Math.round(w / 1.25);
					
					parent.setLayoutParams(parent_layoutParams);
					FrameLayout.LayoutParams v_lp = (FrameLayout.LayoutParams) gradient.getLayoutParams();
					if ((boolean)_data.get((int)(_position)).get("folder")) {
						int h = (int) Math.round(w / 1.25);
						
						int mL = Math.round(w * 0.1f);
						int mR = mL;
						int mB = Math.round(h * 0.125f);
						int mT = Math.round(h * 0.25f);
						
						int bleed = Math.max(1, Math.round(Math.min(w, h) * 0.05f));
						
						mL = Math.max(0, mL - bleed);
						mR = Math.max(0, mR - bleed);
						mB = Math.max(0, mB - bleed);
						mT = Math.max(0, mT - bleed);
						
						v_lp.setMargins(mL, mT, mR, mB);
						folder.setVisibility(View.VISIBLE);
					} else {
						v_lp.setMargins(0, 0, 0, 0);
						folder.setVisibility(View.INVISIBLE);
					}
					gradient.setLayoutParams(v_lp);
				}
			});
			parent.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if ((boolean)_data.get((int)(_position)).get("folder")) {
						String clickedId = String.valueOf(_data.get(_position).get("id"));
						String clickedName = String.valueOf(_data.get(_position).get("name"));
						
						folderIdStack.add(clickedId);
						folderNameStack.add(clickedName);
						
						folderPath = joinWithSlash(folderNameStack);
						inFolder = true;
						applySortFilter(
						search_txt.getText().toString(),
						loadSortTypeId(),
						loadOrderId(),
						loadFilterId()
						);
						return;
					} else {
						try {
							HashMap<String, Object> card = _data.get((int) _position);
							
							int cur = 0;
							Object raw = card.get("used");
							if (raw instanceof Number) {
								cur = ((Number) raw).intValue();
							} else if (raw != null) {
								cur = Integer.parseInt(String.valueOf(raw));
							}
							
							cur++;
							card.put("used", cur);
							
							card_prefs.edit().putString("cards", new Gson().toJson(cards_list_all)).apply();
							
						} catch (Exception ignore) { }
						bottomShii = new com.google.android.material.bottomsheet.BottomSheetDialog(MainActivity.this);
						View bottomShiiV;
						bottomShiiV = getLayoutInflater().inflate(R.layout.card_info_dialog,null );
						bottomShii.setContentView(bottomShiiV);
						bottomShii.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
						final EditText card_name_txt = (EditText) bottomShiiV.findViewById(R.id.card_name_txt);
						final TextView card_code_txt = (TextView) bottomShiiV.findViewById(R.id.card_code_txt);
						final TextView card_type_txt = (TextView) bottomShiiV.findViewById(R.id.card_type_txt);
						final ImageView code_img = (ImageView) bottomShiiV.findViewById(R.id.code_img);
						final ImageView fav_btn = (ImageView) bottomShiiV.findViewById(R.id.fav_btn);
						final ImageView edit_btn = (ImageView) bottomShiiV.findViewById(R.id.edit_btn);
						final LinearLayout dialog_parent = (LinearLayout) bottomShiiV.findViewById(R.id.parent);
						final LinearLayout img_parent = (LinearLayout) bottomShiiV.findViewById(R.id.img_parent);
						bottomShii.setCancelable(true);
						editable = false;
						float scale = textScaleFromLevel((int) textLevel);
						applyTextScale(card_name_txt, scale);
						TextView[] views = new TextView[] { card_code_txt, card_type_txt };
						for (TextView tv : views) {
							if (tv != null) applyTextScale(tv, scale);
						}
						bottomShii.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								
								if (loadSortTypeId() == R.id.by_use_count) {
									applySortFilter(
									search_txt.getText().toString(),
									loadSortTypeId(),
									loadOrderId(),
									loadFilterId()
									);
								}
							}
						});
						card_name_txt.setText(_data.get((int)(_position)).get("name").toString());
						card_code_txt.setText(getString(R.string.card_code).concat(" ".concat(_data.get((int)(_position)).get("code").toString())));
						card_type_txt.setText(getString(R.string.card_type).concat(" ".concat(_data.get((int)(_position)).get("type").toString())));
						KeyListener card_name_txt_kl = card_name_txt.getKeyListener();
						InputMethodManager card_name_txt_imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						card_name_txt.setFocusable(false);
						card_name_txt.setFocusableInTouchMode(false);
						card_name_txt.setClickable(false);
						card_name_txt.setCursorVisible(false);
						card_name_txt.setKeyListener(null);
						card_name_txt_imm.hideSoftInputFromWindow(card_name_txt.getWindowToken(), 0);
						fav_btn.setSelected((boolean) _data.get(_position).get("favorite"));
						dialog_parent.setClickable(true);
						
						final float dialog_parent_rTL = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 12,
						getResources().getDisplayMetrics()
						);
						
						final float dialog_parent_rTR = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 12,
						getResources().getDisplayMetrics()
						);
						
						final float dialog_parent_rBR = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 0,
						getResources().getDisplayMetrics()
						);
						
						final float dialog_parent_rBL = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						(float) 0,
						getResources().getDisplayMetrics()
						);
						
						dialog_parent.setBackground(new ShapeDrawable(new RoundRectShape(
						new float[]{
							dialog_parent_rTL, dialog_parent_rTL,
							dialog_parent_rTR, dialog_parent_rTR,
							dialog_parent_rBR, dialog_parent_rBR,
							dialog_parent_rBL, dialog_parent_rBL
						},
						null,
						null
						)) {{
								getPaint().setColor(0xFFF2EAF5);
							}});
						img_parent.setClickable(true);
						final float img_parent_rTL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 0, getResources().getDisplayMetrics());
						final float img_parent_rTR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 0, getResources().getDisplayMetrics());
						final float img_parent_rBR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 0, getResources().getDisplayMetrics());
						final float img_parent_rBL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 0, getResources().getDisplayMetrics());
						final int img_parent_strokePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 2, getResources().getDisplayMetrics());
						final GradientDrawable img_parent_bg = new GradientDrawable();
						img_parent_bg.setColor(Color.TRANSPARENT);
						img_parent_bg.setCornerRadii(new float[]{img_parent_rTL,img_parent_rTL,img_parent_rTR,img_parent_rTR,img_parent_rBR,img_parent_rBR,img_parent_rBL,img_parent_rBL});
						img_parent_bg.setStroke(img_parent_strokePx, 0xFFCAC5CC);
						img_parent.setBackground(img_parent_bg);
						edit_btn.setClickable(true);
						edit_btn.setBackground(new RippleDrawable(
						new ColorStateList(
						new int[][]{new int[]{}},
						new int[]{0xFFC3A2CF}
						),
						new GradientDrawable() {
							public GradientDrawable getIns(int a, int b, int c, int d) {
								this.setCornerRadius(a);
								this.setStroke(b, c);
								this.setColor(d);
								return this;
							}
						}.getIns((int)360, (int)0, Color.TRANSPARENT, 0xFFF2EAF5), 
						null
						));
						
						String code_img_data = _data.get((int)(_position)).get("code").toString();
						String code_img_typeStr = _data.get((int)(_position)).get("type").toString();
						
						int code_img_targetW = (int)(SketchwareUtil.getDisplayWidthPixels(getApplicationContext()) * 0.8d);
						
						try {
							BarcodeFormat code_img_format = BarcodeFormat.valueOf(code_img_typeStr);
							
							boolean code_img_is2D =
							code_img_format == BarcodeFormat.QR_CODE ||
							code_img_format == BarcodeFormat.DATA_MATRIX ||
							code_img_format == BarcodeFormat.AZTEC ||
							code_img_format == BarcodeFormat.PDF_417;
							
							int code_img_targetH;
							if (code_img_is2D) {
								code_img_targetH = code_img_targetW;
							} else {
								code_img_targetH = (int) (code_img_targetW * 0.35d);
								if (code_img_targetH < 180) code_img_targetH = 180;
							}
							
							ViewGroup.LayoutParams code_img_lp = code_img.getLayoutParams();
							code_img_lp.width = code_img_targetW;
							code_img_lp.height = code_img_targetH;
							code_img.setLayoutParams(code_img_lp);
							
							Map<EncodeHintType, Object> code_img_hints = new EnumMap<>(EncodeHintType.class);
							code_img_hints.put(EncodeHintType.MARGIN, 2);
							
							BitMatrix code_img_bm = new MultiFormatWriter().encode(code_img_data, code_img_format, code_img_targetW, code_img_targetH, code_img_hints);
							
							Bitmap code_img_bmp = Bitmap.createBitmap(code_img_targetW, code_img_targetH, Bitmap.Config.RGB_565);
							for (int code_img_y = 0; code_img_y < code_img_targetH; code_img_y++) {
								for (int code_img_x = 0; code_img_x < code_img_targetW; code_img_x++) {
									code_img_bmp.setPixel(code_img_x, code_img_y, code_img_bm.get(code_img_x, code_img_y) ? 0xFF000000 : 0xFFFFFFFF);
								}
							}
							
							code_img.setImageBitmap(code_img_bmp);
							
							
						} catch (Exception code_img_e) {
							SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.code_fail).concat(" ".concat(code_img_e.getClass().getSimpleName())));
						}
						fav_btn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								toggleFavorite(fav_btn, false, _data, _position);
							}
						});
						edit_btn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								if (editable) {
									if (card_name_txt.getText().toString().isEmpty()) {
										SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.invalid_edit));
									} else {
										card_name_txt.setFocusable(false);
										card_name_txt.setFocusableInTouchMode(false);
										card_name_txt.setClickable(false);
										card_name_txt.setCursorVisible(false);
										card_name_txt.setKeyListener(null);
										card_name_txt_imm.hideSoftInputFromWindow(card_name_txt.getWindowToken(), 0);
										edit_btn.setImageResource(R.drawable.ic_edit);
										editable = false;
										_data.get((int)(_position)).put("name", card_name_txt.getText().toString());
										card_prefs.edit().putString("cards", new Gson().toJson(cards_list_all)).commit();
										applySortFilter(
										search_txt.getText().toString(),
										loadSortTypeId(),
										loadOrderId(),
										loadFilterId()
										);
									}
								} else {
									card_name_txt.setFocusable(true);
									card_name_txt.setFocusableInTouchMode(true);
									card_name_txt.setClickable(true);
									card_name_txt.setCursorVisible(true);
									card_name_txt.setKeyListener(card_name_txt_kl);
									card_name_txt.requestFocus();
									card_name_txt.setSelection(card_name_txt.getText().length());
									card_name_txt_imm.showSoftInput(card_name_txt, InputMethodManager.SHOW_IMPLICIT);
									edit_btn.setImageResource(R.drawable.ic_check);
									editable = true;
								}
							}
						});
						bottomShii.show();
					}
				}
			});
			parent.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View _view) {
					delFolder = (boolean)_data.get((int)(_position)).get("folder");
					d = new AlertDialog.Builder(MainActivity.this).create();
					LayoutInflater dLI = getLayoutInflater();
					View dCV = (View) dLI.inflate(R.layout.dialog, null);
					d.setView(dCV);
					final TextView message_txt = (TextView)
					dCV.findViewById(R.id.message_txt);
					final TextView positive_txt = (TextView)
					dCV.findViewById(R.id.positive_txt);
					final TextView negative_txt = (TextView)
					dCV.findViewById(R.id.negative_txt);
					final LinearLayout dialog_parent = (LinearLayout)
					dCV.findViewById(R.id.parent);
					final LinearLayout buttons_bar = (LinearLayout)
					dCV.findViewById(R.id.buttons_bar);
					float scale = textScaleFromLevel((int) textLevel);
					TextView[] views = new TextView[] { message_txt, positive_txt, negative_txt };
					for (TextView tv : views) {
						if (tv != null) applyTextScale(tv, scale);
					}
					positive_txt.setClickable(true);
					positive_txt.setBackground(new RippleDrawable(
					new ColorStateList(
					new int[][]{new int[]{}},
					new int[]{0xFFF2EAF5}
					),
					new GradientDrawable() {
						public GradientDrawable getIns(int a, int b, int c, int d) {
							this.setCornerRadius(a);
							this.setStroke(b, c);
							this.setColor(d);
							return this;
						}
					}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFD2B6DC), 
					null
					));
					
					negative_txt.setClickable(true);
					negative_txt.setBackground(new RippleDrawable(
					new ColorStateList(
					new int[][]{new int[]{}},
					new int[]{0xFFF2EAF5}
					),
					new GradientDrawable() {
						public GradientDrawable getIns(int a, int b, int c, int d) {
							this.setCornerRadius(a);
							this.setStroke(b, c);
							this.setColor(d);
							return this;
						}
					}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFD2B6DC), 
					null
					));
					
					dialog_parent.setClickable(true);
					final float dialog_parent_rTL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
					final float dialog_parent_rTR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
					final float dialog_parent_rBR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
					final float dialog_parent_rBL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
					final int dialog_parent_strokePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 2, getResources().getDisplayMetrics());
					final GradientDrawable dialog_parent_bg = new GradientDrawable();
					dialog_parent_bg.setColor(0xFFFFFFFF);
					dialog_parent_bg.setCornerRadii(new float[]{dialog_parent_rTL,dialog_parent_rTL,dialog_parent_rTR,dialog_parent_rTR,dialog_parent_rBR,dialog_parent_rBR,dialog_parent_rBL,dialog_parent_rBL});
					dialog_parent_bg.setStroke(dialog_parent_strokePx, 0xFF424242);
					dialog_parent.setBackground(dialog_parent_bg);
					buttons_bar.setClickable(true);
					
					final float buttons_bar_rTL = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					(float) 0,
					getResources().getDisplayMetrics()
					);
					
					final float buttons_bar_rTR = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					(float) 0,
					getResources().getDisplayMetrics()
					);
					
					final float buttons_bar_rBR = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					(float) 12,
					getResources().getDisplayMetrics()
					);
					
					final float buttons_bar_rBL = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					(float) 12,
					getResources().getDisplayMetrics()
					);
					
					buttons_bar.setBackground(new ShapeDrawable(new RoundRectShape(
					new float[]{
						buttons_bar_rTL, buttons_bar_rTL,
						buttons_bar_rTR, buttons_bar_rTR,
						buttons_bar_rBR, buttons_bar_rBR,
						buttons_bar_rBL, buttons_bar_rBL
					},
					null,
					null
					)) {{
							getPaint().setColor(0xFFD2B6DC);
						}});
					d.setCancelable(true);
					d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					if (delFolder) {
						message_txt.setText(getString(R.string.folder_del_ask));
					} else {
						message_txt.setText(getString(R.string.card_del_ask));
					}
					positive_txt.setText(getString(R.string.yes));
					negative_txt.setText(getString(R.string.no));
					positive_txt.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View _view) {
							int pos = (int) _position;
							String targetId = String.valueOf(_data.get(pos).get("id"));
							ArrayList<HashMap<String, Object>> container;
							if (inFolder && !folderIdStack.isEmpty()) {
								container = resolveContainerList(cards_list_all, folderIdStack);
							} else {
								container = cards_list_all;
							}
							boolean removed = removeByIdInList(container, targetId);
							if (!removed) removed = removeByIdRecursive(cards_list_all, targetId);
							card_prefs.edit().putString("cards", new Gson().toJson(cards_list_all)).commit();
							if (delFolder) {
								SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.folder_del_msg));
							} else {
								SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.card_del_msg));
							}
							applySortFilter(
							search_txt.getText().toString(),
							loadSortTypeId(),
							loadOrderId(),
							loadFilterId()
							);
							d.dismiss();
						}
					});
					negative_txt.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View _view) {
							d.dismiss();
						}
					});
					d.show();
					return true;
				}
			});
		}
		
		@Override
		public int getItemCount() {
			return _data.size();
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder {
			public ViewHolder(View v) {
				super(v);
			}
		}
	}
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}