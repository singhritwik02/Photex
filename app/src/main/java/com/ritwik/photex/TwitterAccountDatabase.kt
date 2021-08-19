package com.ritwik.photex

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TwitterAccountDatabase(context: Context,
): SQLiteOpenHelper(context, "TWITTER_ACCOUNTS", null, 1) {
    val tableName = "TwitterAccounts"
    override fun onCreate(database: SQLiteDatabase?) {
        database?.let {
            it.execSQL("create table $tableName(NAME varchar(20),username varchar(20))")
        }
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL("drop table if exists $tableName")
        onCreate(p0)
    }

    fun addAccount(name: String, username: String)
    {
        writableDatabase.execSQL("insert into $tableName values(\"$name\",\"$username)\"")
    }
    fun removeAccount(name:String,username:String)
    {
        writableDatabase.execSQL("delete from $tableName where name = \"$name\" and username = \"$username\"")
    }

}