package com.ritwik.colortest

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class ColorDatabase: SQLiteOpenHelper {

    private lateinit var context: Context
    constructor(
        context: Context?
    ) : super(context, "Colors.db", null, 1)
    {
        this.context = context!!
    }

    override fun onCreate(database: SQLiteDatabase?) {
        database?.let {
            it.execSQL("create table colors(number integer primary key autoincrement,color text,lastused date)")
           it.execSQL("insert into colors (color,lastused) values(\"#FFFFFF\",\"${getDate()}\")")
            it.execSQL("insert into colors (color,lastused) values(\"#000000\",\"${getDate()}\")")
            it.execSQL("insert into colors (color,lastused) values(\"#FF0000\",\"${getDate()}\")")
            it.execSQL("insert into colors (color,lastused) values(\"#FFFFFF\",\"${getDate()}\")")

        }


    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL("drop table if exists colors")
        onCreate(p0)
    }
    // add a color
    fun addColor(colorCode:String)
    {
        val lastUsed = getDate()
        val db = writableDatabase
        // checking if color already exists
        val cursor = writableDatabase.rawQuery("select *from colors where color =  \"${colorCode}\"",null)
        Log.d(TAG, "addColor: number of records = ${cursor.count}")
        if(cursor.count==0) {
            Log.d(TAG, "addColor: no previous record found")
            db.execSQL("insert into colors (color,lastused) values(\"$colorCode\",\"$lastUsed\")");
        }
        else{
            Log.d(TAG, "addColor: Previous record found")
            cursor.moveToFirst()
            val index = cursor.getInt((cursor.getColumnIndex("number")))
            db.execSQL("delete from colors where number = $index")
            addColor(colorCode)
        }
        val tCursor = db.rawQuery("select *from colors",null)
        Log.d(TAG, "addColor: ${tCursor.count}")
        var count = tCursor.count

            while (count >= 15) {
                Log.d(TAG, "addColor: deleting $count")
                db.execSQL("delete from colors where number = ${count}")
                count -= 1
            }
        Log.d(TAG, "addColor: deleting $count")



    }
    fun clearTable()
    {
        writableDatabase?.execSQL("drop table if exists colors")
        onCreate(writableDatabase)
        val db = writableDatabase
        db.execSQL("insert into colors (color,lastused) values(\"#FFFFFF\",\"${getDate()}\")")
        db.execSQL("insert into colors (color,lastused) values(\"#000000\",\"${getDate()}\")")
        db.execSQL("insert into colors (color,lastused) values(\"#FF0000\",\"${getDate()}\")")
    }
    // update the position of the color
    fun getColorList():ArrayList<String>
    {
        val list = arrayListOf<String>()
        val cursor = writableDatabase.rawQuery("select color from colors order by lastused desc",null)
        if(cursor.count !=0) {
            while (cursor.moveToNext()) {
                val color = cursor.getString((cursor.getColumnIndex("color")))
                Log.d(TAG, "getColorList: color = "+color);
                list.add(color)
            }
        }
        else
        {
            Log.d(TAG, "getColorList: Cursor count 0")
        }
        return list
    }
    fun removeColor(colorString:String)
    {
        Log.d(TAG, "removeColor: removingColor")
        val db = writableDatabase
        db.execSQL("delete from colors where color = $colorString")
        
    }
    fun getDate():String{
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = Date()
        val time = formatter.format(date)
        return time
    }
    fun getTopColor():String
    {
        val cursor = writableDatabase.rawQuery("select color from colors order by lastused desc",null)
        cursor.moveToFirst()
        return cursor.getString(cursor.getColumnIndex("color"))
    }
    companion object
    {
        private const val TAG = "ColorDatabase"
    }
}