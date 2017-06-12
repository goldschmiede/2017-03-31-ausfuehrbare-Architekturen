package gold.schmiede.evy.modules

import gold.schmiede.evy.distributed.DistributedComponent

/**
 * Simulates an iSeries DataQueue
 *
 * a client can write data entries to such a queue and read data entries from it
 * as this is a system object (like a database table) it is a distributed component
 * from a client's viewpoint
 */
class DataQueue implements DistributedComponent {

    def entries = []

    def write(String data) {
        assert connected
        entries << data
        disconnect()
    }

    String read() {
        assert connected
        String firstEntry = entries.empty ? null : entries.remove(0)
        disconnect()
        firstEntry
    }
}
