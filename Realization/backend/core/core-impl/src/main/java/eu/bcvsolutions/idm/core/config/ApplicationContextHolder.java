package eu.bcvsolutions.idm.core.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHolder implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    private static ApplicationContext applicationContext;
    private static volatile boolean refreshed;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        refreshed = true;
    }

    public static boolean hasApplicationContext() {
        return (refreshed && applicationContext != null);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static boolean isRefreshed() {
        return refreshed;
    }
}

