package com.ln.simplechat.ui.chat

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.util.Util
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ln.simplechat.R
import com.ln.simplechat.SimpleChatActivity
import com.ln.simplechat.api.TopicAPI
import com.ln.simplechat.application.loadData
import com.ln.simplechat.application.saveData
import com.ln.simplechat.application.toast
import com.ln.simplechat.model.*
import com.ln.simplechat.repository.ChatRepository
import com.ln.simplechat.utils.MyResult
import com.ln.simplechat.utils.toChatMedia
import com.luck.picture.lib.entity.LocalMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), LifecycleObserver {

    val userId = savedStateHandle.get<String>(SimpleChatActivity.EXTRA_CURRENT_USER)!!

    private lateinit var channelId: String
    private lateinit var query: Query
    private lateinit var fileName: String
    private val gson = Gson()
    private val typeListMessage = object : TypeToken<List<Message>>() {}.type

    private var listenerRegistration: ListenerRegistration? = null
    private var _channel: Channel? = null
    val channel: LiveData<MyResult<Channel>> = liveData {
        val result = chatRepository.getChannel(channelId)
        if (result is MyResult.Success) {
            _channel = result.data
        }
        emit(result)
    }

    private var _me: Member? = null
    val listMember: LiveData<List<Member>> = channel.switchMap { channel ->
        liveData {
            when (channel) {
                is MyResult.Success -> {
                    val list = chatRepository.getListMember(channel.data.listUser)
                    _me = list.find { it.id == userId }
                    emit(list)
                }
                is MyResult.Error -> {
                    _toastMessage.postValue(channel.exception.message)
                    emit(emptyList())
                }
                else -> emit(emptyList())
            }
        }
    }


    private var _listMessage = MutableLiveData<List<Message>>()
    val listMessage: LiveData<List<Message>> = _listMessage

    private var _sendStatus = MutableLiveData<Status>()
    val sendStatus: LiveData<Status> = _sendStatus

    private var _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage


    private val db: FirebaseFirestore = Firebase.firestore
    private val messages = db.collection("messages")
    private var listener: EventListener<QuerySnapshot?> = object : EventListener<QuerySnapshot?> {
        override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
            if (error != null) {
                return
            }
            val messages = value?.toObjects(Message::class.java)
            _listMessage.postValue(messages)
        }
    }

    fun showAsBubble() {
        _listMessage.value?.last()?.let {
            notifyMessage(it)
        }
    }

    fun initNotificationHelper(channel: Channel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            chatRepository.initNotificationHelper(channel)
    }

    fun notifyMessage(mess: Message) {
        val sender = mess.sender.let { sender ->
            listMember.value?.find { it.id == sender }
        }
        if (sender != null) {
            chatRepository.showChat(mess)
        }
    }

    fun sendReactMessage() {
        val tmpMessage = Message(Util.autoId(), userId, isReact = true)
        sendMessage(tmpMessage)
    }

    fun fmSendToTopic(message: Message) {
        if (_channel == null || _me == null) {
            toast(R.string.cant_send_topic_notification)
            return
        }
        val topicData = TopicData(_channel!!, message, _me!!)
        viewModelScope.launch {
            try {
                val result = TopicAPI.retrofitService.sendNotification(topicData)
            } catch (e: Exception) {
                toast(e.message)
            }
        }
    }

    fun sendMessage(message: Message, callback: ((DocumentReference) -> Unit)? = null) {
        _sendStatus.postValue(Status.LOADING)
        val messageRef = messages.document(channelId).collection("list-message").document(message.id)
        messageRef.set(message)
            .addOnSuccessListener {
                callback?.invoke(messageRef)
                _sendStatus.postValue(Status.SUCCESS)
            }
            .addOnFailureListener {
                Log.e(TAG, "sendMessage: ERROR", it)
                _sendStatus.postValue(Status.FAIL)
                _toastMessage.postValue(it.message)
            }
    }

    fun sendAudio(result: ArrayList<LocalMedia>) {
        if (result.isEmpty()) return

        val tmpList = result.map { it.toChatMedia() }
        Log.d(TAG, "sendAudio tmpList=$tmpList")
        val tmpMessage = Message(Util.autoId(), userId, medias = tmpList)
        sendMessage(tmpMessage) { ref ->
            uploadAudio(result) { listAudio ->
                Log.d(TAG, "sendAudio listAudio=$listAudio")
                val list = tmpList.mapIndexed { index, chatMedia ->
                    chatMedia.apply { path = listAudio[index] }
                }
                tmpMessage.medias = list
                ref.set(tmpMessage)
                fmSendToTopic(tmpMessage)
            }
        }
    }

    private fun uploadAudio(result: ArrayList<LocalMedia>, callback: (List<String>) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            val uploadResult = chatRepository.uploadFiles(result, channelId) {}
            Log.d(TAG, "uploadAudio: $uploadResult")
            when (uploadResult) {
                is MyResult.Success -> {
                    val listAudio = uploadResult.data
                    callback(listAudio)
                }
                is MyResult.Error -> _toastMessage.postValue(uploadResult.exception.message)
            }
        }
    }

    fun sendImagesMessage(context: Context, result: ArrayList<LocalMedia>, tmpMessage: Message) {
        if (result.isEmpty()) return
        val tmpList = tmpMessage.medias
        sendMessage(tmpMessage) { ref ->
            uploadImage(context, result) { listImg ->
                val list = tmpList?.mapIndexed { index, chatMedia ->
                    chatMedia.apply { path = listImg[index] }
                }
                tmpMessage.medias = list
                ref.set(tmpMessage)
                fmSendToTopic(tmpMessage)
            }
        }
    }

    private fun uploadImage(context: Context, result: ArrayList<LocalMedia>, callback: (List<String>) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            val uploadResult = chatRepository.uploadFiles(result, channelId) { url ->
                Glide.with(context)
                    .downloadOnly()
                    .load(url)
                    .submit()
                    .get()
            }
            Log.d(TAG, "uploadImage: $uploadResult")
            when (uploadResult) {
                is MyResult.Success -> {
                    val listImg = uploadResult.data
                    callback(listImg)
                }
                is MyResult.Error -> _toastMessage.postValue(uploadResult.exception.message)
            }
        }
    }

    fun startListening() {
        listenerRegistration = query.addSnapshotListener(listener)
    }

    fun stopListening() {
        listenerRegistration?.remove()
    }

    fun resumeListening() {
        if (listenerRegistration == null)
            return
        listenerRegistration = query.addSnapshotListener(listener)
    }

    fun initData(channelId: String) {
        query = FirebaseFirestore.getInstance()
            .collection("messages")
            .document(channelId)
            .collection("list-message")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        this.channelId = channelId
        this.fileName = "$CHAT_FILE$channelId"
    }

    fun loadData() {
        val data = loadData<String>(fileName)
        val loaded: List<Message>? = gson.fromJson(data, typeListMessage)
        loaded?.let {
            _listMessage.postValue(loaded)
        }
    }

    fun saveData() {
        saveData(fileName, gson.toJson(_listMessage.value?.takeLast(20), typeListMessage))
    }

    fun pushReact(messageId: String, reactId: Int) {
        val message = _listMessage.value?.find { it.id == messageId }
        val react = message!!.reactions
        chatRepository.pushReact(channelId, messageId, userId, react, reactId)
    }

    fun checkIdle(): Boolean {
        val timestamp = System.currentTimeMillis()

        val lastItem = listMessage.value?.lastOrNull()
        val lastTimestamp = lastItem?.timestamp ?: timestamp

        val timeIdle = timestamp - lastTimestamp
        val idleBreak = timeIdle > ChatFragment.TIME_IDLE_BREAK

        if (timestamp - lastTimestamp > ChatFragment.TIME_LINE_BREAK) {
            sendMessage(
                Message(Util.autoId(), isTimeline = true, timestamp = timestamp)
            )
        }
        return idleBreak
    }

    companion object {
        val TAG = "ChatViewModel"
        val CHAT_FILE = "channel_"
    }
}