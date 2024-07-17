package bke.iso.engine.serialization

import bke.iso.engine.world.actor.Component
import bke.iso.game.weapon.system.RangedWeapon
import bke.iso.game.weapon.system.Weapon
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.hasAnnotation

class Serializer {

    private val module = SerializersModule {
        contextual(Vector3Serializer)
        contextual(ColorSerializer)

        polymorphic(Component::class) {
            for (kClass in getComponentSubTypes()) {
                subclass(kClass)
            }
        }

        polymorphic(Weapon::class) {
            subclass(RangedWeapon::class)
        }
    }

    val format: Json = Json {
        serializersModule = module
        isLenient = true
        prettyPrint = true
    }

    private fun getComponentSubTypes(): List<KClass<out Component>> =
        Reflections("bke.iso")
            .getSubTypesOf(Component::class.java)
            .map(Class<out Component>::kotlin)
            .filter { kClass -> kClass.hasAnnotation<Serializable>() }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Component> PolymorphicModuleBuilder<Component>.subclass(kClass: KClass<T>) {
        subclass(kClass, serializer(kClass.createType()) as KSerializer<T>)
    }

    inline fun <reified T : Any> read(content: String): T =
        format.decodeFromString(content)

    inline fun <reified T : Any> write(value: T): String =
        format.encodeToString(value)
}
