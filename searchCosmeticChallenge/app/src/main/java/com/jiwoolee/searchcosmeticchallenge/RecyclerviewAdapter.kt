package com.jiwoolee.searchcosmeticchallenge

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jiwoolee.searchcosmeticchallenge.data.SearchedItem
import com.jiwoolee.searchcosmeticchallenge.databinding.RecyclerviewItemBinding

class RecyclerviewAdapter(private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<RecyclerviewAdapter.CustomViewHolder>() {
    private var dataList = java.util.ArrayList<SearchedItem>()

    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = DataBindingUtil.inflate<RecyclerviewItemBinding>(LayoutInflater.from(parent.context), R.layout.recyclerview_item, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.apply {
            onBind(dataList[position])
            itemView.tag = dataList[position]
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClicked(dataList[position])
        }
    }

    class CustomViewHolder (var binding: RecyclerviewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: SearchedItem) {
            binding.apply {
                recyclerviewItem = item
            }
        }
    }

    fun addItem(item: SearchedItem) {
        dataList.add(item)
    }

    fun removeItem(){
        dataList.clear()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}

@BindingAdapter("setImage")
fun bindingImageFromString(view: ImageView, image: String) {
    Glide.with(view.context)
        .load(image)
        .fitCenter()
        .into(view)
}

interface OnItemClickListener{
    fun onItemClicked(item: SearchedItem)
}