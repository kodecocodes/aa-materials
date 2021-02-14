package com.raywenderlich.podplay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.podplay.databinding.EpisodeItemBinding
import com.raywenderlich.podplay.viewmodel.PodcastViewModel

class EpisodeListAdapter(
    private var episodeViewList: List<PodcastViewModel.EpisodeViewData>?) :
    RecyclerView.Adapter<EpisodeListAdapter.ViewHolder>() {

  inner class ViewHolder(databinding: EpisodeItemBinding) : RecyclerView.ViewHolder(databinding.root) {
    var episodeViewData: PodcastViewModel.EpisodeViewData? = null
    val titleTextView: TextView = databinding.titleView
    val descTextView: TextView = databinding.descView
    val durationTextView: TextView = databinding.durationView
    val releaseDateTextView: TextView = databinding.releaseDateView
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeListAdapter.ViewHolder {
    return ViewHolder(EpisodeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val episodeViewList = episodeViewList ?: return
    val episodeView = episodeViewList[position]

    holder.episodeViewData = episodeView
    holder.titleTextView.text = episodeView.title
    holder.descTextView.text =  episodeView.description
    holder.durationTextView.text = episodeView.duration
    holder.releaseDateTextView.text = episodeView.releaseDate.toString()
  }

  override fun getItemCount(): Int {
    return episodeViewList?.size ?: 0
  }
}