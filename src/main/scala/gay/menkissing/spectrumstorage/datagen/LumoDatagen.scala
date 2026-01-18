package gay.menkissing.spectrumstorage.datagen

import gay.menkissing.spectrumstorage.content.SpectrumStorageItems
import gay.menkissing.spectrumstorage.util.registry.InfoCollector
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.{DataGeneratorEntrypoint, FabricDataGenerator, FabricDataOutput}
import net.minecraft.data.models.{BlockModelGenerators, ItemModelGenerators}
import net.minecraft.data.models.model.{ModelLocationUtils, ModelTemplates, TextureMapping}




object LumoDatagen extends DataGeneratorEntrypoint:
  override def onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator): Unit =
    val pack = fabricDataGenerator.createPack()

    pack.addProvider((o: FabricDataOutput) => ModelGenerator(o))

    InfoCollector.instance.registerDataGenerators(pack)


  private class ModelGenerator(output: FabricDataOutput) extends FabricModelProvider(output):

    override def generateItemModels(itemModelGenerator: ItemModelGenerators): Unit =
      ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(SpectrumStorageItems.bottomlessBottle, "_base"), TextureMapping.layer0(SpectrumStorageItems.bottomlessBottle), itemModelGenerator.output)

    override def generateBlockStateModels(blockStateModelGenerator: BlockModelGenerators): Unit =
      ()
