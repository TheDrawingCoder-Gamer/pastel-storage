package gay.menkissing.pastelstorage.datagen.providers

import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.PastelStorageBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.data.{BlockTagsProvider, ExistingFileHelper}

import java.util.concurrent.CompletableFuture

class PastelStorageBlockTagsProvider
  (output: PackOutput,
   lookupProvider: CompletableFuture[HolderLookup.Provider],
   existingFileHelper: ExistingFileHelper)
  extends BlockTagsProvider(output, lookupProvider, PastelStorage.ModId, existingFileHelper):

  override protected def addTags(provider: HolderLookup.Provider): Unit =
    tag(BlockTags.MINEABLE_WITH_AXE)
      .add(PastelStorageBlocks.bottomlessShelf.get())
      .add(PastelStorageBlocks.bottomlessBarrel.get())
      .add(PastelStorageBlocks.bottomlessAmphora.get())
      .add(PastelStorageBlocks.bottomlessWorm.get())
      .add(PastelStorageBlocks.filterChest.get())
