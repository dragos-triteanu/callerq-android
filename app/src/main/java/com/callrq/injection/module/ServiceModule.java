package com.callrq.injection.module;

import com.callrq.services.ScheduleService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class ServiceModule {

    @Provides
    @Singleton
    ScheduleService provideScheduleService() {
        return new ScheduleService();
    }

}
