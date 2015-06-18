package com.example.robospock.database;

import com.j256.ormlite.field.DatabaseField;

public class DatabaseObject {
    @DatabaseField public String title;
    @DatabaseField public int size;
    @DatabaseField(id = true) public int id;

    public DatabaseObject() {
        super();
    }

    public DatabaseObject(final String title, final int size, final int id) {
        super();
        this.title = title;
        this.size = size;
        this.id = id;
    }

}
