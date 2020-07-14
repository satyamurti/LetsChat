package com.mrspd.letschat.fragments.findUser

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
import com.mrspd.letschat.databinding.UserItemBinding
import com.mrspd.letschat.models.User
import java.util.*


var mQuery = ""


class UserAdapter(private val clickListener: UserClickListener) :
    ListAdapter<User, UserAdapter.ViewHolder>(DiffCallbackUsers()), Filterable,
    OnQueryTextChange {


    var userList = mutableListOf<User?>()
    var filteredUserList = mutableListOf<User?>()


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: UserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: UserClickListener, item: User) {
            println("ViewHolder.bind:")

            val userName: String = item.username.toString()

            //if query text isn't empty_box set the selected text with sky blue+bold
            if (mQuery.isEmpty()) {
                binding.usernameTextView.text = userName
            } else {
                var index = userName.indexOf(mQuery, 0, true)
                val sb = SpannableStringBuilder(userName)
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
                    index = userName.indexOf(mQuery, index + 1)
                }
                binding.usernameTextView.text = sb
            }
            binding.clickListener = clickListener
            binding.user = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = UserItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                filteredUserList = mutableListOf()
                if (charString.isEmpty()) {
                    filteredUserList = userList


                } else {
                    for (user in userList) {
                        if (user?.username?.toLowerCase(Locale.ENGLISH)?.contains(
                                charString.toLowerCase(Locale.ENGLISH)
                            )!!
                        ) {
                            filteredUserList.add(user)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredUserList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {

                val mutableList = filterResults.values as MutableList<User?>
                submitList(mutableList)
                notifyItemRangeChanged(0, mutableList.size)
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
class DiffCallbackUsers : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}

class UserClickListener(val clickListener: (user: User) -> Unit) {
    fun onClick(user: User) {
        return clickListener(user)
    }
}


interface OnQueryTextChange {
    fun onChange(query: String)
}