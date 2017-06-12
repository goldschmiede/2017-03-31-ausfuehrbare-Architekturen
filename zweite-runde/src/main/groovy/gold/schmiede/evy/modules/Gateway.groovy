package gold.schmiede.evy.modules

import gold.schmiede.evy.broker.BrokerClient
import gold.schmiede.evy.broker.Message
import gold.schmiede.evy.distributed.DistributedComponent

/**
 * The Gateway transfers messages from the broker to the adapter and vice versa
 */
class Gateway implements DistributedComponent {

    Adapter adapter
    BrokerClient brokerClient
    String queueName

    def processNextEntry() {
        assert brokerClient, 'ConfigurationException: broker client not initialized'
        assert adapter, 'ConfigurationException: adapter not initialized'

        Message message = brokerClient.receivePending(queueName)
        if ( message ) {
            try {
                adapter.connect()
                adapter.receiveAndProcess(message)
                brokerClient.ack(queueName, message.messageID)
                adapter.disconnect()
            } catch (AssertionError ex) {
                brokerClient.nack(queueName, message.messageID)
                println "caught and handled: $ex"
            }
        }
    }

}
