package net.poynt.developer.paymentfragmentsample;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import co.poynt.api.model.Card;
import co.poynt.api.model.CardType;
import co.poynt.api.model.FundingSourceAccountType;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionReference;
import co.poynt.api.model.TransactionReferenceType;
import co.poynt.os.contentproviders.orders.transactionreferences.TransactionreferencesColumns;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.model.PaymentStatus;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // request code for payment service activity
    private static final int COLLECT_PAYMENT_REQUEST = 13132;
    private String mReferenceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button payBtn = (Button) findViewById(R.id.payBtn);
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPayment();
            }
        });
    }

    private void launchPayment() {

        mReferenceId = UUID.randomUUID().toString();

        Payment payment = new Payment();
        // amount in cents
        payment.setAmount(1000l);
        
        TransactionReference posRefId = new TransactionReference();
        // "posReferenceId" is a predefined param which is searchable in Poynt HQ (merchant dashboard)
        posRefId.setCustomType("posReferenceId");
        posRefId.setType(TransactionReferenceType.CUSTOM);
        posRefId.setId(mReferenceId);

        payment.setReferences(Arrays.asList(posRefId));

        payment.setCurrency("USD");

        try {
            Intent collectPaymentIntent = new Intent(Intents.ACTION_COLLECT_PAYMENT);
            collectPaymentIntent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
            startActivityForResult(collectPaymentIntent, COLLECT_PAYMENT_REQUEST);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Poynt Payment Activity not found - did you install PoyntServices?", ex);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "Received onActivityResult (" + requestCode + ")");
        // Check which request we're responding to
        if (requestCode == COLLECT_PAYMENT_REQUEST) {
            logData("Received onActivityResult from Payment Action");
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {

                    // see the bottom of this file for full JSON of the Payment object
                    Payment payment = data.getParcelableExtra(Intents.INTENT_EXTRAS_PAYMENT);
                    if (payment != null) {
                        Gson gson = new Gson();
                        Type paymentType = new TypeToken<Payment>(){}.getType();
                        Log.d(TAG, gson.toJson(payment, paymentType));

                        // tip amounts may not be in the Transaction object as tip adjustment
                        // happens asynchronously, but Payment object carries the mapping between
                        // transaction UUID (transaction.getId) and the tip amount in cents
                        // Map<UUID, Long> tipAmounts =  payment.getTipAmounts();

                        for (Transaction t : payment.getTransactions()) {
                            Log.d(TAG, "Processor response: " + t.getProcessorResponse());
                        }

                        Log.d(TAG, "Received onPaymentAction from PaymentFragment w/ Status("
                                + payment.getStatus() + ")");
                        if (payment.getStatus().equals(PaymentStatus.COMPLETED)) {
                            logData("Payment Completed");
                        } else if (payment.getStatus().equals(PaymentStatus.DECLINED)) {
                            logData("Payment DECLINED");
                        } else if (payment.getStatus().equals(PaymentStatus.CANCELED)) {
                            logData("Payment Canceled");
                        } else if (payment.getStatus().equals(PaymentStatus.FAILED)) {
                            logData("Payment Failed");
                        } else if (payment.getStatus().equals(PaymentStatus.REFUNDED)) {
                            logData("Payment Refunded");
                        } else if (payment.getStatus().equals(PaymentStatus.VOIDED)) {
                            logData("Payment Voided");
                        } else {
                            logData("Payment Completed");
                        }
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                logData("Payment Canceled");
            }
        }
    }

    private void logData(String s) {
        Log.d(TAG, "logData: " + s);
    }
}


/*

PAYMENT object:

{
   "referenceId" : "8be638f0-015d-1000-c324-3148dc293b76",
   "authzOnly" : false,
   "disableManual" : false,
   "disableTip" : false,
   "tipAmount" : 250,
   "skipReceiptScreen" : false,
   "tipAmounts" : {
      "e5ac2522-7520-4bca-b34f-c72eddbd5b77" : 250
   },
   "disableDebit" : false,
   "currency" : "USD",
   "readCardDataOnly" : false,
   "disableChangeAmount" : false,
   "disableDebitCards" : false,
   "isBalanceInquiry" : false,
   "references" : [
      {
         "id" : "9f7c020e-f9f1-4a73-9b0c-6ef9fc86c7de",
         "customType" : "posReferenceId",
         "type" : "CUSTOM"
      }
   ],
   "disableEbtVoucher" : false,
   "skipPaymentConfirmationScreen" : false,
   "cashbackAmount" : 0,
   "adjustToAddCharges" : false,
   "nonReferencedCredit" : false,
   "disableCash" : false,
   "disableOther" : false,
   "disableEMVCL" : false,
   "disableEbtCashBenefits" : false,
   "voucher" : false,
   "disableDCC" : false,
   "manualEntry" : false,
   "disableEbtFoodStamps" : false,
   "cashOnly" : false,
   "creditOnly" : false,
   "amount" : 1000,
   "multiTender" : false,
   "disablePaymentOptions" : false,
   "skipSignatureScreen" : false,
   "applicationIndex" : 0,
   "offlineAuth" : false,
   "status" : "COMPLETED",
   "transactions" : [
      {
         "references" : [
            {
               "type" : "CUSTOM",
               "customType" : "referenceId",
               "id" : "8be638f0-015d-1000-c324-3148dc293b76"
            },
            {
               "id" : "9f7c020e-f9f1-4a73-9b0c-6ef9fc86c7de",
               "customType" : "posReferenceId",
               "type" : "CUSTOM"
            }
         ],
         "customerLanguage" : "en",
         "processorResponse" : {
            "statusCode" : "1",
            "transactionId" : "e5ac2522-7520-4bca-b34f-c72eddbd5b77",
            "approvalCode" : "105502",
            "statusMessage" : "Successful",
            "status" : "Successful",
            "processor" : "MOCK",
            "batchId" : "1",
            "approvedAmount" : 1000,
            "acquirer" : "CHASE_PAYMENTECH",
            "retrievalRefNum" : "e5ac2522-7520-4bca-b34f-c72eddbd5b77"
         },
         "amounts" : {
            "cashbackAmount" : 0,
            "orderAmount" : 1000,
            "transactionAmount" : 1000,
            "tipAmount" : 0,
            "currency" : "USD"
         },
         "signatureRequired" : false,
         "fundingSource" : {
            "debit" : false,
            "emvData" : {
               "emvTags" : {
                  "0x1F8160" : "03",
                  "0x1F8104" : "39343630",
                  "0x9F06" : "A000000025010901",
                  "0x1F815E" : "25",
                  "0x1F8103" : "333730323935",
                  "0x9F34" : "1F0002",
                  "0x95" : "8000001000",
                  "0x5F30" : "728F",
                  "0x1F815D" : "3C",
                  "0x1F8162" : "00",
                  "0x5F20" : "56414C55454420435553544F4D45522020202020203030363937",
                  "0x1F815F" : "04",
                  "0x9F39" : "91",
                  "0x57" : "F62A5810397052AAAA5DE98C4CDE68A3E123DA8A4AB46320EC82FDBE885F4EC9F5870F5DDFA94EC5",
                  "0x9F35" : "22",
                  "0x1F8102" : "FFFF9876543210E00494",
                  "0x1F8161" : "00",
                  "0x56" : "FA31EA1F973DE4E04F40DF2725138247CB66FE1D98251E0C280FB545A9E273801896801E6D276DA8E4C7FB7797CE1BB3A8496465C74BE416B64FAB810D9C22BA",
                  "0x5F24" : "210331"
               }
            },
            "entryDetails" : {
               "entryMode" : "CONTACTLESS_MAGSTRIPE",
               "customerPresenceStatus" : "PRESENT"
            },
            "type" : "CREDIT_DEBIT",
            "card" : {
               "expirationDate" : 31,
               "id" : 8757913,
               "numberLast4" : "9460",
               "cardHolderLastName" : "VALUED CUSTOMER      00697",
               "type" : "AMERICAN_EXPRESS",
               "serviceCode" : "728",
               "numberFirst6" : "370295",
               "expirationMonth" : 3,
               "numberMasked" : "370295******9460",
               "expirationYear" : 2021,
               "encrypted" : true,
               "cardHolderFirstName" : "",
               "cardHolderFullName" : "VALUED CUSTOMER      00697"
            }
         },
         "authOnly" : false,
         "createdAt" : {
            "minute" : 11,
            "dayOfMonth" : 28,
            "hourOfDay" : 18,
            "second" : 58,
            "year" : 2017,
            "month" : 6
         },
         "customerUserId" : 10307362,
         "action" : "AUTHORIZE",
         "id" : "e5ac2522-7520-4bca-b34f-c72eddbd5b77",
         "status" : "AUTHORIZED",
         "context" : {
            "sourceApp" : "co.poynt.services",
            "storeAddressTerritory" : "CA",
            "employeeUserId" : 1526454,
            "businessType" : "TEST_MERCHANT",
            "businessId" : "469e957c-57a7-4d54-a72a-9e8f3296adad",
            "mcc" : "5812",
            "transmissionAtLocal" : {
               "hourOfDay" : 18,
               "second" : 0,
               "month" : 6,
               "year" : 2017,
               "minute" : 12,
               "dayOfMonth" : 28
            },
            "storeDeviceId" : "urn:tid:df0f23b3-1d90-3466-91a9-0f2157da5687",
            "storeTimezone" : "America/Los_Angeles",
            "source" : "INSTORE",
            "storeId" : "c2855b41-1dd5-4ecc-8258-f0c89ae40338",
            "storeAddressCity" : "Palo Alto"
         },
         "updatedAt" : {
            "dayOfMonth" : 28,
            "minute" : 11,
            "month" : 6,
            "year" : 2017,
            "second" : 58,
            "hourOfDay" : 18
         }
      }
   ],
   "disableEMVCT" : false,
   "debitOnly" : false,
   "disableMSR" : false,
   "disableCheck" : false
}


*/

