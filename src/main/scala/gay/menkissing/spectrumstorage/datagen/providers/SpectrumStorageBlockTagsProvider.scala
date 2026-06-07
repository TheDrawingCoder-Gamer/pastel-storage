package gay.menkissing.spectrumstorage.datagen.providers

import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.content.SpectrumStorageBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.data.{BlockTagsProvider, ExistingFileHelper}

import java.util.concurrent.CompletableFuture

class SpectrumStorageBlockTagsProvider
  (output: PackOutput,
   lookupProvider: CompletableFuture[HolderLookup.Provider],
   existingFileHelper: ExistingFileHelper)
  extends BlockTagsProvider(output, lookupProvider, SpectrumStorage.ModId, existingFileHelper):

  override protected def addTags(provider: HolderLookup.Provider): Unit =
    tag(BlockTags.MINEABLE_WITH_AXE)
      .add(SpectrumStorageBlocks.bottomlessShelf.get())
      .add(SpectrumStorageBlocks.bottomlessBarrel.get())
      .add(SpectrumStorageBlocks.bottomlessAmphora.get())
      .add(SpectrumStorageBlocks.filterChest.get())
