To build, simply:

mvn clean package

To run, 
simply:

java -Xms12g -Xmx12g -jar coinprice-1.0-SNAPSHOT.jar server exchange_simulation.yaml

the JVM options is totally optional, actually 2g should be fine, 
config file is in yaml format, sample in the config directory

1. if there is simulationConfiguration config, means we are running simulation,
   there are 2 choices;
a) local mode, then you need to give a fileName property, which is the raw
price log.
b) exchange mode, then we still pull out the price info from exchange, then we
log into price log.

2. if there is no simulationConfiguration, then we need to specify production
   exchanges, hopefully we are close to it!

3. when system start with exchange mode, it will also provide price.csv (which
   log only the max_buy and min_sell into csv for modeling purpose), order.csv
is for the action, but they both need to be buffered to 8192 bytes to be in
disk. to get the balance history, you can send the api to server.

