package gold.schmiede.evy.broker

/**
 * Models a RabbitMQ Exchange
 *
 * @author : Uwe Wardenbach - mailto:uwe.wardenbach@gmx.de
 *
 * Exchanges are entry points for messages
 * An exchange serves as a message receiver.
 *
 * A client accesses an exchange via name.
 *
 * The broker distributes the messages according to the exchange's bindings to queues
 */
class Exchange {

    String name

    def bindings = []

    /**
     * receive a message which will be distributed to the bound queues
     */
    def receive(Message message) {
        bindings.each { it.accept(message) }
    }
}