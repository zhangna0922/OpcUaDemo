package com.opcuaserver.opcuaserver.demo;

import java.util.Map;

/**
 *
 */
public class EquipmentModel {
    private String name;
    private String nodeId;

    private Map<String, Object> statusMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Map<String, Object> getStatusMap() {
        return statusMap;
    }

    public void setStatusMap(Map<String, Object> statusMap) {
        this.statusMap = statusMap;
    }
}
