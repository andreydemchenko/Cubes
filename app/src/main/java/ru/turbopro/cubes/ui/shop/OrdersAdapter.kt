package ru.turbopro.cubes.ui.shop

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.turbopro.cubes.R
import ru.turbopro.cubes.data.UserData
import ru.turbopro.cubes.databinding.LayoutOrderSummaryCardBinding
import java.time.Month
import java.util.*

class OrdersAdapter(ordersList: List<UserData.OrderItem>, private val context: Context) :
	RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

	lateinit var onClickListener: OnClickListener
	var data: List<UserData.OrderItem> = ordersList

	inner class ViewHolder(private val binding: LayoutOrderSummaryCardBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(orderData: UserData.OrderItem) {
			binding.orderSummaryCard.setOnClickListener { onClickListener.onCardClick(orderData.orderId) }
			binding.orderSummaryIdTv.text = orderData.orderId
			val calendar = Calendar.getInstance()
			calendar.time = orderData.orderDate
			binding.orderSummaryDateTv.text =
				context.getString(
					R.string.order_date_text,
					Month.values()[(calendar.get(Calendar.MONTH))].name,
					calendar.get(Calendar.DAY_OF_MONTH).toString(),
					calendar.get(Calendar.YEAR).toString()
				)
			binding.orderSummaryStatusValueTv.text = orderData.status
			val totalItems = orderData.items.map { it.quantity }.sum()
			binding.orderSummaryItemsCountTv.text =
				context.getString(R.string.order_items_count_text, totalItems.toString())
			var totalAmount = 0.0
			orderData.itemsPrices.forEach { (itemId, price) ->
				totalAmount += price * (orderData.items.find { it.itemId == itemId }?.quantity ?: 1)
			}
			binding.orderSummaryTotalAmountTv.text = totalAmount.toString()
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
			LayoutOrderSummaryCardBinding.inflate(
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

	interface OnClickListener {
		fun onCardClick(orderId: String)
	}
}