package com.ln.simplechat.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.util.Util
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ln.simplechat.ChannelNotFoundException
import com.ln.simplechat.model.*
import com.ln.simplechat.utils.MyResult
import com.ln.simplechat.utils.bubble.NotificationHelper
import com.ln.simplechat.utils.getFileUri
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    private val dispatcherIO = Dispatchers.IO
    private val db: FirebaseFirestore = Firebase.firestore
    private val storageReference = Firebase.storage
    private val messages = db.collection("messages")

    fun showChat(message: Message) {
        notificationHelper.showNotification(message, true)
    }

    fun dismissNotification(channelId: Int) {
        notificationHelper.dismissNotification(channelId)
    }

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

    suspend fun getListChannelAndMember(channelId: String): MyResult<List<ChannelAndMember>> {
        return withContext(dispatcherIO) {
            try {
                val setUser: MutableSet<String> = mutableSetOf()
                when (val listChannel = getListChannel(channelId)) {
                    is MyResult.Success -> {
                        listChannel.data.forEach { channel -> setUser.addAll(channel.listUser) }
                        val listMember = getListMember(setUser.toList())
                        val listChannelAndMember = listChannel.data.map { channel ->
                            val listChannelMember = listMember.filter { member -> member.id in channel.listUser }
                            ChannelAndMember(channel, listChannelMember)
                        }
                        MyResult.Success(listChannelAndMember)
                    }
                    is MyResult.Error -> MyResult.Error(listChannel.exception)
                    else -> MyResult.Loading
                }
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun uploadFiles(
        result: ArrayList<LocalMedia>,
        channelId: String,
        onUploaded: ((String) -> Unit)?
    ): MyResult<List<String>> {
        val uploadedUrls = MutableList(result.size) { "" }
        return withContext(dispatcherIO) {
            supervisorScope {
                try {
                    val uploadTask = result.mapIndexed { index, localMedia ->
                        async {
                            val file = getFileUri(localMedia.availablePath) ?: return@async
                            val ref = storageReference.reference.child("messages/$channelId/${Util.autoId()}")
                            val url = ref.putFile(file)
                                .await()
                                .metadata!!.reference!!.downloadUrl
                                .await().toString()
                            onUploaded?.invoke(url)
                            uploadedUrls[index] = url
                        }
                    }
                    uploadTask.awaitAll()
                    MyResult.Success(uploadedUrls)
                } catch (e: Exception) {
                    MyResult.Error(e)
                }
            }
        }
    }

    suspend fun getChannel(id: String): MyResult<Channel> {
        return withContext(dispatcherIO) {
            try {
                val task = db.collection("channels").document(id).get().await()
                val channel = task.toObject(Channel::class.java)
                if (channel == null)
                    MyResult.Error(ChannelNotFoundException(id))
                else
                    MyResult.Success(channel)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    fun initNotificationHelper(channel: Channel) {
        notificationHelper.initData(channel)
        notificationHelper.setUpNotificationChannels()
    }

    fun pushReact(channelId: String, messageId: String, userId: String, react: Reaction, reactId: Int) {
        val reactName = Reaction.reactOrder[reactId]
        val list = react.getArray(reactName).toMutableList()
        if (list.contains(userId))
            list.remove(userId)
        else
            list.add(userId)
        messages.document(channelId).collection("list-message").document(messageId)
            .update("reactions.$reactName", list)
    }
}
