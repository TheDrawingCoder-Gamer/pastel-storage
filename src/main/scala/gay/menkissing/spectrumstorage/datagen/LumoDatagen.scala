package gay.menkissing.spectrumstorage.datagen

import com.klikli_dev.modonomicon.api.datagen.book.condition.{BookAdvancementConditionModel, BookConditionModel}
import com.klikli_dev.modonomicon.api.datagen.book.page.{BookSpotlightPageModel, BookTextPageModel}
import com.klikli_dev.modonomicon.api.datagen.book.{BookCategoryModel, BookModel}
import gay.menkissing.spectrumstorage.content.block.BottomlessShelfBlock
import gay.menkissing.spectrumstorage.content.{SpectrumStorageBlocks, SpectrumStorageItems}
import gay.menkissing.spectrumstorage.util.registry.InfoCollector
import net.fabricmc.fabric.api.datagen.v1.provider.{FabricCodecDataProvider, FabricModelProvider}
import net.fabricmc.fabric.api.datagen.v1.{DataGeneratorEntrypoint, FabricDataGenerator, FabricDataOutput}
import net.minecraft.core.Direction
import net.minecraft.data.models.blockstates.{Condition, MultiPartGenerator, Variant, VariantProperties}
import net.minecraft.data.models.{BlockModelGenerators, ItemModelGenerators}
import net.minecraft.data.models.model.{ModelLocationUtils, ModelTemplate, ModelTemplates, TextureMapping, TextureSlot}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.properties.{BlockStateProperties, EnumProperty}
import com.klikli_dev.modonomicon.api.datagen.{BookProvider, LanguageProviderCache}
import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.util.registry.provider.generators.LumoBookProvider
import gay.menkissing.spectrumstorage.util.registry.provider.generators.book.{BookNbtSpotlightPageModel, BookPedestalPageModel}
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.enchantment.{EnchantmentHelper, Enchantments}
import gay.menkissing.spectrumstorage.util.resources.{*, given}
import net.minecraft.world.level.ItemLike

import scala.collection.mutable


