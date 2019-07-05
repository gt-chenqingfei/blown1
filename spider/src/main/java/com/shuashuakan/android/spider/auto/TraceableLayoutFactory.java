package com.shuashuakan.android.spider.auto;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatDelegate;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.shuashuakan.android.spider.Spider;
import com.shuashuakan.android.spider.Utils;
import com.shuashuakan.android.spider.auto.ViewMonitor.ViewMonitorFactory;
import com.shuashuakan.android.spider.auto.widget.*;
import timber.log.Timber;

import java.lang.reflect.Constructor;
import java.util.Map;

import static com.shuashuakan.android.spider.Utils.checkNotNull;

/**
 * Created by twocity on 12/26/16.
 */

public class TraceableLayoutFactory implements LayoutInflater.Factory2 {
  private static final Map<String, Constructor<? extends View>> CONSTRUCTOR_MAP = new ArrayMap<>();
  private static final Class<?>[] CONSTRUCTOR_SIGNATURE = new Class[] {
      Context.class, AttributeSet.class
  };
  private final ViewProxyFactory viewProxyFactory;
  private final ViewMonitorFactory viewMonitorFactory;
  private final AppCompatDelegate appCompatDelegate;
  private final Object[] constructorArgs = new Object[2];

  private TraceableLayoutFactory(AppCompatDelegate appCompatDelegate, ViewProxyFactory factory,
                                 ViewMonitorFactory viewMonitorFactory) {
    this.appCompatDelegate = appCompatDelegate;
    this.viewMonitorFactory = viewMonitorFactory;
    this.viewProxyFactory = factory;
  }

  public static LayoutInflater.Factory2 create(@NonNull AppCompatDelegate delegate,
      ViewProxyFactory viewProxyFactory, ViewMonitorFactory viewMonitorFactory) {
    return new TraceableLayoutFactory(checkNotNull(delegate), checkNotNull(viewProxyFactory),
        checkNotNull(viewMonitorFactory));
  }

  @Override
  public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
    View view = inflateTraceableView(name, context, attrs);

    /*
      首先加载自动生成的 view（如果有的话
     */
    if (view == null) {
      view = inflateGeneratedView(name, context, attrs);
    }
    /*
     *  保证自定义的 view 会被 TraceableLayoutFactory 加载
     *  如果 view 是 TraceableView 的话，我们可以吧 ViewProxy 加载进去
     */
    if (view == null && name.startsWith("com.shuashuakan.android")) {
      view = inflateHighgardenView(name, context, attrs);
    }
    /*
    尝试使用 AppCompat 内置的 inflater 来加载 view
     */
    if (view == null) {
      view = appCompatDelegate.createView(parent, name, context, attrs);
    }

    /*
    设置我们定义的 ViewProxy
     */
    if (view != null && view instanceof TraceableView) {
      ((TraceableView) view).installViewProxy(viewProxyFactory.createViewProxy());
    }
    return view;
  }

  private View inflateHighgardenView(String name, Context context, AttributeSet attrs) {
    Constructor<? extends View> constructor = CONSTRUCTOR_MAP.get(name);
    try {
      if (constructor == null) {
        Class<? extends View> clazz =
            context.getClassLoader().loadClass(name).asSubclass(View.class);
        constructor = clazz.getConstructor(CONSTRUCTOR_SIGNATURE);
        constructor.setAccessible(true);
        CONSTRUCTOR_MAP.put(name, constructor);
      }
      constructorArgs[0] = context;
      constructorArgs[1] = attrs;
      return constructor.newInstance(constructorArgs);
    } catch (Exception e) {
      Timber.e(e, "Can't inflate view: %s", name);
    }
    return null;
  }

  private View inflateGeneratedView(String name, Context context, AttributeSet attrs) {
    Constructor<? extends View> constructor = CONSTRUCTOR_MAP.get(name);
    try {
      if (constructor == null) {
        Class<? extends View> clazz = ViewClassUtil.findProxyViewClass(context, name);
        if (clazz == null) {
          return null;
        }
        Timber.d("%s loaded as: %s", name, clazz.getCanonicalName());
        constructor = clazz.getConstructor(CONSTRUCTOR_SIGNATURE);
        constructor.setAccessible(true);
        CONSTRUCTOR_MAP.put(name, constructor);
      }
      constructorArgs[0] = context;
      constructorArgs[1] = attrs;
      return constructor.newInstance(constructorArgs);
    } catch (Exception e) {
      Timber.e(e, "Can't inflate view: %s", name);
    }
    return null;
  }

  private View inflateTraceableView(String name, Context context, AttributeSet attrs) {
    View view;
    switch (name) {
      case "TextView":
        view = new TraceableTextView(context, attrs);
        break;
      case "Button":
        view = new TraceableButton(context, attrs);
        break;
      case "ImageButton":
        view = new TraceableImageButton(context, attrs);
        break;
      case "ImageView":
        view = new TraceableImageView(context, attrs);
        break;
      case "CheckedTextView":
        view = new TraceableCheckedTextView(context, attrs);
        break;
      case "FrameLayout":
        view = new TraceableFrameLayout(context, attrs);
        break;
      case "LinearLayout":
        view = new TraceableLinearLayout(context, attrs);
        break;
      case "RelativeLayout":
        view = new TraceableRelativeLayout(context, attrs);
        break;
      case "android.support.design.widget.TabLayout":
        TraceableTabLayout tabLayout = new TraceableTabLayout(context, attrs);
        tabLayout.installMonitor(viewMonitorFactory.createViewMonitor());
        view = tabLayout;
        break;
      case "android.support.v7.widget.Toolbar":
        view = new TraceableToolbar(context, attrs, viewMonitorFactory.createViewMonitor());
        break;
      case "android.support.constraint.ConstraintLayout":
        view = new TraceableConstraintLayout(context, attrs);
        break;
      case "com.facebook.drawee.view.SimpleDraweeView":
        view = new TraceableSimpleDraweeView(context, attrs);
        break;
      default:
        view = null;
    }
    return view;
  }

  @Override public View onCreateView(String name, Context context, AttributeSet attrs) {
    return null;
  }
}
