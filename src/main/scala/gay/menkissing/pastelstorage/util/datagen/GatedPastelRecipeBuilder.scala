package gay.menkissing.pastelstorage.util.datagen

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

abstract class GatedPastelRecipeBuilder(result: ItemStack) extends SimpleRecipeBuilder(result):
  protected var isSecret: Boolean = false
  protected var theRequiredAdvancementIdentifier: Option[ResourceLocation] = None
  
  def secret(v: Boolean): this.type =
    isSecret = v
    this
  
  def requiredAdvancement(loc: ResourceLocation): this.type =
    theRequiredAdvancementIdentifier = Option(loc)
    this
    
    
  
