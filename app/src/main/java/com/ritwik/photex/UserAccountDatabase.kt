package com.ritwik.photex

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class UserAccountDatabase(
    context: Context?,

) : SQLiteOpenHelper(context, "UserAccounts.db", null, 1) {
    override fun onCreate(database: SQLiteDatabase?) {

        database?.let {
            it.execSQL("create table UserAccounts(platform varchar(20),username varchar(20))")
        }
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL("drop table if exists UserAccounts")
        onCreate(p0)
    }
    fun addAccount(platformp:String, usernamep:String)
    {
        writableDatabase.execSQL("insert into UserAccounts values(\"$platformp\",\"$usernamep\")")
    }
    fun deleteAccount(username: String)
    {
        writableDatabase.execSQL("delete from UserAccounts where username = \"$username\"")
    }
    fun getAllUsernames():ArrayList<UsernameData>
    {
        val cursor = writableDatabase.rawQuery("select *from UserAccounts",null)
        val list = arrayListOf<UsernameData>()
        if(cursor.count!=0)
        {
            while (cursor.moveToNext())
            {
                val temp = UsernameData()
                temp.setUsername(cursor.getString(cursor.getColumnIndex("username")))
                var res = 0
                when(cursor.getString(cursor.getColumnIndex("platform")))
                {
                    "Instagram"->
                    {
                        res = R.drawable.instagram
                    }
                    "Twitter"->
                    {
                        res = R.drawable.twitter
                    }
                    "Youtube"->
                    {
                        res = R.drawable.youtube
                    }
                }
                temp.setImageResourceId(res)
                list.add(temp)



            }
        }
        else
        {
            Log.d(TAG, "getAllUsernames: Cursor count is 0")
        }
        return list
    }
    companion object
    {
        private const val TAG = "UserAccountDatabase"
    }
}