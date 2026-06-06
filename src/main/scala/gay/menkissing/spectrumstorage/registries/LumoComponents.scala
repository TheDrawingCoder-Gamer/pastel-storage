package gay.menkissing.spectrumstorage.registries

import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.content.item.BottomlessBottleItem.BottomlessBottleContents
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister

object LumoComponents {
  val registrar = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SpectrumStorage.ModId)


  val BottomlessBottleContentsComponent =
    registrar.register(
      "bottomless_bottle_contents",
      () => DataComponentType.builder[BottomlessBottleContents]().persistent(BottomlessBottleContents.CODEC).networkSynchronized(BottomlessBottleContents.STREAM_CODEC).build()
    )

  def submit(bus: IEventBus): Unit =
    registrar.register(bus)
}
