package gay.menkissing.pastelstorage.registries

import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.advancement.{BarrelVoiderCriterion, InsertIntoVoidBarrelCriterion}
import net.minecraft.core.registries.Registries
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister

object PastelStorageCriteria:
  private val registrar = DeferredRegister.create(Registries.TRIGGER_TYPE, PastelStorage.ModId)

  val BARREL_VOIDING = new BarrelVoiderCriterion
  val INSERT_INTO_VOIDING_BARREL = new InsertIntoVoidBarrelCriterion

  def register(bus: IEventBus): Unit =
    // im bussing......
    registrar.register(BarrelVoiderCriterion.Id.getPath, () => BARREL_VOIDING)
    registrar.register(InsertIntoVoidBarrelCriterion.ID.getPath, () => INSERT_INTO_VOIDING_BARREL)
    
    registrar.register(bus)
