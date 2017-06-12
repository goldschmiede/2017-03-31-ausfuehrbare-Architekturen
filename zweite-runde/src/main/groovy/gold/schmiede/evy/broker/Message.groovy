package gold.schmiede.evy.broker

import groovy.transform.AutoClone
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents a message in our messaging system
 *
 * @author : Uwe Wardenbach - mailto:uwe.wardenbach@gmx.de
 *
 * A message in most messaging systems is represented by
 * a mesage-id, a body and some header attributes
 *
 * note the usage of ToString and EqualsAndHashCode annotations
 * which is always useful for "domain" objects
 */
@ToString(includePackage = false)
@EqualsAndHashCode
@AutoClone
class Message {
    String messageID = UUID.randomUUID().toString()
    def headers = []
    Object data
}
