package com.sawwere.makeitso.data.model

import com.google.firebase.firestore.DocumentId

data class TodoItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val priority: Int = Priority.LOW.value,
    val completed: Boolean = false,
    val flagged: Boolean = false,
    val ownerId: String = ""
)

val TodoItem.isHighPriority: Boolean
    get() = this.priority == Priority.HIGH.value

val TodoItem.isMediumPriority: Boolean
    get() = this.priority == Priority.MEDIUM.value

val TodoItem.isLowPriority: Boolean
    get() = this.priority == Priority.LOW.value

