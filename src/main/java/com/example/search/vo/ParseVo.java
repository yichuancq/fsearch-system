package com.example.search.vo;

import java.util.HashMap;

public class ParseVo {
    private String content;
    private HashMap<String, String> metadataNamesMap = new HashMap<String, String>();

    public ParseVo() {
    }

    public ParseVo(String content, HashMap metadataNamesMap) {
        this.content = content;
        this.metadataNamesMap = metadataNamesMap;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public HashMap getMetadataNamesMap() {
        return metadataNamesMap;
    }

    public void setMetadataNamesMap(HashMap metadataNamesMap) {
        this.metadataNamesMap = metadataNamesMap;
    }
}
