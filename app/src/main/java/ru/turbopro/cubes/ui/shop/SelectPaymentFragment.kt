package ru.turbopro.cubes.ui.shop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.turbopro.cubes.PAYMENT_METHOD_CASH_ON_DELIVERY
import ru.turbopro.cubes.PAYMENT_METHOD_DEBIT_CARD
import ru.turbopro.cubes.PAYMENT_METHOD_POINTS
import ru.turbopro.cubes.R
import ru.turbopro.cubes.databinding.FragmentSelectPaymentBinding
import ru.turbopro.cubes.viewmodels.OrderViewModel

class SelectPaymentFragment : Fragment() {

	private val TAG = SelectPaymentFragment::class.java.simpleName

	private lateinit var binding: FragmentSelectPaymentBinding
	private var methodsAdapter = PayByAdapter(getPaymentMethods())
	private val orderViewModel: OrderViewModel by activityViewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentSelectPaymentBinding.inflate(layoutInflater)

		setViews()
		return binding.root
	}

	private fun setViews() {
		binding.payByAppBar.topAppBar.title = getString(R.string.pay_by_title)
		binding.payByAppBar.topAppBar.setNavigationOnClickListener {
			findNavController().navigateUp()
		}
		binding.payByErrorTextView.visibility = View.GONE
		binding.payByPaymentsRecyclerView.adapter = methodsAdapter
		binding.payByNextBtn.text =
			getString(R.string.pay_by_next_btn_text, orderViewModel.getItemsPriceTotal().toString())
		binding.payByNextBtn.setOnClickListener {
			checkMoneyForOrder(methodsAdapter.lastCheckedMethod)
		}
	}

	private fun checkMoneyForOrder(method: String?) {
		if (method != null) {
			orderViewModel.getUserData()
			orderViewModel.setSelectedPaymentMethod(method)
			Log.d(TAG, "check money/points for order")
			binding.payByErrorTextView.visibility = View.GONE
			if (orderViewModel.checkIsEnoughMoney())
				navigateToOrderSuccess()
			else Toast.makeText(requireContext(),"You don't have enough points!", Toast.LENGTH_SHORT).show()
		} else {
			Log.d(TAG, "Error: Select a payment method!")
			binding.payByErrorTextView.visibility = View.VISIBLE
		}
	}

	private fun navigateToOrderSuccess() {
		Log.d(TAG, "navigate to order Success")
		// save order
		// wait for save add observer
		orderViewModel.finalizeOrder()
		// if success, navigate
		findNavController().navigate(R.id.action_selectPaymentFragment_to_orderSuccessFragment)
	}

	private fun getPaymentMethods(): List<String> {
		return listOf(PAYMENT_METHOD_POINTS, PAYMENT_METHOD_DEBIT_CARD, PAYMENT_METHOD_CASH_ON_DELIVERY)
	}
}