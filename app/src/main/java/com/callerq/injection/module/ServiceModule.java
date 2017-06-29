package com.callerq.injection.module;

import com.callerq.services.CalendarService;
import com.callerq.services.ScheduleService;
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

    @Provides
    @Singleton
    CalendarService provideCalendarService() {
        return new CalendarService();
    }


}
