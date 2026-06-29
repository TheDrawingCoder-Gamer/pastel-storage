package gay.menkissing.pastelstorage.datagen.providers

import earth.terrarium.pastel.api.item.GemstoneColor
import earth.terrarium.pastel.api.recipe.IngredientStack
import earth.terrarium.pastel.recipe.pedestal.{PastelGemstoneColor, PedestalTier}
import earth.terrarium.pastel.registries.{PastelBlocks, PastelItemTags, PastelItems}
import gay.menkissing.pastelstorage.util.datagen.ShapedPedestalRecipeBuilder
import gay.menkissing.pastelstorage.content.{PastelStorageBlocks, PastelStorageItems}
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.registries.ids.PastelStorageAdvancementIds
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.{RecipeOutput, RecipeProvider}
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.{ItemTags, TagKey}
import net.minecraft.world.item.{ItemStack, Items}
import net.minecraft.world.level.block.Blocks
import net.neoforged.neoforge.registries.DeferredHolder

import java.util.concurrent.CompletableFuture

class PastelStorageRecipeProvider(output: PackOutput, lookup: CompletableFuture[HolderLookup.Provider]) extends RecipeProvider(output, lookup):
  import PastelStorageRecipeProvider.pedestalRecipeLocation

  override def buildRecipes(output: RecipeOutput): Unit =
    // SIMPLE
    ShapedPedestalRecipeBuilder(ItemStack(PastelStorageItems.toolContainer.get()))
      .tier(PedestalTier.SIMPLE)
      .craftingTime(240)
      .withPowderInput(PastelGemstoneColor.CYAN, 8)
      .experience(2.0)
      .pattern("QGQ")
      .pattern("ICI")
      .pattern("QIQ")
      .key('Q', PastelItems.QUITOXIC_POWDER.get())
      .key('I', Items.IRON_INGOT)
      .key('G', Items.GOLD_INGOT)
      .key('C', Items.CHEST)
      .requiredAdvancement(PastelStorageAdvancementIds.toolContainerUnlock)
      .save(output, pedestalRecipeLocation(PastelStorageItems.toolContainer, PedestalTier.SIMPLE))

    ShapedPedestalRecipeBuilder(ItemStack(PastelStorageItems.bottomlessBottle.get()))
      .tier(PedestalTier.SIMPLE)
      .craftingTime(240)
      .withPowderInput(PastelGemstoneColor.CYAN, 8)
      .experience(2.0)
      .pattern("Q Q")
      .pattern("GQG")
      .pattern(" G ")
      .key('Q', PastelItems.QUITOXIC_POWDER.get())
      .key('G', Items.GLASS)
      .requiredAdvancement(PastelStorageAdvancementIds.bottomlessBottleUnlock)
      .save(output, pedestalRecipeLocation(PastelStorageItems.bottomlessBottle, PedestalTier.SIMPLE))

    ShapedPedestalRecipeBuilder(ItemStack(PastelStorageBlocks.bottomlessShelf.get()))
      .tier(PedestalTier.SIMPLE)
      .craftingTime(240)
      .withPowderInput(PastelGemstoneColor.CYAN, 4)
      .experience(2.0)
      .pattern("WWW")
      .pattern("QQQ")
      .pattern("WWW")
      .key('Q', PastelItems.QUITOXIC_POWDER.get())
      .key('W', IngredientStack.ofTag(ItemTags.PLANKS))
      .requiredAdvancement(PastelStorageAdvancementIds.bottomlessShelfUnlock)
      .save(output, pedestalRecipeLocation(PastelStorageBlocks.bottomlessShelf, PedestalTier.SIMPLE))

    // ADVANCED
    ShapedPedestalRecipeBuilder(ItemStack(PastelStorageBlocks.bottomlessWorm.get(), 2))
      .tier(PedestalTier.ADVANCED)
      .craftingTime(240)
      .withPowderInput(PastelGemstoneColor.CYAN, 12)
      .withPowderInput(PastelGemstoneColor.BLACK, 4)
      .experience(2.0)
      .pattern("QTQ")
      .pattern("S S")
      .pattern("CQC")
      .key('Q', IngredientStack.ofItems(PastelItems.QUITOXIC_POWDER.get()))
      .key('S', IngredientStack.ofItems(PastelItems.STRATINE_FRAGMENTS.get()))
      .key('C', IngredientStack.ofItems(PastelItems.CYAN_PIGMENT.get()))
      .key('T', IngredientStack.ofItems(PastelItems.STORM_STONE.get()))
      .requiredAdvancement(PastelStorageAdvancementIds.bottomlessWormUnlock)
      .save(output, pedestalRecipeLocation(PastelStorageBlocks.bottomlessWorm, PedestalTier.ADVANCED))

    ShapedPedestalRecipeBuilder(ItemStack(PastelStorageBlocks.bottomlessBarrel.get()))
      .tier(PedestalTier.ADVANCED)
      .craftingTime(240)
      .withPowderInput(PastelGemstoneColor.CYAN, 4)
      .withPowderInput(PastelGemstoneColor.BLACK, 2)
      .experience(2.0)
      .pattern("QCQ")
      .pattern("SBS")
      .pattern("CQC")
      .key('Q', PastelItems.QUITOXIC_POWDER.get())
      .key('S', PastelItems.STRATINE_FRAGMENTS.get())
      .key('B', Blocks.BARREL.asItem())
      .key('C', PastelItems.CYAN_PIGMENT.get())
      .requiredAdvancement(PastelStorageAdvancementIds.bottomlessBarrelUnlock)
      .save(output, pedestalRecipeLocation(PastelStorageBlocks.bottomlessBarrel, PedestalTier.ADVANCED))

    ShapedPedestalRecipeBuilder(ItemStack(PastelStorageBlocks.filterChest.get()))
      .tier(PedestalTier.ADVANCED)
      .craftingTime(240)
      .withPowderInput(PastelGemstoneColor.YELLOW, 2)
      .withPowderInput(PastelGemstoneColor.CYAN, 2)
      .withPowderInput(PastelGemstoneColor.BLACK, 1)
      .experience(2.0)
      .pattern("IPI")
      .pattern("W W")
      .pattern("AAA")
      .key('W', IngredientStack.ofTag(ItemTags.WOOL))
      .key('I', Items.IRON_INGOT)
      .key('P', PastelItems.BLUE_PIGMENT.get())
      .key('A', PastelBlocks.POLISHED_BASALT.asItem())
      .requiredAdvancement(PastelStorageAdvancementIds.filterChestUnlock)
      .save(output, pedestalRecipeLocation(PastelStorageBlocks.filterChest, PedestalTier.ADVANCED))

    ShapedPedestalRecipeBuilder(ItemStack(PastelStorageItems.bottomlessBattery.get()))
      .tier(PedestalTier.ADVANCED)
      .craftingTime(240)
      .withPowderInput(PastelGemstoneColor.YELLOW, 4)
      .withPowderInput(PastelGemstoneColor.CYAN, 6)
      .withPowderInput(PastelGemstoneColor.BLACK, 1)
      .experience(2.0)
      .pattern("QSQ")
      .pattern("CQC")
      .pattern("YYY")
      .key('Q', PastelItems.QUITOXIC_POWDER.get())
      .key('S', PastelItems.STORM_STONE.get())
      .key('C', Items.COPPER_INGOT)
      .key('Y', PastelItems.YELLOW_PIGMENT.get())
      .requiredAdvancement(PastelStorageAdvancementIds.bottomlessBatteryUnlock)
      .save(output, pedestalRecipeLocation(PastelStorageItems.bottomlessBattery, PedestalTier.ADVANCED))
    
    // COMPLEX
    ShapedPedestalRecipeBuilder(ItemStack(PastelStorageBlocks.bottomlessAmphora.get()))
      .tier(PedestalTier.COMPLEX)
      .craftingTime(240)
      .withPowderInput(PastelGemstoneColor.CYAN, 6)
      .withPowderInput(PastelGemstoneColor.BLACK, 2)
      .withPowderInput(PastelGemstoneColor.WHITE, 2)
      .experience(2.0)
      .pattern("CSC")
      .pattern("QAQ")
      .pattern("QCQ")
      .key('Q', PastelItems.QUITOXIC_POWDER.get())
      .key('S', PastelItems.STRATINE_GEM.get())
      .key('A', IngredientStack.ofTag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("pastel", "noxwood_amphoras"))))
      .key('C', PastelItems.CYAN_PIGMENT.get())
      .requiredAdvancement(PastelStorageAdvancementIds.bottomlessAmphoraUnlock)
      .save(output, pedestalRecipeLocation(PastelStorageBlocks.bottomlessAmphora, PedestalTier.COMPLEX))


object PastelStorageRecipeProvider:
  def pedestalRecipeLocation(holder: DeferredHolder[?, ?], tier: PedestalTier): ResourceLocation =
    val id = holder.getId
    val tierId =
      tier match
        case PedestalTier.BASIC => "tier1"
        case PedestalTier.SIMPLE => "tier2"
        case PedestalTier.ADVANCED => "tier3"
        case PedestalTier.COMPLEX => "tier4"
    ResourceLocation.fromNamespaceAndPath(id.getNamespace, s"pedestal/$tierId/${id.getPath}")