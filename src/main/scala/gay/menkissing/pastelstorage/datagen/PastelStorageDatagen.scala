package gay.menkissing.pastelstorage.datagen

import earth.terrarium.pastel.registries.PastelEnchantments
import gay.menkissing.pastelstorage.datagen.providers.{PastelStorageAdvancementProvider, PastelStorageBlockStateGenerator, PastelStorageBlockTagsProvider, PastelStorageBook, PastelStorageItemModelGenerator, PastelStorageItemTagsProvider, PastelStorageLootTableProvider}
import gay.menkissing.pastelstorage.util.registry.provider.generators.PastelStorageBaseBookProvider
import net.minecraft.core.registries.Registries
import net.minecraft.core.{HolderLookup, RegistrySetBuilder}
import net.minecraft.data.PackOutput
import net.minecraft.network.chat.Component
import net.minecraft.world.item.enchantment.Enchantment
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.data.event.GatherDataEvent.DataProviderFromOutputLookup

import java.util.concurrent.CompletableFuture

object PastelStorageDatagen:
  def submit(modBus: IEventBus): Unit =
    modBus.addListener(register)

  def register(event: GatherDataEvent): Unit =
    val generator = event.getGenerator
    val output = generator.getPackOutput
    val lookupProvider = event.getLookupProvider
    val existingFileHelper = event.getExistingFileHelper

    event.createDatapackRegistryObjects(
      new RegistrySetBuilder().add(Registries.ENCHANTMENT, bootstrap => {
        bootstrap.register(PastelEnchantments.VOIDING, Enchantment(Component.empty(), null, null, null))
      })
    )
    event.createProvider((a, b) => PastelStorageBook(a, b, event.includeClient(), event.includeServer()))
    if event.includeClient() then
      event.addProvider(PastelStorageBlockStateGenerator(output, existingFileHelper))
      event.addProvider(PastelStorageItemModelGenerator(output, existingFileHelper))
    if event.includeServer() then
      val blockTagProvider = PastelStorageBlockTagsProvider(output, lookupProvider, existingFileHelper)

      event.addProvider(blockTagProvider)
      event.addProvider(PastelStorageItemTagsProvider(output, lookupProvider, blockTagProvider.contentsGetter, existingFileHelper))
      event.createProvider(PastelStorageLootTableProvider.apply)
      event.addProvider(PastelStorageAdvancementProvider(output, lookupProvider, existingFileHelper))


