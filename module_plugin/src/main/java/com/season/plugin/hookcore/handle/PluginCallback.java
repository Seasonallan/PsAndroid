package com.season.plugin.hookcore.handle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.season.lib.util.LogUtil;
import com.season.plugin.hookcore.Env;
import com.season.plugin.core.PluginManager;
import com.season.plugin.core.PluginProcessManager;
import com.season.plugin.hookcore.ProxyHookPackageManager;
import com.season.plugin.stub.ShortcutProxyActivity;
import com.season.lib.support.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Disc: 处理假的activity启动后替换为原本的activity并加载application和ClassLoader的替换
 * Hook点：
 * @see com.season.plugin.hookcore.HookHandlerCallback
 *
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:29
 */
public class PluginCallback implements Handler.Callback {

    private static final String TAG = "PluginCallback";

    public static final int LAUNCH_ACTIVITY = 100;
    public static final int PAUSE_ACTIVITY = 101;
    public static final int PAUSE_ACTIVITY_FINISHING = 102;
    public static final int STOP_ACTIVITY_SHOW = 103;
    public static final int STOP_ACTIVITY_HIDE = 104;
    public static final int SHOW_WINDOW = 105;
    public static final int HIDE_WINDOW = 106;
    public static final int RESUME_ACTIVITY = 107;
    public static final int SEND_RESULT = 108;
    public static final int DESTROY_ACTIVITY = 109;
    public static final int BIND_APPLICATION = 110;
    public static final int EXIT_APPLICATION = 111;
    public static final int NEW_INTENT = 112;
    public static final int RECEIVER = 113;
    public static final int CREATE_SERVICE = 114;
    public static final int SERVICE_ARGS = 115;
    public static final int STOP_SERVICE = 116;
    public static final int REQUEST_THUMBNAIL = 117;
    public static final int CONFIGURATION_CHANGED = 118;
    public static final int CLEAN_UP_CONTEXT = 119;
    public static final int GC_WHEN_IDLE = 120;
    public static final int BIND_SERVICE = 121;
    public static final int UNBIND_SERVICE = 122;
    public static final int DUMP_SERVICE = 123;
    public static final int LOW_MEMORY = 124;
    public static final int ACTIVITY_CONFIGURATION_CHANGED = 125;
    public static final int RELAUNCH_ACTIVITY = 126;
    public static final int PROFILER_CONTROL = 127;
    public static final int CREATE_BACKUP_AGENT = 128;
    public static final int DESTROY_BACKUP_AGENT = 129;
    public static final int SUICIDE = 130;
    public static final int REMOVE_PROVIDER = 131;
    public static final int ENABLE_JIT = 132;
    public static final int DISPATCH_PACKAGE_BROADCAST = 133;
    public static final int SCHEDULE_CRASH = 134;
    public static final int DUMP_HEAP = 135;
    public static final int DUMP_ACTIVITY = 136;
    public static final int SLEEPING = 137;
    public static final int SET_CORE_SETTINGS = 138;
    public static final int UPDATE_PACKAGE_COMPATIBILITY_INFO = 139;
    public static final int TRIM_MEMORY = 140;
    public static final int DUMP_PROVIDER = 141;
    public static final int UNSTABLE_PROVIDER_DIED = 142;
    public static final int REQUEST_ASSIST_CONTEXT_EXTRAS = 143;
    public static final int TRANSLUCENT_CONVERSION_COMPLETE = 144;
    public static final int INSTALL_PROVIDER = 145;
    public static final int ON_NEW_ACTIVITY_OPTIONS = 146;
    public static final int CANCEL_VISIBLE_BEHIND = 147;
    public static final int BACKGROUND_VISIBLE_BEHIND_CHANGED = 148;
    public static final int ENTER_ANIMATION_COMPLETE = 149;

    public static final int EXECUTE_TRANSACTION = 159;

