package com.ln.simplechat.ui.chat

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ln.simplechat.R

class ReactionAdapter(val data: List<Pair<Int, List<String>>>) :
    RecyclerView.Adapter<ReactionAdapter.ReactionViewHolder>() {

    class ReactionViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.iv_reaction)
        fun bind(reaction: Pair<Int, List<String>>) {
            val resource = reactionMap[reaction.first]
            val reactPeople = reaction.second /* dialog user who react this */
            image.setImageResource(resource)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionViewHolder {
        val image = ImageView(parent.context)
        image.id = R.id.iv_reaction
        return ReactionViewHolder(image)
    }

    override fun getItemId(position: Int): Long {
        return if (position == itemCount) 0 else 1
    }

    override fun onBindViewHolder(holder: ReactionViewHolder, position: Int) {
        val react = data[position]
        holder.bind(react)
    }

    override fun getItemCount() = data.size

    companion object {
        val reactionMap = listOf(
            R.drawable.ic_love,
            R.drawable.ic_haha,
            R.drawable.ic_wow,
            R.drawable.ic_sad,
            R.drawable.ic_angry,
            R.drawable.ic_liker,
        )
    }
}
