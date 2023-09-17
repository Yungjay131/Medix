package app.slyworks.payment_lib

import android.app.Activity
import android.content.Context
import app.slyworks.utils_lib.KEY_FAILED_TRANSACTIONS
import app.slyworks.utils_lib.Outcome
import app.slyworks.utils_lib.PreferenceManager
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

const val CARD_NUMBER = "cardNumber"
const val CARD_EXPIRY_MONTH = "cardExpiryMonth"
const val CARD_EXPIRY_YEAR = "cardExpiryYear"
const val CARD_CVC = "cardCVC"
const val AMOUNT_IN_KOBO = "amountInKobo"
const val CUSTOMER_NAME = "customerName"
const val CUSTOMER_EMAIL = "customerEmail"
class PaystackPaymentAgentImpl(
    private val apiKey:String,
    private val preferenceManager: PreferenceManager,
    applicationContext: Context
) : PaymentAgent {

    init {
        PaystackSdk.initialize(applicationContext)
        PaystackSdk.setPublicKey(apiKey)
    }

    override suspend fun pay(activity: Activity?, details: PaymentDetails): Outcome =
        suspendCancellableCoroutine<Outcome> { continuation ->
            var isTransactionInProgress: Boolean = false

            var transactionReference: String? = null

            val card: Card =
                Card(
                    details.getDetails()[CARD_NUMBER] as String,
                    details.getDetails()[CARD_EXPIRY_MONTH] as Int,
                    details.getDetails()[CARD_EXPIRY_YEAR] as Int,
                    details.getDetails()[CARD_CVC] as String,
                    details.getDetails()[CUSTOMER_NAME] as String
                )

            val charge: Charge =
                Charge().apply {
                    setAmount(details.getDetails()[AMOUNT_IN_KOBO] as Int)
                    setEmail(details.getDetails()[CUSTOMER_EMAIL] as String)
                    setCard(card)
                }

            PaystackSdk.chargeCard(activity, charge,
                object : Paystack.TransactionCallback {
                    override fun onSuccess(transaction: Transaction?) {
                        isTransactionInProgress = false
                        continuation.resume(Outcome.SUCCESS(Unit))
                    }

                    override fun beforeValidate(transaction: Transaction?) {
                        isTransactionInProgress = true

                        transactionReference = transaction?.reference
                    }

                    override fun onError(error: Throwable?, transaction: Transaction?) {
                        isTransactionInProgress = false
                        continuation.resume(Outcome.FAILURE(transaction?.reference, error?.message))
                    }
                })

            continuation.invokeOnCancellation {
                if (!isTransactionInProgress)
                    return@invokeOnCancellation

                if (transactionReference == null)
                    return@invokeOnCancellation

                val failedTransactions: MutableList<String> =
                    preferenceManager.get(KEY_FAILED_TRANSACTIONS, mutableListOf<String>())!!
                failedTransactions.add(transactionReference!!)

                preferenceManager.set(KEY_FAILED_TRANSACTIONS, failedTransactions)
            }
        }
}