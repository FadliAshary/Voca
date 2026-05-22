package com.example.voca.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voca.databinding.ItemTipBinding
import com.example.voca.model.FinanceTip

class TipsAdapter(private var tips: List<FinanceTip>) : RecyclerView.Adapter<TipsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTipBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tip = tips[position]
        holder.binding.tvTipTitle.text = tip.title
        holder.binding.tvTipBody.text = tip.body
    }

    override fun getItemCount(): Int = tips.size

    fun updateData(newTips: List<FinanceTip>) {
        tips = newTips
        notifyDataSetChanged()
    }
}