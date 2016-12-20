package com.jd.eptid.scheduler.core.domain.message;

/**
 * Created by classdan on 16-9-12.
 */
public class Header {
    private int length;
    private int headerLength;
    private int version = 1;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Header{" +
                "length=" + length +
                ", headerLength=" + headerLength +
                '}';
    }
}
