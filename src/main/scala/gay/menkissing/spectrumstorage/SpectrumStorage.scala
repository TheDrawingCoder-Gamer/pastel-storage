package gay.menkissing.spectrumstorage

import gay.menkissing.spectrumstorage.content.{SpectrumStorageBlocks, SpectrumStorageItems}
import gay.menkissing.spectrumstorage.datagen.SpectrumStorageDatagen
import gay.menkissing.spectrumstorage.datagen.providers.SpectrumStorageBook
import gay.menkissing.spectrumstorage.registries.{SpectrumStorageComponents, SpectrumStorageScreens, SpectrumStorageTags}
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.registries.RegisterEvent
import net.neoforged.neoforge.server.ServerLifecycleHooks
import org.slf4j.{Logger, LoggerFactory}

object SpectrumStorage:
  inline val ModId = "pastelstorage"
  val Logger: Logger = LoggerFactory.getLogger("pastelstorage")

  def locate(id: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(ModId, id)

  def getRegistryAccess: RegistryAccess | Null =
    val server = getSidedServer
    if server == null then
      null
    else
      server.registryAccess()
  
  def getSidedServer: MinecraftServer | Null =
    if FMLEnvironment.dist == Dist.DEDICATED_SERVER then
      ServerLifecycleHooks.getCurrentServer
    else
      SpectrumStorageSided.getClientSever

@Mod(SpectrumStorage.ModId)
class SpectrumStorage(modBus: IEventBus):
  SpectrumStorageBlocks.submit(modBus)
  SpectrumStorageItems.submit(modBus)
  SpectrumStorageComponents.submit(modBus)
  SpectrumStorageScreens.submit(modBus)
  SpectrumStorageDatagen.submit(modBus)
  // InfoCollector.instance.addBookRegister(SpectrumStorageBook.init)