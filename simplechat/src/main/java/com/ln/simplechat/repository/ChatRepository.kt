package com.ln.simplechat.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ln.simplechat.model.Member
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRepository @Inject constructor() {
    private val dispatcherIO = Dispatchers.IO
    val db: FirebaseFirestore = Firebase.firestore

    suspend fun getListMember(listId: List<String>): List<Member> {
        val listMember: MutableList<Member> = mutableListOf()
        return withContext(dispatcherIO) {
            try {
                for (id in listId) {
                    val userTask = db.collection("users").document(id).get(Source.SERVER).await()
                    val user = userTask.toObject(Member::class.java) ?: continue
                    listMember.add(user)
                }
                listMember
            } catch (e: Exception) {
                Log.e("TAG", "getListMember: Fail ", e)
                emptyList()
            }
        }
    }
}
