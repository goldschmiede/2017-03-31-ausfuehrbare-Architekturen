package gold.schmiede.evy.broker

import spock.lang.Shared
import spock.lang.Specification

/**
 * Basic tests for working with a broker
 */
class BrokerTests extends Specification {

    @Shared
    Broker broker

    @Shared
    BrokerClient sender, receiver

    /**
     * by convention: Spock calls this once before all tests run
     *
     * we create a broker
     * start it
     * create an exchange and a queue and bind them together
     */
    def setup() {
        broker = new Broker()
        broker.startup()
        broker.createExchange('Hamburg')
        broker.createQueue('Köln')
        broker.bind('Hamburg', 'Köln')
        sender = new BrokerClient(broker: broker)
        receiver = new BrokerClient(broker: broker)
    }

    def 'simple test sending one message from one client to another'() {
        given:
        def msg = new Message(data: 'xx1-xx1')

        when:
        sender.send('Hamburg', msg)
        and:
        def result = receiver.receive('Köln')

        then:
        // note: Groovy's power assert is used here
        msg == result
    }

    def 'simple error: broker client not configured'() {
        given: 'receiver not configured'
        BrokerClient receiver = new BrokerClient()

        when:
        sender.send('Hamburg', new Message(data: 'xxx-xxx'))
        then:
        1

        when:
        receiver.receive('Köln')
        then: 'we expect an assertion error'
        def ex = thrown(AssertionError)
        and:
        ex.message.startsWith('ConfigurationError')
    }

    def 'test a connection error'() {
        given:
        def msg = new Message(data: 'xxx-xxx')

        when:
        sender.send('Hamburg', msg)
        and:
        broker.unavailable()
        and:
        receiver.receive('Köln')
        then:
        AssertionError ex = thrown()
        and:
        ex.message.startsWith('ConnectError')
    }

    def 'test with manual acknowledging'() {
        given:
        def msg = new Message(data: 'xxx-xxx')

        when:
        sender.send('Hamburg', msg)
        and: 'we read the message with manual ack'
        def rcvd = receiver.receivePending('Köln')
        then:
        msg == rcvd
        and: 'the pending message is not available to new reads'
        receiver.receivePending('Köln') == null

        when:
        receiver.nack('Köln', rcvd.messageID)
        then: 'the pending message is again available to new reads'
        receiver.receivePending('Köln') == msg

        when:
        receiver.ack('Köln', rcvd.messageID)

        then: 'the pending message is confirmed and removed'
        receiver.receive('Köln') == null
    }


}
