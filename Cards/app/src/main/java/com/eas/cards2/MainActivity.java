package com.eas.cards2;

import android.Manifest;
import android.animation.*;
import android.animation.ObjectAnimator;
import android.app.*;
import android.app.Activity;
import android.content.*;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.*;

import java.io.InputStream;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.common.BitMatrix;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.squareup.picasso.Picasso;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.slider.Slider;


public class MainActivity extends AppCompatActivity {
    // ==================== INTS ====================
    private int theme;
    private static final int REQ_CAMERA = 1000;
    private static final int REQ_TAKE_PHOTO = 2001;
    private static final int REQ_PICK_IMAGE = 2002;
    private static final int REQ_EXPORT_JSON = 6101;
    private static final int REQ_IMPORT_JSON = 6102;
    private static final int TAG_BASE_TEXT_PX = 0x7f0a0123;
    private static final int KEEP = Integer.MIN_VALUE;

	// ==================== BOOLEANS ====================
    private boolean favorite = false;
    private boolean folder = false;
    private boolean inFolder = false;
    private boolean debug = false;
    private boolean pickInProgress = false;
    private boolean scanImage = false;
    private boolean scan = false;
    private boolean newCardSaved = false;
    private boolean noCode = false;

    // ==================== STRINGS ====================
    private String cardSaveName = "";
    private String cardSaveType = "";
    private String cardSaveCode = "";
    private String selectedGradStyle = "";
    private String folderPath = "";
    private String backup = "";
    private String id = "";
    private String pendingCameraInternalPath = null;
    private static final String PREF_SORT_TYPE = "pref_sort_type";
    private static final String PREF_SORT_ORDER = "pref_sort_order";
    private static final String PREF_SORT_FILTER = "pref_sort_filter";
    private static final String KEY_ROW_TYPE = "_rowType";
    private static final String ROW_HEADER = "header";
    private static final String KEY_VIRTUAL = "_virtual";
    private static final String VIRTUAL_FAVORITES = "favorites";

    // ==================== MAPS AND LISTS ====================
    private HashMap<String, Object> cards = new HashMap<>();
    private HashMap<String, Object> colors = new HashMap<>();
    private HashMap<String, Object> settings = new HashMap<>();
    private HashMap<String, Object> types = new HashMap<>();
    private HashMap<String, Object> pictures = new HashMap<>();
    private ArrayList<String> pendingImages = new ArrayList<>();
    private ArrayList<String> pendingDelete = new ArrayList<>();
    private final ArrayList<String> folderIdStack = new ArrayList<>();
    private final ArrayList<String> folderNameStack = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> cards_list = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> cards_list_all = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> colors_list = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> types_list = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> pictures_list = new ArrayList<>();

    // ==================== OTHER VARIABLES ====================
    private double progress = 0;
    private double textLevel = 0;
    private long newId;
    private Intent i = new Intent();
    private SharedPreferences card_prefs;
    private PopupWindow p;
    private Uri pendingCameraUri = null;
    private OnImagePicked pendingPicked;
    private OnCancelled pendingCancelled;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // ==================== LINEARLAYOUTS ====================
    private LinearLayout fab;
    private LinearLayout code_menu_lay;
    private LinearLayout parent;
    private LinearLayout filter_parent;
    private LinearLayout no_items_lay;
    private LinearLayout search_bar;
    private LinearLayout filter_bar;
    private LinearLayout settings_bar;

    // ==================== TEXTS AND EDITTEXTS ====================
    private TextView type_txt;
    private TextView no_items_top_txt;
    private TextView no_items_bottom_txt;
	private EditText code_edit;
    private EditText search_txt;

    // ==================== IMAGES ====================
    private ImageView search_img;
    private ImageView filter_img;
    private ImageView settings_img;
    private ImageView wallet_img;

    // ==================== RECYCLERS ====================
    private RecyclerView cards_rec;
    private RecyclerView colors_rec;
    private RecyclerView items_rec;
    private RecyclerView pictures_rec;

    // ==================== OTHER VIEWS ====================
	private SwipeRefreshLayout srefresh;

    // ==================== ADAPTERS ====================
    private Cards_recAdapter cardsAdapter;
    private Colors_recAdapter colorsAdapter;
    private Items_recAdapter itemsAdapter;
    private Pictures_recAdapter picturesAdapter;

