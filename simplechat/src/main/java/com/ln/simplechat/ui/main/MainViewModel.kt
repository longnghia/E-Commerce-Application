package com.ln.simplechat.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ln.simplechat.repository.ChatRepository
import com.ln.simplechat.utils.MyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: ChatRepository) : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid!! // recheck

    val listChannel = flow {
        emit(userId.let { repository.getListChannel(it) })
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = MyResult.Loading
    )
}