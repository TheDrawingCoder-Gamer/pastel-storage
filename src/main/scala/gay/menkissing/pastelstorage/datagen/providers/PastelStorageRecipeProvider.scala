package gay.menkissing.pastelstorage.datagen.providers

import earth.terrarium.pastel.api.item.GemstoneColor
import earth.terrarium.pastel.api.recipe.IngredientStack
import earth.terrarium.pastel.recipe.pedestal.{PastelGemstoneColor, PedestalTier}
import earth.terrarium.pastel.registries.PastelItems
import gay.menkissing.pastelstorage.util.datagen.ShapedPedestalRecipeBuilder
import gay.menkissing.pastelstorage.content.PastelStorageBlocks
import gay.menkissing.pastelstorage.PastelStorage
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.{RecipeOutput, RecipeProvider}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

import java.util.concurrent.CompletableFuture

class PastelStorageRecipeProvider(output: PackOutput, lookup: CompletableFuture[HolderLookup.Provider]) extends RecipeProvider(output, lookup):
  override def buildRecipes(output: RecipeOutput): Unit =
    ShapedPedestalRecipeBuilder(ItemStack(PastelStorageBlocks.bottomlessWorm.get()))
      .tier(PedestalTier.ADVANCED)
      .craftingTime(240)
      .withPowderInput(PastelGemstoneColor.CYAN, 6)
      .withPowderInput(PastelGemstoneColor.BLACK, 2)
      .experience(2.0)
      .pattern("QTQ")
      .pattern("S S")
      .pattern("CQC")
      .key('Q', IngredientStack.ofItems(PastelItems.QUITOXIC_POWDER.get()))
      .key('S', IngredientStack.ofItems(PastelItems.STRATINE_FRAGMENTS.get()))
      .key('C', IngredientStack.ofItems(PastelItems.CYAN_PIGMENT.get()))
      .key('T', IngredientStack.ofItems(PastelItems.STORM_STONE.get()))
      .requiredAdvancement(PastelStorage.locate("unlocks/blocks/bottomless_worm"))
      .save(output, PastelStorage.locate("pedestal/tier3/bottomless_worm"))
