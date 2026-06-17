package gay.menkissing.pastelstorage.util

import net.minecraft.network.chat.Component
import net.minecraft.server.packs.repository.{BuiltInPackSource, KnownPack, Pack, PackSource}
import net.minecraft.server.packs.{PackLocationInfo, PackSelectionConfig, PackType, PathPackResources}
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.neoforge.event.AddPackFindersEvent

import java.util.Optional

object BuiltinPackHelper:
  def addDatapack(bus: IEventBus, container: ModContainer, name: String): Unit =
    bus.addListener: (ev: AddPackFindersEvent) =>
      ev.getPackType match
        case PackType.CLIENT_RESOURCES => ()
        case PackType.SERVER_DATA =>
          val modInfo = container.getModInfo
          val resourcePath = modInfo.getOwningFile.getFile
                                    .findResource(s"data/${container.getModId}/datapacks/$name")
          val version = modInfo.getVersion
          val pack =
            Pack.readMetaAndCreate(
              new PackLocationInfo(s"${container.getModId}:$name", Component
                .translatable(s"datapacks.${container.getModId}.$name"), PackSource.FEATURE, Optional
                .of(new KnownPack(container.getModId, name, version.toString))),
              BuiltInPackSource.fromName(path => PathPackResources(path, resourcePath)),
              PackType.SERVER_DATA,
              new PackSelectionConfig(false, Pack.Position.TOP, false)
            )
          ev.addRepositorySource(_.accept(pack))
