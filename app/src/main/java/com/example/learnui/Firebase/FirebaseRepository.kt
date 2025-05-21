package com.example.learnui.Firebase

import com.example.learnui.DataClass.Topic
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseRepository{

    fun fetchSubjects(onResult: (List<String>) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("ARVidya")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val subjectList = mutableListOf<String>()
                for(subjectSnapshot in snapshot.children){
                    subjectList.add(subjectSnapshot.key ?: "")
                }
                onResult(subjectList)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun fetchTopics(subject: String, onResult: (List<Topic>) -> Unit){
        val database = FirebaseDatabase.getInstance().getReference("ARVidya/$subject")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val topicList = mutableListOf<Topic>()
                for(topicSnap in snapshot.children){
                    topicSnap.getValue(Topic::class.java)?.let {
                        topicList.add(it)
                    }
                }
                onResult(topicList)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}