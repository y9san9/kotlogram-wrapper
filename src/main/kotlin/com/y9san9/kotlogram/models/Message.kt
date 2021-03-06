package com.y9san9.kotlogram.models

import com.github.badoualy.telegram.api.utils.*
import com.github.badoualy.telegram.tl.api.*
import com.y9san9.kotlogram.KotlogramClient
import com.y9san9.kotlogram.models.markup.ReplyMarkup
import com.y9san9.kotlogram.models.markup.wrap
import com.y9san9.kotlogram.models.media.Media
import com.y9san9.kotlogram.models.media.wrap

fun TLAbsMessage.wrap(client: KotlogramClient) : Message = when(this){
    is TLMessage -> Message(client, this, null)
    is TLMessageService -> Message(client, TLMessage(
        out,
        mentioned,
        mediaUnread,
        silent,
        post,
        id,
        fromId,
        toId,
        null,
        null,
        replyToMsgId,
        date,
        null,
        null,
        null,
        null,
        null,
        null
    ), action)
    is TLMessageEmpty -> Message(client, TLMessage(
        false,
        false,
        false,
        false,
        false,
        id,
        fromId,
        toId,
        null,
        null,
        replyToMsgId,
        date,
        null,
        null,
        null,
        null,
        null,
        null
    ), null)
    else -> throw UnsupportedOperationException()
}

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class Message(
    val client: KotlogramClient,
    val source: TLMessage,
    val action: TLAbsMessageAction?
){
    val id = source.id
    val isService = action == null
    val out = source.out
    val mentioned = source.mentioned
    val mediaUnread = source.mediaUnread
    val silent = source.silent
    val post = source.post
    val from by lazy {
        client.getUser(source.fromId)
            ?: throw UnsupportedOperationException("Cannot find owner by id ${source.fromId}")
    }
    val to by lazy {
        source.toId.wrap(client).entity
    }
    val fwdFrom: TLMessageFwdHeader? = source.fwdFrom
    val viaBot by lazy {
        client.getUser(source.viaBotId)
            ?: throw UnsupportedOperationException("Cannot find via bot by id ${source.viaBotId}")
    }
    val isReply = source.isReply
    val replyToMsgId: Int? = source.replyToMsgId
    val reply by lazy { client.getMessage(source.replyToMsgId ?: return@lazy null) }
    val date = source.date
    val message: String? = source.message
    val media: Media? = source.media?.wrap(client)
    val replyMarkup = source.replyMarkup?.wrap(client, this)
    val entities: List<TLAbsMessageEntity>? = source.entities
    val views: Int? = source.views
    val editDate: Int? = source.editDate

    fun reply(
        text: String = "",
        silent: Boolean = false,
        clearDraft: Boolean = true,
        replyMarkup: ReplyMarkup? = null,
        media: List<Media> = listOf(),
        scheduledDate: Int? = null,
        entities: List<TLAbsMessageEntity> = listOf()
    ) = client.sendMessage(
        if(to.peer.isUser && !out) from else to,
        text, silent, clearDraft, id, replyMarkup, media, scheduledDate, entities
    )

    fun edit(
        text: String? = null,
        replyMarkup: ReplyMarkup? = null,
        entities: Array<TLAbsMessageEntity>
    ) = client.editMessage(to, id, text, replyMarkup, entities)

    fun delete(deleteForAll: Boolean = true) = client.deleteMessage(deleteForAll, id)

}