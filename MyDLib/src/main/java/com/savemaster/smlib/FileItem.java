package com.savemaster.smlib;

public class FileItem {

    private String url;
    private String extend;
    private String fname;

    private YFile yFile;
    private String xd;

    private YFile voiceFile;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public YFile getyFile() {
        return yFile;
    }

    public void setyFile(YFile yFile) {
        this.yFile = yFile;
    }

    public String getXd() {
        return xd;
    }

    public void setXd(String xd) {
        this.xd = xd;
    }

    public YFile getVoiceFile() {
        return voiceFile;
    }

    public void setVoiceFile(YFile voiceFile) {
        this.voiceFile = voiceFile;
    }

}
