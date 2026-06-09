package gay.menkissing.pastelstorage.registries

import com.mojang.serialization.Codec
import earth.terrarium.pastel.api.block.FilterConfigurable
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.screen.ToolContainerMenu.ToolContainerData
import gay.menkissing.pastelstorage.screen.{BottomlessStorageMenu, FilterChestMenu, ToolContainerMenu}
import net.minecraft.core.Registry
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.minecraft.network.{FriendlyByteBuf, RegistryFriendlyByteBuf}
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.flag.{FeatureFlag, FeatureFlagSet, FeatureFlags}
import net.minecraft.world.inventory.{AbstractContainerMenu, MenuType}
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredRegister

object PastelStorageScreens:
  private val registry = DeferredRegister.create(Registries.MENU, PastelStorage.ModId)

  val toolContainer =
    registry.register(
      "tool_container",
      () => extendedWrapper(ToolContainerMenu.fromNetwork, ToolContainerData.STREAM_CODEC)
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
      () => extendedWrapper(FilterChestMenu.fromNetwork, FilterConfigurable.ExtendedDataWithPos.STREAM_CODEC)
    )

  def extendedWrapper[R <: AbstractContainerMenu, T](constructor: (Int, Inventory, T) => R, codec: StreamCodec[? >: RegistryFriendlyByteBuf, T]): MenuType[R] =
    IMenuTypeExtension.create[R](
      (syncId, inv, buf) => {
        val data: T = if buf == null then null.asInstanceOf[T] else codec.decode(buf)
        constructor(syncId, inv, data)
      }
    )
  def submit(bus: IEventBus): Unit =
    registry.register(bus)
