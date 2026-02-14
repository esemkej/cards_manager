package com.eas.cards2;

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
import java.text.DecimalFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.*;
import jp.wasabeef.picasso.transformations.*;
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
import android.os.Build;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
	
	private FloatingActionButton _fab;
	private HashMap<String, Object> cards = new HashMap<>();
	private String cardSaveName = "";
	private String cardSaveType = "";
	private String cardSaveCode = "";
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
	private String id = "";
	private EditText code_edit;
	private TextView type_txt;
	private HashMap<String, Object> types = new HashMap<>();
	private HashMap<String, Object> pictures = new HashMap<>();
	private static final int REQ_TAKE_PHOTO = 2001;
	private static final int REQ_PICK_IMAGE = 2002;
	private Uri pendingCameraUri = null;
	private String pendingCameraInternalPath = null;
	private long newId;
	private boolean scan = false;
	private boolean newCardSaved = false;
	private LinearLayout code_menu_lay;
	
	private ArrayList<HashMap<String, Object>> cards_list = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> cards_list_all = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> colors_list = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> types_list = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> pictures_list = new ArrayList<>();
	private ArrayList<String> pendingImages = new ArrayList<>();
	private ArrayList<String> pendingDelete = new ArrayList<>();
	
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
    private RecyclerView items_rec;
    private RecyclerView pictures_rec;
    private Cards_recAdapter cardsAdapter;
    private Colors_recAdapter colorsAdapter;
    private Items_recAdapter itemsAdapter;
    private Pictures_recAdapter picturesAdapter;
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
				final RadioButton by_type = (RadioButton) bottomShiiV.findViewById(R.id.by_type);
				final RadioButton ascending = (RadioButton) bottomShiiV.findViewById(R.id.ascending);
				final RadioButton descending = (RadioButton) bottomShiiV.findViewById(R.id.descending);
				final RadioButton all = (RadioButton) bottomShiiV.findViewById(R.id.all);
				final RadioButton favorites = (RadioButton) bottomShiiV.findViewById(R.id.favorites);
				float scale = textScaleFromLevel((int) textLevel);
				applyTextScale(sort_by_txt, scale);
				RadioButton[] views = new RadioButton[] { by_name, by_date_created, by_use_count, by_type, ascending, descending, all, favorites};
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
											SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.import_fail).concat(" ".concat(e.getMessage())));
										}
										_loadLastId();
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
						final androidx.recyclerview.widget.GridLayoutManager cards_rec_layoutManager =
						new androidx.recyclerview.widget.GridLayoutManager(
						cards_rec.getContext(),
						(int) progress,
						androidx.recyclerview.widget.RecyclerView.VERTICAL,
						false
						);
						cards_rec.setLayoutManager(cards_rec_layoutManager);
						
						final android.content.Context cards_rec_ctx = cards_rec.getContext();
						
						final int cards_rec_hSpacingPx = Math.round(android.util.TypedValue.applyDimension(
						android.util.TypedValue.COMPLEX_UNIT_DIP,
						(float) 8,
						cards_rec_ctx.getResources().getDisplayMetrics()
						));
						
						final int cards_rec_vSpacingPx = Math.round(android.util.TypedValue.applyDimension(
						android.util.TypedValue.COMPLEX_UNIT_DIP,
						(float) 2,
						cards_rec_ctx.getResources().getDisplayMetrics()
						));
						
						if (cards_rec.getItemDecorationCount() > 0) {
							cards_rec.removeItemDecorationAt(0);
						}
						
						cards_rec.addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
							@Override
							public void getItemOffsets(android.graphics.Rect outRect,
							android.view.View view,
							androidx.recyclerview.widget.RecyclerView parent,
							androidx.recyclerview.widget.RecyclerView.State state) {
								
								int cards_rec_pos = parent.getChildAdapterPosition(view);
								if (cards_rec_pos == androidx.recyclerview.widget.RecyclerView.NO_POSITION) return;
								
								androidx.recyclerview.widget.RecyclerView.LayoutManager cards_rec_lm =
								parent.getLayoutManager();
								if (!(cards_rec_lm instanceof androidx.recyclerview.widget.GridLayoutManager)) return;
								
								androidx.recyclerview.widget.GridLayoutManager cards_rec_glm =
								(androidx.recyclerview.widget.GridLayoutManager) cards_rec_lm;
								
								int cards_rec_spanCount = cards_rec_glm.getSpanCount();
								
								androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup cards_rec_ssl =
								cards_rec_glm.getSpanSizeLookup();
								
								int cards_rec_spanSize = cards_rec_ssl.getSpanSize(cards_rec_pos);
								int cards_rec_spanIndex = cards_rec_ssl.getSpanIndex(cards_rec_pos, cards_rec_spanCount);
								
								outRect.left =
								(cards_rec_spanIndex * cards_rec_hSpacingPx) / cards_rec_spanCount;
								
								outRect.right =
								cards_rec_hSpacingPx
								- ((cards_rec_spanIndex + cards_rec_spanSize) * cards_rec_hSpacingPx)
								/ cards_rec_spanCount;
								
								if (cards_rec_pos >= cards_rec_spanCount) {
									outRect.top = cards_rec_vSpacingPx;
								}
							}
						});
					}
				});
				bottomShii.show();
				if ((boolean)settings.get("settings_tutorial") && !debug) {
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
				_displayInfo(cards_list, 0, true);
			}
		});
	}
	
	private void initializeLogic() {
		debug = false;
		if (card_prefs.contains("settings")) {
			settings = new Gson().fromJson(card_prefs.getString("settings", ""), new TypeToken<HashMap<String, Object>>(){}.getType());
			final androidx.recyclerview.widget.GridLayoutManager cards_rec_layoutManager =
			new androidx.recyclerview.widget.GridLayoutManager(
			cards_rec.getContext(),
			(int) (double)settings.get("grid_amount"),
			androidx.recyclerview.widget.RecyclerView.VERTICAL,
			false
			);
			cards_rec.setLayoutManager(cards_rec_layoutManager);
			
			final android.content.Context cards_rec_ctx = cards_rec.getContext();
			
			final int cards_rec_hSpacingPx = Math.round(android.util.TypedValue.applyDimension(
			android.util.TypedValue.COMPLEX_UNIT_DIP,
			(float) 8,
			cards_rec_ctx.getResources().getDisplayMetrics()
			));
			
			final int cards_rec_vSpacingPx = Math.round(android.util.TypedValue.applyDimension(
			android.util.TypedValue.COMPLEX_UNIT_DIP,
			(float) 2,
			cards_rec_ctx.getResources().getDisplayMetrics()
			));
			
			if (cards_rec.getItemDecorationCount() > 0) {
				cards_rec.removeItemDecorationAt(0);
			}
			
			cards_rec.addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
				@Override
				public void getItemOffsets(android.graphics.Rect outRect,
				android.view.View view,
				androidx.recyclerview.widget.RecyclerView parent,
				androidx.recyclerview.widget.RecyclerView.State state) {
					
					int cards_rec_pos = parent.getChildAdapterPosition(view);
					if (cards_rec_pos == androidx.recyclerview.widget.RecyclerView.NO_POSITION) return;
					
					androidx.recyclerview.widget.RecyclerView.LayoutManager cards_rec_lm =
					parent.getLayoutManager();
					if (!(cards_rec_lm instanceof androidx.recyclerview.widget.GridLayoutManager)) return;
					
					androidx.recyclerview.widget.GridLayoutManager cards_rec_glm =
					(androidx.recyclerview.widget.GridLayoutManager) cards_rec_lm;
					
					int cards_rec_spanCount = cards_rec_glm.getSpanCount();
					
					androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup cards_rec_ssl =
					cards_rec_glm.getSpanSizeLookup();
					
					int cards_rec_spanSize = cards_rec_ssl.getSpanSize(cards_rec_pos);
					int cards_rec_spanIndex = cards_rec_ssl.getSpanIndex(cards_rec_pos, cards_rec_spanCount);
					
					outRect.left =
					(cards_rec_spanIndex * cards_rec_hSpacingPx) / cards_rec_spanCount;
					
					outRect.right =
					cards_rec_hSpacingPx
					- ((cards_rec_spanIndex + cards_rec_spanSize) * cards_rec_hSpacingPx)
					/ cards_rec_spanCount;
					
					if (cards_rec_pos >= cards_rec_spanCount) {
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
			final androidx.recyclerview.widget.GridLayoutManager cards_rec_layoutManager =
			new androidx.recyclerview.widget.GridLayoutManager(
			cards_rec.getContext(),
			(int) 2,
			androidx.recyclerview.widget.RecyclerView.VERTICAL,
			false
			);
			cards_rec.setLayoutManager(cards_rec_layoutManager);
			
			final android.content.Context cards_rec_ctx = cards_rec.getContext();
			
			final int cards_rec_hSpacingPx = Math.round(android.util.TypedValue.applyDimension(
			android.util.TypedValue.COMPLEX_UNIT_DIP,
			(float) 8,
			cards_rec_ctx.getResources().getDisplayMetrics()
			));
			
			final int cards_rec_vSpacingPx = Math.round(android.util.TypedValue.applyDimension(
			android.util.TypedValue.COMPLEX_UNIT_DIP,
			(float) 2,
			cards_rec_ctx.getResources().getDisplayMetrics()
			));
			
			if (cards_rec.getItemDecorationCount() > 0) {
				cards_rec.removeItemDecorationAt(0);
			}
			
			cards_rec.addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
				@Override
				public void getItemOffsets(android.graphics.Rect outRect,
				android.view.View view,
				androidx.recyclerview.widget.RecyclerView parent,
				androidx.recyclerview.widget.RecyclerView.State state) {
					
					int cards_rec_pos = parent.getChildAdapterPosition(view);
					if (cards_rec_pos == androidx.recyclerview.widget.RecyclerView.NO_POSITION) return;
					
					androidx.recyclerview.widget.RecyclerView.LayoutManager cards_rec_lm =
					parent.getLayoutManager();
					if (!(cards_rec_lm instanceof androidx.recyclerview.widget.GridLayoutManager)) return;
					
					androidx.recyclerview.widget.GridLayoutManager cards_rec_glm =
					(androidx.recyclerview.widget.GridLayoutManager) cards_rec_lm;
					
					int cards_rec_spanCount = cards_rec_glm.getSpanCount();
					
					androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup cards_rec_ssl =
					cards_rec_glm.getSpanSizeLookup();
					
					int cards_rec_spanSize = cards_rec_ssl.getSpanSize(cards_rec_pos);
					int cards_rec_spanIndex = cards_rec_ssl.getSpanIndex(cards_rec_pos, cards_rec_spanCount);
					
					outRect.left =
					(cards_rec_spanIndex * cards_rec_hSpacingPx) / cards_rec_spanCount;
					
					outRect.right =
					cards_rec_hSpacingPx
					- ((cards_rec_spanIndex + cards_rec_spanSize) * cards_rec_hSpacingPx)
					/ cards_rec_spanCount;
					
					if (cards_rec_pos >= cards_rec_spanCount) {
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
		if ((boolean)settings.get("main_tutorial") && !debug) {
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
		if (_requestCode == REQ_TAKE_PHOTO) {
			if (_resultCode != Activity.RESULT_OK) {
				if (pendingCameraInternalPath != null) {
					File f = new File(pendingCameraInternalPath);
					if (f.exists()) f.delete();
				}
				pendingCameraUri = null;
				pendingCameraInternalPath = null;
				return;
			}
			
			if (pendingCameraInternalPath == null) return;
			
			File f = new File(pendingCameraInternalPath);
			if (f.exists() && f.length() > 0) {
				String filename = f.getName();
				pendingImages.add(filename);
				HashMap<String, Object> m = new HashMap<>();
				m.put("image", filename);
				int insertPos = Math.max(0, pictures_list.size() - 1);
				pictures_list.add(insertPos, m);
				picturesAdapter.notifyItemInserted(insertPos);
			} else {
				if (f.exists()) f.delete();
				SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.photo_save_error));
			}
			
			pendingCameraUri = null;
			pendingCameraInternalPath = null;
			return;
		}
		
		if (_requestCode == REQ_PICK_IMAGE) {
			if (_resultCode != Activity.RESULT_OK || _data == null) return;
			
			Uri uri = _data.getData();
			if (uri == null) return;
			
			try {
				String filename = copyPickedImageIntoInternalStorage(id, uri);
				pendingImages.add(filename);
				HashMap<String, Object> m = new HashMap<>();
				m.put("image", filename);
				int insertPos = Math.max(0, pictures_list.size() - 1);
				pictures_list.add(insertPos, m);
				picturesAdapter.notifyItemInserted(insertPos);
			} catch (Exception e) {
				SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.photo_import_error) + " " + e.getMessage());
			}
			return;
		}
		
		if (_resultCode != Activity.RESULT_OK || _data == null) return;
		
		Uri uri = _data.getData();
		if (uri != null) {
			final int takeFlags = _data.getFlags()
			& (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			try {
				getContentResolver().takePersistableUriPermission(uri, takeFlags);
			} catch (Exception ignored) {}
		}
		
		if (_requestCode == REQ_EXPORT_JSON) {
			exportCardsToUri(uri);
		} else if (_requestCode == REQ_IMPORT_JSON) {
			importCardsFromUri(uri);
		}
		
		switch (_requestCode) {
			
			default:
			break;
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
			if (bottomShii != null && bottomShii.isShowing() && code_edit != null) {
				code_menu_lay.setVisibility(View.VISIBLE);
				code_edit.setText(card_prefs.getString("code", ""));
				type_txt.setText(getString(R.string.card_type).concat(" ".concat(card_prefs.getString("type", ""))));
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
	private void openSystemCameraForCard(String cardId) {
		try {
			File dir = new File(getFilesDir(), "card_images/" + cardId);
			if (!dir.exists()) dir.mkdirs();
			
			String uuid = UUID.randomUUID().toString();
			File outFile = new File(dir, "img_" + uuid + ".jpg");
			
			Uri outUri = FileProvider.getUriForFile(
			this,
			getPackageName() + ".fileprovider",
			outFile
			);
			
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
			intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			
			pendingCameraUri = outUri;
			pendingCameraInternalPath = outFile.getAbsolutePath();
			
			startActivityForResult(intent, REQ_TAKE_PHOTO);
		} catch (Exception e) {
			SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.camera_failed) + " " + e.getMessage());
			pendingCameraUri = null;
			pendingCameraInternalPath = null;
		}
	}
	private void pickImageForCard() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
		startActivityForResult(intent, REQ_PICK_IMAGE);
	}
	private String copyPickedImageIntoInternalStorage(String cardId, Uri srcUri) throws Exception {
		File dir = new File(getFilesDir(), "card_images/" + cardId);
		if (!dir.exists()) dir.mkdirs();
		
		String uuid = UUID.randomUUID().toString();
		
		String ext = "jpg";
		String mime = getContentResolver().getType(srcUri);
		if (mime != null) {
			String guessed = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
			if (guessed != null && guessed.length() > 0) ext = guessed;
		}
		
		String filename = "img_" + uuid + "." + ext;
		File outFile = new File(dir, filename);
		
		try (InputStream in = getContentResolver().openInputStream(srcUri);
		OutputStream out = new FileOutputStream(outFile)) {
			
			if (in == null) throw new Exception("Unable to open selected image");
			
			byte[] buf = new byte[8192];
			int r;
			while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
			out.flush();
		}
		
		if (!outFile.exists() || outFile.length() == 0) {
			if (outFile.exists()) outFile.delete();
			throw new Exception("Copy failed (empty file)");
		}
		
		return filename;
	}
	private void deleteRecursive(File f) {
		if (f == null || !f.exists()) return;
		
		if (f.isDirectory()) {
			File[] kids = f.listFiles();
			if (kids != null) {
				for (File c : kids) deleteRecursive(c);
			}
		}
		f.delete();
	}
	private boolean isValidCode(String input) {
		types_list.clear();
		if (isValidEan13(input)) {
			types = new HashMap<>();
			types.put("type", "EAN_13");
			types_list.add(types);
			types = new HashMap<>();
			types.put("type", getString(R.string.none));
			types.put("label", getString(R.string.removes_code));
			types_list.add(types);
			return true;
		} else if (input.matches("\\d+") && input.length() == 13 && !isValidChecksum(input)) {
			types = new HashMap<>();
			types.put("type", "EAN_13");
			types.put("label", getString(R.string.invalid_checksum));
			types_list.add(types);
			types = new HashMap<>();
			types.put("type", "CODE_128");
			types_list.add(types);
			types = new HashMap<>();
			types.put("type", "QR_CODE");
			types_list.add(types);
			types = new HashMap<>();
			types.put("type", getString(R.string.none));
			types.put("label", getString(R.string.removes_code));
			types_list.add(types);
			return true;
		} else if (isValidCode128(input)) {
			types = new HashMap<>();
			types.put("type", "CODE_128");
			types.put("label", getString(R.string.recommended));
			types_list.add(types);
			types = new HashMap<>();
			types.put("type", "QR_CODE");
			types_list.add(types);
			types = new HashMap<>();
			types.put("type", getString(R.string.none));
			types.put("label", getString(R.string.removes_code));
			types_list.add(types);
			return true;
		} else if (input.length() > 0) {
			types = new HashMap<>();
			types.put("type", "QR_CODE");
			types_list.add(types);
			types = new HashMap<>();
			types.put("type", getString(R.string.none));
			types.put("label", getString(R.string.removes_code));
			types_list.add(types);
			return true;
		} else if (input.length() == 0) {
			types = new HashMap<>();
			types.put("type", getString(R.string.none));
			types.put("label", getString(R.string.removes_code));
			types_list.add(types);
			return true;
		} else {
			return false;
		}    
	}
	private boolean isValidEan13(String input) {
		if (input.length() != 13 || !input.matches("\\d+")) {
			return false;
		}
		return isValidChecksum(input);
	}
	private boolean isValidChecksum(String input) {
		int sum = 0;
		for (int i = 0; i < 12; i++) {
			int digit = Character.getNumericValue(input.charAt(i));
			sum += (i % 2 == 0) ? digit : digit * 3;
		}
		int calculatedChecksum = (10 - (sum % 10)) % 10;
		int providedChecksum = Character.getNumericValue(input.charAt(12));
		return calculatedChecksum == providedChecksum;
	}
	private boolean isValidCode128(String input) {
		int len = input.length();
		if (len < 1 || len > 30) {
			return false;
		}
		for (char c : input.toCharArray()) {
			if (c < 32 || c > 126) {
				return false;
			}
		}
		return true;
	}
	public static int indexOfCardById(
	ArrayList<HashMap<String, Object>> cardsList,
	String targetId
	) {
		if (cardsList == null || targetId == null) return -1;
		
		for (int i = 0; i < cardsList.size(); i++) {
			Object id = cardsList.get(i).get("id");
			if (id instanceof String && targetId.equals(id)) {
				return i;
			}
		}
		return -1;
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
			
			_loadLastId();
			applySortFilter(
			search_txt.getText().toString(),
			loadSortTypeId(),
			loadOrderId(),
			loadFilterId()
			);
			
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
				
				_loadLastId();
				applySortFilter(
				search_txt.getText().toString(),
				loadSortTypeId(),
				loadOrderId(),
				loadFilterId()
				);
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
				} else if (sortTypeCheckedId == R.id.by_type) {
					
					boolean af = getBool(a, "folder", false);
					boolean bf = getBool(b, "folder", false);
					
					if (af == bf) {
						res = 0;
					} else {
						res = af ? -1 : 1;
					}
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
			if (scan) {
				i.setClass(getApplicationContext(), ScannerActivity.class);
				startActivity(i);
				return;
			} else {
				openSystemCameraForCard(id);
			}    
		}
		
		if (checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
			if (scan) {
				i.setClass(getApplicationContext(), ScannerActivity.class);
				startActivity(i);
			} else {
				openSystemCameraForCard(id);
			}    
		} else {
			requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQ_CAMERA);
		}
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
		if (requestCode == REQ_CAMERA) {
			if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
				if (scan) {
					i.setClass(getApplicationContext(), ScannerActivity.class);
					startActivity(i);
				} else {
					openSystemCameraForCard(id);
				}
				
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
		if (!isNew) {
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
						if ("lr".equals(selectedGradStyle)) {
							grad_styles.check(R.id.leftright);
						} else if ("rl".equals(selectedGradStyle)) {
							grad_styles.check(R.id.rightleft);
						} else if ("tb".equals(selectedGradStyle)) {
							grad_styles.check(R.id.topbottom);
						} else if ("bt".equals(selectedGradStyle)) {
							grad_styles.check(R.id.bottomtop);
						} else if ("tl_br".equals(selectedGradStyle)) {
							grad_styles.check(R.id.tl_br);
						} else if ("tr_bl".equals(selectedGradStyle)) {
							grad_styles.check(R.id.tr_bl);
						} else if ("bl_tr".equals(selectedGradStyle)) {
							grad_styles.check(R.id.bl_tr);
						} else {
							grad_styles.check(R.id.leftright);
						}
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
			parent.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View _view) {
					int pos = _holder.getBindingAdapterPosition();
					if (!(_data.get(pos).get("color").toString().equals("plus") || _data.get(pos).get("color").toString().equals("settings"))) {
						_data.remove(pos);
						colorsAdapter.notifyItemRemoved(pos);
						colorsAdapter.notifyItemRangeChanged(pos, _data.size() - pos);
					}
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
	public class Items_recAdapter extends RecyclerView.Adapter<Items_recAdapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Items_recAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dropdown_recycler, parent, false);
			RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			v.setLayoutParams(lp);
			return new ViewHolder(v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, final int _position) {
			View _view = _holder.itemView;
			final LinearLayout item = _view.findViewById(R.id.item);
			final LinearLayout div = _view.findViewById(R.id.div);
			final TextView item_txt = _view.findViewById(R.id.item_txt);
			final TextView label_txt = _view.findViewById(R.id.label_txt);
			item_txt.setText(_data.get((int)(_position)).get("type").toString());
			item.setClickable(true);
			item.setBackground(new RippleDrawable(
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
			}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFFFFFFF), 
			null
			));
			
			applyTextScale(item_txt, textScaleFromLevel((int) textLevel));
			if (_data.get((int)(_position)).containsKey("label")) {
				label_txt.setVisibility(View.VISIBLE);
				applyTextScale(label_txt, textScaleFromLevel((int) textLevel));
				String label = _data.get(_position).get("label").toString();
				label_txt.setText(label);
			} else {
				label_txt.setVisibility(View.GONE);
			}
			if (_position == (_data.size() - 1)) {
				div.setVisibility(View.GONE);
			} else {
				div.setVisibility(View.VISIBLE);
			}
			item.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (_data.get((int)(_position)).containsKey("label") && _data.get((int)(_position)).get("label").toString().equals(getString(R.string.invalid_checksum))) {
						_showEanWarning();
					} else {
						String type = _data.get(_position).get("type").toString();
						if (type.equals(getString(R.string.none))) {
							code_edit.setText("");
						}
						cardSaveType = type;
						type_txt.setText(getString(R.string.card_type).concat(" ".concat(type)));
						p.dismiss();
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
	public class Pictures_recAdapter extends RecyclerView.Adapter<Pictures_recAdapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Pictures_recAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pictures_recycler, parent, false);
			RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			v.setLayoutParams(lp);
			return new ViewHolder(v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, final int _position) {
			View _view = _holder.itemView;
			final LinearLayout parent = _view.findViewById(R.id.parent);
			final ImageView picture = _view.findViewById(R.id.picture);
			parent.setClickable(true);
			parent.setBackground(new RippleDrawable(
			new ColorStateList(
			new int[][]{new int[]{}},
			new int[]{0xFF212121}
			),
			new GradientDrawable() {
				public GradientDrawable getIns(int a, int b, int c, int d) {
					this.setCornerRadius(a);
					this.setStroke(b, c);
					this.setColor(d);
					return this;
				}
			}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFFFFFFF), 
			null
			));
			
			parent.setClipToOutline(true);
			String v = String.valueOf(_data.get(_position).get("image"));
			if (v.equals("plus")) {
				picture.setImageResource(R.drawable.ic_add_grey);
			} else {
				File f = new File(getFilesDir(), "card_images/" + id + "/" + v);
				
				if (f.exists() && f.length() > 0) {
					Picasso.with(getApplicationContext())
					.load(f)
					.fit()
					.centerCrop()
					.into(picture);
				} else {
					picture.setImageResource(R.drawable.ic_broken_image);
				}
			}
			parent.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					if (v.equals("plus")) {
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
						pictures_rec = (RecyclerView) p_pv.findViewById(R.id.pictures_rec);
						camera_scanner_txt.setText(getString(R.string.take_photo));
						scan_from_image_txt.setText(getString(R.string.pick_image));
						camera_img.setImageResource(R.drawable.ic_camera);
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
						parent.getLocationOnScreen(p_loc);
						int p_anchorX = p_loc[0];
						int p_anchorY = p_loc[1];
						
						android.util.DisplayMetrics p_dm = getResources().getDisplayMetrics();
						int p_screenW = p_dm.widthPixels;
						int p_screenH = p_dm.heightPixels;
						
						int p_x = p_anchorX + parent.getWidth() - p_popupW;
						
						int p_yBelow = p_anchorY + parent.getHeight();
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
								scan = false;
								openScannerOrRequestPermission();
							}
						});
						image_lay.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View _view) {
								p_dismissAnim.run();
								pickImageForCard();
							}
						});
						p_pv.setAlpha(0f);
						p_pv.setScaleX(0.94f);
						p_pv.setScaleY(0.94f);
						
						p.showAtLocation(
						parent,
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
					} else {
						_displayImage(v);
					}
				}
			});
			parent.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View _view) {
					int pos = _holder.getBindingAdapterPosition();
					if (!v.equals("plus")) {
						_data.remove(pos);
						picturesAdapter.notifyItemRemoved(pos);
						picturesAdapter.notifyItemRangeChanged(pos, _data.size() - pos);
						pendingDelete.add(v);
						pendingImages.remove(v);
					}
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
	
	
	public void _displayInfo(final ArrayList<HashMap<String, Object>> _data, final double _position, final boolean _newItem) {
		pendingImages.clear();
		pictures_list.clear();
		boolean noCode = false;
		if (_newItem) {
			newId = card_prefs.getLong("lastId", -1) + 1;
			cardSaveName = "";
			cardSaveType = getString(R.string.none);
			cardSaveCode = "";
			selectedGradStyle = "lr";
			id = String.valueOf(newId);;
			favorite = false;
			folder = false;
			newCardSaved = false;
		} else {
			cardSaveName = _data.get((int)(_position)).get("name").toString();
			selectedGradStyle = _data.get((int)(_position)).get("grad_style").toString();
			id = _data.get((int)(_position)).get("id").toString();
			favorite = (boolean)_data.get((int)(_position)).get("favorite");
			folder = (boolean)_data.get((int)(_position)).get("folder");
			if (!folder && (_data.get((int)(_position)).containsKey("type") && _data.get((int)(_position)).containsKey("code"))) {
				cardSaveType = _data.get((int)(_position)).get("type").toString();
				cardSaveCode = _data.get((int)(_position)).get("code").toString();
			} else {
				noCode = true;
			}
			if (_data.get((int)(_position)).containsKey("images")) {
				pendingImages.addAll((ArrayList<String>) _data.get((int) _position).get("images"));
				for (String fn : pendingImages) {
					if (fn == null) continue;
					HashMap<String, Object> m = new HashMap<>();
					m.put("image", fn);
					pictures_list.add(m);
				}
			}
		}
		bottomShii = new com.google.android.material.bottomsheet.BottomSheetDialog(MainActivity.this);
		View bottomShiiV;
		bottomShiiV = getLayoutInflater().inflate(R.layout.add_new_dialog,null );
		bottomShii.setContentView(bottomShiiV);
		bottomShii.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
		final TextInputEditText card_name_txt = (TextInputEditText) bottomShiiV.findViewById(R.id.card_name_txt);
		final TextView scan_btn = (TextView) bottomShiiV.findViewById(R.id.scan_btn);
		final TextView save_btn = (TextView) bottomShiiV.findViewById(R.id.save_btn);
		final TextView del_btn = (TextView) bottomShiiV.findViewById(R.id.del_btn);
		final TextView folder_txt = (TextView) bottomShiiV.findViewById(R.id.folder_txt);
		final TextView code_txt = (TextView) bottomShiiV.findViewById(R.id.code_txt);
		final TextView picture_gallery_txt = (TextView) bottomShiiV.findViewById(R.id.picture_gallery_txt);
		final TextView color_theme_txt = (TextView) bottomShiiV.findViewById(R.id.color_theme_txt);
		final ImageView fav_btn = (ImageView) bottomShiiV.findViewById(R.id.fav_btn);
		final ImageView folder_btn = (ImageView) bottomShiiV.findViewById(R.id.folder_btn);
		final ImageView dropdown_btn = (ImageView) bottomShiiV.findViewById(R.id.dropdown_btn);
		final ImageView code_img = (ImageView) bottomShiiV.findViewById(R.id.code_img);
		final LinearLayout img_parent = (LinearLayout) bottomShiiV.findViewById(R.id.img_parent);
		code_edit = bottomShiiV.findViewById(R.id.code_edit);
		type_txt = bottomShiiV.findViewById(R.id.type_txt);
		pictures_rec = bottomShiiV.findViewById(R.id.pictures_rec);
		colors_rec = bottomShiiV.findViewById(R.id.colors_rec);
		code_menu_lay = bottomShiiV.findViewById(R.id.code_menu_lay);
		float scale = textScaleFromLevel((int) textLevel);
		applyTextScale(card_name_txt, scale);
		applyTextScale(scan_btn, scale);
		applyTextScale(save_btn, scale);
		applyTextScale(folder_txt, scale);
		applyTextScale(del_btn, scale);
		applyTextScale(color_theme_txt, scale);
		if (!folder) {
			applyTextScale(code_txt, scale);
			applyTextScale(code_edit, scale);
			applyTextScale(type_txt, scale);
			applyTextScale(picture_gallery_txt, scale);
		}    
		bottomShii.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (_newItem && !newCardSaved) {
					File dir = new File(getFilesDir(), "card_images/" + id);
					if (dir.exists()) deleteRecursive(dir);
				}
				if (_newItem && !pendingImages.isEmpty()) {
					for (String v : pendingImages) {
						File f = new File(getFilesDir(), "card_images/" + id + "/" + v);
						if (f.exists()) f.delete();
					}
					pendingImages.clear();
				}
			}
		});
		bottomShii.setCancelable(true);
		if (_newItem) {
			img_parent.setVisibility(View.GONE);
			del_btn.setVisibility(View.GONE);
			pictures_rec.setVisibility(View.VISIBLE);
			picture_gallery_txt.setVisibility(View.VISIBLE);
			type_txt.setText(getString(R.string.card_type).concat(" ".concat(cardSaveType)));
		} else {
			card_name_txt.setText(cardSaveName);
			if (folder) {
				del_btn.setText(getString(R.string.del_folder));
				img_parent.setVisibility(View.GONE);
				code_menu_lay.setVisibility(View.GONE);
				pictures_rec.setVisibility(View.GONE);
				picture_gallery_txt.setVisibility(View.GONE);
			} else {
				if (noCode) {
					code_menu_lay.setVisibility(View.GONE);
					img_parent.setVisibility(View.GONE);
					picture_gallery_txt.setVisibility(View.GONE);
				} else {
					code_menu_lay.setVisibility(View.VISIBLE);
					img_parent.setVisibility(View.VISIBLE);
					picture_gallery_txt.setVisibility(View.VISIBLE);
					code_edit.setText(cardSaveCode);
					type_txt.setText(getString(R.string.card_type).concat(" ".concat(cardSaveType)));
					String code_img_data = cardSaveCode;
					String code_img_typeStr = cardSaveType;
					
					int code_img_targetW = (int)(SketchwareUtil.getDisplayWidthPixels(getApplicationContext()) * 0.95d);
					
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
				}
				del_btn.setText(getString(R.string.del_card));
				pictures_rec.setVisibility(View.VISIBLE);
			}
			del_btn.setClickable(true);
			del_btn.setBackground(new RippleDrawable(
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
			}.getIns((int)12, (int)2, 0xFF212121, 0xFFFF0000), 
			null
			));
			
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
			fav_btn.setSelected(favorite);
			folder_btn.setSelected(folder);
			folder_btn.setClickable(false);
			folder_btn.setFocusable(false);
		}
		if (inFolder) {
			folder_txt.setText(getString(R.string.folder).concat(" ".concat(folderPath)));
		} else {
			folder_txt.setVisibility(View.GONE);
		}
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
		
		if (folder) {
			scan_btn.setClickable(false);
			scan_btn.setFocusable(false);
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
		} else {
			final androidx.recyclerview.widget.GridLayoutManager pictures_rec_layoutManager =
			new androidx.recyclerview.widget.GridLayoutManager(
			pictures_rec.getContext(),
			(int) 1,
			androidx.recyclerview.widget.RecyclerView.HORIZONTAL,
			false
			);
			pictures_rec.setLayoutManager(pictures_rec_layoutManager);
			
			final android.content.Context pictures_rec_ctx = pictures_rec.getContext();
			
			final int pictures_rec_hSpacingPx = Math.round(android.util.TypedValue.applyDimension(
			android.util.TypedValue.COMPLEX_UNIT_DIP,
			(float) 4,
			pictures_rec_ctx.getResources().getDisplayMetrics()
			));
			
			final int pictures_rec_vSpacingPx = Math.round(android.util.TypedValue.applyDimension(
			android.util.TypedValue.COMPLEX_UNIT_DIP,
			(float) 0,
			pictures_rec_ctx.getResources().getDisplayMetrics()
			));
			
			if (pictures_rec.getItemDecorationCount() > 0) {
				pictures_rec.removeItemDecorationAt(0);
			}
			
			pictures_rec.addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
				@Override
				public void getItemOffsets(android.graphics.Rect outRect,
				android.view.View view,
				androidx.recyclerview.widget.RecyclerView parent,
				androidx.recyclerview.widget.RecyclerView.State state) {
					
					int pictures_rec_pos = parent.getChildAdapterPosition(view);
					if (pictures_rec_pos == androidx.recyclerview.widget.RecyclerView.NO_POSITION) return;
					
					androidx.recyclerview.widget.RecyclerView.LayoutManager pictures_rec_lm =
					parent.getLayoutManager();
					if (!(pictures_rec_lm instanceof androidx.recyclerview.widget.GridLayoutManager)) return;
					
					androidx.recyclerview.widget.GridLayoutManager pictures_rec_glm =
					(androidx.recyclerview.widget.GridLayoutManager) pictures_rec_lm;
					
					int pictures_rec_spanCount = pictures_rec_glm.getSpanCount();
					
					androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup pictures_rec_ssl =
					pictures_rec_glm.getSpanSizeLookup();
					
					int pictures_rec_spanSize = pictures_rec_ssl.getSpanSize(pictures_rec_pos);
					int pictures_rec_spanIndex = pictures_rec_ssl.getSpanIndex(pictures_rec_pos, pictures_rec_spanCount);
					
					outRect.left =
					(pictures_rec_spanIndex * pictures_rec_hSpacingPx) / pictures_rec_spanCount;
					
					outRect.right =
					pictures_rec_hSpacingPx
					- ((pictures_rec_spanIndex + pictures_rec_spanSize) * pictures_rec_hSpacingPx)
					/ pictures_rec_spanCount;
					
					if (pictures_rec_pos >= pictures_rec_spanCount) {
						outRect.top = pictures_rec_vSpacingPx;
					}
				}
			});
			pictures = new HashMap<>();
			pictures.put("image", "plus");
			pictures_list.add(pictures);
			picturesAdapter = new Pictures_recAdapter(pictures_list);
			pictures_rec.setAdapter(picturesAdapter);
			dropdown_btn.setClickable(true);
			dropdown_btn.setBackground(new RippleDrawable(
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
			}.getIns((int)360, (int)0, Color.TRANSPARENT, 0xFFF2EAF5), 
			null
			));
			
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
			
			scan_btn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View _clickedView){
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
													code_menu_lay.setVisibility(View.VISIBLE);
													code_edit.setText(text);
													type_txt.setText(getString(R.string.card_type).concat(" ".concat(type)));
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
						scan = true;
						openScannerOrRequestPermission();
					}
				}
			});
			scan_btn.setOnLongClickListener(new View.OnLongClickListener(){
				@Override
				public boolean onLongClick(View _longClickedView){
					
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
							scan = true;
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
														code_menu_lay.setVisibility(View.VISIBLE);
														code_edit.setText(text);
														type_txt.setText(getString(R.string.card_type).concat(" ".concat(type)));
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
					return false;
				}
			});
			dropdown_btn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View _clickedView){
					if (isValidCode(code_edit.getText().toString())) {
						try {
							if (p != null) p.dismiss();
						} catch (Exception p_e) {}
						
						LayoutInflater p_li = getLayoutInflater();
						View p_pv = p_li.inflate(R.layout.dropdown_layout, null);
						
						p = new PopupWindow(
						p_pv,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						true
						);
						
						p.setOutsideTouchable(true);
						p.setFocusable(true);
						p.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
						final LinearLayout parent = (LinearLayout) p_pv.findViewById(R.id.parent);
						items_rec = (RecyclerView) p_pv.findViewById(R.id.items_rec);
						
						items_rec.setLayoutManager(
						new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false)
						);
						itemsAdapter = new Items_recAdapter(types_list);
						items_rec.setAdapter(itemsAdapter);
						parent.setClickable(true);
						final float parent_rTL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final float parent_rTR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final float parent_rBR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final float parent_rBL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
						final int parent_strokePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 2, getResources().getDisplayMetrics());
						final GradientDrawable parent_bg = new GradientDrawable();
						parent_bg.setColor(0xFFFFFFFF);
						parent_bg.setCornerRadii(new float[]{parent_rTL,parent_rTL,parent_rTR,parent_rTR,parent_rBR,parent_rBR,parent_rBL,parent_rBL});
						parent_bg.setStroke(parent_strokePx, 0xFF212121);
						parent.setBackground(parent_bg);
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
						dropdown_btn.getLocationOnScreen(p_loc);
						int p_anchorX = p_loc[0];
						int p_anchorY = p_loc[1];
						
						android.util.DisplayMetrics p_dm = getResources().getDisplayMetrics();
						int p_screenW = p_dm.widthPixels;
						int p_screenH = p_dm.heightPixels;
						
						int p_x = p_anchorX + dropdown_btn.getWidth() - p_popupW;
						
						int p_yBelow = p_anchorY + dropdown_btn.getHeight();
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
						p_pv.setAlpha(0f);
						p_pv.setScaleX(0.94f);
						p_pv.setScaleY(0.94f);
						
						p.showAtLocation(
						dropdown_btn,
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
					} else {
						SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.unsupported_code));
					}
				}
			});
		}
		save_btn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View _clickedView){
				cardSaveName = card_name_txt.getText().toString();
				if (!folder) {
					cardSaveCode = code_edit.getText().toString();
				}
				if (_isValidItem()) {
					ArrayList<Integer> picked = new ArrayList<>();
					for (HashMap<String, Object> m : colors_list) {
						Object v = m.get("color");
						if (!(v instanceof Integer)) continue;
						picked.add((Integer) v);
					}
					cards = new HashMap<>();
					cards.put("name", cardSaveName);
					if (!folder) {
						if (!cardSaveType.equals(getString(R.string.none))) {
							cards.put("type", cardSaveType);
							cards.put("code", cardSaveCode);
						}
						if (!pendingImages.isEmpty()) {
							cards.put("images", new ArrayList<String>(pendingImages));
							pendingImages.clear();
						}
						if (!pendingDelete.isEmpty()) {
							for (String v : pendingDelete) {
								File f = new File(getFilesDir(), "card_images/" + id + "/" + v);
								if (f.exists()) f.delete();
							}
							pendingDelete.clear();
						}
					}
					cards.put("folder", folder);
					cards.put("grad_style", selectedGradStyle);
					cards.put("id", id);
					cards.put("favorite", favorite);
					cards.put("colors", new Gson().toJson(picked));
					if (_newItem) {
						newCardSaved = true;
						if (folder) {
							cards.put("data", new ArrayList<HashMap<String,Object>>());
						}
						cards.put("used", (double)(0));
						card_prefs.edit().putLong("lastId", newId).commit();
						_data.add(cards);
						if (inFolder) {
							ArrayList<HashMap<String, Object>> masterContainer =
							resolveContainerList(cards_list_all, folderIdStack);
							masterContainer.add(cards);
						} else {
							cards_list_all.add(cards);
						}
					} else {
						if (folder) {
							cards.put("data", (ArrayList<HashMap<String,Object>>)_data.get((int)(_position)).get("data"));
						}
						cards.put("used", (double)((double)_data.get((int)(_position)).get("used")));
						_data.set((int)(_position), cards);
						if (inFolder) {
							ArrayList<HashMap<String, Object>> masterContainer =
							resolveContainerList(cards_list_all, folderIdStack);
							int index = indexOfCardById(masterContainer, id);
							masterContainer.set(index, cards);
						} else {
							int index = indexOfCardById(cards_list_all, id);
							cards_list_all.set((int)(index), cards);
						}
					}
					card_prefs.edit().putString("cards", new Gson().toJson(cards_list_all)).commit();
					applySortFilter(
					search_txt.getText().toString(),
					loadSortTypeId(),
					loadOrderId(),
					loadFilterId()
					);
					bottomShii.dismiss();
				} else {
					SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.empty_err));
				}
			}
		});
		fav_btn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View _clickedView){
				if (_newItem) {
					toggleFavorite(fav_btn, true, new ArrayList<HashMap<String, Object>>(), 0);
				} else {
					toggleFavorite(fav_btn, false, _data, (int) _position);
				}    
				favorite = !favorite;
			}
		});
		if (_newItem) {
			folder_btn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View _clickedView){
					if (folder_btn.isSelected()) {
						folder = false;
						folder_btn.setSelected(false);
						ObjectAnimator anim1 = ObjectAnimator.ofFloat(code_txt, "alpha", 0f, 1f);
						ObjectAnimator anim2 = ObjectAnimator.ofFloat(code_edit, "alpha", 0f, 1f);
						ObjectAnimator anim3 = ObjectAnimator.ofFloat(type_txt, "alpha", 0f, 1f);
						ObjectAnimator anim4 = ObjectAnimator.ofFloat(dropdown_btn, "alpha", 0f, 1f);
						ObjectAnimator anim5 = ObjectAnimator.ofFloat(picture_gallery_txt, "alpha", 0f, 1f);
						ObjectAnimator anim6 = ObjectAnimator.ofFloat(pictures_rec, "alpha", 0f, 1f);
						ObjectAnimator anim7 = ObjectAnimator.ofFloat(color_theme_txt, "alpha", 0f, 1f);
						code_txt.setVisibility(View.VISIBLE);
						code_edit.setVisibility(View.VISIBLE);
						type_txt.setVisibility(View.VISIBLE);
						dropdown_btn.setVisibility(View.VISIBLE);
						picture_gallery_txt.setVisibility(View.VISIBLE);
						pictures_rec.setVisibility(View.VISIBLE);
						color_theme_txt.setVisibility(View.VISIBLE);
						AnimatorSet animSet = new AnimatorSet();
						animSet.playTogether(anim1, anim2, anim3, anim4);
						animSet.setDuration(250);
						animSet.setInterpolator(new LinearInterpolator());
						animSet.start();
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
						ObjectAnimator anim1 = ObjectAnimator.ofFloat(code_txt, "alpha", 1f, 0f);
						ObjectAnimator anim2 = ObjectAnimator.ofFloat(code_edit, "alpha", 1f, 0f);
						ObjectAnimator anim3 = ObjectAnimator.ofFloat(type_txt, "alpha", 1f, 0f);
						ObjectAnimator anim4 = ObjectAnimator.ofFloat(dropdown_btn, "alpha", 1f, 0f);
						ObjectAnimator anim5 = ObjectAnimator.ofFloat(picture_gallery_txt, "alpha", 1f, 0f);
						ObjectAnimator anim6 = ObjectAnimator.ofFloat(pictures_rec, "alpha", 1f, 0f);
						ObjectAnimator anim7 = ObjectAnimator.ofFloat(color_theme_txt, "alpha", 1f, 0f);
						AnimatorSet animSet = new AnimatorSet();
						animSet.playTogether(anim1, anim2, anim3, anim4);
						animSet.setDuration(250);
						animSet.setInterpolator(new LinearInterpolator());
						animSet.addListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								code_txt.setVisibility(View.GONE);
								code_edit.setVisibility(View.GONE);
								type_txt.setVisibility(View.GONE);
								dropdown_btn.setVisibility(View.GONE);
								picture_gallery_txt.setVisibility(View.GONE);
								pictures_rec.setVisibility(View.GONE);
								color_theme_txt.setVisibility(View.GONE);
							}
						});
						animSet.start();
						scan_btn.setClickable(false);
						scan_btn.setFocusable(false);
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
		} else {
			del_btn.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View _clickedView){
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
					}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFFF0000), 
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
					}.getIns((int)12, (int)0, Color.TRANSPARENT, 0xFFFF0000), 
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
							getPaint().setColor(0xFFFF0000);
						}});
					positive_txt.setTextColor(0xFFFFFFFF);
					negative_txt.setTextColor(0xFFFFFFFF);
					d.setCancelable(true);
					d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					message_txt.setText(getString(R.string.card_del_ask));
					positive_txt.setText(getString(R.string.yes));
					negative_txt.setText(getString(R.string.no));
					positive_txt.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View _clickedView){
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
							File dir = new File(getFilesDir(), "card_images/" + id);
							if (dir.exists()) deleteRecursive(dir);
							card_prefs.edit().putString("cards", new Gson().toJson(cards_list_all)).commit();
							SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.card_del_msg));
							applySortFilter(
							search_txt.getText().toString(),
							loadSortTypeId(),
							loadOrderId(),
							loadFilterId()
							);
							d.dismiss();
							bottomShii.dismiss();
						}
					});
					negative_txt.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View _clickedView){
							d.dismiss();
						}
					});
					d.show();
				}
			});
		}
		colors_rec.setLayoutManager(
		new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false)
		);
		final int spacingPx = (int) (4 * getResources().getDisplayMetrics().density);
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
		if (_newItem) {
			colors = new HashMap<>();
			colors.put("color", (int)(0xFF008DCD));
			colors_list.add(colors);
		} else {
			try {
				java.lang.reflect.Type t =
				new com.google.gson.reflect.TypeToken<ArrayList<Integer>>(){}.getType();
				ArrayList<Integer> picked =
				new Gson().fromJson(String.valueOf(_data.get((int) _position).get("colors")), t);
				
				if (picked != null) {
					for (Integer c : picked) {
						colors = new HashMap<>();
						colors.put("color", c);
						colors_list.add(colors);
					}
				}
			} catch (Exception e) {
				colors = new HashMap<>();
				colors.put("color", (int)(0xFF008DCD));
				colors_list.add(colors);
				SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.color_fail).concat("".concat(e.getMessage())));
			}
		}
		colors = new HashMap<>();
		colors.put("color", "plus");
		colors_list.add(colors);
		colors = new HashMap<>();
		colors.put("color", "settings");
		colors_list.add(colors);
		colorsAdapter = new Colors_recAdapter(colors_list);
		colors_rec.setAdapter(colorsAdapter);
		bottomShii.show();
		if ((boolean)settings.get("colors_tutorial") && (_newItem && !debug)) {
			tapTargetRoot = (ViewGroup) bottomShii.getWindow().getDecorView();
			colors_rec.post(new Runnable() {
				@Override
				public void run() {
					showColorsTutorialStep(bottomShii, colors_rec, 0);
				}
			});
		}
	}
	
	
	public void _loadLastId() {
		long new_id = card_prefs.getLong("lastId", -1) + 1;
		java.util.ArrayDeque<HashMap<String, Object>> stack = new java.util.ArrayDeque<>();
		for (HashMap<String, Object> m : cards_list_all) {
			stack.push(m);
		}
		while(!stack.isEmpty()) {
			HashMap<String, Object> map = stack.pop();
			String id_string = map.get("id").toString();
			long id_long = Long.valueOf(id_string);
			if (new_id < id_long) {
				new_id = id_long;
			}
			Boolean isFolder = (Boolean) map.get("folder");
			if (isFolder) {
				ArrayList<HashMap<String, Object>> folder_data = (ArrayList<HashMap<String, Object>>) map.get("data");
				for (HashMap<String, Object> child : folder_data) {
					stack.push(child);
				}    
			}
		}
		card_prefs.edit().putLong("lastId", new_id).commit();
	}
	
	
	public void _showEanWarning() {
		d = new AlertDialog.Builder(MainActivity.this).create();
		LayoutInflater dLI = getLayoutInflater();
		View dCV = (View) dLI.inflate(R.layout.dialog, null);
		d.setView(dCV);
		final LinearLayout dialog_parent = (LinearLayout)
		dCV.findViewById(R.id.parent);
		final LinearLayout buttons_bar = (LinearLayout)
		dCV.findViewById(R.id.buttons_bar);
		final TextView message_txt = (TextView)
		dCV.findViewById(R.id.message_txt);
		final TextView positive_txt = (TextView)
		dCV.findViewById(R.id.positive_txt);
		final TextView negative_txt = (TextView)
		dCV.findViewById(R.id.negative_txt);
		d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		d.setCancelable(false);
		dialog_parent.setClickable(true);
		final float dialog_parent_rTL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
		final float dialog_parent_rTR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
		final float dialog_parent_rBR = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
		final float dialog_parent_rBL = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, getResources().getDisplayMetrics());
		final int dialog_parent_strokePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 2, getResources().getDisplayMetrics());
		final GradientDrawable dialog_parent_bg = new GradientDrawable();
		dialog_parent_bg.setColor(0xFFFFFFFF);
		dialog_parent_bg.setCornerRadii(new float[]{dialog_parent_rTL,dialog_parent_rTL,dialog_parent_rTR,dialog_parent_rTR,dialog_parent_rBR,dialog_parent_rBR,dialog_parent_rBL,dialog_parent_rBL});
		dialog_parent_bg.setStroke(dialog_parent_strokePx, 0xFF212121);
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
		
		negative_txt.setVisibility(View.GONE);
		message_txt.setText(getString(R.string.invalid_checksum_desc));
		positive_txt.setText(getString(R.string.close));
		float scale = textScaleFromLevel((int) textLevel);
		applyTextScale(message_txt, scale);
		applyTextScale(positive_txt, scale);
		positive_txt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				d.dismiss();
			}
		});
		d.show();
	}
	
	
	public void _displayImage(final String _image) {
		d = new AlertDialog.Builder(MainActivity.this).create();
		LayoutInflater dLI = getLayoutInflater();
		View dCV = (View) dLI.inflate(R.layout.image_display_dialog, null);
		d.setView(dCV);
		final FrameLayout parent = (FrameLayout)
		dCV.findViewById(R.id.parent);
		final ImageView display_img = (ImageView)
		dCV.findViewById(R.id.display_img);
		final ImageView close_img = (ImageView)
		dCV.findViewById(R.id.close_img);
		d.setCancelable(true);
		d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		Window window = d.getWindow();
		if (window != null) {
			window.setLayout(
			ViewGroup.LayoutParams.WRAP_CONTENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
			);
			window.setGravity(Gravity.CENTER);
		}
		parent.setClickable(true);
		
		final float parent_rTL = TypedValue.applyDimension(
		TypedValue.COMPLEX_UNIT_DIP,
		(float) 12,
		getResources().getDisplayMetrics()
		);
		
		final float parent_rTR = TypedValue.applyDimension(
		TypedValue.COMPLEX_UNIT_DIP,
		(float) 12,
		getResources().getDisplayMetrics()
		);
		
		final float parent_rBR = TypedValue.applyDimension(
		TypedValue.COMPLEX_UNIT_DIP,
		(float) 12,
		getResources().getDisplayMetrics()
		);
		
		final float parent_rBL = TypedValue.applyDimension(
		TypedValue.COMPLEX_UNIT_DIP,
		(float) 12,
		getResources().getDisplayMetrics()
		);
		
		parent.setBackground(new ShapeDrawable(new RoundRectShape(
		new float[]{
			parent_rTL, parent_rTL,
			parent_rTR, parent_rTR,
			parent_rBR, parent_rBR,
			parent_rBL, parent_rBL
		},
		null,
		null
		)) {{
				getPaint().setColor(0xFF000000);
			}});
		int w = (int) (SketchwareUtil.getDisplayWidthPixels(getApplicationContext()) * 0.8);
		parent.setClipToOutline(true);
		File f = new File(getFilesDir(), "card_images/" + id + "/" + _image);
		
		if (f.exists() && f.length() > 0) {
			Picasso.with(getApplicationContext())
			.load(f)
			.into(display_img);
		} else {
			display_img.setImageResource(R.drawable.ic_broken_image);
			ViewGroup.LayoutParams display_img_layoutParams = display_img.getLayoutParams();
			display_img_layoutParams.width = w;
			display_img_layoutParams.height = w;
			display_img.setLayoutParams(display_img_layoutParams);
		}
		close_img.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				d.dismiss();
			}
		});
		d.show();
	}
	
	
	public boolean _isValidItem() {
		if (cardSaveName.isEmpty()) {
			return (false);
		}
		if (!folder && !debug) {
			if (!cardSaveCode.isEmpty() && cardSaveType.equals(getString(R.string.none))) {
				// Can't have a code without code type
				return (false);
			}
			if (cardSaveCode.isEmpty() && pendingImages.isEmpty()) {
				// Can't have no code and no images if not a folder
				return (false);
			}
		}
		return (true);
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
			final ImageView folder_outline = _view.findViewById(R.id.folder_outline);
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
			applyTextScale(card_name, textScaleFromLevel((int) textLevel));
			parent.post(new Runnable() {
				@Override
				public void run() {
					int w = parent.getWidth();
					card_name.setPadding((int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					(int) Math.round(w * 0.1),
					getResources().getDisplayMetrics()
					), (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					8,
					getResources().getDisplayMetrics()
					), (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					(int) Math.round(w * 0.1),
					getResources().getDisplayMetrics()
					), (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					8,
					getResources().getDisplayMetrics()
					));
					
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
						folder_outline.setVisibility(View.VISIBLE);
					} else {
						v_lp.setMargins(0, 0, 0, 0);
						folder_outline.setVisibility(View.INVISIBLE);
					}
					gradient.setLayoutParams(v_lp);
				}
			});
			parent.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					try {
						HashMap<String, Object> card = _data.get((int) _position);
						double cur = (double) card.get("used");
						cur++;
						card.put("used", cur);
						card_prefs.edit().putString("cards", new Gson().toJson(cards_list_all)).apply();
					} catch (Exception ignore) {}
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
						if (debug) {
							SketchwareUtil.showMessage(getApplicationContext(), "width: ".concat(String.valueOf(parent.getWidth()).concat("\n".concat("height: ".concat(String.valueOf(parent.getHeight()))))));
						} else {
							_displayInfo(_data, _position, false);
						}
					}
				}
			});
			parent.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View _view) {
					_displayInfo(_data, _position, false);
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