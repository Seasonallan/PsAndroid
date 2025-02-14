package com.season.plugin.hookcore;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.AndroidRuntimeException;

import com.season.lib.support.reflect.MethodUtils;
import com.season.lib.util.LogUtil;
import com.season.plugin.compat.ActivityManagerCompat;
import com.season.plugin.compat.ActivityManagerNativeCompat;
import com.season.plugin.hookcore.handle.HookHandleActivityManager;
import com.season.plugin.hookcore.handle.BaseHookHandle;
import com.season.lib.support.reflect.FieldUtils;
import com.season.lib.support.reflect.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

/**
 * Disc: hook ActivityManager拦截startActivity类似请求，替换intent
 * 绑定的代理
 * @see HookHandleActivityManager
 *  2020-03-17 10:07 更新：新增对23以上SDK的支持，26，27，28的ams获取方式是通过ActivityManager.getService()
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public class ProxyHookActivityManager extends BaseHookProxy {

    private static final String TAG = ProxyHookActivityManager.class.getSimpleName();

    public ProxyHookActivityManager(Context hostContext) {
        super(hostContext);
    }

    @Override
    public BaseHookHandle createHookHandle() {
        return new HookHandleActivityManager(mHostContext);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return super.invoke(proxy, method, args);
        } catch (SecurityException e) {
            String msg = String.format("msg[%s],args[%s]", e.getMessage(), Arrays.toString(args));
            SecurityException e1 = new SecurityException(msg);
            e1.initCause(e);
            throw e1;
        }
    }

    @Override
    public void onInstall() throws Throwable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//26，27，28的ams获取方式是通过ActivityManager.getService()
            onInstall28();
        } else {//25往下，是ActivityManagerNative.getDefault()
            onInstall25();
        }
    }

    public void onInstall28() throws Throwable {
        Class cls = ActivityManagerCompat.Class();
        Object obj = FieldUtils.readStaticField(cls, "IActivityManagerSingleton");
        if (obj == null) {
            ActivityManagerCompat.getDefault();
            obj = FieldUtils.readStaticField(cls, "IActivityManagerSingleton");
        }

        if (isIActivityManager(obj)) {
            setOldObj(obj);
            Class<?> objClass = mOldObj.getClass();
            List<Class<?>> interfaces = Utils.getAllInterfaces(objClass);
            Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
            Object proxyActivityManager = Proxy.newProxyInstance(objClass.getClassLoader(), ifs,
                    this);
            FieldUtils.writeStaticField(cls, "IActivityManagerSingleton", proxyActivityManager);
            LogUtil.i(TAG, "-->28 Install ActivityManager BaseHook 1 old=%s,new=%s", mOldObj, proxyActivityManager);
        } else if (isSingleton(obj)) {

            Object obj1 = FieldUtils.readField(obj, "mInstance");
            if (obj1 == null) {
                MethodUtils.invokeMethod(obj, "get");
                obj1 = FieldUtils.readField(obj, "mInstance");
            }
            setOldObj(obj1);
            List<Class<?>> interfaces = Utils.getAllInterfaces(mOldObj.getClass());
            Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
            final Object object = Proxy.newProxyInstance(mOldObj.getClass().getClassLoader(), ifs,
                    this);

            //这里先写一次，防止后面找不到Singleton类导致的挂钩子失败的问题。
            FieldUtils.writeField(obj, "mInstance", object);

            LogUtil.i(TAG, "28 Install ActivityManager 2 BaseHook  old=%s,new=%s", mOldObj, object);
            //这里使用方式1，如果成功的话，会导致上面的写操作被覆盖。
            FieldUtils.writeStaticField(cls, "IActivityManagerSingleton", new android.util.Singleton<Object>() {
                @Override
                protected Object create() {
                    LogUtil.i(TAG, "28 Install ActivityManager 3 BaseHook  old=%s,new=%s", mOldObj, object);
                    return object;
                }
            });

        } else {
            throw new AndroidRuntimeException("Can not install IActivityManagerNative hook");
        }
    }

    public void onInstall25() throws Throwable {
        Class cls = ActivityManagerNativeCompat.Class();
        Object obj = FieldUtils.readStaticField(cls, "gDefault");
        if (obj == null) {
            ActivityManagerNativeCompat.getDefault();
            obj = FieldUtils.readStaticField(cls, "gDefault");
        }

        if (isIActivityManager(obj)) {
            setOldObj(obj);
            Class<?> objClass = mOldObj.getClass();
            List<Class<?>> interfaces = Utils.getAllInterfaces(objClass);
            Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
            Object proxyActivityManager = Proxy.newProxyInstance(objClass.getClassLoader(), ifs,
                    this);
            FieldUtils.writeStaticField(cls, "gDefault", proxyActivityManager);
            LogUtil.i(TAG, "Install ActivityManager BaseHook 1 old=%s,new=%s", mOldObj, proxyActivityManager);
        } else if (isSingleton(obj)) {
            Object obj1 = FieldUtils.readField(obj, "mInstance");
            if (obj1 == null) {
                MethodUtils.invokeMethod(obj, "get");
                obj1 = FieldUtils.readField(obj, "mInstance");
            }
            setOldObj(obj1);
            List<Class<?>> interfaces = Utils.getAllInterfaces(mOldObj.getClass());
            Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
            final Object object = Proxy.newProxyInstance(mOldObj.getClass().getClassLoader(), ifs,
                    this);
            Object iam1 = ActivityManagerNativeCompat.getDefault();

            //这里先写一次，防止后面找不到Singleton类导致的挂钩子失败的问题。
            FieldUtils.writeField(obj, "mInstance", object);

            //这里使用方式1，如果成功的话，会导致上面的写操作被覆盖。
            FieldUtils.writeStaticField(cls, "gDefault", new android.util.Singleton<Object>() {
                @Override
                protected Object create() {
                    LogUtil.i(TAG, "Install ActivityManager 3 BaseHook  old=%s,new=%s", mOldObj, object);
                    return object;
                }
            });

            LogUtil.i(TAG, "Install ActivityManager BaseHook 2 old=%s,new=%s", mOldObj.toString(), object);
            Object iam2 = ActivityManagerNativeCompat.getDefault();
            // 方式2
            if (iam1 == iam2) {
                //这段代码是废的，没啥用，写这里只是不想改而已。
                FieldUtils.writeField(obj, "mInstance", object);
            }
        } else {
            throw new AndroidRuntimeException("Can not install IActivityManagerNative hook");
        }
    }


    private boolean isSingleton(Object obj) {
        if (obj == null) {
            return false;
        } else {
            try {
                return Class.forName("android.util.Singleton").isInstance(obj);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

    private boolean isIActivityManager(Object obj){
        if (obj == null) {
            return false;
        } else {
            try {
                return Class.forName("android.app.IActivityManager").isInstance(obj);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

}
