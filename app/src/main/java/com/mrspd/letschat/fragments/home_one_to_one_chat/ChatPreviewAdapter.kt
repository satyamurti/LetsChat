package com.mrspd.letschat.fragments.home_one_to_one_chat

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mrspd.letschat.models.ChatParticipant
import com.mrspd.letschat.databinding.ItemChatOneToOneBinding

import java.util.*

var mQuery = ""


class ChatPreviewAdapter(private val clickListener: ClickListener) :
    ListAdapter<ChatParticipant, ChatPreviewAdapter.ViewHolder>(DiffCallbackUsers())
    , Filterable, OnQueryTextChange {


    var chatList = listOf<ChatParticipant>()
    var filteredChatList = mutableListOf<ChatParticipant>()


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemChatOneToOneBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ClickListener, chatParticipant: ChatParticipant) {
            println("ViewHolder.bind:")

            binding.chatParticipant = chatParticipant
            binding.clickListener = clickListener
            //if query text isn't empty_box set the selected text with sky blue+bold
            val username = chatParticipant.particpant?.username
            if (mQuery.isEmpty()) {
                binding.nameTextView.text = username
             } else {
                var index = username?.indexOf(mQuery, 0, true)!!
                val sb = SpannableStringBuilder(username)
                 while (index >= 0) {
                     val fcs = ForegroundColorSpan(Color.rgb(135, 206, 235))
                     sb.setSpan(
                         fcs,
                         index,
                         index + mQuery.length,
                         Spannable.SPAN_INCLUSIVE_INCLUSIVE
                     )
                     sb.setSpan(
                         StyleSpan(Typeface.BOLD),
                         index,
                         index + mQuery.length,
                         Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                     )
                     index = username.indexOf(mQuery, index + 1)
                 }
                binding.nameTextView.text = sb
             }

            binding.executePendingBindings()

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemChatOneToOneBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }


    override fun getFilter(): Filter {
           return object : Filter() {
               override fun performFiltering(charSequence: CharSequence): FilterResults {
                   val charString = charSequence.toString()
                   filteredChatList = mutableListOf()
                   if (charString.isEmpty()) {
                       filteredChatList = chatList as MutableList<ChatParticipant>


                   } else {
                       for (chatParticipant in chatList) {
                           if (chatParticipant.particpant?.username?.toLowerCase(Locale.ENGLISH)?.contains(
                                   charString.toLowerCase(Locale.ENGLISH)
                               )!!
                           ) {
                               filteredChatList.add(chatParticipant)
                           }
                       }
                   }
                   val filterResults = FilterResults()
                   filterResults.values = filteredChatList
                   return filterResults
               }

               override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {

                   val mutableList = filterResults.values as MutableList<ChatParticipant?>?
                   submitList(mutableList)
                   mutableList?.size?.let { notifyItemRangeChanged(0, it) }
                   notifyItemChanged(0)


               }
           }
       }
       //get search text from fragment using callback
       override fun onChange(query: String) {
           mQuery = query
       }


}

/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minumum number of changes between and old list and a new
 * list that's been passed to `submitList`.
 */
class DiffCallbackUsers : DiffUtil.ItemCallback<ChatParticipant>() {
    override fun areItemsTheSame(oldItem: ChatParticipant, newItem: ChatParticipant): Boolean {
        return oldItem.lastMessageDate == newItem.lastMessageDate
    }

    override fun areContentsTheSame(oldItem: ChatParticipant, newItem: ChatParticipant): Boolean {
        return oldItem == newItem
    }
}

class ClickListener(val clickListener: (chatParticipant: ChatParticipant) -> Unit) {
    fun onClick(chatParticipant: ChatParticipant) {
        return clickListener(chatParticipant)
    }
}


interface OnQueryTextChange {
    fun onChange(query: String)
}