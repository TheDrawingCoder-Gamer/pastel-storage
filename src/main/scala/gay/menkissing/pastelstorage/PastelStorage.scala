package gay.menkissing.pastelstorage

import gay.menkissing.pastelstorage.content.{PastelStorageBlocks, PastelStorageItems}
import gay.menkissing.pastelstorage.datagen.PastelStorageDatagen
import gay.menkissing.pastelstorage.datagen.providers.PastelStorageBook
import gay.menkissing.pastelstorage.registries.{PastelStorageComponents, PastelStorageCriteria, PastelStorageScreens, PastelStorageTags}
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
  modBus.addListener: (ev: AddPackFindersEvent) =>
    ev.getPackType match
      case PackType.CLIENT_RESOURCES => ()
      case PackType.SERVER_DATA =>
        val modInfo = container.getModInfo
        val resourcePath = modInfo.getOwningFile.getFile.findResource("data/pastelstorage/datapacks/enable_barrel_amphora")
        val version = modInfo.getVersion
        val pack =
          Pack.readMetaAndCreate(
            new PackLocationInfo("pastelstorage:enable_barrel_amphora", Component.translatable("datapacks.pastelstorage.enable_barrel_amphora"), PackSource.FEATURE, Optional.of(new KnownPack("pastelstorage", "enable_barrel_amphora", version.toString))),
            BuiltInPackSource.fromName(path => PathPackResources(path, resourcePath)),
            PackType.SERVER_DATA,
            new PackSelectionConfig(false, Pack.Position.TOP, false)
          )
        ev.addRepositorySource(_.accept(pack))

  PastelStorageBlocks.submit(modBus)
  PastelStorageItems.submit(modBus)
  PastelStorageComponents.submit(modBus)
  PastelStorageScreens.submit(modBus)
  PastelStorageDatagen.submit(modBus)
  PastelStorageCriteria.register(modBus)
  // InfoCollector.instance.addBookRegister(SpectrumStorageBook.init)