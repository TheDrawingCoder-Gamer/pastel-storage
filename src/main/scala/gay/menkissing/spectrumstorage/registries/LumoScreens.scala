package gay.menkissing.spectrumstorage.registries

import com.mojang.serialization.Codec
import de.dafuqs.spectrum.api.block.FilterConfigurable
import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.screen.ToolContainerMenu.ToolContainerData
import gay.menkissing.spectrumstorage.screen.{BottomlessStorageMenu, FilterChestMenu, ToolContainerMenu}
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.flag.{FeatureFlag, FeatureFlagSet, FeatureFlags}
import net.minecraft.world.inventory.{AbstractContainerMenu, MenuType}

object LumoScreens:
  val toolContainer: ExtendedScreenHandlerType[ToolContainerMenu, ToolContainerData] = 
    new ExtendedScreenHandlerType[ToolContainerMenu, ToolContainerData](ToolContainerMenu.fromNetwork, ToolContainerData.STREAM_CODEC)
  Registry.register(BuiltInRegistries.MENU, SpectrumStorage.locate("tool_container"), toolContainer)
  val bottomlessBarrel: MenuType[BottomlessStorageMenu] = new MenuType[BottomlessStorageMenu](BottomlessStorageMenu.barrel, FeatureFlags.VANILLA_SET)
  Registry.register(BuiltInRegistries.MENU, SpectrumStorage.locate("bottomless_barrel"), bottomlessBarrel)
  val bottomlessAmphora: MenuType[BottomlessStorageMenu] = new MenuType[BottomlessStorageMenu](BottomlessStorageMenu.amphora, FeatureFlags.VANILLA_SET)
  Registry.register(BuiltInRegistries.MENU, SpectrumStorage.locate("bottomless_amphora"), bottomlessAmphora)
  val filterChest: MenuType[FilterChestMenu] = 
    new ExtendedScreenHandlerType[FilterChestMenu, FilterConfigurable.ExtendedDataWithPos](FilterChestMenu.fromNetwork, FilterConfigurable.ExtendedDataWithPos.PACKET_CODEC)
  Registry.register(BuiltInRegistries.MENU, SpectrumStorage.locate("filter_chest"), filterChest)
  

  def init(): Unit = ()
