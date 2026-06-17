package gay.menkissing.pastelstorage

import gay.menkissing.pastelstorage.content.{PastelStorageBlocks, PastelStorageItems}
import gay.menkissing.pastelstorage.datagen.PastelStorageDatagen
import gay.menkissing.pastelstorage.datagen.providers.PastelStorageBook
import gay.menkissing.pastelstorage.registries.{PastelStorageComponents, PastelStorageCriteria, PastelStorageScreens, PastelStorageTags}
import gay.menkissing.pastelstorage.util.BuiltinPackHelper
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.packs.{PackLocationInfo, PackSelectionConfig, PackType, PathPackResources}
import net.minecraft.server.packs.repository.{BuiltInPackSource, KnownPack, Pack, PackSource, RepositorySource}
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.{ModContainer, ModList}
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.common.NeoForgeMod
import net.neoforged.neoforge.event.AddPackFindersEvent
import net.neoforged.neoforge.registries.RegisterEvent
import net.neoforged.neoforge.server.ServerLifecycleHooks
import org.slf4j.{Logger, LoggerFactory}

import java.util.Optional

object PastelStorage:
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
      PastelStorageSided.getClientSever

@Mod(PastelStorage.ModId)
class PastelStorage(modBus: IEventBus, container: ModContainer):
  BuiltinPackHelper.addDatapack(modBus, container, "enable_barrel_amphora")

  PastelStorageBlocks.submit(modBus)
  PastelStorageItems.submit(modBus)
  PastelStorageComponents.submit(modBus)
  PastelStorageScreens.submit(modBus)
  PastelStorageDatagen.submit(modBus)
  PastelStorageCriteria.register(modBus)
  // InfoCollector.instance.addBookRegister(SpectrumStorageBook.init)