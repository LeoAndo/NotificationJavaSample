package com.leoleo.notificationjavasample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String EXTRA_KEY_NOTIFICATION_ID = "notificationId";
    private static final String EXTRA_KEY_TITLE = "title";
    private NotificationManager notificationManager;

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                Log.d(TAG, "granted: " + granted);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notificationManager = getSystemService(NotificationManager.class);

        // Notification Channelの作成
        createNotificationChannels();

        // Spinnerの設定
        final Spinner spPriority = findViewById(R.id.priority);
        List<String> entries =
                Arrays.stream(NotificationChannelId.values())
                        .map(NotificationChannelId::getUserVisibleName)
                        .collect(Collectors.toList());
        spPriority.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entries)
        );

        final EditText edtNotificationId = findViewById(R.id.notification_id);

        findViewById(R.id.show).setOnClickListener(v -> {
            try {
                final int notificationId = Integer.parseInt(edtNotificationId.getText().toString());
                Optional<NotificationChannelId> notificationChannelId =
                        Arrays.stream(NotificationChannelId.values())
                                .filter(n -> n.ordinal() == spPriority.getSelectedItemPosition())
                                .findFirst();
                if (!notificationChannelId.isPresent()) return;

                final String channelId = notificationChannelId.get().name();
                final String title = "鉄人" + SystemClock.elapsedRealtime() + "号";

                // 通知タップ時に起動するIntentを作成
                final Intent intent = new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(EXTRA_KEY_NOTIFICATION_ID, notificationId)
                        .putExtra(EXTRA_KEY_TITLE, title);
                final int requestCode = UUID.randomUUID().hashCode();
                final PendingIntent contentIntent = PendingIntent.getActivity(
                        this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE
                );

                // setSmallIcon設定しないとnotificationManager.notifyの後に以下のエラーが発生する
                // java.lang.IllegalArgumentException: Invalid notification (no valid small icon)
                final NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this, channelId)
                                .setContentTitle(title)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentText("Hello, Notification")
                                .setAutoCancel(false)
                                .setCategory(NotificationCompat.CATEGORY_EVENT)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setLocalOnly(true);
                builder.setContentIntent(contentIntent);
                final Notification notification = builder.build();
                notificationManager.notify(notificationId, notification);
            } catch (NumberFormatException e) {
                Log.e(TAG, "error: ", e);
            }
        });

        findViewById(R.id.cancel).setOnClickListener(v -> {
            try {
                final int notificationId = Integer.parseInt(edtNotificationId.getText().toString());
                notificationManager.cancel(notificationId);
            } catch (NumberFormatException e) {
                Log.e(TAG, "error: ", e);
            }
        });

        final int notificationId = getIntent().getIntExtra(EXTRA_KEY_NOTIFICATION_ID, -1);
        final String title = getIntent().getStringExtra(EXTRA_KEY_TITLE);
        Log.d(TAG, "notificationId: " + notificationId + " title: " + title);
        if (notificationId != -1) {
            notificationManager.cancel(notificationId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            final boolean isDenied =
                    ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_DENIED;
            Log.d(TAG, "isDenied: " + isDenied);
            if (isDenied) {
                requestPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    /**
     * アプリで使用するNotificationChannelを一括登録する.
     */
    public void createNotificationChannels() {
        for (NotificationChannelId notificationChannelId : NotificationChannelId.values()) {
            // TODO: 2024/01/04 @Importanceアノテーションの影響で、
            //  enumでNotificationManager.IMPORTANCE_XXXの定数を持つとワーニングが出るので、ここでswitch判定する
            final int importance;
            switch (notificationChannelId) {
                case MIN_PRIORITY:
                    importance = NotificationManager.IMPORTANCE_MIN;
                    break;
                case LOW_PRIORITY:
                    importance = NotificationManager.IMPORTANCE_LOW;
                    break;
                case DEFAULT_PRIORITY:
                    importance = NotificationManager.IMPORTANCE_DEFAULT;
                    break;
                case HIGH_PRIORITY:
                    importance = NotificationManager.IMPORTANCE_HIGH;
                    break;
                default:
                    throw new IllegalArgumentException("unknown enum value: " + notificationChannelId);
            }
            final NotificationChannel notificationChannel = new NotificationChannel(
                    notificationChannelId.name(),
                    notificationChannelId.getUserVisibleName(),
                    importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}