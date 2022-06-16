package com.goldenowl.ecommerce.utils

import android.content.Context
import android.util.Log
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule


//@GlideModule
//class ECommerceGlideModule : AppGlideModule() {
//
//    override fun applyOptions(context: Context, builder: GlideBuilder) {
//        super.applyOptions(context, builder)
//        builder.apply { RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL) }
//    }
//
//}

@GlideModule
class ECommerceGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.ERROR)
    }
}