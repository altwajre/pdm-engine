package cn.betasoft.pdm.engine.stats;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EngineActorStatus implements Comparable{

    private String name;

    private String actorPath;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private TreeNodeType nodeType;

    private String showData;

    private List<EngineActorStatus> children = new ArrayList<>();

    private int descendantNum;

    @JsonIgnore
    private Map<String,EngineActorStatus> allDescendants = new HashMap<>();

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

    public TreeNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(TreeNodeType nodeType) {
        this.nodeType = nodeType;
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

    public int getDescendantNum() {
        return descendantNum;
    }

    public void setDescendantNum(int descendantNum) {
        this.descendantNum = descendantNum;
    }

    public Map<String, EngineActorStatus> getAllDescendants() {
        return allDescendants;
    }

    public void setAllDescendants(Map<String, EngineActorStatus> allDescendants) {
        this.allDescendants = allDescendants;
    }

    @Override public int compareTo(Object obj) {
        EngineActorStatus b = (EngineActorStatus) obj;
        return this.getNodeType().ordinal() - b.getNodeType().ordinal();
    }
}
