package com.idi.userlogin.utils;

import com.idi.userlogin.Handlers.ConnectionHandler;

import static com.idi.userlogin.Main.jsonHandler;

public abstract class DBUtils<T> {

    public abstract void updateItem(T item, String sql);

    public enum DBTable {

        M(jsonHandler.getSelJobID() + "_M"), C(jsonHandler.getSelJobID() + "_C"), H(jsonHandler.getSelJobID() + "_H"), D(jsonHandler.getSelJobID() + "_D"), G(jsonHandler.getSelJobID() + "_G");

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