    // ==================== LIFECYCLE ====================
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
        final View root = findViewById(R.id._coordinator);
        final View parent = findViewById(R.id.parent);
        final View fab = findViewById(R.id.fab);

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
    @Override
    public void onResume() {
        super.onResume();
        if (card_prefs.contains("code") && card_prefs.contains("type")) {
            if (code_edit != null) {
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

    // ==================== INITIALIZATION ====================
	private void initialize(Bundle _savedInstanceState) {
		fab = findViewById(R.id.fab);
		parent = findViewById(R.id.parent);
		filter_parent = findViewById(R.id.filter_parent);
		srefresh = findViewById(R.id.srefresh);
		no_items_lay = findViewById(R.id.no_items_lay);
		search_bar = findViewById(R.id.search_bar);
		filter_bar = findViewById(R.id.filter_bar);
		settings_bar = findViewById(R.id.settings_bar);
		search_img = findViewById(R.id.search_img);
		search_txt = findViewById(R.id.search_txt);
		filter_img = findViewById(R.id.filter_img);
		settings_img = findViewById(R.id.settings_img);
		cards_rec = findViewById(R.id.cards_rec);
		wallet_img = findViewById(R.id.wallet_img);
		no_items_top_txt = findViewById(R.id.no_items_top_txt);
		no_items_bottom_txt = findViewById(R.id.no_items_bottom_txt);
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
				showBottomSheet(R.layout.filters_dialog, 0, (bs, root) -> {
					final View top_bar = root.findViewById(R.id.top_bar);
					final TextView filters_txt = root.findViewById(R.id.filters_txt);
					final TextView sort_by_txt = root.findViewById(R.id.sort_by_txt);
                    final TextView order_txt = root.findViewById(R.id.order_txt);
                    final TextView show_txt = root.findViewById(R.id.show_txt);
                    final TextView by_name = root.findViewById(R.id.by_name);
                    final TextView by_date_created = root.findViewById(R.id.by_date_created);
                    final TextView by_use_count = root.findViewById(R.id.by_use_count);
                    final TextView ascending_txt = root.findViewById(R.id.ascending_txt);
                    final TextView descending_txt = root.findViewById(R.id.descending_txt);
                    final TextView all_txt = root.findViewById(R.id.all_txt);
                    final TextView favorites_txt = root.findViewById(R.id.favorites_txt);
                    final TextView apply_filters = root.findViewById(R.id.apply_filters);
                    final TextView reset_to_default = root.findViewById(R.id.reset_to_default);
					final ImageView close_img = root.findViewById(R.id.close_img);
					final ImageView sort_img = root.findViewById(R.id.sort_img);
                    final ImageView order_img = root.findViewById(R.id.order_img);
                    final ImageView show_img = root.findViewById(R.id.show_img);
                    final ImageView ascending_img = root.findViewById(R.id.ascending_img);
                    final ImageView descending_img = root.findViewById(R.id.descending_img);
                    final ImageView all_img = root.findViewById(R.id.all_img);
                    final ImageView favorites_img = root.findViewById(R.id.favorites_img);
                    final LinearLayout orders = root.findViewById(R.id.orders);
                    final LinearLayout ascending = root.findViewById(R.id.ascending);
                    final LinearLayout descending = root.findViewById(R.id.descending);
                    final LinearLayout all = root.findViewById(R.id.all);
                    final LinearLayout favorites = root.findViewById(R.id.favorites);

					float scale = textScaleFromLevel((int) textLevel);
					View[] views = new View[] {
							filters_txt,
							sort_by_txt,
                            order_txt,
                            show_txt,
							by_name,
							by_date_created,
							by_use_count,
                            ascending_txt,
                            descending_txt,
                            all_txt,
                            favorites_txt,
                            apply_filters,
                            reset_to_default
					};
					for (View v : views) {
						applyTextScale((TextView) v, scale);
					}

                    List<View[]> pairs = new ArrayList<>();
                    pairs.add(new View[]{close_img, filters_txt});
                    pairs.add(new View[]{sort_img, sort_by_txt});
                    pairs.add(new View[]{order_img, order_txt});
                    pairs.add(new View[]{ascending_img, ascending_txt});
                    pairs.add(new View[]{descending_img, descending_txt});
                    pairs.add(new View[]{show_img, show_txt});
                    pairs.add(new View[]{all_img, all_txt});
                    pairs.add(new View[]{favorites_img, favorites_txt});
                    for (View[] pair : pairs) {
                        matchImageSize(pair, 1.2f);
                    }

                    Bg.apply(orders, color(R.color.app_surface_var), null, null, 16, null, 1, color(R.color.app_stroke), null);
                    makeTopBar(top_bar);
                    makeButton(apply_filters, 1);

                    TextView[] sort_buttons = new TextView[] {by_name, by_date_created, by_use_count};
                    refreshToggleGroup(sort_buttons, loadSortTypeId());
                    for (TextView t : sort_buttons) {
                        t.setOnClickListener(v -> {int selectedId = t.getId(); saveSortPrefs(selectedId, loadOrderId(), loadFilterId()); refreshToggleGroup(sort_buttons, selectedId);});
                    }

                    List<View[]> order_buttons = new ArrayList<>();
                    order_buttons.add(new View[]{ascending, ascending_img, ascending_txt});
                    order_buttons.add(new View[]{descending, descending_img, descending_txt});
                    refreshSwitchGroup(order_buttons, loadOrderId(), 0);
                    for (LinearLayout l : new LinearLayout[]{ascending, descending}) {
                        l.setOnClickListener(v -> {int selectedId = l.getId(); saveSortPrefs(loadSortTypeId(), selectedId, loadFilterId()); refreshSwitchGroup(order_buttons, selectedId, 0);});
                    }

                    List<View[]> show_buttons = new ArrayList<>();
                    show_buttons.add(new View[]{all, all_img, all_txt});
                    show_buttons.add(new View[]{favorites, favorites_img, favorites_txt});
                    refreshSwitchGroup(show_buttons, loadFilterId(), 1);
                    for (LinearLayout l : new LinearLayout[]{all, favorites}) {
                        l.setOnClickListener(v -> {int selectedId = l.getId(); saveSortPrefs(loadSortTypeId(), loadOrderId(), selectedId); refreshSwitchGroup(show_buttons, selectedId, 1);});
                    }

                    close_img.setOnClickListener(v -> {bs.dismiss();});
                    apply_filters.setOnClickListener(v -> {bs.dismiss();});
                    reset_to_default.setOnClickListener(v -> {
                        saveSortPrefs(R.id.by_name, R.id.ascending, R.id.all);
                        refreshToggleGroup(sort_buttons, R.id.by_name);
                        refreshSwitchGroup(order_buttons, R.id.ascending, 0);
                        refreshSwitchGroup(show_buttons, R.id.all, 1);
                    });
				});
			}
		});
		
		settings_bar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
                showBottomSheet(R.layout.settings_dialog, 0, (bs, root) -> {
                    final View top_bar = root.findViewById(R.id.top_bar);
                    final TextView settings_txt = root.findViewById(R.id.settings_txt);
                    final TextView visual_settings_txt = root.findViewById(R.id.visual_settings_txt);
                    final TextView ux_txt = root.findViewById(R.id.ux_txt);
                    final TextView data_txt = root.findViewById(R.id.data_txt);
                    final TextView column_count_txt = root.findViewById(R.id.column_count_txt);
                    final TextView text_size_txt = root.findViewById(R.id.text_size_txt);
                    final TextView column_count = root.findViewById(R.id.column_count);
                    final TextView text_size = root.findViewById(R.id.text_size);
                    final TextView theme_txt = root.findViewById(R.id.theme_txt);
                    final TextView scan_mode_txt = root.findViewById(R.id.scan_mode_txt);
                    final TextView light = root.findViewById(R.id.light);
                    final TextView dark = root.findViewById(R.id.dark);
                    final TextView system_default = root.findViewById(R.id.system_default);
                    final TextView camera_txt = root.findViewById(R.id.camera_txt);
                    final TextView image_txt = root.findViewById(R.id.image_txt);
                    final TextView import_txt = root.findViewById(R.id.import_txt);
                    final TextView export_txt = root.findViewById(R.id.export_txt);
                    final TextView save = root.findViewById(R.id.save);
                    final TextView reset_to_default = root.findViewById(R.id.reset_to_default);
                    final ImageView close_img = root.findViewById(R.id.close_img);
                    final ImageView visual_img = root.findViewById(R.id.visual_img);
                    final ImageView ux_img = root.findViewById(R.id.ux_img);
                    final ImageView data_img = root.findViewById(R.id.data_img);
                    final ImageView camera_img = root.findViewById(R.id.camera_img);
                    final ImageView image_img = root.findViewById(R.id.image_img);
                    final ImageView import_img = root.findViewById(R.id.import_img);
                    final ImageView export_img = root.findViewById(R.id.export_img);
                    final Slider column_count_sbar = root.findViewById(R.id.column_count_sbar);
                    final Slider text_size_sbar = root.findViewById(R.id.text_size_sbar);
                    final LinearLayout scan_modes = root.findViewById(R.id.scan_modes);
                    final LinearLayout camera = root.findViewById(R.id.camera);
                    final LinearLayout image = root.findViewById(R.id.image);
                    final LinearLayout import_btn = root.findViewById(R.id.import_btn);
                    final LinearLayout export_btn = root.findViewById(R.id.export_btn);

                    try {
                        progress = (double) settings.get("grid_amount");
                        textLevel = (double) settings.get("text_level");
                        scanImage = (boolean) settings.get("scan_image");
                    } catch(Exception e) {
                        progress = 2;
                        textLevel = 3;
                        scanImage = false;
                    }
                    theme = loadTheme();

                    float scale = textScaleFromLevel((int) textLevel);
                    View[] views = new View[]{
                            settings_txt,
                            visual_settings_txt,
                            ux_txt,
                            data_txt,
                            column_count_txt,
                            text_size_txt,
                            column_count,
                            text_size,
                            theme_txt,
                            scan_mode_txt,
                            light,
                            dark,
                            system_default,
                            camera_txt,
                            image_txt,
                            import_txt,
                            export_txt,
                            save,
                            reset_to_default
                    };
                    for (View v : views) {
                        applyTextScale((TextView) v, scale);
                    }

                    List<View[]> pairs = new ArrayList<>();
                    pairs.add(new View[]{close_img, settings_txt});
                    pairs.add(new View[]{visual_img, visual_settings_txt});
                    pairs.add(new View[]{ux_img, ux_txt});
                    pairs.add(new View[]{data_img, data_txt});
                    pairs.add(new View[]{camera_img, camera_txt});
                    pairs.add(new View[]{image_img, image_txt});
                    pairs.add(new View[]{import_img, import_txt});
                    pairs.add(new View[]{export_img, export_txt});
                    for (View[] pair : pairs) {
                        matchImageSize(pair, 1.2f);
                    }

                    TextView[] theme_buttons = new TextView[] {light, dark, system_default};
                    refreshToggleGroup(theme_buttons, loadTheme());
                    for (TextView t : theme_buttons) {
                        t.setOnClickListener(v -> {theme = t.getId(); refreshToggleGroup(theme_buttons, theme);});
                    }

                    List<View[]> mode_buttons = new ArrayList<>();
                    mode_buttons.add(new View[]{camera, camera_img, camera_txt});
                    mode_buttons.add(new View[]{image, image_img, image_txt});
                    refreshSwitchGroup(mode_buttons, scanImage ? R.id.image : R.id.camera, 0);

                    Bg.apply(scan_modes, color(R.color.app_surface_var), null, null, 16, null, 1, color(R.color.app_stroke), null);
                    makeTopBar(top_bar);
                    makeButton(import_btn, 0);
                    makeButton(export_btn, 0);
                    makeButton(save, 1);

                    column_count.setText(String.valueOf((int) progress));
                    text_size.setText(String.valueOf((int) textLevel));
                    column_count_sbar.setValue((float) progress);
                    text_size_sbar.setValue((float) textLevel);

                    column_count_sbar.addOnChangeListener((s, value, fromUser) -> {
                        progress = value;
                        column_count.setText(String.valueOf((int) value));
                    });
                    text_size_sbar.addOnChangeListener((s, value, fromUser) -> {
                        float text_scale = textScaleFromLevel((int) value);
                        textLevel = value;
                        text_size.setText(String.valueOf((int) value));

                        for (View v : views) {
                            applyTextScale((TextView) v, text_scale);
                        }
                        for (View[] pair : pairs) {
                            matchImageSize(pair, 1.2f);
                        }
                    });
                    camera.setOnClickListener(v -> {scanImage = false; refreshSwitchGroup(mode_buttons, R.id.camera, 0);});
                    image.setOnClickListener(v -> {scanImage = true; refreshSwitchGroup(mode_buttons, R.id.image, 0);});
                    save.setOnClickListener(v -> {
                        applyTextScale(search_txt, textScaleFromLevel((int) textLevel));
                        applyTheme(theme);
                        saveTheme(theme);
                        ViewGroup.LayoutParams sbarLP = search_bar.getLayoutParams();
                        sbarLP.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        search_bar.setLayoutParams(sbarLP);
                        search_txt.post(() -> {
                            float density = getResources().getDisplayMetrics().density;
                            int searchH = search_txt.getHeight();
                            int sbarH = (int) Math.round(searchH + (4 * density));
                            int simgH = (int) Math.round(searchH - (10 * density));
                            int fsbarS = (int) Math.round(searchH * 0.7f);
                            sbarLP.height = sbarH;
                            search_bar.setLayoutParams(sbarLP);
                            setSize(filter_bar, fsbarS, fsbarS);
                            setSize(settings_bar, fsbarS, fsbarS);
                            setSize(search_img, simgH, simgH);
                        });
                        applySortFilter(
                                search_txt.getText().toString(),
                                loadSortTypeId(),
                                loadOrderId(),
                                loadFilterId()
                        );
                        settings.put("grid_amount", progress);
                        settings.put("text_level", textLevel);
                        settings.put("scan_image", scanImage);
                        saveSettings();
                        final GridLayoutManager cards_rec_layoutManager = new GridLayoutManager(cards_rec.getContext(), ((Double) progress).intValue(), RecyclerView.VERTICAL, false);

                        cards_rec_layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                            @Override
                            public int getSpanSize(int position) {
                                RecyclerView.Adapter a = cards_rec.getAdapter();
                                if (a == null) return cards_rec_layoutManager.getSpanCount();
                                int t = a.getItemViewType(position);
                                return (t == Cards_recAdapter.TYPE_HEADER) ? cards_rec_layoutManager.getSpanCount() : 1;
                            }
                        });
                        cards_rec.setLayoutManager(cards_rec_layoutManager);
                        bs.dismiss();
                    });
                    reset_to_default.setOnClickListener(v -> {
                        column_count_sbar.setValue(2);
                        text_size_sbar.setValue(3);
                        theme = R.id.system_default;
                        scanImage = false;
                        refreshToggleGroup(theme_buttons, R.id.system_default);
                        refreshSwitchGroup(mode_buttons, R.id.camera, 0);
                    });
                    import_btn.setOnClickListener(v -> {
                        showPopup(import_btn, PopupPos.TOP_RIGHT, 0, 0, R.string.from_text, R.string.from_file, R.drawable.ic_text, R.drawable.ic_file, () -> {
                                showDialog(R.layout.import_dialog, R.id.parent, (dlg, droot) -> {
                                    final TextView message_txt = droot.findViewById(R.id.message_txt);
                                    final TextView positive_txt = droot.findViewById(R.id.positive_txt);
                                    final TextView negative_txt = droot.findViewById(R.id.negative_txt);
                                    final EditText import_etxt = droot.findViewById(R.id.import_txt);
                                    final LinearLayout buttons_bar = droot.findViewById(R.id.buttons_bar);
                                    float tscale = textScaleFromLevel((int) textLevel);
                                    applyTextScale(import_etxt, tscale);
                                    TextView[] tviews = new TextView[] { message_txt, positive_txt, negative_txt };
                                    for (TextView tv : tviews) {
                                        if (tv != null) applyTextScale(tv, tscale);
                                    }
                                    Bg.apply(positive_txt, 0xFFD2B6DC, null, null, 12, null, 0, Color.TRANSPARENT, 0xFFF2EAF5);
                                    Bg.apply(negative_txt, 0xFFD2B6DC, null, null, 12, null, 0, Color.TRANSPARENT, 0xFFF2EAF5);
                                    Bg.apply(buttons_bar, 0xFFD2B6DC, null, null, 0, new float[]{0, 0, 12, 12}, 0, Color.TRANSPARENT, Color.TRANSPARENT);
                                    message_txt.setText(getString(R.string.import_warning));
                                    positive_txt.setText(getString(R.string.import_positive));
                                    negative_txt.setText(getString(R.string.cancel));
                                    positive_txt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View _view) {
                                            backup = new Gson().toJson(cards_list_all);
                                            try {
                                                saveCards(import_etxt.getText().toString());
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
                                            } catch(Exception e){
                                                saveCards(backup);
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
                                            loadLastId();
                                            applySortFilter(
                                                    search_txt.getText().toString(),
                                                    loadSortTypeId(),
                                                    loadOrderId(),
                                                    loadFilterId()
                                            );
                                            dlg.dismiss();
                                        }
                                    });
                                    negative_txt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View _view) {
                                            dlg.dismiss();
                                        }
                                    });
                                });
                            }, () -> {
                                i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                                i.addCategory(Intent.CATEGORY_OPENABLE);
                                i.setType("application/json");
                                i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/*", "*/*"});
                                startActivityForResult(i, REQ_IMPORT_JSON);
                            }, null
                        );
                    });
                    export_btn.setOnClickListener(v -> {
                        showPopup(export_btn, PopupPos.TOP_RIGHT, 0, 0, R.string.copy_as_text, R.string.save_as_file, R.drawable.ic_copy, R.drawable.ic_to_file, () -> {
                                    ((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", new Gson().toJson(cards_list_all)));
                                    SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.text_copied));
                                }, () -> {
                                    String fileName = "cards_backup_" + System.currentTimeMillis() + ".json";
                                    i.setAction(Intent.ACTION_CREATE_DOCUMENT);
                                    i.addCategory(Intent.CATEGORY_OPENABLE);
                                    i.setType("application/json");
                                    i.putExtra(Intent.EXTRA_TITLE, fileName);
                                    startActivityForResult(i, REQ_EXPORT_JSON);
                                }, null
                        );
                    });
                    close_img.setOnClickListener(v -> {bs.dismiss();});
                }, dialog -> {
                    boolean mode = (boolean) settings.get("scan_image");
                    if (!scanImage == mode) {
                        scanImage = mode;
                    }
                });
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
		
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				displayInfo(new HashMap<String, Object>(), true);
			}
		});
	}
    private void initializeLogic() {
        debug = false;
        applyTheme(loadTheme());
        if (card_prefs.contains("settings")) {
            settings = new Gson().fromJson(card_prefs.getString("settings", ""), new TypeToken<HashMap<String, Object>>(){}.getType());
            final GridLayoutManager cards_rec_layoutManager =
                    new GridLayoutManager(cards_rec.getContext(), ((Double) settings.get("grid_amount")).intValue(), RecyclerView.VERTICAL, false);

            cards_rec_layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    RecyclerView.Adapter a = cards_rec.getAdapter();
                    if (a == null) return cards_rec_layoutManager.getSpanCount();
                    int t = a.getItemViewType(position);
                    return (t == Cards_recAdapter.TYPE_HEADER) ? cards_rec_layoutManager.getSpanCount() : 1;
                }
            });

            cards_rec.setLayoutManager(cards_rec_layoutManager);
        } else {
            settings = new HashMap<>();
            settings.put("grid_amount", (double)(2));
            settings.put("text_level", (double)(3));
            settings.put("scan_image", false);
            settings.put("main_tutorial", true);
            settings.put("colors_tutorial", true);
            settings.put("settings_tutorial", true);
            saveSettings();
            final GridLayoutManager cards_rec_layoutManager =
                    new GridLayoutManager(cards_rec.getContext(), 2, RecyclerView.VERTICAL, false);

            cards_rec_layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    RecyclerView.Adapter a = cards_rec.getAdapter();
                    if (a == null) return cards_rec_layoutManager.getSpanCount();
                    int t = a.getItemViewType(position);
                    return (t == Cards_recAdapter.TYPE_HEADER) ? cards_rec_layoutManager.getSpanCount() : 1;
                }
            });

            cards_rec.setLayoutManager(cards_rec_layoutManager);
        }
        try{
            textLevel = (double)settings.get("text_level");
            scanImage = (boolean)settings.get("scan_image");
        }catch(Exception e){
            textLevel = 3;
            scanImage = false;
        }
        applyTextScale(search_txt, textScaleFromLevel((int) textLevel));
        Bg.apply(search_bar, getResources().getColor(R.color.app_surface_var), null, null, 16, null, 0, Color.TRANSPARENT, Color.TRANSPARENT);
        search_txt.post(() -> {
            float density = getResources().getDisplayMetrics().density;
            int searchH = search_txt.getHeight();
            int sbarH = (int) Math.round(searchH + (4 * density));
            int simgH = (int) Math.round(searchH - (10 * density));
            int fsbarS = (int) Math.round(searchH * 0.7f);
            setSize(search_bar, KEEP, sbarH);
            setSize(filter_bar, fsbarS, fsbarS);
            setSize(settings_bar, fsbarS, fsbarS);
            setSize(search_img, simgH, simgH);
        });
        refreshList();
        applySortFilter(
                search_txt.getText().toString(),
                loadSortTypeId(),
                loadOrderId(),
                loadFilterId()
        );
        initImagePicker();
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

    // ==================== THEME ====================
    private void applyTheme(int theme) {
        if (theme == R.id.light) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme == R.id.dark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme == R.id.system_default) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
    private void saveTheme(int theme) {
        card_prefs.edit().putInt("theme", theme).apply();
    }
    private int loadTheme() {
        return card_prefs.getInt("theme", R.id.system_default);
    }

    // ==================== SORT / FILTER ====================
    private void applySortFilter(String searchQuery, int sortTypeCheckedId, int orderCheckedId, int filterCheckedId) {

        String q = (searchQuery == null) ? "" : searchQuery.toLowerCase().trim();

        boolean onlyFav = (filterCheckedId == R.id.favorites);
        boolean ascending = (orderCheckedId == R.id.ascending);

        ArrayList<HashMap<String, Object>> source;

        if (inFolder && !folderIdStack.isEmpty()) {
            source = resolveContainerList(cards_list_all, folderIdStack);
        } else {
            source = cards_list_all;
        }

        boolean atRoot = !(inFolder && !folderIdStack.isEmpty());

        ArrayList<HashMap<String, Object>> filtered = new ArrayList<>();

        if (atRoot) {
            injectVirtualFavoritesIntoFiltered(filtered);
        }

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

            filtered.add(card);
        }

        java.util.Collections.sort(filtered, new java.util.Comparator<HashMap<String, Object>>() {
            @Override
            public int compare(HashMap<String, Object> a, HashMap<String, Object> b) {

                boolean av = isVirtualFavorites(a);
                boolean bv = isVirtualFavorites(b);

                if (av != bv) {
                    return av ? -1 : 1;
                }

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

        ArrayList<HashMap<String, Object>> sectioned = buildSectionedList(filtered);

        cards_list.clear();
        cards_list.addAll(sectioned);

        cardsAdapter.notifyDataSetChanged();
        srefresh.setRefreshing(false);
        float scale = textScaleFromLevel((int) textLevel);
        applyTextScale(no_items_top_txt, scale);
        applyTextScale(no_items_bottom_txt, scale);

        float dens = getResources().getDisplayMetrics().density;
        parent.post(() -> {
            int w = parent.getWidth();
            CoordinatorLayout.LayoutParams flp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();

            if (cards_list_all.isEmpty()) {
                srefresh.setVisibility(View.GONE);
                no_items_lay.setVisibility(View.VISIBLE);

                flp.bottomMargin = Math.round(125 * dens);
                int fab_size = (int) Math.round(w * 0.25);
                setSize(fab, fab_size, fab_size);
            } else {
                srefresh.setVisibility(View.VISIBLE);
                no_items_lay.setVisibility(View.GONE);

                flp.bottomMargin = Math.round(16 * dens);
                flp.rightMargin = Math.round(16 * dens);
                int fab_size = (int) Math.round(w * 0.18);
                setSize(fab, fab_size, fab_size);
            }
            fab.setLayoutParams(flp);
        });
    }
    private void saveSortPrefs(int sortTypeId, int orderId, int filterId) {
        card_prefs.edit()
                .putInt(PREF_SORT_TYPE, sortTypeId)
                .putInt(PREF_SORT_ORDER, orderId)
                .putInt(PREF_SORT_FILTER, filterId)
                .apply();
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

    // ==================== CARD DATA ====================
    public void saveCards(final String _value) {
        if (_value == null) {
            card_prefs.edit().putString("cards", new Gson().toJson(cards_list_all)).commit();
        } else {
            card_prefs.edit().putString("cards", _value).commit();
        }
    }
    public void saveSettings() {
        card_prefs.edit().putString("settings", new Gson().toJson(settings)).commit();
    }
    public void loadLastId() {
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
    public void refreshList() {
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
    public boolean isValidItem() {
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
        return true;
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
    private String cardsCountText(int count) {
        return getResources().getQuantityString(R.plurals.cards_count, count, count);
    }
    private ArrayList<HashMap<String, Object>> buildSectionedList(ArrayList<HashMap<String, Object>> sortedItems) {
        ArrayList<HashMap<String, Object>> folders = new ArrayList<>();
        ArrayList<HashMap<String, Object>> cards = new ArrayList<>();

        for (int i = 0; i < sortedItems.size(); i++) {
            HashMap<String, Object> m = sortedItems.get(i);
            if (getBool(m, "folder", false)) folders.add(m);
            else cards.add(m);
        }

        HashMap<String, Object> fav = null;
        for (int i = 0; i < folders.size(); i++) {
            if (isVirtualFavorites(folders.get(i))) {
                fav = folders.remove(i);
                break;
            }
        }
        if (fav != null) folders.add(fav);

        ArrayList<HashMap<String, Object>> out = new ArrayList<>();

        if (!folders.isEmpty()) {
            out.add(makeHeaderRow(getString(R.string.folders)));
            out.addAll(folders);
        }

        if (!cards.isEmpty()) {
            out.add(makeHeaderRow(getString(R.string.cards)));
            out.addAll(cards);
        }

        return out;
    }

    // ==================== CARD CRUD ====================
    public void displayInfo(final HashMap<String, Object> _data, final boolean _newItem) {
        pendingImages.clear();
        pictures_list.clear();
        noCode = false;
        boolean favFolder = isVirtualFavorites(_data);
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
            cardSaveName = _data.get("name").toString();
            selectedGradStyle = _data.get("grad_style").toString();
            id = _data.get("id").toString();
            if (!favFolder) {
                favorite = (boolean)_data.get("favorite");
            }
            folder = (boolean)_data.get("folder");
            if (!folder && (_data.containsKey("type") && _data.containsKey("code"))) {
                cardSaveType = _data.get("type").toString();
                cardSaveCode = _data.get("code").toString();
            } else {
                noCode = true;
            }
            if (_data.containsKey("images")) {
                pendingImages.addAll((ArrayList<String>) _data.get("images"));
                for (String fn : pendingImages) {
                    if (fn == null) continue;
                    HashMap<String, Object> m = new HashMap<>();
                    m.put("image", fn);
                    pictures_list.add(m);
                }
            }
        }

        showBottomSheet(R.layout.display_dialog, 0, (bs, root) -> {
            final TextView card_name = root.findViewById(R.id.card_name);
            final TextView card_name_txt = root.findViewById(R.id.card_name_txt);
            final ImageView folder_btn = root.findViewById(R.id.folder_btn);
            final ImageView fav_btn = root.findViewById(R.id.fav_btn);
            final LinearLayout card_name_lay = root.findViewById(R.id.card_name_lay);

            float scale = textScaleFromLevel((int) textLevel);
            View[] views = new View[]{
                    card_name,
                    card_name_txt
            };
            for (View v : views) {
                applyTextScale((TextView) v, scale);
            }

            List<View[]> pairs = new ArrayList<>();
            pairs.add(new View[]{folder_btn, card_name_lay});
            pairs.add(new View[]{fav_btn, card_name_lay});
            for (View[] pair : pairs) {
                matchImageSize(pair, 0.75f);
            }
        });
    }
    private void toggleFavorite(View heartView, boolean isNew, HashMap<String, Object> _data) {
        boolean currentlyFavorite = false;
        if (isNew) {
            currentlyFavorite = heartView.isSelected();
        } else {
            currentlyFavorite = (boolean) _data.get("favorite");
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
            _data.put("favorite", newFavorite);
            if (inFolder) {
                ArrayList<HashMap<String, Object>> masterContainer = resolveContainerList(cards_list_all, folderIdStack);
                int index = indexOfCardById(masterContainer, id);
                masterContainer.get(index).put("favorite", newFavorite);
            } else {
                int index = indexOfCardById(cards_list_all, id);
                cards_list_all.get(index).put("favorite", newFavorite);
            }
            card_prefs.edit().putString("cards", new Gson().toJson(cards_list_all)).commit();
            applySortFilter(search_txt.getText().toString(),loadSortTypeId(),loadOrderId(), loadFilterId());
        }
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
    public static int indexOfCardById(ArrayList<HashMap<String, Object>> cardsList, String targetId) {
        if (cardsList == null || targetId == null) return -1;

        for (int i = 0; i < cardsList.size(); i++) {
            Object id = cardsList.get(i).get("id");
            if (id instanceof String && targetId.equals(id)) {
                return i;
            }
        }
        return -1;
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

    // ==================== FOLDER HELPERS ====================
    private void injectVirtualFavoritesIntoFiltered(ArrayList<HashMap<String, Object>> filteredRootOnly) {
        ArrayList<HashMap<String, Object>> favData = collectFavoriteItemsRecursive(cards_list_all);
        if (favData.isEmpty()) return;

        HashMap<String, Object> favFolder = buildVirtualFavoritesFolder(favData);

        for (int i = 0; i < filteredRootOnly.size(); i++) {
            if (isVirtualFavorites(filteredRootOnly.get(i))) return;
        }

        filteredRootOnly.add(favFolder);
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
    private boolean isVirtualFavorites(HashMap<String, Object> m) {
        Object v = m.get(KEY_VIRTUAL);
        return v != null && VIRTUAL_FAVORITES.equals(String.valueOf(v));
    }
    private String joinWithSlash(ArrayList<String> parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) sb.append("/");
            sb.append(parts.get(i));
        }
        return sb.toString();
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
    private HashMap<String, Object> buildVirtualFavoritesFolder(ArrayList<HashMap<String, Object>> favoritesData) {
        HashMap<String, Object> fav = new HashMap<>();

        fav.put(KEY_VIRTUAL, VIRTUAL_FAVORITES);
        fav.put("folder", true);
        fav.put("name", getString(R.string.favorites));
        fav.put("data", favoritesData);

        fav.put("used", 0d);

        fav.put("grad_style", "tl_br");

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(0xFF2B2B2E);
        colors.add(0xFF1C1C1E);
        fav.put("colors", new Gson().toJson(colors));

        fav.put("id", -999999999L);

        return fav;
    }
    private ArrayList<HashMap<String, Object>> resolveContainerList(ArrayList<HashMap<String, Object>> root, ArrayList<String> folderIdStack) {

        if (folderIdStack != null && !folderIdStack.isEmpty()) {
            String top = String.valueOf(folderIdStack.get(folderIdStack.size() - 1));
            if (top.equals("__favorites__")) {
                return collectFavoriteItemsRecursive(cards_list_all);
            }
        }

        ArrayList<HashMap<String, Object>> curList = root;

        for (int i = 0; i < folderIdStack.size(); i++) {
            String targetFolderId = folderIdStack.get(i);

            HashMap<String, Object> folderMap = findFolderById(curList, targetFolderId);
            if (folderMap == null) {
                return root;
            }

            Object dataObj = folderMap.get("data");
            ArrayList<HashMap<String, Object>> child = normalizeListOfMaps(dataObj);

            folderMap.put("data", child);

            curList = child;
        }

        return curList;
    }
    private ArrayList<HashMap<String, Object>> collectFavoriteItemsRecursive(ArrayList<HashMap<String, Object>> container) {
        ArrayList<HashMap<String, Object>> out = new ArrayList<>();
        if (container == null) return out;

        for (int i = 0; i < container.size(); i++) {
            HashMap<String, Object> item = container.get(i);

            if (getBool(item, "folder", false)) {
                Object dataObj = item.get("data");
                if (dataObj instanceof ArrayList) {
                    try {
                        @SuppressWarnings("unchecked")
                        ArrayList<HashMap<String, Object>> child = (ArrayList<HashMap<String, Object>>) dataObj;
                        out.addAll(collectFavoriteItemsRecursive(child));
                    } catch (Exception ignore) {}
                }

                if (getBool(item, "favorite", false)) {
                    out.add(item);
                }
            } else {
                if (getBool(item, "favorite", false)) {
                    out.add(item);
                }
            }
        }

        return out;
    }

    // ==================== CAMERA / IMAGE ====================
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
    private void pickImageForCard() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQ_PICK_IMAGE);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                if (scan) {
                    i.setClass(getApplicationContext(), ScannerActivity.class);
                    startActivity(i);
                } else {
                    openSystemCameraForCard(id);
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
            }
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
    private android.graphics.Bitmap rotateBitmap(android.graphics.Bitmap src, int degrees) {
        android.graphics.Matrix m = new android.graphics.Matrix();
        m.postRotate(degrees);
        return android.graphics.Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
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
    public interface OnImagePicked {
        void onPicked(Uri uri);
    }
    public interface OnCancelled {
        void onCancelled();
    }

    // ==================== IMPORT / EXPORT ====================
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

            saveCards(importedJson);

            ArrayList<HashMap<String, Object>> tmp = new Gson().fromJson(importedJson, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
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

            loadLastId();
            applySortFilter(
                    search_txt.getText().toString(),
                    loadSortTypeId(),
                    loadOrderId(),
                    loadFilterId()
            );

            SketchwareUtil.showMessage(getApplicationContext(), getString(R.string.import_success));
        } catch (Exception e) {
            try {
                saveCards(prev);
            } catch (Exception ignored) {}

            try {
                ArrayList<HashMap<String, Object>> tmp = new Gson().fromJson(prev, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
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

                loadLastId();
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

    // ==================== UI HELPERS ====================
    public void showEanWarning() {
        showDialog(R.layout.dialog, R.id.parent, (dlg, root) -> {
            final LinearLayout parent = (LinearLayout) root.findViewById(R.id.parent);
            final LinearLayout buttons_bar = (LinearLayout) root.findViewById(R.id.buttons_bar);
            final TextView message_txt = (TextView) root.findViewById(R.id.message_txt);
            final TextView positive_txt = (TextView) root.findViewById(R.id.positive_txt);
            final TextView negative_txt = (TextView) root.findViewById(R.id.negative_txt);
            dlg.setCancelable(false);
            dlg.setCanceledOnTouchOutside(false);
            Bg.apply(buttons_bar, 0xFFD2B6DC, null, null, 0, new float[]{0, 0, 12, 12}, 0, Color.TRANSPARENT, Color.TRANSPARENT);
            Bg.apply(positive_txt, 0xFFD2B6DC, null, null, 12, null, 0, Color.TRANSPARENT, 0xFFF2EAF5);
            negative_txt.setVisibility(View.GONE);
            message_txt.setText(getString(R.string.invalid_checksum_desc));
            positive_txt.setText(getString(R.string.close));
            float scale = textScaleFromLevel((int) textLevel);
            applyTextScale(message_txt, scale);
            applyTextScale(positive_txt, scale);
            positive_txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View _view) {
                    dlg.dismiss();
                }
            });
        });
    }
    public void displayImage(final String _image) {
        showDialog(R.layout.image_display_dialog, R.id.parent, (dlg, root) -> {
            final FrameLayout parent = (FrameLayout) root.findViewById(R.id.parent);
            final ImageView display_img = (ImageView) root.findViewById(R.id.display_img);
            final ImageView close_img = (ImageView) root.findViewById(R.id.close_img);
            Bg.apply(parent, 0xFF000000, null, null, 12, null, 0, Color.TRANSPARENT, Color.TRANSPARENT);
            int w = (int) (SketchwareUtil.getDisplayWidthPixels(getApplicationContext()) * 0.8);
            parent.setClipToOutline(true);
            File f = new File(getFilesDir(), "card_images/" + id + "/" + _image);

            if (f.exists() && f.length() > 0) {
                Picasso.get()
                        .load(f)
                        .into(display_img);
            } else {
                display_img.setImageResource(R.drawable.ic_broken_image);
                ViewGroup.LayoutParams display_img_layoutParams = display_img.getLayoutParams();
                setSize(display_img, w, w);
            }
            close_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View _view) {
                    dlg.dismiss();
                }
            });
        });
    }
    private void makeTopBar(View top_bar) {
        Bg.apply(top_bar, color(R.color.app_icon_disabled), null, null, 360, null, 0, null, null);
    }
    private void makeButton(View button, int style) {
        if (style == 0) {
            Bg.apply(button, color(R.color.app_surface), null, null, 16, null, 1, color(R.color.app_stroke), color(R.color.app_ripple));
        } else if (style == 1) {
            Bg.apply(button, null, new int[]{color(R.color.app_accent), color(R.color.app_accent_light)}, GradientDrawable.Orientation.LEFT_RIGHT, 16, null, 0, null, color(R.color.app_ripple));
        }
    }
    private void makeSwitchButton(View[] views, boolean toggled, int style) {
        final LinearLayout parent = (LinearLayout) views[0];
        final ImageView image = (ImageView) views[1];
        final TextView text = (TextView) views[2];
        if (toggled) {
            Bg.apply(parent, style == 0 ? color(R.color.app_surface) : color(R.color.app_surface_var), null, null, 16, null, style == 0 ? 0 : 2, style == 0 ? null : color(R.color.app_accent), color(R.color.app_ripple));
            parent.setElevation(style == 0 ? 1 * getResources().getDisplayMetrics().density : 0);
            image.setColorFilter(color(R.color.app_accent));
            text.setTextColor((color(R.color.app_accent)));
        } else {
            Bg.apply(parent, style == 0 ? color(R.color.app_surface_var) : color(R.color.app_surface), null, null, 16, null, style == 0 ? 0 : 2, style == 0 ? null : color(R.color.app_stroke), color(R.color.app_ripple));
            parent.setElevation(0);
            image.setColorFilter((color(R.color.app_text_secondary)));
            text.setTextColor(color(R.color.app_text_secondary));
        }
    }
    private void makeToggleButton(View v, boolean toggled) {
        if (toggled) {
            Bg.apply(v, color(R.color.app_accent), null, null, 16, null, 0, null, color(R.color.app_ripple));
            v.setElevation(1 * getResources().getDisplayMetrics().density);
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(color(R.color.app_text_primary));
            }
        } else {
            Bg.apply(v, color(R.color.app_surface_var), null, null, 16, null, 1, color(R.color.app_stroke), color(R.color.app_ripple));
            v.setElevation(0);
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(color(R.color.app_text_dark));
            }
        }
    }
    private void refreshSwitchGroup(List<View[]> views_list, int selectedId, int style) {
        for (View[] views : views_list) {
            boolean toggled = views[0].getId() == selectedId;
            makeSwitchButton(views, toggled, style);
        }
        applySortFilter(search_txt.getText().toString(), loadSortTypeId(), loadOrderId(), loadFilterId());
    }
    private void refreshToggleGroup(View[] views, int selectedId) {
        for (View v : views) {
            makeToggleButton(v, v.getId() == selectedId);
        }
        applySortFilter(search_txt.getText().toString(), loadSortTypeId(), loadOrderId(), loadFilterId());
    }
    private void matchImageSize(View[] views, float multiplier) {
        ImageView image = (ImageView) views[0];
        View view = views[1];
        view.post(() -> {
            int h = (int) Math.round(view.getHeight() * multiplier);
            setSize(image, ViewGroup.LayoutParams.WRAP_CONTENT, h);
        });
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
    private static float textScaleFromLevel(int level) {
        int clamped = Math.max(1, Math.min(5, level));
        int delta = clamped - 3;
        return (float) Math.pow(1.12, delta);
    }
    private static void setSize(View v, int width, int height) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp == null) return;

        boolean changed = false;

        if (width != KEEP && lp.width != width) {
            lp.width = width;
            changed = true;
        }

        if (height != KEEP && lp.height != height) {
            lp.height = height;
            changed = true;
        }

        if (changed) v.setLayoutParams(lp);
    }
    private static void setSizeDp(View v, float widthDp, float heightDp) {
        float d = v.getResources().getDisplayMetrics().density;
        int w = (widthDp == KEEP ? KEEP : Math.round(widthDp * d));
        int h = (heightDp == KEEP ? KEEP : Math.round(heightDp * d));
        setSize(v, w, h);
    }
    private int color(int id) {
        return ContextCompat.getColor(MainActivity.this, id);
    }
    private HashMap<String, Object> makeHeaderRow(String title) {
        HashMap<String, Object> h = new HashMap<>();
        h.put(KEY_ROW_TYPE, ROW_HEADER);
        h.put("title", title);
        return h;
    }
    private GradientDrawable.Orientation gradOrientationFromStyle(String style) {
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

    // ==================== DIALOGS / POPUPS ====================
    private void autoStyleDialogRoot(View content, int rootIdOr0) {
        if (rootIdOr0 == 0) return;
        View root = content.findViewById(rootIdOr0);
        if (root == null) return;

        Bg.apply(root, 0xFFFFFFFF, null, null, 12f, null, 2f, 0xFF212121, null);
    }
    private void showPopup(View anchor, PopupPos pos, int xDp, int yDp, int topTextRes, int bottomTextRes, int topIconRes, int bottomIconRes, Runnable onTop, Runnable onBottom, Runnable onDismiss) {
        if (anchor == null) return;
        anchor.post(() -> {
            LayoutInflater li = getLayoutInflater();
            View overlay = li.inflate(R.layout.mode_popup, null);

            PopupWindow pw = new PopupWindow(overlay, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            pw.setOutsideTouchable(true);
            pw.setFocusable(true);
            pw.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

            View parent = overlay.findViewById(R.id.parent);
            View top_lay = overlay.findViewById(R.id.camera_lay);
            View bottom_lay = overlay.findViewById(R.id.image_lay);
            TextView top_txt = (TextView) overlay.findViewById(R.id.camera_scanner_txt);
            TextView bottom_txt = (TextView) overlay.findViewById(R.id.scan_from_image_txt);
            ImageView top_img = (ImageView) overlay.findViewById(R.id.camera_img);
            ImageView bottom_img = (ImageView) overlay.findViewById(R.id.scan_img);

            float scale = textScaleFromLevel((int) textLevel);
            if (top_txt != null) {
                top_txt.setText(getString(topTextRes));
                applyTextScale(top_txt, scale);
            }
            if (bottom_txt != null) {
                bottom_txt.setText(getString(bottomTextRes));
                applyTextScale(bottom_txt, scale);
            }
            if (top_img != null) top_img.setImageResource(topIconRes);
            if (bottom_img != null) bottom_img.setImageResource(bottomIconRes);

            if (top_lay != null) Bg.apply(top_lay, 0xFFFFFFFF, null, null, 12, null, 2, 0xFF212121, 0xFFD2B6DC);
            if (bottom_lay != null) Bg.apply(bottom_lay, 0xFFFFFFFF, null, null, 12, null, 2, 0xFF212121, 0xFFD2B6DC);

            if (parent != null) {
                parent.setClickable(true);
                parent.setOnClickListener(v -> { });
            }

            final Runnable dismissAnim = () -> {
                if (parent == null) {
                    try { pw.dismiss(); } catch (Exception e) { }
                    if (onDismiss != null) onDismiss.run();
                    return;
                }
                parent.animate()
                        .alpha(0f)
                        .scaleX(0.96f)
                        .scaleY(0.96f)
                        .setDuration(120)
                        .setInterpolator(new android.view.animation.AccelerateInterpolator())
                        .withEndAction(() -> {
                            try { pw.dismiss(); } catch (Exception e) { }
                            if (onDismiss != null) onDismiss.run();
                        })
                        .start();
            };

            overlay.setClickable(true);
            overlay.setOnClickListener(v -> dismissAnim.run());

            if (top_lay != null) top_lay.setOnClickListener(v -> {
                dismissAnim.run();
                if (onTop != null) onTop.run();
            });

            if (bottom_lay != null) bottom_lay.setOnClickListener(v -> {
                dismissAnim.run();
                if (onBottom != null) onBottom.run();
            });

            if (parent != null) {
                parent.setAlpha(0f);
                parent.setScaleX(0.94f);
                parent.setScaleY(0.94f);
            }

            View decor = anchor.getRootView();
            pw.showAtLocation(decor, android.view.Gravity.TOP | android.view.Gravity.START, 0, 0);

            overlay.post(() -> {
                if (parent == null) return;

                parent.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int popupW = parent.getMeasuredWidth();
                int popupH = parent.getMeasuredHeight();

                int[] loc = new int[2];
                anchor.getLocationInWindow(loc);
                int aX = loc[0];
                int aY = loc[1];
                int aW = anchor.getWidth();
                int aH = anchor.getHeight();

                int winW = decor.getWidth();
                int winH = decor.getHeight();

                float density = getResources().getDisplayMetrics().density;
                int dx = (int) Math.round(xDp * density);
                int dy = (int) Math.round(yDp * density);

                int x = 0;
                int y = 0;

                switch (pos) {
                    case TOP_LEFT: {
                        x = aX;
                        y = aY - popupH;
                        break;
                    }
                    case TOP: {
                        x = aX + (aW / 2) - (popupW / 2);
                        y = aY - popupH;
                        break;
                    }
                    case TOP_RIGHT: {
                        x = aX + aW - popupW;
                        y = aY - popupH;
                        break;
                    }
                    case RIGHT_TOP: {
                        x = aX + aW;
                        y = aY;
                        break;
                    }
                    case RIGHT: {
                        x = aX + aW;
                        y = aY + (aH / 2) - (popupH / 2);
                        break;
                    }
                    case RIGHT_BOTTOM: {
                        x = aX + aW;
                        y = aY + aH - popupH;
                        break;
                    }
                    case BOTTOM_LEFT: {
                        x = aX;
                        y = aY + aH;
                        break;
                    }
                    case BOTTOM: {
                        x = aX + (aW / 2) - (popupW / 2);
                        y = aY + aH;
                        break;
                    }
                    case BOTTOM_RIGHT: {
                        x = aX + aW - popupW;
                        y = aY + aH;
                        break;
                    }
                    case LEFT_TOP: {
                        x = aX - popupW;
                        y = aY;
                        break;
                    }
                    case LEFT: {
                        x = aX - popupW;
                        y = aY + (aH / 2) - (popupH / 2);
                        break;
                    }
                    case LEFT_BOTTOM: {
                        x = aX - popupW;
                        y = aY + aH - popupH;
                        break;
                    }
                }

                x += dx;
                y += dy;

                if (x < 0) x = 0;
                if (y < 0) y = 0;
                if (x + popupW > winW) x = Math.max(0, winW - popupW);
                if (y + popupH > winH) y = Math.max(0, winH - popupH);

                parent.setTranslationX(x);
                parent.setTranslationY(y);

                parent.setPivotX(popupW);
                parent.setPivotY(0f);

                parent.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(140)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            });

            pw.setOnDismissListener(() -> {
                if (onDismiss != null) onDismiss.run();
            });
        });
    }
    private static void setupWindowTransparent(Dialog d) {
        if (d == null) return;
        d.setCancelable(true);
        d.setCanceledOnTouchOutside(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }
    private DialogShell<Dialog> showDialog(int layoutResId, Binder<Dialog> onBind, android.content.DialogInterface.OnDismissListener onDismiss) {
        Dialog d = new Dialog(MainActivity.this);
        View content = getLayoutInflater().inflate(layoutResId, null);
        d.setContentView(content);
        setupWindowTransparent(d);

        if (onDismiss != null) d.setOnDismissListener(onDismiss);
        if (onBind != null) onBind.bind(d, content);

        d.show();
        return new DialogShell<>(d, content);
    }
    private DialogShell<Dialog> showDialog(int layoutResId, int rootIdOr0, Binder<Dialog> onBind, android.content.DialogInterface.OnDismissListener onDismiss) {
        return showDialog(layoutResId, (dlg, content) -> {
            autoStyleDialogRoot(content, rootIdOr0);
            if (onBind != null) onBind.bind(dlg, content);
        }, onDismiss);
    }
    private DialogShell<Dialog> showDialog(int layoutResId, int rootIdOr0, Binder<Dialog> onBind) {
        return showDialog(layoutResId, rootIdOr0, onBind, null);
    }
    private DialogShell<com.google.android.material.bottomsheet.BottomSheetDialog> showBottomSheet(int layoutResId, Binder<com.google.android.material.bottomsheet.BottomSheetDialog> onBind, android.content.DialogInterface.OnDismissListener onDismiss) {
        com.google.android.material.bottomsheet.BottomSheetDialog bs = new com.google.android.material.bottomsheet.BottomSheetDialog(MainActivity.this);

        View content = getLayoutInflater().inflate(layoutResId, null);
        bs.setContentView(content);
        setupWindowTransparent(bs);

        View sheet = bs.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet != null) sheet.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        if (onDismiss != null) bs.setOnDismissListener(onDismiss);
        if (onBind != null) onBind.bind(bs, content);

        bs.show();
        return new DialogShell<>(bs, content);
    }
    private DialogShell<com.google.android.material.bottomsheet.BottomSheetDialog> showBottomSheet(int layoutResId, int rootIdOr0, Binder<com.google.android.material.bottomsheet.BottomSheetDialog> onBind, android.content.DialogInterface.OnDismissListener onDismiss) {
        return showBottomSheet(layoutResId, (dlg, content) -> {
            autoStyleDialogRoot(content, rootIdOr0);
            if (onBind != null) onBind.bind(dlg, content);
        }, onDismiss);
    }
    private DialogShell<com.google.android.material.bottomsheet.BottomSheetDialog> showBottomSheet(int layoutResId, int rootIdOr0, Binder<com.google.android.material.bottomsheet.BottomSheetDialog> onBind) {
        return showBottomSheet(layoutResId, rootIdOr0, onBind, null);
    }
    private static final class DialogShell<T> {
        final T dialog;
        final View content;
        DialogShell(T dialog, View content) {
            this.dialog = dialog;
            this.content = content;
        }
    }
    private interface Binder<V> {
        void bind(V dialog, View content);
    }
    private enum PopupPos {
        TOP_LEFT, TOP, TOP_RIGHT,
        RIGHT_TOP, RIGHT, RIGHT_BOTTOM,
        BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT,
        LEFT_TOP, LEFT, LEFT_BOTTOM
    }

    // ==================== ADAPTERS ====================
    public class Cards_recAdapter extends RecyclerView.Adapter<Cards_recAdapter.ViewHolder> {

        public static final int TYPE_HEADER = 0;
        public static final int TYPE_ITEM = 1;

        ArrayList<HashMap<String, Object>> _data;

        public Cards_recAdapter(ArrayList<HashMap<String, Object>> _arr) {
            _data = _arr;
        }

        @Override
        public int getItemViewType(int position) {
            HashMap<String, Object> m = _data.get(position);
            Object t = m.get(KEY_ROW_TYPE);
            if (t != null && ROW_HEADER.equals(String.valueOf(t))) return TYPE_HEADER;
            return TYPE_ITEM;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater _inflater = getLayoutInflater();

            if (viewType == TYPE_HEADER) {
                View _v = _inflater.inflate(R.layout.header, parent, false);
                RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                _v.setLayoutParams(_lp);
                return new ViewHolder(_v, true);
            }

            View _v = _inflater.inflate(R.layout.cards_recycler, parent, false);
            RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            _v.setLayoutParams(_lp);
            return new ViewHolder(_v, false);
        }

        @Override
        public void onBindViewHolder(ViewHolder _holder, final int _position) {
            View _view = _holder.itemView;

            final HashMap<String, Object> m = _data.get(_position);

            if (_holder.isHeader) {
                Object titleObj = m.get("title");
                _holder.tvHeader.setText(titleObj == null ? "" : String.valueOf(titleObj));
                return;
            }

            final FrameLayout parent = _view.findViewById(R.id.parent);
            final ImageView type_img = _view.findViewById(R.id.type_img);
            final TextView card_name = _view.findViewById(R.id.card_name);
            final TextView card_label = _view.findViewById(R.id.card_label);

            type_img.setVisibility(View.VISIBLE);
            Object nameObj = m.get("name");
            card_name.setText(nameObj == null ? "" : String.valueOf(nameObj));
            ArrayList<Integer> picked = new ArrayList<>();
            try {
                Object raw = m.get("colors");
                if (raw != null) {
                    String s = String.valueOf(raw);
                    ArrayList<Integer> tmp = new Gson().fromJson(
                            s,
                            new com.google.gson.reflect.TypeToken<ArrayList<Integer>>() {}.getType()
                    );
                    if (tmp != null) picked.addAll(tmp);
                }
            } catch (Exception ignore) { }

            GradientDrawable.Orientation ori = GradientDrawable.Orientation.LEFT_RIGHT;
            try {
                Object st = m.get("grad_style");
                String style = (st == null) ? null : String.valueOf(st);
                ori = gradOrientationFromStyle(style);
            } catch (Exception ignore) { }

            Integer solidColor = null;
            int[] gradientColors = null;

            if (picked.size() <= 0) {
                solidColor = 0xFFFFFFFF;
            } else if (picked.size() == 1) {
                solidColor = picked.get(0);
            } else {
                gradientColors = new int[picked.size()];
                for (int i = 0; i < picked.size(); i++) {
                    gradientColors[i] = picked.get(i);
                }
            }

            Bg.apply(parent, solidColor, gradientColors, ori, 16f, null, 0f, 0x00000000, 0xFFD2B6DC);
            float scale = textScaleFromLevel((int) textLevel);
            applyTextScale(card_name, scale);
            applyTextScale(card_label, scale);
            parent.post(() -> {
                int w = parent.getWidth();
                int type_w = (int) Math.round(w * 0.15);
                int type_m = (int) Math.round(w * 0.05);
                setSize(parent, KEEP, (int) Math.round(w * 0.7));
                ViewGroup.LayoutParams t_lp = type_img.getLayoutParams();
                t_lp.width = type_w;
                t_lp.height = type_w;
                if (t_lp instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams t_mlp = (ViewGroup.MarginLayoutParams) t_lp;
                    t_mlp.setMargins(0, 0, type_m, type_m);
                }
                type_img.setLayoutParams(t_lp);
                ViewGroup.MarginLayoutParams c_mlp = (ViewGroup.MarginLayoutParams) card_name.getLayoutParams();
                c_mlp.setMargins(type_m, type_m, type_m, type_m);
                card_name.setLayoutParams(c_mlp);
                if ((boolean)m.get("folder")) {
                    ArrayList<HashMap<String, Object>> folder_data = new ArrayList<>();
                    folder_data.addAll((ArrayList<HashMap<String, Object>>) _data.get(_position).get("data"));
                    int len = folder_data.size();
                    card_name.setAllCaps(false);
                    card_label.setText(cardsCountText(len));
                    card_label.setVisibility(View.VISIBLE);
                    if (isVirtualFavorites(m)) {
                        type_img.setImageResource(R.drawable.ic_fav);
                    } else {
                        type_img.setImageResource(R.drawable.ic_folder);
                    }
                } else {
                    card_name.setAllCaps(true);
                    card_label.setVisibility(View.GONE);
                    if (m.containsKey("type")) {
                        String type = String.valueOf(m.get("type"));
                        if (type.equals("QR_CODE")) {
                            type_img.setImageResource(R.drawable.ic_qr_soft);
                        } else if (type.equals("CODE_128") | type.equals("EAN_13")) {
                            type_img.setImageResource(R.drawable.ic_bar_soft);
                        } else {
                            type_img.setVisibility(View.GONE);
                        }
                    } else {
                        type_img.setImageResource(R.drawable.ic_img_soft);
                    }
                }
            });
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View _view) {
                    try {
                        double cur = (double) m.get("used");
                        cur++;
                        m.put("used", cur);
                        card_prefs.edit().putString("cards", new Gson().toJson(cards_list_all)).apply();
                    } catch (Exception ignore) {}
                    if ((boolean)m.get("folder")) {
                        if (isVirtualFavorites(m)) {
                            folderIdStack.clear();
                            folderNameStack.clear();

                            folderIdStack.add("__favorites__");
                            folderNameStack.add(getString(R.string.favorites));

                            folderPath = joinWithSlash(folderNameStack);
                            inFolder = true;

                            applySortFilter(
                                    search_txt.getText().toString(),
                                    loadSortTypeId(),
                                    loadOrderId(),
                                    loadFilterId()
                            );
                            return;
                        }
                        String clickedId = String.valueOf(m.get("id"));
                        String clickedName = String.valueOf(m.get("name"));

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
                        displayInfo(m, false);
                    }
                }
            });
            parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View _view) {
                    displayInfo(m, false);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return _data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public boolean isHeader;

            public TextView tvHeader;

            public FrameLayout parent;
            public LinearLayout vertical_layout;
            public ImageView type_img;
            public TextView card_name;
            public TextView card_label;

            public ViewHolder(View v, boolean header) {
                super(v);
                isHeader = header;

                if (header) {
                    tvHeader = v.findViewById(R.id.tvHeader);
                } else {
                    parent = v.findViewById(R.id.parent);
                    vertical_layout = v.findViewById(R.id.vertical_layout);
                    type_img = v.findViewById(R.id.type_img);
                    card_name = v.findViewById(R.id.card_name);
                    card_label = v.findViewById(R.id.card_label);
                }
            }
        }
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
                Bg.apply(parent, 0xFFFFFFFF, null, null, 360, null, 2, 0xFF212121, 0xFFD2B6DC);
                plus_lay.setVisibility(View.VISIBLE);
                color.setVisibility(View.GONE);
                plus_img.setImageResource(R.drawable.ic_add_grey);
                ViewGroup.LayoutParams plus_img_layoutParams = plus_img.getLayoutParams();
                plus_img_layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                plus_img_layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                plus_img.setLayoutParams(plus_img_layoutParams);
            } else {
                if (_data.get((int)(_position)).get("color").toString().equals("settings")) {
                    Bg.apply(parent, 0xFFFFFFFF, null, null, 360, null, 2, 0xFF212121, 0xFFD2B6DC);
                    plus_lay.setVisibility(View.VISIBLE);
                    color.setVisibility(View.GONE);
                    plus_img.setImageResource(R.drawable.ic_settings);
                    plus_img.post(() -> {
                        ViewGroup.LayoutParams plus_img_layoutParams = plus_img.getLayoutParams();
                        plus_img_layoutParams.width = (int) (plus_img.getWidth() * 0.6);
                        plus_img_layoutParams.height = (int) (plus_img.getHeight() * 0.6);
                        plus_img.setLayoutParams(plus_img_layoutParams);
                    });
                } else {
                    int bgColor = (int) (_data.get((int)(_position)).get("color"));
                    Bg.apply(parent, bgColor, null, null, 360f, null, 2f, 0xFF212121, 0xFFD2B6DC);
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
                        showDialog(R.layout.radio_dialog, R.id.parent, (dlg, root) -> {
                            final RadioGroup grad_styles = (RadioGroup) root.findViewById(R.id.grad_styles);
                            final TextView title_txt = (TextView) root.findViewById(R.id.title_txt);
                            final TextView save_txt = (TextView) root.findViewById(R.id.save_txt);
                            final TextView close_txt = (TextView) root.findViewById(R.id.close_txt);
                            final LinearLayout button_bar = (LinearLayout) root.findViewById(R.id.button_bar);
                            final RadioButton leftright = (RadioButton) root.findViewById(R.id.leftright);
                            final RadioButton rightleft = (RadioButton) root.findViewById(R.id.rightleft);
                            final RadioButton topbottom = (RadioButton) root.findViewById(R.id.topbottom);
                            final RadioButton bottomtop = (RadioButton) root.findViewById(R.id.bottomtop);
                            final RadioButton tl_br = (RadioButton) root.findViewById(R.id.tl_br);
                            final RadioButton tr_bl = (RadioButton) root.findViewById(R.id.tr_bl);
                            final RadioButton bl_tr = (RadioButton) root.findViewById(R.id.bl_tr);
                            final RadioButton br_tl = (RadioButton) root.findViewById(R.id.br_tl);
                            float scale = textScaleFromLevel((int) textLevel);
                            TextView[] views = new TextView[] { title_txt, save_txt, close_txt };
                            for (TextView tv : views) {
                                if (tv != null) applyTextScale(tv, scale);
                            }
                            RadioButton[] buttons = new RadioButton[] { leftright, rightleft, topbottom, bottomtop, tl_br, tr_bl, bl_tr, br_tl };
                            for (RadioButton rb : buttons) {
                                if (rb != null) applyTextScale(rb, scale);
                            }
                            Bg.apply(save_txt, 0xFFD2B6DC, null, null, 12, null, 0, Color.TRANSPARENT, 0xFFF2EAF5);
                            Bg.apply(close_txt, 0xFFD2B6DC, null, null, 12, null, 0, Color.TRANSPARENT, 0xFFF2EAF5);
                            Bg.apply(button_bar, 0xFFD2B6DC, null, null, 0, new float[]{0, 0, 12, 12}, 0, Color.TRANSPARENT, Color.TRANSPARENT);
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
                                    dlg.dismiss();
                                }
                            });
                            close_txt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View _view) {
                                    dlg.dismiss();
                                }
                            });
                        });
                    } else {
                        showDialog(R.layout.color_picker_dialog, R.id.parent, (dlg, root) -> {
                            final HsvColorPickerView color_picker = (HsvColorPickerView) root.findViewById(R.id.color_picker);
                            final LinearLayout button_bar = (LinearLayout) root.findViewById(R.id.button_bar);
                            final TextView add_txt = (TextView) root.findViewById(R.id.add_txt);
                            final TextView close_txt = (TextView) root.findViewById(R.id.close_txt);
                            applyTextScale(add_txt, textScaleFromLevel((int) textLevel));
                            applyTextScale(close_txt, textScaleFromLevel((int) textLevel));
                            if (!isNew) {
                                add_txt.setText(getString(R.string.change));
                            }
                            Bg.apply(add_txt, 0xFFD2B6DC, null, null, 12, null, 0, Color.TRANSPARENT, 0xFFF2EAF5);
                            Bg.apply(close_txt, 0xFFD2B6DC, null, null, 12, null, 0, Color.TRANSPARENT, 0xFFF2EAF5);
                            Bg.apply(button_bar, 0xFFD2B6DC, null, null, 0, new float[]{0, 0, 12, 12}, 0, Color.TRANSPARENT, Color.TRANSPARENT);
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
                                    dlg.dismiss();
                                }
                            });
                            close_txt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View _view) {
                                    dlg.dismiss();
                                }
                            });
                        });
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
            String v = String.valueOf(_data.get(_position).get("image"));
            if (v.equals("plus")) {
                picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picture.setImageResource(R.drawable.ic_add_photo);
                float parent_density = getResources().getDisplayMetrics().density;
                parent.setClickable(true);
                parent.setBackground(new RippleDrawable(
                        new ColorStateList(
                                new int[][]{new int[]{}},
                                new int[]{Color.TRANSPARENT}
                        ),
                        new GradientDrawable() {
                            public GradientDrawable getIns(int a, int b, int c, int d) {
                                this.setCornerRadius(a);
                                this.setStroke(b, c, 8 * parent_density, 4 * parent_density);
                                this.setColor(d);
                                return this;
                            }
                        }.getIns((int) (12 * parent_density), (int) (3 * parent_density), 0xFFC6CACF, 0xFFF2EAF5),
                        null
                ));
                picture.post(() -> {
                    ViewGroup.LayoutParams picture_layoutParams = picture.getLayoutParams();
                    picture_layoutParams.width = (int) (picture.getWidth() * 0.35);
                    picture_layoutParams.height = (int) (picture.getHeight() * 0.35);
                    picture.setLayoutParams(picture_layoutParams);
                });
            } else {
                picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
                File f = new File(getFilesDir(), "card_images/" + id + "/" + v);

                if (f.exists() && f.length() > 0) {
                    Picasso.get()
                            .load(f)
                            .fit()
                            .centerCrop()
                            .into(picture);
                } else {
                    picture.setImageResource(R.drawable.ic_broken_image);
                }
                Bg.apply(parent, 0xFFFFFFFF, null, null, 12, null, 0, Color.TRANSPARENT, Color.TRANSPARENT);
                ViewGroup.LayoutParams picture_layoutParams = picture.getLayoutParams();
                picture_layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                picture_layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                picture.setLayoutParams(picture_layoutParams);
            }
            parent.setClipToOutline(true);
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View _view) {
                    if (v.equals("plus")) {
                        showPopup(parent, PopupPos.RIGHT_TOP, (int) 0, (int) 0, R.string.take_photo, R.string.pick_image, R.drawable.ic_camera, R.drawable.ic_image,
                                () -> {
                                    scan = false;
                                    openScannerOrRequestPermission();
                                },
                                () -> {
                                    pickImageForCard();
                                }, null
                        );
                    } else {
                        displayImage(v);
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
            Bg.apply(item, 0xFFFFFFFF, null, null, 12, null, 0, Color.TRANSPARENT, 0xFFD2B6DC);
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
                        showEanWarning();
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

    // ==================== OTHER ====================
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
}