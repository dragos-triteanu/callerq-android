package com.callerq.injection.component;

import com.callerq.activities.CallerqActivity;
import com.callerq.activities.MainActivity;
import com.callerq.activities.StartActivity;
import com.callerq.injection.module.ApplicationModule;
import com.callerq.injection.module.ServiceModule;
import com.callerq.receivers.CallBroadcastReceiver;
import com.callerq.services.NotificationActionService;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ApplicationModule.class, ServiceModule.class})
public interface ApplicationComponent {
    void inject(CallerqActivity callerqActivity);
    void inject(StartActivity startActivity);
    void inject(MainActivity mainActivity);
    void inject(CallBroadcastReceiver broadcastReceiver);
    void inject(NotificationActionService notificationActionService);
}
