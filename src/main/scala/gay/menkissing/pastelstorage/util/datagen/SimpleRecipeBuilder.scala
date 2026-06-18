package gay.menkissing.pastelstorage.util.datagen

import net.minecraft.advancements.Criterion
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.world.item.{Item, ItemStack}

import javax.annotation.Nullable
import scala.collection.mutable

abstract class SimpleRecipeBuilder(protected val result: ItemStack)
  extends RecipeBuilder {
  final protected val criteria = mutable.LinkedHashMap.empty[String, Criterion[?]]
  protected var storedGroup: Option[String] = None

  // This method adds a criterion for the recipe advancement.
  override def unlockedBy(name: String, criterion: Criterion[?]): this.type = {
    this.criteria.put(name, criterion)
    this
  }

  // This method adds a recipe book group. If you do not want to use recipe book groups,
  // remove the this.group field and make this method no-op (i.e. return this).
  override def group(@Nullable group: String): this.type = {
    this.storedGroup = Option(group)
    this
  }

  // Vanilla wants an Item here, not an ItemStack. You still can and should use the ItemStack
  // for serializing the recipes.
  def getResult: Item = this.result.getItem
}