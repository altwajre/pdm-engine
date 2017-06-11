package cn.betasoft.pdm.engine.stats;


public enum EngineStatus {

    INSTANCE;

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

    public int getDeviceNum() {
        return deviceNum;
    }

    public int getMoNum() {
        return moNum;
    }

    public int getIndicatorNum() {
        return indicatorNum;
    }

    public int getAlarmTaskNum() {
        return alarmTaskNum;
    }

    public int getRuleTaskNum() {
        return ruleTaskNum;
    }

    public  int addDeviceNum(){
        deviceNum++;
        return deviceNum;
    }

    public int minusDeviceNum(){
        deviceNum--;
        return deviceNum;
    }

    public  int addMoNum(){
        moNum++;
        return moNum;
    }

    public int minusMoNum(){
        moNum--;
        return moNum;
    }

    public  int addIndicatorNum(){
        indicatorNum++;
        return indicatorNum;
    }

    public int minusIndicatorNum(){
        indicatorNum--;
        return indicatorNum;
    }

    public  int addAlarmTaskNum(){
        alarmTaskNum++;
        return alarmTaskNum;
    }

    public int minusAlarmTaskNum(){
        alarmTaskNum--;
        return alarmTaskNum;
    }

    public  int addRuleTaskNum(){
        ruleTaskNum++;
        return ruleTaskNum;
    }

    public int minusRuleTaskNum(){
        ruleTaskNum--;
        return ruleTaskNum;
    }
}
