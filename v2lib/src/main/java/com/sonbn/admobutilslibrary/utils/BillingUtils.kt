package com.sonbn.admobutilslibrary.utils

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BillingUtils {
    companion object {
        private var instance: BillingUtils? = null
        fun getInstance(): BillingUtils {
            if (instance == null) {
                instance = BillingUtils()
            }
            return instance!!
        }
    }

    private var productDetailsList: MutableList<ProductDetails?> = mutableListOf()

    private var billingClient: BillingClient? = null
    private var onPurchaseCompleteListener: ((Purchase) -> Unit)? = null

    fun init(activity: Activity) {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    fun setOnPurchaseCompleteListener(listener: (Purchase) -> Unit) {
        this.onPurchaseCompleteListener = listener
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    fun fetchProductList(
        productIds: List<Pair<String, String>>,
        onProductDetails: (List<ProductDetails?>) -> Unit = {}
    ) {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->  }).launch {
                        val productDetailsDeferred = productIds.map { pair ->
                            async {
                                fetchProductDetails(pair)
                            }
                        }
                        val productDetailsList = productDetailsDeferred.awaitAll().flatten()
                        onProductDetails(productDetailsList)
                        this@BillingUtils.productDetailsList = productDetailsList.toMutableList()
                    }

                } else {

                }
            }

            override fun onBillingServiceDisconnected() {

            }
        })
    }

    private suspend fun fetchProductDetails(productId: Pair<String, String>): List<ProductDetails?> {
        return withContext(Dispatchers.IO) {
            val productList: MutableList<QueryProductDetailsParams.Product> = mutableListOf()
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId.first)
                    .setProductType(productId.second)
                    .build()
            )

            val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val result = mutableListOf<ProductDetails?>()
            val latch = java.util.concurrent.CountDownLatch(1)

            billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    result.addAll(productDetailsList)
                } else {

                }
                latch.countDown()
            }

            latch.await()
            return@withContext result
        }
    }

    fun getProductDetailsList(): MutableList<ProductDetails?> {
        return productDetailsList
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient!!.acknowledgePurchase(
                    acknowledgePurchaseParams
                ) { billingResult: BillingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        onPurchaseCompleteListener?.invoke(purchase)
                    }
                }
            } else {
                onPurchaseCompleteListener?.invoke(purchase)
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList: MutableList<ProductDetailsParams> = mutableListOf(
            if (productDetails.productType == BillingClient.ProductType.SUBS){
                ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(productDetails.subscriptionOfferDetails?.get(0)?.offerToken?: "")
                    .build()
            }else{
                ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            }
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    fun queryPurchase(type: String = BillingClient.ProductType.SUBS) {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(type)
                .build()
        ) { billingResult: BillingResult, purchases: List<Purchase?> ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase != null) {
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }

    suspend fun restorePurchases(): List<Purchase> = coroutineScope {
        val subs = async { queryPurchases(BillingClient.ProductType.SUBS) }
        val inapp = async { queryPurchases(BillingClient.ProductType.INAPP) }
        return@coroutineScope subs.await() + inapp.await()
    }

    private suspend fun queryPurchases(type: String): List<Purchase> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Purchase>()
        val latch = java.util.concurrent.CountDownLatch(1)

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    billingClient?.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder().setProductType(type).build()
                    ) { billingResult1: BillingResult, list: List<Purchase> ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            result.addAll(list)

                        }
                        latch.countDown()
                    }
                } else {
                    latch.countDown()
                }
            }
        })

        latch.await() // Wait for the query to complete
        return@withContext result
    }
}

fun ProductDetails.getPrice(): String{
    return if (this.productType == BillingClient.ProductType.SUBS){
        val size = this.subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.size?: 0
        this.subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(size)?.formattedPrice?: ""
    }else{
        this.oneTimePurchaseOfferDetails?.formattedPrice?: ""
    }
}