    String codeToString(int code) {
        switch (code) {
            case LAUNCH_ACTIVITY:
                return "LAUNCH_ACTIVITY";
            case PAUSE_ACTIVITY:
                return "PAUSE_ACTIVITY";
            case PAUSE_ACTIVITY_FINISHING:
                return "PAUSE_ACTIVITY_FINISHING";
            case STOP_ACTIVITY_SHOW:
                return "STOP_ACTIVITY_SHOW";
            case STOP_ACTIVITY_HIDE:
                return "STOP_ACTIVITY_HIDE";
            case SHOW_WINDOW:
                return "SHOW_WINDOW";
            case HIDE_WINDOW:
                return "HIDE_WINDOW";
            case RESUME_ACTIVITY:
                return "RESUME_ACTIVITY";
            case SEND_RESULT:
                return "SEND_RESULT";
            case DESTROY_ACTIVITY:
                return "DESTROY_ACTIVITY";
            case BIND_APPLICATION:
                return "BIND_APPLICATION";
            case EXIT_APPLICATION:
                return "EXIT_APPLICATION";
            case NEW_INTENT:
                return "NEW_INTENT";
            case RECEIVER:
                return "RECEIVER";
            case CREATE_SERVICE:
                return "CREATE_SERVICE";
            case SERVICE_ARGS:
                return "SERVICE_ARGS";
            case STOP_SERVICE:
                return "STOP_SERVICE";
            case CONFIGURATION_CHANGED:
                return "CONFIGURATION_CHANGED";
            case CLEAN_UP_CONTEXT:
                return "CLEAN_UP_CONTEXT";
            case GC_WHEN_IDLE:
                return "GC_WHEN_IDLE";
            case BIND_SERVICE:
                return "BIND_SERVICE";
            case UNBIND_SERVICE:
                return "UNBIND_SERVICE";
            case DUMP_SERVICE:
                return "DUMP_SERVICE";
            case LOW_MEMORY:
                return "LOW_MEMORY";
            case ACTIVITY_CONFIGURATION_CHANGED:
                return "ACTIVITY_CONFIGURATION_CHANGED";
            case RELAUNCH_ACTIVITY:
                return "RELAUNCH_ACTIVITY";
            case PROFILER_CONTROL:
                return "PROFILER_CONTROL";
            case CREATE_BACKUP_AGENT:
                return "CREATE_BACKUP_AGENT";
            case DESTROY_BACKUP_AGENT:
                return "DESTROY_BACKUP_AGENT";
            case SUICIDE:
                return "SUICIDE";
            case REMOVE_PROVIDER:
                return "REMOVE_PROVIDER";
            case ENABLE_JIT:
                return "ENABLE_JIT";
            case DISPATCH_PACKAGE_BROADCAST:
                return "DISPATCH_PACKAGE_BROADCAST";
            case SCHEDULE_CRASH:
                return "SCHEDULE_CRASH";
            case DUMP_HEAP:
                return "DUMP_HEAP";
            case DUMP_ACTIVITY:
                return "DUMP_ACTIVITY";
            case SLEEPING:
                return "SLEEPING";
            case SET_CORE_SETTINGS:
                return "SET_CORE_SETTINGS";
            case UPDATE_PACKAGE_COMPATIBILITY_INFO:
                return "UPDATE_PACKAGE_COMPATIBILITY_INFO";
            case TRIM_MEMORY:
                return "TRIM_MEMORY";
            case DUMP_PROVIDER:
                return "DUMP_PROVIDER";
            case UNSTABLE_PROVIDER_DIED:
                return "UNSTABLE_PROVIDER_DIED";
            case REQUEST_ASSIST_CONTEXT_EXTRAS:
                return "REQUEST_ASSIST_CONTEXT_EXTRAS";
            case TRANSLUCENT_CONVERSION_COMPLETE:
                return "TRANSLUCENT_CONVERSION_COMPLETE";
            case INSTALL_PROVIDER:
                return "INSTALL_PROVIDER";
            case ON_NEW_ACTIVITY_OPTIONS:
                return "ON_NEW_ACTIVITY_OPTIONS";
            case CANCEL_VISIBLE_BEHIND:
                return "CANCEL_VISIBLE_BEHIND";
            case BACKGROUND_VISIBLE_BEHIND_CHANGED:
                return "BACKGROUND_VISIBLE_BEHIND_CHANGED";
            case ENTER_ANIMATION_COMPLETE:
                return "ENTER_ANIMATION_COMPLETE";
        }
        return Integer.toString(code);
    }


    private Handler mOldHandle = null;
    private Context mHostContext;
    private Handler.Callback mCallback;
    public PluginCallback(Context hostContext, Handler oldHandle, Handler.Callback callback) {
        mOldHandle = oldHandle;
        this.mCallback = callback;
        mHostContext = hostContext;
    }

