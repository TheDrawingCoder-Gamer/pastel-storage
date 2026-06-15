package gay.menkissing.pastelstorage.registries

import com.mojang.serialization.Codec
import gay.menkissing.pastelstorage.PastelStorage
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.minecraft.network.codec.ByteBufCodecs
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
  
  val BottomlessBatteryEnergyComponent: DeferredHolder[DataComponentType[?], DataComponentType[Integer]] =
    registrar.register(
      "bottomless_battery_energy",
      () => DataComponentType.builder[Integer]().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build()
    )

  def submit(bus: IEventBus): Unit =
    registrar.register(bus)
}
