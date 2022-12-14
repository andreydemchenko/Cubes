package ru.turbopro.cubes.ui.shop

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.turbopro.cubes.R
import ru.turbopro.cubes.data.Product
import ru.turbopro.cubes.data.UserData
import ru.turbopro.cubes.databinding.CartListItemBinding

class OrderProductsAdapter(
	private val context: Context, items: List<UserData.CartItem>,
	products: List<Product>, userLikes: List<String>
) : RecyclerView.Adapter<OrderProductsAdapter.ViewHolder>() {

	var data: List<UserData.CartItem> = items
	var proList: List<Product> = products
	var likesList: List<String> = userLikes

	inner class ViewHolder(private val binding: CartListItemBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(itemData: UserData.CartItem) {
			binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
			val proData = proList.find { it.productId == itemData.productId } ?: Product()
			binding.cartProductTitleTv.text = proData.name
			binding.cartProductPriceTv.text = proData.price.toString()
			if (proData.images.isNotEmpty()) {
				val imgUrl = proData.images[0].toUri().buildUpon().scheme("https").build()
				Glide.with(context)
					.asBitmap()
					.load(imgUrl)
					.into(binding.productImageView)
				binding.productImageView.clipToOutline = true
			}
			binding.cartProductDeleteBtn.visibility = View.GONE
			binding.cartProductQuantityButtonsLayout.visibility = View.GONE
			binding.cartProductLikeBtn.visibility = View.GONE
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
			CartListItemBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
		)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(data[position])
	}

	override fun getItemCount() = data.size
}