package com.ln.simplechat.ui.main

import androidx.lifecycle.*
import com.ln.simplechat.SimpleChatActivity.Companion.EXTRA_CURRENT_USER
import com.ln.simplechat.model.ChannelAndMember
import com.ln.simplechat.repository.ChatRepository
import com.ln.simplechat.utils.MyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), LifecycleObserver {

    val userId = savedStateHandle.get<String>(EXTRA_CURRENT_USER)

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