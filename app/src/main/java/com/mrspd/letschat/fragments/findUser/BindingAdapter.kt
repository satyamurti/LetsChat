package com.mrspd.letschat.fragments.findUser

import android.text.SpannableString
import android.text.format.DateUtils
import android.text.style.UnderlineSpan
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.mrspd.letschat.R
import com.mrspd.letschat.models.ChatParticipant
import com.mrspd.letschat.models.GroupName
import com.mrspd.letschat.models.User
import com.mrspd.letschat.util.LoadState


@BindingAdapter("setRoundImage")
fun setRoundImage(imageView: ImageView, item: User) {
    item.let {
        val imageUri = it.profile_picture_url
        Glide.with(imageView.context)
            .load(imageUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.anonymous_profile)
                    .circleCrop()
            )
            .into(imageView)
    }

}

//github push

@BindingAdapter("formatDate")
fun formatDate(textView: TextView, timestamp: Timestamp?) {
    textView.text = timestamp?.seconds?.let { DateUtils.getRelativeTimeSpanString(it * 1000) }


}

@BindingAdapter("formatDateFromMap")
fun formatDateFromMap(textView: TextView, map: Map<String, Double>?) {
    var time = (map?.get("seconds"))
    if (time != null) {
        textView.text = DateUtils.getRelativeTimeSpanString(time.toLong() * 1000)
    }
}


@BindingAdapter("setLoadingState")
fun MaterialButton.setTheLoadingState(state: LoadState) {
    when (state) {
        LoadState.SUCCESS -> {
            setIconResource(R.drawable.ic_person_add_black_24dp)
        }
        LoadState.LOADING -> {
            setIconResource(R.drawable.loading_animation)
        }


    }

}


@BindingAdapter("setDuration")
fun setDuration(textView: TextView, timeinmillis: String?) {

    if (timeinmillis == null) return

    val h = (timeinmillis.toInt().div(3600000))
    val m = (timeinmillis.toInt().div(60000).rem(60))
    val s = (timeinmillis.toInt().div(1000).rem(60))

    val sp = when (h) {
        0 -> {
            StringBuilder().append(m).append(":").append(s)
        }
        else -> {
            StringBuilder().append(h).append(":").append(m).append(":").append(s)
        }
    }
    textView.text = sp
}


@BindingAdapter("setLastMessageText")
fun setLastMessageText(textView: TextView, chatParticipant: ChatParticipant) {

    //format last message to show like you:hello OR amr:Hi depending on sender OR you sent photo OR amr sent photo
    //depending on sender and is it text or image message

    if (chatParticipant.isLoggedUser!! && chatParticipant.lastMessageType == 0.0) {
        //format last message to show like you:hello
        textView.text = textView.context.getString(R.string.you, chatParticipant.lastMessage)

    } else if (!chatParticipant.isLoggedUser!! && chatParticipant.lastMessageType == 0.0) {
        //format last message to show like amr:hello
        textView.text = textView.context.getString(
            R.string.other,
            chatParticipant.particpant!!.username!!.split("\\s".toRegex())[0],
            chatParticipant.lastMessage
        )
    } else if (chatParticipant.isLoggedUser!! && chatParticipant.lastMessageType == 1.0) {
        //format last message to show like you sent an image
        textView.text = textView.context.getString(R.string.you_sent_image)
    } else if (!chatParticipant.isLoggedUser!! && chatParticipant.lastMessageType == 1.0) {
        //format last message to show like amr sent an image
        textView.text = textView.context.getString(
            R.string.other_image,
            chatParticipant.particpant!!.username!!.split("\\s".toRegex())[0]
        )
    } else if (!chatParticipant.isLoggedUser!! && chatParticipant.lastMessageType == 2.0) {
        //format last message to show like amr sent a file
        textView.text = textView.context.getString(
            R.string.other_file,
            chatParticipant.particpant!!.username!!.split("\\s".toRegex())[0]
        )
    } else if (chatParticipant.isLoggedUser!! && chatParticipant.lastMessageType == 2.0) {
        //format last message to show like you sent a file
        textView.text = textView.context.getString(R.string.you_sent_file)
    } else if (!chatParticipant.isLoggedUser!! && chatParticipant.lastMessageType == 3.0) {
        //format last message to show like amr sent a voice record
        textView.text = textView.context.getString(
            R.string.other_record,
            chatParticipant.particpant!!.username!!.split("\\s".toRegex())[0]
        )
    } else if (chatParticipant.isLoggedUser!! && chatParticipant.lastMessageType == 3.0) {
        //format last message to show like you  sent a voice record
        textView.text = textView.context.getString(R.string.you_sent_record)
    } else {

    }

}


@BindingAdapter("setRoundImageFromChatParticipant")
fun setRoundImageFromChatParticipant(imageView: ImageView, chatParticipant: ChatParticipant) {

    Glide.with(imageView.context)
        .load(chatParticipant.particpant!!.profile_picture_url)
        .apply(
            RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.anonymous_profile)
                .circleCrop()
        )
        .into(imageView)

}
@BindingAdapter("setRoundImageFromGroupName")
fun setRoundImageFromGroupName(imageView: ImageView, groupName: GroupName) {

    Glide.with(imageView.context)
        .load(groupName.imageurl)
        .apply(
            RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.anonymous_profile)
                .circleCrop()
        )
        .into(imageView)

}


@BindingAdapter("setChatImage")
fun setChatImage(imageView: ImageView, imageUri: String) {

    Glide.with(imageView.context)
        .load(imageUri)
        .apply(
            RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_poor_connection_black_24dp)
        )
        .into(imageView)

}

@BindingAdapter("setUnderlinedText")
fun setUnderlinedText(textView: TextView, text: String) {

    val content = SpannableString(text)
    content.setSpan(UnderlineSpan(), 0, content.length, 0)
    textView.text = content

}


