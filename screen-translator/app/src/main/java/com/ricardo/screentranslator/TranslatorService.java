package com.ricardo.screentranslator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TranslatorService extends Service {

    private static final String CHANNEL = "translator";
    private static final long AUTO_INTERVAL_MS = 3000;
    private static final long CAPTURE_DELAY_MS = 200;

    private final Handler main = new Handler(Looper.getMainLooper());
    private final HashMap<String, String> cache = new HashMap<>();

    private WindowManager wm;
    private OverlayView overlay;
    private TextView button;
    private WindowManager.LayoutParams buttonLp;

    private MediaProjection projection;
    private MediaProjection.Callback projectionCallback;
    private VirtualDisplay virtualDisplay;
    private ImageReader reader;
    private int capW;
    private int capH;
    private Bitmap lastBitmap;
    private int lastHash;

    private TextRecognizer recognizer;
    private Translator translator;

    private boolean auto = false;
    private boolean busy = false;
    private boolean errorShown = false;

    // estado del arrastre del botón
    private float downRawX;
    private float downRawY;
    private int startX;
    private int startY;
    private long downTime;
    private boolean moved;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (projection != null) {
            return START_NOT_STICKY;
        }

        NotificationChannel ch = new NotificationChannel(CHANNEL, "Traductor", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        Notification n = new Notification.Builder(this, CHANNEL)
                .setContentTitle("Traductor de pantalla activo")
                .setContentText("Botón flotante T: toque = traducir, mantener = automático")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .build();
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        } else {
            startForeground(1, n);
        }

        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        int code = intent.getIntExtra("code", 0);
        Intent data = intent.getParcelableExtra("data");
        if (data == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        projection = mpm.getMediaProjection(code, data);
        projectionCallback = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                main.post(TranslatorService.this::stopSelf);
            }
        };
        projection.registerCallback(projectionCallback, main);

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        translator = Translation.getClient(new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.SPANISH)
                .build());

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        addOverlay();
        addButton();
        return START_NOT_STICKY;
    }

    // ---------------------------------------------------------------- UI

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private void addOverlay() {
        overlay = new OverlayView(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        if (Build.VERSION.SDK_INT >= 28) {
            lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        wm.addView(overlay, lp);
    }

    private void addButton() {
        button = new TextView(this);
        button.setText("T");
        button.setTextColor(Color.WHITE);
        button.setTextSize(18);
        button.setGravity(Gravity.CENTER);
        setButtonColor(0xCC1565C0);

        int size = dp(48);
        buttonLp = new WindowManager.LayoutParams(
                size, size,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        buttonLp.gravity = Gravity.TOP | Gravity.START;
        buttonLp.x = 0;
        buttonLp.y = dp(120);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downRawX = e.getRawX();
                        downRawY = e.getRawY();
                        startX = buttonLp.x;
                        startY = buttonLp.y;
                        downTime = e.getEventTime();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = e.getRawX() - downRawX;
                        float dy = e.getRawY() - downRawY;
                        if (Math.abs(dx) > dp(8) || Math.abs(dy) > dp(8)) {
                            moved = true;
                        }
                        if (moved) {
                            buttonLp.x = (int) (startX + dx);
                            buttonLp.y = (int) (startY + dy);
                            wm.updateViewLayout(button, buttonLp);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) {
                            if (e.getEventTime() - downTime > 600) {
                                setAuto(!auto);
                            } else {
                                onTap();
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        wm.addView(button, buttonLp);
    }

    private void setButtonColor(int color) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        button.setBackground(bg);
    }

    private void onTap() {
        if (auto) {
            setAuto(false);
            overlay.clear();
        } else if (overlay.hasBoxes()) {
            overlay.clear();
        } else {
            runOnce();
        }
    }

    private void setAuto(boolean on) {
        auto = on;
        main.removeCallbacks(loop);
        if (on) {
            button.setText("A");
            setButtonColor(0xCC2E7D32);
            main.post(loop);
        } else {
            button.setText("T");
            setButtonColor(0xCC1565C0);
        }
    }

    private final Runnable loop = new Runnable() {
        @Override
        public void run() {
            if (!auto) {
                return;
            }
            runOnce();
            main.postDelayed(this, AUTO_INTERVAL_MS);
        }
    };

    // ---------------------------------------------------------------- captura

    private void ensureCapture() {
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        if (reader != null && dm.widthPixels == capW && dm.heightPixels == capH) {
            return;
        }
        ImageReader nr = ImageReader.newInstance(dm.widthPixels, dm.heightPixels, PixelFormat.RGBA_8888, 2);
        if (virtualDisplay == null) {
            virtualDisplay = projection.createVirtualDisplay("translator",
                    dm.widthPixels, dm.heightPixels, dm.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    nr.getSurface(), null, main);
        } else {
            virtualDisplay.resize(dm.widthPixels, dm.heightPixels, dm.densityDpi);
            virtualDisplay.setSurface(nr.getSurface());
        }
        if (reader != null) {
            reader.close();
        }
        reader = nr;
        capW = dm.widthPixels;
        capH = dm.heightPixels;
        lastBitmap = null;
        lastHash = 0;
    }

    private Bitmap grab() {
        Image img = null;
        try {
            img = reader.acquireLatestImage();
        } catch (Exception ignored) {
            // sin imagen nueva
        }
        if (img == null) {
            return lastBitmap;
        }
        try {
            Image.Plane plane = img.getPlanes()[0];
            ByteBuffer buf = plane.getBuffer();
            int pixelStride = plane.getPixelStride();
            int rowStride = plane.getRowStride();
            int w = img.getWidth();
            int h = img.getHeight();
            int padded = rowStride / pixelStride;
            Bitmap full = Bitmap.createBitmap(padded, h, Bitmap.Config.ARGB_8888);
            full.copyPixelsFromBuffer(buf);
            Bitmap bmp = (padded == w) ? full : Bitmap.createBitmap(full, 0, 0, w, h);
            lastBitmap = bmp;
            return bmp;
        } finally {
            img.close();
        }
    }

    private int quickHash(Bitmap b) {
        int w = 48;
        int h = 27;
        Bitmap s = Bitmap.createScaledBitmap(b, w, h, true);
        int[] px = new int[w * h];
        s.getPixels(px, 0, w, 0, 0, w, h);
        int hash = 1;
        for (int v : px) {
            hash = 31 * hash + (v & 0xF8F8F8F8);
        }
        return hash;
    }

    // ---------------------------------------------------------------- OCR + traducción

    private void runOnce() {
        if (busy) {
            return;
        }
        busy = true;
        try {
            ensureCapture();
        } catch (Exception e) {
            busy = false;
            Toast.makeText(this, "No se pudo capturar la pantalla: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        // Se oculta la traducción un instante para que el OCR vea el texto original.
        final boolean hadBoxes = overlay.hasBoxes();
        if (hadBoxes) {
            overlay.setVisibility(View.INVISIBLE);
        }
        main.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp;
                try {
                    bmp = grab();
                } catch (Exception e) {
                    bmp = null;
                }
                if (hadBoxes) {
                    overlay.setVisibility(View.VISIBLE);
                }
                if (bmp == null) {
                    busy = false;
                    return;
                }
                int h = quickHash(bmp);
                if (hadBoxes && h == lastHash) {
                    busy = false;
                    return;
                }
                lastHash = h;
                recognize(bmp);
            }
        }, CAPTURE_DELAY_MS);
    }

    private void recognize(Bitmap bmp) {
        recognizer.process(InputImage.fromBitmap(bmp, 0))
                .addOnSuccessListener(this::translateBlocks)
                .addOnFailureListener(e -> busy = false);
    }

    private static String clean(String s) {
        return s.replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }

    private static boolean worthTranslating(String s) {
        int letters = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetter(s.charAt(i))) {
                letters++;
            }
        }
        return letters >= 3 && letters * 2 >= s.length();
    }

    private void translateBlocks(Text text) {
        final List<Rect> rects = new ArrayList<>();
        final List<String> sources = new ArrayList<>();
        for (Text.TextBlock b : text.getTextBlocks()) {
            Rect r = b.getBoundingBox();
            if (r == null) {
                continue;
            }
            String s = clean(b.getText());
            if (!worthTranslating(s)) {
                continue;
            }
            rects.add(r);
            sources.add(s);
        }
        if (rects.isEmpty()) {
            overlay.setBoxes(new ArrayList<OverlayView.Box>());
            busy = false;
            return;
        }

        final OverlayView.Box[] results = new OverlayView.Box[rects.size()];
        final int[] left = {rects.size()};
        for (int i = 0; i < rects.size(); i++) {
            final int idx = i;
            final Rect r = rects.get(i);
            final String src = sources.get(i);
            String cached = cache.get(src);
            if (cached != null) {
                blockDone(results, left, idx, src, r, cached);
                continue;
            }
            translator.translate(src)
                    .addOnSuccessListener(t -> {
                        cache.put(src, t);
                        blockDone(results, left, idx, src, r, t);
                    })
                    .addOnFailureListener(e -> {
                        if (!errorShown) {
                            errorShown = true;
                            Toast.makeText(this, "Error al traducir: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        blockDone(results, left, idx, src, r, null);
                    });
        }
    }

    private void blockDone(OverlayView.Box[] results, int[] left, int idx,
                           String src, Rect r, String translated) {
        if (translated != null && !translated.equalsIgnoreCase(src)) {
            results[idx] = new OverlayView.Box(r, translated);
        }
        left[0]--;
        if (left[0] == 0) {
            List<OverlayView.Box> out = new ArrayList<>();
            for (OverlayView.Box b : results) {
                if (b != null) {
                    out.add(b);
                }
            }
            overlay.setBoxes(out);
            busy = false;
        }
    }

    // ---------------------------------------------------------------- limpieza

    @Override
    public void onDestroy() {
        auto = false;
        main.removeCallbacksAndMessages(null);
        try {
            if (overlay != null) {
                wm.removeView(overlay);
            }
            if (button != null) {
                wm.removeView(button);
            }
        } catch (Exception ignored) {
            // vista ya removida
        }
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        if (reader != null) {
            reader.close();
        }
        if (projection != null) {
            if (projectionCallback != null) {
                projection.unregisterCallback(projectionCallback);
            }
            projection.stop();
        }
        if (recognizer != null) {
            recognizer.close();
        }
        if (translator != null) {
            translator.close();
        }
        super.onDestroy();
    }
}
