package com.messdiener.cms.request;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class StatusCaptureResponseWrapper extends HttpServletResponseWrapper {

    private int httpStatus = 200;

    public StatusCaptureResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
        this.httpStatus = sc;
    }

    @Override
    public void sendError(int sc) throws java.io.IOException {
        super.sendError(sc);
        this.httpStatus = sc;
    }

    @Override
    public void sendError(int sc, String msg) throws java.io.IOException {
        super.sendError(sc, msg);
        this.httpStatus = sc;
    }

    public int getStatus() {
        return this.httpStatus;
    }
}
