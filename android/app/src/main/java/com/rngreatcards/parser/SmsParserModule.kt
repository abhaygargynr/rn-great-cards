package com.rngreatcards.parser

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.uimanager.ViewManager

class SmsParserModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val parser = SmsParser()

    override fun getName(): String = "SmsParserModule"

    @ReactMethod
    fun parseSms(samples: ReadableArray, promise: Promise) {
        try {
            val input = buildList {
                for (index in 0 until samples.size()) {
                    add(samples.getString(index).orEmpty())
                }
            }
            val results = parser.parseSms(input)
            promise.resolve(resultsToWritableArray(results))
        } catch (error: Exception) {
            promise.reject("PARSER_ERROR", error.message, error)
        }
    }

    private fun resultsToWritableArray(results: List<ParsedResult>): WritableArray {
        val array = Arguments.createArray()
        results.forEach { result ->
            array.pushMap(resultToMap(result))
        }
        return array
    }

    private fun resultToMap(result: ParsedResult): WritableMap {
        val map = Arguments.createMap()
        map.putString("rawSms", result.rawSms)
        map.putString("decision", result.decision.name)
        map.putString("excludeReason", result.excludeReason?.name)
        map.putDouble("confidence", result.confidence)
        if (result.transaction == null) {
            map.putNull("transaction")
        } else {
            val transaction = Arguments.createMap()
            transaction.putDouble("amount", result.transaction.amount)
            transaction.putString("currency", result.transaction.currency)
            transaction.putString("bank", result.transaction.bank)
            transaction.putString("cardLastFour", result.transaction.cardLastFour)
            transaction.putString("merchant", result.transaction.merchant)
            transaction.putString("type", result.transaction.type.name)
            transaction.putString("date", result.transaction.date)
            map.putMap("transaction", transaction)
        }
        return map
    }
}

class SmsParserPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> =
        listOf(SmsParserModule(reactContext))

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> =
        emptyList()
}
