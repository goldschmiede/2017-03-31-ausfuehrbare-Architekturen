package gold.schmiede.evy.broker

/**
 * Models a RabbitMQ client library
 *
 * A module can use the broker through a client
 */
class BrokerClient {

    Broker broker

    Broker getBroker() {
        assert broker, 'ConfigurationError: broker not initialized'
        broker
    }

    def send(String destination, Message message) {
        getBroker().connect()
        broker.sendTo(destination, '*', message)
        broker.disconnect()
    }

    Message receive(String source) {
        getBroker().connect()
        def result = broker.retrieveFrom(source)
        broker.disconnect()
        result
    }

    Message receivePending(String source) {
        getBroker().connect()
        def result = broker.retrieveFromUnAcknowledged(source)
        broker.disconnect()
        result
    }

    def ack(String source, String id) {
        getBroker().connect()
        broker.acknowledge(source, id)
    }

    def nack(String source, String id) {
        getBroker().connect()
        broker.unacknowledge(source, id)
    }
}
