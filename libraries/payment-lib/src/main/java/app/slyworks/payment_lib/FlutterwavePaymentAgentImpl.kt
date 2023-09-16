package app.slyworks.payment_lib

import android.app.Activity
import app.slyworks.utils_lib.Outcome
import com.flutterwave.raveandroid.rave_presentation.RaveNonUIManager
import com.flutterwave.raveandroid.rave_presentation.card.Card
import com.flutterwave.raveandroid.rave_presentation.card.CardPaymentCallback
import com.flutterwave.raveandroid.rave_presentation.card.CardPaymentManager

class FlutterwavePaymentAgentImpl : PaymentAgent {

    override suspend fun pay(activity: Activity?, details: PaymentDetails): Outcome {
        /*val raveManager: RaveNonUIManager =
            RaveNonUIManager()
                .onStagingEnv()
                .setPublicKey()
                .setEncryptionKey()
                .setIsPreAuth()
                .setCurrency()
                .setAmount()
                .setEmail()
                .setfName()
                .setlName()
                .setNarration()
                .setTxRef()
                .initialize()

        val cardPaymentManager: CardPaymentManager =
            CardPaymentManager(raveManager,
                object : CardPaymentCallback {
                    override fun showProgressIndicator(active: Boolean) {
                        TODO("Not yet implemented")
                    }

                    override fun collectCardPin() {
                        cardPaymentManager.submitPin(pin)
                    }

                    override fun collectOtp(message: String?) {
                        cardPayManager.submitOtp(otp)
                    }

                    override fun collectAddress() {
                        TODO("Not yet implemented")
                    }

                    override fun showAuthenticationWebPage(authenticationUrl: String?) {

                    }

                    override fun onError(errorMessage: String?, flwRef: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onSuccessful(flwRef: String?) {
                        TODO("Not yet implemented")
                    }
                })

        val card: Card = Card("", "", "", "")

        cardPaymentManager.chargeCard(card)*/
        return Outcome.SUCCESS(Unit)
    }
}