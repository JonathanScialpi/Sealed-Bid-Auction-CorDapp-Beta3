package com.template.contracts

import com.template.states.AuctionState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class AuctionContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.AuctionContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<AuctionContract.Commands>()
        when(command.value){
            is Commands.Issue -> requireThat{
                "No inputs should be consumed when issuing an Auction." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing an Auction." using (tx.outputs.size == 1)
                val auction = tx.outputsOfType<AuctionState>().single()
                "The proposed state status must be 'Open'" using (auction.status == "Open")
                "The auction must begin with zero bids." using (auction.acknowledgedBids.isEmpty())
                "The participants list of a auction must have at least two parties." using (auction.participants.size > 1)
                "An auction cannot be issued with a winner" using (auction.winner == null)
                "numOfRequiredBids must be greater than 1." using (auction.numOfRequiredBids > 1)
            }

            is Commands.AcknowledgeBid -> requireThat{
                "One input state must be consumed when acknowledging a bidder." using (tx.inputs.size == 1)
                "One output state must be consumed when acknowledging a bidder." using (tx.outputs.size == 1)
                val auctionInput = tx.inputsOfType<AuctionState>().single()
                val auctionOutput = tx.outputsOfType<AuctionState>().single()
                "cannot alter the number of required bids." using (auctionInput.numOfRequiredBids == auctionOutput.numOfRequiredBids)
                "The newly proposed list of acknowledged bids must grow in size." using (auctionOutput.acknowledgedBids.size > auctionInput.acknowledgedBids.size)
                "The status cannot change unless the numOfRequiredBids is met." using ((auctionInput.status != auctionOutput.status) && (auctionOutput.acknowledgedBids.size < auctionOutput.numOfRequiredBids))
                "Winner cannot be set unless the numOfRequiredBids is met." using ((auctionInput.winner != null) && (auctionOutput.acknowledgedBids.size < auctionOutput.numOfRequiredBids))
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Issue : TypeOnlyCommandData(), Commands
        class AcknowledgeBid : TypeOnlyCommandData(), Commands
        class CloseAuction : TypeOnlyCommandData(), Commands
    }
}