package com.junt.imagecompressor.exception;

public class CompressException extends Exception {
    public CompressException(String message) {
        super(String.format("ImageCompressor ===> %s", message));
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
