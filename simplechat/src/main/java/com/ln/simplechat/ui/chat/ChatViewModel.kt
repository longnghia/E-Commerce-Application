package com.ln.simplechat.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ln.simplechat.model.Channel
import com.ln.simplechat.model.Member
import com.ln.simplechat.model.Message
import com.ln.simplechat.model.Status
import com.ln.simplechat.repository.ChatRepository
import com.ln.simplechat.utils.MyResult
import com.ln.simplechat.utils.toChatMedia
import com.luck.picture.lib.entity.LocalMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val chatRepository: ChatRepository) : ViewModel() {

    private lateinit var channelId: String
    private lateinit var query: Query

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "DPql1uxYezTe4m6HrP0UMlm3Ikh2" // recheck

    private var listenerRegistration: ListenerRegistration? = null

    private var _listMessage = MutableLiveData<List<Message>>()
    val listMessage: LiveData<List<Message>> = _listMessage

    private var _sendStatus = MutableLiveData<Status>()
    val sendStatus: LiveData<Status> = _sendStatus

    private var _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private var _listMember = MutableLiveData<List<Member>>()
    val listMember: LiveData<List<Member>> = _listMember

    private val db: FirebaseFirestore = Firebase.firestore
    private val messages = db.collection("messages")
    private var listener: EventListener<QuerySnapshot?>

    init {
        listener = object : EventListener<QuerySnapshot?> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    return
                }
                val messages = value?.toObjects(Message::class.java)
                _listMessage.postValue(messages)
            }
        }
    }

    fun sendMessage(channelId: String, message: Message, callback: ((DocumentReference) -> Unit)? = null) {
        _sendStatus.postValue(Status.LOADING)
        messages.document(channelId).collection("list-message")
            .add(message)
            .addOnSuccessListener {
                callback?.invoke(it)
                _sendStatus.postValue(Status.SUCCESS)
            }
            .addOnFailureListener {
                Log.e(TAG, "sendMessage: ERROR", it)
                _sendStatus.postValue(Status.FAIL)
                _toastMessage.postValue(it.message)
            }
    }

    fun sendImagesMessage(result: ArrayList<LocalMedia>) {
        if (result.isEmpty()) return

        val tmpList = result.map { it.toChatMedia() }
        val tmpMessage = Message(userId, listImageUrl = tmpList)
        sendMessage(channelId, tmpMessage) { ref ->
            uploadImage(result) { listImg ->
                val list = tmpList.mapIndexed { index, chatMedia ->
                    chatMedia.apply { path = listImg[index] }
                }
                tmpMessage.listImageUrl = list
                ref.set(tmpMessage)
            }
        }
    }

    private fun uploadImage(result: ArrayList<LocalMedia>, callback: (List<String>) -> Unit) {
        Log.d(TAG, "uploadImage")
        viewModelScope.launch(Dispatchers.Main) {
            val uploadResult = chatRepository.uploadImage(result, channelId)
            when (uploadResult) {
                is MyResult.Success -> {
                    val listImg = uploadResult.data
                    callback(listImg)
                }
                is MyResult.Error -> toastMessage
            }
        }
    }

    private fun getListMember(listUser: List<String>) {
        viewModelScope.launch(Dispatchers.Main) {
            val list = chatRepository.getListMember(listUser)
            _listMember.postValue(list)
            listenerRegistration = query.addSnapshotListener(listener)
        }
    }

    fun startListening() {
        if (listenerRegistration == null) return
        query.addSnapshotListener(listener)
    }

    fun stopListening() {
        listenerRegistration?.remove()
    }

    fun initData(channel: Channel) {
        channelId = channel.id

        query = FirebaseFirestore.getInstance()
            .collection("messages")
            .document(channelId)
            .collection("list-message")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        getListMember(channel.listUser)
    }

    companion object {
        val TAG = "ChatViewModel"
    }
}