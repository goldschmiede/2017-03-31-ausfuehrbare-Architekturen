package gold.schmiede.evy.distributed

/**
 * This trait emulates some main properties of a distributed component:
 *
 * - it can be unavailable - dang!
 * - you have to connect to it to use it
 */
trait DistributedComponent {

    boolean ready
    boolean connected

    def startup() { ready = true }

    def shutdown() { ready = false }

    def unavailable() { ready = false }

    def checkConnected() {
        assert connected, 'not connected'
    }

    def connect() {
        assert ready, 'ConnectError'
        connected = true
    }

    def disconnect() {
        connected = false
    }
}