object LumoDatagen extends DataGeneratorEntrypoint:
  override def onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator): Unit =
    val pack = fabricDataGenerator.createPack()

    pack.addProvider((o: FabricDataOutput) => ModelGenerator(o))
    pack.addProvider((o: FabricDataOutput) => BookGenerator(o))

    InfoCollector.instance.registerDataGenerators(pack)


  private class ModelGenerator(output: FabricDataOutput) extends FabricModelProvider(output):

    override def generateItemModels(itemModelGenerator: ItemModelGenerators): Unit =
      ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(SpectrumStorageItems.bottomlessBottle, "_base"), TextureMapping.layer0(SpectrumStorageItems.bottomlessBottle), itemModelGenerator.output)

    override def generateBlockStateModels(blockStateModelGenerator: BlockModelGenerators): Unit =
      ()

  private class BookGenerator(output: FabricDataOutput) extends LumoBookProvider(output):
    override def addEntries(): Unit =
      val spectrumBook = ResourceLocation("spectrum", "guidebook")
      val equipCategory = addToCategory(spectrumBook, ResourceLocation("spectrum", "equipment"))
      val powerVStack =
        ItemStack(SpectrumStorageItems.bottomlessBottle)
      powerVStack.enchant(Enchantments.POWER_ARROWS, 5)
      val commonDesc = "book.spectrumstorage.added_by_spectrumstorage"
      val pedestalTitle = "container.spectrum.rei.pedestal_recipe"
      def langIncrementor(id: String): () => String =
        var i: Int = 0
        () =>
          val x = i
          i += 1
          s"book.spectrumstorage.guidebook.$id.page$x.text"

      val bottomlessBottlePages = langIncrementor("bottomless_bottle")
      val toolContainerPages = langIncrementor("tool_container")

      def itemUnlock(id: String): BookAdvancementConditionModel =
        BookAdvancementConditionModel
          .builder()
          .withAdvancementId(s"spectrumstorage:unlocks/items/$id")
          .build()

      def blockUnlock(id: String): BookAdvancementConditionModel =
        BookAdvancementConditionModel
          .builder()
          .withAdvancementId(s"spectrumstorage:unlocks/blocks/$id")
          .build()

      equipCategory.withNewEntry(ResourceLocation("spectrum", "equipment/bottomless_bottle"), "item.spectrumstorage.bottomless_bottle"): entry =>
        entry
          .withLocation(3, 4)
          .withDescription(commonDesc)
          .withCondition(BookAdvancementConditionModel.builder().withAdvancementId(SpectrumStorage.locate("unlocks/items/bottomless_bottle")).build())
          .withIcon(SpectrumStorageItems.bottomlessBottle)
          .withName("item.spectrumstorage.bottomless_bottle")
          .hideWhileLocked(true)
          .withPage(
            BookSpotlightPageModel.builder()
              .withItem(Ingredient.of(SpectrumStorageItems.bottomlessBottle))
              .withText(bottomlessBottlePages())
              .withTitle(SpectrumStorageItems.bottomlessBottle.getDescriptionId)
              .build()
          )
          .withPage(
            BookPedestalPageModel.Builder()
              .withText(bottomlessBottlePages())
              .withTitle(pedestalTitle)
              .withRecipeId("spectrumstorage:pedestal/tier2/bottomless_bottle")
              .build()
          )
          .withPage(
            BookNbtSpotlightPageModel.Builder()
             .withText(bottomlessBottlePages())
             .withTitle("enchantment.minecraft.power")
             .withCondition(BookAdvancementConditionModel.builder().withAdvancementId("spectrum:midgame/build_enchanting_structure").build())
             .withItem(ItemVariant.of(powerVStack))
             .build()
          )
      .withNewEntry(ResourceLocation("spectrum", "equipment/tool_container"), SpectrumStorageItems.toolContainer.getDescriptionId):
        _
          .withLocation(4, 4)
          .withDescription(commonDesc)
          .withCondition(BookAdvancementConditionModel.builder().withAdvancementId("spectrumstorage:unlocks/items/tool_container").build())
          .withIcon(SpectrumStorageItems.toolContainer)
          .hideWhileLocked(true)
          .withPage(
            BookSpotlightPageModel
              .builder()
              .withItem(Ingredient.of(SpectrumStorageItems.toolContainer))
              .withTitle(SpectrumStorageItems.toolContainer.getDescriptionId)
              .withText(toolContainerPages())
              .build()
          )
          .withPage(
            BookPedestalPageModel
              .Builder()
              .withText(toolContainerPages())
              .withTitle(pedestalTitle)
              .withRecipeId("spectrumstorage:pedestal/tier2/tool_container")
              .build()
          )

      val magicalBlockCategory = addToCategory(spectrumBook, ResourceLocation("spectrum", "magical_blocks"))

      val bottomlessAmphoraPages = langIncrementor("bottomless_amphora")
      val bottomlessBarrelPages = langIncrementor("bottomless_barrel")
      val bottomlessShelfPages = langIncrementor("bottomless_shelf")
      val filterChestPages = langIncrementor("filter_chest")

      def firstPage(item: ItemLike, text: String): BookSpotlightPageModel =
        BookSpotlightPageModel
          .builder()
          .withText(text)
          .withItem(Ingredient.of(item))
          .withTitle(item.asItem().getDescriptionId)
          .build()

      magicalBlockCategory.withNewEntry(ResourceLocation("spectrum", "magical_blocks/bottomless_amphora"), SpectrumStorageBlocks.bottomlessAmphora.getDescriptionId):
        _
          .withLocation(1, 5)
          .withDescription(commonDesc)
          .withCondition(blockUnlock("bottomless_amphora"))
          .withIcon(SpectrumStorageBlocks.bottomlessAmphora)
          .hideWhileLocked(true)
          .withPage(
            firstPage(SpectrumStorageBlocks.bottomlessAmphora, bottomlessAmphoraPages())
          )
          .withPage(
            BookPedestalPageModel
              .Builder()
              .withText(bottomlessAmphoraPages())
              .withTitle(pedestalTitle)
              .withRecipeId("spectrumstorage:pedestal/tier4/bottomless_amphora")
              .build()
          )
          .withPage(
            BookTextPageModel
              .builder()
              .withText(bottomlessAmphoraPages())
              .build()
          )
      .withNewEntry(ResourceLocation("spectrum", "magical_blocks/bottomless_barrel"), SpectrumStorageBlocks.bottomlessBarrel.getDescriptionId):
        _
          .withLocation(0, 5)
          .withDescription(commonDesc)
          .withCondition(blockUnlock("bottomless_barrel"))
          .withIcon(SpectrumStorageBlocks.bottomlessBarrel)
          .hideWhileLocked(true)
          .withPage(
            BookSpotlightPageModel
              .builder()
              .withItem(Ingredient.of(SpectrumStorageBlocks.bottomlessBarrel))
              .withTitle(SpectrumStorageBlocks.bottomlessBarrel.getDescriptionId)
              .withText(bottomlessBarrelPages())
              .build()
          )
          .withPage(
            BookPedestalPageModel
              .Builder()
              .withText(bottomlessBarrelPages())
              .withTitle(pedestalTitle)
              .withRecipeId("spectrumstorage:pedestal/tier3/bottomless_barrel")
              .build()
          )
          .withPage(
            BookTextPageModel
              .builder()
              .withText(bottomlessBarrelPages())
              .build()
          )
      .withNewEntry(ResourceLocation("spectrum", "magical_blocks/bottomless_shelf"), SpectrumStorageBlocks.bottomlessShelf.getDescriptionId):
        _
          .withLocation(1, 4)
          .withDescription(commonDesc)
          .withCondition(blockUnlock("bottomless_shelf"))
          .withIcon(SpectrumStorageBlocks.bottomlessShelf)
          .hideWhileLocked(true)
          .withPage(
            firstPage(SpectrumStorageBlocks.bottomlessShelf, bottomlessShelfPages())
          )
          .withPage(
            BookPedestalPageModel
              .Builder()
              .withTitle(pedestalTitle)
              .withRecipeId("spectrumstorage:pedestal/tier2/bottomless_shelf")
              .withText(bottomlessShelfPages())
              .build()
          )
          .withPage(
            BookTextPageModel
              .builder()
              .withText(bottomlessShelfPages())
              .build()
          )
      .withNewEntry(ResourceLocation("spectrum", "magical_blocks/filter_chest"), SpectrumStorageBlocks.filterChest.getDescriptionId):
        _
          .withLocation(0, 4)
          .withDescription(commonDesc)
          .withCondition(blockUnlock("filter_chest"))
          .withIcon(SpectrumStorageBlocks.filterChest)
          .hideWhileLocked(true)
          .withPage(
            firstPage(SpectrumStorageBlocks.filterChest, filterChestPages())
          )
          .withPage(
            BookPedestalPageModel
              .Builder()
              .withText(filterChestPages())
              .withTitle(pedestalTitle)
              .withRecipeId("spectrumstorage:pedestal/tier3/filter_chest")
              .build()
          )
