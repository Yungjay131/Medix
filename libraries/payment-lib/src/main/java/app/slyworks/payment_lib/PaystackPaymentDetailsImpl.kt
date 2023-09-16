package app.slyworks.payment_lib

data class PaystackPaymentDetailsImpl(private val cardNumber:String,
                                      private val cardExpiryMonth:Int,
                                      private val cardExpiryYear:Int,
                                      private val cardCVC:String,
                                      private val amountInKobo:Int,
                                      private val customerName:String,
                                      private val customerEmail:String ) : PaymentDetails {

    override fun getDetails(): HashMap<String, Any> =
        hashMapOf(
            CARD_NUMBER to cardNumber,
            CARD_EXPIRY_MONTH to cardExpiryMonth,
            CARD_EXPIRY_YEAR to cardExpiryYear,
            CARD_CVC to cardCVC,
            AMOUNT_IN_KOBO to amountInKobo,
            CUSTOMER_NAME to customerName,
            CUSTOMER_EMAIL to customerEmail )
}