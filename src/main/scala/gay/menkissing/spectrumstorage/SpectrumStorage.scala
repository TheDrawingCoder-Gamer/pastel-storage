package gay.menkissing.spectrumstorage

import gay.menkissing.spectrumstorage.content.{SpectrumStorageBlocks, SpectrumStorageItems}
import gay.menkissing.spectrumstorage.datagen.SpectrumStorageDatagen
import gay.menkissing.spectrumstorage.datagen.providers.SpectrumStorageBook
import gay.menkissing.spectrumstorage.registries.{SpectrumStorageComponents, SpectrumStorageScreens, SpectrumStorageTags}
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.registries.RegisterEvent
import org.slf4j.{Logger, LoggerFactory}

object SpectrumStorage:
  inline val ModId = "spectrumstorage"
  val Logger: Logger = LoggerFactory.getLogger("spectrumstorage")

  def locate(id: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(ModId, id)


@Mod(SpectrumStorage.ModId)
class SpectrumStorage(modBus: IEventBus):
  SpectrumStorageBlocks.submit(modBus)
  SpectrumStorageItems.submit(modBus)
  SpectrumStorageComponents.submit(modBus)
  SpectrumStorageScreens.submit(modBus)
  SpectrumStorageDatagen.submit(modBus)
  // InfoCollector.instance.addBookRegister(SpectrumStorageBook.init)