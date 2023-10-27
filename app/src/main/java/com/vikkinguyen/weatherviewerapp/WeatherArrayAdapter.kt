package com.vikkinguyen.weatherviewerapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


class WeatherArrayAdapter (context: Context, forecast: List<Weather>)
    : ArrayAdapter<Weather>(context, -1, forecast){

    private val bitmaps = HashMap<String, Bitmap>()

    private class ViewHolder {
        var conditionImageView: ImageView? = null
        var dayTextView: TextView? = null
        var lowTextView: TextView? = null
        var hiTextView: TextView? = null
        var humidityTextView: TextView? = null
    }

    override fun getView(position: Int, _convertView: View?, parent: ViewGroup): View {
        var convertView = _convertView
        val day = getItem(position)
        val viewHolder: ViewHolder

        if (convertView == null){
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.list_item,parent,false)
            viewHolder.conditionImageView = convertView!!.findViewById(R.id.conditionImageView)
            viewHolder.dayTextView = convertView!!.findViewById(R.id.dayTextView)
            viewHolder.lowTextView = convertView!!.findViewById(R.id.lowTextView)
            viewHolder.hiTextView = convertView!!.findViewById(R.id.hiTextView)
            viewHolder.humidityTextView = convertView!!.findViewById(R.id.humidityTextView)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        if (bitmaps.containsKey(day!!.iconURL)){
            viewHolder.conditionImageView!!.setImageBitmap(bitmaps[day.iconURL])
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                val bitmap: Bitmap? = getImage(day.iconURL)
                viewHolder.conditionImageView!!.setImageBitmap(bitmap)
            }
        }

        viewHolder.dayTextView!!.text = context.getString(R.string.day_description, day.dayOfWeek, day.description)
        viewHolder.lowTextView!!.text = context.getString(R.string.low_temp, day.minTemp)
        viewHolder.hiTextView!!.text = context.getString(R.string.high_temp, day.maxTemp)
        viewHolder.humidityTextView!!.text = context.getString(R.string.humidity, day.humidity)

        return  convertView
    }
    private suspend fun getImage(urlString: String): Bitmap? =
        withContext(Dispatchers.IO){
            var connection: HttpURLConnection? = null
            try {
                var bitmap: Bitmap? = null
                var url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.inputStream.use { inputStream ->
                    bitmap = BitmapFactory.decodeStream(inputStream)
                    bitmaps[urlString] = bitmap!!
                }
                    bitmap
        } catch (e: Exception) {
            e.printStackTrace()
                } finally {
                    connection!!.disconnect()
                } as Bitmap?
    }
}