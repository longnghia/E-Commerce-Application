package com.ln.simplechat.utils.media


//private class ImageCropEngine : CropEngine {
//    override fun onStartCrop(
//        fragment: Fragment, currentLocalMedia: LocalMedia,
//        dataSource: ArrayList<LocalMedia>, requestCode: Int
//    ) {
//        val currentCropPath = currentLocalMedia.availablePath
//        val inputUri: Uri = if (PictureMimeType.isContent(currentCropPath) || PictureMimeType.isHasHttp(currentCropPath)) {
//            Uri.parse(currentCropPath)
//        } else {
//            Uri.fromFile(File(currentCropPath))
//        }
//        val fileName: String = DateUtils.getCreateFileName("CROP_").toString() + ".jpg"
//        val destinationUri: Uri = Uri.fromFile(File(getSandboxPath(), fileName))
//        val options: UCrop.Options = buildOptions()
//        val dataCropSource: ArrayList<String> = ArrayList()
//        for (i in 0 until dataSource.size()) {
//            val media = dataSource[i]
//            dataCropSource.add(media.availablePath)
//        }
//        val uCrop = UCrop.of(inputUri, destinationUri, dataCropSource)
//        uCrop.withOptions(options)
//        uCrop.setImageEngine(object : UCropImageEngine {
//            override fun loadImage(context: Context?, url: String?, imageView: ImageView?) {
//                if (!ImageLoaderUtils.assertValidRequest(context)) {
//                    return
//                }
//                Glide.with(context).load(url).override(180, 180).into<Target<Drawable>>(imageView)
//            }
//
//            override fun loadImage(
//                context: Context?,
//                url: Uri?,
//                maxWidth: Int,
//                maxHeight: Int,
//                call: UCropImageEngine.OnCallbackListener<Bitmap?>?
//            ) {
//            }
//        })
//        uCrop.start(fragment.requireActivity(), fragment, requestCode)
//    }
//}