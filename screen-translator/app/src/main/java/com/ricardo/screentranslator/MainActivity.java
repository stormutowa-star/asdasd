package com.ricardo.screentranslator;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

public class MainActivity extends Activity {

    private static final int REQ_PROJECTION = 1;
    private static final int REQ_OVERLAY = 2;

    private Translator translator;
    private TextView status;

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
        translator.downloadModelIfNeeded(new DownloadConditions.Builder().build())
                .addOnSuccessListener(v -> {
                    status.setText("Modelo listo (offline). Acepta la captura de pantalla.");
                    MediaProjectionManager m =
                            (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    startActivityForResult(m.createScreenCaptureIntent(), REQ_PROJECTION);
                })
                .addOnFailureListener(e -> status.setText(
                        "No se pudo bajar el modelo: " + e.getMessage()
                                + "\nConéctate a internet una vez y reintenta."));
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
        translator.close();
        super.onDestroy();
    }
}
