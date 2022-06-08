package com.goldenowl.ecommerce.ui.tutorial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R

class TutorialPagerAdapter() : RecyclerView.Adapter<TutorialPagerAdapter.ViewHolder>() {

    val NUM_PAGES = 4

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val tutImg = view.findViewById<ImageView>(R.id.tut_img)
        val tutTitle =  view.findViewById<TextView>(R.id.tut_title)
        val tutDes =  view.findViewById<TextView>(R.id.tut_des)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_tutorial,parent,false))
    }

    override fun getItemCount(): Int = NUM_PAGES

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        when(position) {
            0 -> holder.apply {
                tutImg.setImageResource(R.drawable.tut_1)
                tutTitle.setText("Introduction 1")
                tutDes.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nisi nulla purus neque quisque dictum dui. Accumsan fames adipiscing.")
            }
            1 -> holder.apply {
                tutImg.setImageResource(R.drawable.tut_2)
                tutTitle.setText("Introduction 2")
                tutDes.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nisi nulla purus neque quisque dictum dui. Accumsan fames adipiscing.")
            }
            2 -> holder.apply {
                tutImg.setImageResource(R.drawable.tut_3)
                tutTitle.setText("Introduction 3")
                tutDes.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nisi nulla purus neque quisque dictum dui. Accumsan fames adipiscing.")
            }
            3 -> holder.apply {
                tutImg.setImageResource(R.drawable.tut_4)
                tutTitle.setText("Explore the app")
                tutDes.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nisi nulla purus neque quisque dictum dui. Accumsan fames adipiscing.")
            }
        }
    }
}
