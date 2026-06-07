package gay.menkissing.spectrumstorage

import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer

object SpectrumStorageSided:
  def getClientSever: MinecraftServer | Null =
    Minecraft.getInstance().getSingleplayerServer
