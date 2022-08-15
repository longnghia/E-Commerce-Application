package com.ln.simplechat.ui.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ln.simplechat.model.Member
import com.ln.simplechat.model.Message
import com.ln.simplechat.model.Status
import com.ln.simplechat.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val chatRepository: ChatRepository) : ViewModel() {

    private var listenerRegistration: ListenerRegistration? = null
    private var query: Query

    private var _listMessage = MutableLiveData<List<Message>>()
    val listMessage = _listMessage

    private var _sendStatus = MutableLiveData<Status>()
    val sendStatus = _listMessage

    private var _toastMessage = MutableLiveData<String?>()
    val toastMessage = _toastMessage

    private var _listMember = MutableLiveData<List<Member>>()
    val listMember = _listMember

    val db: FirebaseFirestore = Firebase.firestore

    fun sendTextMessage(channelId: String, message: Message) {
        if (message.text.isNullOrEmpty())
            return

        var messages = db.collection("messages")
        _sendStatus.postValue(Status.LOADING)
        messages.document(channelId).collection("list-message")
            .add(message)
            .addOnSuccessListener {
                _sendStatus.postValue(Status.SUCCESS)
            }
            .addOnFailureListener {
                _sendStatus.postValue(Status.FAIL)
                _toastMessage.postValue(it.message)
            }
    }

    val listener = object : EventListener<QuerySnapshot?> {
        override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
            if (error != null) {
                return
            }
            val messages = value?.toObjects(Message::class.java)
            _listMessage.postValue(messages)
        }
    }

    init {
        val channelId = "4be9efc7-69e1-47aa-990c-1ddaafaf0500"
        query = FirebaseFirestore.getInstance()
            .collection("messages")
            .document(channelId)
            .collection("list-message")
            .orderBy("timestamp", Query.Direction.ASCENDING)
    }

    fun getListMember(listUser: List<String>) {
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

    companion object {
        val TAG = "ChatViewModel"
    }
}