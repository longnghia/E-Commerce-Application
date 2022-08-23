package com.ln.simplechat.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ln.simplechat.model.ChannelAndMember
import com.ln.simplechat.repository.ChatRepository
import com.ln.simplechat.utils.MyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: ChatRepository) : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "DPql1uxYezTe4m6HrP0UMlm3Ikh2" // recheck

    val listChannelAndMembers = flow {
        emit(userId?.let { repository.getListChannelAndMember(it) })
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = MyResult.Loading
    ).asLiveData()


    fun searchChannel(query: String): List<ChannelAndMember>? {
        listChannelAndMembers.value.let {
            if (it is MyResult.Success) {
                val list = it.data.filter { it.channel.name.indexOf(query, ignoreCase = true) > -1 }
                return list
            } else
                return null
        }
    }
}