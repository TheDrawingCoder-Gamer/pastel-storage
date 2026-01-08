package gay.menkissing.spectrumstorage.registries

import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel
import com.klikli_dev.modonomicon.api.datagen.book.condition.BookAdvancementConditionModel
import com.klikli_dev.modonomicon.api.datagen.book.page.{BookSpotlightPageModel, BookTextPageModel}
import gay.menkissing.spectrumstorage.content.{SpectrumStorageBlocks, SpectrumStorageItems}
import gay.menkissing.spectrumstorage.util.registry.InfoCollector
import gay.menkissing.spectrumstorage.util.registry.book.EntryLocation
import gay.menkissing.spectrumstorage.util.registry.provider.generators.book.{BookNbtSpotlightPageModel, BookPedestalPageModel}
import net.minecraft.resources.ResourceLocation
import gay.menkissing.spectrumstorage.util.resources.{*, given}
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments

object SpectrumStorageBook:

  def init(): Unit =
    val spectrumBook = ResourceLocation("spectrum", "guidebook")
    val magicalCategory = ResourceLocation("spectrum", "magical_blocks")
    val equipCategory = ResourceLocation("spectrum", "equipment")
    val commonDesc = "book.spectrumstorage.added_by_spectrumstorage"

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
    def entryLoc(path: String): EntryLocation =
      EntryLocation(spectrumBook, ResourceLocation("spectrum", path))

    def firstPage(item: ItemLike, text: String): BookSpotlightPageModel =
      BookSpotlightPageModel
        .builder()
        .withText(text)
        .withItem(Ingredient.of(item))
        .withTitle(item.asItem().getDescriptionId)
        .build()

    def pedestalPage(recipe: String, text: String): BookPedestalPageModel =
      BookPedestalPageModel
        .Builder()
        .withText(text)
        .withRecipeId(recipe)
        .withTitle("container.spectrum.rei.pedestal_recipe")
        .build()

    def commonEntry(item: ItemLike, entry: BookEntryModel)(firstTxt: String): BookEntryModel =
      entry
        .withName(item.asItem().getDescriptionId)
        .withDescription(commonDesc)
        .withIcon(item)
        .hideWhileLocked(true)
        .withPage(
          firstPage(item, firstTxt)
        )

    val powerVStack =
      ItemStack(SpectrumStorageItems.bottomlessBottle)
    powerVStack.enchant(Enchantments.POWER_ARROWS, 5)

    InfoCollector
      .instance
      .addGuidebookEntry(entryLoc("equipment/bottomless_bottle"), equipCategory, SpectrumStorageItems.bottomlessBottle.location) {
        (trans, entry) =>
          commonEntry(SpectrumStorageItems.bottomlessBottle, entry)(
            trans(
              """
                |I find myself often carrying half a dozen buckets holding the same thing. It would be nice if I could just cram all that liquid in one item.
                |
                |The bottomless bottle does just that - it stores a large amount of liquid inside itself.
                |""".stripMargin
            )
          )
            .withLocation(3, 4)
            .withCondition(itemUnlock("bottomless_bottle"))
            .withPage(
              pedestalPage(
                "spectrumstorage:pedestal/tier2/bottomless_bottle",
                trans("Right-click picks up liquid, while sneak right-click places it.")
              )
            )
            .withPage(
              BookNbtSpotlightPageModel
                .Builder()
                .withTitle("enchantment.minecraft.power")
                .withCondition(BookAdvancementConditionModel.builder().withAdvancementId("spectrum:midgame/build_enchanting_structure").build())
                .withItem(ItemVariant.of(powerVStack))
                .withText(trans("Power increases its capacity eightfold each level."))
                .build()
            )
      }
      .addGuidebookEntry(entryLoc("equipment/tool_container"), equipCategory, SpectrumStorageItems.toolContainer.location) {
        (trans, entry) =>
          entry
            .withName(SpectrumStorageItems.toolContainer.getDescriptionId)
            .withLocation(4, 4)
            .withDescription(commonDesc)
            .withCondition(itemUnlock("tool_container"))
            .withIcon(SpectrumStorageItems.toolContainer)
            .hideWhileLocked(true)
            .withPage(
              firstPage(
                SpectrumStorageItems.toolContainer,
                trans(
                  """
                    |On my travels I find myself carrying half a tool shed with me.
                    |
                    |I can mitigate this somewhat by making a tool container to keep all my tools in.
                    |""".stripMargin
                ))
            )
            .withPage(
              pedestalPage("spectrumstorage:pedestal/tier2/tool_container",
                trans("*Don't ask how they all fit*"))
            )
      }
      .addGuidebookEntry(entryLoc("magical_blocks/bottomless_amphora"), magicalCategory, SpectrumStorageBlocks.bottomlessAmphora.location) {
        (trans, entry) =>
          entry
            .withName(SpectrumStorageBlocks.bottomlessAmphora.getDescriptionId)
            .withLocation(1, 5)
            .withDescription(commonDesc)
            .withCondition(blockUnlock("bottomless_amphora"))
            .withIcon(SpectrumStorageBlocks.bottomlessAmphora)
            .hideWhileLocked(true)
            .withPage(
              firstPage(SpectrumStorageBlocks.bottomlessAmphora,
                trans(
                  """
                    |I had thought that barrels could store a lot, but amphoras can store double that. So if I make a bottomless amphora...
                    |
                    |The bottomless amphora can store a whopping 54 bottomless bundles or bottles inside of it!
                    |""".stripMargin
                ))
            )
            .withPage(
              pedestalPage("spectrumstorage:pedestal/tier4/bottomless_amphora",
                trans("*This is getting absurd*")
              )
            )
            .withPage(
              BookTextPageModel
                .builder()
                .withText(trans(
                  """
                    |Just like the bottomless shelf and bottomless barrel before it, the bottomless amphora remembers what items and fluids were inside its items, meaning you can use it as a filter.
                    |""".stripMargin
                ))
                .build()
            )
      }
      .addGuidebookEntry(entryLoc("magical_blocks/bottomless_barrel"), magicalCategory, SpectrumStorageBlocks.bottomlessBarrel.location) {
        (trans, entry) =>
          entry
            .withName(SpectrumStorageBlocks.bottomlessBarrel.getDescriptionId)
            .withLocation(0, 5)
            .withDescription(commonDesc)
            .withCondition(blockUnlock("bottomless_barrel"))
            .withIcon(SpectrumStorageBlocks.bottomlessBarrel)
            .hideWhileLocked(true)
            .withPage(
              firstPage(SpectrumStorageBlocks.bottomlessBarrel,
                trans(
                  """
                    |While bottomless shelves are great, I find myself making complicated systems to access many of them.
                    |It would be nice if I could compact them even more.
                    |
                    |The bottomless barrel does just that - it can hold up to 27 bottomless bundles or bottles.
                    |""".stripMargin
                )
              )
            ).withPage(
              pedestalPage(
                "spectrumstorage:pedestal/tier3/bottomless_barrel",
                trans("*That's a lot of stuff*")
              )
            ).withPage(
              BookTextPageModel
                .builder()
                .withText(
                  trans("""
                          |Just like the bottomless shelf, the bottomless barrel will remember what items and fluids were inside its items, meaning you can use it as a filter
                          |""".stripMargin)
                )
                .build()
            )
      }
      .addGuidebookEntry(entryLoc("magical_blocks/bottomless_shelf"), magicalCategory, SpectrumStorageBlocks.bottomlessShelf.location) {
        (trans, entry) =>
          commonEntry(SpectrumStorageBlocks.bottomlessShelf, entry)(
            trans(
              """
                |My bottomless bottle seems to be too unstable to place down on its own - So why not put it on a shelf?
                |
                |The bottomless shelf can hold 6 bottomless bundles or 6 bottomless bundles, or a mix of them.
                |""".stripMargin
            )
          )
            .withLocation(1, 4)
            .withCondition(blockUnlock("bottomless_shelf"))
            .withPage(
              pedestalPage(
                "spectrumstorage:pedestal/tier2/bottomless_shelf",
                trans("*Don't be shelfish!*")
              )
            )
            .withPage(
              BookTextPageModel
                .builder()
                .withText(
                  trans(
                    """
                      |A bottomless shelf will remember what items and fluids were inside its items, meaning you can use it as a filter.
                      |""".stripMargin
                  )
                )
                .build()
            )
      }
      .addGuidebookEntry(entryLoc("magical_blocks/filter_chest"), magicalCategory, SpectrumStorageBlocks.filterChest.location) {
        (trans, entry) =>
          commonEntry(SpectrumStorageBlocks.filterChest, entry)(
            trans(
              """
                |The machines I build to sort my vast catalog of items tend to get quite large. It would be nice to have something that easily filters them.
                |
                |The filter barrel can filter up to 18 unique items, but it can only hold 9 stacks of items. It will only allow insertion of items if they are in its filter.
                |""".stripMargin
            )
          )
            .withLocation(0, 4)
            .withCondition(blockUnlock("filter_chest"))
            .withPage(
              pedestalPage(
                "spectrumstorage:pedestal/tier3/filter_chest",
                trans("*Filter? I hardly know her!*")
              )
            )
      }