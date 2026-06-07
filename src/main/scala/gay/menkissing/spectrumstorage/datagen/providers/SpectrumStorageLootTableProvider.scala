package gay.menkissing.spectrumstorage.datagen.providers

import gay.menkissing.spectrumstorage.content.SpectrumStorageBlocks
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

final class SpectrumStorageLootTableProvider(output: PackOutput, lookup: CompletableFuture[HolderLookup.Provider])
  extends LootTableProvider(output, ju.Set.of(), ju.List.of(SubProviderEntry(SpectrumStorageLootTableProvider.BlockSubProvider.apply, LootContextParamSets.BLOCK)), lookup)

object SpectrumStorageLootTableProvider:
  private final class BlockSubProvider(lookup: HolderLookup.Provider) extends BlockLootSubProvider(java.util.Set.of(), FeatureFlags.DEFAULT_FLAGS, lookup):
    override def getKnownBlocks: lang.Iterable[Block] =
      SpectrumStorageBlocks.blockRegistrar.getEntries.stream().map(_.value()).toList

    override def generate(): Unit =
      dropSelf(SpectrumStorageBlocks.bottomlessShelf.get())
      dropSelf(SpectrumStorageBlocks.bottomlessBarrel.get())
      dropSelf(SpectrumStorageBlocks.bottomlessAmphora.get())
      dropSelf(SpectrumStorageBlocks.filterChest.get())
