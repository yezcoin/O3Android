package network.o3.o3wallet.API.O3Platform

import com.google.gson.JsonElement
import org.json.JSONObject
import java.math.BigDecimal

/**
 * Created by drei on 11/24/17.
 */
data class TransactionHistoryEntry(val GAS: Double,
                                   val NEO: Double,
                                   val block_index: Int,
                                   val gas_sent: Boolean,
                                   val neo_sent: Boolean,
                                   val txid: String
                                   )

data class TransactionHistory(val address: String,
                              val history: Array<TransactionHistoryEntry>,
                              val name: String,
                              val net: String)


data class PlatformResponse(val code: Int, val result: JsonElement)

data class Claim(val claim: Int,
                 val end: Int,
                 val index: Int,
                 val start: Int,
                 val sysfee: Int,
                 val txid: String,
                 val value: Int)


data class TokenListingsData(val data: TokenListings)

data class TokenListings(val nep5tokens: Array<TokenListing>)

data class TokenListing(val logoURL: String,
                        val logoSVG: String,
                        val webURL: String,
                        val tokenHash: String,
                        val name: String,
                        val symbol: String,
                        val decimal: Int,
                        val totalSupply: Int)

data class ClaimData(val data: Claimable)

data class Claimable(val gas: String, val claims: Array<UTXO>)

data class UTXOS(val data: Array<UTXO>)

data class UTXO(val asset: String, val index: Int, val txid: String, val value: String,  val createdAtBlock: Int)

data class TransferableBalanceData(val data: TransferableBalances)
data class TransferableBalances(val version: String, val address: String, val scriptHash: String, val assets: Array<TransferableBalance>,
                                val nep5Tokens: Array<TransferableBalance>,
                                val ontology: Array<TransferableBalance>)

data class TransferableBalance(val id: String, val name: String, val value: String, val symbol: String, val decimals: Int)

data class O3Inbox(val data: List<O3InboxItem>)
data class O3InboxItem(val title: String, val subtitle: String, val description: String,
                       val actionTitle: String, val actionURL: String, val readmoreTitle: String, val readmoreURL: String, val iconURL: String)

data class VerifiedAddressData(val data: VerifiedAddress)
data class VerifiedAddress(val address: String, val publicKey: String, val displayName: String)

data class O3RealTimePriceData(val data: O3RealTimePrice)
data class O3RealTimePrice(val symbol: String, val currency: String, val price: Double, val lastUpdate: Long)



class TransferableAssets(private val balances: TransferableBalances) {
    var version: String
    var address: String
    var scriptHash: String
    var assets: ArrayList<TransferableAsset> = arrayListOf()
    var tokens: ArrayList<TransferableAsset> = arrayListOf()
    var ontology: ArrayList<TransferableAsset> = arrayListOf()

    init {
        version = balances.version
        address = balances.address
        scriptHash = balances.scriptHash
        for (asset in balances.assets) {
            assets.add(TransferableAsset(asset))
        }
        for (token in balances.nep5Tokens) {
            assets.add(TransferableAsset(token))
        }
       for (ontAsset in balances.ontology) {
            assets.add(TransferableAsset(ontAsset))
        }
    }
}

class TransferableAsset(val asset: TransferableBalance) {
    var id: String
    var name: String
    var value: BigDecimal
    var symbol: String
    var decimals: Int

    init {
        id = asset.id
        name = asset.name
        decimals = asset.decimals
        symbol = asset.symbol
        value = BigDecimal(asset.value)
        if (asset.symbol.toUpperCase() != "GAS") {
            value = value.divide(BigDecimal(Math.pow(10.0, decimals.toDouble())), decimals, BigDecimal.ROUND_HALF_UP)
        }
        print(value)

    }

    fun deepCopy(): TransferableAsset {
        var copyValue: BigDecimal
        copyValue = value.multiply(BigDecimal(Math.pow(10.0, decimals.toDouble())))
        if (asset.symbol.toUpperCase() == "GAS") {
            copyValue = value.divide(BigDecimal(Math.pow(10.0, decimals.toDouble())), decimals, BigDecimal.ROUND_HALF_UP)
        }
        val balance = TransferableBalance(asset.id, asset.name, copyValue.toPlainString(), asset.symbol, asset.decimals)
        return TransferableAsset(balance)
    }
}


