package com.season.plugin.hookcore.handle;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.net.Uri.Builder;
import android.text.TextUtils;


import com.season.plugin.hookcore.Env;

import java.lang.reflect.Method;


/**
 * Disc: hook ContentProvider
 * hook点
 * @see HookHandleContentProvider
 *
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public class HookHandleContentProvider extends BaseHookHandle {
    private final ProviderInfo mStubProvider;
    private final ProviderInfo mTargetProvider;
    private final boolean mLocalProvider;

    public HookHandleContentProvider(Context hostContext, ProviderInfo stubProvider, ProviderInfo targetProvider, boolean localProvider) {
        super(hostContext);
        mStubProvider = stubProvider;
        mTargetProvider = targetProvider;
        mLocalProvider = localProvider;
    }


    @Override
    protected void init() {
        sHookedMethodHandlers.put("query", new query(mHostContext));
        sHookedMethodHandlers.put("getType", new getType(mHostContext));
        sHookedMethodHandlers.put("insert", new insert(mHostContext));
        sHookedMethodHandlers.put("bulkInsert", new bulkInsert(mHostContext));
        sHookedMethodHandlers.put("delete", new delete(mHostContext));
        sHookedMethodHandlers.put("update", new update(mHostContext));
        sHookedMethodHandlers.put("openFile", new openFile(mHostContext));
        sHookedMethodHandlers.put("openAssetFile", new openAssetFile(mHostContext));
        sHookedMethodHandlers.put("applyBatch", new applyBatch(mHostContext));
        sHookedMethodHandlers.put("call", new call(mHostContext));
        sHookedMethodHandlers.put("createCancellationSignal", new createCancellationSignal(mHostContext));
        sHookedMethodHandlers.put("canonicalize", new canonicalize(mHostContext));
        sHookedMethodHandlers.put("uncanonicalize", new uncanonicalize(mHostContext));
        sHookedMethodHandlers.put("getStreamTypes", new getStreamTypes(mHostContext));
        sHookedMethodHandlers.put("openTypedAssetFile", new openTypedAssetFile(mHostContext));
    }

    private class MyHandler extends BaseHookMethodHandlerOfReplaceCallingPackage {

        public MyHandler(Context hostContext) {
            super(hostContext);
        }

        private int indexFirstUri(Object[] args) {
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Uri) {
                        return i;
                    }
                }
            }
            return -1;
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
//            if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
//                final int index = 0;
//                if (args != null && args.length > index && args[index] instanceof String) {
//                    String pkg = (String) args[index];
//                    if (!TextUtils.equals(pkg, mHostContext.getPackageName())) {
//                        args[index] = mHostContext.getPackageName();
//                    }
//                }
//            }
            if (!mLocalProvider && mStubProvider != null) {
                final int index = indexFirstUri(args);
                if (index >= 0) {
                    Uri uri = (Uri) args[index];
                    String authority = uri.getAuthority();
                    if (!TextUtils.equals(authority, mStubProvider.authority)) {
                        Builder b = new Builder();
                        b.scheme(uri.getScheme());
                        b.authority(mStubProvider.authority);
                        b.path(uri.getPath());
                        b.query(uri.getQuery());
                        b.appendQueryParameter(Env.EXTRA_TARGET_AUTHORITY, authority);
                        b.fragment(uri.getFragment());
                        args[index] = b.build();
                    }
                }
            }

            return super.beforeInvoke(receiver, method, args);
        }
    }

    private class query extends MyHandler {
        public query(Context context) {
            super(context);
        }
    }

    private class getType extends MyHandler {
        public getType(Context context) {
            super(context);
        }
    }

    private class insert extends MyHandler {
        public insert(Context context) {
            super(context);
        }
    }

    private class bulkInsert extends MyHandler {
        public bulkInsert(Context context) {
            super(context);
        }
    }

    private class delete extends MyHandler {
        public delete(Context context) {
            super(context);
        }
    }

    private class update extends MyHandler {
        public update(Context context) {
            super(context);
        }
    }

    private class openFile extends MyHandler {
        public openFile(Context context) {
            super(context);
        }
    }

    private class openAssetFile extends MyHandler {
        public openAssetFile(Context context) {
            super(context);
        }
    }

    private class applyBatch extends MyHandler {
        public applyBatch(Context context) {
            super(context);
        }
    }

    private class call extends MyHandler {
        public call(Context context) {
            super(context);
        }
    }

    private class createCancellationSignal extends MyHandler {
        public createCancellationSignal(Context context) {
            super(context);
        }
    }

    private class canonicalize extends MyHandler {
        public canonicalize(Context context) {
            super(context);
        }
    }

    private class uncanonicalize extends MyHandler {
        public uncanonicalize(Context context) {
            super(context);
        }
    }

    private class getStreamTypes extends MyHandler {
        public getStreamTypes(Context context) {
            super(context);
        }
    }

    private class openTypedAssetFile extends MyHandler {
        public openTypedAssetFile(Context context) {
            super(context);
        }
    }
}
