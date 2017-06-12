package gold.schmiede.evy.broker

/**
 * Models a RabbitMQ Queue
 *
 * @author : Uwe Wardenbach - mailto:uwe.wardenbach@gmx.de
 *
 * Queues are exit points for messages
 *
 * A client retrieves messages from a queue.
 * A client accesses a queue via its name.
 *
 * The broker distributes the messages to queues according to the exchange's bindings
 */
class Queue {

    String name

    def messages = []

    def pending = []

    def put(Message message) {
        messages << message
    }

    Message get() {
        return messages.empty ? null : messages.remove(0) as Message
    }

    Message getWithAck() {
        Message result = get()
        if ( result )
            pending << result
        result
    }

    void acknowledge(String id) {
        pending.removeAll { it.messageID == id }
    }

    void unacknowledge(String id) {
        def message = pending.find { it.messageID == id }
        if ( message ) {
            pending.removeAll { it.messageID == id }
            messages.add(0, message)
        }
    }

    void purge() {
        messages = []
        pending = []
    }
}
