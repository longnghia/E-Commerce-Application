package com.ln.simplechat.ui.main

import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ln.simplechat.SimpleChatActivity.Companion.EXTRA_CURRENT_USER
import com.ln.simplechat.application.loadData
import com.ln.simplechat.application.saveData
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
    private val fileName = "$BOARD_FILE$userId"

    private val gson = Gson()
    private val typeListChannelAndMember = object : TypeToken<List<ChannelAndMember>>() {}.type

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


    fun restoreData(): List<ChannelAndMember>? {
        val data = loadData<String>(fileName)
        return gson.fromJson(data, typeListChannelAndMember)
    }

    fun saveData(data: List<ChannelAndMember>) {
        saveData(
            fileName,
            gson.toJson(data, typeListChannelAndMember)
        )
    }

    companion object {
        const val BOARD_FILE = "board_"
    }
}