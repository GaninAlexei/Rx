package com.school.rxhomework

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.annotations.SerializedName
import com.school.rxhomework.databinding.ActivityMainBinding
import com.school.rxhomework.databinding.ItemHolderBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import com.jakewharton.rxbinding4.swiperefreshlayout.refreshes

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<ActivityViewModel>()

    //compositeDisposable - собирает наши подписки
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val adapter = Adapter()

        ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)

            recyclerView.adapter = adapter
            viewModel.state.observe(this@MainActivity) { state ->
                when (state) {
                    State.Loading -> root.isRefreshing = true
                    is State.Loaded -> {
                        root.isRefreshing = false
                        adapter.submitList(state.content)
                    }
                }
            }

            viewModel.getStateObserver.onNext(Unit)
            compositeDisposable.add(root.refreshes().subscribe(viewModel.getStateObserver::onNext))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    class Adapter : ListAdapter<Adapter.Item, Adapter.Holder>(DiffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(parent)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position))
        }

        class Holder(private val binding: ItemHolderBinding) : RecyclerView.ViewHolder(binding.root) {
            constructor(parent: ViewGroup) : this(ItemHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

            fun bind(item: Item) {
                binding.apply {
                    titleTV.text = item.title
                    bodyTV.text = item.body
                }
            }
        }

        data class Item(
                @SerializedName("id")
                val id: Long,
                @SerializedName("title")
                val title: String,
                @SerializedName("body")
                val body: String
        )

        object DiffCallback : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }
        }
    }
}
