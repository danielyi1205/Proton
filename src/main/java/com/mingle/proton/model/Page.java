package com.mingle.proton.model;

import java.util.List;

/**
 * Created by Daniel on 2015/7/14.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
public class Page<T> {
    private int total;
    private List<T> rows;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
