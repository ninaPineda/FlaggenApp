package de.hsfl.nina.meineapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.floor

class HighscoreViewAdapter(private val highscoreList: MutableList<Highscore>) :
    RecyclerView.Adapter<HighscoreViewAdapter.HighscoreViewHolder>() {

    class HighscoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerNameTextView: TextView = itemView.findViewById(R.id.playerNameTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighscoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_highscore, parent, false)
        return HighscoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: HighscoreViewHolder, position: Int) {
        val highscore = highscoreList[position]
        holder.playerNameTextView.text = "Name: " + highscore.name
        holder.timeTextView.text = "Time: " + timeToString(highscore.time) + " Distance: " + highscore.distance
    }

    override fun getItemCount(): Int {
        return highscoreList.size
    }

    fun updateData(newData: MutableList<Highscore>){
        highscoreList.clear()
        highscoreList.addAll(newData)
        notifyDataSetChanged()
    }

    private fun timeToString(timeInMillis: Long): String {
        var time = timeInMillis.toDouble()
        val millis = (time % 1000).toInt()
        time = floor(time / 1000.0)
        val seconds = (time % 60).toInt()
        time = floor(time / 60.0)
        val minutes = (time % 60).toInt()
        val hours = (time / 60.0).toInt()

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
    }
}
