package com.shuashuakan.android;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import dagger.android.AndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasContentProviderInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
import java.util.Locale;
import timber.log.Timber;

public final class FishInjection {
  private static final String TAG = "dagger.android";

  /**
   * Injects {@code activity} if an associated {@link AndroidInjector} implementation can be found,
   * otherwise throws an {@link IllegalArgumentException}.
   *
   * @throws RuntimeException if the {@link Application} doesn't implement {@link
   * HasActivityInjector}.
   */
  public static void inject(Activity activity) {
    checkNotNull(activity, "activity");
    Application application = activity.getApplication();

    AndroidInjector<Activity> activityInjector =
        AndroidInjectorProvider.Companion.activityInjector(application);

    checkNotNull(activityInjector, "%s.activityInjector() returned null", application.getClass());
    activityInjector.inject(activity);
  }

  /**
   * Injects {@code fragment} if an associated {@link AndroidInjector} implementation can be found,
   * otherwise throws an {@link IllegalArgumentException}.
   *
   * <p>Uses the following algorithm to find the appropriate {@code AndroidInjector<Fragment>} to
   * use to inject {@code fragment}:
   *
   * <ol>
   * <li>Walks the parent-fragment hierarchy to find the a fragment that implements {@link
   * HasFragmentInjector}, and if none do
   * <li>Uses the {@code fragment}'s {@link Fragment#getActivity() activity} if it implements
   * {@link HasFragmentInjector}, and if not
   * <li>Uses the {@link Application} if it implements {@link HasFragmentInjector}.
   * </ol>
   *
   * If none of them implement {@link HasFragmentInjector}, a {@link IllegalArgumentException} is
   * thrown.
   *
   * @throws IllegalArgumentException if no parent fragment, activity, or application implements
   * {@link HasFragmentInjector}.
   */
  public static void inject(Fragment fragment) {
    checkNotNull(fragment, "fragment");
    HasFragmentInjector hasFragmentInjector = findHasFragmentInjector(fragment);

    Timber.tag(TAG)
        .d("An injector for %s was found in %s", fragment.getClass().getCanonicalName(),
            hasFragmentInjector.getClass().getCanonicalName());

    AndroidInjector<Fragment> fragmentInjector = hasFragmentInjector.fragmentInjector();
    checkNotNull(fragmentInjector, "%s.fragmentInjector() returned null",
        hasFragmentInjector.getClass());

    fragmentInjector.inject(fragment);
  }

  private static HasFragmentInjector findHasFragmentInjector(Fragment fragment) {
    Fragment parentFragment = fragment;
    while ((parentFragment = parentFragment.getParentFragment()) != null) {
      if (parentFragment instanceof HasFragmentInjector) {
        return (HasFragmentInjector) parentFragment;
      }
    }
    Activity activity = fragment.getActivity();
    if (activity instanceof HasFragmentInjector) {
      return (HasFragmentInjector) activity;
    }
    if (activity.getApplication() instanceof HasFragmentInjector) {
      return (HasFragmentInjector) activity.getApplication();
    }

    throw new IllegalArgumentException(
        String.format("No injector was found for %s", fragment.getClass().getCanonicalName()));
  }

  public static void inject(android.support.v4.app.Fragment fragment) {
    checkNotNull(fragment, "fragment");
    HasSupportFragmentInjector hasFragmentInjector = findHasFragmentInjector(fragment);

    Timber.tag(TAG)
        .d("An injector for %s was found in %s", fragment.getClass().getCanonicalName(),
            hasFragmentInjector.getClass().getCanonicalName());

    AndroidInjector<android.support.v4.app.Fragment> fragmentInjector =
        hasFragmentInjector.supportFragmentInjector();
    checkNotNull(fragmentInjector, "%s.fragmentInjector() returned null",
        hasFragmentInjector.getClass());

    fragmentInjector.inject(fragment);
  }

  private static HasSupportFragmentInjector findHasFragmentInjector(
      android.support.v4.app.Fragment fragment) {
    android.support.v4.app.Fragment parentFragment = fragment;
    while ((parentFragment = parentFragment.getParentFragment()) != null) {
      if (parentFragment instanceof HasSupportFragmentInjector) {
        return (HasSupportFragmentInjector) parentFragment;
      }
    }
    Activity activity = fragment.getActivity();
    if (activity instanceof HasSupportFragmentInjector) {
      return (HasSupportFragmentInjector) activity;
    }

    return new HasSupportFragmentInjector() {
      @Override public AndroidInjector<android.support.v4.app.Fragment> supportFragmentInjector() {
        return AndroidInjectorProvider.Companion.supportFragmentInjector(activity.getApplication());
      }
    };
  }

  /**
   * Injects {@code service} if an associated {@link AndroidInjector} implementation can be found,
   * otherwise throws an {@link IllegalArgumentException}.
   *
   * @throws RuntimeException if the {@link Application} doesn't implement {@link
   * HasServiceInjector}.
   */
  public static void inject(Service service) {
    checkNotNull(service, "service");
    Application application = service.getApplication();
    AndroidInjector<Service> serviceInjector =
        AndroidInjectorProvider.Companion.serviceInjector(application);
    checkNotNull(serviceInjector, "%s.serviceInjector() returned null", application.getClass());

    serviceInjector.inject(service);
  }

  /**
   * Injects {@code broadcastReceiver} if an associated {@link AndroidInjector} implementation can
   * be found, otherwise throws an {@link IllegalArgumentException}.
   *
   * @throws RuntimeException if the {@link Application} from {@link
   * Context#getApplicationContext()} doesn't implement {@link HasBroadcastReceiverInjector}.
   */
  public static void inject(BroadcastReceiver broadcastReceiver, Context context) {
    checkNotNull(broadcastReceiver, "broadcastReceiver");
    checkNotNull(context, "context");
    Application application = (Application) context.getApplicationContext();
    AndroidInjector<BroadcastReceiver> broadcastReceiverInjector =
        AndroidInjectorProvider.Companion.broadcastInjector(application);
    checkNotNull(broadcastReceiverInjector, "%s.broadcastReceiverInjector() returned null",
        application.getClass());

    broadcastReceiverInjector.inject(broadcastReceiver);
  }

  /**
   * Injects {@code contentProvider} if an associated {@link AndroidInjector} implementation can be
   * found, otherwise throws an {@link IllegalArgumentException}.
   *
   * @throws RuntimeException if the {@link Application} doesn't implement {@link
   * HasContentProviderInjector}.
   */
  public static void inject(ContentProvider contentProvider) {
    throw new IllegalStateException("Not implementation");
    //checkNotNull(contentProvider, "contentProvider");
    //Application application = (Application) contentProvider.getContext().getApplicationContext();
    //if (!(application instanceof HasContentProviderInjector)) {
    //  throw new RuntimeException(
    //      String.format("%s does not implement %s", application.getClass().getCanonicalName(),
    //          HasContentProviderInjector.class.getCanonicalName()));
    //}
    //
    //AndroidInjector<ContentProvider> contentProviderInjector =
    //    ((HasContentProviderInjector) application).contentProviderInjector();
    //checkNotNull(contentProviderInjector, "%s.contentProviderInjector() returned null",
    //    application.getClass());
    //
    //contentProviderInjector.inject(contentProvider);
  }

  private static void checkNotNull(Object object, String message, Object... args) {
    checkNotNull(object, String.format(Locale.US, message, args));
  }

  private static void checkNotNull(Object object, String message) {
    if (object == null) {
      throw new NullPointerException(message);
    }
  }

  private FishInjection() {
  }
}
