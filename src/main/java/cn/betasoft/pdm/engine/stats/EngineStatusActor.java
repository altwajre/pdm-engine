package cn.betasoft.pdm.engine.stats;

import akka.actor.AbstractActor;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.model.Indicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActorBean
public class EngineStatusActor extends AbstractActor {

    static public class DeviceAdd { }

    static public class DeviceMinus { }

    static public class MoAdd { }

    static public class MoMinus { }

    static public class IndicatorAdd { }

    static public class IndicatorMinus { }

    static public class AlarmTaskAdd { }

    static public class AlarmTaskMinus { }

    static public class RuleTaskAdd { }

    static public class RuleTaskMinus { }

    private static final Logger logger = LoggerFactory.getLogger(EngineStatusActor.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(DeviceAdd.class, info -> {
            EngineStatus.INSTANCE.addDeviceNum();
        }).match(DeviceMinus.class, info -> {
            EngineStatus.INSTANCE.minusDeviceNum();
        }).match(MoAdd.class, info -> {
            EngineStatus.INSTANCE.addMoNum();
        }).match(MoMinus.class, info -> {
            EngineStatus.INSTANCE.minusMoNum();
        }).match(IndicatorAdd.class, info -> {
            EngineStatus.INSTANCE.addIndicatorNum();
        }).match(IndicatorMinus.class, info -> {
            EngineStatus.INSTANCE.minusIndicatorNum();
        }).match(AlarmTaskAdd.class, info -> {
            EngineStatus.INSTANCE.addAlarmTaskNum();
        }).match(AlarmTaskMinus.class, info -> {
            EngineStatus.INSTANCE.minusAlarmTaskNum();
        }).match(RuleTaskAdd.class, info -> {
            EngineStatus.INSTANCE.addRuleTaskNum();
        }).match(RuleTaskMinus.class, info -> {
            EngineStatus.INSTANCE.minusRuleTaskNum();
        }).matchAny(o -> logger.info("received unknown message")).build();
    }
}
