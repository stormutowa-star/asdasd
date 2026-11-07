package com.ricardo.screentranslator;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.File;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQ_PROJECTION = 1;
    private static final int REQ_OVERLAY = 2;

    private Translator translator;
    private TextView status;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long downloadStartMs;
    private boolean polling;
    private final Runnable poller = new Runnable() {
        @Override
        public void run() {
            if (!polling) return;
            status.setText(buildDownloadStatus());
            handler.postDelayed(this, 700);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        translator = Translation.getClient(new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.SPANISH)
                .build());

        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(pad, pad * 2, pad, pad);

        TextView title = new TextView(this);
        title.setText("Traductor de pantalla\nInglés → Español (offline)");
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        status = new TextView(this);
        status.setTextSize(15);
        status.setPadding(0, pad, 0, pad);
        status.setText("1) Pulsa Iniciar.\n2) Da permiso de \"Mostrar sobre otras apps\".\n"
                + "3) La primera vez baja el modelo (~30 MB, con internet). Después funciona sin internet.\n"
                + "4) Abre tu juego. Botón flotante \"T\": toque = traducir ahora, "
                + "mantener pulsado = modo automático, arrastrar = mover.");
        root.addView(status);

        Button start = new Button(this);
        start.setText("Iniciar");
        start.setOnClickListener(v -> startFlow());
        root.addView(start);

        Button stop = new Button(this);
        stop.setText("Detener");
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, TranslatorService.class));
            status.setText("Traductor detenido.");
        });
        root.addView(stop);

        setContentView(root);

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
        }
    }

    private void startFlow() {
        if (!Settings.canDrawOverlays(this)) {
            status.setText("Activa \"Mostrar sobre otras apps\" para esta app, vuelve y pulsa Iniciar otra vez.");
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())), REQ_OVERLAY);
            return;
        }
        status.setText("Preparando modelo de traducción...");
        downloadStartMs = SystemClock.elapsedRealtime();
        polling = true;
        handler.removeCallbacks(poller);
        handler.postDelayed(poller, 700);
        translator.downloadModelIfNeeded(new DownloadConditions.Builder().build())
                .addOnSuccessListener(v -> {
                    stopPolling();
                    status.setText("Modelo listo (offline). Acepta la captura de pantalla.");
                    MediaProjectionManager m =
                            (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    startActivityForResult(m.createScreenCaptureIntent(), REQ_PROJECTION);
                })
                .addOnFailureListener(e -> {
                    stopPolling();
                    status.setText(
                            "No se pudo bajar el modelo: " + e.getMessage()
                                    + "\nConéctate a internet una vez y reintenta.");
                });
    }

    private void stopPolling() {
        polling = false;
        handler.removeCallbacks(poller);
    }

    private static String mb(long bytes) {
        return String.format(Locale.US, "%.1f", bytes / 1048576.0);
    }

    private static long dirSize(File f) {
        try {
            if (f == null || !f.exists()) return 0;
            if (f.isFile()) return f.length();
            File[] kids = f.listFiles();
            if (kids == null) return 0;
            long sum = 0;
            for (File k : kids) sum += dirSize(k);
            return sum;
        } catch (Exception e) {
            return 0;
        }
    }

    private String buildDownloadStatus() {
        long secs = (SystemClock.elapsedRealtime() - downloadStartMs) / 1000;
        String disk = " · en disco: "
                + mb(dirSize(getNoBackupFilesDir()) + dirSize(getFilesDir())) + " MB";
        try {
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm == null) return "Descargando modelo... · " + secs + " s" + disk;
            String active = null;
            String failed = null;
            Cursor c = dm.query(new DownloadManager.Query());
            if (c != null) {
                try {
                    int iStatus = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int iDone = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                    int iTotal = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                    int iReason = c.getColumnIndex(DownloadManager.COLUMN_REASON);
                    while (c.moveToNext()) {
                        int st = c.getInt(iStatus);
                        long done = c.getLong(iDone);
                        long total = c.getLong(iTotal);
                        int reason = c.getInt(iReason);
                        if (st == DownloadManager.STATUS_FAILED) {
                            if (failed == null) failed = "Falló la descarga (código " + reason + ")";
                        } else if (active == null && (st == DownloadManager.STATUS_PENDING
                                || st == DownloadManager.STATUS_RUNNING
                                || st == DownloadManager.STATUS_PAUSED)) {
                            StringBuilder sb = new StringBuilder("Descargando modelo... ");
                            if (total > 0) {
                                sb.append(done * 100 / total).append("% (")
                                        .append(mb(done)).append(" / ").append(mb(total))
                                        .append(" MB)");
                            } else {
                                sb.append(mb(done)).append(" MB");
                            }
                            if (st == DownloadManager.STATUS_PENDING) {
                                sb.append(" · en cola");
                            } else if (st == DownloadManager.STATUS_PAUSED) {
                                if (reason == 1) sb.append(" · reintentando");
                                else if (reason == 2) sb.append(" · esperando red (¿sin internet?)");
                                else if (reason == 3) sb.append(" · esperando wifi");
                                else sb.append(" · pausada (código ").append(reason).append(")");
                            }
                            active = sb.toString();
                        }
                    }
                } finally {
                    c.close();
                }
            }
            String main = active != null ? active : failed;
            if (main != null) return main + " · " + secs + " s" + disk;
        } catch (Exception e) {
            return "Descargando modelo... · " + secs + " s";
        }
        return "Esperando respuesta del servidor... · " + secs + " s" + disk;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PROJECTION) {
            if (resultCode == RESULT_OK && data != null) {
                Intent i = new Intent(this, TranslatorService.class);
                i.putExtra("code", resultCode);
                i.putExtra("data", data);
                startForegroundService(i);
                status.setText("Traductor activo. Abre tu juego y usa el botón flotante \"T\".");
                moveTaskToBack(true);
            } else {
                status.setText("Captura de pantalla cancelada.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopPolling();
        translator.close();
        super.onDestroy();
    }
}
