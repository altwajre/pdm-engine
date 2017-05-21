package cn.betasoft.pdm.engine.config.aspectj;

import cn.betasoft.pdm.engine.monitor.MonitorActorAspect;
import org.aspectj.lang.Aspects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

@EnableLoadTimeWeaving()
@Configuration
public class AspectJConfig {

    @Bean
    public MonitorActorAspect hystrixAspect() {
        MonitorActorAspect aspect = Aspects.aspectOf(MonitorActorAspect.class);
        return aspect;
    }
}