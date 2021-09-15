package io.inway.ringtone.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FlutterRingtonePlayerPlugin
 */
public class FlutterRingtonePlayerPlugin implements FlutterPlugin,MethodCallHandler {
    private Context context;
    private MethodChannel channel;
    private RingtoneManager ringtoneManager;
    private Ringtone ringtone;

//    public FlutterRingtonePlayerPlugin(Context context) {
//        this.context = context;
//        this.ringtoneManager = new RingtoneManager(context);
//        this.ringtoneManager.setStopPreviousRingtone(true);
//    }

    /**
     * Plugin registration.
     */
    @SuppressWarnings("deprecation")
    public static void registerWith(io.flutter.plugin.common.PluginRegistry.Registrar registrar) {
        FlutterRingtonePlayerPlugin instance = new FlutterRingtonePlayerPlugin();
        instance.channel = new MethodChannel(registrar.messenger(), "flutter_ringtone_player");
        instance.context = registrar.context();
        instance.ringtoneManager = new RingtoneManager(instance.context);
        instance.ringtoneManager.setStopPreviousRingtone(true);
        instance.channel.setMethodCallHandler(instance);
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
      channel = new MethodChannel(binding.getBinaryMessenger(), "flutter_ringtone_player");
      context = binding.getApplicationContext();
        ringtoneManager = new RingtoneManager(context);
        ringtoneManager.setStopPreviousRingtone(true);
      channel.setMethodCallHandler(this);
    }
  
    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
      channel.setMethodCallHandler(null);
      channel = null;
    }

    @Override
    public void onMethodCall(final MethodCall call,final Result result) {
        try {
            Uri ringtoneUri = null;

            if (call.method.equals("play") && !call.hasArgument("android")) {
                result.notImplemented();
            } else if (call.method.equals("play")) {
                final int kind = call.argument("android");

                switch (kind) {
                    case 1:
                        ringtoneUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                        break;
                    case 2:
                        ringtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                        break;
                    case 3:
                        ringtoneUri = Settings.System.DEFAULT_RINGTONE_URI;
                        break;
                    default:
                        result.notImplemented();
                }
            } else if (call.method.equals("stop")) {
                if (ringtone != null) {
                    ringtone.stop();
                }

                result.success(null);
            }

            if (ringtoneUri != null) {
                if (ringtone != null) {
                    ringtone.stop();
                }
                ringtone = ringtoneManager.getRingtone(context, ringtoneUri);

                if (call.hasArgument("volume")) {
                    final double volume = call.argument("volume");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ringtone.setVolume((float) volume);
                    }
                }

                if (call.hasArgument("looping")) {
                    final boolean looping = call.argument("looping");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ringtone.setLooping(looping);
                    }
                }

                if (call.hasArgument("asAlarm")) {
                    final boolean asAlarm = call.argument("asAlarm");
                    /* There's also a .setAudioAttributes method
                       that is more flexible, but .setStreamType
                       is supported in all Android versions
                       whereas .setAudioAttributes needs SDK > 21.
                       More on that at
                       https://developer.android.com/reference/android/media/Ringtone
                    */
                    if (asAlarm) {
                        ringtone.setStreamType(AudioManager.STREAM_ALARM);
                    }
                }

                ringtone.play();

                result.success(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.error("Exception", e.getMessage(), null);
        }
    }
}
