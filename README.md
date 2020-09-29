# Sealed-Bid-Auction-CorDapp-Beta3

## Sealed Bid Auction Conclave App
The [sealed-bid-auction](https://github.com/JonathanScialpi/Sealed-Bid-Auction-CorDapp-Beta3/tree/master/sealed-bid-auction) is a Java application designed to demonstrate how to confidentially submit bids to an auction by leveraging [Conclave Beta 3](https://docs.conclave.net/#beta-3). 

All bids are submitted are delievered as Conclave [Mail](https://docs.conclave.net/architecture.html#mail) via HTTP to the Spring Host which is running an [Enclave](https://docs.conclave.net/enclaves.html).

Each of these bids are confidentially decrypted by the Enclave to not let the host or anyone else know of the value. Once The enclave has received all five bids, it calculates which of the five is the highest and sends the winning encrypted bid back to host.

This winning bid is saved in host memory so that it can be queired to reveal the winning bid using the [reveal_winner](https://github.com/JonathanScialpi/Sealed-Bid-Auction-CorDapp-Beta3/blob/master/sealed-bid-auction/Webserver/host/src/main/java/com/r3/conclave/sample/host/HostController.java#L65) GET endpoint. 

### Creating the Docker Image
1. Install Docker
2. `docker run --name sba-conclave-corda -p 8080:8080 -it -d -v {path to the Conclave sdk}/sba-conclave-corda/sealed-bid-auction:/sdk -w /sdk ubuntu bash`
3. `docker exec -ti sba-conclave-corda apt update`

### Starting the Spring Server
1. Open the project in IntelliJ and run the Host's `assemble` configuration to create the Spring Server jar file.
2. `docker exec -ti sba-conclave-corda cp /sdk/Webserver/host/build/libs/host.jar /tmp/`
3. `docker exec -ti sba-conclave-corda java -jar /sdk/Webserver/host/build/libs/host.jar`

### Running the Application
1. Run `Client.main()`

## Sealed Bid Auction CorDapp
The [sba-cordapp](https://github.com/JonathanScialpi/Sealed-Bid-Auction-CorDapp-Beta3/tree/master/sba-cordapp) is Corda application design to interact with an Enclave for a sealed-bid-auction use case. This CorDapp is not currently working and would have to be refactored once a Corda compatible version of Conclave's Mail API becomes available.
