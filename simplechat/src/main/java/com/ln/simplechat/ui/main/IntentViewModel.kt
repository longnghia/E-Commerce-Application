package com.ln.simplechat.ui.main

import androidx.lifecycle.*
import com.ln.simplechat.ChannelNotFoundException
import com.ln.simplechat.SimpleChatActivity.Companion.EXTRA_CURRENT_USER
import com.ln.simplechat.repository.ChatRepository
import com.ln.simplechat.utils.MyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntentViewModel @Inject constructor(
    private val repository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), LifecycleObserver {

    val userId = savedStateHandle.get<String>(EXTRA_CURRENT_USER)
    val channelCreated = MutableLiveData<MyResult<String>>()

    fun createChannel(listUser: ArrayList<String>) {
        channelCreated.value = MyResult.Loading
        viewModelScope.launch {
            when (val channelFound = repository.findChannelByListUser(listUser)) {
                is MyResult.Success -> {
                    channelCreated.value = MyResult.Success(channelFound.data.id)
                }
                is MyResult.Error -> {
                    if (channelFound.exception is ChannelNotFoundException) {
                        channelCreated.postValue(repository.createChannel(listUser))
                    } else
                        channelCreated.value = MyResult.Error(channelFound.exception)
                }
                else -> {}
            }
        }
    }
}