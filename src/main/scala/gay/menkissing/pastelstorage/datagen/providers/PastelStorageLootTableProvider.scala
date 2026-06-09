package gay.menkissing.pastelstorage.datagen.providers

import gay.menkissing.pastelstorage.content.PastelStorageBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry
import net.minecraft.data.loot.{BlockLootSubProvider, LootTableProvider}
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.parameters.{LootContextParamSet, LootContextParamSets}

import java.lang
import java.util as ju
import java.util.concurrent.CompletableFuture

final class PastelStorageLootTableProvider(output: PackOutput, lookup: CompletableFuture[HolderLookup.Provider])
  extends LootTableProvider(output, ju.Set.of(), ju.List.of(SubProviderEntry(PastelStorageLootTableProvider.BlockSubProvider.apply, LootContextParamSets.BLOCK)), lookup)

object PastelStorageLootTableProvider:
  private final class BlockSubProvider(lookup: HolderLookup.Provider) extends BlockLootSubProvider(java.util.Set.of(), FeatureFlags.DEFAULT_FLAGS, lookup):
    override def getKnownBlocks: lang.Iterable[Block] =
      PastelStorageBlocks.blockRegistrar.getEntries.stream().map(_.value()).toList

    override def generate(): Unit =
      dropSelf(PastelStorageBlocks.bottomlessShelf.get())
      dropSelf(PastelStorageBlocks.bottomlessBarrel.get())
      dropSelf(PastelStorageBlocks.bottomlessAmphora.get())
      dropSelf(PastelStorageBlocks.filterChest.get())
