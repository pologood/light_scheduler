package com.jd.eptid.scheduler.server.web.page;

/**
 * Created by classdan on 16-12-7.
 */
public class PageData<T> {
    private int draw;
    private int recordsTotal;
    private int recordsFiltered;
    private T data;

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> PageData<T> build(int pageNo, int total, T data) {
        PageData<T> pageData = new PageData<T>();
        pageData.draw = pageNo;
        pageData.data = data;
        pageData.recordsTotal = total;
        pageData.recordsFiltered = pageData.recordsTotal;
        return pageData;
    }
}
