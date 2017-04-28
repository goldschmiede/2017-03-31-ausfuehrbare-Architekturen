package base

/**
 * Base class for entities (DDD) - objects with an identity
 */
abstract class Entity {
    String id = UUID.randomUUID().toString()
}
