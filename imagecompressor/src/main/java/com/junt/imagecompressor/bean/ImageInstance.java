package com.junt.imagecompressor.bean;

public class ImageInstance {
    private String inputPath;
    private String outPutPath;

    public ImageInstance(String inputPath, String outPutPath) {
        this.inputPath = inputPath;
        this.outPutPath = outPutPath;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutPutPath() {
        return outPutPath;
    }

    public void setOutPutPath(String outPutPath) {
        this.outPutPath = outPutPath;
    }
}
