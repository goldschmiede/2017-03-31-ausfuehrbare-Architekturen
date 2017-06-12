package gold.schmiede.evy.modules

import gold.schmiede.evy.broker.Broker
import gold.schmiede.evy.broker.BrokerClient
import gold.schmiede.evy.broker.Message
import gold.schmiede.evy.broker.Queue
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates how to check the expected behaviour with Spock tests
 *
 * @author : Uwe Wardenbach - mailto:uwe.wardenbach@gmx.de
 */
class DistributedTests extends Specification {

    @Shared Adapter adapter

    @Shared Broker broker

    @Shared Gateway gateway

    @Shared DataQueue dataQueue

    /**
     * Spock convention: executed once for the whole test suite
     */
    def setupSpec() {
        dataQueue = new DataQueue()
        dataQueue.startup()

        adapter = new Adapter(dataQueue: dataQueue)
        adapter.startup()

        broker = new Broker()
        broker.startup()
        broker.createExchange('Incoming')
        broker.createQueue('Outgoing')
        broker.bind('Incoming', 'Outgoing')

        BrokerClient client = new BrokerClient(broker: broker)
        gateway = new Gateway(adapter: adapter, brokerClient: client, queueName: 'Outgoing')
    }

    def geradeausFlug() {
        given:
        def sender = new BrokerClient(broker: broker)

        when: 'send a message to the - Incoming - exchange'
        Message msg = new Message(messageID: '2134', data: 'xxx-yyy')
        sender.send('Incoming', msg)

        and: 'as the gateway is wired to the - Outgoing - queue it should receive the message'
        gateway.processNextEntry()

        then: 'the message will be passed to the adapter and finally end up in the dataqueue'
        dataQueue.entries.size() == 1

        and:
        dataQueue.entries.first() == 'Message(2134, [], xxx-yyy)'

    }

    def dataQueueNotAvailable() {
        given:
        def sender = new BrokerClient(broker: broker)
        def out = broker.queues.Outgoing as Queue

        when: 'send a message to the - Incoming - exchange'
        Message msg = new Message(messageID: '2134', data: 'xxx-yyy')
        sender.send('Incoming', msg)

        and: 'but the queue is not available'
        dataQueue.unavailable()

        and: 'now if we process the message we stumble upon an error in delivering it to the adapter'
        gateway.processNextEntry()

        then: 'it should not be deleted from the queue'
        !out.messages.empty

        cleanup:
        out.purge()
        dataQueue.startup()
    }

    def adapterNotAvailable() {
        setup: 'clear dataqueue'
        dataQueue.entries = []
        
        and:
        def sender = new BrokerClient(broker: broker)
        def out = broker.queues.Outgoing as Queue

        when: 'once again - send a message'
        Message msg = new Message(messageID: '2135', data: 'xxx-yyy')
        sender.send('Incoming', msg)

        and: 'adapter or network is down'
        adapter.unavailable()

        and:
        gateway.processNextEntry()

        then: 'again - the system as a whole should handle this gracefully and be able to re-deliver the message'
        !out.messages.empty

        when: 'adapter back on track'
        adapter.startup()

        and:
        gateway.processNextEntry()

        then: 'now the message is delivered'
        dataQueue.entries.size() == 1

        and:
        dataQueue.entries.first() == 'Message(2135, [], xxx-yyy)'

        and: 'the message has been removed from the queue'
        out.messages.empty
    }


}
