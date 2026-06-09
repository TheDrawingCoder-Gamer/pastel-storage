package gay.menkissing.pastelstorage.util.network

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Player

object PastelStorageNetworking:
  def openExtendedMenu[T](player: Player, handler: GayScreenHandler[T]): java.util.OptionalInt =
    player.openMenu(handler, it => handler.codec.encode(it, handler.getOpeningData(player.asInstanceOf[ServerPlayer])))
  
  
  trait GayScreenHandler[T](val codec: StreamCodec[? >: RegistryFriendlyByteBuf, T]) extends MenuProvider:
    def getOpeningData(player: ServerPlayer): T
