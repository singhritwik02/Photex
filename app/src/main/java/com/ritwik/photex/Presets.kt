package com.ritwik.photex

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Environment
import android.util.Log

class Presets(val context: Context) {
    val DEFAULT_SIZE = 20f
    val DEFAULT_FONT = "DEFAULT"

    val sfManager = context.getSharedPreferences("PRESETS", MODE_PRIVATE)

    var preFont = ""
        get() {
            if (sfManager.contains("FONT")) {
                val font = sfManager.getString("FONT", DEFAULT_FONT) ?: DEFAULT_FONT
                Log.d(TAG, "getting font from presets:$font ")
                val file =
                    context!!.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/Fonts/${font}")
                if (file != null) {
                    if (file.exists()) {
                        return font
                    } else {
                        Log.d(TAG, "setting font: selected font not found ")
                        return DEFAULT_FONT
                    }
                } else {
                    return DEFAULT_FONT
                }

            } else {
                return DEFAULT_FONT
            }
        }
        set(value) {
            with(sfManager.edit())
            {
                putString("FONT", value)
                commit()
            }
            field = value
        }

    var preSize: Float = 10f
        get() {

            if (sfManager.contains("FONT_SIZE")) {
                val size = sfManager.getFloat("FONT_SIZE", DEFAULT_SIZE)
                Log.d(TAG, "Getting from presets: $size")
                return size
            } else {
                return DEFAULT_SIZE
            }
        }
        set(value) {
            Log.d(TAG, "value = $value: ")
            Log.d(TAG, "field = $field: ")
            Log.d(TAG, "putting $value into presets: ")
            with(sfManager.edit())
            {
                putFloat("FONT_SIZE", value)
                commit()
            }

            field = value

        }
    var preTextColor = "#000000"
        get() {

            val color = if (sfManager.contains("TEXT_COLOR")) {
                sfManager.getString("TEXT_COLOR", "#FFFFFF") ?: "#000000"
            } else {
                "#000000"
            }
            Log.d(TAG, "getting text color:$color ")
            return color
        }
        set(value) {
            with(sfManager.edit())
            {
                putString("TEXT_COLOR", value)
                commit()
            }
            field = value
        }

    var preStrokeColor = "#000000"
    get(){
        val strokeColor = if(sfManager.contains("STROKE_COLOR"))
        {
            sfManager.getString("STROKE_COLOR","#000000")?:"#000000"
        }
        else
        {
            "#000000"
        }
        Log.d(TAG, "getting stroke color from presets = :$strokeColor ")
        return strokeColor

    }
    set(value)
    {
        field = value

    }
    companion object {
        private const val TAG = "Presets"
    }
}