package com.ln.simplechat.observer.chat

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ln.simplechat.ui.chat.ChatAdapter

class ChatAdapterObserver(
    private val recycler: RecyclerView,
    private val adapter: ChatAdapter,
    private val manager: LinearLayoutManager,
    private val onUnseenMessage: (Int) -> Unit
) : RecyclerView.AdapterDataObserver() {
    var unSeenMessages = 0
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        val count = adapter.itemCount
        val lastVisiblePosition = manager.findLastCompletelyVisibleItemPosition()
        val loading = lastVisiblePosition == -1
        val atBottom = positionStart >= count - 1 && lastVisiblePosition == positionStart - 1
        adapter.notifyItemRangeChanged(positionStart - 1, itemCount + 1)
        if (loading || atBottom) {
            unSeenMessages = 0
            recycler.scrollToPosition(positionStart)
        } else {
            unSeenMessages += itemCount
        }
        onUnseenMessage(unSeenMessages)
    }
}
