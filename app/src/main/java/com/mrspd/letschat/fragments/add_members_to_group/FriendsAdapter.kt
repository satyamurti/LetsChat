package com.mrspd.letschat.fragments.add_members_to_group

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrspd.letschat.databinding.CheckableListLayoutBinding
import com.mrspd.letschat.models.User
import kotlinx.android.synthetic.main.checkable_list_layout.view.*
import java.util.*


class FriendsAdapter(private val itemClickCallback: ItemClickCallback) :
    RecyclerView.Adapter<FriendsAdapter.UserHolder>() {


    private var mUsers = listOf<User>()


    fun setDataSource(users: List<User>?) {
        if (users != null) {
            mUsers = users
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {

        return UserHolder.from(
            parent
        )

    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val item = mUsers[position]

        holder.bind(item, itemClickCallback)
    }

    class UserHolder private constructor(val binding: CheckableListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var selectedItems: ArrayList<User>? = null
        var nonselectedItems: ArrayList<User>? = null
        fun bind(
            item: User,
            itemClickCallback: ItemClickCallback
        ) {

            binding.user = item
            binding.executePendingBindings()

            //callback to parent fragment when button clicked
            binding.txtTitle.setOnClickListener { view ->
                itemClickCallback.onItemClicked(item,view)
            }

        }

        public  fun returnTotalMembers(): ArrayList<User>? {
            return selectedItems
        }
        companion object {
            fun from(parent: ViewGroup): UserHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CheckableListLayoutBinding.inflate(layoutInflater, parent, false)

                return UserHolder(
                    binding
                )
            }
        }


    }


    interface ItemClickCallback {
        fun onItemClicked(user: User, view: View)

    }


}


