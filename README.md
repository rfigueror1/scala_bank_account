Bank accounts exercise in functional programming language Scala.

In order to run the server, you should have installed IntelliJ.
The project has both the source code and the sbt file. The sbt file is the dependencies manager in Akka and Scala

In order to run the server, go to the folder where Intellij was installed and run the script `idea.sh` located in the `idea/bin` folder.

- Run the server.

In order to test the server with different HTTP requests run the following commands:

1. To modify the bank and add a transaction to a specific account in the bank:
`curl -X PUT -H "Content-Type: application/json" -d '{}' "http://localhost:8080/?bid=-100&user=RICK&typ=loan"
`, specifying altogether the amount, the id of the account (which in this case I chose it to be a string but could also had been a UUID, and the type of transaction.
2. To get the balance of an specific account run the following:
`curl -X GET -H "Content-Type: application/json" -d '{}' 'http://localhost:8080/balance?user=RICK'`
3. To get the bank statement for pair of dates run ``
3. To get the accounts outstanding debts in different times run the following:
`curl -X GET -H "Content-Type: application/json" -d '{}' 'http://localhost:8080/outstandingPrincipal?user=RICK'`



