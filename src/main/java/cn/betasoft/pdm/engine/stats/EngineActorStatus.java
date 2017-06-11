package cn.betasoft.pdm.engine.stats;


import java.util.ArrayList;
import java.util.List;

public class EngineActorStatus {

    private String name;

    private String actorPath;

    private int mailboxNum;

    private String showData;

    private List<EngineActorStatus> children = new ArrayList<>();

    public EngineActorStatus(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActorPath() {
        return actorPath;
    }

    public void setActorPath(String actorPath) {
        this.actorPath = actorPath;
    }

    public int getMailboxNum() {
        return mailboxNum;
    }

    public void setMailboxNum(int mailboxNum) {
        this.mailboxNum = mailboxNum;
    }

    public String getShowData() {
        return showData;
    }

    public void setShowData(String showData) {
        this.showData = showData;
    }

    public List<EngineActorStatus> getChildren() {
        return children;
    }

    public void setChildren(List<EngineActorStatus> children) {
        this.children = children;
    }
}
