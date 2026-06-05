package gay.menkissing.spectrumstorage.registries

import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.content.item.BottomlessBottleItem.BottomlessBottleContents
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.{BuiltInRegistries, Registries}

object LumoComponents {
  val BottomlessBottleContentsComponent: DataComponentType[BottomlessBottleContents] =
    Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE, 
      SpectrumStorage.locate("bottomless_bottle_contents"),
      DataComponentType.builder[BottomlessBottleContents]().persistent(BottomlessBottleContents.CODEC).networkSynchronized(BottomlessBottleContents.STREAM_CODEC).build()
    )
}
