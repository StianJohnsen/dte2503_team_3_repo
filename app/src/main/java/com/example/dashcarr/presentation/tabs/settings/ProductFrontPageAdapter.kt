package com.example.dashcarr.presentation.tabs.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dashcarr.databinding.ProductInfoItemBinding

class ProductFrontPageAdapter: ListAdapter
<ProductInfo, ProductFrontPageAdapter.ProductFrontPageViewHolder>(DiffCallback)
{

    companion object DiffCallback: DiffUtil.ItemCallback<ProductInfo>(){
        override fun areItemsTheSame(oldItem: ProductInfo, newItem: ProductInfo): Boolean {
            return oldItem.info == newItem.info
        }

        override fun areContentsTheSame(oldItem: ProductInfo, newItem: ProductInfo): Boolean {
            return oldItem.id == newItem.id
        }
    }

    class ProductFrontPageViewHolder(private var binding: ProductInfoItemBinding):
            RecyclerView.ViewHolder(binding.root){

                fun bind(productInfo: ProductInfo){
                    binding.productInfo = productInfo
                    binding.executePendingBindings()
                }
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductFrontPageViewHolder {
        return ProductFrontPageViewHolder(ProductInfoItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ProductFrontPageViewHolder, position: Int) {
        val productFrontPage = getItem(position)
        holder.bind(productFrontPage)
    }
}