package gay.menkissing.pastelstorage.datagen.providers

import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel
import com.klikli_dev.modonomicon.api.datagen.book.condition.BookAdvancementConditionModel
import com.klikli_dev.modonomicon.api.datagen.book.page.{BookSpotlightPageModel, BookTextPageModel}
import earth.terrarium.pastel.registries.{PastelBlocks, PastelEnchantments, PastelItems}
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.{PastelStorageBlocks, PastelStorageItems}
import gay.menkissing.pastelstorage.registries.PastelStorageTranslationKeys
import gay.menkissing.pastelstorage.util.registry.book.{BookEntry, EntryLocation}
import gay.menkissing.pastelstorage.util.registry.provider.generators.PastelStorageBaseBookProvider
import gay.menkissing.pastelstorage.util.registry.provider.generators.book.BookPedestalPageModel
import gay.menkissing.pastelstorage.util.resources.{*, given}
import net.minecraft.core.registries.Registries
import net.minecraft.core.{Holder, HolderLookup, HolderOwner}
import net.minecraft.data.PackOutput
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.{Item, ItemStack, Items}
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.enchantment.{Enchantment, Enchantments, ItemEnchantments}
import net.minecraft.world.level.ItemLike

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

final class PastelStorageBook(output: PackOutput, lookup: CompletableFuture[HolderLookup.Provider], clientActive: Boolean, serverActive: Boolean) extends PastelStorageBaseBookProvider(PastelStorage.ModId, output, lookup, clientActive, serverActive):
  def addTooltip(item: Supplier[? <: Item], sub: String, value: String): Unit =
    val it = item.get()
    val key = s"${it.getDescriptionId}.tooltip.$sub"
    add(key, value)

  override def addTranslations(): Unit =
    addBlock(PastelStorageBlocks.bottomlessBarrel, "Bottomless Barrel")
    addBlock(PastelStorageBlocks.bottomlessAmphora, "Bottomless Amphora")
    addBlock(PastelStorageBlocks.filterChest, "Filter Chest")
    addBlock(PastelStorageBlocks.bottomlessShelf, "Bottomless Shelf")

    addItem(PastelStorageItems.bottomlessBottle, "Bottomless Bottle")
    addTooltip(PastelStorageItems.bottomlessBottle, "empty", "Empty")
    addTooltip(PastelStorageItems.bottomlessBottle, "usage_pickup", "Use to pickup")
    addTooltip(PastelStorageItems.bottomlessBottle, "usage_place", "Sneak-use to place")
    addTooltip(PastelStorageItems.bottomlessBottle, "count_mb", "%1$s mB / %2$s buckets")
    addItem(PastelStorageItems.toolContainer, "Tool Container")

    add(PastelStorageTranslationKeys.keys.addedByPastelStorage, "§oAdded by Pastel Storage")
    add(PastelStorageTranslationKeys.keys.voidWithLava, "Voiding with Lava")

    List(
      "bottomless_barrel" -> "Bottomless Barrel",
      "bottomless_amphora" -> "Bottomless Amphora",
      "filter_chest" -> "Filter Chest"
    ).foreach: (k, v) =>
      add(s"container.pastelstorage.$k", v)
      
    add("tag.item.pastelstorage.valid_tools", "Valid Tools")



  override def addEntries(lookup: HolderLookup.Provider): Unit =
    val spectrumBook = ResourceLocation.fromNamespaceAndPath("pastel", "guidebook")
    val magicalCategory = ResourceLocation.fromNamespaceAndPath("pastel", "magical_blocks")
    val equipCategory = ResourceLocation.fromNamespaceAndPath("pastel", "equipment")
    val commonDesc = PastelStorageTranslationKeys.keys.addedByPastelStorage

    def itemUnlock(id: String): BookAdvancementConditionModel =
      BookAdvancementConditionModel
        .create()
        .withAdvancementId(s"pastelstorage:unlocks/items/$id")

    def blockUnlock(id: String): BookAdvancementConditionModel =
      BookAdvancementConditionModel
        .create()
        .withAdvancementId(s"pastelstorage:unlocks/blocks/$id")
    def entryLoc(path: String): EntryLocation =
      EntryLocation(spectrumBook, ResourceLocation.fromNamespaceAndPath("pastel", path))

    def firstPage(item: ItemLike, text: String): BookSpotlightPageModel =
      BookSpotlightPageModel
        .create()
        .withText(text)
        .withItem(Ingredient.of(item))
        .withTitle(item.asItem().getDescriptionId)

    def pedestalPage(recipe: String, text: String): BookPedestalPageModel =
      BookPedestalPageModel
        .create()
        .withText(text)
        .withRecipeId(recipe)
        .withTitle("container.pastel.rei.pedestal_recipe")

    def commonEntry(item: ItemLike, entry: BookEntry)(firstTxt: String): BookEntry =
      entry
        .withName(item.asItem().getDescriptionId)
        .withDescription(commonDesc)
        .withIcon(item)
        .withHideWhileLocked(true)
        .addPage(
          trans => firstPage(item, trans.text(firstTxt))
        )
    val enchantLookup = lookup.lookupOrThrow(Registries.ENCHANTMENT)

    // HACK : (
    // dummy out this so MAYBE it works
    val powerEnchant = enchantLookup.getOrThrow(Enchantments.POWER)
    val voidingEnchant = enchantLookup.getOrThrow(PastelEnchantments.VOIDING)

    val powerVStack =
      ItemStack(PastelStorageItems.bottomlessBottle.get())
    powerVStack.enchant(powerEnchant, 5)
    val voidingBundle =
      ItemStack(PastelBlocks.BOTTOMLESS_BUNDLE.asItem())
    voidingBundle.enchant(voidingEnchant, 1)

    val buildEnchanter = BookAdvancementConditionModel.create().withAdvancementId("pastel:midgame/build_enchanting_structure")

    def voidingPage(text: String): BookSpotlightPageModel =
      BookSpotlightPageModel
        .create()
        .withCondition(buildEnchanter)
        .withItem(voidingBundle)
        .withTitle("enchantment.pastel.voiding")
        .withText(
          text
        )

    def voidingLavaPage(text: String): BookSpotlightPageModel =
      BookSpotlightPageModel
        .create()
        .withItem(Items.LAVA_BUCKET)
        .withTitle(PastelStorageTranslationKeys.keys.voidWithLava)
        .withText(text)

    this.addEntry(entryLoc("equipment/bottomless_bottle"), equipCategory, PastelStorageItems.bottomlessBottle.get().location) {
      entry =>
        commonEntry(PastelStorageItems.bottomlessBottle, entry)(
          """
            |I find myself often carrying half a dozen buckets holding the same thing. It would be nice if I could just cram all that liquid in one item.
            |
            |The bottomless bottle does just that - it stores a large amount of liquid inside itself.
            |""".stripMargin
        )
          .withLocation(3, 4)
          .withCondition(itemUnlock("bottomless_bottle"))
          .addPage(trans =>
            pedestalPage(
              "pastelstorage:pedestal/tier2/bottomless_bottle",
              trans.text("Right-click picks up liquid, while sneak right-click places it.")
            )
          )
          .addPage(trans =>
            BookSpotlightPageModel
              .create()
              .withTitle("enchantment.minecraft.power")
              .withCondition(buildEnchanter)
              .withItem(powerVStack)
              .withText(trans.text("Power increases its capacity fourfold each level."))
          )
    }
    this.addEntry(entryLoc("equipment/tool_container"), equipCategory, PastelStorageItems.toolContainer.get().location) {
        entry =>
          entry
            .withName(PastelStorageItems.toolContainer.get().getDescriptionId)
            .withLocation(4, 4)
            .withDescription(commonDesc)
            .withCondition(itemUnlock("tool_container"))
            .withIcon(PastelStorageItems.toolContainer.get())
            .withHideWhileLocked(true)
            .addPage(trans =>
              firstPage(
                PastelStorageItems.toolContainer.get(),
                trans.text(
                  """
                    |On my travels I find myself carrying half a tool shed with me.
                    |
                    |I can mitigate this somewhat by making a tool container to keep all my tools in.
                    |""".stripMargin
                ))
            )
            .addPage(trans =>
              pedestalPage("pastelstorage:pedestal/tier2/tool_container",
                trans.text("*Don't ask how they all fit*"))
            )
      }
    this.addEntry(entryLoc("magical_blocks/bottomless_amphora"), magicalCategory, PastelStorageBlocks.bottomlessAmphora.get().location) {
        entry =>
          entry
            .withName(PastelStorageBlocks.bottomlessAmphora.get().getDescriptionId)
            .withLocation(1, 5)
            .withDescription(commonDesc)
            .withCondition(blockUnlock("bottomless_amphora"))
            .withIcon(PastelStorageBlocks.bottomlessAmphora.get())
            .withHideWhileLocked(true)
            .addPage(trans =>
              firstPage(PastelStorageBlocks.bottomlessAmphora.get(),
                trans.text(
                  """
                    |I had thought that barrels could store a lot, but amphoras can store double that. So if I make a bottomless amphora...
                    |
                    |The bottomless amphora can store a whopping 54 bottomless bundles or bottles inside of it!
                    |""".stripMargin
                ))
            )
            .addPage(trans =>
              pedestalPage("pastelstorage:pedestal/tier4/bottomless_amphora",
                trans.text("*This is getting absurd*")
              )
            )
            .addPage(trans =>
              BookTextPageModel
                .create()
                .withText(trans.text(
                  """
                    |Just like the bottomless shelf and bottomless barrel before it, the bottomless amphora remembers what items and fluids were inside its items, meaning you can use it as a filter.
                    |""".stripMargin
                ))
            ).addPage(trans =>
              voidingPage(
                trans.text(
                  """
                    |Like the bottomless shelf and bottomless barrel, if I insert a bottomless bundle that has the Curse of the Void into
                    |my bottomless amphora, it will still keep its filter and won't accept any
                    |arbitrary items. Any items inserted that overflow the bundle will be voided.
                    |""".stripMargin.linesIterator.mkString(" ")
                )
              )
            )
            .addPage(trans =>
              voidingLavaPage(
                trans.text(
                  """
                    |Like with the bottomless barrel, I can place a lava bucket into my amphora to void any excess items. 
                    |
                    |Be careful, if it's placed before an actual bundle or bottle it will end up voiding items and fluids before they can be stored!
                    |There may be other items that work in my amphora... maybe I should try placing some sharp items in it!
                    |""".stripMargin
                )
              )
            )
      }
    this.addEntry(entryLoc("magical_blocks/bottomless_barrel"), magicalCategory, PastelStorageBlocks.bottomlessBarrel.get().location) {
        entry =>
          entry
            .withName(PastelStorageBlocks.bottomlessBarrel.get().getDescriptionId)
            .withLocation(0, 5)
            .withDescription(commonDesc)
            .withCondition(blockUnlock("bottomless_barrel"))
            .withIcon(PastelStorageBlocks.bottomlessBarrel.get())
            .withHideWhileLocked(true)
            .addPage(trans =>
              firstPage(PastelStorageBlocks.bottomlessBarrel.get(),
                trans.text(
                  """
                    |While bottomless shelves are great, I find myself making complicated systems to access many of them.
                    |It would be nice if I could compact them even more.
                    |
                    |The bottomless barrel does just that - it can hold up to 27 bottomless bundles or bottles.
                    |""".stripMargin
                )
              )
            ).addPage(trans =>
              pedestalPage(
                "pastelstorage:pedestal/tier3/bottomless_barrel",
                trans.text("*That's a lot of stuff*")
              )
            ).addPage(trans =>
              BookTextPageModel
                .create()
                .withText(
                  trans.text("""
                          |Just like the bottomless shelf, the bottomless barrel will remember what items and fluids were inside its items, meaning you can use it as a filter
                          |""".stripMargin)
                )
            )
            .addPage(trans =>
              voidingPage(
                  trans.text(
                    """
                      |Like the bottomless shelf, if I insert a bottomless bundle that has the Curse of the Void into
                      |my bottomless barrel, it will still keep its filter and won't accept any
                      |arbitrary items. Any items inserted that overflow the bundle will be voided.
                      |""".stripMargin.linesIterator.mkString(" ")
                    // ^ im not having a 200 character long line
                  )
                )
            )
            .addPage(trans =>
              voidingLavaPage(
                trans.text(
                  """
                    |With the ability to open my barrels inventory, I can now put a lava bucket into it to void any excess items.
                    |
                    |Be careful, if it's placed before an actual bundle or bottle it will end up voiding items and fluids before they can be stored!
                    |""".stripMargin
                )
              )
            )
      }
    this.addEntry(entryLoc("magical_blocks/bottomless_shelf"), magicalCategory, PastelStorageBlocks.bottomlessShelf.get().location) {
        entry =>
          commonEntry(PastelStorageBlocks.bottomlessShelf.get(), entry)(
              """
                |My bottomless bottle seems to be too unstable to place down on its own - So why not put it on a shelf?
                |
                |The bottomless shelf can hold 6 bottomless bundles or 6 bottomless bundles, or a mix of them.
                |""".stripMargin
          )
            .withLocation(1, 4)
            .withCondition(blockUnlock("bottomless_shelf"))
            .addPage(trans =>
              pedestalPage(
                "pastelstorage:pedestal/tier2/bottomless_shelf",
                trans.text("*Don't be shelfish!*")
              )
            )
            .addPage(trans =>
              BookTextPageModel
                .create()
                .withText(
                  trans.text(
                    """
                      |A bottomless shelf will remember what items and fluids were inside its items, meaning you can use it as a filter.
                      |""".stripMargin
                  )
                )
            )
            .addPage(trans =>
              voidingPage(
                trans.text(
                  """
                    |If I insert a bottomless bundle that has the Curse of the Void into
                    |my bottomless shelf, it will still keep its filter and won't accept any
                    |arbitrary items. Any items inserted that overflow the bundle will be voided.
                    |""".stripMargin.linesIterator.mkString(" ")
                    // ^ im not having a 200 character long line
                )
              )
            )
      }
    this.addEntry(entryLoc("magical_blocks/filter_chest"), magicalCategory, PastelStorageBlocks.filterChest.get().location) {
        entry =>
          commonEntry(PastelStorageBlocks.filterChest.get(), entry)(
            """
              |The machines I build to sort my vast catalog of items tend to get quite large. It would be nice to have something that easily filters them.
              |
              |The filter barrel can filter up to 18 unique items, but it can only hold 9 stacks of items. It will only allow insertion of items if they are in its filter.
              |""".stripMargin
          )
          .withLocation(0, 4)
          .withCondition(blockUnlock("filter_chest"))
          .addPage(trans =>
            pedestalPage(
              "pastelstorage:pedestal/tier3/filter_chest",
              trans.text("*Filter? I hardly know her!*")
            )
          )
      }
