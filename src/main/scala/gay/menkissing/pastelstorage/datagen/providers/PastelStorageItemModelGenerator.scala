package gay.menkissing.pastelstorage.datagen.providers

import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.{PastelStorageBlocks, PastelStorageItems}
import net.minecraft.data.PackOutput
import net.minecraft.data.models.model.{ModelLocationUtils, ModelTemplates, TextureMapping}
import net.neoforged.neoforge.client.model.generators.{ItemModelProvider, ModelProvider}
import net.neoforged.neoforge.common.data.ExistingFileHelper
import gay.menkissing.pastelstorage.util.resources.{*, given}

class PastelStorageItemModelGenerator(output: PackOutput, existingFileHelper: ExistingFileHelper) extends ItemModelProvider(output, PastelStorage.ModId, existingFileHelper):
  override def registerModels(): Unit =
    // Spectrum right now doesn't render the item inside a bundle, so I'll just not bother for now
    basicItem(PastelStorageItems.bottomlessBottle.get())
    basicItem(PastelStorageItems.toolContainer.get())

    withExistingParent(PastelStorageBlocks.bottomlessShelf.getId.toString, modLoc("block/bottomless_shelf_inventory"))


