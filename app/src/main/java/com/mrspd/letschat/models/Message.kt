package com.mrspd.letschat.models

import com.google.firebase.Timestamp


interface Message {
    val from: String?
    val created_at: Timestamp?
    val type: Double?
    val to: String?
    val senderName: String?
}


/**0*/
data class TextMessage(
    override val from: String?,
    override val created_at: Timestamp?,
    override val type: Double?,
    override val to: String?,
    override val senderName: String?,
    val text: String?

) : Message


/**1*/
data class ImageMessage(

    override val from: String?,
    override val created_at: Timestamp?,
    override val type: Double?,
    override val to: String?,
    override val senderName: String?,
    val uri: String?

) : Message


/**2*/
data class FileMessage(
    override val from: String?,
    override val created_at: Timestamp?,
    override val type: Double?,
    override val to: String?,
    override val senderName: String?,
    val name: String?,
    val uri: String?
) : Message

/**3*/
data class RecordMessage(

    override val from: String?,
    override val created_at: Timestamp?,
    override val type: Double?,
    override val to: String?,
    override val senderName: String?,
    var duration: String?,
    val uri: String?,
    var currentProgress: String?,
    var isPlaying: Boolean?

) : Message



