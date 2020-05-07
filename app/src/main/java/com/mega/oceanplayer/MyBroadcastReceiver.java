package com.mega.oceanplayer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;
import java.util.Objects;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = MyBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG, intent.getAction());

        MusicController controller = MusicController.getInstance();

        switch(Objects.requireNonNull(intent.getAction())) {
            case Intent.ACTION_SCREEN_ON:
                LogUtil.d(TAG, "Sceen is On");
                break;
            case Intent.ACTION_SCREEN_OFF:
                LogUtil.d(TAG, "Sceen is Off");
                break;
            case "com.mega.oceanplayer.MusicController.playPrevious":
                controller.playPrevious();
                break;
            case "com.mega.oceanplayer.MusicController.playPause":
                if(controller.isPlaying()) {
                    controller.pausePlay();
                }
                else {
                    controller.continuePlay();
                }
                break;
            case "com.mega.oceanplayer.MusicController.playNext":
                controller.playNext();
                break;
            case "com.mega.oceanplayer.MusicController.PlayingActivity":
                bringAppToFront(context);
                Activity topActivity = MyApplication.topActivity;
                Intent intent1 = new Intent(topActivity, PlayingActivity.class);
                if(topActivity.getClass() == PlayingActivity.class) {
                    LogUtil.d(TAG, "PlayingActivity is on the top");
                }
                topActivity.startActivity(intent1, ActivityOptions.makeSceneTransitionAnimation(topActivity).toBundle());
                break;
        }
    }

    public void bringAppToFront(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        assert activityManager != null;
        List<ActivityManager.AppTask> taskList = activityManager.getAppTasks();
        for (ActivityManager.AppTask task : taskList) {
            ActivityManager.RecentTaskInfo taskInfo = task.getTaskInfo();
            assert taskInfo.topActivity != null;
            if (taskInfo.topActivity.getPackageName().equals(context.getPackageName())) {
                activityManager.moveTaskToFront(taskInfo.id, 0);
                LogUtil.d(TAG, "find task: " + taskInfo.toString());
                break;
            }
        }
    }
}
