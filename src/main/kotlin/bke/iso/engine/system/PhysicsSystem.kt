//package bke.iso.engine.system
//
//import bke.iso.engine.entity.Component
//import bke.iso.engine.entity.Entities
//import bke.iso.engine.entity.Entity
//
//data class Velocity(
//    val dx: Float,
//    val dy: Float
//) : Component()
//
//class PhysicsSystem(private val entities: Entities) : System {
//    override fun update(deltaTime: Float) {
//        entities.withComponent(Velocity::class) { entity, velocity ->
//            entity.setY(calculateY(entity, velocity.dy * deltaTime))
//            entity.setX(calculateX(entity, velocity.dx * deltaTime))
//        }
//    }
//
//    private fun calculateY(entity: Entity, dy: Float): Float {
//        val pos = entity.getPos()
//        val defaultY = pos.y + dy
//
//        val collision = entity.getComponent<Collision>() ?: return defaultY
//        val collisionFrameData = entity.getComponent<CollisionFrameData>() ?: return defaultY
//        val solidCollision = collisionFrameData.solidCollisionY ?: return defaultY
//
//        val collisionArea = collisionFrameData.collisionArea
//        val otherCollisionArea = solidCollision.collisionArea
//        return if (otherCollisionArea.y > collisionArea.y) {
//            (otherCollisionArea.y - collision.bounds.length - collision.bounds.offset.y)
//        } else {
//            (otherCollisionArea.y + otherCollisionArea.height - collision.bounds.offset.y)
//        }
//    }
//
//    private fun calculateX(entity: Entity, dx: Float): Float {
//        val pos = entity.getPos()
//        val defaultX = pos.x + dx
//
//        val collision = entity.getComponent<Collision>() ?: return defaultX
//        val collisionFrameData = entity.getComponent<CollisionFrameData>() ?: return defaultX
//        val solidCollision = collisionFrameData.solidCollisionX ?: return defaultX
//
//        val collisionArea = collisionFrameData.collisionArea
//        val otherCollisionArea = solidCollision.collisionArea
//        return if (otherCollisionArea.x > collisionArea.x) {
//            (otherCollisionArea.x - collision.bounds.length - collision.bounds.offset.x)
//        } else {
//            (otherCollisionArea.x + otherCollisionArea.width - collision.bounds.offset.x)
//        }
//    }
//}
