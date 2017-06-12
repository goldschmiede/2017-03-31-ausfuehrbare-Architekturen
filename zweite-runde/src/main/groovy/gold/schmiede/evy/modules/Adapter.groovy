package gold.schmiede.evy.modules

import gold.schmiede.evy.broker.Message
import gold.schmiede.evy.distributed.DistributedComponent

/**
 * Simulates the adapter on the iSeries side
 *
 * The adapter is a web service which connects to a iSeries DataQueue object
 * and writes messages to the queue
 */
class Adapter implements DistributedComponent {

    DataQueue dataQueue

    def receiveAndProcess(Message message) {
        assert connected
        assert dataQueue, 'ConfigurationException: data queue not initialized'
        dataQueue.connect()
        dataQueue.write(message.toString())
        dataQueue.disconnect()
        disconnect()
    }
}
