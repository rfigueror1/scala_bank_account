Bank accounts exercise in functional programming language Scala.

In order to run the server, you should have installed IntelliJ and scala, please find a script to automate the installation of scala 'install_scala.sh'. To make the file executable run `sudo chmod +x install_scala.sh` and to execute, run `./install_scala.sh`

to install IntelliJ visit `https://www.jetbrains.com/idea/download/#section=linux`, download the compressed file.

The project has both the source code and the sbt file. The sbt file is the dependencies manager in Akka and Scala

In order to run the server, go to the folder where Intellij was installed and run the script `idea.sh` located in the `idea/bin` folder.

- Run the server.

In order to test the server with different HTTP requests run the following commands:

1. To modify the bank and add a transaction to a specific account in the bank:
`curl -X PUT -H "Content-Type: application/json" -d '{}' "http://localhost:8080/?bid=-100&user=RICK&typ=loan"
`, specifying altogether the amount, the id of the account (which in this case I chose it to be a string but could also had been a UUID, and the type of transaction.
2. To get the balance of an specific account run the following:
`curl -X GET -H "Content-Type: application/json" -d '{}' 'http://localhost:8080/balance?user=RICK'`
3. To get the bank statement for pair of dates run `curl -X GET -H "Content-Type: application/json" -d '{}' 'http://localhost:8080/BankStatements?user=RICK&date1=2019-04-15&date2=2019-05-15'`
4. To get the accounts outstanding debts in different times run the following:
`curl -X GET -H "Content-Type: application/json" -d '{}' 'http://localhost:8080/outstandingPrincipal?user=RICK'`


The client side was also implemented with a generic function to make requests of the previously described types.
