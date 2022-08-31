package com.ln.simplechat.model

data class Reaction(
    var love: List<String> = emptyList(),
    var haha: List<String> = emptyList(),
    var angry: List<String> = emptyList(),
    var like: List<String> = emptyList(),
    var wow: List<String> = emptyList(),
    var sad: List<String> = emptyList(),
) {
    companion object {
        val reactOrder = listOf("love", "haha", "wow", "sad", "angry", "like")
    }

    fun getNonEmptyReaction(): List<Pair<Int, List<String>>> {
        val list = mutableListOf<Pair<Int, List<String>>>()
        reactOrder.forEachIndexed { index, r ->

            val listUserReact = getArray(r)
            if (listUserReact.isNotEmpty())
                list.add(Pair(index, listUserReact))
        }
        return list
    }

    fun getArray(reactName: String): List<String> {
        return when (reactName) {
            "love" -> love
            "haha" -> haha
            "wow" -> wow
            "sad" -> sad
            "angry" -> angry
            else -> like
        }
    }
}


