package com.template.flows



import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AuctionContract
import com.template.states.AuctionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class BidNotificationFlow(
        private val auctionLinearId: UniqueIdentifier
) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call() : SignedTransaction{
        // Initiator flow logic goes here.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val transactionBuilder = TransactionBuilder(notary)

        // get current auction state and use as input state
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(auctionLinearId))
        val auctionStateAndRef =  serviceHub.vaultService.queryBy<AuctionState>(queryCriteria).states.single()

        transactionBuilder.addInputState(auctionStateAndRef)

        val inputAuctionState = auctionStateAndRef.state.data
        val acknowledgedBids = inputAuctionState.acknowledgedBids.plus(ourIdentity.owningKey)
        //build transaction
        val outputAuctionState = AuctionState(
                null,
                inputAuctionState.auctioneer,
                inputAuctionState.status,
                inputAuctionState.numOfRequiredBids,
                acknowledgedBids,
                inputAuctionState.participants
        )
        val commandData = AuctionContract.Commands.AcknowledgeBid()
        transactionBuilder.addCommand(commandData, inputAuctionState.participants.map { it.owningKey })
        transactionBuilder.addOutputState(outputAuctionState, AuctionContract.ID)
        transactionBuilder.verify(serviceHub)

        // sign and get other signatures
        val ptx = serviceHub.signInitialTransaction(transactionBuilder)
        val sessions = (inputAuctionState.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))
        return subFlow(FinalityFlow(stx, sessions))
    }
}

@InitiatedBy(BidNotificationFlow::class)
class BidNotificationResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an bid State transaction" using (output is AuctionState)
            }
        }

        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(otherSideSession = counterpartySession, expectedTxId = txWeJustSignedId.id))
    }
}
