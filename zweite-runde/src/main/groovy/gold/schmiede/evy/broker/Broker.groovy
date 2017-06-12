package gold.schmiede.evy.broker

import gold.schmiede.evy.distributed.DistributedComponent

/**
 * Models a RabbitMQ Broker
 *
 * @author : Uwe Wardenbach - mailto:uwe.wardenbach@gmx.de
 *
 * A RabbitMQ broker provides exchanges, queues and bindings between them
 *
 * Those objects are created through an administrative interface and used by clients.
 */

class Broker implements DistributedComponent {

    /**
     * exchanges by name
     */
    def exchanges = [:]

    /**
     * queues by name
     */
    def queues = [:]

    /**
     * used by clients to send messages to a named exchange
     * @param destination the exchange name
     * @param routingKey provides further routing information for topic exchanges
     * @param message the message being sent
     */
    def sendTo(String destination, String routingKey, Message message) {
        checkConnected()
        def exchange = exchanges[destination] as Exchange
        assert exchange, "unknown exchange $destination"
        exchange.receive(message)
    }

    Message retrieveFrom(String source) {
        checkConnected()
        def queue = queues[source] as Queue
        queue.get()
    }

    def retrieveFromUnAcknowledged(String source) {
        checkConnected()
        def queue = queues[source] as Queue
        queue.getWithAck()
    }

    def acknowledge(String source, String id) {
        checkConnected()
        def queue = queues[source] as Queue
        queue.acknowledge(id)
    }

    def unacknowledge(String source, String id) {
        checkConnected()
        def queue = queues[source] as Queue
        queue.unacknowledge(id)
    }

    def bind(String source, String target) {
        def binding = new Binding()
        binding.queues = queues[target]
        def exchange = exchanges[source] as Exchange
        exchange.bindings << binding
    }

    def createExchange(String name) {
        exchanges[name] = new Exchange()
    }

    def createQueue(String name) {
        queues[name] = new Queue()
    }

}
