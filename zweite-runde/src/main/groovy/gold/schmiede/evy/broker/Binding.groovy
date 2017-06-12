package gold.schmiede.evy.broker

/**
 * Models a RabbitMQ Binding
 *
 * @author : Uwe Wardenbach - mailto:uwe.wardenbach@gmx.de
 *
 * Bindings serve to bind queues to exchanges.
 * The broker distributes incoming messages to queues according to the exchange's bindings
 */
class Binding {

    def queues = []

    def accept(Message message) { queues.each {it.put(message.clone())} }
}
