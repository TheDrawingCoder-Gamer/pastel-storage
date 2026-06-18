package gay.menkissing.pastelstorage.util.datagen

import earth.terrarium.pastel.api.item.GemstoneColor
import earth.terrarium.pastel.recipe.pedestal.PedestalTier
import net.minecraft.world.item.ItemStack

import scala.collection.mutable

abstract class PedestalRecipeBuilder(result: ItemStack) extends GatedPastelRecipeBuilder(result):
  protected var tier: PedestalTier = null
  protected var craftingTime: Int = 200
  protected var ignoreYieldUpgrades: Boolean = false
  protected var skipRemainders: Boolean = false
  protected var powderInputs = mutable.HashMap.empty[GemstoneColor, Int]
  protected var xp: Float = 0
  
  def tier(value: PedestalTier): this.type =
    this.tier = value
    this
  
  def craftingTime(value: Int): this.type =
    this.craftingTime = value
    this
  
  def ignoreYieldUpgrades(v: Boolean): this.type =
    this.ignoreYieldUpgrades = v
    this
  
  def skipRemainders(v: Boolean): this.type =
    this.skipRemainders = v
    this
  
  def withPowderInput(color: GemstoneColor, value: Int): this.type =
    powderInputs(color) = value
    this
  
  def experience(value: Float): this.type =
    this.xp = value
    this
