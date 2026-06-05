package gay.menkissing.spectrumstorage.datagen

import de.dafuqs.spectrum.registries.SpectrumEnchantments
import gay.menkissing.spectrumstorage.content.SpectrumStorageItems
import gay.menkissing.spectrumstorage.util.registry.InfoCollector
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.{DataGeneratorEntrypoint, FabricDataGenerator, FabricDataOutput}
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.data.models.{BlockModelGenerators, ItemModelGenerators}
import net.minecraft.data.models.model.{ModelLocationUtils, ModelTemplates, TextureMapping}
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.network.chat.Component
import net.minecraft.world.item.enchantment.Enchantment




object LumoDatagen extends DataGeneratorEntrypoint:
  val dummyEnchant: Enchantment = new Enchantment(Component.empty(), null, null, null)

  def bootstrapEnchant(context: BootstrapContext[Enchantment]): Unit =
    context.register(SpectrumEnchantments.VOIDING, dummyEnchant)
  
  override def onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator): Unit =
    val pack = fabricDataGenerator.createPack()

    pack.addProvider((o: FabricDataOutput) => ModelGenerator(o))

    InfoCollector.instance.registerDataGenerators(pack)

  override def buildRegistry(registryBuilder: RegistrySetBuilder): Unit =
    registryBuilder.add(Registries.ENCHANTMENT, bootstrapEnchant)

  private class ModelGenerator(output: FabricDataOutput) extends FabricModelProvider(output):

    override def generateItemModels(itemModelGenerator: ItemModelGenerators): Unit =
      ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(SpectrumStorageItems.bottomlessBottle, "_base"), TextureMapping.layer0(SpectrumStorageItems.bottomlessBottle), itemModelGenerator.output)

    override def generateBlockStateModels(blockStateModelGenerator: BlockModelGenerators): Unit =
      ()
