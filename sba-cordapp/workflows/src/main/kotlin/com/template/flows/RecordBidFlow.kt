package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AuctionContract
import com.template.states.BidState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class RecordBidFlow(
        private val bid: Int,
        private val mailSequenceNumber: Int,
        private val auction: UniqueIdentifier
) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call() : SignedTransaction{
        // Initiator flow logic goes here.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val transactionBuilder = TransactionBuilder(notary)
        val participants : List<Party> = listOf(ourIdentity)
        //build transaction
        val outputAuctionState = BidState(
                bid,
                mailSequenceNumber,
                auction,
                participants
        )
        val commandData = AuctionContract.Commands.Issue()
        transactionBuilder.addCommand(commandData, participants.plus(ourIdentity).map { it.owningKey })
        transactionBuilder.addOutputState(outputAuctionState, AuctionContract.ID)
        transactionBuilder.verify(serviceHub)

        // sign and get other signatures
        val ptx = serviceHub.signInitialTransaction(transactionBuilder)
        val sessions = (participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))
        return subFlow(FinalityFlow(stx, sessions))
    }
}

//@InitiatedBy(IssueAuction::class)
//class RecordBidResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
//    @Suspendable
//    override fun call(): SignedTransaction {
//        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
//            override fun checkTransaction(stx: SignedTransaction) = requireThat {
//                val output = stx.tx.outputs.single().data
//                "This must be an buction State transaction" using (output is AuctionState)
//            }
//        }
//
//        val txWeJustSignedId = subFlow(signedTransactionFlow)
//        return subFlow(ReceiveFinalityFlow(otherSideSession = counterpartySession, expectedTxId = txWeJustSignedId.id))
//    }
//}