package com.paulcoding.pindownloader.ui

import androidx.lifecycle.ViewModel
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.paulcoding.pindownloader.App.Companion.appContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PremiumViewModel : ViewModel() {
    lateinit var billingClient: BillingClient

    private var _uiStateFlow = MutableStateFlow(UiState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val exception: Throwable? = null,
    )

    fun setError(th: Throwable?) {
        _uiStateFlow.update { it.copy(exception = th) }
    }

    private val sku = "your_premium_feature_product_id"

    init {
        billingClient = BillingClient.newBuilder(appContext)
            .setListener { billingResult, purchases ->
                handlePurchase(billingResult, purchases)
            }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()

        startBillingClientConnection()
    }

    private fun handlePurchase(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
//                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//                    _isPremiumUser.postValue(true)
//                    if (!purchase.isAcknowledged) {
//                        val ackParams = AcknowledgePurchaseParams.newBuilder()
//                            .setPurchaseToken(purchase.purchaseToken)
//                            .build()
//                        billingClient.acknowledgePurchase(ackParams) { billingResult ->
//                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                            }
//                        }
//                    }
//                }
            }
        }
    }

     fun startBillingClientConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection or notify the user
            }
        })
    }

    private fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(sku)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ))
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Store product details and update UI if needed
            }
        }
    }
}