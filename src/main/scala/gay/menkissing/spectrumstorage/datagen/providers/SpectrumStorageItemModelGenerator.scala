package gay.menkissing.spectrumstorage.datagen.providers

import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.content.{SpectrumStorageBlocks, SpectrumStorageItems}
import net.minecraft.data.PackOutput
import net.minecraft.data.models.model.{ModelLocationUtils, ModelTemplates, TextureMapping}
import net.neoforged.neoforge.client.model.generators.{ItemModelProvider, ModelProvider}
import net.neoforged.neoforge.common.data.ExistingFileHelper
import gay.menkissing.spectrumstorage.util.resources.{*, given}

class SpectrumStorageItemModelGenerator(output: PackOutput, existingFileHelper: ExistingFileHelper) extends ItemModelProvider(output, SpectrumStorage.ModId, existingFileHelper):
  override def registerModels(): Unit =
    // Spectrum right now doesn't render the item inside a bundle, so I'll just not bother for now
    basicItem(SpectrumStorageItems.bottomlessBottle.get())
    basicItem(SpectrumStorageItems.toolContainer.get())

    withExistingParent(SpectrumStorageBlocks.bottomlessShelf.getId.toString, modLoc("block/bottomless_shelf_inventory"))


