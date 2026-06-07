package gay.menkissing.spectrumstorage.datagen

import de.dafuqs.spectrum.registries.SpectrumEnchantmentKeys
import gay.menkissing.spectrumstorage.datagen.providers.{SpectrumStorageBlockStateGenerator, SpectrumStorageBlockTagsProvider, SpectrumStorageBook, SpectrumStorageItemModelGenerator, SpectrumStorageItemTagsProvider, SpectrumStorageLootTableProvider}
import gay.menkissing.spectrumstorage.util.registry.provider.generators.SpectrumStorageBaseBookProvider
import net.minecraft.core.registries.Registries
import net.minecraft.core.{HolderLookup, RegistrySetBuilder}
import net.minecraft.data.PackOutput
import net.minecraft.network.chat.Component
import net.minecraft.world.item.enchantment.Enchantment
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.data.event.GatherDataEvent.DataProviderFromOutputLookup

import java.util.concurrent.CompletableFuture

object SpectrumStorageDatagen:
  def submit(modBus: IEventBus): Unit =
    modBus.addListener(register)

  def register(event: GatherDataEvent): Unit =
    val generator = event.getGenerator
    val output = generator.getPackOutput
    val lookupProvider = event.getLookupProvider
    val existingFileHelper = event.getExistingFileHelper

    event.createDatapackRegistryObjects(
      new RegistrySetBuilder().add(Registries.ENCHANTMENT, bootstrap => {
        bootstrap.register(SpectrumEnchantmentKeys.VOIDING, Enchantment(Component.empty(), null, null, null))
      })
    )
    event.createProvider((a, b) => SpectrumStorageBook(a, b, event.includeClient(), event.includeServer()))
    if event.includeClient() then
      event.addProvider(SpectrumStorageBlockStateGenerator(output, existingFileHelper))
      event.addProvider(SpectrumStorageItemModelGenerator(output, existingFileHelper))
    if event.includeServer() then
      val blockTagProvider = SpectrumStorageBlockTagsProvider(output, lookupProvider, existingFileHelper)

      event.addProvider(blockTagProvider)
      event.addProvider(SpectrumStorageItemTagsProvider(output, lookupProvider, blockTagProvider.contentsGetter, existingFileHelper))
      event.createProvider(SpectrumStorageLootTableProvider.apply)


