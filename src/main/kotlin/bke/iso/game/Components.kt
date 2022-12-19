package bke.iso.game

import bke.iso.engine.entity.Component
import java.util.UUID

class Player : Component()

data class Bullet(val shooterId: UUID) : Component()
