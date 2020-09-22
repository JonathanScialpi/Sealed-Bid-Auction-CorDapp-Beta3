import com.fasterxml.jackson.annotation.JsonCreator
import com.google.gson.Gson
import com.template.flows.BidNotificationFlow
import com.template.flows.IssueAuctionFlow
import com.template.flows.RecordBidFlow
import com.template.states.AuctionState
import com.template.states.BidState
import com.template.webserver.NodeRPCConnection
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.startFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    // @DEV: A simple endpoint to make sure that the server is up and running
    @GetMapping(value = ["/status"])
    private fun isAlive() = "Up and running!"

    data class AuctionObj @JsonCreator constructor(
            val numOfRequiredBids : Int,
            val participants : String
    )

    // @DEV: An endpoint to issue an auction using JSON.
    @RequestMapping(value = "/issueAuction", method = [RequestMethod.POST])
    private fun issueAuction(@RequestBody newAuction : AuctionObj): ResponseEntity<String?> {
        val result = proxy.startFlow(
                ::IssueAuctionFlow,
                newAuction.numOfRequiredBids,
                newAuction.participants.split(";").map{ proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(it)) as Party }
        )
        val auction = result.returnValue.get().tx.outputs[0].data as AuctionState
        val responseMap = HashMap<String, Any>()
//        responseMap["winner"] = auction.winner
        responseMap["auctioneer"] = auction.auctioneer
        responseMap["status"] = auction.status
        responseMap["numOfRequiredBids"] = auction.numOfRequiredBids
        responseMap["acknowledgedBids"] = auction.acknowledgedBids.toString()
        responseMap["participants"] = auction.participants.toString()
        responseMap["LinearID"] = auction.linearId
        return ResponseEntity.ok(Gson().toJson(responseMap))
    }

    data class ProposedAuctionObj @JsonCreator constructor(
            val auctionLinearId: String
    )

    // @DEV: An endpoint that allows a user to notify all participants that a bid was made.
    @RequestMapping(value = "/bidNotification", method = [RequestMethod.POST])
    private fun updateCorpus(@RequestBody updatedAuction : ProposedAuctionObj): ResponseEntity<String?> {
        val result = proxy.startFlow(
                ::BidNotificationFlow,
                UniqueIdentifier.fromString(updatedAuction.auctionLinearId)
        )

        val auction = result.returnValue.get().tx.outputs[0].data as AuctionState
        val responseMap = HashMap<String, Any>()
//        responseMap["winner"] = auction.winner
        responseMap["auctioneer"] = auction.auctioneer
        responseMap["status"] = auction.status
        responseMap["numOfRequiredBids"] = auction.numOfRequiredBids
        responseMap["acknowledgedBids"] = auction.acknowledgedBids.toString()
        responseMap["participants"] = auction.participants.toString()
        responseMap["LinearID"] = auction.linearId
        return ResponseEntity.ok(Gson().toJson(responseMap))
    }

    data class RecordBidObj @JsonCreator constructor(
            val bid: Int,
            val mailSequenceNumber: Int,
            val auctionLinearId: String
    )

    // @DEV: An endpoint for clients to record a bid they made in their vault.
    @RequestMapping(value = "/recordBid", method = [RequestMethod.POST])
    private fun updateClassificationEndpoint(@RequestBody bid : RecordBidObj): ResponseEntity<String?> {
        val result = proxy.startFlow(
                ::RecordBidFlow,
                bid.bid,
                bid.mailSequenceNumber,
                UniqueIdentifier.fromString(bid.auctionLinearId)
        )
        val bid = result.returnValue.get().tx.outputs[0].data as BidState
        val responseMap = HashMap<String, Any>()
        responseMap["bid"] = bid.bid
        responseMap["mailSequenceNumber"] = bid.mailSequenceNumber
        responseMap["auction"] = bid.auction
        responseMap["participants"] = bid.participants.toString()
        responseMap["LinearID"] = bid.linearId
        return ResponseEntity.ok(Gson().toJson(responseMap))
    }
}