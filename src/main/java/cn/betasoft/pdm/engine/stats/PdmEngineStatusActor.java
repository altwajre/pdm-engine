package cn.betasoft.pdm.engine.stats;

import akka.actor.AbstractActor;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActorBean
public class PdmEngineStatusActor extends AbstractActor {

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

    private static final Logger logger = LoggerFactory.getLogger(PdmEngineStatusActor.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(DeviceAdd.class, info -> {
            PdmEngineStatus.INSTANCE.addDeviceNum();
        }).match(DeviceMinus.class, info -> {
            PdmEngineStatus.INSTANCE.minusDeviceNum();
        }).match(MoAdd.class, info -> {
            PdmEngineStatus.INSTANCE.addMoNum();
        }).match(MoMinus.class, info -> {
            PdmEngineStatus.INSTANCE.minusMoNum();
        }).match(IndicatorAdd.class, info -> {
            PdmEngineStatus.INSTANCE.addIndicatorNum();
        }).match(IndicatorMinus.class, info -> {
            PdmEngineStatus.INSTANCE.minusIndicatorNum();
        }).match(AlarmTaskAdd.class, info -> {
            PdmEngineStatus.INSTANCE.addAlarmTaskNum();
        }).match(AlarmTaskMinus.class, info -> {
            PdmEngineStatus.INSTANCE.minusAlarmTaskNum();
        }).match(RuleTaskAdd.class, info -> {
            PdmEngineStatus.INSTANCE.addRuleTaskNum();
        }).match(RuleTaskMinus.class, info -> {
            PdmEngineStatus.INSTANCE.minusRuleTaskNum();
        }).matchAny(o -> logger.info("received unknown message")).build();
    }
}
