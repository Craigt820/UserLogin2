package com.idi.userlogin.utils;

import com.idi.userlogin.Handlers.JsonHandler;


public abstract class DBUtils<T> {

    public abstract void updateItem(T item, String sql);

    public enum DBTable {

        M("_M"), C("_C"), H("_H"), D("_D"), G("_G");

        private String table;

        DBTable(String table) {
            this.table = table;
        }

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

    }
}
