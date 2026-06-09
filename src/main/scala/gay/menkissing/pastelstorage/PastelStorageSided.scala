package gay.menkissing.pastelstorage

import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer

object PastelStorageSided:
  def getClientSever: MinecraftServer | Null =
    Minecraft.getInstance().getSingleplayerServer
