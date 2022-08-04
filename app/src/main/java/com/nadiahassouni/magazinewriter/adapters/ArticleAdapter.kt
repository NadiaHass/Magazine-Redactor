package com.nadiahassouni.magazinewriter.adapters

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nadiahassouni.magazinewriter.model.Article
import com.nadiahassouni.magazinewriter.R

class ArticleAdapter (
    private val context: Context,
    private val articlesList: ArrayList<Article>)
    : RecyclerView.Adapter<ArticleAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: TextView = view.findViewById(R.id.tv_title)
        var tvDate: TextView = view.findViewById(R.id.tv_date)
        var tvCategory: TextView = view.findViewById(R.id.tv_category)
        var imageView: ImageView = view.findViewById(R.id.iv_article)
        var layout : CardView = view.findViewById(R.id.cardView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_rv_item , parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.tvTitle.text = articlesList[position].title
        holder.tvCategory.text = articlesList[position].category
        holder.tvDate.text = articlesList[position].date
            Glide.with(context)
                .load(Uri.parse(articlesList[position].imageUrl))
                .into(holder.imageView)

        holder.layout.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("article" , articlesList.get(position))
            Navigation.findNavController(holder.itemView).navigate(R.id.action_homeFragment_to_articleFragment , bundle)
        }
    }

    override fun getItemCount()= articlesList.size

}