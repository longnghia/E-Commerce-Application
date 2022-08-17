package com.ln.simplechat.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ln.simplechat.model.Channel
import com.ln.simplechat.model.Member
import com.ln.simplechat.utils.MyResult
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class ChatRepository @Inject constructor() {
    private val dispatcherIO = Dispatchers.IO
    private val db: FirebaseFirestore = Firebase.firestore
    private val storageReference = Firebase.storage

    suspend fun getListMember(listId: List<String>): List<Member> {
        val listMember: MutableList<Member> = mutableListOf()
        return withContext(dispatcherIO) {
            try {
                for (id in listId) {
                    val userTask = db.collection("users").document(id).get(Source.SERVER).await()
                    val user = userTask.toObject(Member::class.java) ?: continue
                    listMember.add(user)
                }
                listMember
            } catch (e: Exception) {
                Log.e("TAG", "getListMember ERROR", e)
                emptyList()
            }
        }
    }

    suspend fun getListChannel(userId: String): MyResult<List<Channel>> {
        return withContext(dispatcherIO) {
            try {
                val task = db.collection("channels").whereArrayContains("listUser", userId).get().await()
                val list: List<Channel> = task.toObjects(Channel::class.java)
                MyResult.Success(list)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun uploadImage(
        result: ArrayList<LocalMedia>,
        channelId: String,
        onUploaded: ((String) -> Unit)?
    ): MyResult<List<String>> {
        val uploadedUrls = MutableList(result.size) { "" }
        return supervisorScope {
            try {
                val uploadTask = result.mapIndexed { index, localMedia ->
                    async {
                        val file = Uri.fromFile(File(localMedia.availablePath))
                        val ref = storageReference.reference.child("messages/$channelId/${file.lastPathSegment}")
                        val url = ref.putFile(file)
                            .await()
                            .metadata!!.reference!!.downloadUrl
                            .await().toString()
                        onUploaded?.invoke(url)
                        uploadedUrls[index] = url
                    }
                }
                uploadTask.awaitAll()
                Log.d("TAG", "uploadImage:$uploadedUrls ")
                MyResult.Success(uploadedUrls)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }

    }
}
