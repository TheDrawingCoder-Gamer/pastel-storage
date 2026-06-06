package gay.menkissing.spectrumstorage.registries

import com.mojang.serialization.Codec
import de.dafuqs.spectrum.api.block.FilterConfigurable
import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.screen.ToolContainerMenu.ToolContainerData
import gay.menkissing.spectrumstorage.screen.{BottomlessStorageMenu, FilterChestMenu, ToolContainerMenu}
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.Registry
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.flag.{FeatureFlag, FeatureFlagSet, FeatureFlags}
import net.minecraft.world.inventory.{AbstractContainerMenu, MenuType}
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister

object LumoScreens:
  private val registry = DeferredRegister.create(Registries.MENU, SpectrumStorage.ModId)

  val toolContainer =
    registry.register(
      "tool_container",
      () => new ExtendedScreenHandlerType[ToolContainerMenu, ToolContainerData](ToolContainerMenu.fromNetwork, ToolContainerData.STREAM_CODEC)
    )
  val bottomlessBarrel =
    registry.register(
      "bottomless_barrel",
      () => new MenuType[BottomlessStorageMenu](BottomlessStorageMenu.barrel, FeatureFlags.VANILLA_SET)
    )

  val bottomlessAmphora =
    registry.register(
      "bottomless_amphora",
      () => new MenuType[BottomlessStorageMenu](BottomlessStorageMenu.amphora, FeatureFlags.VANILLA_SET)
    )

  val filterChest =
    registry.register(
      "filter_chest",
      () => new ExtendedScreenHandlerType[FilterChestMenu, FilterConfigurable.ExtendedDataWithPos](FilterChestMenu.fromNetwork, FilterConfigurable.ExtendedDataWithPos.PACKET_CODEC)
    )
  

  def submit(bus: IEventBus): Unit =
    registry.register(bus)