    @Override
    public boolean handleMessage(Message msg) {
        long b = System.currentTimeMillis();
        try {
            if (PluginProcessManager.isPluginProcess(mHostContext)) {
                if (!PluginManager.getInstance().isConnected()) {
                    //这里必须要这么做。如果当前进程是插件进程，并且，还没有绑定上插件管理服务，我们则把消息延迟一段时间再处理。
                    //这样虽然会降低启动速度，但是可以解决在没绑定服务就启动，会导致的一系列时序问题。
                    LogUtil.i(TAG, "handleMessage not isConnected post and wait,msg=%s", msg);
                    mOldHandle.sendMessageDelayed(Message.obtain(msg), 5);
                    //返回true，告诉下面的handle不要处理了。
                    return true;
                }
            }
            if (msg.what == LAUNCH_ACTIVITY) {
                return handleLaunchActivity(msg);
            }else if (msg.what == EXECUTE_TRANSACTION){
                return handleLaunchActivity9(msg);
            }
            if (mCallback != null)
                return mCallback.handleMessage(msg);
            return false;
        } finally {
            LogUtil.i(TAG, "handleMessage(%s,%s) cost %s ms", msg.what, codeToString(msg.what), (System.currentTimeMillis() - b));

        }
    }

    private boolean handleLaunchActivity9(Message msg) {
        try {
            Object clientTransObj = msg.obj;

            Field mActivityCallbacksField = clientTransObj.getClass().getDeclaredField("mActivityCallbacks");
            mActivityCallbacksField.setAccessible(true);
            List<Object> mActivityCallbacksObj = (List<Object>) mActivityCallbacksField.get(clientTransObj);
            Object launchActivityItem = null;
            for(Object obj:mActivityCallbacksObj){
                LogUtil.e("121", "name: "+obj.getClass().getName());
                if(obj.getClass().getName().contains("android.app.servertransaction.LaunchActivityItem")){
                    launchActivityItem = obj;
                    break;
                }
            }
            if(launchActivityItem == null) {
                return false;
            }
            Field mIntentField = launchActivityItem.getClass().getDeclaredField("mIntent");
            mIntentField.setAccessible(true);
            Intent proxyIntent = (Intent)mIntentField.get(launchActivityItem);

            //设置目标Intent
            Intent targetIntent = proxyIntent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
            LogUtil.e("1211","targetIntent = "+targetIntent+","+clientTransObj);
            //判断是否是之前我们hook时候，启动的代理Activity

            if(targetIntent != null) {
                ProxyHookPackageManager.fixContextPackageManager(mHostContext);
                ComponentName targetComponentName = targetIntent.resolveActivity(mHostContext.getPackageManager());
                ActivityInfo targetActivityInfo = PluginManager.getInstance().getActivityInfo(targetComponentName, 0);

                LogUtil.e("1211","targetComponentName = "+targetComponentName.toString());
                if (targetActivityInfo != null) {

                    if (targetComponentName != null && targetComponentName.getClassName().startsWith(".")) {
                        targetIntent.setClassName(targetComponentName.getPackageName(), targetComponentName.getPackageName() + targetComponentName.getClassName());
                    }

                    ResolveInfo resolveInfo = mHostContext.getPackageManager().resolveActivity(proxyIntent, 0);
                    ActivityInfo stubActivityInfo = resolveInfo != null ? resolveInfo.activityInfo : null;
                    if (stubActivityInfo != null) {
                        PluginManager.getInstance().reportMyProcessName(stubActivityInfo.processName, targetActivityInfo.processName, targetActivityInfo.packageName);
                    }
                    PluginProcessManager.preLoadApk(mHostContext, targetActivityInfo);
                    ClassLoader pluginClassLoader = PluginProcessManager.getPluginClassLoader(targetComponentName.getPackageName());
                    setIntentClassLoader(targetIntent, pluginClassLoader);
                    setIntentClassLoader(proxyIntent, pluginClassLoader);

                    Thread.currentThread().setContextClassLoader(pluginClassLoader);
                    try {
                        targetIntent.putExtra(Env.EXTRA_TARGET_INFO, targetActivityInfo);
                        if (stubActivityInfo != null) {
                            targetIntent.putExtra(Env.EXTRA_STUB_INFO, stubActivityInfo);
                        }
                        mIntentField.set(launchActivityItem, targetIntent);
                    } catch (Exception e) {
                        LogUtil.e(TAG, "putExtra 1 fail", e);
                    }
                    Field mActivityInfoField = launchActivityItem.getClass().getDeclaredField("mInfo");
                    mActivityInfoField.setAccessible(true);
                    mActivityInfoField.set(launchActivityItem, targetActivityInfo);
                   // FieldUtils.writeDeclaredField(msg.obj, "activityInfo", targetActivityInfo);

                    LogUtil.i(TAG, "handleLaunchActivity OK");
                } else {
                    LogUtil.e(TAG, "handleLaunchActivity oldInfo==null");
                }
            }



                //mIntentField.set(launchActivityItem, targetIntent);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mCallback != null)
            return mCallback.handleMessage(msg);
        return false;
    }

    private void setIntentClassLoader(Intent intent, ClassLoader classLoader) {
        try {
            Bundle mExtras = (Bundle) FieldUtils.readField(intent, "mExtras");
            if (mExtras != null) {
                mExtras.setClassLoader(classLoader);
            } else {
                Bundle value = new Bundle();
                value.setClassLoader(classLoader);
                FieldUtils.writeField(intent, "mExtras", value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            intent.setExtrasClassLoader(classLoader);
        }
    }

    private boolean handleLaunchActivity(Message msg) {
        try {
            Object obj = msg.obj;
            Intent stubIntent = (Intent) FieldUtils.readField(obj, "intent");
            LogUtil.i(TAG, "handleLaunchActivity stubIntent=" + stubIntent.toString());
            LogUtil.i(TAG, "handleLaunchActivity msg=" + msg.obj);
            //ActivityInfo activityInfo = (ActivityInfo) FieldUtils.readField(obj, "activityInfo", true);
            stubIntent.setExtrasClassLoader(mHostContext.getClassLoader());
            Intent targetIntent = stubIntent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
            // 这里多加一个isNotShortcutProxyActivity的判断，因为ShortcutProxyActivity的很特殊，启动它的时候，
            // 也会带上一个EXTRA_TARGET_INTENT的数据，就会导致这里误以为是启动插件Activity，所以这里要先做一个判断。
            // 之前ShortcutProxyActivity错误复用了key，但是为了兼容，所以这里就先这么判断吧。
            if (targetIntent != null && !isShortcutProxyActivity(stubIntent)) {
                ProxyHookPackageManager.fixContextPackageManager(mHostContext);
                ComponentName targetComponentName = targetIntent.resolveActivity(mHostContext.getPackageManager());
                ActivityInfo targetActivityInfo = PluginManager.getInstance().getActivityInfo(targetComponentName, 0);
                if (targetActivityInfo != null) {

                    if (targetComponentName != null && targetComponentName.getClassName().startsWith(".")) {
                        targetIntent.setClassName(targetComponentName.getPackageName(), targetComponentName.getPackageName() + targetComponentName.getClassName());
                    }

                    ResolveInfo resolveInfo = mHostContext.getPackageManager().resolveActivity(stubIntent, 0);
                    ActivityInfo stubActivityInfo = resolveInfo != null ? resolveInfo.activityInfo : null;
                    if (stubActivityInfo != null) {
                        PluginManager.getInstance().reportMyProcessName(stubActivityInfo.processName, targetActivityInfo.processName, targetActivityInfo.packageName);
                    }
                    PluginProcessManager.preLoadApk(mHostContext, targetActivityInfo);
                    ClassLoader pluginClassLoader = PluginProcessManager.getPluginClassLoader(targetComponentName.getPackageName());
                    setIntentClassLoader(targetIntent, pluginClassLoader);
                    setIntentClassLoader(stubIntent, pluginClassLoader);
                    try {
                        targetIntent.putExtra(Env.EXTRA_TARGET_INFO, targetActivityInfo);
                        if (stubActivityInfo != null) {
                            targetIntent.putExtra(Env.EXTRA_STUB_INFO, stubActivityInfo);
                        }
                    } catch (Exception e) {
                        LogUtil.e(TAG, "putExtra 1 fail", e);
                    }

                    FieldUtils.writeDeclaredField(msg.obj, "intent", targetIntent);
                    FieldUtils.writeDeclaredField(msg.obj, "activityInfo", targetActivityInfo);

                    LogUtil.i(TAG, "handleLaunchActivity OK");
                } else {
                    LogUtil.e(TAG, "handleLaunchActivity oldInfo==null");
                }
            } else {
                LogUtil.e(TAG, "handleLaunchActivity targetIntent==null");
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "handleLaunchActivity FAIL", e);
        }

        mOldHandle.handleMessage(msg);
        return true;
    }

    private boolean isShortcutProxyActivity(Intent targetIntent) {
        try {
            if (PluginManager.ACTION_SHORTCUT_PROXY.equalsIgnoreCase(targetIntent.getAction())) {
                return true;
            }
            PackageManager pm = mHostContext.getPackageManager();
            ResolveInfo info = pm.resolveActivity(targetIntent, 0);
            if (info != null) {
                String name = info.activityInfo.name;
                if (name != null && name.startsWith(".")) {
                    name = info.activityInfo.packageName + info.activityInfo.name;
                }
                return ShortcutProxyActivity.class.getName().equals(name);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }


}