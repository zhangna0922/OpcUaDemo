package com.opcuaserver.opcuaserver.Simple2;

public class OpcModel {
    private String accessPath;
    private String itemName;
    private int namespaceIndex;

    public OpcModel() {
    }

    public OpcModel(int namespaceIndex,String accessPath, String itemName) {
        this.accessPath = accessPath;
        this.itemName = itemName;
        this.namespaceIndex = namespaceIndex;
    }

    public OpcModel(int namespaceIndex, String accessPath) {
        this.accessPath = accessPath;
        this.namespaceIndex = namespaceIndex;
    }

    public int getNamespaceIndex() {
        return namespaceIndex;
    }

    public void setNamespaceIndex(int namespaceIndex) {
        this.namespaceIndex = namespaceIndex;
    }

    public String getAccessPath() {
        return accessPath;
    }

    public void setAccessPath(String accessPath) {
        this.accessPath = accessPath;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
