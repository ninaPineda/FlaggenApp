package de.hsfl.nina.meineapp

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import de.hsfl.nina.meineapp.MainViewModel


class MapPointsAdapter(
    private val mapPointsList: MutableList<MapPoint>,
    private val create: Boolean,
    private var listener: MapPointClickListener? = null
) : RecyclerView.Adapter<MapPointsAdapter.MapPointViewHolder>() {

    class MapPointViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val indexTextView: TextView = itemView.findViewById(R.id.indexTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapPointViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_map_point, parent, false)
        return MapPointViewHolder(view)
    }

    override fun onBindViewHolder(holder: MapPointViewHolder, position: Int) {
        val mapPoint = mapPointsList[position]
        val number = position + 1
        val state = mapPoint.state
        holder.indexTextView.text = "Punkt $number - $state"
        if (!create) {
            holder.deleteButton.visibility = View.GONE
        } else {
            holder.deleteButton.setOnClickListener {
                listener?.onDeleteButtonClick(position)
            }
        }

    }

    override fun getItemCount(): Int {
        return mapPointsList.size
    }


    fun updateData(newData: MutableList<MapPoint>) {
        mapPointsList.clear()
        mapPointsList.addAll(newData)
        notifyDataSetChanged()
    }

    interface MapPointClickListener {
        fun onDeleteButtonClick(position: Int)
    }

    fun setMapPointClickListener(listener: MapPointClickListener) {
        this.listener = listener
    }




}
