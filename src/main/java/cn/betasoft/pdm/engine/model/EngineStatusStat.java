package cn.betasoft.pdm.engine.model;

public class EngineStatusStat {

    //设备数量
    private int deviceNum = 0;

    //mo数量
    private int moNum = 0;

    //指标数量
    private int indicatorNum = 0;

    //告警任务数
    private int alarmTaskNum = 0;

    //智维任务数
    private int ruleTaskNum = 0;

    public EngineStatusStat(){

    }

    public int getDeviceNum() {
        return deviceNum;
    }

    public void setDeviceNum(int deviceNum) {
        this.deviceNum = deviceNum;
    }

    public int getMoNum() {
        return moNum;
    }

    public void setMoNum(int moNum) {
        this.moNum = moNum;
    }

    public int getIndicatorNum() {
        return indicatorNum;
    }

    public void setIndicatorNum(int indicatorNum) {
        this.indicatorNum = indicatorNum;
    }

    public int getAlarmTaskNum() {
        return alarmTaskNum;
    }

    public void setAlarmTaskNum(int alarmTaskNum) {
        this.alarmTaskNum = alarmTaskNum;
    }

    public int getRuleTaskNum() {
        return ruleTaskNum;
    }

    public void setRuleTaskNum(int ruleTaskNum) {
        this.ruleTaskNum = ruleTaskNum;
    }
}
