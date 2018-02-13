package com.callrq.injection.component;

import com.callrq.activities.CallerqActivity;
import com.callrq.activities.MainActivity;
import com.callrq.activities.IntroActivity;
import com.callrq.injection.module.ApplicationModule;
import com.callrq.injection.module.ServiceModule;
import com.callrq.receivers.CallBroadcastReceiver;
import com.callrq.services.NotificationActionService;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ApplicationModule.class, ServiceModule.class})
public interface ApplicationComponent {
    void inject(CallerqActivity callerqActivity);

    void inject(IntroActivity introActivity);

    void inject(MainActivity mainActivity);

    void inject(CallBroadcastReceiver broadcastReceiver);

    void inject(NotificationActionService notificationActionService);
}
