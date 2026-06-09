package gay.menkissing.pastelstorage.registries

import gay.menkissing.pastelstorage.PastelStorage
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.fluids.SimpleFluidContent
import net.neoforged.neoforge.registries.{DeferredHolder, DeferredRegister}

object PastelStorageComponents {
  val registrar = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PastelStorage.ModId)


  val BottomlessBottleContentsComponent: DeferredHolder[DataComponentType[?], DataComponentType[SimpleFluidContent]] =
    registrar.register(
      "bottomless_bottle_contents",
      () => DataComponentType.builder[SimpleFluidContent]().persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC).build()
    )
  

  def submit(bus: IEventBus): Unit =
    registrar.register(bus)
}
