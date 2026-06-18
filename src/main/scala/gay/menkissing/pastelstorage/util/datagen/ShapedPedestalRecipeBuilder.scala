package gay.menkissing.pastelstorage.util.datagen

import earth.terrarium.pastel.api.item.GemstoneColor
import earth.terrarium.pastel.api.recipe.IngredientStack
import earth.terrarium.pastel.recipe.pedestal.RawShapedPedestalRecipe
import earth.terrarium.pastel.recipe.pedestal.ShapedPedestalRecipe
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.{Item, ItemStack}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

class ShapedPedestalRecipeBuilder(result: ItemStack) extends PedestalRecipeBuilder(result):
  protected var rows: mutable.ListBuffer[String] = mutable.ListBuffer.empty[String]
  protected var keyMap: mutable.HashMap[Char, IngredientStack] = mutable.HashMap.empty

  def pattern(row: String): this.type =
    if rows.nonEmpty && row.length != rows.head.length then
      throw new IllegalArgumentException("All patterns must be the same length!")
    else
      rows.append(row)
    this

  def key(c: Char, v: IngredientStack): this.type =
    keyMap(c) = v
    this
    
  def key(c: Char, v: Item): this.type =
    key(c, IngredientStack.ofItems(v))

  override def save(output: RecipeOutput, id: ResourceLocation): Unit =
    val rawDoggie = RawShapedPedestalRecipe.create(keyMap.asJava.asInstanceOf[java.util.Map[Character, IngredientStack]], rows.asJava)
    val realRecipe =
      ShapedPedestalRecipe(
        this.storedGroup.getOrElse(""),
        this.isSecret,
        this.theRequiredAdvancementIdentifier.toJava,
        this.tier,
        rawDoggie,
        this.powderInputs.asJava.asInstanceOf[java.util.Map[GemstoneColor, Integer]],
        this.result,
        this.xp,
        this.craftingTime,
        this.skipRemainders,
        this.ignoreYieldUpgrades
      )
    
    output.accept(id, realRecipe, null)

